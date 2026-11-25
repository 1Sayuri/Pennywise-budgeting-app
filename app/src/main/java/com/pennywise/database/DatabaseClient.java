package com.pennywise.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseClient {
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE bank_accounts ADD COLUMN lastFour TEXT");
            db.execSQL("ALTER TABLE bank_accounts ADD COLUMN cardImage TEXT");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `wishlist_items` ("
                    + "`itemId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`accountId` INTEGER NOT NULL, "
                    + "`name` TEXT, "
                    + "`priceCents` INTEGER NOT NULL, "
                    + "`savedCents` INTEGER NOT NULL, "
                    + "`emoji` TEXT, "
                    + "`link` TEXT, "
                    + "FOREIGN KEY(`accountId`) REFERENCES `bank_accounts`(`accountId`) "
                    + "ON UPDATE NO ACTION ON DELETE CASCADE)");

            db.execSQL("CREATE INDEX IF NOT EXISTS `index_wishlist_items_accountId` "
                    + "ON `wishlist_items` (`accountId`)");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE wishlist_items ADD COLUMN imagePath TEXT");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE savings_challenges ADD COLUMN templateKey TEXT");
            db.execSQL("ALTER TABLE savings_challenges ADD COLUMN endDate INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {

            db.execSQL("CREATE TABLE IF NOT EXISTS `savings_challenges` ("
                    + "`challengeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`title` TEXT, "
                    + "`targetAmountCents` INTEGER NOT NULL, "
                    + "`startDate` INTEGER NOT NULL, "
                    + "`status` TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS `savings_weeks` ("
                    + "`weekId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`challengeId` INTEGER NOT NULL, "
                    + "`weekNumber` INTEGER NOT NULL, "
                    + "`targetAmountCents` INTEGER NOT NULL, "
                    + "`savedAmountCents` INTEGER NOT NULL, "
                    + "`isCompleted` INTEGER NOT NULL, "
                    + "FOREIGN KEY(`challengeId`) REFERENCES `savings_challenges`(`challengeId`) "
                    + "ON UPDATE NO ACTION ON DELETE CASCADE)");

            db.execSQL("CREATE INDEX IF NOT EXISTS `index_savings_weeks_challengeId` "
                    + "ON `savings_weeks` (`challengeId`)");
        }
    };

    private static DatabaseClient instance;

    private final AppDatabase appDatabase;

    private DatabaseClient(Context context) {

        appDatabase = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "pennywise_database"
                )
                .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6
                )
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