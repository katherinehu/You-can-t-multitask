package com.wave.sbauction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

public class DisplayData extends AppCompatActivity {

    RecyclerView rc;
    RecyclerView.Adapter adapter;
    RadioButton checkedButton;

    RadioGroup rdgSort;
    CardView cardSort;
    CardView cardExpand;

    Button btnApply;

    CheckBox cbCommon;
    CheckBox cbUncommon;
    CheckBox cbRare;
    CheckBox cbEpic;
    CheckBox cbLegendary;
    CheckBox cbMythic;
    CheckBox cbSupreme;
    CheckBox cbSpecial;
    CheckBox cbVerySpecial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        //define ui elems
        rdgSort = findViewById(R.id.rdgSort);
        cardSort = findViewById(R.id.cardSort);
        cardExpand = findViewById(R.id.cardExpand);
        btnApply = findViewById(R.id.btnApply);

        // shrink card if clicked
        cardSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardSort.setVisibility(View.GONE);
                cardExpand.setVisibility(View.VISIBLE);
            }
        });

        //expand card if clicked
        cardExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardSort.setVisibility(View.VISIBLE);
                cardExpand.setVisibility(View.GONE);
            }
        });


        //define database and get all data
        new Thread(){
            @Override
            public void run() {
                super.run();

                //define database
                AppDatabase currentAuctionsdb;
                currentAuctionsdb = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "CurrentAuctionsDB")
                        .build();

                final List<Auction> auctions = currentAuctionsdb.AuctionDao().getAllAuctions();
                final List<Auction> displayList = null;



                //if button is clicked, filter list based on user preferences
                btnApply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        filterList(displayList, auctions);
                    }
                });




                //output data onto UI
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



    //check which radio button is checked
    public void sortList(View v) {

        int radioId = rdgSort.getCheckedRadioButtonId();
        checkedButton = findViewById(radioId);

    }

    public void filterList(List<Auction> displayList, List<Auction> auctions) {
        for (int i = 0; i < auctions.size(); i++) {
            String myTier = auctions.get(i).getTier();
            if (cbCommon.isChecked() && myTier == "COMMON" || cbUncommon.isChecked() && myTier == "UNCOMMON"
                || cbEpic.isChecked() && myTier == "EPIC" || cbLegendary.isChecked() && myTier == "LEGENDARY"
                || cbMythic.isChecked() && myTier == "MYTHIC" || cbRare.isChecked() && myTier == "RARE"
                || cbSpecial.isChecked() && myTier == "SPECIAL" || cbVerySpecial.isChecked() && myTier == "VERY_SPECIAL"
                || cbSupreme.isChecked() && myTier == "SUPREME") {
                displayList.add(auctions.get(i));
            }

        }
    }


}