package com.example.tiketiin.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tiketiin.ui.components.EmptyBox
import com.example.tiketiin.ui.components.ErrorBox
import com.example.tiketiin.ui.components.EventGridCard
import com.example.tiketiin.ui.components.LoadingBox
import com.example.tiketiin.ui.components.ScreenTopBar
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight

@Composable
fun FlashTicketsScreen(
    uiState: HomeUiState,
    onBack: () -> Unit,
    onOpenEvent: (Int) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ScreenTopBar(
            title = "Flash Ticket",
            onBack = onBack,
            titleFontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.10f), Color.Transparent)
                    )
                )
        )
        val error = uiState.errorMessage
        when {
            uiState.isLoading -> LoadingBox()
            error != null -> ErrorBox(message = error, onRetry = onRetry)
            uiState.flashTickets.isEmpty() ->
                EmptyBox(text = "Belum ada Flash Ticket aktif.")

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.flashTickets, key = { it.event.id }) { summary ->
                    EventGridCard(
                        summary = summary,
                        onClick = { onOpenEvent(summary.event.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}