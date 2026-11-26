package com.example.tiketiin.ui.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications

private data class HomeCategory(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val category: EventCategory?
)

private val homeCategories = listOf(
    HomeCategory("Konser", AppIcons.Music, Color(0xFF7B5CF5), EventCategory.MUSIK),
    HomeCategory("Pameran", AppIcons.Exhibition, Color(0xFFE5395B), EventCategory.PAMERAN),
    HomeCategory("Olahraga", AppIcons.Sport, Color(0xFF2E9E48), EventCategory.OLAHRAGA),
    HomeCategory("Seminar", AppIcons.Seminar, Color(0xFF2B45B4), EventCategory.SEMINAR),
    HomeCategory("Seni", AppIcons.Art, Color(0xFFF59E0B), EventCategory.SENI),
    HomeCategory("Film", AppIcons.Film, Color(0xFF0EA5E9), EventCategory.FILM),
    HomeCategory("Tempat Rekreasi", AppIcons.Event, Color(0xFF14B8A6), null),
    HomeCategory("Akomodasi", AppIcons.Hotel, Color(0xFF6366F1), null)
)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSearch: (String) -> Unit,
    onOpenSaved: () -> Unit,
    onOpenNotifications: () -> Unit,
    unreadNotificationCount: Int?,
    onOpenEvent: (Int) -> Unit,
    onSelectCategory: (EventCategory?) -> Unit,
    onRetry: () -> Unit,
    onToggleSaved: (Int) -> Unit,
    onRetryWishlist: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Banner event teratas + kolom cari di atasnya
        Box(modifier = Modifier.fillMaxWidth().height(270.dp)) {
            val banner = uiState.banner
            if (banner != null) {
                PosterBox(
                    event = banner.event,
                    titleSize = 26.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onOpenEvent(banner.event.id) }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(BluePrimary))
            }
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .clickable { onSearch("all") }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = AppIcons.Search, contentDescription = null, tint = TextDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Cari event", fontSize = 13.sp, color = TextDark)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onOpenSaved),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppIcons.Cart,
                        contentDescription = "Event tersimpan",
                        tint = TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))


                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(onClick = onOpenNotifications),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Buka notifikasi",
                            tint = TextDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (
                        unreadNotificationCount != null &&
                        unreadNotificationCount > 0
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    color = Color(0xFFE5395B),
                                    shape = CircleShape
                                )
                                .defaultMinSize(
                                    minWidth = 18.dp,
                                    minHeight = 18.dp
                                )
                                .padding(horizontal = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadNotificationCount > 9) {
                                    "9+"
                                } else {
                                    unreadNotificationCount.toString()
                                },
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

            }
        }

        // Lembar putih yang menimpa bagian bawah banner
        Column(
            modifier = Modifier
                .offset(y = (-28).dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .padding(top = 18.dp)
        ) {
            SectionHeader(title = "Kategori Event", modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(10.dp))
            for (rowItems in homeCategories.chunked(4)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    rowItems.forEach { item ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSearch(item.category?.name ?: "all") }
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(item.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = item.icon, contentDescription = null, tint = item.color)
                            }
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                color = TextGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (uiState.isShowingCache) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF4D6))
                        .padding(12.dp),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = if (uiState.isRefreshing) {
                            "Memperbarui data event..."
                        } else {
                            "Menampilkan data terakhir. Harga dan stok mungkin berubah."
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF856404),
                        modifier = Modifier.weight(1f)
                    )

                    if (!uiState.isRefreshing) {
                        Text(
                            text = "Refresh",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BluePrimary,
                            modifier = Modifier.clickable {
                                onRetry()
                            }
                        )
                    }
                }
            }

            val error = uiState.errorMessage
            if (uiState.isLoading) {
                LoadingBox()
            } else if (error != null) {
                ErrorBox(message = error, onRetry = onRetry)
            } else {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(
                    title = "Flash Ticket",
                    actionLabel = "Lihat semua",
                    onAction = { onSearch("flash") },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(
                        items = uiState.flashTickets,
                        key = { it.event.id }
                    ) { summary ->

                        val eventId = summary.event.id

                        Box(
                            modifier = Modifier.width(150.dp)
                        ) {
                            EventGridCard(
                                summary = summary,
                                onClick = {
                                    onOpenEvent(eventId)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            IconButton(
                                onClick = {
                                    onToggleSaved(eventId)
                                },
                                enabled = uiState.isWishlistLoaded &&
                                        !uiState.isWishlistLoading &&
                                        eventId !in uiState.wishlistBusyIds,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(36.dp)
                                    .background(
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                            ) {
                                val isSaved =
                                    eventId in uiState.savedEventIds

                                Icon(
                                    imageVector = if (isSaved) {
                                        AppIcons.Bookmark
                                    } else {
                                        AppIcons.BookmarkBorder
                                    },
                                    contentDescription = if (isSaved) {
                                        "Hapus event tersimpan"
                                    } else {
                                        "Simpan event"
                                    },
                                    tint = if (isSaved) {
                                        BluePrimary
                                    } else {
                                        TextGray
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Event Populer",
                    actionLabel = "Lihat semua",
                    onAction = { onSearch("popular") },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategoryChips(selected = uiState.selectedCategory, onSelect = onSelectCategory)
                Spacer(modifier = Modifier.height(8.dp))

                uiState.wishlistError?.let { message ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message,
                            color = RedDark,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = onRetryWishlist,
                            enabled = !uiState.isWishlistLoading
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }

                if (uiState.filteredPopular.isEmpty()) {
                    EmptyBox(text = "Belum ada event di kategori ini.")
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        items(
                            items = uiState.filteredPopular,
                            key = { it.event.id }
                        ) { summary ->

                            val eventId = summary.event.id

                            PopularCard(
                                summary = summary,

                                onClick = {
                                    onOpenEvent(eventId)
                                },

                                isSaved = eventId in uiState.savedEventIds,

                                bookmarkEnabled =
                                    uiState.isWishlistLoaded &&
                                            !uiState.isWishlistLoading &&
                                            eventId !in uiState.wishlistBusyIds,

                                onToggleSaved = {
                                    onToggleSaved(eventId)
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}

// Kartu lebar pada bagian Event Populer
@Composable
private fun PopularCard(
    summary: EventSummary,
    onClick: () -> Unit,
    isSaved: Boolean,
    bookmarkEnabled: Boolean,
    onToggleSaved: () -> Unit
) {
    val event = summary.event
    Column(
        modifier = Modifier
            .width(250.dp)
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
    ) {
        PosterBox(event = event, titleSize = 18.sp, modifier = Modifier.fillMaxWidth().height(120.dp))
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onToggleSaved,
                    enabled = bookmarkEnabled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) {
                            AppIcons.Bookmark
                        } else {
                            AppIcons.BookmarkBorder
                        },
                        contentDescription = if (isSaved) {
                            "Hapus event tersimpan"
                        } else {
                            "Simpan event"
                        },
                        tint = if (isSaved) {
                            BluePrimary
                        } else {
                            TextGray
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row {
                IconText(icon = AppIcons.Calendar, text = formatDateShort(event.date), fontSize = 9.sp)
                Spacer(modifier = Modifier.width(10.dp))
                IconText(icon = AppIcons.Ticket, text = "${summary.ticketCount} Ticket", fontSize = 9.sp)
            }
            IconText(icon = AppIcons.Location, text = event.city, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = formatRupiah(summary.lowestPrice), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
