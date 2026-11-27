
package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaymentDto(
    val id: Int,

    @SerializedName("order_id")
    val orderId: Int,

    @SerializedName("payment_method")
    val paymentMethod: String,

    val amount: Long,

    @SerializedName("payment_reference")
    val paymentReference: String? = null,

    val status: String,

    @SerializedName("escrow_status")
    val escrowStatus: String,

    @SerializedName("paid_at")
    val paidAt: String? = null,

    @SerializedName("released_at")
    val releasedAt: String? = null,

    @SerializedName("refunded_at")
    val refundedAt: String? = null,

    @SerializedName("created_at")
    val createdAt: String
)
