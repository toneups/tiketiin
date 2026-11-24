package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

// Request login
class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val data: RegisterMetadata
)

data class RegisterMetadata(
    val name: String,
    val phone: String,

    @SerializedName("referral_code")
    val referralCode: String? = null
)

// Response signup dapat berisi user tanpa token,
// tergantung pengaturan konfirmasi email di Supabase.
data class RegisterResponse(
    val user: AuthUserDto? = null
)

// Request memperbarui access token
class RefreshTokenRequest(

    @SerializedName("refresh_token")
    val refreshToken: String

)

// Identitas pengguna Supabase
data class AuthUserDto(
    val id: String,
    val email: String?,
    val phone: String?
)

// Response setelah login berhasil
class AuthResponse(

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("token_type")
    val tokenType: String,

    @SerializedName("expires_in")
    val expiresIn: Long,

    @SerializedName("expires_at")
    val expiresAt: Long?,

    val user: AuthUserDto

)

data class UpdatePasswordRequest(
    val password: String,

    @SerializedName("current_password")
    val currentPassword: String
)