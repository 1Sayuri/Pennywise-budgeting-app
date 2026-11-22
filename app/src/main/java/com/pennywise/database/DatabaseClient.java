package com.pennywise.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;



public class DatabaseClient {


    // Version 1 -> 2: add card details to bank accounts
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE bank_accounts ADD COLUMN lastFour TEXT");
            db.execSQL("ALTER TABLE bank_accounts ADD COLUMN cardImage TEXT");
        }
    };

    private static DatabaseClient instance;

    private final AppDatabase appDatabase;

    private DatabaseClient(Context context) {

        appDatabase = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "pennywise_database"
        ).addMigrations(MIGRATION_1_2)
                .build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {

        if (instance == null) {
            instance = new DatabaseClient(context);
        }

        return instance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}