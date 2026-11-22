package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

// Body untuk memanggil RPC get_price_history
data class PriceHistoryRequest(
    @SerializedName("p_event_id") val eventId: Int,
    @SerializedName("p_seat_category") val seatCategory: String
)

// Satu baris hasil RPC (kolom unit_price)
data class PriceHistoryDto(
    @SerializedName("unit_price") val unitPrice: Long
)