
package com.example.tiketiin.data.repository

import com.example.tiketiin.data.auth.AuthenticatedRequest
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.remote.RetrofitClient
import com.example.tiketiin.data.remote.dto.PurchaseHistoryDto

class PurchaseHistoryRepository {

    suspend fun getPurchasedEventIds(): Set<Int> {
        val userId = requireNotNull(SessionManager.userId) {
            "Sesi pengguna tidak ditemukan."
        }

        val orders = AuthenticatedRequest.execute {
            RetrofitClient.api.getCompletedBuyerOrders(
                buyerId = "eq.$userId"
            )
        }

        check(SessionManager.userId == userId) {
            "Sesi pengguna berubah."
        }

        return orders
            .filter { order ->
                order.buyerId == userId &&
                        order.status == "PAID" &&
                        order.paidAt != null
            }
            .map { it.eventId }
            .toSet()
    }


    // ==========================================
    // RIWAYAT PEMBELIAN UNTUK STATISTIK PROFILE
    // ==========================================
    suspend fun getPurchaseHistory(): List<PurchaseHistoryDto> {
        val userId = requireNotNull(SessionManager.userId) {
            "Sesi pengguna tidak ditemukan."
        }

        val orders = AuthenticatedRequest.execute {
            RetrofitClient.api.getCompletedBuyerOrders(
                buyerId = "eq.$userId"
            )
        }

        check(SessionManager.userId == userId) {
            "Sesi pengguna berubah."
        }

        return orders.filter { order ->
            order.buyerId == userId &&
                    order.status in listOf("PAID", "COMPLETED") &&
                    order.paidAt != null
        }
    }
}
