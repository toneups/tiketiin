package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateEventDto(
    val name: String,
    val category: String,
    @SerializedName("venue_id") val venueId: Int,
    val date: String,
    val time: String
)