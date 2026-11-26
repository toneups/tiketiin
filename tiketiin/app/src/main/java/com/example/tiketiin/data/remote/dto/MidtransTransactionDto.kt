
package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateMidtransTransactionRequest(
    @SerializedName("order_id")
    val orderId: Int
)

data class MidtransTransactionResponse(
    val success: Boolean,

    val message: String? = null,

    val error: String? = null,

    @SerializedName("order_id")
    val orderId: Int? = null,

    @SerializedName("snap_token")
    val snapToken: String? = null,

    @SerializedName("redirect_url")
    val redirectUrl: String? = null,

    @SerializedName("gross_amount")
    val grossAmount: Long? = null
)
