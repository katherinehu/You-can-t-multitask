package com.wave.sbauction;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {

    // create constructors (ctr-N)
    @NonNull
    @PrimaryKey
    public String uuid;

    @ColumnInfo
    public String auctioneer;

    @ColumnInfo
    public String profile_id;

    @ColumnInfo
    public Double start;

    @ColumnInfo
    public Double end;

    @ColumnInfo
    public String item_name;

    @ColumnInfo
    public String item_lore;

    @ColumnInfo
    public String extra;

    @ColumnInfo
    public String category;

    @ColumnInfo
    public String tier;

    @ColumnInfo
    public Double starting_bid;

    @ColumnInfo
    public boolean claimed;

    @ColumnInfo
    public Double highest_bid_amount;

    @ColumnInfo
    public boolean bin;
}

