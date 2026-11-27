
package com.example.tiketiin.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiketiin.data.model.*
import com.example.tiketiin.domain.*
import com.example.tiketiin.ui.components.*
import com.example.tiketiin.ui.theme.*


@Composable
fun OrdersScreen(
    uiState: OrdersUiState,
    onSelectTab: (Int) -> Unit,
    onSelectStatus: (OrderStatus) -> Unit,
    onRate: (Int, Int) -> Unit,
    onConfirmTicket: (Int) -> Unit,
    onOpenTicket: (Int) -> Unit,
    onRefund: (Int) -> Unit,
    isRefunding: Boolean
) {
    var selectedRefundOrderId by remember {
        mutableStateOf<Int?>(null)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 14.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Orders",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrderTypeTab(
                    label = "Order Saya",
                    selected = uiState.selectedTab == 0,
                    onClick = { onSelectTab(0) }
                )

                OrderTypeTab(
                    label = "Order Toko",
                    selected = uiState.selectedTab == 1,
                    onClick = { onSelectTab(1) }
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp
                    )
                )
                .background(Color.White)
        ) {

            // Pesan berhasil
            uiState.successMessage?.let { message ->
                Text(
                    text = message,
                    color = GreenMain,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                )
            }

// Pesan error
            uiState.error?.let { message ->
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                )
            }

            // Tab status pesanan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                OrderStatus.entries.forEach { status ->

                    val selected =
                        uiState.selectedStatus == status

                    val count =
                        uiState.countFor(status)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onSelectStatus(status)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (count > 0) {
                                "${status.label} ($count)"
                            } else {
                                status.label
                            },
                            fontSize = 8.sp,
                            maxLines = 1,
                            color = if (selected) {
                                BluePrimary
                            } else {
                                TextDark
                            },
                            fontWeight = if (selected) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                            modifier = Modifier.padding(vertical = 5.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    if (selected) BluePrimary
                                    else TextGray
                                )
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    LoadingBox()
                }

                uiState.visibleOrders.isEmpty() -> {
                    EmptyBox(
                        text = "Belum ada pesanan pada status ini."
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = 32.dp,
                            vertical = 14.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.visibleOrders) { item ->

                            if (uiState.selectedTab == 0) {
                                MyOrderCard(
                                    item = item,
                                    onRate = onRate,
                                    onConfirmTicket = onConfirmTicket,
                                    onOpenTicket = onOpenTicket,
                                    onRefund = { orderId -> selectedRefundOrderId = orderId },
                                    isRefunding = isRefunding
                                )
                            } else {
                                StoreOrderCard(
                                    item = item
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedRefundOrderId != null) {
        AlertDialog(
            onDismissRequest = {
                if (!isRefunding) {
                    selectedRefundOrderId = null
                }
            },
            title = {
                Text(
                    text = "Konfirmasi Refund",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Apakah kamu yakin ingin mengajukan refund untuk pesanan ini? " +
                            "Pesanan akan dibatalkan dan dana akan dikembalikan melalui " +
                            "simulasi escrow Tiketiin."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val orderId = selectedRefundOrderId

                        if (orderId != null && !isRefunding) {
                            selectedRefundOrderId = null
                            onRefund(orderId)
                        }
                    },
                    enabled = !isRefunding
                ) {
                    Text(
                        text = if (isRefunding) {
                            "Memproses..."
                        } else {
                            "Ya, Ajukan Refund"
                        },
                        color = BluePrimary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedRefundOrderId = null
                    },
                    enabled = !isRefunding
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

// =====================================================
// ORDER TYPE TAB
// =====================================================

@Composable
private fun OrderTypeTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) Color.White
                else LineGray
            )
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) {
                BluePrimary
            } else {
                TextGray
            }
        )
    }
}

// =====================================================
// ORDER INFORMATION
// =====================================================

@Composable
private fun OrderInfo(item: OrderItem) {

    val event = item.event

    Text(
        text = event?.name ?: "Event dihapus",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )

    if (event != null) {

        IconText(
            icon = AppIcons.Calendar,
            text = formatDateShort(event.date)
        )

        Row {
            IconText(
                icon = AppIcons.Location,
                text = "${event.venue}, ${event.city}",
                modifier = Modifier.weight(1f)
            )

            IconText(
                icon = AppIcons.Ticket,
                text = item.order.quantity.toString()
            )
        }
    }

    IconText(
        icon = AppIcons.Seat,
        text = item.order.seatLabel
    )
}


private fun Order.getEscrowMessage(): String {
    return when (escrowStatus.uppercase().trim()) {
        "HELD" ->
            "Pembayaran berhasil. Dana masih ditahan dalam escrow Tiketiin."

        "RELEASED" ->
            "Dana telah dicairkan kepada penjual."

        "REFUNDED" ->
            "Dana telah dikembalikan kepada pembeli."

        "PENDING" -> {
            when (paymentStatus.uppercase().trim()) {
                "PENDING" ->
                    "Menunggu pembayaran."

                "SUCCESS" ->
                    "Pembayaran berhasil. Status escrow sedang diperbarui."

                "EXPIRED" ->
                    "Pembayaran telah kedaluwarsa."

                "FAILED" ->
                    "Pembayaran gagal."

                else ->
                    "Status escrow belum tersedia."
            }
        }

        else ->
            "Status escrow: $escrowStatus"
    }
}


// =====================================================
// MY ORDER CARD (BUYER)
// =====================================================

@Composable
private fun MyOrderCard(
    item: OrderItem,
    onRate: (Int, Int) -> Unit,
    onConfirmTicket: (Int) -> Unit,
    onOpenTicket: (Int) -> Unit,
    onRefund: (Int) -> Unit,
    isRefunding: Boolean
) {

    val order = item.order

    WhiteCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onOpenTicket(order.id)
            }
    ) {

        OrderInfo(item = item)

        Spacer(modifier = Modifier.height(8.dp))

        when (order.status) {

            // PENDING
            OrderStatus.BELUM_BAYAR -> {
                Text(
                    text = "Menunggu pembayaran.",
                    fontSize = 11.sp,
                    color = TextGray
                )
            }

            // PAID

            OrderStatus.DIPROSES -> {

                Text(
                    text = order.getEscrowMessage(),
                    fontSize = 11.sp,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // TOMBOL REFUND
                if (
                    order.paymentStatus.equals("SUCCESS", ignoreCase = true) &&
                    order.escrowStatus.equals("HELD", ignoreCase = true)
                ) {
                    OutlinedButton(
                        onClick = {
                            onRefund(order.id)
                        },
                        enabled = !isRefunding,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isRefunding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )

                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Memproses refund...")
                        } else {
                            Text("Ajukan Refund")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // QR CODE
                val qrValue = order.qrCode
                    ?.takeIf { it.isNotBlank() }

                if (qrValue != null) {

                    TicketQrCode(
                        qrContent = qrValue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PrimaryButton(
                        text = "Konfirmasi tiket valid",
                        onClick = {
                            onConfirmTicket(order.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                } else {

                    Text(
                        text = "Kode QR belum tersedia. Silakan muat ulang pesanan.",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
            }


            // COMPLETED
            OrderStatus.SELESAI -> {

                Text(
                    text = order.getEscrowMessage(),
                    fontSize = 11.sp,
                    color = GreenMain
                )

                Spacer(modifier = Modifier.height(8.dp))

                val qrValue = order.qrCode
                    ?.takeIf { it.isNotBlank() }

                if (qrValue != null) {

                    QrCodeBox(
                        seed = qrValue,
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                } else {

                    Text(
                        text = "Kode QR tidak tersedia.",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
            }

            // CANCELLED

            OrderStatus.DIBATALKAN -> {

                if (order.escrowStatus.equals("REFUNDED", ignoreCase = true)) {

                    Text(
                        text = "Refund berhasil. Dana telah dikembalikan kepada pembeli.",
                        fontSize = 11.sp,
                        color = GreenMain
                    )

                } else {

                    Text(
                        text = "Pesanan telah dibatalkan.",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
            }


            // EXPIRED
            OrderStatus.KEDALUWARSA -> {

                Text(
                    text = "Batas waktu pembayaran telah habis.",
                    fontSize = 11.sp,
                    color = TextGray
                )
            }
        }
    }

    // Rating hanya tersedia setelah pesanan selesai

    // Rating hanya tersedia setelah pesanan selesai
    if (order.status == OrderStatus.SELESAI) {
        Spacer(modifier = Modifier.height(10.dp))

        val hasRated = order.rating > 0

        WhiteCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (hasRated) {
                            "Penilaian Kamu"
                        } else {
                            "Beri Penilaian"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = if (hasRated) {
                            "Terima kasih sudah memberikan penilaian!"
                        } else {
                            "Ketuk bintang untuk menilai penjual"
                        },
                        fontSize = 10.sp,
                        color = TextGray
                    )
                }

                for (star in 1..5) {
                    Icon(
                        imageVector = if (star <= order.rating) {
                            AppIcons.Star
                        } else {
                            AppIcons.StarBorder
                        },
                        contentDescription = "Bintang $star",
                        tint = if (star <= order.rating) {
                            YellowMain
                        } else {
                            TextGray
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                enabled = !hasRated
                            ) {
                                onRate(order.id, star)
                            }
                    )
                }
            }
        }
    }

}

// =====================================================
// STORE ORDER CARD (SELLER)
// =====================================================

@Composable
private fun StoreOrderCard(
    item: OrderItem
) {

    val order = item.order

    WhiteCard(
        modifier = Modifier.fillMaxWidth()
    ) {

        OrderInfo(item = item)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = LineGray
        )

        DetailRow(
            label = "Pembeli",
            value = order.buyerName
        )

        DetailRow(
            label = "No. Transaksi",
            value = order.transactionNo
        )

        val escrowLabel = when (order.escrowStatus.uppercase().trim()) {
            "HELD" -> "Dana ditahan"
            "RELEASED" -> "Dana diterima"
            "REFUNDED" -> "Dana dikembalikan"
            else -> "Status dana"
        }

        val sellerAmount = order.ticketPrice * order.quantity

        DetailRow(
            label = escrowLabel,
            value = formatRupiah(sellerAmount),
            bold = true
        )

        when (order.status) {


            // PAID
            OrderStatus.DIPROSES -> {

                Text(
                    text = "QR tiket telah dibuat.",
                    fontSize = 10.sp,
                    color = TextGray
                )

                Text(
                    text = order.getEscrowMessage(),
                    fontSize = 10.sp,
                    color = TextGray
                )
            }



            // COMPLETED
            OrderStatus.SELESAI -> {

                Text(
                    text = if (order.rating > 0) {
                        "Selesai. Penilaian pembeli: ${order.rating}/5"
                    } else {
                        "Selesai."
                    },
                    fontSize = 10.sp,
                    color = GreenMain
                )

                Text(
                    text = order.getEscrowMessage(),
                    fontSize = 10.sp,
                    color = GreenMain
                )
            }


            // PENDING
            OrderStatus.BELUM_BAYAR -> {

                Text(
                    text = "Menunggu pembayaran pembeli.",
                    fontSize = 10.sp,
                    color = TextGray
                )
            }

            // CANCELLED
            OrderStatus.DIBATALKAN -> {

                Text(
                    text = "Pesanan telah dibatalkan oleh pembeli.",
                    fontSize = 10.sp,
                    color = TextGray
                )
            }

            // EXPIRED
            OrderStatus.KEDALUWARSA -> {

                Text(
                    text = "Pesanan kedaluwarsa karena belum dibayar.",
                    fontSize = 10.sp,
                    color = TextGray
                )
            }
        }
    }
}

// =====================================================
// TICKET DETAIL SCREEN
// =====================================================

@Composable
fun TicketDetailScreen(
    uiState: TicketUiState,
    onBack: () -> Unit
) {

    val item = uiState.item

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenGray)
    ) {

        ScreenTopBar(
            title = "Tiket Saya",
            onBack = onBack
        )

        if (uiState.isLoading) {

            LoadingBox()

        } else if (item == null) {

            EmptyBox(
                text = "Tiket tidak ditemukan."
            )

        } else {

            val order = item.order
            val event = item.event

            val qrValue = order.qrCode
                ?.takeIf { it.isNotBlank() }

            val hasTicket = (
                    order.status == OrderStatus.DIPROSES ||
                            order.status == OrderStatus.SELESAI
                    ) && qrValue != null

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (event != null) {

                    PosterBox(
                        event = event,
                        titleSize = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }

                WhiteCard(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    OrderInfo(item = item)

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (item.daysToEvent >= 0) {
                                    BlueSurface
                                } else {
                                    FieldGray
                                }
                            )
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = when {
                                item.daysToEvent > 0 ->
                                    "Event dimulai ${item.daysToEvent} hari lagi"

                                item.daysToEvent == 0L ->
                                    "Event berlangsung hari ini"

                                else ->
                                    "Event sudah selesai"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.daysToEvent >= 0) {
                                BluePrimary
                            } else {
                                TextGray
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (hasTicket) {

                        QrCodeBox(
                            seed = qrValue!!,
                            modifier = Modifier
                                .size(200.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        Text(
                            text = "Tunjukkan kode ini di pintu masuk venue.",
                            fontSize = 10.sp,
                            color = TextGray,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 6.dp)
                        )

                    } else {

                        Text(
                            text = "Kode QR belum tersedia. Silakan periksa kembali status pembayaran.",
                            fontSize = 11.sp,
                            color = TextGray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }


                // Tombol unduh e-ticket dari penjual
                WhiteCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "E-ticket Kamu",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Unduh tiket asli yang diberikan oleh penjual.",
                                fontSize = 10.sp,
                                color = TextGray
                            )
                        }

                        Icon(
                            imageVector = AppIcons.Document,
                            contentDescription = "E-ticket",
                            tint = BluePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            // Akan dihubungkan ke file e-ticket
                            // dari Supabase Storage
                        },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary
                        )
                    ) {
                        Icon(
                            imageVector = AppIcons.Document,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Unduh E-ticket")
                    }

                    Text(
                        text = "E-ticket belum tersedia dari penjual.",
                        fontSize = 10.sp,
                        color = TextGray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }


                // Detail transaksi
                WhiteCard(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    DetailRow(
                        label = "Status",
                        value = order.status.label
                    )

                    DetailRow(
                        label = "Nama Pemesan",
                        value = order.buyerName
                    )

                    DetailRow(
                        label = "Metode Pembayaran",
                        value = order.paymentMethod
                    )

                    DetailRow(
                        label = "No. Transaksi",
                        value = order.transactionNo
                    )

                    DetailRow(
                        label = "Waktu",
                        value = order.createdAt
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = LineGray
                    )

                    DetailRow(
                        label = "Harga Tiket",
                        value = formatRupiah(order.ticketPrice)
                    )

                    DetailRow(
                        label = "Biaya Layanan (5%)",
                        value = formatRupiah(order.serviceFee)
                    )

                    DetailRow(
                        label = "Total",
                        value = formatRupiah(order.total),
                        bold = true
                    )
                }
            }
        }
    }
}
