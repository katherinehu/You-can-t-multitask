package com.wave.sbauction;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;
import java.util.Objects;

@Dao
public interface AuctionDao {
    @Query("SELECT * FROM Auction")
    List<Auction> getAllAuctions();

    @Query("SELECT * from Auction LIMIT 1")
    Auction getAnAuctionItem();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Auction... auctions);

    @RawQuery
    List<Auction> runtimeQuery(SupportSQLiteQuery sortQuery);
}
