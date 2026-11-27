package com.example.tiketiin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_listings")
data class CachedListingEntity(
    @PrimaryKey val id: Int,
    val eventId: Int,
    val sellerId: String,
    val seatCategory: String,
    val seatRow: String,
    val faceValue: Long,
    val price: Long,
    val quantity: Int,
    val status: String,
    val views: Int,
    val saves: Int,
    val offers: Int
)