package com.example.tiketiin.data.remote.api

import com.example.tiketiin.data.remote.dto.AuthResponse
import com.example.tiketiin.data.remote.dto.AuthUserDto
import com.example.tiketiin.data.remote.dto.LoginRequest
import com.example.tiketiin.data.remote.dto.RegisterRequest
import com.example.tiketiin.data.remote.dto.RegisterResponse
import com.example.tiketiin.data.remote.dto.UpdatePasswordRequest
import retrofit2.http.PUT
import retrofit2.http.Header
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import com.example.tiketiin.data.remote.dto.RefreshTokenRequest

interface AuthApiService {

    // Login menggunakan email dan password
    @POST("auth/v1/token")
    suspend fun login(

        @Body request: LoginRequest,

        @Query("grant_type")
        grantType: String = "password"

    ): AuthResponse


    // Memperbarui access token
    @POST("auth/v1/token")
    suspend fun refreshToken(

        @Body request: RefreshTokenRequest,

        @Query("grant_type")
        grantType: String = "refresh_token"

    ): AuthResponse


    // Mengambil identitas pengguna yang sedang login
    @GET("auth/v1/user")
    suspend fun getCurrentUser(): AuthUserDto


    // Logout dari sesi sekarang
    @POST("auth/v1/logout")
    suspend fun logout(

        @Query("scope")
        scope: String = "local"

    ): Response<Unit>

    @POST("auth/v1/signup")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    // Mengubah kata sandi pengguna yang sedang login
    @PUT("auth/v1/user")
    suspend fun updatePassword(
        @Header("Authorization") authorization: String,
        @Body request: UpdatePasswordRequest
    ): Response<Unit>
}