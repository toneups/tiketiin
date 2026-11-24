package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CityOptionDto(
    val id: Long,
    val name: String,
    @SerializedName("administrative_type")
    val administrativeType: String,
    val province: String
) {
    val displayName: String
        get() = "$administrativeType $name"

    val fullLabel: String
        get() = "$displayName — $province"
}