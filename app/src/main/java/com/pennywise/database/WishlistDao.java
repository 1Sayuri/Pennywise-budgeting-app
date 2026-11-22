package com.pennywise.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WishlistDao {

    @Insert
    long insertItem(WishlistEntity item);

    @Update
    void updateItem(WishlistEntity item);

    @Delete
    void deleteItem(WishlistEntity item);

    @Query("SELECT * FROM wishlist_items ORDER BY itemId DESC")
    List<WishlistEntity> getAllItems();

    @Query("SELECT COALESCE(SUM(savedCents), 0) FROM wishlist_items WHERE accountId = :accountId")
    long getReservedForAccount(int accountId);
}