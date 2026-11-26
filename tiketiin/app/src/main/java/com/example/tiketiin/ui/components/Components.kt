package com.example.tiketiin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tiketiin.Routes
import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.domain.formatDateShort
import com.example.tiketiin.domain.formatFlashLabel
import com.example.tiketiin.domain.formatRupiah
import com.example.tiketiin.ui.theme.*

// ---------------------------------------------------------------------------
// Logo
// ---------------------------------------------------------------------------

// Menambahkan tulisan "tiketiin" berwarna ke dalam teks (dipakai di judul).
fun AnnotatedString.Builder.appendLogo(baseColor: Color = BluePrimary) {
    withStyle(SpanStyle(color = baseColor)) { append("tiket") }
    withStyle(SpanStyle(color = LogoYellow)) { append("i") }
    withStyle(SpanStyle(color = LogoOrange)) { append("i") }
    withStyle(SpanStyle(color = LogoPink)) { append("n") }
}

@Composable
fun TiketiinLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 28.sp,
    baseColor: Color = BluePrimary
) {
    Text(
        text = buildAnnotatedString { appendLogo(baseColor) },
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

// ---------------------------------------------------------------------------
// Tombol
// ---------------------------------------------------------------------------

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = BluePrimary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Text(text = text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = BluePrimary,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, if (enabled) color else LineGray)
    ) {
        Text(
            text = text,
            color = if (enabled) color else TextGray,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

// Tombol gradasi pink-oranye ("Jual Tiket Sekarang")
@Composable
fun GradientButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(GradientPink, GradientOrange)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// Input
// ---------------------------------------------------------------------------

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    leading: (@Composable () -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val trailing: (@Composable () -> Unit)? = if (isPassword) {
        {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) AppIcons.EyeOff else AppIcons.Eye,
                    contentDescription = "Tampilkan kata sandi",
                    tint = TextGray
                )
            }
        }
    } else {
        null
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, color = TextGray, fontSize = 14.sp) },
        singleLine = true,
        readOnly = readOnly,
        shape = RoundedCornerShape(16.dp),
        leadingIcon = leading,
        trailingIcon = trailing,
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}

// Label + keterangan kecil + isi (biasanya AppTextField)
@Composable
fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 13.sp, color = TextDark)
        if (hint != null) {
            Text(text = hint, fontSize = 9.sp, color = TextGray, lineHeight = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

// ---------------------------------------------------------------------------
// Bagian atas dan bawah layar
// ---------------------------------------------------------------------------

@Composable
fun ScreenTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    containerColor: Color = Color.White
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 12.dp)
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(imageVector = AppIcons.Back, contentDescription = "Kembali", tint = TextDark)
            }
        }
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onSelect: (String) -> Unit
) {
    val items = listOf(
        NavItem(Routes.HOME, "Beranda", AppIcons.Home),
        NavItem(Routes.FOR_YOU, "Untukmu", AppIcons.Fire),
        NavItem(Routes.SELL, "Jual Tiket", AppIcons.Add),
        NavItem(Routes.ORDERS, "Pesanan", AppIcons.Ticket),
        NavItem(Routes.PROFILE, "Profil", AppIcons.Person)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp)
            .background(Color.White)
            .navigationBarsPadding()
            .height(68.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            val color = if (selected) BluePrimary else TextGray
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelect(item.route) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (item.route == Routes.SELL) {
                    // Tab Jual Tiket: kotak berisi tanda plus
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = color,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = item.label, fontSize = 11.sp, color = color)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Poster, tag, chip
// ---------------------------------------------------------------------------

/**
 * Poster event. Selama gambar asli belum ada, tampil gradasi warna + nama event.
 * Kalau event.posterRes diisi (R.drawable....), gambar itu yang ditampilkan.
 */

@Composable
fun PosterBox(
    event: Event,
    modifier: Modifier = Modifier,
    titleSize: TextUnit = 14.sp,
    showTitle: Boolean = true
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    Color(event.posterStart),
                    Color(event.posterEnd)
                )
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        when {
            !event.posterUrl.isNullOrBlank() -> {
                AsyncImage(
                    model = event.posterUrl,
                    contentDescription = "Poster ${event.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            event.posterRes != null -> {
                Image(
                    painter = painterResource(id = event.posterRes),
                    contentDescription = event.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            showTitle -> {
                Text(
                    text = event.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = titleSize,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}


@Composable
fun CategoryTag(category: EventCategory, modifier: Modifier = Modifier) {
    val (background, textColor) = when (category) {
        EventCategory.MUSIK -> BlueSoft to BluePrimary
        EventCategory.FILM -> YellowSoft to Color(0xFF7A5200)
        EventCategory.OLAHRAGA -> GreenSoft to Color(0xFF1B6B2E)
        EventCategory.SENI -> PinkSoft to Color(0xFFA3195B)
        EventCategory.SEMINAR -> BlueSurface to BluePrimary
        EventCategory.PAMERAN -> YellowSoft to Color(0xFF7A5200)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 22.dp, vertical = 1.dp)
    ) {
        Text(text = category.label, color = textColor, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PillChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) BluePrimary else LineGray)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Baris chip kategori: Semua, Musik, Olahraga, ...
@Composable
fun CategoryChips(
    selected: EventCategory?,
    onSelect: (EventCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        item {
            PillChip(label = "Semua", selected = selected == null, onClick = { onSelect(null) })
        }
        items(EventCategory.entries.toList()) { category ->
            PillChip(
                label = category.label,
                selected = selected == category,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
fun IconText(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconTint: Color = BluePrimary,
    textColor: Color = TextGray,
    fontSize: TextUnit = 10.sp,
    iconSize: Dp = 13.dp
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(iconSize))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FlashBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            .background(RedBadge)
            .padding(horizontal = 12.dp, vertical = 3.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ---------------------------------------------------------------------------
// Kartu event
// ---------------------------------------------------------------------------

// Kartu event bentuk kotak (Flash Ticket, For You, Event Populer)
@Composable
fun EventGridCard(
    summary: EventSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val event = summary.event
    val flashHours = event.flashEndsInHours
    Column(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
    ) {
        Box {
            PosterBox(
                event = event,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
            )
            if (flashHours != null) {
                FlashBadge(
                    text = formatFlashLabel(flashHours),
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = event.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconText(icon = AppIcons.Calendar, text = formatDateShort(event.date), fontSize = 9.sp)
                Spacer(modifier = Modifier.width(10.dp))
                IconText(icon = AppIcons.Ticket, text = "${summary.ticketCount} Ticket", fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatRupiah(summary.lowestPrice),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Kartu event bentuk baris (hasil Cari)
@Composable
fun EventListCard(
    summary: EventSummary,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val event = summary.event
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        PosterBox(
            event = event,
            titleSize = 10.sp,
            modifier = Modifier
                .size(width = 100.dp, height = 82.dp)
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
            IconText(icon = AppIcons.Location, text = "${event.venue}, ${event.city}", fontSize = 9.sp)
            Text(
                text = formatRupiah(summary.lowestPrice),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column(
            modifier = Modifier.height(82.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = if (summary.isSaved) AppIcons.Bookmark else AppIcons.BookmarkBorder,
                contentDescription = "Simpan event",
                tint = BluePrimary,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onToggleSave)
            )
            IconText(icon = AppIcons.Ticket, text = summary.ticketCount.toString(), fontSize = 9.sp)
        }
    }
}

// Ringkasan event kecil di atas form pesanan / pembayaran
@Composable
fun OrderEventHeader(
    event: Event,
    seatLabel: String,
    priceLabel: String,
    quantity: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp)
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        PosterBox(
            event = event,
            titleSize = 9.sp,
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            IconText(icon = AppIcons.Calendar, text = formatDateShort(event.date))
            IconText(icon = AppIcons.Location, text = "${event.venue}, ${event.city}")
            IconText(icon = AppIcons.Seat, text = seatLabel)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = priceLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconText(icon = AppIcons.Ticket, text = quantity.toString())
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Lain-lain
// ---------------------------------------------------------------------------

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                fontSize = 13.sp,
                color = BluePrimary,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

// Pita informasi biru muda (hitung mundur pesanan, pesan sukses, dll.)
@Composable
fun InfoBanner(text: AnnotatedString, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BlueSoft)
            .padding(horizontal = 24.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = BluePrimary,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )
    }
}

// Kotak kecil berisi angka + label (statistik)
@Composable
fun MiniStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    background: Color = BlueSurface,
    valueColor: Color = BluePrimary
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1
        )
        Text(text = label, fontSize = 10.sp, color = valueColor, textAlign = TextAlign.Center)
    }
}

// Langkah 1-2-3-4 pada alur jual tiket
@Composable
fun SellStepper(currentStep: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        for (step in 1..4) {
            val active = step <= currentStep
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (active) BluePrimary else LineGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.toString(),
                    color = if (active) Color.White else TextGray,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (step < 4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (step < currentStep) BluePrimary else LineGray)
                )
            }
        }
    }
}

/**
 * Gambar kode QR tiruan (pola acak dari teks seed) untuk tampilan tiket.
 * Bukan QR asli; nanti bisa diganti library pembuat QR.
 */
@Composable
fun QrCodeBox(seed: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.5.dp, BlueSoft, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cells = 21
            val cell = size.minDimension / cells
            val random = java.util.Random(seed.hashCode().toLong())
            for (row in 0 until cells) {
                for (col in 0 until cells) {
                    val top = row < 7
                    val bottom = row >= cells - 7
                    val left = col < 7
                    val right = col >= cells - 7
                    val inFinder = (top && left) || (top && right) || (bottom && left)
                    val randomFill = random.nextBoolean()
                    val filled = if (inFinder) {
                        val r = if (bottom) row - (cells - 7) else row
                        val c = if (right) col - (cells - 7) else col
                        r == 0 || r == 6 || c == 0 || c == 6 || (r in 2..4 && c in 2..4)
                    } else {
                        randomFill
                    }
                    if (filled) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(col * cell, row * cell),
                            size = Size(cell, cell)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BluePrimary)
    }
}

@Composable
fun EmptyBox(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(text = text, color = TextGray, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun ErrorBox(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = AppIcons.Warning, contentDescription = null, tint = RedDark)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, color = TextGray, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        OutlineButton(text = "Coba lagi", onClick = onRetry)
    }
}

// Baris "label ......... nilai" pada rincian pembayaran
@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false
) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = label,
            fontSize = if (bold) 13.sp else 11.sp,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            color = if (bold) TextDark else TextGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = if (bold) 13.sp else 11.sp,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            color = if (bold) TextDark else TextGray
        )
    }
}

// Kartu putih bersudut bulat dengan bayangan
@Composable
fun WhiteCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp),
        content = content
    )
}
