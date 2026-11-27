
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
    val payment = payments

    return Order(
        id = id,
        eventId = eventId,
        listingId = listingId,
        buyerId = buyerId,
        sellerId = sellerId,
        buyerName = buyerName,
        buyerEmail = buyerEmail,
        buyerPhone = buyerPhone,
        quantity = quantity,

        seatLabel = listing?.let {
            "${it.seatCategory}, ${it.seatRow}"
        } ?: "-",

        ticketPrice = unitPrice,
        serviceFee = serviceFee,

        total = totalPrice
            ?: (unitPrice * quantity + serviceFee),

        paymentMethod = payment?.paymentMethod
            ?: paymentMethod
            ?: "-",

        paymentStatus = payment?.status ?: "PENDING",
        escrowStatus = payment?.escrowStatus ?: "PENDING",

        transactionNo = orderCode,
        createdAt = createdAt,
        status = status.toOrderStatus(),
        qrCode = qrCode,
        ticketUsedAt = ticketUsedAt,
        rating = 0
    )
}

