
package com.example.tiketiin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tiketiin.data.local.entity.HotelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HotelDao {

    @Query("""
        SELECT * FROM cached_hotels
        WHERE eventId = :eventId
        ORDER BY rating DESC
    """)
    fun observeHotels(eventId: Int): Flow<List<HotelEntity>>

    @Query("""
        SELECT * FROM cached_hotels
        WHERE eventId = :eventId
        ORDER BY rating DESC
    """)
    suspend fun getHotels(eventId: Int): List<HotelEntity>

    @Query("""
        SELECT * FROM cached_hotels
        WHERE id = :hotelId AND eventId = :eventId
    """)
    suspend fun getHotelById(
        hotelId: Int,
        eventId: Int
    ): HotelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotels(hotels: List<HotelEntity>)

    @Query("""
        DELETE FROM cached_hotels
        WHERE eventId = :eventId
    """)
    suspend fun clearHotels(eventId: Int)

    @Query("""
        SELECT COUNT(*) FROM cached_hotels
        WHERE eventId = :eventId
    """)
    suspend fun countHotels(eventId: Int): Int

    @Query("""
        SELECT MAX(cachedAt) FROM cached_hotels
        WHERE eventId = :eventId
    """)
    suspend fun getLastCachedAt(eventId: Int): Long?

    @Query("DELETE FROM cached_hotels")
    suspend fun clearAllHotels()
}
