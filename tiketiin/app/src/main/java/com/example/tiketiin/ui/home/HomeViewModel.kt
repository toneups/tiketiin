package com.example.tiketiin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.repository.EventCatalogRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException
import com.example.tiketiin.data.repository.WishlistRepository

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val banner: EventSummary? = null,
    val flashTickets: List<EventSummary> = emptyList(),
    val popularEvents: List<EventSummary> = emptyList(),
    val selectedCategory: EventCategory? = null,

    // Status data cache
    val isShowingCache: Boolean = false,
    val isRefreshing: Boolean = false,
    val savedEventIds: Set<Int> = emptySet(),
    val isWishlistLoading: Boolean = false,
    val isWishlistLoaded: Boolean = false,
    val wishlistBusyIds: Set<Int> = emptySet(),
    val wishlistError: String? = null
) {
    val filteredPopular: List<EventSummary>
        get() = if (selectedCategory == null) {
            popularEvents
        } else {
            popularEvents.filter {
                it.event.category == selectedCategory
            }
        }
}

class HomeViewModel(
    private val repository: EventCatalogRepository,
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load() {

        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = it.popularEvents.isEmpty(),
                    isRefreshing = true,
                    errorMessage = null
                )
            }

            var hasCachedData = false

            // 1. Membaca cache dari Room
            try {

                val cachedEvents =
                    repository.getCachedEvents()

                if (cachedEvents.isNotEmpty()) {
                    hasCachedData = true

                    showEvents(
                        events = cachedEvents,
                        fromCache = true
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Tetap mencoba mengambil data remote
            }

            // 2. Mengambil data terbaru dari Supabase
            try {

                repository.refreshEvents()

                val updatedEvents =
                    repository.getCachedEvents()

                showEvents(
                    events = updatedEvents,
                    fromCache = false
                )

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                val hasData = hasCachedData ||
                        _uiState.value.popularEvents.isNotEmpty()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isShowingCache = hasData,
                        errorMessage = if (hasData) {
                            null
                        } else {
                            "Gagal memuat event. Periksa koneksi dan coba lagi."
                        }
                    )
                }

            } finally {

                _uiState.update {
                    it.copy(isRefreshing = false)
                }
            }
        }
    }

    private fun showEvents(
        events: List<EventSummary>,
        fromCache: Boolean
    ) {

        val today = LocalDate.now()

        val active = events.filter {
            !it.event.date.isBefore(today) &&
                    it.ticketCount > 0
        }

        val popular = active.sortedByDescending {
            it.event.viewCount
        }

        _uiState.update { state ->

            state.copy(
                isLoading = false,
                errorMessage = null,
                isShowingCache = fromCache,
                banner = popular.firstOrNull(),

                flashTickets = active
                    .filter {
                        it.event.flashEndsInHours != null
                    }
                    .sortedBy {
                        it.event.flashEndsInHours
                    },

                popularEvents = popular
            )
        }
    }

    fun selectCategory(category: EventCategory?) {

        _uiState.update {
            it.copy(selectedCategory = category)
        }
    }


    fun loadWishlist() {
        val current = _uiState.value

        if (current.isWishlistLoading ||
            current.wishlistBusyIds.isNotEmpty()
        ) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isWishlistLoading = true,
                    wishlistError = null
                )
            }

            try {
                val savedIds =
                    wishlistRepository.getSavedEventIds()

                _uiState.update {
                    it.copy(
                        savedEventIds = savedIds,
                        isWishlistLoading = false,
                        isWishlistLoaded = true,
                        wishlistError = null
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isWishlistLoading = false,
                        isWishlistLoaded = false,
                        wishlistError =
                            "Gagal memuat bookmark. Coba lagi."
                    )
                }
            }
        }
    }

    fun toggleSaved(eventId: Int) {
        val current = _uiState.value

        if (!current.isWishlistLoaded ||
            current.isWishlistLoading ||
            eventId in current.wishlistBusyIds
        ) return

        // Hanya event yang benar-benar ada pada
        // katalog Beranda yang boleh disimpan.
        val existsInCatalog =
            current.popularEvents.any { it.event.id == eventId } ||
                    current.flashTickets.any { it.event.id == eventId }

        if (!existsInCatalog) return

        viewModelScope.launch {
            val wasSaved =
                eventId in _uiState.value.savedEventIds

            _uiState.update {
                it.copy(
                    wishlistBusyIds =
                        it.wishlistBusyIds + eventId,
                    wishlistError = null
                )
            }

            try {
                if (wasSaved) {
                    wishlistRepository.removeSavedEvent(eventId)
                } else {
                    wishlistRepository.addSavedEvent(eventId)
                }

                _uiState.update {
                    it.copy(
                        savedEventIds =
                            if (wasSaved) {
                                it.savedEventIds - eventId
                            } else {
                                it.savedEventIds + eventId
                            },
                        wishlistError = null
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        wishlistError =
                            "Bookmark gagal diperbarui. Coba lagi."
                    )
                }

            } finally {
                _uiState.update {
                    it.copy(
                        wishlistBusyIds =
                            it.wishlistBusyIds - eventId
                    )
                }
            }
        }
    }

}