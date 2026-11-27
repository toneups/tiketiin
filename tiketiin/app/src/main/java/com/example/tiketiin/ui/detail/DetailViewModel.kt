package com.example.tiketiin.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.model.TicketListing
import com.example.tiketiin.data.repository.EventCatalogRepository
import com.example.tiketiin.data.repository.TiketiinRepository
import com.example.tiketiin.data.repository.WishlistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class DetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val actionError: String? = null,
    val summary: EventSummary? = null,
    val listings: List<TicketListing> = emptyList(),
    val listing: TicketListing? = null,
    val selectedListingId: Int? = null,
    val quantity: Int = 1,
    val selectedTab: Int = 0,
    val isSaving: Boolean = false
) {
    val totalPrice: Long
        get() = (listing?.price ?: 0L) * quantity
}

class DetailViewModel(
    private val eventRepository: EventCatalogRepository,
    private val wishlistRepository: WishlistRepository,
    // Dipakai untuk mencatat jumlah view (statistik Hot Opportunity).
    private val repository: TiketiinRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())

    val uiState: StateFlow<DetailUiState> =
        _uiState.asStateFlow()

    private var loadJob: Job? = null

    // Event yang view-nya sudah dicatat selama ViewModel ini hidup
    private val viewedEventIds = mutableSetOf<Int>()

    fun load(eventId: Int) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    actionError = null,
                    summary = null,
                    listing = null,
                    listings = emptyList(),
                    selectedListingId = null,
                    quantity = 1
                )
            }

            try {
                require(eventId > 0) {
                    "ID event tidak valid."
                }

                // Ambil data terbaru dari Supabase
                eventRepository.refreshEvents()

                // Ambil ringkasan event dari cache Room
                val summary = eventRepository.getCachedEvents()
                    .firstOrNull { it.event.id == eventId }
                    ?: throw NoSuchElementException(
                        "Event tidak ditemukan."
                    )

                // Ambil bookmark dari akun yang sedang login
                val savedIds =
                    wishlistRepository.getSavedEventIds()

                // Ambil listing tiket aktif dari Supabase
                val listings =
                    eventRepository.getActiveTicketListings(eventId)

                // Pilih listing termurah sebagai pilihan awal
                val selectedListing =
                    listings.minByOrNull { it.price }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        summary = summary.copy(
                            isSaved = eventId in savedIds
                        ),
                        listings = listings,
                        listing = selectedListing,
                        selectedListingId = selectedListing?.id,
                        quantity = 1
                    )
                }

                recordViewOnce(eventId)

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = null,
                        listing = null,
                        listings = emptyList(),
                        selectedListingId = null,
                        errorMessage = when (e) {
                            is IOException ->
                                "Koneksi bermasalah. Periksa internet."

                            is NoSuchElementException ->
                                "Event tidak ditemukan."

                            else ->
                                "Gagal memuat detail event: ${e.message}"
                        }
                    )
                }
            }
        }
    }

    // Mencatat view ke Supabase. Gagal mencatat tidak boleh mengganggu halaman.
    private suspend fun recordViewOnce(eventId: Int) {
        val repo = repository ?: return
        if (!viewedEventIds.add(eventId)) return

        try {
            repo.recordRemoteView(eventId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            viewedEventIds.remove(eventId)
            android.util.Log.w("DetailViewModel", "Gagal mencatat view event", e)
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update {
            it.copy(selectedTab = tab.coerceIn(0, 2))
        }
    }

    // Memilih listing tiket
    fun selectListing(listingId: Int) {
        val selected = _uiState.value.listings
            .firstOrNull { it.id == listingId }
            ?: return

        _uiState.update {
            it.copy(
                listing = selected,
                selectedListingId = selected.id,
                quantity = 1
            )
        }
    }

    // Mengubah jumlah tiket sesuai stok listing
    fun changeQuantity(delta: Int) {
        _uiState.update { state ->
            val max = state.listing?.quantity ?: 1

            state.copy(
                quantity = (state.quantity + delta)
                    .coerceIn(1, max.coerceAtLeast(1))
            )
        }
    }

    // Menambah atau menghapus event dari wishlist
    fun toggleSaved() {
        val summary = _uiState.value.summary ?: return

        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    actionError = null
                )
            }

            try {
                val eventId = summary.event.id

                if (summary.isSaved) {
                    wishlistRepository.removeSavedEvent(eventId)
                } else {
                    wishlistRepository.addSavedEvent(eventId)
                }

                _uiState.update { state ->
                    state.copy(
                        summary = state.summary?.copy(
                            isSaved = !summary.isSaved
                        ),
                        isSaving = false
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        actionError =
                            "Gagal mengubah bookmark. Periksa koneksi."
                    )
                }
            }
        }
    }
}