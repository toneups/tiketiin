package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ValidateTicketRequestDto(
    @SerializedName("p_qr_code")
    val qrCode: String
)