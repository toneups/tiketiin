package com.example.tiketiin.ui.sell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.TicketListing
import com.example.tiketiin.data.repository.TiketiinRepository
import com.example.tiketiin.domain.FairPriceCalculator
import com.example.tiketiin.domain.FairPriceResult
import com.example.tiketiin.domain.PriceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import com.example.tiketiin.data.repository.ProfileRepository

data class SellUiState(
    // e-KYC
    val isKycVerified: Boolean = false,
    val ktpUploaded: Boolean = false,
    val selfieTaken: Boolean = false,
    val kycName: String = "",
    val kycNik: String = "",
    val kycError: String? = null,
    val isVerifying: Boolean = false,

    // Form jual tiket
    val events: List<Event> = emptyList(),
    val selectedEvent: Event? = null,
    val seatCategory: String = "",
    val row: String = "",
    val quantityText: String = "1",
    val faceValueText: String = "",
    val priceText: String = "",
    val proofAttached: Boolean = false,
    val proofPath: String? = null,
    val isUploadingProof: Boolean = false,
    val formError: String? = null,

    // Cek harga wajar
    val priceResult: FairPriceResult? = null,
    val isPublishing: Boolean = false,

    // Listing yang baru tayang
    val publishedListing: TicketListing? = null,
    val publishedEvent: Event? = null
)

/**
 * Dipakai bersama oleh layar e-KYC, Form Jual Tiket, Cek Harga Wajar, dan Listing Tayang
 * (dibuat di TiketiinApp supaya isian tidak hilang saat pindah langkah).
 */
class SellViewModel(
    private val repository: TiketiinRepository,
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SellUiState())
    val uiState: StateFlow<SellUiState> = _uiState.asStateFlow()

    // Dipanggil saat mulai menjual. eventId = -1 berarti event dipilih sendiri di form.
    // onReady memberi tahu apakah akun sudah lolos e-KYC (menentukan layar berikutnya).
    fun startSelling(eventId: Int, onReady: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUser()
            val profile = try {
                profileRepository.getMyProfile()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            val today = LocalDate.now()
            val events = repository.getEventSummaries()
                .map { it.event }
                .filter { !it.date.isBefore(today) }
            val verified = (profile?.isKycVerified == true) || user.isKycVerified
            _uiState.value = SellUiState(
                isKycVerified = verified,
                kycName = profile?.name.orEmpty(),
                events = events,
                selectedEvent = events.find { it.id == eventId }
            )
            onReady(verified)
        }
    }
    // ----- e-KYC -----
    fun toggleKtp() = _uiState.update { it.copy(ktpUploaded = !it.ktpUploaded) }
    fun toggleSelfie() = _uiState.update { it.copy(selfieTaken = !it.selfieTaken) }
    fun updateKycName(value: String) = _uiState.update { it.copy(kycName = value) }
    fun updateKycNik(value: String) = _uiState.update { it.copy(kycNik = value.filter { ch -> ch.isDigit() }.take(16)) }

    fun submitKyc(onVerified: () -> Unit) {
        val state = _uiState.value
        val error = when {
            !state.ktpUploaded -> "Unggah foto KTP terlebih dahulu."
            !state.selfieTaken -> "Ambil foto selfie dengan KTP terlebih dahulu."
            state.kycName.isBlank() -> "Nama wajib diisi."
            state.kycNik.length != 16 -> "NIK harus 16 digit."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(kycError = error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, kycError = null) }
            repository.verifyKyc()
            _uiState.update { it.copy(isVerifying = false, isKycVerified = true) }
            onVerified()
        }
    }

    // ----- Form jual tiket -----
    fun selectEvent(event: Event) = _uiState.update { it.copy(selectedEvent = event) }
    fun updateSeatCategory(value: String) = _uiState.update { it.copy(seatCategory = value) }
    fun updateRow(value: String) = _uiState.update { it.copy(row = value) }
    fun updateQuantity(value: String) = _uiState.update { it.copy(quantityText = value.filter { ch -> ch.isDigit() }.take(2)) }
    fun updateFaceValue(value: String) = _uiState.update { it.copy(faceValueText = value.filter { ch -> ch.isDigit() }.take(9)) }
    fun updatePrice(value: String) = _uiState.update { it.copy(priceText = value.filter { ch -> ch.isDigit() }.take(9)) }
    fun toggleProof() = _uiState.update { it.copy(proofAttached = !it.proofAttached) }
    // Unggah bukti tiket ke Supabase Storage (bucket ticket-proofs)
    fun uploadProof(bytes: ByteArray, mimeType: String, fileName: String) {
        if (_uiState.value.isUploadingProof) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingProof = true, formError = null) }
            try {
                val path = repository.uploadTicketProof(bytes, mimeType, fileName)
                _uiState.update {
                    it.copy(isUploadingProof = false, proofAttached = true, proofPath = path)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploadingProof = false,
                        proofAttached = false,
                        proofPath = null,
                        formError = "Gagal mengunggah bukti: ${e.message ?: "coba lagi"}"
                    )
                }
            }
        }
    }

    // Validasi form, lalu jalankan Fungsi 1 (cek harga wajar).
    fun submitForm(onValid: () -> Unit) {
        val state = _uiState.value
        val event = state.selectedEvent
        val faceValue = state.faceValueText.toLongOrNull() ?: 0L
        val price = state.priceText.toLongOrNull() ?: 0L
        val quantity = state.quantityText.toIntOrNull() ?: 0

        val error = when {
            event == null -> "Pilih event terlebih dahulu."
            state.seatCategory.isBlank() -> "Tipe tiket wajib diisi."
            quantity < 1 -> "Jumlah tiket minimal 1."
            faceValue < 10_000 -> "Harga asli (face value) belum benar."
            price < 10_000 -> "Harga jual belum benar."
            !state.proofAttached -> "Unggah bukti tiket terlebih dahulu."
            else -> null
        }
        if (error != null || event == null) {
            _uiState.update { it.copy(formError = error) }
            return
        }
        viewModelScope.launch {
            val history = repository.getPriceHistory(event.id, state.seatCategory)
            val result = FairPriceCalculator.check(faceValue, price, history)
            _uiState.update { it.copy(formError = null, priceResult = result) }
            onValid()
        }
    }

    // Terbitkan listing dengan harga tertentu (harga awal atau harga rekomendasi).
    fun publish(price: Long, onPublished: (Int) -> Unit) {
        val state = _uiState.value
        val event = state.selectedEvent ?: return
        val result = state.priceResult ?: return
        if (state.isPublishing) return

        // Harga "Terlalu tinggi" yang tidak disesuaikan ditolak.
        val check = FairPriceCalculator.check(result.faceValue, price, emptyList())
        if (check.status == PriceStatus.TERLALU_TINGGI) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true) }
            val listing = TicketListing(
                id = 0,
                eventId = event.id,
                sellerId = "",
                seatCategory = state.seatCategory,
                row = state.row.ifBlank { "-" },
                faceValue = result.faceValue,
                price = price,
                quantity = state.quantityText.toIntOrNull() ?: 1
            )
            val id = repository.addListing(listing)
            val saved = repository.getListing(id)
            _uiState.update {
                it.copy(
                    isPublishing = false,
                    publishedListing = saved,
                    publishedEvent = event
                )
            }
            onPublished(id)
        }
    }
}
