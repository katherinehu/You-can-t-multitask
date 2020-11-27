package com.wave.sbauction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

/*
The user uses this page to access auction information.
Furthermore, this activity is where the data is actually loaded from the server
-P
 */
public class AuctionMainMenu extends AppCompatActivity {

    Button btnGetData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction_main_menu);
        setTitle("Skyblock Auction Menu");

        //regionInitialize views
        btnGetData = findViewById(R.id.btnGetData);

        final Intent getData = new Intent(this,RetrieveData.class);
        btnGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(getData);
            }
        });
    }
}

