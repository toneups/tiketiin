package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EventViewDto(
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("user_id") val userId: String
)