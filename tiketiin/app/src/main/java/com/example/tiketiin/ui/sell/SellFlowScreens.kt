package com.example.tiketiin.ui.sell

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
import androidx.compose.ui.graphics.vector.ImageVector

// Tombol besar di bagian bawah layar alur jual
@Composable
private fun BottomAction(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp)
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 32.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

// ---------------------------------------------------------------------------
// Langkah 1 - Verifikasi e-KYC (hanya sekali, saat pertama kali menjual)
// ---------------------------------------------------------------------------

@Composable
fun EkycScreen(
    uiState: SellUiState,
    onBack: () -> Unit,
    onToggleKtp: () -> Unit,
    onToggleSelfie: () -> Unit,
    onNameChange: (String) -> Unit,
    onNikChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        ScreenTopBar(title = "Jual Tiket", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 8.dp)
        ) {
            SellStepper(currentStep = 1)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Verifikasi Identitas (e-KYC)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Unggah KTP", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            UploadBox(
                icon = AppIcons.Upload,
                label = if (uiState.ktpUploaded) "KTP terunggah. Ketuk untuk mengganti." else "Unggah atau foto KTP",
                done = uiState.ktpUploaded,
                onClick = onToggleKtp,
                modifier = Modifier.fillMaxWidth().height(110.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Foto Selfie dengan KTP", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BlueSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.Person,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(text = "Pastikan wajah dan tulisan jelas.", fontSize = 9.sp, color = TextGray)
                }
                UploadBox(
                    icon = AppIcons.Camera,
                    label = if (uiState.selfieTaken) "Selfie tersimpan" else "Buka Kamera",
                    done = uiState.selfieTaken,
                    onClick = onToggleSelfie,
                    modifier = Modifier.weight(1f).height(110.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Detail KTP", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            FormField(label = "Nama", hint = "Gunakan nama lengkap sesuai dengan KTP/Paspor.") {
                AppTextField(value = uiState.kycName, onValueChange = onNameChange, placeholder = "Nama lengkap")
            }
            Spacer(modifier = Modifier.height(10.dp))
            FormField(label = "NIK", hint = "Gunakan nomor yang tertera pada kartu identitas kamu.") {
                AppTextField(
                    value = uiState.kycNik,
                    onValueChange = onNikChange,
                    placeholder = "16 digit NIK",
                    keyboardType = KeyboardType.Number
                )
            }
            val error = uiState.kycError
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = RedDark, fontSize = 12.sp)
            }
        }
        BottomAction {
            PrimaryButton(
                text = if (uiState.isVerifying) "Memverifikasi..." else "Selanjutnya",
                onClick = onSubmit,
                enabled = !uiState.isVerifying,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Kotak unggah tiruan: ketuk untuk menandai sudah diunggah (kamera/galeri asli menyusul).
@Composable
private fun UploadBox(
    icon: ImageVector,
    label: String,
    done: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, if (done) GreenMain else BluePrimary, RoundedCornerShape(12.dp))
            .background(if (done) GreenSoft else Color.White)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (done) AppIcons.CheckCircle else icon,
            contentDescription = null,
            tint = if (done) GreenMain else BluePrimary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (done) GreenMain else BluePrimary,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------------------
// Langkah 2 - Form Jual Tiket
// ---------------------------------------------------------------------------

@Composable
fun SellFormScreen(
    uiState: SellUiState,
    onBack: () -> Unit,
    onSelectEvent: (Event) -> Unit,
    onSeatCategoryChange: (String) -> Unit,
    onRowChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onFaceValueChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onToggleProof: () -> Unit,
    onSubmit: () -> Unit
) {
    val event = uiState.selectedEvent
    var menuOpen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        ScreenTopBar(title = "Jual Tiket", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 8.dp)
        ) {
            SellStepper(currentStep = 2)
            Spacer(modifier = Modifier.height(12.dp))

            WhiteCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Buat Listing Baru", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))

                // Nama event dipilih dari daftar event yang ada
                FormField(label = "Nama Event") {
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, TextGray, RoundedCornerShape(16.dp))
                                .clickable { menuOpen = true }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event?.name ?: "Pilih event",
                                fontSize = 14.sp,
                                color = if (event == null) TextGray else TextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(imageVector = AppIcons.ChevronDown, contentDescription = null, tint = TextGray)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            uiState.events.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(text = option.name, fontSize = 13.sp) },
                                    onClick = {
                                        onSelectEvent(option)
                                        menuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Data di bawah ini otomatis mengikuti event yang dipilih
                FormField(label = "Kategori") {
                    AppTextField(value = event?.category?.label ?: "", onValueChange = {}, placeholder = "Mengikuti event", readOnly = true)
                }
                Spacer(modifier = Modifier.height(8.dp))
                FormField(label = "Lokasi") {
                    AppTextField(
                        value = if (event != null) "${event.venue}, ${event.city}" else "",
                        onValueChange = {},
                        placeholder = "Mengikuti event",
                        readOnly = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FormField(label = "Tanggal", modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = if (event != null) formatDateShort(event.date) else "",
                            onValueChange = {},
                            placeholder = "-",
                            readOnly = true
                        )
                    }
                    FormField(label = "Waktu", modifier = Modifier.weight(1f)) {
                        AppTextField(value = event?.time ?: "", onValueChange = {}, placeholder = "-", readOnly = true)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FormField(label = "Tipe Tiket", modifier = Modifier.weight(1f)) {
                        AppTextField(value = uiState.seatCategory, onValueChange = onSeatCategoryChange, placeholder = "CAT 1")
                    }
                    FormField(label = "Baris / Kursi", modifier = Modifier.weight(1f)) {
                        AppTextField(value = uiState.row, onValueChange = onRowChange, placeholder = "Row 1.12")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                FormField(label = "Jumlah Tiket") {
                    AppTextField(
                        value = uiState.quantityText,
                        onValueChange = onQuantityChange,
                        placeholder = "1",
                        keyboardType = KeyboardType.Number
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FormField(label = "Harga asli (face value)", hint = "Harga saat kamu membeli tiket ini, dalam rupiah.") {
                    AppTextField(
                        value = uiState.faceValueText,
                        onValueChange = onFaceValueChange,
                        placeholder = "1200000",
                        keyboardType = KeyboardType.Number
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FormField(label = "Harga jual", hint = "Akan dicek kewajarannya di langkah berikutnya.") {
                    AppTextField(
                        value = uiState.priceText,
                        onValueChange = onPriceChange,
                        placeholder = "1500000",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            WhiteCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Bukti tiket", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleProof),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (uiState.proofAttached) GreenSoft else BlueSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.proofAttached) AppIcons.Document else AppIcons.Upload,
                            contentDescription = null,
                            tint = if (uiState.proofAttached) GreenMain else BluePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (uiState.proofAttached) "eticket.pdf" else "Ketuk untuk mengunggah e-ticket",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (uiState.proofAttached) {
                                "Terbaca. Dicek hasil keasliannya saat listing tayang"
                            } else {
                                "Format PDF atau foto tiket"
                            },
                            fontSize = 10.sp,
                            color = TextGray
                        )
                    }
                }
            }
            val error = uiState.formError
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = RedDark, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        BottomAction {
            PrimaryButton(text = "Selanjutnya", onClick = onSubmit, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ---------------------------------------------------------------------------
// Langkah 3 - Cek Harga Wajar (Fungsi 1)
// ---------------------------------------------------------------------------

@Composable
fun PriceCheckScreen(
    uiState: SellUiState,
    onBack: () -> Unit,
    onPublish: (Long) -> Unit
) {
    val result = uiState.priceResult
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        ScreenTopBar(title = "Jual Tiket", onBack = onBack)
        if (result == null) {
            EmptyBox(text = "Isi form jual tiket terlebih dahulu.", modifier = Modifier.weight(1f))
            return@Column
        }

        val statusColor = when (result.status) {
            PriceStatus.WAJAR -> GreenMain
            PriceStatus.PERLU_DITINJAU -> YellowMain
            PriceStatus.TERLALU_TINGGI -> RedDark
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 8.dp)
        ) {
            SellStepper(currentStep = 3)
            Spacer(modifier = Modifier.height(12.dp))

            WhiteCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Cek Harga Wajar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PriceBox(
                        label = "Harga asli (face value)",
                        value = formatRupiah(result.faceValue),
                        borderColor = Color.Transparent,
                        modifier = Modifier.weight(1f)
                    )
                    PriceBox(
                        label = "Harga jual",
                        value = formatRupiah(result.sellPrice),
                        borderColor = statusColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Skala Wajar - Perlu ditinjau - Terlalu tinggi
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50))) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(GreenMain))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(YellowMain))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(RedDark))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    PriceStatus.entries.forEach { status ->
                        val active = status == result.status
                        Text(
                            text = if (active) "▲ ${status.label}" else status.label,
                            fontSize = 10.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) statusColor else TextGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (result.status == PriceStatus.WAJAR) AppIcons.CheckCircle else AppIcons.Warning,
                        contentDescription = null,
                        tint = statusColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (result.status) {
                            PriceStatus.WAJAR ->
                                "Harga jualmu wajar (markup %.1f%%). Listing siap diterbitkan.".format(result.markupPercent)
                            PriceStatus.PERLU_DITINJAU ->
                                "Harga jualmu %.1f%% di atas batas wajar. Pembeli bisa ragu atau menawar jauh di bawah.".format(result.overRangePercent)
                            PriceStatus.TERLALU_TINGGI ->
                                "Markup %.1f%% melewati batas 50%%. Listing ditolak kecuali harganya disesuaikan.".format(result.markupPercent)
                        },
                        fontSize = 11.sp,
                        color = TextDark,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BlueSurface)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(text = "Rekomendasi rentang harga wajar", fontSize = 10.sp, color = TextGray)
                    Text(
                        text = "${formatRupiah(result.rangeMin)} - ${formatRupiah(result.rangeMax)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            WhiteCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Dasar Perhitungan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                DetailRow(
                    label = if (result.usedHistory) "Histori tiket terjual" else "Rentang dari markup wajar",
                    value = "${formatRupiah(result.rangeMin)} - ${formatRupiah(result.rangeMax)}"
                )
                DetailRow(label = "Batas markup wajar", value = "maks. 22%")
                DetailRow(label = "Markup harga jualmu", value = "%.1f%%".format(result.markupPercent))
                DetailRow(label = "Kamu terima (harga rekomendasi)", value = formatRupiah(result.recommendedPrice), bold = true)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        BottomAction {
            if (result.status != PriceStatus.WAJAR) {
                PrimaryButton(
                    text = "Terapkan ${formatRupiah(result.recommendedPrice)}",
                    onClick = { onPublish(result.recommendedPrice) },
                    enabled = !uiState.isPublishing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (result.status == PriceStatus.WAJAR) {
                PrimaryButton(
                    text = if (uiState.isPublishing) "Menerbitkan..." else "Terbitkan ${formatRupiah(result.sellPrice)}",
                    onClick = { onPublish(result.sellPrice) },
                    enabled = !uiState.isPublishing,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlineButton(
                    text = "Tetap terbitkan ${formatRupiah(result.sellPrice)}",
                    onClick = { onPublish(result.sellPrice) },
                    enabled = result.status != PriceStatus.TERLALU_TINGGI && !uiState.isPublishing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PriceBox(label: String, value: String, borderColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ScreenGray)
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = TextGray, maxLines = 1)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------------------------
// Langkah 4 - Listing Berhasil Tayang
// ---------------------------------------------------------------------------

@Composable
fun ListingPublishedScreen(
    uiState: SellUiState,
    onViewStoreOrders: () -> Unit,
    onViewListings: () -> Unit
) {
    val listing = uiState.publishedListing
    val event = uiState.publishedEvent
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        ScreenTopBar(title = "Listing Berhasil Tayang")
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SellStepper(currentStep = 4)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Pembeli yang mencari event ini akan melihat tiketmu",
                fontSize = 11.sp,
                color = BluePrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Icon(imageVector = AppIcons.CheckCircle, contentDescription = null, tint = GreenMain, modifier = Modifier.size(56.dp))
            Text(text = "Berhasil", fontSize = 11.sp, color = TextGray)
            Spacer(modifier = Modifier.height(8.dp))

            if (listing != null && event != null) {
                WhiteCard(modifier = Modifier.fillMaxWidth()) {
                    Row {
                        PosterBox(
                            event = event,
                            titleSize = 9.sp,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = event.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            IconText(icon = AppIcons.Location, text = event.city)
                            IconText(icon = AppIcons.Calendar, text = formatDateShort(event.date))
                            IconText(icon = AppIcons.Seat, text = "${listing.seatCategory}, ${listing.row}")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatRupiah(listing.price),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(GreenSoft)
                                        .padding(horizontal = 14.dp, vertical = 1.dp)
                                ) {
                                    Text(text = listing.status.label, fontSize = 10.sp, color = GreenMain, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat(value = listing.views.toString(), label = "Dilihat", modifier = Modifier.weight(1f))
                    MiniStat(value = listing.saves.toString(), label = "Disimpan", modifier = Modifier.weight(1f))
                    MiniStat(value = listing.offers.toString(), label = "Nego masuk", modifier = Modifier.weight(1f))
                }
            }
        }
        BottomAction {
            PrimaryButton(text = "Lihat Order Toko", onClick = onViewStoreOrders, modifier = Modifier.fillMaxWidth())
            OutlineButton(text = "Kelola Tiket Saya", onClick = onViewListings, modifier = Modifier.fillMaxWidth())
        }
    }
}
