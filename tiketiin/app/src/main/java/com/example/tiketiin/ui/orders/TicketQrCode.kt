
package com.example.tiketiin.ui.orders

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun TicketQrCode(
    qrContent: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(qrContent) {
        if (qrContent.isBlank()) {
            null
        } else {
            try {
                val matrix = QRCodeWriter().encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    512,
                    512
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
                                if (matrix[x, y]) android.graphics.Color.BLACK
                                else android.graphics.Color.WHITE
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code Tiket",
                modifier = Modifier.size(220.dp)
            )
        } else {
            Text(
                text = "QR Code belum tersedia",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
