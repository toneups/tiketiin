package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PayOrderRequestDto(
    @SerializedName("p_order_id")
    val orderId: Int,

    @SerializedName("p_payment_method")
    val paymentMethod: String
)
