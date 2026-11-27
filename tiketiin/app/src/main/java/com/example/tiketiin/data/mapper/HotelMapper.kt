
package com.example.tiketiin.data.mapper

import com.example.tiketiin.data.model.Hotel
import com.example.tiketiin.data.remote.dto.HotelDto

fun HotelDto.toModel(): Hotel {
    return Hotel(
        id = hotelId,
        name = hotelName,
        area = "$address, $city",
        latitude = latitude,
        longitude = longitude,
        pricePerNight = pricePerNight,
        rating = rating,
        availableRooms = availableRooms,
        colorStart = 0xFFE3F2FD,
        colorEnd = 0xFFBBDEFB,
        imageUrl = imageUrl,
        bookingUrl = bookingUrl,
        address = address,
        city = city
    )
}

fun List<HotelDto>.toHotelModels(): List<Hotel> {
    return map { it.toModel() }
}
