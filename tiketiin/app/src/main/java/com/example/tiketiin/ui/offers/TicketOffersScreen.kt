package com.example.tiketiin.ui.offers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiketiin.data.remote.dto.TicketOfferDto
import com.example.tiketiin.data.repository.MyOffersRepository
import com.example.tiketiin.domain.formatRupiah
import com.example.tiketiin.ui.components.ScreenTopBar
import com.example.tiketiin.ui.simpleFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class OffersUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val offers: List<TicketOfferDto> = emptyList()
)

class TicketOffersViewModel(
    private val repository: MyOffersRepository
) : ViewModel() {

    private val state = MutableStateFlow(OffersUiState())
    val uiState = state.asStateFlow()

    private var loadJob: Job? = null

    fun load() {
        if (loadJob?.isActive == true) return

        state.update {
            it.copy(isLoading = true, error = null)
        }

        loadJob = viewModelScope.launch {
            try {
                state.value = OffersUiState(
                    isLoading = false,
                    offers = repository.load()
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.update {
                    it.copy(
                        isLoading = false,
                        error = "Gagal memuat penawaran. Coba lagi."
                    )
                }
            }
        }
    }
}

@Composable
fun TicketOffersScreen(
onBack: () -> Unit,
embedded: Boolean = false
) {
    val viewModel: TicketOffersViewModel = viewModel(
        factory = simpleFactory {
            TicketOffersViewModel(MyOffersRepository())
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.load()
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = if (embedded) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            }
        ) {
            if (!embedded) {
                ScreenTopBar(
                    title = "Penawaran Saya",
                    onBack = onBack
                )
            }

            TextButton(
                onClick = viewModel::load,
                enabled = !uiState.isLoading
            ) {
                Text("Perbarui status")
            }

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(24.dp)
                    )
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(uiState.error.orEmpty())

                        TextButton(onClick = viewModel::load) {
                            Text("Coba lagi")
                        }
                    }
                }

                uiState.offers.isEmpty() -> {
                    Text(
                        text = "Belum ada penawaran. " +
                                "Buka detail event untuk mengajukan harga.",
                        modifier = Modifier.padding(24.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.offers,
                            key = { it.id }
                        ) { offer ->
                            OfferCard(offer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferCard(offer: TicketOfferDto) {
    val blue = Color(0xFF3448C5)
    val muted = Color(0xFF727789)

    val statusLabel = when (offer.status) {
        "PENDING" -> "Menunggu penjual"
        "ACCEPTED" -> "Diterima"
        "REJECTED" -> "Ditolak"
        "CANCELLED" -> "Dibatalkan"
        "USED" -> "Sudah digunakan"
        "EXPIRED" -> "Kedaluwarsa"
        else -> offer.status
    }

    val statusColor = when (offer.status) {
        "PENDING" -> Color(0xFF946200)
        "ACCEPTED", "USED" -> Color(0xFF178354)
        "REJECTED" -> Color(0xFFC13D4B)
        else -> muted
    }

    val statusBackground = when (offer.status) {
        "PENDING" -> Color(0xFFFFF4D8)
        "ACCEPTED", "USED" -> Color(0xFFE7F6ED)
        "REJECTED" -> Color(0xFFFFEBEE)
        else -> Color(0xFFF0F1F5)
    }

    val message = when (offer.status) {
        "PENDING" ->
            "Penawaranmu sudah terkirim. Tunggu jawaban penjual, " +
                    "lalu perbarui status di halaman ini."

        "ACCEPTED" ->
            "Penjual menerima penawaranmu. Pembayaran dengan " +
                    "harga penawaran belum tersedia di versi ini."

        "REJECTED" ->
            "Penawaran ditolak. Kamu bisa kembali ke " +
                    "event untuk memilih tiket lainnya."

        "CANCELLED" -> "Penawaran ini telah dibatalkan."
        "USED" -> "Penawaran ini sudah digunakan."
        "EXPIRED" -> "Masa berlaku penawaran ini sudah berakhir."
        else -> "Perbarui halaman untuk melihat status terbaru."
    }

    val total = if (
        offer.quantity > 0 &&
        offer.pricePerTicket > 0 &&
        offer.pricePerTicket <= Long.MAX_VALUE / offer.quantity
    ) {
        formatRupiah(offer.pricePerTicket * offer.quantity)
    } else {
        "Tidak valid"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFE6E9F2)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PENAWARAN #${offer.id}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = muted
                )

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = statusBackground
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = offer.listing?.event?.name
                        ?: "Listing #${offer.listingId}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF202438)
                )

                Text(
                    text = "${offer.listing?.seatCategory ?: "Tiket"}" +
                            "  •  ${offer.quantity} tiket",
                    fontSize = 13.sp,
                    color = muted
                )
            }

            HorizontalDivider(
                color = Color(0xFFEDF0F6)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "Harga penawaran / tiket",
                        fontSize = 11.sp,
                        color = muted
                    )

                    Text(
                        text = formatRupiah(offer.pricePerTicket),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF30364B)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "Total penawaran",
                        fontSize = 11.sp,
                        color = muted
                    )

                    Text(
                        text = total,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = blue
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF4F6FF)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = when (offer.status) {
                            "PENDING" -> "Apa langkah selanjutnya?"
                            "ACCEPTED" -> "Penawaran disetujui"
                            else -> "Informasi penawaran"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = blue
                    )

                    Text(
                        text = message,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF60677C)
                    )

                    if (offer.status == "PENDING") {
                        Text(
                            text = "Belum perlu membayar • Stok belum dipesan",
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = muted
                        )
                    }
                }
            }
        }
    }
}