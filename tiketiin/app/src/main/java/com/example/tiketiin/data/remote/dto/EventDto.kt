package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EventDto(

    val id: Int,

    val name: String,

    val category: String,

    @SerializedName("venue_id")
    val venueId: Int,

    val date: String,

    val time: String,

    val description: String,

    @SerializedName("poster_url")
    val posterUrl: String?,

    @SerializedName("poster_start")
    val posterStart: Long,

    @SerializedName("poster_end")
    val posterEnd: Long,

    @SerializedName("flash_ends_at")
    val flashEndsAt: String?,

    @SerializedName("base_sold")
    val baseSold: Int,

    @SerializedName("base_wishlist")
    val baseWishlist: Int,

    @SerializedName("view_count")
    val viewCount: Int,

    @SerializedName("views_last_7_days")
    val viewsLast7Days: Int,

    val venue: VenueDto?,

    @SerializedName("ticket_listings")
    val ticketListings: List<TicketListingDto>? = null

)