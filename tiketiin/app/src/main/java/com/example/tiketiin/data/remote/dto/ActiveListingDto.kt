package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ActiveListingDto(
    val id: Int,
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("seller_id") val sellerId: String,
    @SerializedName("seat_category") val seatCategory: String,
    @SerializedName("seat_row") val seatRow: String,
    @SerializedName("face_value") val faceValue: Long,
    val price: Long,
    val quantity: Int
)