
package com.example.tiketiin.data.auth

import com.example.tiketiin.data.remote.AuthRemoteDataSource
import com.example.tiketiin.data.remote.dto.AuthUserDto
import com.example.tiketiin.data.remote.dto.RegisterResponse
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AuthRepository(
    private val remoteDataSource: AuthRemoteDataSource =
        AuthRemoteDataSource()
) {

    companion object {
        private val sessionMutex = Mutex()
    }

    suspend fun login(
        email: String,
        password: String
    ): AuthUserDto = sessionMutex.withLock {
        check(!SessionManager.hasSession) {
            "Logout terlebih dahulu"
        }

        val response = remoteDataSource.login(
            email = email,
            password = password
        )

        withContext(NonCancellable) {
            SessionManager.saveSession(response)
        }

        response.user
    }

    suspend fun getCurrentUser(): AuthUserDto {
        ensureValidSession()
        return remoteDataSource.getCurrentUser()
    }

    private suspend fun performRefresh(): AuthUserDto {
        val previousUserId = requireNotNull(
            SessionManager.userId
        )

        val refreshToken = requireNotNull(
            SessionManager.refreshToken
        ) {
            "Sesi tidak tersedia"
        }

        val response = remoteDataSource.refreshToken(
            refreshToken
        )

        check(response.user.id == previousUserId) {
            "Identitas sesi tidak sesuai"
        }

        withContext(NonCancellable) {
            SessionManager.saveSession(response)
        }

        return response.user
    }

    suspend fun refreshSession(): AuthUserDto =
        sessionMutex.withLock {
            performRefresh()
        }

    suspend fun ensureValidSession(
        forceRefresh: Boolean = false
    ) = sessionMutex.withLock {
        check(SessionManager.hasSession) {
            "Pengguna belum login"
        }

        if (
            forceRefresh ||
            SessionManager.isTokenExpired()
        ) {
            performRefresh()
        }

        Unit
    }

    suspend fun logout() = sessionMutex.withLock {
        try {
            if (SessionManager.hasSession) {
                if (SessionManager.isTokenExpired()) {
                    performRefresh()
                }

                remoteDataSource.logout()
            }
        } finally {
            withContext(NonCancellable) {
                SessionManager.clearSession()
            }
        }
    }

    suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        referralCode: String
    ): RegisterResponse {
        check(!SessionManager.hasSession) {
            "Logout sebelum mendaftarkan akun baru."
        }

        return remoteDataSource.register(
            name = name,
            phone = phone,
            email = email,
            password = password,
            referralCode = referralCode
        )
    }

    suspend fun updatePassword(
        currentPassword: String,
        newPassword: String
    ) {
        require(currentPassword.isNotBlank()) {
            "Kata sandi saat ini wajib diisi."
        }

        require(
            newPassword.length >= 8 &&
                    newPassword.any { it.isLetter() } &&
                    newPassword.any { it.isDigit() } &&
                    newPassword.any {
                        !it.isLetterOrDigit() && !it.isWhitespace()
                    }
        ) {
            "Kata sandi baru harus memenuhi persyaratan."
        }

        require(currentPassword != newPassword) {
            "Kata sandi baru harus berbeda dari kata sandi lama."
        }

        sessionMutex.withLock {
            check(SessionManager.hasSession) {
                "Silakan login kembali."
            }

            if (SessionManager.isTokenExpired()) {
                performRefresh()
            }

            val accessToken = requireNotNull(
                SessionManager.accessToken
            ) {
                "Sesi tidak tersedia. Silakan login kembali."
            }

            remoteDataSource.updatePassword(
                accessToken = accessToken,
                currentPassword = currentPassword,
                newPassword = newPassword
            )
        }
    }
}
