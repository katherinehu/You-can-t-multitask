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
        AppDatabase currentAuctionsdb;
        currentAuctionsdb = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "CurrentAuctionsDB")
                .build();

        List<Auction> auctions = currentAuctionsdb.AuctionDao().getAllAuctions();

        rc = findViewById(R.id.my_recycler_view);
        rc.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AuctionAdapter(auctions);
        rc.setAdapter(adapter);
    }
}