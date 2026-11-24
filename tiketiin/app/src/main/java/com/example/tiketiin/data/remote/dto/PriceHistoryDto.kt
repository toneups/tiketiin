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
// Body untuk insert listing baru ke tabel ticket_listings
data class CreateListingRequest(
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("seller_id") val sellerId: String,
    @SerializedName("seat_category") val seatCategory: String,
    @SerializedName("seat_row") val seatRow: String,
    @SerializedName("face_value") val faceValue: Long,
    @SerializedName("price") val price: Long,
    @SerializedName("quantity") val quantity: Int
)

// Satu baris listing hasil insert
data class ListingRowDto(
    @SerializedName("id") val id: Int
)
// Satu listing lengkap dari tabel ticket_listings
data class ListingDto(
    @SerializedName("id") val id: Int,
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("seller_id") val sellerId: String,
    @SerializedName("seat_category") val seatCategory: String,
    @SerializedName("seat_row") val seatRow: String,
    @SerializedName("face_value") val faceValue: Long,
    @SerializedName("price") val price: Long,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("status") val status: String,
    @SerializedName("views") val views: Int = 0,
    @SerializedName("saves") val saves: Int = 0,
    @SerializedName("offers") val offers: Int = 0
)