
package com.example.tiketiin.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "cached_hotels",
    primaryKeys = ["id", "eventId"]
)
data class HotelEntity(

    val id: Int,
    val eventId: Int,

    val name: String,
    val area: String,
    val address: String,
    val city: String,

    val latitude: Double,
    val longitude: Double,
    val pricePerNight: Long,
    val rating: Double,
    val availableRooms: Int,
    val colorStart: Long,
    val colorEnd: Long,
    val imageUrl: String?,
    val bookingUrl: String?,

    val cachedAt: Long = System.currentTimeMillis()
)
