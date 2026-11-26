
package com.example.tiketiin.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiketiin.data.remote.dto.NotificationDto
import com.example.tiketiin.ui.components.AppIcons
import com.example.tiketiin.ui.theme.BluePrimary
import com.example.tiketiin.ui.theme.TextDark
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PageBackground = Color(0xFFF7F8FC)
private val SecondaryText = Color(0xFF7C8090)

@Composable
fun NotificationScreen(
    uiState: NotificationUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenEvent: (Int) -> Unit,
    onMarkAsRead: (Int) -> Unit
) {
    var unreadOnly by rememberSaveable {
        mutableStateOf(false)
    }

    val visibleNotifications = remember(
        uiState.notifications,
        unreadOnly
    ) {
        if (unreadOnly) {
            uiState.notifications.filter {
                !it.isRead
            }
        } else {
            uiState.notifications
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 12.dp,
                        bottom = 18.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(
                        text = "←",
                        fontSize = 25.sp,
                        color = TextDark
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Notifikasi",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Text(
                        text = "Informasi terbaru akunmu",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            BluePrimary.copy(alpha = 0.10f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppIcons.Event,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(
                    bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(
                    10.dp
                )
            ) {
                NotificationTab(
                    text = "Semua",
                    selected = !unreadOnly,
                    onClick = {
                        unreadOnly = false
                    }
                )

                NotificationTab(
                    text = "Belum Dibaca (${uiState.unreadCount})",
                    selected = unreadOnly,
                    onClick = {
                        unreadOnly = true
                    }
                )
            }
        }

        uiState.actionErrorMessage?.let { message ->
            Surface(
                color = Color(0xFFFFF0F0),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(14.dp),
                    color = Color(0xFFB42318),
                    fontSize = 13.sp
                )
            }
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = BluePrimary
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Memuat notifikasi...",
                            color = SecondaryText
                        )
                    }
                }
            }

            uiState.errorMessage != null -> {
                NotificationMessageState(
                    title = "Notifikasi belum bisa dimuat",
                    description = uiState.errorMessage,
                    actionText = "Coba Lagi",
                    onAction = onRetry,
                    modifier = Modifier.weight(1f)
                )
            }

            visibleNotifications.isEmpty() -> {
                NotificationMessageState(
                    title = if (unreadOnly) {
                        "Semua sudah dibaca"
                    } else {
                        "Belum ada notifikasi"
                    },
                    description = if (unreadOnly) {
                        "Tidak ada notifikasi yang belum dibaca."
                    } else {
                        "Informasi event, tiket, dan pesananmu " +
                                "akan muncul di sini."
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                        12.dp
                    )
                ) {
                    items(
                        items = visibleNotifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                if (!notification.isRead) {
                                    onMarkAsRead(notification.id)
                                }

                                notification.eventId?.let { eventId ->
                                    onOpenEvent(eventId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) {
                    BluePrimary
                } else {
                    Color(0xFFF0F1F5)
                }
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 17.dp,
                vertical = 10.dp
            )
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) {
                Color.White
            } else {
                SecondaryText
            }
        )
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationDto,
    onClick: () -> Unit
) {
    val (icon, iconColor, iconBackground) =
        notificationAppearance(notification.type)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (notification.isRead) {
            Color.White
        } else {
            Color(0xFFF0F3FF)
        },
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!notification.isRead) {
                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    BluePrimary,
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(7.dp)
                )

                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = Color(0xFF565B69),
                    lineHeight = 18.sp
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notificationTypeLabel(
                            notification.type
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = iconColor,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = formatNotificationDate(
                            notification.createdAt
                        ),
                        fontSize = 10.sp,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}

private data class NotificationAppearance(
    val icon: ImageVector,
    val foreground: Color,
    val background: Color
)

private fun notificationAppearance(
    type: String
): NotificationAppearance = when (type) {
    "RECOMMENDATION", "HOT_OPPORTUNITY" ->
        NotificationAppearance(
            AppIcons.Event,
            Color(0xFF3046B8),
            Color(0xFFE1E7FF)
        )

    "ORDER", "PAYMENT", "ESCROW" ->
        NotificationAppearance(
            AppIcons.Ticket,
            Color(0xFF14835C),
            Color(0xFFDFF5EA)
        )

    "EVENT_REMINDER" ->
        NotificationAppearance(
            AppIcons.Calendar,
            Color(0xFFB8740C),
            Color(0xFFFFF1D7)
        )

    "PRICE_ALERT" ->
        NotificationAppearance(
            AppIcons.Cart,
            Color(0xFFD04C70),
            Color(0xFFFFE5ED)
        )

    else ->
        NotificationAppearance(
            AppIcons.Event,
            Color(0xFF6366F1),
            Color(0xFFEAE9FF)
        )
}

private fun notificationTypeLabel(
    type: String
): String = when (type) {
    "RECOMMENDATION" -> "Rekomendasi Untukmu"
    "HOT_OPPORTUNITY" -> "Peluang Tiket"
    "PRICE_ALERT" -> "Perubahan Harga"
    "EVENT_REMINDER" -> "Pengingat Event"
    "ORDER" -> "Pesanan"
    "PAYMENT" -> "Pembayaran"
    "ESCROW" -> "Escrow"
    "KYC" -> "Verifikasi Akun"
    else -> "Informasi"
}

private fun formatNotificationDate(
    value: String
): String {
    return runCatching {
        val date = OffsetDateTime.parse(value)
            .atZoneSameInstant(
                ZoneId.systemDefault()
            )

        val formatter = DateTimeFormatter.ofPattern(
            "dd MMM yyyy · HH:mm",
            Locale("id", "ID")
        )

        date.format(formatter)
    }.getOrDefault("")
}

@Composable
private fun NotificationMessageState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8EDFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Event,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = description,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = SecondaryText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (actionText != null && onAction != null) {
                Spacer(
                    modifier = Modifier.height(22.dp)
                )

                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = actionText,
                        modifier = Modifier.padding(
                            horizontal = 15.dp,
                            vertical = 4.dp
                        )
                    )
                }
            }
        }
    }
}
