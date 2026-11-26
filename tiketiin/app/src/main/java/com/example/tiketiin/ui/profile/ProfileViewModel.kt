
package com.example.tiketiin.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.auth.AuthRepository
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.model.User
import com.example.tiketiin.data.remote.dto.CityOptionDto
import com.example.tiketiin.data.repository.ProfileRepository
import com.example.tiketiin.data.repository.PurchaseHistoryRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

data class ProfileUiState(
    val user: User? = null,
    val purchaseCount: Int = 0,
    val ticketCount: Int = 0,
    val spending: Long = 0L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
    val isChangingPassword: Boolean = false,
    val passwordError: String? = null,
    val passwordSuccess: Boolean = false,
    val isUpdatingNotificationPreference: Boolean = false,
    val notificationPreferenceError: String? = null,
    val cityOptions: List<CityOptionDto> = emptyList(),
    val isLoadingCities: Boolean = false,
    val cityOptionsError: String? = null,
    val hasLoadedCities: Boolean = false,
    val discountSaved: Long = 0L,
    val isLoadingStatistics: Boolean = false,
    val statisticsError: String? = null,

) {
    val levelProgress: Float
        get() {
            val points = user?.membershipPoints ?: 0L

            return when {
                points >= 500L -> 1f
                points >= 250L -> (points - 250L).toFloat() / 250L
                points >= 30L -> (points - 30L).toFloat() / 220L
                else -> points.toFloat() / 30L
            }.coerceIn(0f, 1f)
        }

    val nextMembershipLevel: String
        get() {
            val points = user?.membershipPoints ?: 0L

            return when {
                points >= 500L -> "Platinum"
                points >= 250L -> "Platinum"
                points >= 30L -> "Gold"
                else -> "Silver"
            }
        }

    val pointsToNextLevel: Long
        get() {
            val points = user?.membershipPoints ?: 0L

            return when {
                points >= 500L -> 0L
                points >= 250L -> 500L - points
                points >= 30L -> 250L - points
                else -> 30L - points
            }.coerceAtLeast(0L)
        }
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val purchaseHistoryRepository: PurchaseHistoryRepository =
        PurchaseHistoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())

    val uiState: StateFlow<ProfileUiState> =
        _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load() {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            val cachedUser = profileRepository.getCachedProfile()

            _uiState.update { state ->
                val previousUserId = state.user?.id
                val currentUserId = SessionManager.userId

                if (
                    previousUserId != null &&
                    previousUserId != currentUserId
                ) {
                    ProfileUiState(
                        user = cachedUser,
                        isLoading = true
                    )
                } else {
                    state.copy(
                        user = cachedUser,
                        isLoading = true,
                        errorMessage = null
                    )
                }
            }

            try {
                val user = profileRepository.getMyProfile()

                _uiState.update {
                    it.copy(
                        user = user,
                        isLoading = false,
                        errorMessage = null
                    )
                }

                loadPurchaseStatistics(user.id)

            } catch (e: CancellationException) {
                throw e

            } catch (e: IOException) {
                val savedUser = profileRepository.getCachedProfile()
                    ?: _uiState.value.user?.takeIf { user ->
                        user.id == SessionManager.userId
                    }

                _uiState.update {
                    it.copy(
                        user = savedUser,
                        isLoading = false,
                        errorMessage = if (savedUser == null) {
                            "Koneksi internet bermasalah."
                        } else {
                            null
                        }
                    )
                }

            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401 -> "Sesi login tidak valid. Silakan masuk kembali."
                    403 -> "Akses profil ditolak."
                    else -> "Gagal mengambil profil (${e.code()})."
                }

                val allowedCache = if (
                    e.code() == 401 || e.code() == 403
                ) {
                    null
                } else {
                    profileRepository.getCachedProfile()
                }

                _uiState.update {
                    it.copy(
                        user = allowedCache,
                        isLoading = false,
                        errorMessage = message
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        user = null,
                        isLoading = false,
                        errorMessage = "Profil belum dapat dimuat. Coba lagi."
                    )
                }
            }
        }
    }

    private suspend fun loadPurchaseStatistics(userId: String) {
        _uiState.update {
            it.copy(
                isLoadingStatistics = true,
                statisticsError = null
            )
        }

        try {
            val orders = purchaseHistoryRepository.getPurchaseHistory()

            check(SessionManager.userId == userId) {
                "Sesi pengguna berubah."
            }

            val purchaseCount = orders.size

            val ticketCount = orders.sumOf { order ->
                order.quantity
            }

            val spending = orders.sumOf { order ->
                order.totalPrice
            }

            val discountSaved = orders.sumOf { order ->
                order.membershipDiscountAmount
            }

            _uiState.update {
                it.copy(
                    purchaseCount = purchaseCount,
                    ticketCount = ticketCount,
                    spending = spending,
                    discountSaved = discountSaved,
                    isLoadingStatistics = false,
                    statisticsError = null
                )
            }

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoadingStatistics = false,
                    statisticsError = "Statistik pembelian belum dapat dimuat."
                )
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.logout()

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                // Sesi lokal dibersihkan oleh AuthRepository.

            } finally {
                profileRepository.clearCachedProfile()
                _uiState.value = ProfileUiState()
            }

            onDone()
        }
    }

    fun updateProfile(
        name: String,
        city: String
    ) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    saveError = null,
                    saveSuccess = false
                )
            }

            try {
                val updatedUser = profileRepository.updateMyProfile(
                    name = name,
                    city = city
                )

                _uiState.update {
                    it.copy(
                        user = updatedUser,
                        isSaving = false,
                        saveSuccess = true
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                val message = when (e) {
                    is IllegalArgumentException ->
                        e.message ?: "Data profil tidak valid."

                    is IOException ->
                        "Koneksi bermasalah. Coba lagi."

                    else ->
                        "Perubahan profil gagal disimpan."
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = message
                    )
                }
            }
        }
    }

    fun resetSaveStatus() {
        _uiState.update {
            it.copy(
                saveError = null,
                saveSuccess = false
            )
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        if (_uiState.value.isChangingPassword) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isChangingPassword = true,
                    passwordError = null,
                    passwordSuccess = false
                )
            }

            try {
                authRepository.updatePassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )

                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        passwordSuccess = true,
                        passwordError = null
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: HttpException) {
                val errorCode = try {
                    val errorJson = e.response()
                        ?.errorBody()
                        ?.string()
                        .orEmpty()

                    val json = JSONObject(errorJson)

                    json.optString("error_code").ifBlank {
                        json.optString("code")
                    }
                } catch (_: Exception) {
                    ""
                }

                val message = when (errorCode) {
                    "current_password_required" ->
                        "Masukkan kata sandi saat ini."

                    "current_password_mismatch",
                    "current_password_invalid" ->
                        "Kata sandi saat ini tidak sesuai."

                    "same_password" ->
                        "Kata sandi baru harus berbeda dari kata sandi lama."

                    "reauthentication_needed",
                    "reauthentication_not_valid" ->
                        "Supabase meminta verifikasi ulang sebelum " +
                                "kata sandi dapat diubah."

                    "weak_password" ->
                        "Kata sandi belum memenuhi persyaratan keamanan."

                    else -> when (e.code()) {
                        400, 422 ->
                            "Kata sandi ditolak oleh server " +
                                    "(HTTP ${e.code()}, kode: " +
                                    "${errorCode.ifBlank { "tidak tersedia" }})."

                        401 ->
                            "Sesi tidak valid. Silakan login kembali."

                        429 ->
                            "Terlalu banyak percobaan. Coba lagi nanti."

                        else ->
                            "Gagal mengubah kata sandi (HTTP ${e.code()})."
                    }
                }

                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        passwordError = message
                    )
                }

            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        passwordError = "Koneksi internet bermasalah."
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        passwordError =
                            "Kata sandi gagal diperbarui. Coba lagi."
                    )
                }
            }
        }
    }

    fun resetPasswordStatus() {
        if (_uiState.value.isChangingPassword) return

        _uiState.update {
            it.copy(
                passwordError = null,
                passwordSuccess = false
            )
        }
    }

    fun updateRecommendationNotifications(enabled: Boolean) {
        if (_uiState.value.isUpdatingNotificationPreference) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingNotificationPreference = true,
                    notificationPreferenceError = null
                )
            }

            try {
                val updatedUser =
                    profileRepository.updateRecommendationNotifications(
                        enabled = enabled
                    )

                _uiState.update {
                    it.copy(
                        user = updatedUser,
                        isUpdatingNotificationPreference = false,
                        notificationPreferenceError = null
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isUpdatingNotificationPreference = false,
                        notificationPreferenceError =
                            "Koneksi bermasalah. Pengaturan belum disimpan."
                    )
                }

            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401 -> "Sesi tidak valid. Silakan login kembali."
                    403 -> "Perubahan pengaturan tidak diizinkan."
                    else -> "Gagal menyimpan pengaturan (${e.code()})."
                }

                _uiState.update {
                    it.copy(
                        isUpdatingNotificationPreference = false,
                        notificationPreferenceError = message
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdatingNotificationPreference = false,
                        notificationPreferenceError =
                            "Pengaturan belum berhasil disimpan."
                    )
                }
            }
        }
    }

    fun resetNotificationPreferenceError() {
        _uiState.update {
            it.copy(
                notificationPreferenceError = null
            )
        }
    }

    fun loadCityOptions() {
        if (
            _uiState.value.isLoadingCities ||
            _uiState.value.hasLoadedCities
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingCities = true,
                    cityOptionsError = null
                )
            }

            try {
                val cities = profileRepository.getCityOptions()

                if (cities.isEmpty()) {
                    throw IllegalStateException(
                        "Daftar kota belum tersedia."
                    )
                }

                _uiState.update {
                    it.copy(
                        cityOptions = cities,
                        isLoadingCities = false,
                        cityOptionsError = null,
                        hasLoadedCities = true
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoadingCities = false,
                        cityOptionsError =
                            "Koneksi internet bermasalah. Coba lagi."
                    )
                }

            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401 ->
                        "Sesi login tidak valid. Silakan masuk kembali."

                    403 ->
                        "Akses daftar kota ditolak."

                    else ->
                        "Gagal mengambil daftar kota (${e.code()})."
                }

                _uiState.update {
                    it.copy(
                        isLoadingCities = false,
                        cityOptionsError = message
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingCities = false,
                        cityOptionsError =
                            "Daftar kota belum dapat dimuat. Coba lagi."
                    )
                }
            }
        }
    }

    fun retryLoadCityOptions() {
        if (_uiState.value.isLoadingCities) return

        _uiState.update {
            it.copy(
                cityOptionsError = null,
                hasLoadedCities = false
            )
        }

        loadCityOptions()
    }
}
