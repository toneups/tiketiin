
package com.example.tiketiin.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import androidx.camera.core.ExperimentalGetImage
import androidx.annotation.OptIn

@Composable
fun TicketScannerScreen(
    viewModel: TicketScannerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            cameraGranted = granted
        }

    LaunchedEffect(Unit) {
        if (!cameraGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("Kembali")
            }

            Text(
                text = "Scan Tiket",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (cameraGranted) {
            if (!uiState.isLoading && uiState.isValid == null) {
                TicketCameraPreview(
                    onQrDetected = { qr ->
                        viewModel.validateTicket(qr)
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text("Pemindaian selesai.")
                    }
                }
            }
        } else {
            Text("Aplikasi membutuhkan izin kamera untuk memindai QR.")
            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            ) {
                Text("Izinkan Kamera")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.message.isNotBlank()) {
            Text(
                text = uiState.message,
                style = MaterialTheme.typography.titleMedium,
                color = if (uiState.isValid == true) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        if (uiState.isValid != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = viewModel::resetState
            ) {
                Text("Scan Tiket Berikutnya")
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun TicketCameraPreview(
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewView by remember {
        mutableStateOf<PreviewView?>(null)
    }

    val scanner = remember {
        BarcodeScanning.getClient()
    }

    val executor = remember {
        Executors.newSingleThreadExecutor()
    }

    val scanHandled = remember {
        AtomicBoolean(false)
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).also {
                previewView = it
                it.scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    )

    DisposableEffect(previewView) {
        val view = previewView

        if (view != null) {
            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)

            val listener = Runnable {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = view.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .build()

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image

                    if (mediaImage == null || scanHandled.get()) {
                        imageProxy.close()
                    } else {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                val qr = barcodes
                                    .firstOrNull {
                                        it.rawValue != null
                                    }
                                    ?.rawValue

                                if (
                                    !qr.isNullOrBlank() &&
                                    scanHandled.compareAndSet(false, true)
                                ) {
                                    ContextCompat.getMainExecutor(context)
                                        .execute {
                                            onQrDetected(qr)
                                        }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            cameraProviderFuture.addListener(
                listener,
                ContextCompat.getMainExecutor(context)
            )
        }

        onDispose {
            try {
                ProcessCameraProvider.getInstance(context)
                    .get()
                    .unbindAll()
            } catch (_: Exception) {
            }

            scanner.close()
            executor.shutdown()
        }
    }
}
