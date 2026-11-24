
package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    val id: Int,

    @SerializedName("user_id")
    val userId: String,

    val title: String,
    val message: String,
    val type: String,

    @SerializedName("event_id")
    val eventId: Int?,

    @SerializedName("order_id")
    val orderId: Int?,

    @SerializedName("is_read")
    val isRead: Boolean,

    @SerializedName("created_at")
    val createdAt: String
)


data class NotificationReadUpdate(
    @SerializedName("is_read")
    val isRead: Boolean = true
)
