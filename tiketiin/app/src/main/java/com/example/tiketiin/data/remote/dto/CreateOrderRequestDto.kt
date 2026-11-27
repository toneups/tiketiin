package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateOrderRequestDto(

    @SerializedName("p_listing_id")
    val listingId: Int,

    @SerializedName("p_quantity")
    val quantity: Int,

    @SerializedName("p_buyer_name")
    val buyerName: String,

    @SerializedName("p_buyer_email")
    val buyerEmail: String,

    @SerializedName("p_buyer_phone")
    val buyerPhone: String,

    @SerializedName("p_payment_method")
    val paymentMethod: String
)
