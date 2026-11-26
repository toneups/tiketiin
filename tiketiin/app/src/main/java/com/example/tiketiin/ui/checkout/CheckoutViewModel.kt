package com.example.tiketiin.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.Order
import com.example.tiketiin.data.model.TicketListing
import com.example.tiketiin.data.repository.EventCatalogRepository
import com.example.tiketiin.data.repository.TiketiinRepository
import com.example.tiketiin.domain.HotelFinder
import com.example.tiketiin.domain.NearbyHotel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import android.util.Log
import com.example.tiketiin.data.model.OrderStatus
import retrofit2.HttpException
val PAYMENT_CATEGORIES = listOf(
    "Transfer Bank",
    "Kartu Kredit / Debit",
    "E-Wallet / QRIS",
)

val PAYMENT_OPTIONS = mapOf(
    "Transfer Bank" to listOf(
        "SeaBank",
        "Bank BCA",
        "Bank Mandiri",
        "Bank BNI",
        "Bank BRI",
        "BSI",
        "Bank Permata",
        "CIMB Niaga",
        "Bank lainnya"
    ),
    "Kartu Kredit / Debit" to listOf(
        "Visa",
        "Mastercard"
    ),
    "E-Wallet / QRIS" to listOf(
        "DANA",
        "ShopeePay",
        "GoPay",
        "OVO",
        "QRIS"
    )
)

data class CheckoutUiState(
    val event: Event? = null,
    val listing: TicketListing? = null,
    val quantity: Int = 1,
    // Isi formulir pesanan
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val idNumber: String = "",
    val address: String = "",
    val powerOfAttorney: Boolean = false,
    val formError: String? = null,
    // Pembayaran
    val paymentCategory: String = PAYMENT_CATEGORIES.first(),
    val paymentMethod: String = "SeaBank",
    val secondsLeft: Int = LOCK_SECONDS,
    val isPaying: Boolean = false,
    val payError: String? = null,
    val pendingOrderId: Int? = null,
) {
    val ticketPrice: Long get() = (listing?.price ?: 0L) * quantity
    val serviceFee: Long get() = ticketPrice * 5 / 100
    val total: Long get() = ticketPrice + serviceFee
    val isExpired: Boolean get() = secondsLeft <= 0
    val seatLabel: String get() = listing?.let { "${it.seatCategory}, ${it.row}" } ?: "-"

    companion object {
        // Tiket dikunci sementara 10 menit supaya tidak dibeli dua orang sekaligus.
        const val LOCK_SECONDS = 600
    }
}

/**
 * Dipakai bersama oleh Form Pesanan dan Pembayaran (dibuat di TiketiinApp,
 * jadi datanya tetap ada saat pindah di antara dua layar itu).
 */
class CheckoutViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    // Dipanggil saat tombol "Beli Tiket" ditekan.
    fun start(eventId: Int, listingId: Int, quantity: Int) {
        viewModelScope.launch {
            val user = repository.getUser()
            val summary = repository.getEventSummary(eventId)

            // Ambil listing berdasarkan ID tiket yang dipilih pengguna.
            val listing = repository.getListings(eventId)
                .firstOrNull { it.id == listingId }

            if (listing == null || listing.quantity <= 0) {
                _uiState.value = CheckoutUiState(
                    event = summary?.event,
                    payError = "Tiket tidak tersedia atau stok sudah habis."
                )
                return@launch
            }

            _uiState.value = CheckoutUiState(
                event = summary?.event,
                listing = listing,
                quantity = quantity.coerceIn(
                    1,
                    listing.quantity
                ),
                name = user.name,
                email = user.email,
                phone = user.phone
            )

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.secondsLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(secondsLeft = it.secondsLeft - 1) }
            }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value) }
    fun updatePhone(value: String) = _uiState.update { it.copy(phone = value) }
    fun updateIdNumber(value: String) = _uiState.update { it.copy(idNumber = value) }
    fun updateAddress(value: String) = _uiState.update { it.copy(address = value) }
    fun togglePowerOfAttorney() = _uiState.update { it.copy(powerOfAttorney = !it.powerOfAttorney) }

    // true kalau formulir lengkap.
    fun validateForm(): Boolean {
        val state = _uiState.value
        val error = when {
            state.name.isBlank() -> "Nama wajib diisi."
            !state.email.contains("@") -> "Format email belum benar."
            state.phone.length < 9 -> "Nomor HP belum benar."
            state.idNumber.length < 8 -> "Nomor kartu identitas belum benar."
            state.address.isBlank() -> "Alamat wajib diisi."
            else -> null
        }
        _uiState.update { it.copy(formError = error) }
        return error == null
    }




    fun preparePayment(onReady: (Int) -> Unit) {
        val state = _uiState.value
        val listing = state.listing ?: return

        if (state.isExpired || state.isPaying) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPaying = true,
                    payError = null
                )
            }

            try {
                Log.d("CheckoutPayment", "Mulai createOrder")

                val order = repository.createOrder(
                    listingId = listing.id,
                    quantity = state.quantity,
                    buyerName = state.name,
                    buyerEmail = state.email,
                    buyerPhone = state.phone,
                    paymentMethod = state.paymentMethod
                )

                Log.d(
                    "CheckoutPayment",
                    "Pesanan dibuat. ID=${order.id}"
                )

                _uiState.update {
                    it.copy(
                        isPaying = false,
                        pendingOrderId = order.id
                    )
                }

                onReady(order.id)

            } catch (e: Exception) {
                Log.e(
                    "CheckoutPayment",
                    "Gagal membuat pesanan: ${e.message}",
                    e
                )

                _uiState.update {
                    it.copy(
                        isPaying = false,
                        payError = e.message ?: "Gagal membuat pesanan."
                    )
                }
            }
        }
    }



    fun confirmPayment(onSuccess: (Int) -> Unit) {
        val state = _uiState.value
        val orderId = state.pendingOrderId ?: return

        if (state.isPaying) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPaying = true,
                    payError = null
                )
            }

            try {
                Log.d(
                    "CheckoutPayment",
                    "Memproses pembayaran. ID=$orderId"
                )

                val paidOrder = repository.payOrder(
                    orderId,
                    state.paymentMethod
                )

                timerJob?.cancel()

                _uiState.update {
                    it.copy(isPaying = false)
                }

                onSuccess(paidOrder.id)

            } catch (e: Exception) {
                Log.e(
                    "CheckoutPayment",
                    "Pembayaran gagal: ${e.message}",
                    e
                )

                _uiState.update {
                    it.copy(
                        isPaying = false,
                        payError = e.message ?: "Pembayaran gagal."
                    )
                }
            }
        }
    }


    fun selectPaymentCategory(category: String) {
        val defaultMethod = PAYMENT_OPTIONS[category]?.firstOrNull()
            ?: category

        _uiState.update {
            it.copy(
                paymentCategory = category,
                paymentMethod = defaultMethod,
                payError = null
            )
        }
    }

    fun selectPaymentMethod(method: String) {
        _uiState.update {
            it.copy(
                paymentMethod = method,
                payError = null
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Layar Pembayaran Berhasil + Rekomendasi Hotel
// ---------------------------------------------------------------------------


data class PaymentSuccessUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val event: Event? = null,
    val hotels: List<NearbyHotel> = emptyList(),
    val hotelError: String? = null,
    val error: String? = null
)

class PaymentSuccessViewModel(
    private val repository: TiketiinRepository,
    private val eventCatalogRepository: EventCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentSuccessUiState())
    val uiState: StateFlow<PaymentSuccessUiState> =
        _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load(orderId: Int) {
        if (orderId <= 0) {
            _uiState.value = PaymentSuccessUiState(
                isLoading = false,
                error = "ID pesanan tidak valid."
            )
            return
        }

        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _uiState.value = PaymentSuccessUiState(isLoading = true)

            try {
                val order = repository.getOrder(orderId)

                if (order == null) {
                    _uiState.value = PaymentSuccessUiState(
                        isLoading = false,
                        error = "Pesanan tidak ditemukan."
                    )
                    return@launch
                }

                val event = eventCatalogRepository
                    .getEventSummary(order.eventId)?.event

                if (event == null) {
                    _uiState.value = PaymentSuccessUiState(
                        isLoading = false,
                        order = order,
                        hotelError = "Informasi lokasi event tidak tersedia."
                    )
                    return@launch
                }

                var hotelError: String? = null

                val hotels = try {
                    HotelFinder.findNearby(
                        event.latitude,
                        event.longitude,
                        repository.getHotels(order.eventId)
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    hotelError = "Gagal memuat rekomendasi hotel."
                    emptyList()
                }

                _uiState.value = PaymentSuccessUiState(
                    isLoading = false,
                    order = order,
                    event = event,
                    hotels = hotels,
                    hotelError = hotelError
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = PaymentSuccessUiState(
                    isLoading = false,
                    error = e.message ?: "Gagal memuat informasi pesanan."
                )
            }
        }
    }
}
