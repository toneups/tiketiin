package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GetAvailableHotelsRequestDto(

    @SerializedName("p_event_id")
    val eventId: Int

)
