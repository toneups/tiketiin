
package com.example.tiketiin.ui.hotel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.Hotel
import com.example.tiketiin.data.remote.dto.CreateHotelRequestDto
import com.example.tiketiin.data.remote.dto.UpdateHotelRequestDto
import com.example.tiketiin.data.repository.TiketiinRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HotelManagementUiState(
    val hotels: List<Hotel> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class HotelManagementViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HotelManagementUiState())
    val uiState: StateFlow<HotelManagementUiState> =
        _uiState.asStateFlow()

    // Memuat data hotel saat halaman pertama kali dibuka.
    fun loadHotels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val hotels = repository.getAllHotels()

                _uiState.value = _uiState.value.copy(
                    hotels = hotels,
                    isLoading = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal mengambil data hotel."
                )
            }
        }
    }

    // Memperbarui daftar hotel tanpa mengaktifkan loading halaman.
    private suspend fun refreshHotels() {
        try {
            val hotels = repository.getAllHotels()

            _uiState.value = _uiState.value.copy(
                hotels = hotels,
                error = null
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message ?: "Gagal memperbarui daftar hotel."
            )
        }
    }

    // CREATE HOTEL
    fun createHotel(request: CreateHotelRequestDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                error = null,
                message = null
            )

            try {
                repository.createHotel(request)
                refreshHotels()

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    message = "Hotel berhasil ditambahkan."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Gagal menambahkan hotel."
                )
            }
        }
    }

    // UPDATE HOTEL
    fun updateHotel(
        hotelId: Int,
        request: UpdateHotelRequestDto
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                error = null,
                message = null
            )

            try {
                repository.updateHotel(hotelId, request)
                refreshHotels()

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    message = "Hotel berhasil diperbarui."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Gagal memperbarui hotel."
                )
            }
        }
    }

    // DELETE HOTEL
    fun deleteHotel(hotelId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                error = null,
                message = null
            )

            try {
                repository.deleteHotel(hotelId)
                refreshHotels()

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    message = "Hotel berhasil dihapus."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Gagal menghapus hotel."
                )
            }
        }
    }
}
