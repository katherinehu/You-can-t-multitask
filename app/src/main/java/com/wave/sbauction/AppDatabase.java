package com.wave.sbauction;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Auction.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AuctionDao userDao();
}
