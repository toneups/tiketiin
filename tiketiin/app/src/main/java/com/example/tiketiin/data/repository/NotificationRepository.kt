
package com.example.tiketiin.data.repository

import com.example.tiketiin.data.auth.AuthenticatedRequest
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.remote.RetrofitClient
import com.example.tiketiin.data.remote.dto.NotificationDto
import com.example.tiketiin.data.remote.dto.NotificationReadUpdate

class NotificationRepository {

    suspend fun getMyNotifications(): List<NotificationDto> {
        val userId = requireNotNull(SessionManager.userId) {
            "Silakan login terlebih dahulu."
        }

        val notifications = AuthenticatedRequest.execute {
            RetrofitClient.api.getMyNotifications(
                userId = "eq.$userId"
            )
        }

        check(SessionManager.userId == userId) {
            "Sesi pengguna telah berubah."
        }

        check(notifications.all { it.userId == userId }) {
            "Data notifikasi tidak sesuai dengan akun."
        }

        return notifications
    }

    suspend fun markAsRead(
        notificationId: Int
    ): NotificationDto {
        require(notificationId > 0) {
            "ID notifikasi tidak valid."
        }

        val userId = requireNotNull(SessionManager.userId) {
            "Silakan login terlebih dahulu."
        }

        val updated = AuthenticatedRequest.execute {
            RetrofitClient.api.markNotificationAsRead(
                id = "eq.$notificationId",
                userId = "eq.$userId",
                body = NotificationReadUpdate(
                    isRead = true
                )
            )
        }

        check(SessionManager.userId == userId) {
            "Sesi pengguna telah berubah."
        }

        val notification = updated.singleOrNull()

        check(
            notification != null &&
                    notification.id == notificationId &&
                    notification.userId == userId &&
                    notification.isRead
        ) {
            "Notifikasi gagal ditandai sudah dibaca."
        }

        return notification
    }

    suspend fun getUnreadCount(): Int {
        val userId = requireNotNull(SessionManager.userId) {
            "Silakan login terlebih dahulu."
        }

        val response = AuthenticatedRequest.execute {
            RetrofitClient.api.countUnreadNotifications(
                userId = "eq.$userId"
            )
        }

        check(SessionManager.userId == userId) {
            "Sesi pengguna telah berubah."
        }

        check(response.isSuccessful) {
            "Gagal menghitung notifikasi: ${response.code()}"
        }

        val contentRange = response.headers()["Content-Range"]
            ?: error(
                "Server tidak mengembalikan jumlah notifikasi."
            )

        val total = contentRange
            .substringAfterLast("/")
            .toIntOrNull()
            ?: error(
                "Jumlah notifikasi dari server tidak valid."
            )

        return total
    }
}
