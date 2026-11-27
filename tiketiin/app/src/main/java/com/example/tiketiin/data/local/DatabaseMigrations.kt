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

    // Tabel hotel dan order (Saskia)
    val MIGRATION_3_4 = object : Migration(3, 4) {

        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS cached_hotels (
                    id INTEGER NOT NULL,
                    eventId INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    area TEXT NOT NULL,
                    latitude REAL NOT NULL,
                    longitude REAL NOT NULL,
                    pricePerNight INTEGER NOT NULL,
                    rating REAL NOT NULL,
                    availableRooms INTEGER NOT NULL,
                    colorStart INTEGER NOT NULL,
                    colorEnd INTEGER NOT NULL,
                    imageUrl TEXT,
                    bookingUrl TEXT,
                    cachedAt INTEGER NOT NULL,
                    PRIMARY KEY(id, eventId)
                )
            """.trimIndent())

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS cached_orders (
                    id INTEGER NOT NULL,
                    eventId INTEGER NOT NULL,
                    listingId INTEGER NOT NULL,
                    buyerId TEXT NOT NULL,
                    sellerId TEXT NOT NULL,
                    buyerName TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    seatLabel TEXT NOT NULL,
                    ticketPrice INTEGER NOT NULL,
                    serviceFee INTEGER NOT NULL,
                    total INTEGER NOT NULL,
                    paymentMethod TEXT NOT NULL,
                    transactionNo TEXT NOT NULL,
                    createdAt TEXT NOT NULL,
                    status TEXT NOT NULL,
                    qrCode TEXT,
                    rating INTEGER NOT NULL,
                    cachedAt INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
            """.trimIndent())
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            ALTER TABLE cached_hotels
            ADD COLUMN address TEXT NOT NULL DEFAULT ''
            """.trimIndent()
            )

            db.execSQL(
                """
            ALTER TABLE cached_hotels
            ADD COLUMN city TEXT NOT NULL DEFAULT ''
            """.trimIndent()
            )
        }
    }


    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            ALTER TABLE cached_orders
            ADD COLUMN buyerEmail TEXT NOT NULL DEFAULT ''
            """.trimIndent()
            )

            db.execSQL(
                """
            ALTER TABLE cached_orders
            ADD COLUMN buyerPhone TEXT NOT NULL DEFAULT ''
            """.trimIndent()
            )

            db.execSQL(
                """
            ALTER TABLE cached_orders
            ADD COLUMN ticketUsedAt TEXT
            """.trimIndent()
            )
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            ALTER TABLE cached_orders
            ADD COLUMN paymentStatus TEXT NOT NULL DEFAULT 'PENDING'
            """.trimIndent()
            )

            db.execSQL(
                """
            ALTER TABLE cached_orders
            ADD COLUMN escrowStatus TEXT NOT NULL DEFAULT 'PENDING'
            """.trimIndent()
            )
        }
    }

    // Tabel cache listing tiket (Raisya)
    val MIGRATION_7_8 = object : Migration(7, 8) {
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