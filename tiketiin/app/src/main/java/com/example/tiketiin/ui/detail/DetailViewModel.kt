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
import com.example.tiketiin.data.repository.OfferRepository

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
    val isSaving: Boolean = false,
    val isSubmittingOffer: Boolean = false,
    val offerError: String? = null,
    val submittedOfferId: Long? = null
) {
    val totalPrice: Long
        get() = (listing?.price ?: 0L) * quantity
}

class DetailViewModel(
    private val eventRepository: EventCatalogRepository,
    private val wishlistRepository: WishlistRepository,
    // Dipakai untuk mencatat view event (bahan skor Hot Opportunity).
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())

    val uiState: StateFlow<DetailUiState> =
        _uiState.asStateFlow()

    private var loadJob: Job? = null

    // View dicatat sekali per layar detail yang dibuka.
    private var viewRecorded = false

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

                // Catat view setelah detail berhasil tampil. Kegagalan
                // pencatatan tidak boleh mengganggu layar detail.
                if (!viewRecorded) {
                    try {
                        repository.recordRemoteView(eventId)
                        viewRecorded = true
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        android.util.Log.w(
                            "DetailViewModel",
                            "Gagal mencatat view event",
                            e
                        )
                    }
                }

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
    private val offerRepository = OfferRepository()

    fun resetOfferResult() {
        if (_uiState.value.isSubmittingOffer) return

        _uiState.update {
            it.copy(
                offerError = null,
                submittedOfferId = null
            )
        }
    }

    fun submitOffer(
        listingId: Int,
        pricePerTicket: Long,
        quantity: Int
    ) {
        val state = _uiState.value
        if (state.isSubmittingOffer) return

        val selected = state.listings.firstOrNull {
            it.id == listingId
        }

        val validationError = when {
            selected == null -> "Tiket tidak ditemukan."
            quantity <= 0 || quantity > selected.quantity ->
                "Jumlah tiket melebihi stok."
            pricePerTicket <= 0 || pricePerTicket >= selected.price ->
                "Penawaran harus lebih dari nol dan di bawah harga tiket."
            else -> null
        }

        if (validationError != null) {
            _uiState.update { it.copy(offerError = validationError) }
            return
        }

        _uiState.update {
            it.copy(
                isSubmittingOffer = true,
                offerError = null,
                submittedOfferId = null
            )
        }

        viewModelScope.launch {
            try {
                val offerId = offerRepository.submit(
                    listingId,
                    pricePerTicket,
                    quantity
                )

                _uiState.update {
                    it.copy(
                        isSubmittingOffer = false,
                        submittedOfferId = offerId
                    )
                }
            } catch (e: CancellationException) {
                _uiState.update { it.copy(isSubmittingOffer = false) }
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmittingOffer = false,
                        offerError = "Penawaran gagal dikirim. Coba lagi."
                    )
                }
            }
        }
    }
}