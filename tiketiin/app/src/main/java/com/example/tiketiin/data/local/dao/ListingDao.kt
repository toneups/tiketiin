package com.example.tiketiin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.tiketiin.data.local.entity.CachedListingEntity

@Dao
interface ListingDao {

    @Query("SELECT * FROM cached_listings WHERE sellerId = :sellerId ORDER BY id DESC")
    suspend fun getListingsBySeller(sellerId: String): List<CachedListingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<CachedListingEntity>)

    @Query("DELETE FROM cached_listings WHERE sellerId = :sellerId")
    suspend fun clearListings(sellerId: String)

    @Transaction
    suspend fun replaceListings(sellerId: String, listings: List<CachedListingEntity>) {
        clearListings(sellerId)
        insertListings(listings)
    }
}