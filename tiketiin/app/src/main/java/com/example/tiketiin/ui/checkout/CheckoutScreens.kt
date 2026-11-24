package com.example.tiketiin.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalUriHandler
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import java.util.Locale

// Pita "selesaikan pesanan dalam 09 menit : 30 detik"
@Composable
private fun LockTimerBanner(secondsLeft: Int) {
    val text = buildAnnotatedString {
        if (secondsLeft > 0) {
            append("Tiket berhasil disimpan, selesaikan pesanan dalam\n")
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append("%02d menit : %02d detik".format(secondsLeft / 60, secondsLeft % 60))
            }
        } else {
            append("Waktu pemesanan habis. Tiket dilepas kembali, silakan ulangi dari Detail Event.")
        }
    }
    InfoBanner(text = text)
}

// Bilah bawah: jumlah tiket, harga, dan satu tombol aksi
@Composable
private fun CheckoutBottomBar(
    quantity: Int,
    amount: Long,
    buttonText: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "$quantity Tiket", fontSize = 9.sp)
            Text(text = formatRupiah(amount), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text(text = buttonText, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ---------------------------------------------------------------------------
// Formulir Pesanan
// ---------------------------------------------------------------------------

@Composable
fun OrderFormScreen(
    uiState: CheckoutUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onIdNumberChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onTogglePowerOfAttorney: () -> Unit,
    onContinue: () -> Unit
) {
    val event = uiState.event
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        ScreenTopBar(title = "Formulir Pesanan", onBack = onBack)
        LockTimerBanner(secondsLeft = uiState.secondsLeft)

        if (event == null) {
            LoadingBox(modifier = Modifier.weight(1f))
        } else {
            OrderEventHeader(
                event = event,
                seatLabel = uiState.seatLabel,
                priceLabel = formatRupiah(uiState.listing?.price ?: 0L),
                quantity = uiState.quantity
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Formulir Pesanan", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                FormField(label = "Nama", hint = "Gunakan nama lengkap sesuai dengan KTP/Paspor.") {
                    AppTextField(value = uiState.name, onValueChange = onNameChange, placeholder = "Nama lengkap")
                }
                FormField(label = "Email", hint = "Gunakan email yang masih aktif.") {
                    AppTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        placeholder = "Email",
                        keyboardType = KeyboardType.Email
                    )
                }
                FormField(label = "Nomor HP", hint = "Gunakan nomor HP yang masih aktif.") {
                    AppTextField(
                        value = uiState.phone,
                        onValueChange = onPhoneChange,
                        placeholder = "Nomor HP",
                        keyboardType = KeyboardType.Phone
                    )
                }
                FormField(
                    label = "Nomor Kartu Identitas (KTP/Paspor)",
                    hint = "Gunakan nomor yang tertera pada kartu identitas kamu."
                ) {
                    AppTextField(
                        value = uiState.idNumber,
                        onValueChange = onIdNumberChange,
                        placeholder = "Nomor KTP/Paspor",
                        keyboardType = KeyboardType.Number
                    )
                }
                FormField(label = "Alamat", hint = "Gunakan alamat yang sesuai dengan KTP/Paspor.") {
                    AppTextField(value = uiState.address, onValueChange = onAddressChange, placeholder = "Alamat")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Surat Pindah Kuasa", fontSize = 13.sp)
                        Text(
                            text = "Buat surat pindah kuasa otomatis untuk penukaran tiket di venue.",
                            fontSize = 9.sp,
                            color = TextGray
                        )
                    }
                    Switch(checked = uiState.powerOfAttorney, onCheckedChange = { onTogglePowerOfAttorney() })
                }
                val error = uiState.formError
                if (error != null) {
                    Text(text = error, color = RedDark, fontSize = 12.sp)
                }
            }
        }

        Box(modifier = Modifier.shadow(12.dp).background(Color.White)) {
            CheckoutBottomBar(
                quantity = uiState.quantity,
                amount = uiState.ticketPrice,
                buttonText = "Lanjutkan",
                enabled = event != null && !uiState.isExpired,
                onClick = onContinue
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Pembayaran
// ---------------------------------------------------------------------------

@Composable
fun PaymentScreen(
    uiState: CheckoutUiState,
    onBack: () -> Unit,
    onSelectMethod: (String) -> Unit,
    onPay: () -> Unit
) {
    val event = uiState.event
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        ScreenTopBar(title = "Formulir Pesanan", onBack = onBack)
        LockTimerBanner(secondsLeft = uiState.secondsLeft)

        if (event != null) {
            OrderEventHeader(
                event = event,
                seatLabel = uiState.seatLabel,
                priceLabel = formatRupiah(uiState.listing?.price ?: 0L),
                quantity = uiState.quantity
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 14.dp)
        ) {
            Text(text = "Metode Pembayaran", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            PAYMENT_METHODS.forEach { method ->
                val selected = uiState.paymentMethod == method
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            if (selected) 2.dp else 1.dp,
                            if (selected) BluePrimary else TextGray,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectMethod(method) }
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected, onClick = { onSelectMethod(method) })
                    Text(
                        text = method,
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "Rincian Pembayaran", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            WhiteCard(modifier = Modifier.fillMaxWidth()) {
                DetailRow(label = "Harga Tiket", value = formatRupiah(uiState.ticketPrice))
                DetailRow(label = "Garansi Tiket", value = "Rp0")
                DetailRow(label = "Biaya Layanan (5%)", value = formatRupiah(uiState.serviceFee))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = LineGray)
                DetailRow(label = "Total", value = formatRupiah(uiState.total), bold = true)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dana kamu ditahan (escrow) sampai tiket dikonfirmasi valid.",
                fontSize = 10.sp,
                color = TextGray
            )
            val error = uiState.payError
            if (error != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = error, color = RedDark, fontSize = 12.sp)
            }
        }

        Column(modifier = Modifier.shadow(12.dp).background(Color.White)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp, top = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, BluePrimary, RoundedCornerShape(10.dp))
                    .background(BlueSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = AppIcons.Ticket, contentDescription = null, tint = BluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Punya kode referal?", fontSize = 13.sp, modifier = Modifier.weight(1f))
                Icon(imageVector = AppIcons.ChevronRight, contentDescription = null, tint = BluePrimary)
            }
            CheckoutBottomBar(
                quantity = uiState.quantity,
                amount = uiState.total,
                buttonText = if (uiState.isPaying) "Memproses..." else "Bayar",
                enabled = event != null && !uiState.isExpired && !uiState.isPaying,
                onClick = onPay
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Pembayaran Berhasil + Rekomendasi Hotel
// ---------------------------------------------------------------------------

@Composable
fun PaymentSuccessScreen(
    uiState: PaymentSuccessUiState,
    onBack: () -> Unit,
    onViewOrders: () -> Unit
) {
    val order = uiState.order
    val event = uiState.event

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        ScreenTopBar(title = "Pembayaran Berhasil", onBack = onBack)

        if (uiState.isLoading) {
            LoadingBox(modifier = Modifier.weight(1f))

        } else if (order == null || event == null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = AppIcons.Ticket,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = uiState.error
                        ?: uiState.hotelError
                        ?: "Informasi pesanan belum dapat dimuat",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Silakan periksa kembali pesanan kamu.",
                    fontSize = 12.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(BlueSoft)
                        .padding(horizontal = 40.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Pembayaranmu berhasil diproses. Pesanan tiketmu sudah tercatat di ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("Tiketiin!") }
                        },
                        fontSize = 10.sp,
                        color = BluePrimary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Icon(
                    imageVector = AppIcons.CheckCircle,
                    contentDescription = null,
                    tint = GreenMain,
                    modifier = Modifier.size(56.dp).align(Alignment.CenterHorizontally)
                )
                WhiteCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp)) {
                    Text(
                        text = formatRupiah(order.total),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "Berhasil",
                        fontSize = 11.sp,
                        color = TextGray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = event.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    IconText(icon = AppIcons.Calendar, text = formatDateShort(event.date))
                    Row {
                        IconText(
                            icon = AppIcons.Location,
                            text = "${event.venue}, ${event.city}",
                            modifier = Modifier.weight(1f)
                        )
                        IconText(icon = AppIcons.Ticket, text = order.quantity.toString())
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = LineGray)
                    DetailRow(label = "Nama Pemesan", value = order.buyerName)
                    DetailRow(label = "Metode Pembayaran", value = order.paymentMethod)
                    DetailRow(label = "No. Transaksi", value = order.transactionNo)
                    DetailRow(label = "Waktu", value = order.createdAt)

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!order.qrCode.isNullOrBlank()) {
                        TicketQrCode(
                            qrCode = order.qrCode
                        )
                    } else {
                        Text(
                            text = "QR tiket belum tersedia.",
                            fontSize = 11.sp,
                            color = RedDark,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                }

                // FUNGSI 4: rekomendasi hotel terdekat dari venue
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Rekomendasi Hotel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                when {
                    uiState.hotelError != null -> {
                        EmptyBox(
                            text = uiState.hotelError
                        )
                    }

                    uiState.hotels.isEmpty() -> {
                        EmptyBox(
                            text = "Belum ada hotel tersedia di sekitar lokasi event."
                        )
                    }

                    else -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(uiState.hotels) { index, item ->
                                HotelCard(
                                    item = item,
                                    isNearest = index == 0
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!uiState.isLoading) {
            PrimaryButton(
                text = "Lihat Pesanan",
                onClick = onViewOrders,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp, vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun HotelCard(
    item: NearbyHotel,
    isNearest: Boolean
) {
    val hotel = item.hotel
    val uriHandler = LocalUriHandler.current

    val bookingUrl = hotel.bookingUrl?.trim()?.takeIf {
        val uri = android.net.Uri.parse(it)

        uri.scheme in listOf("http", "https") &&
                !uri.host.isNullOrBlank()
    }

    Column(
        modifier = Modifier
            .width(170.dp)
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        // Gambar / placeholder hotel
        // Gambar hotel dari URL, dengan placeholder jika gambar gagal dimuat
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(hotel.colorStart),
                                Color(hotel.colorEnd)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Hotel,
                    contentDescription = "Hotel",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            if (!hotel.imageUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = hotel.imageUrl,
                    contentDescription = "Foto ${hotel.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator()
                    },
                    error = {
                        Icon(
                            imageVector = AppIcons.Hotel,
                            contentDescription = "Foto hotel tidak tersedia",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    success = {
                        SubcomposeAsyncImageContent()
                    }
                )
            }

            if (isNearest) {
                FlashBadge(
                    text = "Terdekat",
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }

        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Nama hotel
            Text(
                text = hotel.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Rating hotel
            Text(
                text = "★ ${hotel.rating}",
                fontSize = 10.sp,
                color = Color(0xFFFFA000)
            )

            // Jarak dari venue
            Text(
                text = "Jarak ${
                    String.format(Locale.US, "%.2f", item.distanceKm)
                } km dari venue",
                fontSize = 8.sp,
                color = TextGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Alamat
            Text(
                text = hotel.area,
                fontSize = 8.sp,
                color = TextGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Harga hotel
            Text(
                text = formatRupiah(hotel.pricePerNight),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${hotel.availableRooms} kamar tersedia",
                fontSize = 9.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol booking eksternal
            Button(
                onClick = {
                    bookingUrl?.let { uriHandler.openUri(it) }
                },
                enabled = bookingUrl != null,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 4.dp,
                    vertical = 6.dp
                ),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                Text(
                    text = if (bookingUrl != null) {
                        "Lihat Hotel"
                    } else {
                        "Link belum tersedia"
                    },
                    fontSize = 10.sp
                )
            }
        }
    }
}
