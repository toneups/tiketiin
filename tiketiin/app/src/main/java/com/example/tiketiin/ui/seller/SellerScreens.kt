package com.example.tiketiin.ui.seller

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
import androidx.compose.ui.window.Dialog

// Layar-layar ini belum ada di Figma. Dibuat dari daftar fungsi di laporan Bab II
// (Kelola Event, Kelola Tiket, Lihat Data Penjualan) dengan gaya yang sama.

// ---------------------------------------------------------------------------
// Kelola Event
// ---------------------------------------------------------------------------

@Composable
fun ManageEventsScreen(
    uiState: ManageEventsUiState,
    onBack: () -> Unit,
    formatDate: (java.time.LocalDate) -> String,
    onSave: (Event?, String, EventCategory, String, String, String, String, () -> Unit) -> Unit,
    onDelete: (Int) -> Unit,
    onClearError: () -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Event?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.shadow(4.dp)) {
            ScreenTopBar(title = "Kelola Event", onBack = onBack)
        }
        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> LoadingBox()
                uiState.events.isEmpty() -> EmptyBox(text = "Kamu belum membuat event.\nKetuk tombol di bawah untuk menambah.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.events) { event ->
                        val isPast = event.date.isBefore(java.time.LocalDate.now())
                        WhiteCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = event.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    CategoryTag(category = event.category)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    IconText(icon = AppIcons.Calendar, text = "${formatDateShort(event.date)} · ${event.time}")
                                    IconText(icon = AppIcons.Location, text = "${event.venue}, ${event.city}")
                                    // Status berubah otomatis menjadi Selesai saat tanggal lewat
                                    Text(
                                        text = if (isPast) "Selesai" else "Aktif",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isPast) TextGray else GreenMain
                                    )
                                }
                                IconButton(onClick = {
                                    editing = event
                                    onClearError()
                                    showForm = true
                                }) {
                                    Icon(imageVector = AppIcons.Edit, contentDescription = "Ubah", tint = BluePrimary)
                                }
                                IconButton(onClick = { onDelete(event.id) }) {
                                    Icon(imageVector = AppIcons.Delete, contentDescription = "Hapus", tint = RedDark)
                                }
                            }
                        }
                    }
                }
            }
        }
        PrimaryButton(
            text = "Tambah Event",
            onClick = {
                editing = null
                onClearError()
                showForm = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 14.dp)
        )
    }

    if (showForm) {
        EventFormDialog(
            editing = editing,
            initialDate = editing?.let { formatDate(it.date) } ?: "",
            error = uiState.formError,
            onDismiss = { showForm = false },
            onSubmit = { name, category, venue, city, date, time ->
                onSave(editing, name, category, venue, city, date, time) { showForm = false }
            }
        )
    }
}

@Composable
private fun EventFormDialog(
    editing: Event?,
    initialDate: String,
    error: String?,
    onDismiss: () -> Unit,
    onSubmit: (String, EventCategory, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(editing?.name ?: "") }
    var category by remember { mutableStateOf(editing?.category ?: EventCategory.MUSIK) }
    var venue by remember { mutableStateOf(editing?.venue ?: "") }
    var city by remember { mutableStateOf(editing?.city ?: "") }
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf(editing?.time ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (editing == null) "Tambah Event" else "Ubah Event",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            FormField(label = "Nama Event") {
                AppTextField(value = name, onValueChange = { name = it }, placeholder = "Masukkan nama event")
            }
            Text(text = "Kategori", fontSize = 13.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(EventCategory.entries.toList()) { option ->
                    PillChip(label = option.label, selected = category == option, onClick = { category = option })
                }
            }
            FormField(label = "Venue") {
                AppTextField(value = venue, onValueChange = { venue = it }, placeholder = "Masukkan lokasi event")
            }
            FormField(label = "Kota") {
                AppTextField(value = city, onValueChange = { city = it }, placeholder = "Masukkan kota")
            }
            FormField(label = "Tanggal", hint = "Format dd-MM-yyyy") {
                AppTextField(value = date, onValueChange = { date = it }, placeholder = "17-01-2027")
            }
            FormField(label = "Waktu") {
                AppTextField(value = time, onValueChange = { time = it }, placeholder = "14.30 - 23.00")
            }
            if (error != null) {
                Text(text = error, color = RedDark, fontSize = 12.sp)
            }
            PrimaryButton(
                text = "Simpan",
                onClick = { onSubmit(name, category, venue, city, date, time) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlineButton(text = "Batal", onClick = onDismiss, color = TextDark, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ---------------------------------------------------------------------------
// Kelola Tiket
// ---------------------------------------------------------------------------

@Composable
fun MyListingsScreen(
    uiState: MyListingsUiState,
    onBack: () -> Unit,
    onUpdatePrice: (TicketListing, String, () -> Unit) -> Unit,
    onDelete: (Int) -> Unit,
    onClearError: () -> Unit,
    onAddListing: () -> Unit
) {
    var editing by remember { mutableStateOf<TicketListing?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Pesan error untuk aksi di luar dialog (misalnya tombol Hapus gagal)
        if (editing == null) {
            val screenError = uiState.priceError
            if (screenError != null) {
                Text(
                    text = screenError,
                    color = RedDark,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
        Box(modifier = Modifier.shadow(4.dp)) {
            ScreenTopBar(title = "Kelola Tiket", onBack = onBack)
        }
        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> LoadingBox()
                uiState.items.isEmpty() -> EmptyBox(text = "Kamu belum punya listing tiket.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.items) { item ->
                        val listing = item.listing
                        val status = item.displayStatus
                        WhiteCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.event?.name ?: "Event dihapus",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (status == ListingStatus.AKTIF) GreenSoft else FieldGray)
                                        .padding(horizontal = 12.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = status.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (status == ListingStatus.AKTIF) GreenMain else TextGray
                                    )
                                }
                            }
                            IconText(icon = AppIcons.Seat, text = "${listing.seatCategory}, ${listing.row}")
                            IconText(icon = AppIcons.Ticket, text = "Sisa ${listing.quantity} tiket")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = LineGray)
                            DetailRow(label = "Harga asli (face value)", value = formatRupiah(listing.faceValue))
                            DetailRow(label = "Harga jual", value = formatRupiah(listing.price), bold = true)
                            DetailRow(label = "Markup", value = "%.1f%%".format(item.markupPercent))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlineButton(
                                    text = "Ubah harga",
                                    onClick = {
                                        onClearError()
                                        editing = listing
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlineButton(
                                    text = "Hapus",
                                    onClick = { onDelete(listing.id) },
                                    color = RedDark,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
        PrimaryButton(
            text = "Tambah Listing",
            onClick = onAddListing,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 14.dp)
        )
    }

    val target = editing
    if (target != null) {
        var priceText by remember(target.id) { mutableStateOf(target.price.toString()) }
        Dialog(onDismissRequest = { editing = null }) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Ubah Harga Jual", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Harga asli ${formatRupiah(target.faceValue)}. Markup di atas 50% akan ditolak.",
                    fontSize = 11.sp,
                    color = TextGray
                )
                AppTextField(
                    value = priceText,
                    onValueChange = { input -> priceText = input.filter { it.isDigit() }.take(9) },
                    placeholder = "Harga jual",
                    keyboardType = KeyboardType.Number
                )
                val error = uiState.priceError
                if (error != null) {
                    Text(text = error, color = RedDark, fontSize = 12.sp)
                }
                PrimaryButton(
                    text = "Simpan",
                    onClick = { onUpdatePrice(target, priceText) { editing = null } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlineButton(text = "Batal", onClick = { editing = null }, color = TextDark, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Data Penjualan
// ---------------------------------------------------------------------------

@Composable
fun SalesReportScreen(
    uiState: SalesUiState,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.shadow(4.dp)) {
            ScreenTopBar(title = "Data Penjualan", onBack = onBack)
        }
        if (uiState.isLoading) {
            LoadingBox()
            return@Column
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStat(
                    value = formatRupiah(uiState.totalRevenue),
                    label = "Total pendapatan",
                    modifier = Modifier.weight(1f)
                )
                MiniStat(
                    value = uiState.ticketsSold.toString(),
                    label = "Tiket terjual",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStat(
                    value = formatRupiah(uiState.averagePrice),
                    label = "Rata-rata harga jual",
                    background = YellowSoft,
                    valueColor = Color(0xFF4A3500),
                    modifier = Modifier.weight(1f)
                )
                MiniStat(
                    value = uiState.bestSeller,
                    label = "Tiket terlaris",
                    background = GreenSoft,
                    valueColor = Color(0xFF1B6B2E),
                    modifier = Modifier.weight(1f)
                )
            }

            WhiteCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Tiket terjual per event", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.bars.isEmpty()) {
                    EmptyBox(text = "Belum ada penjualan.")
                } else {
                    // Grafik batang sederhana: panjang batang = tiket terjual / nilai tertinggi
                    uiState.bars.forEach { bar ->
                        Text(text = bar.label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(BlueSurface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(bar.tickets.toFloat() / uiState.maxTickets.coerceAtLeast(1))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(BluePrimary)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${bar.tickets} tiket", fontSize = 10.sp, color = TextGray)
                        }
                        Text(text = formatRupiah(bar.revenue), fontSize = 10.sp, color = TextGray)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            Text(
                text = "Data dihitung dari pesanan toko yang sudah dibayar.",
                fontSize = 10.sp,
                color = TextGray
            )
        }
    }
}
