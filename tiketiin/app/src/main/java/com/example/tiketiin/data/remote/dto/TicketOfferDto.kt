package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TicketOfferDto(
    val id: Long,

    @SerializedName("listing_id")
    val listingId: Int,

    @SerializedName("price_per_ticket")
    val pricePerTicket: Long,

    val quantity: Int,
    val status: String,

    @SerializedName("created_at")
    val createdAt: String,

    val listing: OfferListingDto? = null
)

data class OfferListingDto(
    @SerializedName("seat_category")
    val seatCategory: String?,

    val event: OfferEventDto? = null
)

data class OfferEventDto(
    val name: String?
)