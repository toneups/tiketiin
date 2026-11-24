
package com.example.tiketiin.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.repository.TiketiinRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TicketScannerUiState(
    val isLoading: Boolean = false,
    val isValid: Boolean? = null,
    val message: String = ""
)

class TicketScannerViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketScannerUiState())
    val uiState: StateFlow<TicketScannerUiState> =
        _uiState.asStateFlow()

    fun validateTicket(qrCode: String) {
        if (qrCode.isBlank()) {
            _uiState.value = TicketScannerUiState(
                isValid = false,
                message = "QR Code tidak boleh kosong."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = TicketScannerUiState(
                isLoading = true
            )

            try {
                val result = repository.validateTicket(qrCode.trim())

                _uiState.value = TicketScannerUiState(
                    isLoading = false,
                    isValid = result == "VALID",
                    message = if (result == "VALID") {
                        "Tiket valid. Silakan masuk."
                    } else {
                        "Tiket tidak valid."
                    }
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = TicketScannerUiState(
                    isLoading = false,
                    isValid = false,
                    message = e.message ?: "Gagal memvalidasi tiket."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = TicketScannerUiState()
    }
}
