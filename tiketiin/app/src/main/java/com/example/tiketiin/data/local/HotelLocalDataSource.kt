
package com.example.tiketiin.data.local

import com.example.tiketiin.data.local.dao.HotelDao
import com.example.tiketiin.data.local.entity.HotelEntity
import kotlinx.coroutines.flow.Flow

class HotelLocalDataSource(
    private val hotelDao: HotelDao
) {

    fun observeHotels(eventId: Int): Flow<List<HotelEntity>> {
        return hotelDao.observeHotels(eventId)
    }

    suspend fun getHotels(eventId: Int): List<HotelEntity> {
        return hotelDao.getHotels(eventId)
    }

    suspend fun getHotelById(
        hotelId: Int,
        eventId: Int
    ): HotelEntity? {
        return hotelDao.getHotelById(hotelId, eventId)
    }

    suspend fun saveHotels(hotels: List<HotelEntity>) {
        hotelDao.insertHotels(hotels)
    }

    suspend fun clearHotels(eventId: Int) {
        hotelDao.clearHotels(eventId)
    }

    suspend fun countHotels(eventId: Int): Int {
        return hotelDao.countHotels(eventId)
    }

    suspend fun getLastCachedAt(eventId: Int): Long? {
        return hotelDao.getLastCachedAt(eventId)
    }

    suspend fun clearAllHotels() {
        hotelDao.clearAllHotels()
    }
}
