package com.example.tiketiin.data.remote

import com.example.tiketiin.data.remote.api.AuthApiService
import com.example.tiketiin.data.remote.dto.AuthResponse
import com.example.tiketiin.data.remote.dto.AuthUserDto
import com.example.tiketiin.data.remote.dto.LoginRequest
import com.example.tiketiin.data.remote.dto.RegisterRequest
import com.example.tiketiin.data.remote.dto.RegisterMetadata
import com.example.tiketiin.data.remote.dto.RegisterResponse
import com.example.tiketiin.data.remote.dto.UpdatePasswordRequest
import retrofit2.HttpException
import com.example.tiketiin.data.remote.dto.RefreshTokenRequest

class AuthRemoteDataSource(

    private val api: AuthApiService =
        RetrofitClient.authApi

) {

    suspend fun login(
        email: String,
        password: String
    ): AuthResponse {

        return api.login(
            LoginRequest(
                email = email.trim(),
                password = password
            )
        )
    }

    suspend fun refreshToken(
        refreshToken: String
    ): AuthResponse {

        return api.refreshToken(
            RefreshTokenRequest(refreshToken)
        )
    }

    suspend fun getCurrentUser(): AuthUserDto {
        return api.getCurrentUser()
    }

    suspend fun logout() {

        val response = api.logout()

        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }

    suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        referralCode: String
    ): RegisterResponse {

        return api.register(
            RegisterRequest(
                email = email.trim(),
                password = password,
                data = RegisterMetadata(
                    name = name.trim(),
                    phone = phone,
                    referralCode = referralCode
                        .trim()
                        .uppercase()
                        .ifBlank { null }
                )
            )
        )
    }

    suspend fun updatePassword(
        accessToken: String,
        currentPassword: String,
        newPassword: String
    ) {
        val response = api.updatePassword(
            authorization = "Bearer $accessToken",
            request = UpdatePasswordRequest(
                password = newPassword,
                currentPassword = currentPassword
            )
        )

        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
}