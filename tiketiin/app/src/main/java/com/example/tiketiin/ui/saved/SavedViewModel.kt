
package com.example.tiketiin.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.repository.SavedEventsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class SavedUiState(
    val isLoading: Boolean = false,
    val items: List<EventSummary> = emptyList(),
    val selectedCategory: EventCategory? = null,
    val selectedEventId: Int? = null,
    val removingEventId: Int? = null,
    val errorMessage: String? = null
) {
    val visibleItems: List<EventSummary>
        get() = if (selectedCategory == null) {
            items
        } else {
            items.filter {
                it.event.category == selectedCategory
            }
        }

    val total: Long
        get() = items.find {
            it.event.id == selectedEventId
        }?.lowestPrice ?: 0L
}

class SavedViewModel(
    private val repository: SavedEventsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedUiState())

    val uiState: StateFlow<SavedUiState> =
        _uiState.asStateFlow()

    fun load() {
        if (_uiState.value.isLoading ||
            _uiState.value.removingEventId != null
        ) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val saved = repository.getSavedEvents()

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        items = saved,
                        selectedEventId =
                            state.selectedEventId?.takeIf { id ->
                                saved.any { it.event.id == id }
                            },
                        errorMessage = null
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        // Jangan tampilkan daftar lama seolah
                        // sudah berhasil diverifikasi.
                        items = emptyList(),
                        selectedEventId = null,
                        errorMessage = errorFor(e)
                    )
                }
            }
        }
    }

    fun selectCategory(category: EventCategory?) {
        _uiState.update {
            it.copy(selectedCategory = category)
        }
    }

    fun toggleSelected(eventId: Int) {
        if (_uiState.value.isLoading ||
            _uiState.value.removingEventId != null
        ) return

        _uiState.update { state ->
            state.copy(
                selectedEventId =
                    if (state.selectedEventId == eventId) {
                        null
                    } else {
                        eventId
                    }
            )
        }
    }

    fun remove(eventId: Int) {
        val current = _uiState.value

        if (current.isLoading ||
            current.removingEventId != null ||
            current.items.none { it.event.id == eventId }
        ) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    removingEventId = eventId,
                    errorMessage = null
                )
            }

            try {
                repository.removeSavedEvent(eventId)

                _uiState.update { state ->
                    state.copy(
                        removingEventId = null,
                        items = state.items.filterNot {
                            it.event.id == eventId
                        },
                        selectedEventId =
                            state.selectedEventId.takeIf {
                                it != eventId
                            },
                        errorMessage = null
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        removingEventId = null,
                        errorMessage = errorFor(e)
                    )
                }
            }
        }
    }

    private fun errorFor(e: Exception): String {
        return when (e) {
            is HttpException -> when (e.code()) {
                401 -> "Sesi tidak valid. Silakan login kembali."
                403 -> "Akses wishlist ditolak."
                else -> "Kesalahan server (${e.code()})."
            }

            is IOException ->
                "Koneksi bermasalah. Periksa internet dan coba lagi."

            else ->
                "Wishlist gagal diproses. Silakan coba lagi."
        }
    }
}
