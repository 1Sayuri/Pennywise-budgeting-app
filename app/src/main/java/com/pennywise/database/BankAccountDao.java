package com.pennywise.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BankAccountDao {

    @Insert
    long insertAccount(BankAccount account);

    @Update
    void updateAccount(BankAccount account);

    @Delete
    void deleteAccount(BankAccount account);

    @Query("SELECT * FROM bank_accounts")
    List<BankAccount> getAllAccounts();

    @Query("SELECT * FROM bank_accounts WHERE accountId = :accountId")
    BankAccount getAccountById(int accountId);
}