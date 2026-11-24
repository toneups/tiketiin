
package com.example.tiketiin.data.repository

import com.example.tiketiin.data.auth.AuthenticatedRequest
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.remote.RetrofitClient

class PurchaseHistoryRepository {

    suspend fun getPurchasedEventIds(): Set<Int> {

        val userId = requireNotNull(
            SessionManager.userId
        ) {
            "Sesi pengguna tidak ditemukan."
        }

        val orders = AuthenticatedRequest.execute {
            RetrofitClient.api.getCompletedBuyerOrders(
                buyerId = "eq.$userId"
            )
        }

        // Pastikan sesi belum berganti saat request berjalan.
        check(SessionManager.userId == userId) {
            "Sesi pengguna berubah."
        }

        return orders
            .filter { order ->
                order.buyerId == userId &&
                        order.status == "COMPLETED" &&
                        order.paidAt != null
            }
            .map { it.eventId }
            .toSet()
    }
}
