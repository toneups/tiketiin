package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateVenueDto(
    val name: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("created_by") val createdBy: String
)