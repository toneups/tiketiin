package com.example.tiketiin.ui.saved

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
fun SavedScreen(
    uiState: SavedUiState,
    onBack: () -> Unit,
    onSelectCategory: (EventCategory?) -> Unit,
    onToggleSelected: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onOpenEvent: (Int) -> Unit,
    onCheckout: (Int) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.shadow(4.dp)) {
            ScreenTopBar(title = "Event Tersimpan", onBack = onBack)
        }
        Spacer(modifier = Modifier.height(10.dp))
        CategoryChips(selected = uiState.selectedCategory, onSelect = onSelectCategory)

        uiState.errorMessage?.let { message ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 8.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    color = RedDark,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onRetry,
                    enabled = !uiState.isLoading &&
                            uiState.removingEventId == null
                ) {
                    Text("Coba Lagi")
                }
            }
        }


        Box(modifier = Modifier.weight(1f)) {

            when {
                uiState.isLoading -> LoadingBox()

                uiState.errorMessage != null &&
                        uiState.items.isEmpty() -> {
                }

                uiState.visibleItems.isEmpty() -> {
                    EmptyBox(
                        text = "Belum ada event yang kamu simpan."
                    )
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = 24.dp,
                        vertical = 10.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.visibleItems,
                        key = { it.event.id }
                    ) { summary ->
                        SavedCard(
                            summary = summary,
                            checked =
                                uiState.selectedEventId == summary.event.id,
                            onCheck = {
                                if (uiState.removingEventId == null) {
                                    onToggleSelected(summary.event.id)
                                }
                            },
                            onRemove = {
                                if (uiState.removingEventId == null) {
                                    onRemove(summary.event.id)
                                }
                            },
                            onClick = {
                                onOpenEvent(summary.event.id)
                            }
                        )
                    }
                }
            }

        }

        // Bilah bawah: total + Checkout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp)
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.selectedEventId == null) "Pilih satu event" else "1 event dipilih",
                fontSize = 11.sp,
                color = TextGray,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Total", fontSize = 9.sp, color = TextGray)
                Text(text = formatRupiah(uiState.total), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { uiState.selectedEventId?.let(onCheckout) },
                enabled = uiState.selectedEventId != null &&
                        !uiState.isLoading &&
                        uiState.removingEventId == null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(text = "Checkout", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SavedCard(
    summary: EventSummary,
    checked: Boolean,
    onCheck: () -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    val event = summary.event
    val flashHours = event.flashEndsInHours
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, LineGray, RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = checked, onCheckedChange = { onCheck() })
            PosterBox(
                event = event,
                titleSize = 9.sp,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(10.dp))
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
                CategoryTag(category = event.category)
                Spacer(modifier = Modifier.height(3.dp))
                IconText(icon = AppIcons.Calendar, text = formatDateShort(event.date), fontSize = 9.sp)
                IconText(icon = AppIcons.Location, text = event.city, fontSize = 9.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatRupiah(summary.lowestPrice),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconText(icon = AppIcons.Ticket, text = summary.ticketCount.toString(), fontSize = 9.sp)
                }
            }
            Icon(
                imageVector = AppIcons.Bookmark,
                contentDescription = "Hapus dari tersimpan",
                tint = BluePrimary,
                modifier = Modifier
                    .align(Alignment.Top)
                    .size(22.dp)
                    .clickable(onClick = onRemove)
            )
        }
        if (flashHours != null) {
            Box(
                modifier = Modifier.fillMaxWidth().background(RedSoft).padding(vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatClosingLabel(flashHours),
                    color = RedDark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
