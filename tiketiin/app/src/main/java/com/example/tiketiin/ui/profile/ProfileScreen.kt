
package com.example.tiketiin.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tiketiin.data.remote.dto.CityOptionDto
import com.example.tiketiin.ui.components.*
import com.example.tiketiin.ui.theme.*

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onOpenSaved: () -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit,
    onSaveProfile: (String, String) -> Unit,
    onResetSaveStatus: () -> Unit,
    onLoadCityOptions: () -> Unit,
    onRetryLoadCityOptions: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    onResetPasswordStatus: () -> Unit,
    onUpdateRecommendationNotifications: (Boolean) -> Unit,
    onResetNotificationPreferenceError: () -> Unit
) {
    val user = uiState.user
    val context = LocalContext.current

    var showLogout by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showAccountSettings by remember { mutableStateOf(false) }
    var showNotificationPreferences by remember { mutableStateOf(false) }
    var showPrivacySecurity by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showHelpCenter by remember { mutableStateOf(false) }
    var showReferral by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(modifier = Modifier.shadow(4.dp)) {
            ScreenTopBar(title = "Profil")
        }

        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = BluePrimary
            )
        }

        uiState.errorMessage?.let { message ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    color = RedDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRetry,
                    enabled = !uiState.isLoading
                ) {
                    Text("Coba Lagi")
                }
            }
        }

        if (user != null) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(BluePrimary, GradientPink)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1),
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = AppIcons.Edit,
                        contentDescription = "Edit profil",
                        tint = BluePrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                onResetSaveStatus()
                                onLoadCityOptions()
                                showEditProfile = true
                            }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(TextGray, LineGray)
                            )
                        )
                        .padding(
                            horizontal = 26.dp,
                            vertical = 2.dp
                        )
                ) {
                    Text(
                        text = user.level,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ScreenGray)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Informasi Akun",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Email",
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    Text(
                        text = user.email.ifBlank {
                            "Belum tersedia"
                        },
                        fontSize = 14.sp,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = LineGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Nomor HP",
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    Text(
                        text = user.phone.ifBlank {
                            "Belum ditambahkan"
                        },
                        fontSize = 14.sp,
                        color = TextDark
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStat(
                        value = "—",
                        label = "Diskon belum tersedia",
                        background = YellowSoft,
                        valueColor = Color(0xFF4A3500),
                        modifier = Modifier.weight(1f)
                    )

                    MiniStat(
                        value = "—",
                        label = "Total hemat",
                        background = BlueSoft,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Ringkasan Aktivitas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BlueSoft)
                ) {
                    MiniStat(
                        "—",
                        "Pembelian",
                        Modifier.weight(1f),
                        Color.Transparent
                    )

                    MiniStat(
                        "—",
                        "Tiket dibeli",
                        Modifier.weight(1f),
                        Color.Transparent
                    )

                    MiniStat(
                        "—",
                        "Pengeluaran",
                        Modifier.weight(1.3f),
                        Color.Transparent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Statistik transaksi belum tersedia.",
                    fontSize = 11.sp,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ScreenGray)
                ) {
                    ProfileMenuRow(
                        icon = AppIcons.BookmarkBorder,
                        label = "Event Tersimpan",
                        onClick = onOpenSaved
                    )

                    HorizontalDivider(color = LineGray)

                    ProfileMenuRow(
                        icon = AppIcons.Shield,
                        label = "Kode Referral",
                        trailingText = user.referralCode.takeIf {
                            it.isNotBlank()
                        },
                        onClick = {
                            showReferral = true
                        }
                    )

                    HorizontalDivider(color = LineGray)

                    ProfileMenuRow(
                        icon = AppIcons.Info,
                        label = "Pusat Bantuan",
                        onClick = {
                            showHelpCenter = true
                        }
                    )

                    HorizontalDivider(color = LineGray)

                    ProfileMenuRow(
                        icon = AppIcons.Settings,
                        label = "Pengaturan akun",
                        onClick = {
                            onResetNotificationPreferenceError()
                            showAccountSettings = true
                        }
                    )

                    HorizontalDivider(color = LineGray)

                    ProfileMenuRow(
                        icon = AppIcons.Logout,
                        label = "Keluar",
                        tint = RedDark,
                        onClick = {
                            showLogout = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showReferral) {
        val referralCode = user?.referralCode.orEmpty().trim()

        AlertDialog(
            onDismissRequest = {
                showReferral = false
            },
            title = {
                Text(
                    text = "Kode Referral",
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Bagikan kode referral Tiketiin kepada temanmu.",
                        fontSize = 13.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BlueSoft)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = referralCode.ifBlank {
                                "Kode referral belum tersedia"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(
                                Context.CLIPBOARD_SERVICE
                            ) as ClipboardManager

                            clipboard.setPrimaryClip(
                                ClipData.newPlainText(
                                    "Kode Referral Tiketiin",
                                    referralCode
                                )
                            )

                            Toast.makeText(
                                context,
                                "Kode referral berhasil disalin",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        enabled = referralCode.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary
                        )
                    ) {
                        Text("Salin Kode")
                    }

                    OutlinedButton(
                        onClick = {
                            val shareText =
                                "Yuk bergabung di Tiketiin! " +
                                        "Gunakan kode referral saya: $referralCode"

                            val intent = Intent(
                                Intent.ACTION_SEND
                            ).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    shareText
                                )
                            }

                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    "Bagikan kode referral"
                                )
                            )
                        },
                        enabled = referralCode.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Bagikan Kode",
                            color = BluePrimary
                        )
                    }

                    Text(
                        text = "Bagikan kode ini kepada temanmu. " +
                                "Kode referral dapat dimasukkan saat " +
                                "pendaftaran akun Tiketiin.",
                        fontSize = 11.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReferral = false
                    }
                ) {
                    Text(
                        text = "Tutup",
                        color = BluePrimary
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showHelpCenter) {
        AlertDialog(
            onDismissRequest = {
                showHelpCenter = false
            },
            title = {
                Text(
                    text = "Pusat Bantuan",
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Pertanyaan yang Sering Diajukan",
                        fontWeight = FontWeight.SemiBold,
                        color = BluePrimary
                    )

                    Text(
                        text = "Bagaimana cara menyimpan event?",
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Tekan ikon bookmark pada event. " +
                                "Event akan masuk ke halaman Event Tersimpan.",
                        fontSize = 13.sp,
                        color = TextGray
                    )

                    HorizontalDivider(color = LineGray)

                    Text(
                        text = "Bagaimana cara menemukan event?",
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Gunakan fitur pencarian dan filter kategori " +
                                "untuk menemukan event yang diinginkan.",
                        fontSize = 13.sp,
                        color = TextGray
                    )

                    HorizontalDivider(color = LineGray)

                    Text(
                        text = "Bagaimana cara mengubah profil?",
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Buka halaman Profil, lalu tekan ikon edit " +
                                "di samping nama untuk mengubah informasi profil.",
                        fontSize = 13.sp,
                        color = TextGray
                    )

                    HorizontalDivider(color = LineGray)

                    Text(
                        text = "Mengapa data tidak muncul?",
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Periksa koneksi internet dan tekan Coba Lagi. " +
                                "Beberapa fitur membutuhkan koneksi ke server.",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showHelpCenter = false
                    }
                ) {
                    Text(
                        text = "Tutup",
                        color = BluePrimary
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showAccountSettings && user != null) {
        Dialog(
            onDismissRequest = {
                showAccountSettings = false
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.Settings,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Pengaturan Akun",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = LineGray)

                AccountSettingsMenuItem(
                    icon = AppIcons.Lock,
                    title = "Ubah Kata Sandi",
                    description = "Perbarui keamanan akun",
                    onClick = {
                        showAccountSettings = false
                        onResetPasswordStatus()
                        showChangePassword = true
                    }
                )

                HorizontalDivider(color = LineGray)

                AccountSettingsMenuItem(
                    icon = Icons.Filled.Notifications,
                    title = "Preferensi Notifikasi",
                    description = "Atur notifikasi rekomendasi event",
                    onClick = {
                        showAccountSettings = false
                        onResetNotificationPreferenceError()
                        showNotificationPreferences = true
                    }
                )

                HorizontalDivider(color = LineGray)

                AccountSettingsMenuItem(
                    icon = AppIcons.Shield,
                    title = "Privasi dan Keamanan",
                    description = "Informasi keamanan akun",
                    onClick = {
                        showAccountSettings = false
                        showPrivacySecurity = true
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            showAccountSettings = false
                        }
                    ) {
                        Text(
                            text = "Tutup",
                            color = BluePrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    if (showNotificationPreferences && user != null) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isUpdatingNotificationPreference) {
                    showNotificationPreferences = false
                    onResetNotificationPreferenceError()
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = BluePrimary
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Preferensi Notifikasi",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Atur pemberitahuan yang ingin kamu terima.",
                        fontSize = 13.sp,
                        color = TextGray
                    )

                    HorizontalDivider(color = LineGray)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Notifikasi Rekomendasi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = "Terima notifikasi event baru " +
                                        "yang sesuai dengan minatmu.",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = user.recommendationNotificationsEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateRecommendationNotifications(enabled)
                            },
                            enabled = !uiState.isUpdatingNotificationPreference,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = BluePrimary
                            )
                        )
                    }

                    if (uiState.isUpdatingNotificationPreference) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = BluePrimary
                        )

                        Text(
                            text = "Menyimpan pengaturan...",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }

                    uiState.notificationPreferenceError?.let { message ->
                        Text(
                            text = message,
                            fontSize = 12.sp,
                            color = RedDark
                        )
                    }

                    HorizontalDivider(color = LineGray)

                    Text(
                        text = "Notifikasi transaksi dan pesanan " +
                                "tidak dinonaktifkan oleh pengaturan ini.",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationPreferences = false
                        onResetNotificationPreferenceError()
                    },
                    enabled = !uiState.isUpdatingNotificationPreference
                ) {
                    Text(
                        text = "Selesai",
                        color = BluePrimary
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showPrivacySecurity) {
        AlertDialog(
            onDismissRequest = {
                showPrivacySecurity = false
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.Shield,
                        contentDescription = null,
                        tint = BluePrimary
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Privasi dan Keamanan",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Keamanan Sesi Login",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )

                    Text(
                        text = "Tiketiin menyimpan sesi login " +
                                "menggunakan penyimpanan terenkripsi " +
                                "pada perangkat.",
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    HorizontalDivider(color = LineGray)

                    Text(
                        text = "Keamanan Kata Sandi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )

                    Text(
                        text = "Kata sandi dikelola melalui " +
                                "layanan autentikasi Supabase. " +
                                "Kamu dapat memperbaruinya melalui " +
                                "menu Ubah Kata Sandi.",
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    HorizontalDivider(color = LineGray)

                    Text(
                        text = "Informasi Profil",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )

                    Text(
                        text = "Informasi profil disimpan pada " +
                                "akun Tiketiin. Akses profil " +
                                "dibatasi berdasarkan identitas " +
                                "pengguna yang sedang login.",
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    HorizontalDivider(color = LineGray)

                    Text(
                        text = "Tips Keamanan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )

                    Text(
                        text = "Gunakan kata sandi yang kuat, " +
                                "jangan membagikan password atau " +
                                "token login, dan keluar dari akun " +
                                "jika memakai perangkat bersama.",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPrivacySecurity = false
                    }
                ) {
                    Text(
                        text = "Tutup",
                        color = BluePrimary
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showChangePassword) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        var showCurrentPassword by remember {
            mutableStateOf(false)
        }

        var showNewPassword by remember {
            mutableStateOf(false)
        }

        var showConfirmPassword by remember {
            mutableStateOf(false)
        }

        val hasLetter = newPassword.any { it.isLetter() }
        val hasNumber = newPassword.any { it.isDigit() }
        val hasSymbol = newPassword.any {
            !it.isLetterOrDigit() && !it.isWhitespace()
        }

        val isValidPassword =
            newPassword.length >= 8 &&
                    hasLetter &&
                    hasNumber &&
                    hasSymbol

        val passwordsMatch = newPassword == confirmPassword

        AlertDialog(
            onDismissRequest = {
                if (!uiState.isChangingPassword) {
                    showChangePassword = false
                    onResetPasswordStatus()
                }
            },
            title = {
                Text(
                    text = "Ubah Kata Sandi",
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                        },
                        label = {
                            Text("Kata Sandi Saat Ini")
                        },
                        singleLine = true,
                        enabled = !uiState.isChangingPassword,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        visualTransformation = if (showCurrentPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showCurrentPassword = !showCurrentPassword
                                }
                            ) {
                                Icon(
                                    imageVector = if (showCurrentPassword) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    },
                                    contentDescription = if (showCurrentPassword) {
                                        "Sembunyikan kata sandi saat ini"
                                    } else {
                                        "Lihat kata sandi saat ini"
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                        },
                        label = {
                            Text("Kata Sandi Baru")
                        },
                        singleLine = true,
                        enabled = !uiState.isChangingPassword,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        visualTransformation = if (showNewPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showNewPassword = !showNewPassword
                                }
                            ) {
                                Icon(
                                    imageVector = if (showNewPassword) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    },
                                    contentDescription = if (showNewPassword) {
                                        "Sembunyikan kata sandi"
                                    } else {
                                        "Lihat kata sandi"
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                        },
                        label = {
                            Text("Konfirmasi Kata Sandi")
                        },
                        singleLine = true,
                        enabled = !uiState.isChangingPassword,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        visualTransformation = if (showConfirmPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showConfirmPassword = !showConfirmPassword
                                }
                            ) {
                                Icon(
                                    imageVector = if (showConfirmPassword) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    },
                                    contentDescription = if (showConfirmPassword) {
                                        "Sembunyikan kata sandi"
                                    } else {
                                        "Lihat kata sandi"
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Minimal 8 karakter, mengandung " +
                                "huruf, angka, dan simbol.",
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    if (
                        confirmPassword.isNotEmpty() &&
                        !passwordsMatch
                    ) {
                        Text(
                            text = "Konfirmasi kata sandi tidak sama.",
                            color = RedDark,
                            fontSize = 12.sp
                        )
                    }

                    uiState.passwordError?.let { message ->
                        Text(
                            text = message,
                            color = RedDark,
                            fontSize = 12.sp
                        )
                    }

                    if (uiState.passwordSuccess) {
                        Text(
                            text = "Kata sandi berhasil diperbarui.",
                            color = Color(0xFF16803C),
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            onChangePassword(
                                currentPassword,
                                newPassword
                            )
                        },
                        enabled =
                            currentPassword.isNotBlank() &&
                                    currentPassword != newPassword &&
                                    isValidPassword &&
                                    passwordsMatch &&
                                    !uiState.isChangingPassword &&
                                    !uiState.passwordSuccess,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary
                        )
                    ) {
                        if (uiState.isChangingPassword) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Simpan Kata Sandi")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showChangePassword = false
                        onResetPasswordStatus()
                    },
                    enabled = !uiState.isChangingPassword
                ) {
                    Text(
                        text = if (uiState.passwordSuccess) {
                            "Selesai"
                        } else {
                            "Batal"
                        },
                        color = BluePrimary
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showLogout) {
        LogoutDialog(
            onConfirm = {
                showLogout = false
                onLogout()
            },
            onDismiss = {
                showLogout = false
            }
        )
    }

    if (showEditProfile && user != null) {
        EditProfileDialog(
            name = user.name,
            city = user.city,
            isSaving = uiState.isSaving,
            saveError = uiState.saveError,
            cityOptions = uiState.cityOptions,
            isLoadingCities = uiState.isLoadingCities,
            cityOptionsError = uiState.cityOptionsError,
            onRetryLoadCityOptions = onRetryLoadCityOptions,
            onDismiss = {
                if (!uiState.isSaving) {
                    showEditProfile = false
                    onResetSaveStatus()
                }
            },
            onSave = { name, city ->
                onSaveProfile(name, city)
            }
        )
    }

    LaunchedEffect(showEditProfile, uiState.saveSuccess) {
        if (showEditProfile && uiState.saveSuccess) {
            showEditProfile = false
            onResetSaveStatus()
        }
    }
}

@Composable
private fun EditProfileDialog(
    name: String,
    city: String,
    cityOptions: List<CityOptionDto>,
    isLoadingCities: Boolean,
    cityOptionsError: String?,
    isSaving: Boolean,
    saveError: String?,
    onRetryLoadCityOptions: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var editedName by remember(name) {
        mutableStateOf(name)
    }

    var selectedCityId by remember(city) {
        mutableStateOf<Long?>(null)
    }

    var showCityPicker by remember {
        mutableStateOf(false)
    }

    var citySearch by remember {
        mutableStateOf("")
    }

    val cleanName = editedName.trim()

    val selectedCity = cityOptions.firstOrNull {
        it.id == selectedCityId
    } ?: cityOptions.firstOrNull {
        it.displayName.equals(city.trim(), ignoreCase = true)
    }

    val cleanCity = selectedCity?.displayName.orEmpty()
    val isNameValid = cleanName.length in 2..100
    val isCityValid = selectedCity != null

    val hasChanges =
        cleanName != name || cleanCity != city.trim()

    val isValid =
        isNameValid &&
                isCityValid &&
                hasChanges &&
                !isSaving

    Dialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Edit Profil",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Perbarui informasi akun Tiketiin",
                fontSize = 12.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = editedName,
                onValueChange = {
                    if (it.length <= 100) {
                        editedName = it
                    }
                },
                label = {
                    Text("Nama Lengkap")
                },
                singleLine = true,
                enabled = !isSaving,
                isError = editedName.isNotBlank() &&
                        !isNameValid,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    citySearch = ""
                    showCityPicker = true
                },
                enabled = !isSaving &&
                        !isLoadingCities &&
                        cityOptions.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Kota / Kabupaten",
                        fontSize = 11.sp,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = selectedCity?.fullLabel
                            ?: city.takeIf {
                                it.isNotBlank()
                            }?.let {
                                "$it (pilih ulang lokasi)"
                            }
                            ?: "Pilih kota atau kabupaten",
                        color = TextDark,
                        fontSize = 13.sp
                    )
                }

                Icon(
                    imageVector = AppIcons.ChevronRight,
                    contentDescription = null,
                    tint = BluePrimary
                )
            }

            if (isLoadingCities) {
                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = BluePrimary
                )

                Text(
                    text = "Memuat daftar lokasi...",
                    fontSize = 11.sp,
                    color = TextGray
                )
            }

            if (cityOptionsError != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = cityOptionsError,
                    fontSize = 12.sp,
                    color = RedDark
                )

                TextButton(
                    onClick = onRetryLoadCityOptions,
                    enabled = !isLoadingCities
                ) {
                    Text("Coba Lagi")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nama minimal 2 karakter. " +
                        "Pilih kota/kabupaten dari daftar " +
                        "agar lokasi profil lebih akurat.",
                fontSize = 11.sp,
                color = TextGray,
                modifier = Modifier.fillMaxWidth()
            )

            if (saveError != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = saveError,
                    fontSize = 12.sp,
                    color = RedDark,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (selectedCity != null) {
                        onSave(
                            cleanName,
                            selectedCity.displayName
                        )
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Simpan Perubahan")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Batal")
            }
        }
    }

    if (showCityPicker) {
        val filteredCities = remember(
            cityOptions,
            citySearch
        ) {
            cityOptions.filter { option ->
                citySearch.isBlank() ||
                        option.fullLabel.contains(
                            citySearch.trim(),
                            ignoreCase = true
                        )
            }
        }

        Dialog(
            onDismissRequest = {
                showCityPicker = false
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Pilih Kota / Kabupaten",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = citySearch,
                    onValueChange = {
                        citySearch = it
                    },
                    label = {
                        Text("Cari lokasi")
                    },
                    placeholder = {
                        Text("Contoh: Malang")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${filteredCities.size} lokasi ditemukan",
                    fontSize = 12.sp,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (filteredCities.isEmpty()) {
                        Text(
                            text = "Lokasi tidak ditemukan.",
                            fontSize = 13.sp,
                            color = TextGray,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    filteredCities.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCityId = option.id
                                    showCityPicker = false
                                }
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 12.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.fullLabel,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp,
                                color = TextDark
                            )

                            if (selectedCity?.id == option.id) {
                                Text(
                                    text = "✓",
                                    color = BluePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(color = LineGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            showCityPicker = false
                        }
                    ) {
                        Text(
                            text = "Tutup",
                            color = BluePrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountSettingsMenuItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = TextGray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = BluePrimary,
    trailingText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            color = if (tint == RedDark) {
                RedDark
            } else {
                Color(0xFF4A4A4A)
            },
            modifier = Modifier.weight(1f)
        )

        if (trailingText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(YellowSoft)
                    .padding(
                        horizontal = 14.dp,
                        vertical = 1.dp
                    )
            ) {
                Text(
                    text = trailingText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF4A4A4A)
            )
        }
    }
}

@Composable
private fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(RedSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Logout,
                    contentDescription = null,
                    tint = RedDark,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Keluar dari akun?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Kamu perlu masuk kembali untuk " +
                        "membeli atau menjual tiket. " +
                        "Pesanan yang sedang berjalan tetap aman.",
                fontSize = 13.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Ya, Keluar",
                onClick = onConfirm,
                containerColor = RedDark,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlineButton(
                text = "Batal",
                onClick = onDismiss,
                color = TextDark,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
