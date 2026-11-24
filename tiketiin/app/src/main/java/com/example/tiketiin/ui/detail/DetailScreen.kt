package com.example.tiketiin.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiketiin.data.model.*
import com.example.tiketiin.domain.*
import com.example.tiketiin.ui.components.*
import com.example.tiketiin.ui.theme.*

@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onBack: () -> Unit,
    onSelectTab: (Int) -> Unit,
    onChangeQuantity: (Int) -> Unit,
    onToggleSaved: () -> Unit,
    onSelectListing: (Int) -> Unit,onBuy: (eventId: Int, listingId: Int, quantity: Int) -> Unit
) {
    val summary = uiState.summary
    val listing = uiState.listing
    val listings = uiState.listings

    if (uiState.isLoading || summary == null) {
        Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
            ScreenTopBar(title = "Detail Event", onBack = onBack)
            if (uiState.isLoading) {
                LoadingBox()
            } else {
                EmptyBox(text = uiState.errorMessage ?: "Event tidak ditemukan")
            }
        }
        return
    }

    val event = summary.event
    var showOfferInfo by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(270.dp)) {
                PosterBox(event = event, titleSize = 26.sp, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 24.dp, top = 8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = AppIcons.Back, contentDescription = "Kembali")
                }
            }

            Column(
                modifier = Modifier
                    .offset(y = (-28).dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                Text(text = event.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (listing != null) formatRupiah(listing.price) else "Tiket habis",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        IconText(
                            icon = AppIcons.Ticket,
                            text = "${summary.ticketCount} Ticket",
                            textColor = BluePrimary
                        )
                    }
                    Button(
                        onClick = { showOfferInfo = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text(text = "Ajukan Penawaran", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Tentang Event", "Term & Condition", "Ulasan").forEachIndexed { index, label ->
                        val selected = uiState.selectedTab == index
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectTab(index) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = if (selected) BluePrimary else TextGray,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(if (selected) BluePrimary else TextGray)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                when (uiState.selectedTab) {
                    0 -> {
                        IconText(icon = AppIcons.Store, text = event.venue, fontSize = 11.sp, iconSize = 16.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        IconText(icon = AppIcons.Location, text = event.city, fontSize = 11.sp, iconSize = 16.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoField(label = "Tanggal", value = formatDateLong(event.date), modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoField(label = "Waktu", value = event.time, modifier = Modifier.weight(1f))
                            InfoField(
                                label = "Seat",
                                value = listing?.seatCategory ?: "-",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = event.description, fontSize = 12.sp, color = TextGray)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "Seating Plan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        SeatingPlan(highlight = listing?.seatCategory ?: "")
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Pilihan Tiket Tersedia",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (listings.isEmpty()) {
                            Text(
                                text = "Belum ada tiket yang tersedia.",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        } else {
                            listings.forEach { ticket ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clickable {
                                            onSelectListing(ticket.id)
                                        }
                                        .border(
                                            width = if (uiState.selectedListingId == ticket.id) 2.dp else 0.dp,
                                            color = if (uiState.selectedListingId == ticket.id)
                                                BluePrimary else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = FieldGray
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = ticket.seatCategory,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )

                                            Text(
                                                text = formatRupiah(ticket.price),
                                                color = BluePrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(5.dp))

                                        Text(
                                            text = "Baris: ${ticket.row.ifBlank { "-" }}",
                                            fontSize = 11.sp,
                                            color = TextGray
                                        )

                                        Text(
                                            text = "Stok tersedia: ${ticket.quantity}",
                                            fontSize = 11.sp,
                                            color = TextGray
                                        )

                                        Text(
                                            text = "Harga asli: ${formatRupiah(ticket.faceValue)}",
                                            fontSize = 11.sp,
                                            color = TextGray
                                        )
                                    }
                                }
                            }
                        }

                    }
                    1 -> {
                        val terms = listOf(
                            "Tiket yang dijual sudah diverifikasi dan dananya ditahan (escrow) sampai tiket terbukti valid.",
                            "Nama pada tiket mengikuti data pemesan. Gunakan surat pindah kuasa bila diminta penyelenggara.",
                            "Pembayaran harus selesai dalam 10 menit setelah tiket dikunci.",
                            "Uang kembali 100% bila tiket terbukti tidak valid."
                        )
                        terms.forEachIndexed { index, term ->
                            Text(
                                text = "${index + 1}. $term",
                                fontSize = 12.sp,
                                color = TextGray,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                    else -> {
                        ReviewItem(name = "Dimas", stars = 5, text = "Tiket asli, proses cepat. Penjual responsif.")
                        ReviewItem(name = "Sinta", stars = 4, text = "Aman karena dananya ditahan dulu. Recommended.")
                    }
                }
            }
        }

        // Bilah bawah: harga, jumlah, simpan, beli
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp)
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Harga Tiket", fontSize = 9.sp, color = TextDark)
                    Text(text = formatRupiah(uiState.totalPrice), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                QuantityButton(label = "−", onClick = { onChangeQuantity(-1) })
                Text(
                    text = uiState.quantity.toString(),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                QuantityButton(label = "+", onClick = { onChangeQuantity(1) })
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ScreenGray)
                        .clickable(onClick = onToggleSaved),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (summary.isSaved) AppIcons.Bookmark else AppIcons.BookmarkBorder,
                        contentDescription = "Simpan event",
                        tint = BluePrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        listing?.let {
                            onBuy(event.id, it.id, uiState.quantity)
                        }
                    },
                    enabled = listing != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text(text = "Beli Tiket", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showOfferInfo) {
        AlertDialog(
            onDismissRequest = { showOfferInfo = false },
            confirmButton = {
                TextButton(onClick = { showOfferInfo = false }) { Text(text = "Mengerti") }
            },
            title = { Text(text = "Ajukan Penawaran") },
            text = { Text(text = "Fitur nego harga dengan penjual akan tersedia di versi berikutnya.") }
        )
    }
}

@Composable
private fun InfoField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FieldGray)
            .padding(horizontal = 24.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 9.sp, color = TextGray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4A4A4A))
    }
}

@Composable
private fun QuantityButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(1.dp, BluePrimary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = BluePrimary, fontSize = 13.sp, lineHeight = 13.sp)
    }
}

@Composable
private fun ReviewItem(name: String, stars: Int, text: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(8.dp))
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= stars) AppIcons.Star else AppIcons.StarBorder,
                    contentDescription = null,
                    tint = YellowMain,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Text(text = text, fontSize = 12.sp, color = TextGray)
    }
}

// Denah kursi sederhana; kategori tiket yang dijual diberi garis tepi kuning.
@Composable
private fun SeatingPlan(highlight: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SeatBlock(label = "STAGE", color = Color(0xFF9E9E9E), highlight = false, modifier = Modifier.size(width = 110.dp, height = 44.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            SeatBlock("CAT 4", Color(0xFF7D7A2B), highlight.contains("CAT 4"), Modifier.weight(0.8f).fillMaxHeight())
            SeatBlock("CAT 2", Color(0xFF1E3A8A), highlight.contains("CAT 2"), Modifier.weight(1f).fillMaxHeight())
            SeatBlock("VIP", Color(0xFF8B1E1E), highlight.contains("VIP"), Modifier.weight(2.2f).fillMaxHeight())
            SeatBlock("CAT 1", Color(0xFFB08D57), highlight.contains("CAT 1"), Modifier.weight(1f).fillMaxHeight())
            SeatBlock("CAT 3", Color(0xFF1E3A8A), highlight.contains("CAT 3"), Modifier.weight(1f).fillMaxHeight())
        }
        Spacer(modifier = Modifier.height(8.dp))
        SeatBlock(label = "FOH", color = Color(0xFF9E9E9E), highlight = false, modifier = Modifier.size(width = 80.dp, height = 30.dp))
    }
}

@Composable
private fun SeatBlock(label: String, color: Color, highlight: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .border(if (highlight) 3.dp else 0.dp, if (highlight) YellowMain else Color.Transparent, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
