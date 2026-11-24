package com.example.tiketiin.ui.checkout

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

@Composable
fun TicketQrCode(
    qrCode: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(qrCode) {
        runCatching {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1
            )

            val matrix = MultiFormatWriter().encode(
                qrCode,
                BarcodeFormat.QR_CODE,
                512,
                512,
                hints
            )

            Bitmap.createBitmap(
                512,
                512,
                Bitmap.Config.ARGB_8888
            ).apply {
                for (x in 0 until 512) {
                    for (y in 0 until 512) {
                        setPixel(
                            x,
                            y,
                            if (matrix[x, y]) {
                                android.graphics.Color.BLACK
                            } else {
                                android.graphics.Color.WHITE
                            }
                        )
                    }
                }
            }
        }.getOrNull()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "QR Code Tiket",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code Tiket",
                modifier = Modifier.size(200.dp)
            )
        } else {
            Text(
                text = "QR Code tidak dapat dibuat.",
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = qrCode,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tunjukkan QR Code ini saat verifikasi tiket.",
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
