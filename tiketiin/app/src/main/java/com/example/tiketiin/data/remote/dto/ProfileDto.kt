package com.example.tiketiin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileDto(

    val id: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val city: String?,
    val level: String,

    @SerializedName("referral_code")
    val referralCode: String?,

    @SerializedName("is_kyc_verified")
    val isKycVerified: Boolean,

    @SerializedName("avatar_url")
    val avatarUrl: String? = null,

    @SerializedName("recommendation_notifications_enabled")
    val recommendationNotificationsEnabled: Boolean = true
)