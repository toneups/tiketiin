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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

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
    onSelectCategory: (String) -> Unit,
    onSelectMethod: (String) -> Unit,
    onPay: () -> Unit
) {
    var expandedCategory by remember {
        mutableStateOf<String?>(null)
    }

    val bankOptions = PAYMENT_OPTIONS["Transfer Bank"].orEmpty()
    val cardOptions = PAYMENT_OPTIONS["Kartu Kredit / Debit"].orEmpty()
    val walletOptions = PAYMENT_OPTIONS["E-Wallet / QRIS"].orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ScreenTopBar(
            title = "Metode Pembayaran",
            onBack = onBack
        )

        LockTimerBanner(secondsLeft = uiState.secondsLeft)

        if (uiState.event != null) {
            OrderEventHeader(
                event = uiState.event,
                seatLabel = uiState.seatLabel,
                priceLabel = formatRupiah(
                    uiState.listing?.price ?: 0L
                ),
                quantity = uiState.quantity
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Pilih Metode Pembayaran",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // 1. TRANSFER BANK
            PaymentCategoryButton(
                title = "Transfer Bank",
                selected = uiState.paymentCategory == "Transfer Bank",
                expanded = expandedCategory == "Transfer Bank",
                onClick = {
                    val opening = expandedCategory != "Transfer Bank"

                    expandedCategory =
                        if (opening) "Transfer Bank" else null

                    if (opening) {
                        onSelectCategory("Transfer Bank")
                    }
                }
            )

            // Dropdown bank muncul tepat di bawah tombol
            if (expandedCategory == "Transfer Bank") {
                PaymentOptionsList(
                    options = bankOptions,
                    selectedMethod = uiState.paymentMethod,
                    onSelect = onSelectMethod
                )
            }

            // 2. KARTU KREDIT / DEBIT
            PaymentCategoryButton(
                title = "Kartu Kredit / Debit",
                selected = uiState.paymentCategory == "Kartu Kredit / Debit",
                expanded = expandedCategory == "Kartu Kredit / Debit",
                onClick = {
                    val opening =
                        expandedCategory != "Kartu Kredit / Debit"

                    expandedCategory =
                        if (opening) "Kartu Kredit / Debit" else null

                    if (opening) {
                        onSelectCategory("Kartu Kredit / Debit")
                    }
                }
            )

            if (expandedCategory == "Kartu Kredit / Debit") {
                PaymentOptionsList(
                    options = cardOptions,
                    selectedMethod = uiState.paymentMethod,
                    onSelect = onSelectMethod
                )
            }

            // 3. E-WALLET / QRIS
            PaymentCategoryButton(
                title = "QRIS / E-Wallet",
                selected = uiState.paymentCategory == "E-Wallet / QRIS",
                expanded = expandedCategory == "E-Wallet / QRIS",
                onClick = {
                    val opening =
                        expandedCategory != "E-Wallet / QRIS"

                    expandedCategory =
                        if (opening) "E-Wallet / QRIS" else null

                    if (opening) {
                        onSelectCategory("E-Wallet / QRIS")
                    }
                }
            )

            // Dropdown e-wallet muncul tepat di bawah tombol
            if (expandedCategory == "E-Wallet / QRIS") {
                PaymentOptionsList(
                    options = walletOptions,
                    selectedMethod = uiState.paymentMethod,
                    onSelect = onSelectMethod
                )
            }

            // RINCIAN PEMBAYARAN
            WhiteCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Rincian Pembayaran",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                DetailRow(
                    label = "Harga Tiket",
                    value = formatRupiah(uiState.ticketPrice)
                )

                DetailRow(
                    label = "Biaya Layanan (5%)",
                    value = formatRupiah(uiState.serviceFee)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = LineGray
                )

                DetailRow(
                    label = "Total",
                    value = formatRupiah(uiState.total),
                    bold = true
                )
            }


            uiState.payError?.let { error ->
                Text(
                    text = error,
                    color = RedDark,
                    fontSize = 12.sp
                )
            }
        }

        CheckoutBottomBar(
            quantity = uiState.quantity,
            amount = uiState.total,
            buttonText = if (uiState.isPaying) {
                "Memproses..."
            } else {
                "Bayar"
            },
            enabled = uiState.event != null &&
                    !uiState.isExpired &&
                    !uiState.isPaying,
            onClick = onPay
        )
    }
}



@Composable
private fun PaymentCategoryButton(
    title: String,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = if (selected) BluePrimary else LineGray,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            },
            color = Color(0xFF292929)
        )

        Icon(
            imageVector = if (expanded) {
                AppIcons.ChevronRight
            } else {
                AppIcons.ChevronRight
            },
            contentDescription = null,
            tint = BluePrimary
        )
    }
}


@Composable
private fun PaymentOptionsList(
    options: List<String>,
    selectedMethod: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7F6FA))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { method ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selectedMethod == method) {
                            BlueSoft
                        } else {
                            Color.Transparent
                        }
                    )
                    .clickable {
                        onSelect(method)
                    }
                    .padding(
                        horizontal = 12.dp,
                        vertical = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = method,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    fontWeight = if (selectedMethod == method) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    },
                    color = Color(0xFF292929)
                )

                if (selectedMethod == method) {
                    Icon(
                        imageVector = AppIcons.CheckCircle,
                        contentDescription = "Dipilih",
                        tint = BluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}



@Composable
fun PaymentInstructionScreen(
    uiState: CheckoutUiState,
    orderId: Int,
    onBack: () -> Unit,
    onConfirmPayment: () -> Unit
) {
    val method = uiState.paymentMethod
    val context = LocalContext.current

    val clipboard = context.getSystemService(
        Context.CLIPBOARD_SERVICE
    ) as ClipboardManager

    val paymentCode = "88${orderId.toString().padStart(10, '0')}"
    val paymentPayload = "TIKETIIN-$orderId-${uiState.total}"

    val isQris = method.equals("QRIS", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ScreenTopBar(
            title = "Instruksi Pembayaran",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selesaikan Pembayaran",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pesanan #$orderId",
                color = TextGray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            WhiteCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total Pembayaran",
                    fontSize = 12.sp,
                    color = TextGray
                )

                Text(
                    text = formatRupiah(uiState.total),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = LineGray
                )

                DetailRow(
                    label = "Metode Pembayaran",
                    value = method
                )

                DetailRow(
                    label = "Kode Pesanan",
                    value = orderId.toString()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isQris) {
                // PEMBAYARAN QRIS
                Text(
                    text = "Scan QR Code untuk Membayar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                TicketQrCode(
                    qrCode = paymentPayload
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pembayaran QRIS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BluePrimary
                )

                Text(
                    text = "Pindai QR Code menggunakan aplikasi pembayaran pilihanmu.",
                    fontSize = 11.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )

            } else {
                // TRANSFER BANK, KARTU, DAN E-WALLET
                Text(
                    text = "Kode Pembayaran",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                WhiteCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nomor Pembayaran",
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = paymentCode,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            val clip = ClipData.newPlainText(
                                "Kode Pembayaran",
                                paymentCode
                            )

                            clipboard.setPrimaryClip(clip)

                            Toast.makeText(
                                context,
                                "Kode pembayaran berhasil disalin!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Ticket,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Salin Kode Pembayaran")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Gunakan kode pembayaran di atas melalui $method.",
                    fontSize = 11.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp)
                .background(Color.White)
                .navigationBarsPadding()
                .padding(vertical = 14.dp)
        ) {
            PrimaryButton(
                text = if (uiState.isPaying) {
                    "Memproses..."
                } else {
                    "Konfirmasi Pembayaran"
                },
                onClick = onConfirmPayment,
                enabled = !uiState.isPaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
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
    val context = LocalContext.current

    val bookingUrl = hotel.bookingUrl
        ?.trim()
        ?.takeIf { url ->
            url.startsWith("https://", ignoreCase = true) ||
                    url.startsWith("http://", ignoreCase = true)
        }

    Column(
        modifier = Modifier
            .width(170.dp)
            .height(320.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {

        // GAMBAR HOTEL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
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

            if (!hotel.imageUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = hotel.imageUrl,
                    contentDescription = "Foto ${hotel.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
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

            // BADGE HOTEL TERDEKAT
            if (isNearest) {
                FlashBadge(
                    text = "Terdekat",
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }

        // INFORMASI HOTEL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp)
        ) {

            // Nama hotel
            Text(
                text = hotel.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                minLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Rating
            Text(
                text = "★ ${hotel.rating}",
                fontSize = 10.sp,
                color = Color(0xFFFFA000),
                maxLines = 1,
                minLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Jarak hotel dari venue
            Text(
                text = "Jarak ${
                    String.format(
                        Locale.US,
                        "%.2f",
                        item.distanceKm
                    )
                } km dari venue",
                fontSize = 8.sp,
                color = TextGray,
                maxLines = 1,
                minLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Alamat hotel
            Text(
                text = hotel.area,
                fontSize = 8.sp,
                color = TextGray,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Harga per malam
            Text(
                text = formatRupiah(hotel.pricePerNight),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                minLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Ketersediaan kamar
            Text(
                text = "${hotel.availableRooms} kamar tersedia",
                fontSize = 9.sp,
                color = TextGray,
                maxLines = 1,
                minLines = 1
            )

            // Mendorong tombol ke bagian bawah
            Spacer(modifier = Modifier.weight(1f))

            // Tombol booking eksternal
            // Tombol booking eksternal
            Button(
                onClick = {
                    bookingUrl?.let { url ->
                        uriHandler.openUri(url)
                    }
                },
                enabled = bookingUrl != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentPadding = PaddingValues(
                    horizontal = 4.dp,
                    vertical = 4.dp
                ),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    disabledContainerColor = Color.LightGray
                )
            ) {
                Text(
                    text = if (bookingUrl != null) {
                        "Lihat Hotel"
                    } else {
                        "Link belum tersedia"
                    },
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

