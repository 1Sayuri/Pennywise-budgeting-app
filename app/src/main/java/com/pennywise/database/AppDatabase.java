package com.pennywise.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                BankAccount.class,
                TransactionEntity.class,
                WishlistEntity.class,
                SavingsChallenge.class,
                SavingsWeek.class
        },
        version = 7,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BankAccountDao bankAccountDao();

    public abstract TransactionDao transactionDao();

    public abstract WishlistDao wishlistDao();

    public abstract SavingsChallengeDao savingsChallengeDao();
}
