package com.example.tiketiin.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiketiin.R
import com.example.tiketiin.data.model.*
import com.example.tiketiin.domain.*
import com.example.tiketiin.ui.components.*
import com.example.tiketiin.ui.theme.*

private data class HomeCategory(
    val label: String,
    val iconRes: Int,
    val category: EventCategory?
)

private val homeCategories = listOf(
    HomeCategory("Konser", R.drawable.konser, EventCategory.MUSIK),
    HomeCategory("Pameran", R.drawable.pameran, EventCategory.PAMERAN),
    HomeCategory("Olahraga", R.drawable.olahraga, EventCategory.OLAHRAGA),
    HomeCategory("Seminar", R.drawable.seminar, EventCategory.SEMINAR),
    HomeCategory("Seni", R.drawable.seni, EventCategory.SENI),
    HomeCategory("Film", R.drawable.film, EventCategory.FILM),
    HomeCategory("Tempat Rekreasi", R.drawable.rekreasi, null),
    HomeCategory("Akomodasi", R.drawable.akomodasi, null)
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
    onOpenFlashTickets: () -> Unit,
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
            val banners = uiState.popularEvents
                .filter { it.event.posterRes != null || !it.event.posterUrl.isNullOrBlank() }
                .take(3)
                .ifEmpty { listOfNotNull(uiState.banner) }

            if (banners.isNotEmpty()) {
                val pagerState = rememberPagerState { banners.size }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { page -> banners[page].event.id }
                ) { page ->
                    val event = banners[page].event

                    PosterBox(
                        event = event,
                        titleSize = 26.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onOpenEvent(event.id) }
                    )
                }

                if (banners.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 40.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(banners.size) { page ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (pagerState.currentPage == page) {
                                            Color.White
                                        } else {
                                            Color.White.copy(alpha = 0.5f)
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
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
                        .clickable { onSearch("flash") }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = AppIcons.Search, contentDescription = null, tint = TextDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Cari", fontSize = 13.sp, color = TextDark)
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
                            Image(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
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
                    onAction = onOpenFlashTickets,
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
                    onAction = { onSearch(uiState.selectedCategory?.name ?: "all") },
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