
package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WishlistDto(
    val id: Int,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("event_id")
    val eventId: Int
)

data class CreateWishlistRequest(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("event_id")
    val eventId: Int
)
