
package com.example.tiketiin.data.repository

import com.example.tiketiin.data.auth.AuthenticatedRequest
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.remote.RetrofitClient
import com.example.tiketiin.data.remote.dto.CreateWishlistRequest
import java.io.IOException

class WishlistRepository {

    private fun requireUserId(): String {
        return requireNotNull(SessionManager.userId) {
            "Silakan login terlebih dahulu."
        }
    }

    suspend fun getSavedEventIds(): Set<Int> {
        return AuthenticatedRequest.execute {
            val userId = requireUserId()

            RetrofitClient.api.getWishlists(
                userId = "eq.$userId"
            )
                .map { it.eventId }
                .toSet()
        }
    }

    suspend fun addSavedEvent(eventId: Int) {
        require(eventId > 0) {
            "Event tidak valid."
        }

        AuthenticatedRequest.execute {
            val userId = requireUserId()

            val response = RetrofitClient.api.addWishlist(
                CreateWishlistRequest(
                    userId = userId,
                    eventId = eventId
                )
            )

            if (!response.isSuccessful) {
                throw IOException(
                    "Gagal menyimpan event (HTTP ${response.code()})."
                )
            }
        }
    }

    suspend fun removeSavedEvent(eventId: Int) {
        require(eventId > 0) {
            "Event tidak valid."
        }

        AuthenticatedRequest.execute {
            val userId = requireUserId()

            val response = RetrofitClient.api.deleteWishlist(
                userId = "eq.$userId",
                eventId = "eq.$eventId"
            )

            if (!response.isSuccessful) {
                throw IOException(
                    "Gagal menghapus event (HTTP ${response.code()})."
                )
            }
        }
    }
}
