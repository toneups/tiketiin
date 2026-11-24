package com.example.tiketiin.ui.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.ListingStatus
import com.example.tiketiin.data.model.OrderStatus
import com.example.tiketiin.data.model.TicketListing
import com.example.tiketiin.data.repository.TiketiinRepository
import com.example.tiketiin.domain.FairPriceCalculator
import com.example.tiketiin.domain.PriceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException

// ---------------------------------------------------------------------------
// Kelola Event
// ---------------------------------------------------------------------------

data class ManageEventsUiState(
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val formError: String? = null
)

class ManageEventsViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageEventsUiState())
    val uiState: StateFlow<ManageEventsUiState> = _uiState.asStateFlow()

    private val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.events.isEmpty()) }
            val events = repository.getMyEvents()
            _uiState.update { it.copy(isLoading = false, events = events) }
        }
    }

    fun clearError() = _uiState.update { it.copy(formError = null) }

    /**
     * Simpan event baru atau perubahan event. Mengembalikan hasil lewat onSaved.
     * Validasi: tanggal tidak boleh lampau, venue wajib, dan tidak boleh ganda
     * (nama + venue + tanggal sama).
     */
    fun save(
        editing: Event?,
        name: String,
        category: EventCategory,
        venue: String,
        city: String,
        dateText: String,
        time: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val date = try {
                LocalDate.parse(dateText.trim(), dateFormat)
            } catch (e: Exception) {
                null
            }
            val all = repository.getEventSummaries().map { it.event }
            val duplicate = date != null && all.any {
                it.id != (editing?.id ?: -1) &&
                    it.name.equals(name.trim(), ignoreCase = true) &&
                    it.venue.equals(venue.trim(), ignoreCase = true) &&
                    it.date == date
            }
            val error = when {
                name.isBlank() -> "Nama event wajib diisi."
                venue.isBlank() -> "Venue wajib diisi."
                city.isBlank() -> "Kota wajib diisi."
                date == null -> "Tanggal harus berformat dd-MM-yyyy, misalnya 17-01-2027."
                date.isBefore(LocalDate.now()) -> "Tanggal event tidak boleh sudah lewat."
                duplicate -> "Event dengan nama, venue, dan tanggal yang sama sudah ada."
                else -> null
            }
            if (error != null || date == null) {
                _uiState.update { it.copy(formError = error) }
                return@launch
            }

            val event = (editing ?: Event(
                id = 0,
                name = "",
                category = category,
                venue = "",
                city = "",
                date = date,
                time = "",
                latitude = 0.0,
                longitude = 0.0,
                description = "Event buatan penjual.",
                posterStart = 0xFF2B45B4,
                posterEnd = 0xFFFF4FD8
            )).copy(
                name = name.trim(),
                category = category,
                venue = venue.trim(),
                city = city.trim(),
                date = date,
                time = time.trim().ifBlank { "19.00 WIB" }
            )
            repository.saveEvent(event)
            _uiState.update { it.copy(formError = null) }
            load()
            onSaved()
        }
    }

    fun delete(eventId: Int) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
            load()
        }
    }

    fun formatDate(date: LocalDate): String = date.format(dateFormat)
}

// ---------------------------------------------------------------------------
// Kelola Tiket (listing milik penjual)
// ---------------------------------------------------------------------------

data class ListingItem(
    val listing: TicketListing,
    val event: Event?,
    val markupPercent: Double,
    val isEventPast: Boolean
) {
    // Listing otomatis nonaktif bila stok habis atau event sudah lewat.
    val displayStatus: ListingStatus
        get() = when {
            isEventPast -> ListingStatus.NONAKTIF
            listing.quantity == 0 -> ListingStatus.TERJUAL
            else -> listing.status
        }
}

data class MyListingsUiState(
    val isLoading: Boolean = false,
    val items: List<ListingItem> = emptyList(),
    val priceError: String? = null
)

class MyListingsViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyListingsUiState())
    val uiState: StateFlow<MyListingsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.items.isEmpty()) }
            val today = LocalDate.now()
            val items = repository.getMyListings().map { listing ->
                val event = repository.getEventSummary(listing.eventId)?.event
                ListingItem(
                    listing = listing,
                    event = event,
                    markupPercent = if (listing.faceValue > 0) {
                        (listing.price - listing.faceValue) * 100.0 / listing.faceValue
                    } else {
                        0.0
                    },
                    isEventPast = event?.date?.isBefore(today) ?: true
                )
            }
            _uiState.update { it.copy(isLoading = false, items = items) }
        }
    }

    fun clearError() = _uiState.update { it.copy(priceError = null) }

    // Ubah harga: markup di atas 50% ditolak (aturan yang sama dengan Fungsi 1).
    fun updatePrice(listing: TicketListing, priceText: String, onSaved: () -> Unit) {
        val price = priceText.toLongOrNull() ?: 0L
        viewModelScope.launch {
            val result = FairPriceCalculator.check(
                listing.faceValue,
                price,
                repository.getPriceHistory(listing.eventId, listing.seatCategory)
            )
            val error = when {
                price < 10_000 -> "Harga belum benar."
                result.status == PriceStatus.TERLALU_TINGGI ->
                    "Markup %.0f%% melewati batas 50%%. Harga ditolak.".format(result.markupPercent)
                else -> null
            }
            if (error != null) {
                _uiState.update { it.copy(priceError = error) }
                return@launch
            }
            try {
                repository.updateListingPrice(listing.id, price)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(priceError = "Gagal menyimpan harga. Periksa koneksi atau login, lalu coba lagi.")
                }
                return@launch
            }
            _uiState.update { it.copy(priceError = null) }
            load()
            onSaved()
        }
    }

    fun delete(listingId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteListing(listingId)
                _uiState.update { it.copy(priceError = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(priceError = "Gagal menonaktifkan listing. Periksa koneksi atau login, lalu coba lagi.")
                }
            }
            load()
        }
    }
}

// ---------------------------------------------------------------------------
// Data Penjualan
// ---------------------------------------------------------------------------

data class SalesBar(val label: String, val tickets: Int, val revenue: Long)

data class SalesUiState(
    val isLoading: Boolean = true,
    val totalRevenue: Long = 0L,
    val ticketsSold: Int = 0,
    val averagePrice: Long = 0L,
    val bestSeller: String = "-",
    val bars: List<SalesBar> = emptyList()
) {
    val maxTickets: Int get() = bars.maxOfOrNull { it.tickets } ?: 0
}

class SalesViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    // Mengagregasi pesanan toko yang sudah dibayar menjadi ringkasan penjualan.
    fun load() {
        viewModelScope.launch {
            val paid = repository.getStoreOrders().filter { it.status != OrderStatus.BELUM_BAYAR }
            val revenue = paid.sumOf { it.ticketPrice }
            val tickets = paid.sumOf { it.quantity }

            val bars = paid.groupBy { it.eventId }.map { (eventId, orders) ->
                val name = repository.getEventSummary(eventId)?.event?.name ?: "Event #$eventId"
                SalesBar(name, orders.sumOf { it.quantity }, orders.sumOf { it.ticketPrice })
            }.sortedByDescending { it.tickets }

            _uiState.value = SalesUiState(
                isLoading = false,
                totalRevenue = revenue,
                ticketsSold = tickets,
                averagePrice = if (tickets > 0) revenue / tickets else 0L,
                bestSeller = bars.firstOrNull()?.label ?: "-",
                bars = bars
            )
        }
    }
}
