package com.example.tiketiin.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.model.TicketListing
import com.example.tiketiin.data.repository.TiketiinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val summary: EventSummary? = null,
    val listing: TicketListing? = null,
    val listings: List<TicketListing> = emptyList(),
    val selectedListingId: Int? = null,
    val quantity: Int = 1,
    val selectedTab: Int = 0
) {
    val totalPrice: Long get() = (listing?.price ?: 0L) * quantity
}

class DetailViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var viewRecorded = false

    fun load(eventId: Int) {
        viewModelScope.launch {
            // Catat satu kali "dilihat" (bahan skor demand Hot Opportunity).
            if (!viewRecorded) {
                repository.recordView(eventId)
                viewRecorded = true
            }
            val summary = repository.getEventSummary(eventId)
            if (summary == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Event tidak ditemukan") }
                return@launch
            }
            val listings = repository.getListings(eventId)
            val listing = listings.minByOrNull { it.price }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    summary = summary,
                    listing = listing,
                    listings = listings,
                    selectedListingId = listing?.id,
                    quantity = it.quantity.coerceIn(
                        1,
                        (listing?.quantity ?: 1).coerceAtLeast(1)
                    )
                )
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectListing(listingId: Int) {
        val selected = _uiState.value.listings
            .find { it.id == listingId }
            ?: return

        _uiState.update {
            it.copy(
                listing = selected,
                selectedListingId = selected.id,
                quantity = it.quantity.coerceIn(
                    1,
                    selected.quantity.coerceAtLeast(1)
                )
            )
        }
    }

    fun changeQuantity(delta: Int) {
        _uiState.update {
            val max = (it.listing?.quantity ?: 1).coerceAtLeast(1)
            it.copy(quantity = (it.quantity + delta).coerceIn(1, max))
        }
    }

    fun toggleSaved() {
        val eventId = _uiState.value.summary?.event?.id ?: return
        viewModelScope.launch {
            repository.toggleSaved(eventId)
            load(eventId)
        }
    }
}
