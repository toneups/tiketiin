package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.tiketiin.data.remote.dto.EventDemandStatsDto

data class EventDemandStatsDto(
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("sold_count") val soldCount: Int,
    @SerializedName("wishlist_count") val wishlistCount: Int,
    @SerializedName("view_count") val viewCount: Int,
    @SerializedName("views_last_7_days") val viewsLast7Days: Int,
    @SerializedName("available_stock") val availableStock: Int
)