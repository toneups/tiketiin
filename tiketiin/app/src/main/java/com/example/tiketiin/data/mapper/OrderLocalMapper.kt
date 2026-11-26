
package com.example.tiketiin.data.mapper

import com.example.tiketiin.data.local.entity.OrderEntity
import com.example.tiketiin.data.model.Order
import com.example.tiketiin.data.model.OrderStatus

fun Order.toEntity(): OrderEntity {
    return OrderEntity(
        id = id,
        eventId = eventId,
        listingId = listingId,
        buyerId = buyerId,
        sellerId = sellerId,
        buyerName = buyerName,
        buyerEmail = buyerEmail,
        buyerPhone = buyerPhone,
        quantity = quantity,
        seatLabel = seatLabel,
        ticketPrice = ticketPrice,
        serviceFee = serviceFee,
        total = total,
        paymentMethod = paymentMethod,
        paymentStatus = paymentStatus,
        escrowStatus = escrowStatus,
        transactionNo = transactionNo,
        createdAt = createdAt,
        status = status.name,
        qrCode = qrCode,
        ticketUsedAt = ticketUsedAt,
        rating = rating
    )
}

fun OrderEntity.toOrderModel(): Order {
    val orderStatus = runCatching {
        OrderStatus.valueOf(status)
    }.getOrDefault(OrderStatus.BELUM_BAYAR)

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
        seatLabel = seatLabel,
        ticketPrice = ticketPrice,
        serviceFee = serviceFee,
        total = total,
        paymentMethod = paymentMethod,
        paymentStatus = paymentStatus,
        escrowStatus = escrowStatus,
        transactionNo = transactionNo,
        createdAt = createdAt,
        status = orderStatus,
        qrCode = qrCode,
        ticketUsedAt = ticketUsedAt,
        rating = rating
    )
}

fun List<OrderEntity>.toOrderModelsFromLocal(): List<Order> {
    return map { it.toOrderModel() }
}
