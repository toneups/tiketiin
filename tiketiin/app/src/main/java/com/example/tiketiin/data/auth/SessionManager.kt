
package com.example.tiketiin.data.auth

import android.content.Context
import android.os.SystemClock
import com.example.tiketiin.data.remote.RetrofitClient
import com.example.tiketiin.data.remote.dto.AuthResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SessionManager {

    @Volatile
    private var session: AuthResponse? = null

    private lateinit var storage: SecureSessionStorage

    private var isInitialized = false

    @Volatile
    private var fallbackExpiryElapsedMs: Long? = null

    val userId: String?
        get() = session?.user?.id

    val email: String?
        get() = session?.user?.email

    val accessToken: String?
        get() = session?.accessToken

    val refreshToken: String?
        get() = session?.refreshToken

    val expiresAt: Long?
        get() = session?.expiresAt

    val hasSession: Boolean
        get() = session != null

    suspend fun initialize(context: Context) {
        if (isInitialized) return

        storage = SecureSessionStorage(
            context.applicationContext
        )

        session = withContext(Dispatchers.IO) {
            try {
                storage.loadSession()
            } catch (e: Exception) {
                storage.clearSession()
                null
            }
        }

        RetrofitClient.setAccessToken(
            session?.accessToken
        )

        isInitialized = true
    }

    suspend fun saveSession(response: AuthResponse) {
        check(isInitialized) {
            "SessionManager belum diinisialisasi"
        }

        withContext(Dispatchers.IO) {
            storage.saveSession(response)
        }

        session = response

        fallbackExpiryElapsedMs =
            SystemClock.elapsedRealtime() +
                    response.expiresIn * 1000L

        RetrofitClient.setAccessToken(
            response.accessToken
        )
    }

    suspend fun clearSession() {
        session = null
        fallbackExpiryElapsedMs = null

        RetrofitClient.setAccessToken(null)

        if (isInitialized) {
            withContext(Dispatchers.IO) {
                storage.clearSession()
            }
        }
    }

    fun isTokenExpired(): Boolean {
        if (!hasSession) return true

        val expiry = expiresAt

        if (expiry != null) {
            val now = System.currentTimeMillis() / 1000
            return now >= expiry - 60
        }

        val fallback = fallbackExpiryElapsedMs
            ?: return true

        return SystemClock.elapsedRealtime() >=
                fallback - 60_000L
    }
}
