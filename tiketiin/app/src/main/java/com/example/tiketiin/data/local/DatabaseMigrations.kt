package com.example.tiketiin.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {

        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                "ALTER TABLE cached_events ADD COLUMN latitude REAL"
            )

            db.execSQL(
                "ALTER TABLE cached_events ADD COLUMN longitude REAL"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {

        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                "ALTER TABLE cached_events ADD COLUMN lowestPrice INTEGER"
            )

            db.execSQL(
                "ALTER TABLE cached_events ADD COLUMN ticketCount INTEGER"
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {

        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS cached_listings (" +
                        "id INTEGER NOT NULL, " +
                        "eventId INTEGER NOT NULL, " +
                        "sellerId TEXT NOT NULL, " +
                        "seatCategory TEXT NOT NULL, " +
                        "seatRow TEXT NOT NULL, " +
                        "faceValue INTEGER NOT NULL, " +
                        "price INTEGER NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "views INTEGER NOT NULL, " +
                        "saves INTEGER NOT NULL, " +
                        "offers INTEGER NOT NULL, " +
                        "PRIMARY KEY(id))"
            )
        }
    }
}