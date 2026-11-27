package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SellerSalesOrderDto(
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("unit_price") val unitPrice: Long,
    val quantity: Int,
    val event: SalesEventDto
)

data class SalesEventDto(
    val name: String
)