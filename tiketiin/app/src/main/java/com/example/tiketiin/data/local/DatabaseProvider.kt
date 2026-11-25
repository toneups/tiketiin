package com.example.tiketiin.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: TiketiinDatabase? = null

    fun getDatabase(context: Context): TiketiinDatabase {

        return INSTANCE ?: synchronized(this) {

            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                TiketiinDatabase::class.java,
                "tiketiin_database"
            )
                .addMigrations(
                    DatabaseMigrations.MIGRATION_1_2,
                    DatabaseMigrations.MIGRATION_2_3,
                    DatabaseMigrations.MIGRATION_3_4
                )
                .build()
                .also { INSTANCE = it }

        }
    }
}