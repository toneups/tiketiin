
package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderDto(
    val id: Int,

    val payments: List<PaymentDto>? = null,

    val listing: OrderListingDto? = null,

    @SerializedName("order_code")
    val orderCode: String,

    @SerializedName("buyer_id")
    val buyerId: String,

    @SerializedName("seller_id")
    val sellerId: String,

    @SerializedName("event_id")
    val eventId: Int,

    @SerializedName("listing_id")
    val listingId: Int,

    val quantity: Int,

    @SerializedName("unit_price")
    val unitPrice: Long,

    @SerializedName("service_fee")
    val serviceFee: Long,

    @SerializedName("total_price")
    val totalPrice: Long?,

    @SerializedName("buyer_name")
    val buyerName: String,

    @SerializedName("buyer_email")
    val buyerEmail: String,

    @SerializedName("buyer_phone")
    val buyerPhone: String,

    val status: String,

    @SerializedName("payment_method")
    val paymentMethod: String? = null,

    @SerializedName("qr_code")
    val qrCode: String? = null,

    @SerializedName("expires_at")
    val expiresAt: String,

    @SerializedName("paid_at")
    val paidAt: String? = null,

    @SerializedName("completed_at")
    val completedAt: String? = null,

    @SerializedName("ticket_used_at")
    val ticketUsedAt: String? = null,

    @SerializedName("created_at")
    val createdAt: String
)
