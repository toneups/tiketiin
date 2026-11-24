
package com.example.tiketiin.ui.foryou

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.ui.components.*
import com.example.tiketiin.ui.theme.*

@Composable
fun ForYouScreen(
    uiState: ForYouUiState,
    onSelectTab: (Int) -> Unit,
    onSelectCategory: (EventCategory?) -> Unit,
    onSearch: () -> Unit,
    onOpenSaved: () -> Unit,
    onOpenEvent: (Int) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp)
                .background(Color.White)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier.padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 12.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "For ${uiState.userName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                RoundIconButton(
                    icon = AppIcons.Search,
                    onClick = onSearch
                )

                Spacer(modifier = Modifier.width(8.dp))

                RoundIconButton(
                    icon = AppIcons.Cart,
                    onClick = onOpenSaved
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                ForYouTab(
                    label = "Rekomendasi Untukmu",
                    selected = uiState.selectedTab == 0,
                    onClick = { onSelectTab(0) },
                    modifier = Modifier.weight(1f)
                )

                ForYouTab(
                    label = "Di Sekitarmu",
                    selected = uiState.selectedTab == 1,
                    onClick = { onSelectTab(1) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        CategoryChips(
            selected = uiState.selectedCategory,
            onSelect = onSelectCategory
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (uiState.isShowingCache) {
            Text(
                text = "Menampilkan data event terakhir. Harga dan stok mungkin berubah.",
                color = Color(0xFF856404),
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 8.dp
                    )
            )
        }

        val error = uiState.errorMessage

        when {
            uiState.isLoading -> LoadingBox()

            error != null -> ErrorBox(
                message = error,
                onRetry = onRetry
            )

            uiState.visibleEvents.isEmpty() -> EmptyBox(
                text = if (uiState.selectedTab == 1) {
                    "Belum ada event di sekitarmu."
                } else {
                    "Belum ada rekomendasi untuk kategori ini."
                }
            )

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    horizontal = 24.dp,
                    vertical = 8.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.visibleEvents) { summary ->
                    EventGridCard(
                        summary = summary,
                        onClick = {
                            onOpenEvent(summary.event.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ForYouTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (selected) BluePrimary else TextGray,
            fontWeight = if (selected) {
                FontWeight.Medium
            } else {
                FontWeight.Normal
            },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    if (selected) BluePrimary else Color.Transparent
                )
        )
    }
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(ScreenGray)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextDark,
            modifier = Modifier.size(20.dp)
        )
    }
}
