package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderListingDto(
    @SerializedName("seat_category")
    val seatCategory: String,

    @SerializedName("seat_row")
    val seatRow: String
)