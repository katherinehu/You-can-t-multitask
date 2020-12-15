package com.wave.sbauction;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AuctionDao {
    @Query("SELECT * FROM Auction")
    List<Auction> getAllAuctions();

    @Insert
    void insertAll(Auction...auctions) ;

}