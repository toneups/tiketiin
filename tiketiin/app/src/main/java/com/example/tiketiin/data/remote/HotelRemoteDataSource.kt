
package com.example.tiketiin.data.remote

import com.example.tiketiin.data.remote.api.SupabaseApiService
import com.example.tiketiin.data.remote.dto.GetAvailableHotelsRequestDto
import com.example.tiketiin.data.remote.dto.HotelDto
import com.example.tiketiin.data.remote.dto.CreateHotelRequestDto
import com.example.tiketiin.data.remote.dto.UpdateHotelRequestDto

class HotelRemoteDataSource(
    private val api: SupabaseApiService = RetrofitClient.api
) {

    // RECOMMENDATION
    suspend fun getAvailableHotels(
        eventId: Int
    ): List<HotelDto> {
        val result = api.getAvailableHotels(
            request = GetAvailableHotelsRequestDto(
                eventId = eventId
            )
        )

        result.forEach {
            android.util.Log.d(
                "HOTEL_DEBUG",
                "Nama=${it.hotelName}, " +
                        "Harga=${it.pricePerNight}, " +
                        "URL=${it.bookingUrl}"
            )
        }

        return result
    }

    // READ ALL HOTELS
    suspend fun getAllHotels(): List<HotelDto> {
        return api.getAllHotels()
    }

    // CREATE HOTEL
    suspend fun createHotel(
        request: CreateHotelRequestDto
    ): HotelDto {
        return api.createHotel(request).firstOrNull()
            ?: error("Hotel gagal ditambahkan.")
    }

    // UPDATE HOTEL
    suspend fun updateHotel(
        hotelId: Int,
        request: UpdateHotelRequestDto
    ): HotelDto {
        return api.updateHotel(
            hotelId = "eq.$hotelId",
            request = request
        ).firstOrNull() ?: error("Hotel tidak ditemukan atau gagal diperbarui.")
    }

    // DELETE HOTEL
    suspend fun deleteHotel(hotelId: Int) {
        api.deleteHotel(
            hotelId = "eq.$hotelId"
        )
    }


}
