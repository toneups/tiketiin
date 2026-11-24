
package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ConfirmTicketRequestDto(
    @SerializedName("p_order_id")
    val orderId: Int
)
