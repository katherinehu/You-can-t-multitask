package com.wave.sbauction;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Auction {

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

    public Auction(String uuid, String auctioneer, String profile_id, double start, double end, String item_name, String item_lore, String extra, String category, String tier, double starting_bid, boolean claimed, double highest_bid_amount, boolean binFlag) {
    }
}

