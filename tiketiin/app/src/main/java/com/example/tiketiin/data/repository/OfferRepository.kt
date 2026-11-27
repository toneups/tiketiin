package com.example.tiketiin.data.repository

import com.example.tiketiin.data.auth.AuthenticatedRequest
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.remote.RetrofitClient

class OfferRepository {

    suspend fun submit(
        listingId: Int,
        pricePerTicket: Long,
        quantity: Int
    ): Long {
        require(listingId > 0) { "Listing tidak valid." }
        require(pricePerTicket > 0) { "Harga harus lebih dari nol." }
        require(quantity > 0) { "Jumlah tiket tidak valid." }

        return AuthenticatedRequest.execute {
            requireNotNull(SessionManager.userId) {
                "Silakan login terlebih dahulu."
            }

            RetrofitClient.api.submitTicketOffer(
                request = mapOf(
                    "p_listing_id" to listingId.toLong(),
                    "p_price_per_ticket" to pricePerTicket,
                    "p_quantity" to quantity.toLong()
                )
            )
        }
    }
}