package com.example.tiketiin.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.auth.AuthRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class RegisterViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())

    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        referralCode: String
    ) {
        if (_uiState.value.isLoading || _uiState.value.isSuccess) {
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val response = repository.register(
                    name = name,
                    phone = phone,
                    email = email,
                    password = password,
                    referralCode = referralCode
                )

                check(response.user != null) {
                    "Supabase tidak mengembalikan data pengguna."
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val body = try {
                    e.response()?.errorBody()?.string().orEmpty()
                } catch (_: IOException) {
                    ""
                }

                Log.e("Register", "Registrasi gagal: HTTP ${e.code()}")

                val message = when {
                    body.contains(
                        "user_already_exists",
                        ignoreCase = true
                    ) || body.contains(
                        "already registered",
                        ignoreCase = true
                    ) ->
                        "Email ini sudah terdaftar. Silakan masuk."

                    body.contains(
                        "weak_password",
                        ignoreCase = true
                    ) ->
                        "Kata sandi terlalu lemah. Gunakan kombinasi lain."

                    e.code() == 429 ->
                        "Terlalu banyak percobaan. Coba lagi nanti."

                    e.code() == 400 || e.code() == 422 ->
                        "Pendaftaran ditolak. Periksa email, kata sandi, " +
                                "dan kode referral jika diisi."

                    else ->
                        "Terjadi kesalahan server (${e.code()})."
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = message
                    )
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage =
                            "Koneksi bermasalah. Periksa internet lalu coba lagi."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage =
                            "Registrasi belum berhasil. Coba kembali."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}