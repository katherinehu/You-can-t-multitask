package com.wave.sbauction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class RetrieveData extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();
    //private NoteViewModel noteViewModel;;

    TextView tvLoading, tvNerdInfo;
    Button btnGoWithData, btnRedoData;

    static long[] timesRetrieved;
    static boolean firstPageRetrieved = false;
    static int totalAuctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_data);

        setTitle("Load Auction Information");

        //regionDeclare UI elements
        tvLoading = findViewById(R.id.tvLoadingNotification);
        tvNerdInfo = findViewById(R.id.tvNerdInfo);
        btnGoWithData = findViewById(R.id.btnGoWithData);
        btnRedoData = findViewById(R.id.btnRedoData);
        //endregion


        //Inform user what is happening
        Toast.makeText(getApplicationContext(), "Contacting Hypixel API...", Toast.LENGTH_SHORT).show();

        //regionStart a thread so the UI shows up while data retrieval and storage runs -P
        new Thread(){
            @Override
            public void run() {
                super.run();

                //regionGet first page of data, and use it to figure out how many pages we need. -P
                final String auctionURL = "https://api.hypixel.net/skyblock/auctions?page=";
                String firstPage = null;
                try {
                    firstPage = new RetrieveAuctions().execute(auctionURL + "0", "0", "(waiting for Hypixel to say exactly how many)").get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Store the auction info from the first page, use this later for the final set as well
                JSONObject auctionInfo;
                //Store the time retrieved
                long timeUpdated = 0;
                //Find out how many pages we need to eventually retrieve
                int totalPages = 0;
                //Total number of auctions
                try {
                    auctionInfo = new JSONObject(firstPage);
                    timeUpdated = auctionInfo.getLong("lastUpdated");
                    totalPages = auctionInfo.getInt("totalPages");
                    totalAuctions = auctionInfo.getInt("totalAuctions");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Find out how big of an array timesRetrieved needs to be
                timesRetrieved = new long[totalPages + 1];

                //Fill with 0, to make it easier to do later calculations
                for(int i = 0; i < timesRetrieved.length; ++i) {
                    timesRetrieved[0] = 0;
                }
                //Inform the other thread that it's time to start updating
                firstPageRetrieved = true;
                Log.d("DataInformation","First page retrieved");
                timesRetrieved[0] = timeUpdated;

                //regionStore the data -P
                    SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = data.edit();
                    //This informs later processes how many pages there are
                    editor.putInt("totalAuctionPages",totalPages);
                    editor.apply();

                    String filename = "auctionPage0";
                    saveData(filename,firstPage);
                //endregion

                //endregion

                //regionRetrieve the remaining pages -P
                //Starts at 1, because we already retrieved the 0 page as the first page.
                //Also, I checked, and you do need to retrieve the 52nd page if there are say, 52 pages.
                for (int i = 1; i <= totalPages; ++i) {
                    String page = Integer.toString(i);
                    String url = auctionURL + page;
                    new RetrieveAuctions1().execute(url,page);
                }
                //endregion
            }
        }.start();
        //endregion

        //regionThis thread helps to inform the user how far along the data retrieval is coming along -P
        new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                super.run();
                boolean finished = false;
                int arraySize;
                while(!finished){
                    if(firstPageRetrieved){
                        int pagesCompleted = 0;
                        arraySize = timesRetrieved.length;
                        for(int i = 0; i < arraySize; ++i) {
                            if (timesRetrieved[i] > 0){
                                ++pagesCompleted;
                            }
                        }
                        final int pagesCompletedFinal = pagesCompleted;
                        //Update the information so the user knows what's going on
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String loadingNotification;
                                //This part is just here so that the user doesn't get confused when the phone "loads a higher number of auctions than exist"
                                if(pagesCompletedFinal == timesRetrieved.length) {
                                    loadingNotification = "Be patient while your phone gets ALL the auction data.\nLoaded " + totalAuctions + " of " + totalAuctions + " total auctions.";
                                } else {
                                    loadingNotification = "Be patient while your phone gets ALL the auction data.\nLoaded " + (pagesCompletedFinal * 1000) + " of " + totalAuctions + " total auctions.";
                                }
                                tvLoading.setText(loadingNotification);
                            }
                        });
                        if (pagesCompleted == arraySize){
                            //regionCheck to make sure data hasn't changed -P
                            boolean timeChanged = false;
                            for(long element:timesRetrieved){
                                //If any of the times are different, mention it as such
                                if (element != timesRetrieved[0]){
                                    timeChanged = true;
                                    //I mean the break isn't truly necessary because its such a small array, but
                                    //might as well save those few nanoseconds if we can
                                    break;
                                }
                            }
                            if (timeChanged) {
                                //The server has updated since data retrieval has begun, give user option to try again.
                                new ShowFailureButtons().execute();
                                Toast.makeText(getApplicationContext(),"Data retrieval issue.",Toast.LENGTH_SHORT).show();
                            } else {
                                //If no problems found, go back to the auction menu page.
                                Toast.makeText(getApplicationContext(),"All data successfully retrieved.",Toast.LENGTH_SHORT).show();
                                Intent goBack = new Intent(getApplicationContext(),AuctionMainMenu.class);
                                startActivity(goBack);
                            }
                            //endregion
                            finished = true;
                        }
                        Log.d("DataInformation","pagesCompleted: " + pagesCompleted + " arraySize: " + arraySize);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Wait for results
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
        //endregion

        //region Buttons used for data problems -P
        //first make invisible, will become visible if needed
        btnGoWithData.setVisibility(View.GONE);
        btnRedoData.setVisibility(View.GONE);

        //Restart the activity to see if better results can be obtained
        final Intent restart = new Intent(this,RetrieveData.class);
        btnRedoData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(restart);
            }
        });

        //Even if data isn't perfect, just use it and hope for the best
        final Intent giveUp = new Intent(this,AuctionMainMenu.class);
        btnGoWithData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(giveUp);
            }
        });
        //endregion



        //region Room database creation -k
        //NoteRoomDatabase myDatabase;
    }

    //Save files on the device -P
    public void saveData(String filename,String dataToSave) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(dataToSave.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos!= null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //This code was mostly taken from online, I looked up how to retrieve text from a URL. The API that
    //Hypixel uses has a website that has everything needed stored in a text JSON. This just goes there and takes the data
    //in a way that android can handle -P
    public class RetrieveAuctions extends AsyncTask<String, String, String> {

        String totalAuctions = null;
        String currentPage = null;

        protected String doInBackground(String... params) {
            totalAuctions = params[2];
            currentPage = params[1];
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                int currentlyLoaded = Integer.parseInt(currentPage);
                int total = Integer.parseInt(totalAuctions);
                if (total - currentlyLoaded > 1000) {
                    String loadingNotification = "Be patient while your phone gets ALL the auction data.\nLoaded " + currentPage + " of " + totalAuctions + " total auctions.";
                    tvLoading.setText(loadingNotification);
                } else {
                    String loadingNotification = "Be patient while your phone gets ALL the auction data.\nLoaded " + totalAuctions + " of " + totalAuctions + " total auctions." +
                            "\nYour phone is now saving the data. If you see this message for long your phone is really really slow.\n" +
                            "Worry not, you will be redirected back to the app quite soon where you can run fancy analysis.";
                    tvLoading.setText(loadingNotification);
                }
            } catch(Exception e) {
                String loadingNotification = "Establishing contact with Hypixel API...";
                tvLoading.setText(loadingNotification);
            }
        }
    }

    //This class is similar to the other one, but is redesigned to work on not just the first page.
    public class RetrieveAuctions1 extends AsyncTask<String, String, Long> {

        String page;

        protected Long doInBackground(String... params) {
            page = params[1];
            Log.d("DataInformation","started " + page);

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            long timeRetrieved = -1;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                String retrievedData = buffer.toString();
                JSONObject temp = new JSONObject(retrievedData);
                String filename = "auctionPage" + page;
                //Save the data
                saveData(filename,retrievedData);
                //Set the return value
                timeRetrieved = temp.getLong("lastUpdated");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return timeRetrieved;
        }

        @Override
        protected void onPostExecute(Long timeRetrieved) {
            //Update the array from earlier so the user knows how far along the progress is coming
            timesRetrieved[Integer.parseInt(page)] = timeRetrieved;
        }
    }

    //Gives user option to try again, or not, if discrepancy was detected.
    public class ShowFailureButtons extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            btnGoWithData.setVisibility(View.VISIBLE);
            btnRedoData.setVisibility(View.VISIBLE);
            tvNerdInfo.setVisibility(View.GONE);
        }
    }



//    ATTEMPT TO MAKE THIS WORK, THAT DIDN'T PAN OUT, IGNORE IT, UNLESS YOU WANT AN EXAMPLE OF WHAT NOT TO DO -P
//    All of the code that runs in the get data button runs here, so that the user can be alerted to the progress,
//    because you have to retrieve nearly 40 megabytes of data, and that could take a while depending on the connection -P
//    private class StartRetrieveData extends AsyncTask<String,Integer,String> {
//
//        String totalPagesString = "?";
//        String currentPage = "1";
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            tvLoading.setVisibility(View.VISIBLE);
//            tvLoading.setText("Contacting Hypixel API...");
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            //regionRetrieve 1 page of auction data, so we know how many future pages to retrieve. -P
//            String auctionURL = "https://api.hypixel.net/skyblock/auctions?page=";
//            String firstPage = null;
//            try {
//                firstPage = new RetrieveAuctions().execute(auctionURL + "0").get();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            try {
//                auctionInfo = new JSONObject(firstPage);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            //endregion
//
//            //regionRetrieve the remaining pages -P
//            int totalPages = 0;
//            try {
//                totalPages = auctionInfo.getInt("totalPages");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            totalPagesString = Integer.toString(totalPages);
//
//            publishProgress(0);
//
//            //Place to put the rest of the pages
//            ArrayList<String> remainingPages = new ArrayList<>();
//
//            //Starts at 1, because we already retrieved the 0 page as the first page.
//            //Also, I checked, and you do need to retrieve the 52nd page if there are say, 52 pages.
//            for (int i = 1; i <= totalPages; ++i) {
//                currentPage = Integer.toString(i);
//                //Does not actually matter what the progress is, because I found this stupid workaround.
//                publishProgress(0);
//                String newPage = null;
//                try {
//                    newPage = new RetrieveAuctions().execute(auctionURL + i).get();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                remainingPages.add(newPage);
//            }
//            Toast.makeText(getApplicationContext(),"All data received.",Toast.LENGTH_SHORT).show();
//            tvLoading.setVisibility(View.GONE);
//            //endregion
//
//            //regionCombine all pages into one -P
//            for (String page:remainingPages){
//
//            }
//            int e = 3;
//            //endregion
//
//            return null;
//        }
//
//        //This method uses global variables in order to update a progress textview.
//        //Terrible idea, I know, but I went with it.
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            String loadingNotif = "Retrieving page " + currentPage + " of " + totalPagesString;
//            tvLoading.setText(loadingNotif);
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            Toast.makeText(getApplicationContext(),"All data retrieved!",Toast.LENGTH_SHORT).show();
//        }
//    }
}