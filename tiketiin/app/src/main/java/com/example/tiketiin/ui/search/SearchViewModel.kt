package com.example.tiketiin.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.repository.EventCatalogRepository
import com.example.tiketiin.data.repository.WishlistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate

data class SearchUiState(
    val query: String = "",
    val category: EventCategory? = null,
    val sortByCheapest: Boolean = false,
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
    val bookmarkError: String? = null,
    val results: List<EventSummary> = emptyList(),
    val mode: String = "all"
)

class SearchViewModel(
    private val eventRepository: EventCatalogRepository,
    private val wishlistRepository: WishlistRepository,
    initialCategory: EventCategory?,
    initialMode: String = "all"
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SearchUiState(
            category = initialCategory,
            mode = initialMode
        )
    )

    val uiState: StateFlow<SearchUiState> =
        _uiState.asStateFlow()

    private var searchJob: Job? = null

    private val busyEventIds = mutableSetOf<Int>()

    init {
        // Langsung mencoba memuat semua event saat Search dibuka.
        search(
            withDelay = false,
            showInitialError = false
        )
    }

    fun updateQuery(query: String) {
        _uiState.update {
            it.copy(query = query, bookmarkError = null)
        }

        search(withDelay = true)
    }

    fun selectCategory(category: EventCategory?) {
        _uiState.update {
            it.copy(category = category, bookmarkError = null)
        }

        search(withDelay = false)
    }

    fun toggleSort() {
        _uiState.update { state ->
            val cheapest = !state.sortByCheapest

            state.copy(
                sortByCheapest = cheapest,
                results = sortResults(state.results, cheapest)
            )
        }
    }

    fun refresh() {
        search(withDelay = false)
    }

    private fun resetSearch() {
        searchJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = false,
                hasSearched = false,
                errorMessage = null,
                bookmarkError = null,
                results = emptyList()
            )
        }
    }

    private fun search(
        withDelay: Boolean,
        showInitialError: Boolean = true
    ) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    hasSearched = true,
                    errorMessage = null,
                    results = emptyList()
                )
            }

            try {
                if (withDelay) {
                    delay(400)
                }

                val state = _uiState.value

                // Harus berhasil menghubungi Supabase.
                // Room tidak digunakan sebagai fallback Search.
                eventRepository.refreshEvents()

                val allEvents =
                    eventRepository.getCachedEvents()

                val savedIds =
                    wishlistRepository.getSavedEventIds()

                val today = LocalDate.now()
                val keyword = state.query.trim()

                val results = allEvents
                    .asSequence()
                    .filter { summary ->
                        !summary.event.date.isBefore(today) &&
                                summary.ticketCount > 0
                    }
                    .filter { summary ->
                        when (state.mode) {
                            "flash" ->
                                summary.event.flashEndsInHours != null

                            else -> true
                        }
                    }
                    .filter { summary ->
                        state.category == null ||
                                summary.event.category == state.category
                    }
                    .filter { summary ->
                        keyword.isBlank() ||
                                summary.event.name.contains(
                                    keyword,
                                    ignoreCase = true
                                ) ||
                                summary.event.city.contains(
                                    keyword,
                                    ignoreCase = true
                                ) ||
                                summary.event.venue.contains(
                                    keyword,
                                    ignoreCase = true
                                ) ||
                                summary.event.category.label.contains(
                                    keyword,
                                    ignoreCase = true
                                )
                    }
                    .map { summary ->
                        summary.copy(
                            isSaved = summary.event.id in savedIds
                        )
                    }
                    .toList()

                _uiState.update {
                    it.copy(
                        results = sortResults(
                            results,
                            it.sortByCheapest
                        ),
                        isLoading = false,
                        errorMessage = null
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: IOException) {
                if (showInitialError) {
                    showError(
                        "Koneksi internet bermasalah. Periksa koneksi lalu coba lagi."
                    )
                } else {
                    resetSearch()
                }

            } catch (e: Exception) {
                if (showInitialError) {
                    showError(
                        "Pencarian belum dapat dilakukan. Coba lagi."
                    )
                } else {
                    resetSearch()
                }
            }
        }
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                hasSearched = true,
                results = emptyList(),
                errorMessage = message
            )
        }
    }


    private fun sortResults(
        results: List<EventSummary>,
        cheapest: Boolean
    ): List<EventSummary> {

        if (cheapest) {
            return results.sortedBy { it.lowestPrice }
        }

        return when (_uiState.value.mode) {

            "flash" -> results.sortedBy {
                it.event.flashEndsInHours ?: Int.MAX_VALUE
            }

            "popular" -> results.sortedWith(
                compareByDescending<EventSummary> {
                    it.event.viewCount
                }.thenBy {
                    it.event.date
                }
            )

            else -> results.sortedBy {
                it.event.date
            }
        }
    }


    fun toggleSaved(eventId: Int) {
        if (!busyEventIds.add(eventId)) return

        viewModelScope.launch {
            try {
                val isSaved = _uiState.value.results
                    .firstOrNull { it.event.id == eventId }
                    ?.isSaved ?: return@launch

                if (isSaved) {
                    wishlistRepository.removeSavedEvent(eventId)
                } else {
                    wishlistRepository.addSavedEvent(eventId)
                }

                _uiState.update { state ->
                    state.copy(
                        bookmarkError = null,
                        results = state.results.map { item ->
                            if (item.event.id == eventId) {
                                item.copy(isSaved = !isSaved)
                            } else {
                                item
                            }
                        }
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        bookmarkError =
                            "Gagal mengubah bookmark. Periksa koneksi."
                    )
                }

            } finally {
                busyEventIds.remove(eventId)
            }
        }
    }
}