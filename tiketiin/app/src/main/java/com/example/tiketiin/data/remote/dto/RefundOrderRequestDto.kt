package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RefundOrderRequestDto(
    @SerializedName("p_order_id")
    val orderId: Int
)