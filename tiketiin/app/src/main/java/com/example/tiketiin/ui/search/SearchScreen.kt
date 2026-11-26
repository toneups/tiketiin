package com.example.tiketiin.ui.search

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
import androidx.compose.foundation.text.BasicTextField

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSelectCategory: (EventCategory?) -> Unit,
    onToggleSort: () -> Unit,
    onToggleSaved: (Int) -> Unit,
    onOpenEvent: (Int) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp)
                .background(Color.White)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = AppIcons.Back, contentDescription = "Kembali")
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ScreenGray)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = AppIcons.Search, contentDescription = null, tint = TextDark)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (uiState.query.isEmpty()) {
                        Text(text = "Cari event, kota, atau kategori", fontSize = 13.sp, color = TextGray)
                    }
                    BasicTextField(
                        value = uiState.query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextDark, fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Tombol urutkan: tanggal terdekat <-> harga terendah
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (uiState.sortByCheapest) BluePrimary else ScreenGray)
                    .clickable(onClick = onToggleSort),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Filter,
                    contentDescription = "Urutkan",
                    tint = if (uiState.sortByCheapest) Color.White else TextDark
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        val sectionTitle = when (uiState.mode) {
            "flash" -> "Semua Flash Ticket"
            "popular" -> "Event Populer"
            else -> null
        }

        if (sectionTitle != null) {
            Text(
                text = sectionTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(
                    horizontal = 24.dp,
                    vertical = 12.dp
                )
            )
        }
        CategoryChips(selected = uiState.category, onSelect = onSelectCategory)
        Text(
            text = when {
                uiState.sortByCheapest ->
                    "Urut: harga terendah"

                uiState.mode == "flash" ->
                    "Urut: promo segera berakhir"

                uiState.mode == "popular" ->
                    "Urut: paling populer"

                else ->
                    "Urut: tanggal terdekat"
            },
            fontSize = 10.sp,
            color = TextGray,
            modifier = Modifier.padding(
                horizontal = 24.dp,
                vertical = 4.dp
            )
        )


        val error = uiState.errorMessage

        if (uiState.bookmarkError != null) {
            Text(
                text = uiState.bookmarkError,
                color = Color(0xFFB42318),
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    horizontal = 24.dp,
                    vertical = 8.dp
                )
            )
        }

        when {
            uiState.isLoading -> LoadingBox()

            error != null -> ErrorBox(
                message = error,
                onRetry = onRetry
            )

            !uiState.hasSearched -> EmptyBox(
                text = "Cari event berdasarkan nama, kota, atau kategori."
            )

            uiState.results.isEmpty() -> EmptyBox(
                text = "Event tidak ditemukan.\nCoba kata kunci atau kategori lain."
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = 24.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.results) { summary ->
                    EventListCard(
                        summary = summary,
                        onClick = {
                            onOpenEvent(summary.event.id)
                        },
                        onToggleSave = {
                            onToggleSaved(summary.event.id)
                        }
                    )
                }
            }
        }
    }
}
