package com.example.tiketiin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.ui.theme.TiketiinTheme
import kotlinx.coroutines.CancellationException
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            TiketiinTheme {

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {

                    var isInitialized by remember {
                        mutableStateOf(false)
                    }

                    var hasError by remember {
                        mutableStateOf(false)
                    }

                    LaunchedEffect(Unit) {

                        try {

                            SessionManager.initialize(
                                applicationContext
                            )

                            isInitialized = true

                        } catch (e: CancellationException) {

                            throw e

                        } catch (e: Exception) {

                            hasError = true

                        }
                    }

                    when {

                        hasError -> {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    "Gagal memuat sesi aplikasi."
                                )
                            }
                        }

                        !isInitialized -> {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {

                                CircularProgressIndicator()

                            }
                        }

                        else -> {

                            TiketiinApp()
                        }
                    }
                }
            }
        }
    }
}