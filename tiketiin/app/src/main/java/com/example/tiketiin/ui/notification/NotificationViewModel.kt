
package com.example.tiketiin.ui.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.remote.dto.NotificationDto
import com.example.tiketiin.data.repository.NotificationRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class NotificationUiState(
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val notifications: List<NotificationDto> = emptyList(),
    val errorMessage: String? = null,
    val actionErrorMessage: String? = null
) {
    val unreadCount: Int
        get() = notifications.count { !it.isRead }
}

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())

    val uiState: StateFlow<NotificationUiState> =
        _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var loadedUserId: String? = null

    fun load() {
        val currentUserId = SessionManager.userId

        if (loadedUserId != currentUserId) {
            loadJob?.cancel()
            loadedUserId = currentUserId
            _uiState.value = NotificationUiState()
        }

        if (currentUserId == null) {
            _uiState.value = NotificationUiState(
                errorMessage = "Silakan login terlebih dahulu."
            )
            return
        }

        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            _uiState.value = NotificationUiState(
                isLoading = true
            )

            try {
                val result = repository.getMyNotifications()

                if (
                    SessionManager.userId != currentUserId ||
                    loadedUserId != currentUserId
                ) {
                    return@launch
                }

                _uiState.value = NotificationUiState(
                    isLoaded = true,
                    notifications = result
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (
                    SessionManager.userId != currentUserId ||
                    loadedUserId != currentUserId
                ) {
                    return@launch
                }

                Log.e(
                    "TiketiinNotification",
                    "Gagal mengambil notifikasi dari Supabase",
                    e
                )

                val message = when (e) {
                    is HttpException -> when (e.code()) {
                        401 ->
                            "Sesi login berakhir. Silakan login kembali."

                        403 ->
                            "Akses notifikasi ditolak oleh server."

                        else ->
                            "Server gagal memuat notifikasi (${e.code()})."
                    }

                    is IOException ->
                        "Tidak dapat terhubung ke server. Periksa koneksi internet."

                    else ->
                        "Notifikasi belum berhasil dimuat. Silakan coba lagi."
                }

                _uiState.value = NotificationUiState(
                    errorMessage = message
                )
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        val currentUserId = SessionManager.userId ?: return

        val current = _uiState.value.notifications
            .firstOrNull { it.id == notificationId }
            ?: return

        if (current.isRead || loadedUserId != currentUserId) {
            return
        }

        viewModelScope.launch {
            try {
                val updated = repository.markAsRead(
                    notificationId
                )

                if (
                    SessionManager.userId != currentUserId ||
                    loadedUserId != currentUserId
                ) {
                    return@launch
                }

                _uiState.update { state ->
                    state.copy(
                        notifications = state.notifications.map {
                            if (it.id == updated.id) updated else it
                        },
                        actionErrorMessage = null
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (
                    SessionManager.userId != currentUserId ||
                    loadedUserId != currentUserId
                ) {
                    return@launch
                }

                _uiState.update { state ->
                    state.copy(
                        actionErrorMessage =
                            "Gagal menandai notifikasi. Coba lagi."
                    )
                }
            }
        }
    }

    fun clear() {
        loadJob?.cancel()
        loadedUserId = null
        _uiState.value = NotificationUiState()
    }
}
