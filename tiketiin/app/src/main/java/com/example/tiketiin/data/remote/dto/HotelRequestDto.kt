
package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateHotelRequestDto(
    @SerializedName("hotel_name")
    val hotelName: String,

    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,

    @SerializedName("price_per_night")
    val pricePerNight: Long,

    val rating: Double,

    @SerializedName("available_rooms")
    val availableRooms: Int,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("booking_url")
    val bookingUrl: String? = null
)

data class UpdateHotelRequestDto(
    @SerializedName("hotel_name")
    val hotelName: String,

    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,

    @SerializedName("price_per_night")
    val pricePerNight: Long,

    val rating: Double,

    @SerializedName("available_rooms")
    val availableRooms: Int,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("booking_url")
    val bookingUrl: String? = null
)
