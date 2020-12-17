package com.wave.sbauction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/*
The user uses this page to access auction information.
Furthermore, this activity is where the data is actually loaded from the server
-P
 */
public class AuctionMainMenu extends AppCompatActivity {

    Button btnGetData;
    Button btn_dispData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction_main_menu);
        setTitle("Skyblock Auction Menu");

        //regionInitialize views
        btnGetData = findViewById(R.id.btnGetData);
        btn_dispData = findViewById(R.id.btn_dispData);

        final Intent getData = new Intent(this,RetrieveData.class);
        final Intent displayData = new Intent(this,DisplayData.class);

        btnGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(getData);
            }
        });

        btn_dispData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(displayData);
            }
        });

        //regionLoad all the JSON information from memory. Probably best not use this in this particular activity. -P
//        try {
//            //load the first page of the data, convert it into JSON
//            String auctionFirstPage = loadStringData("auctionPage0");
//            JSONObject auctionInfo = null;
//            try {
//                auctionInfo = new JSONObject(auctionFirstPage);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            //NOTE IT IS BETTER TO REPEAT THE PROCESS ONE AT A TIME RATHER THAN MAKE 1 HUGE FILE, I NOTICED IT RUNS OUT OF MEMORY
//            SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//            int pagesRemaining = data.getInt("totalAuctionPages",0);
//            for (int page = 1; page <= pagesRemaining; ++page) {
//                JSONObject currentAuctionAllData = null;
//                try {
//                    String filename = "auctionPage" + page;
//                    currentAuctionAllData = new JSONObject(loadStringData(filename));
//                } catch (JSONException e) {
//                    Toast.makeText(getApplicationContext(),"whoopsie doopsie out of memory",Toast.LENGTH_SHORT).show();
//                }
//
//                JSONArray allAuctionsSoFar;
//                JSONArray justAuctionsFromCurrentPage;
//                JSONArray combined;
//                try {
//                    allAuctionsSoFar = auctionInfo.getJSONArray("auctions");
//                    justAuctionsFromCurrentPage = currentAuctionAllData.getJSONArray("auctions");
//                    combined = concatArray(allAuctionsSoFar,justAuctionsFromCurrentPage);
//                    auctionInfo.put("auctions",combined);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (Exception e) {
//            //This happens on the very first time you open the app, and you have no data to begin with.
//            startActivity(new Intent(getApplicationContext(),RetrieveData.class));
//        }
//
//
//        int e = 6;
        //endregion

    }

    //Put JSONArrays together, found this online -P
    //https://stackoverflow.com/questions/4590753/concatenate-jsonarrays
    private JSONArray concatArray(JSONArray... arrs)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrs) {
            for (int i = 0; i < arr.length(); i++) {
                result.put(arr.get(i));
            }
        }
        return result;
    }

    //Load stored data -P
    private String loadStringData(String filename) {
        String finalText = null;
        FileInputStream fis = null;
        try {
            fis = openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(isr);

            String text = br.readLine();

            while (text != null){
                sb.append(text).append("\n");
                text = br.readLine();
            }
            finalText = sb.toString();

        } catch (FileNotFoundException e) {
            Intent getData = new Intent(getApplicationContext(),RetrieveData.class);
            startActivity(getData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return finalText;
    }
}

