package com.example.tiketiin.data.repository

import com.example.tiketiin.data.auth.AuthenticatedRequest
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.remote.RetrofitClient
import com.example.tiketiin.data.remote.dto.TicketOfferDto

class MyOffersRepository {

    suspend fun load(): List<TicketOfferDto> {
        return AuthenticatedRequest.execute {
            val userId = requireNotNull(SessionManager.userId) {
                "Silakan login terlebih dahulu."
            }

            RetrofitClient.api.getBuyerTicketOffers(
                buyerId = "eq.$userId"
            )
        }
    }
}