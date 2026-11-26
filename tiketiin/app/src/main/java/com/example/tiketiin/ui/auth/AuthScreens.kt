
package com.example.tiketiin.ui.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiketiin.ui.components.*
import com.example.tiketiin.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.tiketiin.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext




@Composable
fun WelcomeScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF4059C6),
                        Color(0xFF3049B4),
                        Color(0xFF233C9A)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 130.dp, y = 120.dp)
                .size(390.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0x99FF72E2),
                            Color(0x55E0A1FF),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-1600).dp, y = (-100).dp)
                .size(350.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0x667EACFF),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp)
            ) {
                Spacer(Modifier.height(30.dp))

                TiketiinLogo(
                    fontSize = 30.sp,
                    baseColor = Color.White
                )

                Spacer(Modifier.height(30.dp))

                Image(
                    painter = painterResource(
                        R.drawable.icon_ticket
                    ),
                    contentDescription = "Ilustrasi tiket festival",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1084f / 741f),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(22.dp))

                Text(
                    text = "Beli tiket second,\nnggak bikin was-was lagi",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Platform jual-beli tiket konser dan event yang aman, fleksibel, dan transparan.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(28.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Color.White.copy(alpha = 0.11f)
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.45f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WelcomeFeature(
                        icon = AppIcons.Shield,
                        text = "Akun\nterverifikasi\ne-KYC",
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(56.dp)
                            .background(
                                Color.White.copy(alpha = 0.35f)
                            )
                    )

                    WelcomeFeature(
                        icon = AppIcons.Lock,
                        text = "Dana ditahan\nescrow",
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(56.dp)
                            .background(
                                Color.White.copy(alpha = 0.35f)
                            )
                    )

                    WelcomeFeature(
                        icon = AppIcons.Refresh,
                        text = "Garansi uang\nkembali 100%",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(20.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 36.dp,
                            topEnd = 36.dp
                        )
                    )
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 28.dp,
                        vertical = 18.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Daftarkan akun",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        Toast.makeText(
                            context,
                            "Sign-in dengan Google belum tersedia",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFF252525)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = TextDark
                    )
                ) {
                    Image(
                        painter = painterResource(
                            R.drawable.devicon_google
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "Lanjut dengan Google",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 30.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sudah punya akun? ",
                        color = TextGray,
                        fontSize = 13.sp
                    )

                    Text(
                        text = "Masuk",
                        color = BluePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            onLogin()
                        }
                    )
                }

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun WelcomeFeature(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AuthTopBar(
    step: Int?,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(
                Alignment.CenterStart
            )
        ) {
            Icon(
                imageVector = AppIcons.Back,
                contentDescription = "Kembali"
            )
        }

        if (step != null) {
            Row(
                modifier = Modifier.align(Alignment.Center)
            ) {
                for (i in 1..2) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(
                                width = 75.dp,
                                height = 5.dp
                            )
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (i == step) {
                                    BluePrimary
                                } else {
                                    TextGray
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(TextGray)
        )

        Text(
            text = "  atau  ",
            color = TextGray,
            fontSize = 13.sp
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(TextGray)
        )
    }
}

private data class PasswordRequirements(
    val minLength: Boolean,
    val hasLetter: Boolean,
    val hasNumber: Boolean,
    val hasSymbol: Boolean
) {
    val isValid: Boolean
        get() = minLength &&
                hasLetter &&
                hasNumber &&
                hasSymbol
}

private fun checkPassword(
    password: String
): PasswordRequirements {
    return PasswordRequirements(
        minLength = password.length >= 8,
        hasLetter = password.any {
            it.isLetter()
        },
        hasNumber = password.any {
            it.isDigit()
        },
        hasSymbol = password.any {
            !it.isLetterOrDigit() && !it.isWhitespace()
        }
    )
}

@Composable
private fun PasswordRule(
    text: String,
    isValid: Boolean
) {
    val color = if (isValid) {
        Color(0xFF16A34A)
    } else {
        Color(0xFF9CA3AF)
    }

    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isValid) {
                Icons.Default.CheckCircle
            } else {
                Icons.Default.RadioButtonUnchecked
            },
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            color = color,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun PasswordValidation(
    password: String
) {
    val requirements = checkPassword(password)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        PasswordRule(
            text = "Minimal 8 karakter",
            isValid = requirements.minLength
        )

        PasswordRule(
            text = "Mengandung huruf",
            isValid = requirements.hasLetter
        )

        PasswordRule(
            text = "Mengandung angka",
            isValid = requirements.hasNumber
        )

        PasswordRule(
            text = "Mengandung simbol",
            isValid = requirements.hasSymbol
        )
    }
}

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegister: (
        name: String,
        phone: String,
        email: String,
        password: String,
        referralCode: String
    ) -> Unit,
    onLogin: () -> Unit,
    isLoading: Boolean = false,
    registerError: String? = null
) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(false) }
    var referralCode by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun validate(): String? {
        return when {
            phone.length < 9 ->
                "Nomor HP belum benar."

            name.isBlank() ->
                "Nama lengkap wajib diisi."

            !email.contains("@") || !email.contains(".") ->
                "Format email belum benar."

            !checkPassword(password).isValid ->
                "Kata sandi harus memenuhi semua persyaratan."

            !agree ->
                "Setujui Syarat & Ketentuan terlebih dahulu."

            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AuthTopBar(
            step = 1,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    append("Buat akun ")
                    appendLogo()
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Daftar sekarang dan mulai temukan tiket event favoritmu!",
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            FormField(label = "Nomor HP") {
                AppTextField(
                    value = phone,
                    onValueChange = { input ->
                        phone = input.filter {
                            it.isDigit()
                        }
                    },
                    placeholder = "Masukkan no HP",
                    keyboardType = KeyboardType.Phone,
                    leading = {
                        Text(
                            text = "+62",
                            modifier = Modifier.padding(
                                start = 12.dp
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FormField(label = "Nama Lengkap") {
                AppTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    placeholder = "Masukkan nama lengkap"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FormField(label = "Email") {
                AppTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    placeholder = "Masukkan email",
                    keyboardType = KeyboardType.Email
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FormField(label = "Kata Sandi") {
                AppTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    placeholder = "Masukkan kata sandi",
                    isPassword = true,
                    keyboardType = KeyboardType.Password
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            PasswordValidation(password = password)

            Spacer(modifier = Modifier.height(8.dp))

            FormField(
                label = "Kode Referral (Opsional)"
            ) {
                AppTextField(
                    value = referralCode,
                    onValueChange = { input ->
                        referralCode = input
                            .trim()
                            .uppercase()
                            .take(30)
                    },
                    placeholder = "Contoh: TKT-XXXXXXXXXX"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agree,
                    onCheckedChange = {
                        agree = it
                    }
                )

                Text(
                    text = buildAnnotatedString {
                        append("Saya setuju dengan ")

                        withStyle(
                            SpanStyle(color = BluePrimary)
                        ) {
                            append("Syarat & Ketentuan")
                        }

                        append(" dan ")

                        withStyle(
                            SpanStyle(color = BluePrimary)
                        ) {
                            append("Kebijakan Privasi")
                        }

                        append(" tiketiin.")
                    },
                    color = TextGray,
                    fontSize = 13.sp
                )
            }

            val message = registerError

            if (message != null) {
                Text(
                    text = message,
                    color = RedDark,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
            }



            PrimaryButton(
                text = if (isLoading) {
                    "Mendaftarkan..."
                } else {
                    "Daftar"
                },
                onClick = {
                    val validationError = validate()

                    if (validationError != null) {
                        Toast.makeText(
                            context,
                            validationError,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        onRegister(
                            name.trim(),
                            "+62$phone",
                            email.trim(),
                            password,
                            referralCode.trim()
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OrDivider()

            Spacer(modifier = Modifier.height(12.dp))


            OutlinedButton(
                onClick = {
                    Toast.makeText(
                        context,
                        "Daftar dengan Google belum tersedia",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF252525)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TextDark
                )
            ) {
                Image(
                    painter = painterResource(
                        R.drawable.devicon_google
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Daftar dengan Google",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }


            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            ) {
                Text(
                    text = "Sudah punya akun? ",
                    color = TextGray,
                    fontSize = 13.sp
                )

                Text(
                    text = "Masuk",
                    color = BluePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(
                        onClick = onLogin
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun OtpScreen(
    phone: String,
    onBack: () -> Unit,
    onVerified: () -> Unit
) {
    var code by remember {
        mutableStateOf("")
    }

    var secondsLeft by remember {
        mutableIntStateOf(165)
    }

    var resendLeft by remember {
        mutableIntStateOf(30)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)

            if (secondsLeft > 0) {
                secondsLeft--
            }

            if (resendLeft > 0) {
                resendLeft--
            }
        }
    }

    fun clock(total: Int): String {
        return "%02d:%02d".format(
            total / 60,
            total % 60
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AuthTopBar(
            step = 2,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(BlueSurface),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    BluePrimary,
                                    GradientPink
                                )
                            )
                        )
                        .padding(
                            horizontal = 18.dp,
                            vertical = 8.dp
                        )
                ) {
                    Text(
                        text = "* * *",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Verifikasi nomor HP",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Kami telah mengirimkan kode OTP ke\n+62$phone",
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box {
                OutlinedTextField(
                    value = code,
                    onValueChange = { input ->
                        code = input
                            .filter { it.isDigit() }
                            .take(6)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .alpha(0f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        6.dp
                    )
                ) {
                    for (i in 0 until 6) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(74.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (i == code.length) {
                                        BluePrimary
                                    } else {
                                        LineGray
                                    },
                                    shape = RoundedCornerShape(
                                        10.dp
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = code.getOrNull(i)
                                    ?.toString()
                                    ?: "",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = buildAnnotatedString {
                    append("Kode OTP berlaku selama ")

                    withStyle(
                        SpanStyle(
                            color = BluePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(clock(secondsLeft))
                    }
                },
                color = TextGray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = "Verifikasi",
                onClick = onVerified,
                enabled = code.length == 6,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tidak menerima kode?",
                color = TextGray,
                fontSize = 13.sp
            )

            Text(
                text = if (resendLeft > 0) {
                    "Kirim ulang (${clock(resendLeft)})"
                } else {
                    "Kirim ulang"
                },
                color = BluePrimary,
                fontSize = 13.sp,
                modifier = Modifier.clickable(
                    enabled = resendLeft == 0
                ) {
                    secondsLeft = 165
                    resendLeft = 30
                }
            )
        }
    }
}


@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
    isLoading: Boolean = false,
    loginError: String? = null
) {
    val context = LocalContext.current

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AuthTopBar(
            step = null,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Masuk ke ")
                    appendLogo()
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Lanjutkan untuk menemukan tiket event favoritmu, aman dan terpercaya.",
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(520.dp)
                        .offset(x = 50.dp, y = (-10).dp)
                        .background(
                            brush = Brush.radialGradient(
                                0.0f to Color(0x554B70E8),
                                0.35f to Color(0x335F82F4),
                                0.70f to Color(0x117B98FF),
                                1.0f to Color.Transparent
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(500.dp)
                        .offset(x = (-50).dp, y = 15.dp)
                        .background(
                            brush = Brush.radialGradient(
                                0.0f to Color(0x55F77CEB),
                                0.40f to Color(0x33F9A1F2),
                                0.75f to Color(0x11FFC8FA),
                                1.0f to Color.Transparent
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(420.dp)
                        .offset(x = 0.dp, y = 10.dp)
                        .background(
                            brush = Brush.radialGradient(
                                0.0f to Color(0x229B8CFF),
                                0.55f to Color(0x117B9CFF),
                                1.0f to Color.Transparent
                            ),
                            shape = CircleShape
                        )
                )

                Image(
                    painter = painterResource(R.drawable.login_ticket),
                    contentDescription = "Ilustrasi tiket Tiketiin",
                    modifier = Modifier
                        .fillMaxWidth(0.83f)
                        .height(190.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            FormField(label = "Email") {
                AppTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    placeholder = "Masukkan email",
                    keyboardType = KeyboardType.Email
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FormField(label = "Kata Sandi") {
                AppTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    placeholder = "Masukkan kata sandi",
                    isPassword = true,
                    keyboardType = KeyboardType.Password
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lupa Kata Sandi",
                color = BluePrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        Toast.makeText(
                            context,
                            "Fitur lupa kata sandi belum tersedia",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            )

            val message = loginError

            if (message != null) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message,
                    color = RedDark,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))


            PrimaryButton(
                text = if (isLoading) {
                    "Memproses..."
                } else {
                    "Masuk"
                },
                onClick = {
                    val validationError = when {
                        email.isBlank() ->
                            "Email wajib diisi."

                        !Patterns.EMAIL_ADDRESS
                            .matcher(email.trim())
                            .matches() ->
                            "Format email belum benar."

                        password.isBlank() ->
                            "Kata sandi wajib diisi."

                        else -> null
                    }

                    if (validationError != null) {
                        Toast.makeText(
                            context,
                            validationError,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        onLogin(
                            email.trim(),
                            password
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OrDivider()

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    Toast.makeText(
                        context,
                        "Login dengan Google belum tersedia",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF252525)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TextDark
                )
            ) {
                Image(
                    painter = painterResource(
                        R.drawable.devicon_google
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Masuk dengan Google",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text(
                    text = "Belum punya akun? ",
                    color = TextGray,
                    fontSize = 13.sp
                )

                Text(
                    text = "Daftar sekarang",
                    color = BluePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(
                        onClick = onRegister
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
