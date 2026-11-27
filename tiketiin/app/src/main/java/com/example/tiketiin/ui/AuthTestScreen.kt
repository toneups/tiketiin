package com.example.tiketiin.ui.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.tiketiin.data.auth.AuthRepository
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.tiketiin.data.auth.AuthenticatedRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


@Composable
fun AuthTestScreen() {

    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Belum diuji") }
    var isBusy by remember { mutableStateOf(false) }
    var isLoggedIn by remember {
        mutableStateOf(SessionManager.hasSession)
    }

    fun runTest(action: suspend () -> String) {

        if (isBusy) return

        isBusy = true

        scope.launch {

            try {

                result = action()

            } catch (e: CancellationException) {

                throw e

            } catch (e: HttpException) {

                result = "HTTP Error: ${e.code()}"

            } catch (e: Exception) {

                result = "Test gagal: ${e.javaClass.simpleName}"

            } finally {

                password = ""
                isBusy = false
                isLoggedIn = SessionManager.hasSession

            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Tiketiin Auth Testing",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = if (isLoggedIn) {
                "Status: Authenticated"
            } else {
                "Status: Not authenticated"
            }
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation =
                PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            enabled = !isBusy &&
                    !isLoggedIn &&
                    email.isNotBlank() &&
                    password.isNotBlank(),

            onClick = {

                runTest {

                    val user = repository.login(
                        email = email,
                        password = password
                    )

                    """
                    Login berhasil!

                    Email: ${user.email}
                    UUID: ${user.id}
                    Access token tersedia:
                    ${SessionManager.accessToken != null}
                    Refresh token tersedia:
                    ${SessionManager.refreshToken != null}
                    """.trimIndent()
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Login")
        }

        OutlinedButton(
            enabled = !isBusy && isLoggedIn,

            onClick = {

                runTest {

                    val user = repository.getCurrentUser()

                    check(user.id == SessionManager.userId)

                    "Get Current User berhasil!\nUUID: ${user.id}"
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Current User")
        }

        OutlinedButton(
            enabled = !isBusy && isLoggedIn,

            onClick = {

                runTest {

                    val userId =
                        requireNotNull(SessionManager.userId)

                    val profile = RetrofitClient.api
                        .getProfile("eq.$userId")
                        .single()

                    check(profile.id == userId)

                    """
                    Profil berhasil diambil!

                    Nama: ${profile.name}
                    Email: ${profile.email}
                    Kota: ${profile.city}
                    Level: ${profile.level}
                    KYC: ${profile.isKycVerified}
                    """.trimIndent()
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Get Profile")
        }

        OutlinedButton(
            enabled = !isBusy && isLoggedIn,

            onClick = {

                runTest {

                    val userId =
                        requireNotNull(SessionManager.userId)

                    val profiles =
                        RetrofitClient.api.getAccessibleProfiles()

                    val ownProfile =
                        profiles.any { it.id == userId }

                    val otherProfiles =
                        profiles.count { it.id != userId }

                    """
                    RLS Test Result

                    Profil sendiri tersedia: $ownProfile
                    Total profil terlihat: ${profiles.size}
                    Profil pengguna lain: $otherProfiles

                    ${if (ownProfile && otherProfiles == 0)
                        "PASS: Hanya profil sendiri yang terlihat."
                    else
                        "PERLU REVIEW: Periksa policy profiles."}
                    """.trimIndent()
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test RLS")
        }

        OutlinedButton(
            enabled = !isBusy && isLoggedIn,

            onClick = {

                runTest {

                    val previousUserId =
                        SessionManager.userId

                    val user =
                        repository.refreshSession()

                    check(user.id == previousUserId)

                    val verifiedUser =
                        repository.getCurrentUser()

                    check(verifiedUser.id == previousUserId)

                    "Refresh token berhasil!\nSesi berhasil diperbarui."
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Refresh Token")
        }

        OutlinedButton(

            enabled = !isBusy && isLoggedIn,

            onClick = {

                runTest {

                    val previousUserId =
                        SessionManager.userId

                    repository.ensureValidSession(
                        forceRefresh = true
                    )

                    val user = AuthenticatedRequest.execute {

                        RetrofitClient.authApi.getCurrentUser()

                    }

                    check(user.id == previousUserId)

                    """
            Session validation berhasil!

            Force refresh berhasil.
            Authenticated request berhasil.
            Identitas pengguna tetap sama.
            """.trimIndent()
                }
            },

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("Test Session Validation")

        }

        OutlinedButton(

            enabled = !isBusy && isLoggedIn,

            onClick = {

                runTest {

                    val userId =
                        requireNotNull(SessionManager.userId)

                    val users = coroutineScope {

                        List(5) {

                            async {

                                AuthenticatedRequest.execute {

                                    RetrofitClient.authApi
                                        .getCurrentUser()

                                }
                            }

                        }.awaitAll()
                    }

                    check(users.all {
                        it.id == userId
                    })

                    """
            Concurrent request berhasil!

            Total request: ${users.size}
            Semua identitas pengguna sesuai.
            """.trimIndent()
                }
            },

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("Test Concurrent Requests")

        }

        Button(
            enabled = !isBusy && isLoggedIn,

            onClick = {

                runTest {

                    repository.logout()

                    check(!SessionManager.hasSession)
                    check(SessionManager.accessToken == null)
                    check(SessionManager.refreshToken == null)

                    "Logout berhasil!\nSesi lokal sudah dihapus."
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Logout")
        }

        if (isBusy) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        HorizontalDivider()

        Text(
            text = "Test Result",
            style = MaterialTheme.typography.titleMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = result,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}