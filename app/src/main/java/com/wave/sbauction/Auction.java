package com.wave.sbauction;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Auction {

    public Auction(@NonNull String uuid, String auctioneer, String profile_id, double start, double end, String item_name, String item_lore, String extra, String category, String tier, double starting_bid, boolean claimed, double highest_bid_amount, boolean bin) {
        this.uuid = uuid;
        this.auctioneer = auctioneer;
        this.profile_id = profile_id;
        this.start = start;
        this.end = end;
        this.item_name = item_name;
        this.item_lore = item_lore;
        this.extra = extra;
        this.category = category;
        this.tier = tier;
        this.starting_bid = starting_bid;
        this.claimed = claimed;
        this.highest_bid_amount = highest_bid_amount;
        this.bin = bin;
    }

    @NonNull
    @PrimaryKey
    public String uuid;

    @ColumnInfo
    public String auctioneer;

    @ColumnInfo
    public String profile_id;

    @ColumnInfo
    public double start;

    @ColumnInfo
    public double end;

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
    public double starting_bid;

    @ColumnInfo
    public boolean claimed;

    @ColumnInfo
    public double highest_bid_amount;

    @ColumnInfo
    public boolean bin;


}

