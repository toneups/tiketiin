
package com.example.tiketiin.data.mapper

import com.example.tiketiin.data.local.entity.HotelEntity
import com.example.tiketiin.data.model.Hotel


fun Hotel.toEntity(eventId: Int): HotelEntity {
    return HotelEntity(
        id = id,
        eventId = eventId,
        name = name,
        area = area,
        address = address,
        city = city,
        latitude = latitude,
        longitude = longitude,
        pricePerNight = pricePerNight,
        rating = rating,
        availableRooms = availableRooms,
        colorStart = colorStart,
        colorEnd = colorEnd,
        imageUrl = imageUrl,
        bookingUrl = bookingUrl
    )
}


fun HotelEntity.toHotelModel(): Hotel {
    return Hotel(
        id = id,
        name = name,
        area = area,
        address = address,
        city = city,
        latitude = latitude,
        longitude = longitude,
        pricePerNight = pricePerNight,
        rating = rating,
        availableRooms = availableRooms,
        colorStart = colorStart,
        colorEnd = colorEnd,
        imageUrl = imageUrl,
        bookingUrl = bookingUrl
    )
}


fun List<HotelEntity>.toHotelModelsFromLocal(): List<Hotel> {
    return map { it.toHotelModel() }
}
