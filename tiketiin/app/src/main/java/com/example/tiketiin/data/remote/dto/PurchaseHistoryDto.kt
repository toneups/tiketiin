
package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PurchaseHistoryDto(
    val id: Int,

    @SerializedName("buyer_id")
    val buyerId: String,

    @SerializedName("event_id")
    val eventId: Int,

    val status: String,

    val quantity: Int = 0,

    @SerializedName("unit_price")
    val unitPrice: Long = 0L,

    @SerializedName("original_unit_price")
    val originalUnitPrice: Long? = null,

    @SerializedName("service_fee")
    val serviceFee: Long = 0L,

    @SerializedName("total_price")
    val totalPrice: Long = 0L,

    @SerializedName("membership_level")
    val membershipLevel: String? = null,

    @SerializedName("membership_discount_percent")
    val membershipDiscountPercent: Double = 0.0,

    @SerializedName("membership_discount_amount")
    val membershipDiscountAmount: Long = 0L,

    @SerializedName("membership_eligible_amount")
    val membershipEligibleAmount: Long = 0L,

    @SerializedName("paid_at")
    val paidAt: String?
)
