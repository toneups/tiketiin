
package com.example.tiketiin.data.repository

import com.example.tiketiin.data.auth.AuthenticatedRequest
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.model.User
import com.example.tiketiin.data.remote.RetrofitClient
import com.example.tiketiin.data.remote.dto.CityOptionDto
import com.example.tiketiin.data.remote.dto.ProfileDto

class ProfileRepository {

    companion object {
        @Volatile
        private var cachedProfile: User? = null
    }

    fun getCachedProfile(): User? {
        val currentUserId = SessionManager.userId

        if (currentUserId == null) {
            cachedProfile = null
            return null
        }

        return cachedProfile?.takeIf {
            it.id == currentUserId
        }
    }

    fun clearCachedProfile() {
        cachedProfile = null
    }

    private fun rememberProfile(user: User): User {
        if (SessionManager.userId == user.id) {
            cachedProfile = user
        }
        return user
    }

    private fun ProfileDto.toUser(): User {
        return User(
            id = id,
            name = name,
            email = email.orEmpty(),
            phone = phone.orEmpty(),
            city = city.orEmpty(),
            level = level,
            referralCode = referralCode.orEmpty(),
            isKycVerified = isKycVerified,
            recommendationNotificationsEnabled =
                recommendationNotificationsEnabled
        )
    }

    suspend fun getMyProfile(): User {
        val userId = requireNotNull(SessionManager.userId) {
            "Sesi login tidak ditemukan."
        }

        val profiles = AuthenticatedRequest.execute {
            RetrofitClient.api.getProfile(
                id = "eq.$userId"
            )
        }

        val profile = profiles.singleOrNull()
            ?: throw IllegalStateException(
                "Profil pengguna tidak ditemukan."
            )

        check(profile.id == userId) {
            "Identitas profil tidak sesuai dengan sesi."
        }

        return rememberProfile(profile.toUser())
    }

    suspend fun updateMyProfile(
        name: String,
        city: String
    ): User {
        val cleanName = name.trim()
        val cleanCity = city.trim()

        require(cleanName.length in 2..100) {
            "Nama harus berisi 2–100 karakter."
        }

        require(cleanCity.length in 2..100) {
            "Kota harus berisi 2–100 karakter."
        }

        val userId = requireNotNull(SessionManager.userId) {
            "Sesi login tidak ditemukan."
        }

        val result = AuthenticatedRequest.execute {
            RetrofitClient.api.updateProfile(
                id = "eq.$userId",
                updates = mapOf(
                    "name" to cleanName,
                    "city" to cleanCity
                )
            )
        }

        val updatedProfile = result.singleOrNull()
            ?: throw IllegalStateException(
                "Profil tidak ditemukan atau perubahan tidak diizinkan."
            )

        check(updatedProfile.id == userId) {
            "Identitas profil tidak sesuai."
        }

        return rememberProfile(updatedProfile.toUser())
    }

    suspend fun updateRecommendationNotifications(
        enabled: Boolean
    ): User {
        val userId = requireNotNull(SessionManager.userId) {
            "Sesi login tidak ditemukan."
        }

        val result = AuthenticatedRequest.execute {
            RetrofitClient.api
                .updateRecommendationNotificationPreference(
                    id = "eq.$userId",
                    updates = mapOf(
                        "recommendation_notifications_enabled" to enabled
                    )
                )
        }

        val updatedProfile = result.singleOrNull()
            ?: throw IllegalStateException(
                "Preferensi tidak berhasil diperbarui."
            )

        check(updatedProfile.id == userId) {
            "Identitas profil tidak sesuai dengan sesi."
        }

        check(
            updatedProfile.recommendationNotificationsEnabled == enabled
        ) {
            "Preferensi yang tersimpan tidak sesuai."
        }

        return rememberProfile(updatedProfile.toUser())
    }

    suspend fun getCityOptions(): List<CityOptionDto> {
        val cityOptions = AuthenticatedRequest.execute {
            RetrofitClient.api.getCityOptions()
        }

        return cityOptions
            .distinctBy { it.id }
            .sortedWith(
                compareBy<CityOptionDto> { it.province }
                    .thenBy { it.administrativeType }
                    .thenBy { it.name }
            )
    }
}
