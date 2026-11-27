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
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

// ---------------------------------------------------------------------------
// Tab Jual Tiket: kartu Hot Opportunity + menu penjual
// ---------------------------------------------------------------------------

@Composable
fun SellScreen(
    uiState: HotOpportunityUiState,
    onSeeAll: () -> Unit,
    onSellTicket: (Int) -> Unit,
    onOpenEvent: (Int) -> Unit,
    onRetry: () -> Unit,
    onManageEvents: () -> Unit,
    onMyListings: () -> Unit,
    onSalesReport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append("Jual Tiketmu di ")
                appendLogo()
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Text(
            text = "Tiket batal dipakai? Tiketiin bisa ubah tiketmu jadi cuan!",
            fontSize = 13.sp,
            color = TextGray,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        val top = uiState.topOpportunity
        val error = uiState.errorMessage
        when {
            uiState.isLoading -> HotOpportunityFrame(onSeeAll = onSeeAll) { LoadingBox() }
            error != null -> HotOpportunityFrame(onSeeAll = onSeeAll) {
                ErrorBox(message = error, onRetry = onRetry)
            }
            top == null -> HotOpportunityFrame(onSeeAll = onSeeAll) {
                EmptyBox(text = "Belum ada event dengan permintaan tinggi saat ini.")
            }
            else -> HotOpportunityFrame(onSeeAll = onSeeAll) {
                HotOpportunityContent(
                    item = top,
                    onSellTicket = { onSellTicket(top.event.id) },
                    onOpenEvent = { onOpenEvent(top.event.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Menu Penjual",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SellerMenuTile(AppIcons.Event, "Kelola Event", onManageEvents, Modifier.weight(1f))
            SellerMenuTile(AppIcons.Ticket, "Kelola Tiket", onMyListings, Modifier.weight(1f))
            SellerMenuTile(AppIcons.Chart, "Data Penjualan", onSalesReport, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlineButton(
            text = "Jual tiket event lain",
            onClick = { onSellTicket(-1) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Bingkai kartu: bar biru "Hot Opportunity" + isi
@Composable
private fun HotOpportunityFrame(
    onSeeAll: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BluePrimary)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = AppIcons.Fire, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Hot Opportunity",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable(onClick = onSeeAll)
                    .padding(start = 12.dp, end = 6.dp, top = 3.dp, bottom = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Lihat Semua", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Icon(
                    imageVector = AppIcons.ChevronRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        content()
    }
}

@Composable
private fun HotOpportunityContent(
    item: HotOpportunity,
    onSellTicket: () -> Unit,
    onOpenEvent: () -> Unit
) {
    val event = item.event

    // Poster + badge + nama event
    Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        PosterBox(event = event, showTitle = false, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))))
        )
        if (item.isHighDemand) {
            HighDemandBadge(modifier = Modifier.padding(start = 14.dp, top = 12.dp))
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = event.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconText(
                icon = AppIcons.Calendar,
                text = formatDateLong(event.date),
                iconTint = Color.White,
                textColor = Color.White,
                fontSize = 11.sp
            )
            IconText(
                icon = AppIcons.Location,
                text = "${event.venue}, ${event.city}",
                iconTint = Color.White,
                textColor = Color.White,
                fontSize = 11.sp
            )
        }
    }

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
        Text(
            text = buildAnnotatedString {
                append("Event ini sedang ")
                withStyle(SpanStyle(color = BluePrimary, fontWeight = FontWeight.SemiBold)) {
                    append("banyak dicari dan permintaannya tinggi.")
                }
                append(" Ini waktu yang tepat untuk menjual tiket dengan keuntungan maksimal!")
            },
            fontSize = 12.sp,
            color = TextDark,
            lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 4 kotak statistik hasil perhitungan DemandCalculator
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DemandStatBox(AppIcons.Chart, RedBadge, "${item.demandScore}/100", "Demand Score", Modifier.weight(1f))
            DemandStatBox(AppIcons.Group, TextDark, item.activeSeekers.toString(), "Pencari Aktif", Modifier.weight(1f))
            DemandStatBox(AppIcons.Ticket, RedBadge, item.stockLabel, "Tiket Tersedia", Modifier.weight(1f))
            DemandStatBox(AppIcons.Trending, Color(0xFF1B5E20), "${item.sellChancePercent}%", "Peluang Laku", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        IconText(
            icon = AppIcons.Info,
            text = "Berdasarkan view, wishlist, dan tiket terjual",
            iconTint = TextGray,
            fontSize = 10.sp
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LineGray)

        Text(
            text = "Event dimulai dalam",
            fontSize = 13.sp,
            color = TextDark,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(6.dp))
        EventCountdown(event = event, modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(12.dp))
        GradientButton(
            text = "Jual Tiket Sekarang",
            icon = AppIcons.Ticket,
            onClick = onSellTicket,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onOpenEvent,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, BluePrimary),
            modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
            Text(text = "Lihat Detail Event", color = BluePrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun HighDemandBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(RedBadge)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = AppIcons.Trending, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "High Demand", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

// Satu kotak statistik: ikon, angka, label. Dipakai 4 kali.
@Composable
private fun DemandStatBox(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(BlueSurface)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (label == "Demand Score") {
            Row(
                modifier = Modifier.size(22.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    Modifier.size(4.dp, 9.dp)
                        .background(Color(0xFFF69A8B), RoundedCornerShape(1.5.dp))
                )
                Box(
                    Modifier.size(4.dp, 14.dp)
                        .background(Color(0xFFD43C28), RoundedCornerShape(1.5.dp))
                )
                Box(
                    Modifier.size(4.dp, 20.dp)
                        .background(Color(0xFF8C1D13), RoundedCornerShape(1.5.dp))
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary,
            maxLines = 1,
            modifier = Modifier.offset(y = 3.dp)
        )
        Text(
            text = label,
            fontSize = 8.sp,
            color = TextGray,
            maxLines = 1
        )
    }
}

// Jam mulai event dibaca dari teks waktu, misalnya "14.30 - 23.00" -> 14:30.
private fun eventStart(event: Event): LocalDateTime {
    val hour = event.time.take(2).toIntOrNull() ?: 0
    val minute = event.time.drop(3).take(2).toIntOrNull() ?: 0
    return event.date.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
}

// Hitung mundur hari : jam : menit, diperbarui tiap 30 detik.
@Composable
private fun EventCountdown(event: Event, modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = LocalDateTime.now()
        }
    }
    val totalMinutes = Duration.between(now, eventStart(event)).toMinutes().coerceAtLeast(0)
    val days = totalMinutes / (60 * 24)
    val hours = (totalMinutes / 60) % 24
    val minutes = totalMinutes % 60

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        CountdownBox(value = days, label = "hari")
        Text(text = ":", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BluePrimary, modifier = Modifier.padding(horizontal = 8.dp))
        CountdownBox(value = hours, label = "jam")
        Text(text = ":", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BluePrimary, modifier = Modifier.padding(horizontal = 8.dp))
        CountdownBox(value = minutes, label = "menit")
    }
}

@Composable
private fun CountdownBox(value: Long, label: String) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(BlueSurface)
            .height(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "%02d".format(value),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary,
            modifier = Modifier.offset(y = 3.dp)
        )
        Text(text = label, fontSize = 10.sp, color = TextGray)
    }
}

@Composable
private fun SellerMenuTile(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BlueSurface)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = BluePrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = BluePrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HotOpportunityListScreen(
    uiState: HotOpportunityUiState,
    onBack: () -> Unit,
    onToggleFilter: () -> Unit,
    onSellTicket: (Int) -> Unit,
    onOpenEvent: (Int) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.shadow(4.dp)) {
            ScreenTopBar(title = "Hot Opportunity", onBack = onBack)
        }
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PillChip(label = "High Demand", selected = uiState.highDemandOnly, onClick = { if (!uiState.highDemandOnly) onToggleFilter() })
            PillChip(label = "Semua Event", selected = !uiState.highDemandOnly, onClick = { if (uiState.highDemandOnly) onToggleFilter() })
        }

        val error = uiState.errorMessage
        when {
            uiState.isLoading -> LoadingBox()
            error != null -> ErrorBox(message = error, onRetry = onRetry)
            uiState.visibleList.isEmpty() -> EmptyBox(text = "Belum ada event dengan permintaan tinggi.")
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.visibleList) { item ->
                    HotOpportunityRow(
                        rank = uiState.visibleList.indexOf(item) + 1,
                        item = item,
                        onSellTicket = { onSellTicket(item.event.id) },
                        onOpenEvent = { onOpenEvent(item.event.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HotOpportunityRow(
    rank: Int,
    item: HotOpportunity,
    onSellTicket: () -> Unit,
    onOpenEvent: () -> Unit
) {
    val event = item.event
    WhiteCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenEvent)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (item.isHighDemand) BluePrimary else LineGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    color = if (item.isHighDemand) Color.White else TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            PosterBox(
                event = event,
                titleSize = 7.sp,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconText(icon = AppIcons.Calendar, text = "${formatDateShort(event.date)} · ${event.city}", fontSize = 9.sp)
                if (item.isHighDemand) {
                    Spacer(modifier = Modifier.height(3.dp))
                    HighDemandBadge()
                }
            }
            Text(
                text = item.demandScore.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isHighDemand) BluePrimary else TextGray
            )
        }

        // Batang skor demand
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(LineGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.demandScore / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (item.isHighDemand) {
                            Brush.horizontalGradient(listOf(GradientPink, GradientOrange))
                        } else {
                            Brush.horizontalGradient(listOf(TextGray, TextGray))
                        }
                    )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Pencari ${item.activeSeekers}  ·  Stok ${item.stockLabel}  ·  Peluang laku ${item.sellChancePercent}%",
                fontSize = 10.sp,
                color = TextGray,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onSellTicket,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier.height(30.dp)
            ) {
                Text(text = "Jual", fontSize = 11.sp)
            }
        }
    }
}
