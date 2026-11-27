
package com.example.tiketiin.ui.auth

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

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> =
        _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (
            _uiState.value.isLoading ||
            _uiState.value.isSuccess
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                repository.login(
                    email = email.trim(),
                    password = password
                )

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
                val message = when (e.code()) {
                    400, 401 ->
                        "Login ditolak. Periksa email, kata sandi, atau verifikasi akun."

                    429 ->
                        "Terlalu banyak percobaan login. Coba lagi nanti."

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
                            "Koneksi gagal. Periksa internet kamu."
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage =
                            "Gagal login. Silakan coba kembali."
                    )
                }
            }
        }
    }
}
