package com.pennywise.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insertTransaction(TransactionEntity transaction);

    @Update
    void updateTransaction(TransactionEntity transaction);

    @Delete
    void deleteTransaction(TransactionEntity transaction);

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    List<TransactionEntity> getTransactionsByAccount(int accountId);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<TransactionEntity> getAllTransactions();

    @Query("DELETE FROM transactions WHERE transactionId = :transactionId")
    void deleteTransactionById(int transactionId);
}