
package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReviewDto(
    val id: Long,

    @SerializedName("order_id")
    val orderId: Int,

    @SerializedName("buyer_id")
    val buyerId: String,

    @SerializedName("seller_id")
    val sellerId: String,

    val rating: Int,

    val comment: String? = null,

    @SerializedName("created_at")
    val createdAt: String
)

data class CreateReviewRequestDto(
    @SerializedName("order_id")
    val orderId: Int,

    @SerializedName("buyer_id")
    val buyerId: String,

    @SerializedName("seller_id")
    val sellerId: String,

    val rating: Int,

    val comment: String? = null
)
