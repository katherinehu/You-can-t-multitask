package com.wave.sbauction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.sqlite.db.SimpleSQLiteQuery;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import java.util.List;

public class DisplayData extends AppCompatActivity {

    static List<Auction> displayList = null;
    static List<Auction> InEnchantments;
    static List<Auction> ExEnchantments;
    static AppDatabase currentAuctionsdb;


    final String PET_LO = "0";
    final String PET_HI = "100";


    RecyclerView rc;
    RecyclerView.Adapter adapter;
    RadioButton checkedButton;

    RadioGroup rdgSort;
    CardView cardSort;
    CardView cardExpand;

    Button btnApply;
    Button btnClear;
    Button btnAddAll;

    CheckBox cbCommon;
    CheckBox cbUncommon;
    CheckBox cbRare;
    CheckBox cbEpic;
    CheckBox cbLegendary;
    CheckBox cbMythic;
    CheckBox cbSupreme;
    CheckBox cbSpecial;
    CheckBox cbVerySpecial;

    CheckBox cbWeapon;
    CheckBox cbArmor;
    CheckBox cbConsumable;
    CheckBox cbMisc;
    CheckBox cbBlocks;
    CheckBox cbAccessories;

    CheckBox cbPets;
    TextView tvPetLo;
    TextView tvPetHi;
    EditText etPetHi;
    EditText etPetLo;

    Button btnInAdd;
    Button btnExAdd;
    EditText etInEnchantment;
    EditText etExEnchantment;
    ListView lvInEnchantments;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        //define ui elems
        rdgSort = findViewById(R.id.rdgSort);
        cardSort = findViewById(R.id.cardSort);
        cardExpand = findViewById(R.id.cardExpand);
        btnApply = findViewById(R.id.btnApply);
        btnAddAll = findViewById(R.id.btnAddAll);
        btnClear  = findViewById(R.id.btnClear);

        cbCommon = findViewById(R.id.cbCommon);
        cbUncommon = findViewById(R.id.cbUncommon);
        cbRare = findViewById(R.id.cbRare);
        cbCommon = findViewById(R.id.cbCommon);
        cbEpic = findViewById(R.id.cbEpic);
        cbLegendary = findViewById(R.id.cbLegendary);
        cbMythic = findViewById(R.id.cbMythic);
        cbSupreme = findViewById(R.id.cbSupreme);
        cbSpecial = findViewById(R.id.cbSpecial);
        cbVerySpecial = findViewById(R.id.cbVerySpecial);
        cbWeapon = findViewById(R.id.cbWeapon);
        cbArmor = findViewById(R.id.cbArmor);
        cbConsumable = findViewById(R.id.cbConsumable);
        cbMisc = findViewById(R.id.cbMisc);
        cbBlocks = findViewById(R.id.cbBlocks);
        cbAccessories = findViewById(R.id.cbAccessories);

        cbPets = findViewById(R.id.cbPets);
        tvPetLo = findViewById(R.id.tvPetLo);
        tvPetHi = findViewById(R.id.tvPetHi);
        etPetLo = findViewById(R.id.etPetLo);
        etPetHi = findViewById(R.id.etPetHi);

        btnInAdd = findViewById(R.id.btnInAdd);
        btnExAdd = findViewById(R.id.btnExAdd);
        etInEnchantment = findViewById(R.id.etInEnchantment);
        etExEnchantment = findViewById(R.id.etExEnchantment);
        lvInEnchantments = findViewById(R.id.lvInEnchantments);

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

        //clear all button, clears all filters and includes nothing
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etInEnchantment.setText("NONE");
                etExEnchantment.setText("ALL");

                cbCommon.setChecked(false);
                cbUncommon.setChecked(false);
                cbRare.setChecked(false);
                cbEpic.setChecked(false);
                cbLegendary.setChecked(false);
                cbMythic.setChecked(false);
                cbSupreme.setChecked(false);
                cbSpecial.setChecked(false);
                cbVerySpecial.setChecked(false);

                cbWeapon.setChecked(false);
                cbArmor.setChecked(false);
                cbAccessories.setChecked(false);
                cbConsumable.setChecked(false);
                cbBlocks.setChecked(false);
                cbMisc.setChecked(false);

                cbPets.setChecked(false);
                etPetLo.setText(PET_LO);
                etPetHi.setText(PET_HI);
            }
        });

        //add all button, adds all items
        btnAddAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                etInEnchantment.setText("ALL");
                etExEnchantment.setText("NONE");

                cbCommon.setChecked(true);
                cbUncommon.setChecked(true);
                cbRare.setChecked(true);
                cbEpic.setChecked(true);
                cbLegendary.setChecked(true);
                cbMythic.setChecked(true);
                cbSupreme.setChecked(true);
                cbSpecial.setChecked(true);
                cbVerySpecial.setChecked(true);

                cbWeapon.setChecked(true);
                cbArmor.setChecked(true);
                cbAccessories.setChecked(true);
                cbConsumable.setChecked(true);
                cbBlocks.setChecked(true);
                cbMisc.setChecked(true);

                cbPets.setChecked(true);
                etPetLo.setText(PET_LO);
                etPetHi.setText(PET_HI);


            }
        });

        //only pets if checked or not checked
        cbPets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CompoundButton) v).isChecked()){

                    etPetHi.setVisibility(View.VISIBLE);
                    etPetLo.setVisibility(View.VISIBLE);
                    tvPetHi.setVisibility(View.VISIBLE);
                    tvPetLo.setVisibility(View.VISIBLE);

                } else {

                    etPetHi.setVisibility(View.GONE);
                    etPetLo.setVisibility(View.GONE);
                    tvPetHi.setVisibility(View.GONE);
                    tvPetLo.setVisibility(View.GONE);

                }
            }
        });

        // add enchantments to include
//        btnInAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        }



        //define database and get all data
        new Thread(){
            @Override
            public void run() {
                super.run();

                //define database

                currentAuctionsdb = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "CurrentAuctionsDB")
                        .build();

                final List<Auction> auctions = currentAuctionsdb.AuctionDao().getAllAuctions();

                //if button is clicked, filter list based on user preferences
                btnApply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        displayList = filterList(auctions);
                    }
                });

                //check which rb is checked
                int radioId = rdgSort.getCheckedRadioButtonId();
                checkedButton = findViewById(radioId);
                if (checkedButton.getText() == "A-Z") {
                    displayList = sortList(displayList, currentAuctionsdb, "item_name", "ASC");
                }
                else if (checkedButton.getText() == "Ending Soon") {
                    displayList = sortList(displayList, currentAuctionsdb, "end", "ASC");
                }
                else if (checkedButton.getText() == "rbPriceHL") {
                    displayList = sortList(displayList, currentAuctionsdb, "highest_bid_amount", "DESC");
                }
                else if (checkedButton.getText() == "rbPriceLH") {
                    displayList = sortList(displayList, currentAuctionsdb, "highest_bid_amount", "ASC");
                }

                //output data onto UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rc = findViewById(R.id.my_recycler_view);
                        rc.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        adapter = new AuctionAdapter(displayList);
                        rc.setAdapter(adapter);
                    }
                });
            }
        }.start();
    }



    public List<Auction> filterList(List<Auction> auctions) {
        List<Auction> filterListRarity = null;

        for (int i = 0; i < auctions.size(); i++) {
            String myTier = auctions.get(i).getTier();
            if (cbCommon.isChecked() && myTier == "COMMON" || cbUncommon.isChecked() && myTier == "UNCOMMON"
                || cbEpic.isChecked() && myTier == "EPIC" || cbLegendary.isChecked() && myTier == "LEGENDARY"
                || cbMythic.isChecked() && myTier == "MYTHIC" || cbRare.isChecked() && myTier == "RARE"
                || cbSpecial.isChecked() && myTier == "SPECIAL" || cbVerySpecial.isChecked() && myTier == "VERY_SPECIAL"
                || cbSupreme.isChecked() && myTier == "SUPREME") {
                filterListRarity.add(auctions.get(i));
            }
        }

        List<Auction> filterListCat = null;

        for (int i = 0; i < filterListCat.size(); i++) {
            String myCat = filterListRarity.get(i).getCategory();

            if (cbWeapon.isChecked() && myCat == "weapon" || cbArmor.isChecked() && myCat == "armor"
                    || cbAccessories.isChecked() && myCat == "accessories" || cbBlocks.isChecked() && myCat == "blocks"
                    || cbConsumable.isChecked() && myCat == "consumable" || cbMisc.isChecked() && myCat == "misc" ){
                filterListCat.add(filterListRarity.get(i));
            }
        }

        return filterListCat;
    }


    public List<Auction> sortList(List<Auction> inputList, AppDatabase appDatabase, String targetField, String ASCorDEC) {
        String query ="SELECT * FROM Auction ORDER BY " + targetField + ASCorDEC;
        List<Auction> listAZ = appDatabase.AuctionDao().runtimeQuery(new SimpleSQLiteQuery(query));
        return listAZ;
    }


}