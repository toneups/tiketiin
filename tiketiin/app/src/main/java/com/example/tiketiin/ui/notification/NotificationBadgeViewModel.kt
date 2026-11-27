
package com.example.tiketiin.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.repository.NotificationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationBadgeUiState(
    val unreadCount: Int? = null,
    val isLoading: Boolean = false,
    val isShowingCache: Boolean = false
)

class NotificationBadgeViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(NotificationBadgeUiState())

    val uiState = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    // Menjaga agar data tidak tertukar antar-akun.
    private var cachedUserId: String? = null

    fun refresh() {
        val userId = SessionManager.userId

        // Jika akun berubah, hapus data akun sebelumnya.
        if (cachedUserId != userId) {
            refreshJob?.cancel()
            cachedUserId = userId
            _uiState.value = NotificationBadgeUiState()
        }

        if (userId == null) {
            _uiState.value = NotificationBadgeUiState()
            return
        }

        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {

            // Pertahankan angka lama selama refresh.
            _uiState.update {
                it.copy(isLoading = true)
            }

            try {
                val count = repository.getUnreadCount()

                // Pastikan akun masih sama.
                if (
                    SessionManager.userId != userId ||
                    cachedUserId != userId
                ) {
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        unreadCount = count,
                        isLoading = false,
                        isShowingCache = false
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {

                // Jangan hapus jumlah terakhir saat offline.
                if (
                    SessionManager.userId == userId &&
                    cachedUserId == userId
                ) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isShowingCache =
                                it.unreadCount != null
                        )
                    }
                }
            }
        }
    }

    fun clear() {
        refreshJob?.cancel()
        cachedUserId = null
        _uiState.value = NotificationBadgeUiState()
    }
}
