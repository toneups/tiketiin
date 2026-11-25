package com.example.tiketiin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tiketiin.data.local.dao.EventDao
import com.example.tiketiin.data.local.entity.CachedEventEntity
import com.example.tiketiin.data.local.dao.ListingDao
import com.example.tiketiin.data.local.entity.CachedListingEntity

@Database(
    entities = [
        CachedEventEntity::class,
        CachedListingEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class TiketiinDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun listingDao(): ListingDao

}

