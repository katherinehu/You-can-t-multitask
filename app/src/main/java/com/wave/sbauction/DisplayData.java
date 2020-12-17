package com.wave.sbauction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class DisplayData extends AppCompatActivity {

    RecyclerView rc;
    RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        //define database and get all data
        new Thread(){
            @Override
            public void run() {
                super.run();
                AppDatabase currentAuctionsdb;
                currentAuctionsdb = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "CurrentAuctionsDB")
                        .build();

                final List<Auction> auctions = currentAuctionsdb.AuctionDao().getAllAuctions();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rc = findViewById(R.id.my_recycler_view);
                        rc.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        adapter = new AuctionAdapter(auctions);
                        rc.setAdapter(adapter);
                    }
                });
            }
        }.start();
    }
}