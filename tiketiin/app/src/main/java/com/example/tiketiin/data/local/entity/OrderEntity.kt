
package com.example.tiketiin.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_orders")
data class OrderEntity(

    @PrimaryKey
    val id: Int,

    val eventId: Int,
    val listingId: Int,
    val buyerId: String,
    val sellerId: String,

    val buyerName: String,
    @ColumnInfo(defaultValue = "''")
    val buyerEmail: String,
    @ColumnInfo(defaultValue = "''")
    val buyerPhone: String,

    val quantity: Int,
    val seatLabel: String,

    val ticketPrice: Long,
    val serviceFee: Long,
    val total: Long,

    val paymentMethod: String,
    val transactionNo: String,
    val createdAt: String,

    // Enum disimpan sebagai String di Room
    val status: String,

    val qrCode: String? = null,
    val ticketUsedAt: String? = null,

    val rating: Int = 0,

    val cachedAt: Long = System.currentTimeMillis()
)
