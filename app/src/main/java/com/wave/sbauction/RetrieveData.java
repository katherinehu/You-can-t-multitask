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
import androidx.room.Room;

import org.json.JSONArray;
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

public class RetrieveData extends AppCompatActivity {

    private String TAG = "DataRetrieval";
    //private NoteViewModel noteViewModel;;

    TextView tvLoading;
    Button btnGoWithData, btnRedoData;
    static boolean[] hasBeenRetrieved;
    static boolean startLooking = false;
    //Total number of auctions
    static String totalAuctions = null;

    static AppDatabase currentAuctionsdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_data);

        setTitle("Load Auction Information");

        currentAuctionsdb = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "CurrentAuctionsDB")
                .build();

        //regionDeclare UI elements
        tvLoading = findViewById(R.id.tvLoadingNotification);
        btnGoWithData = findViewById(R.id.btnGoWithData);
        btnRedoData = findViewById(R.id.btnRedoData);
        //endregion

        //Inform user what is happening
        Toast.makeText(getApplicationContext(), "Contacting Hypixel API...", Toast.LENGTH_SHORT).show();

        //regionStart a thread so the UI shows up while data retrieval and storage runs -P
        final Thread retrieveData = new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                super.run();
                Log.d(TAG,"Data retreival started first page");
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


                //Store the time retrieved
                long timeUpdated = 0;
                //Find out how many pages we need to eventually retrieve
                int totalPages = 0;
                //Store the auction info from the first page
                JSONObject auctionInfo = null;

                try {

                    auctionInfo = new JSONObject(firstPage);
                    timeUpdated = auctionInfo.getLong("lastUpdated");
                    totalPages = auctionInfo.getInt("totalPages");
                    hasBeenRetrieved = new boolean[totalPages+1];
                    for(int i = 0; i<totalPages;++i){
                        hasBeenRetrieved[i] = false;
                    }
                    hasBeenRetrieved[0] = true;
                    startLooking = true;
                    totalAuctions = auctionInfo.getString("totalAuctions");
                    //show the user what's going on
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvLoading.setText("Loaded 1000 of " + totalAuctions + " auctions.");
                        }
                    });
                    //save time first page is retrieved
                    SharedPreferences timeSaved = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = timeSaved.edit();
                    editor.putLong(String.valueOf(timeSaved.getAll().size()), timeUpdated);
                    editor.apply();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //region get data and store it into auction objects -k

                try {
                    JSONArray allAuctions = null;
                    allAuctions = auctionInfo.getJSONArray("auctions");
                    for (int i = 0; i < allAuctions.length();i++) {
                        JSONObject currentAuction;
                        currentAuction = allAuctions.getJSONObject(i);

                        //check if there is a bin, if not, set to false;
                        boolean binFlag;
                        Double highestBid;
                        try{
                            binFlag = currentAuction.getBoolean("bin");
                            highestBid = currentAuction.getDouble("starting_bid");

                        }
                        catch(Exception invalidReq){
                            highestBid = currentAuction.getDouble("highest_bid_amount");
                            binFlag = false;
                        }

                        //add aaa auction items in page to db
                        currentAuctionsdb.AuctionDao()
                                .insertAll(new Auction(
                                        currentAuction.getString("uuid"),
                                        currentAuction.getString("auctioneer"),
                                        currentAuction.getString("profile_id"),
                                        currentAuction.getDouble("start"),
                                        currentAuction.getDouble("end"),
                                        currentAuction.getString("item_name"),
                                        currentAuction.getString("item_lore"),
                                        currentAuction.getString("extra"),
                                        currentAuction.getString("category"),
                                        currentAuction.getString("tier"),
                                        currentAuction.getDouble("starting_bid"),
                                        currentAuction.getBoolean("claimed"),
                                        highestBid,
                                        binFlag
                                        ));
                    }
                    Log.d(TAG,"First page, stuff stored in database");
                } catch (JSONException e) {
                    e.printStackTrace();
                }



//                //Store the data
//                SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                SharedPreferences.Editor editor = data.edit();
//                String storeAuctionInfo = auctionInfo.toString();
//                editor.putInt("totalAuctionPages",totalPages);
//                editor.apply();
//
//                String filename = "auctionPage0";
//                saveData(filename,firstPage);
                //endregion

                //regionRetrieve the remaining pages -P
                //Place to put the last page, since we'll use it later
                String lastPageString = null;
                ArrayList<String> remainingPages = new ArrayList<>();


                //Starts at 1, because we already retrieved the 0 page as the first page.
                //Also, I checked, and you do need to retrieve the 52nd page if there are say, 52 pages.
                for (int i = 1; i <= totalPages; ++i) {
                    Log.d(TAG,"Other pages started");
                    new retrieveDataPerPage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,auctionURL + i,Integer.toString(i));
                    //Store the data
//                        final String filename2 = "auctionPage" + i;
//                        new Thread(){
//                            @Override
//                            public void run() {
//                                super.run();
//                                saveData(filename2, newPage);
//                            }
//                        }.start();
                    //Save the last page to check if the times match up
//                        if(i == totalPages) {
//                            lastPageString = newPage;
//                        }
                }
                //endregion

                //regionCheck to make sure data hasn't changed -P
//                JSONObject lastPage;
//                long timeUpdatedLast = 0;
//                try {
//                    //Get information from the last page of auction data, check to see if the time is the same
//                    lastPage = new JSONObject(lastPageString);
//                    timeUpdatedLast = lastPage.getLong("lastUpdated");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                if (timeUpdatedLast != timeUpdated) {
//                    //The server has updated since data retrieval has begun, give user option to try again.
//                    new ShowFailureButtons().execute();
//                    Toast.makeText(getApplicationContext(),"Data retrieval issue.",Toast.LENGTH_SHORT).show();
//                } else {
//                    //If no problems found, go back to the auction menu page.
//                    Toast.makeText(getApplicationContext(),"All data successfully retrieved.",Toast.LENGTH_SHORT).show();
//                    Intent goBack = new Intent(getApplicationContext(),AuctionMainMenu.class);
//                    startActivity(goBack);
//                }
                //endregion
            }
        };
        //endregion

        //regionThread to clear the old database -k
        new Thread(){
            @Override
            public void run() {
                super.run();
                // clear all items in database
                currentAuctionsdb.clearAllTables();
                boolean notCleared = true;
                while (notCleared) {
                    //check if there are items in the database. if not, leave loop;
                    if (currentAuctionsdb.AuctionDao().getAnAuctionItem() == null) {
                        notCleared = false;
                    }
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                retrieveData.start();
                Log.d(TAG,"database cleared");
            }
        }.start();


        //endregion

        //regionThread to exit activity once all data has been retrieved -P
        final Intent returnToMenu = new Intent(this,AuctionMainMenu.class);
        new Thread(){
            @Override
            public void run() {
                super.run();
                boolean finished = false;
                while(!finished){
                    if (startLooking) {
                        int counter = 1;
                        for (boolean elements : hasBeenRetrieved) {
                            if (elements) {
                                ++counter;
                            }
                        }
                        if (counter == hasBeenRetrieved.length) {
                            finished = true;
                        } else {
                            final int finalCounter = counter;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String display = "Loaded " + (finalCounter * 1000) + " of " + totalAuctions + " auctions.";
                                    tvLoading.setText(display);
                                }
                            });
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.d(TAG,"finished");
                startActivity(returnToMenu);
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
    }

    //region async task to retrieve data for pages 1-last page -k
    private static class retrieveDataPerPage extends AsyncTask<String, Integer, Boolean> {
        int currentPageNumber;
        protected Boolean doInBackground(String... params) {

            //Store the auction info from the first page
            JSONObject auctionInfo = null;
            JSONArray allAuctions = null;

            try {
                currentPageNumber = Integer.parseInt(params[1]);
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                String newPage =null;
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
                    newPage = buffer.toString();
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
                auctionInfo = new JSONObject(newPage);
                allAuctions = auctionInfo.getJSONArray("auctions");
                for (int i = 0; i < allAuctions.length();i++) {
                    JSONObject currentAuction;
                    currentAuction = allAuctions.getJSONObject(i);

                    //check if there is a bin value , if not, set to false;
                    boolean binFlag;
                    try{
                        binFlag = currentAuction.getBoolean("bin");
                    }
                    catch(Exception invalidReq){
                        binFlag = false;
                    }

                    currentAuctionsdb.AuctionDao()
                            .insertAll(new Auction(
                                    currentAuction.getString("uuid"),
                                    currentAuction.getString("auctioneer"),
                                    currentAuction.getString("profile_id"),
                                    currentAuction.getDouble("start"),
                                    currentAuction.getDouble("end"),
                                    currentAuction.getString("item_name"),
                                    currentAuction.getString("item_lore"),
                                    currentAuction.getString("extra"),
                                    currentAuction.getString("category"),
                                    currentAuction.getString("tier"),
                                    currentAuction.getDouble("starting_bid"),
                                    currentAuction.getBoolean("claimed"),
                                    currentAuction.getDouble("highest_bid_amount"),
                                    binFlag
                            ));
                }
                Log.d("DataRetrieval","Page finished: " + currentPageNumber);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            hasBeenRetrieved[currentPageNumber] = result;
        }
    }
    //endregion

//    //Save files on the device -P
//    public void saveData(String filename,String dataToSave) {
//        FileOutputStream fos = null;
//        try {
//            fos = openFileOutput(filename, Context.MODE_PRIVATE);
//            fos.write(dataToSave.getBytes());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (fos!= null){
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

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