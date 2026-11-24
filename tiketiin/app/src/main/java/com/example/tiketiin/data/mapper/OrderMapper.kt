
package com.example.tiketiin.data.mapper

import com.example.tiketiin.data.model.Order
import com.example.tiketiin.data.model.OrderStatus
import com.example.tiketiin.data.remote.dto.OrderDto

fun String.toOrderStatus(): OrderStatus =
    when (uppercase().trim()) {
        "PENDING" -> OrderStatus.BELUM_BAYAR
        "PAID" -> OrderStatus.DIPROSES
        "COMPLETED" -> OrderStatus.SELESAI
        "CANCELLED" -> OrderStatus.DIBATALKAN
        "EXPIRED" -> OrderStatus.KEDALUWARSA

        else -> throw IllegalArgumentException(
            "Status pesanan tidak dikenal: $this"
        )
    }

fun OrderDto.toModel(): Order {
    val payment = payments?.firstOrNull()

    return Order(
        id = id,
        eventId = eventId,
        listingId = listingId,
        buyerId = buyerId,
        sellerId = sellerId,
        buyerName = buyerName,
        quantity = quantity,

        seatLabel = listing?.let {
            "${it.seatCategory}, ${it.seatRow}"
        } ?: "-",

        ticketPrice = unitPrice,
        serviceFee = serviceFee,

        total = totalPrice
            ?: (unitPrice * quantity + serviceFee),

        paymentMethod = payment?.paymentMethod ?: "-",
        transactionNo = orderCode,
        createdAt = createdAt,
        status = status.toOrderStatus(),
        qrCode = qrCode,
        rating = 0
    )
}
