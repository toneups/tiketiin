package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateEventDto(
    val name: String,
    val category: String,
    @SerializedName("venue_id") val venueId: Int,
    val date: String,
    val time: String,
    @SerializedName("created_by") val createdBy: String,
    @com.google.gson.annotations.SerializedName("poster_url")
    val posterUrl: String? = null
)