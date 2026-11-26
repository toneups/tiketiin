
package com.example.tiketiin.data.remote

import com.example.tiketiin.data.remote.api.SupabaseApiService
import com.example.tiketiin.data.remote.dto.ConfirmTicketRequestDto
import com.example.tiketiin.data.remote.dto.CreateOrderRequestDto
import com.example.tiketiin.data.remote.dto.OrderDto
import com.example.tiketiin.data.remote.dto.PayOrderRequestDto
import com.example.tiketiin.data.remote.dto.ValidateTicketRequestDto
import com.example.tiketiin.data.remote.dto.RefundOrderRequestDto

class OrderRemoteDataSource(
    private val api: SupabaseApiService = RetrofitClient.api
) {

    suspend fun createOrder(
        listingId: Int,
        quantity: Int,
        buyerName: String,
        buyerEmail: String,
        buyerPhone: String,
        paymentMethod: String
    ): Int {
        require(listingId > 0) {
            "ID listing tidak valid."
        }
        require(quantity > 0) {
            "Jumlah tiket harus lebih dari 0."
        }

        val request = CreateOrderRequestDto(
            listingId = listingId,
            quantity = quantity,
            buyerName = buyerName,
            buyerEmail = buyerEmail,
            buyerPhone = buyerPhone,
            paymentMethod = paymentMethod
        )

        val orderId = api.createOrder(request)

        check(orderId > 0) {
            "Gagal membuat pesanan: ID pesanan tidak valid."
        }

        return orderId
    }

    suspend fun getOrderById(orderId: Int): OrderDto? {
        require(orderId > 0) {
            "ID pesanan tidak valid."
        }

        return api.getOrderById(
            id = "eq.$orderId"
        ).firstOrNull()
    }

    suspend fun payOrder(
        orderId: Int,
        paymentMethod: String
    ): String {
        require(orderId > 0) {
            "ID pesanan tidak valid."
        }
        require(paymentMethod.isNotBlank()) {
            "Metode pembayaran tidak boleh kosong."
        }

        val request = PayOrderRequestDto(
            orderId = orderId,
            paymentMethod = paymentMethod
        )

        return api.payOrder(request).also {
            check(it.isNotBlank()) {
                "Respons pembayaran kosong."
            }
        }
    }

    suspend fun getBuyerOrders(buyerId: String): List<OrderDto> {
        require(buyerId.isNotBlank()) {
            "ID pembeli tidak valid."
        }

        return api.getBuyerOrders(
            buyerId = "eq.$buyerId"
        )
    }

    suspend fun getSellerOrders(sellerId: String): List<OrderDto> {
        require(sellerId.isNotBlank()) {
            "ID penjual tidak valid."
        }

        return api.getSellerOrders(
            sellerId = "eq.$sellerId"
        )
    }

    suspend fun confirmTicket(orderId: Int): String {
        require(orderId > 0) {
            "ID pesanan tidak valid."
        }

        return api.confirmTicket(
            ConfirmTicketRequestDto(orderId = orderId)
        ).also {
            check(it.isNotBlank()) {
                "Respons konfirmasi tiket kosong."
            }
        }
    }

    suspend fun validateTicket(qrCode: String): String {
        require(qrCode.isNotBlank()) {
            "QR Code tidak boleh kosong."
        }

        val request = ValidateTicketRequestDto(
            qrCode = qrCode
        )

        return api.validateTicket(request).also {
            check(it.isNotBlank()) {
                "Respons validasi tiket kosong."
            }
        }
    }

    suspend fun refundOrder(orderId: Int): Boolean {
        require(orderId > 0) {
            "ID pesanan tidak valid."
        }

        val request = mapOf(
            "p_order_id" to orderId
        )

        return api.refundOrder(request)
    }
}
