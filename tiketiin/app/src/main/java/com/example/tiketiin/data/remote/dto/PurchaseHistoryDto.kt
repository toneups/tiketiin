
package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PurchaseHistoryDto(
    val id: Int,

    @SerializedName("buyer_id")
    val buyerId: String,

    @SerializedName("event_id")
    val eventId: Int,

    val status: String,

    @SerializedName("paid_at")
    val paidAt: String?
)
