package com.wave.sbauction;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AuctionDao {
    @Query("SELECT * FROM Auction")
    List<Auction> getAllAuctions();

    @Query("SELECT * from Auction LIMIT 1")
    Auction getAnAuctionItem();

    @Insert
    void insertAll(Auction...auctions) ;

}
