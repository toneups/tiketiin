
package com.example.tiketiin.data.remote

import com.example.tiketiin.data.remote.api.AuthApiService
import com.example.tiketiin.data.remote.api.SupabaseApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile
    private var accessToken: String? = null

    fun setAccessToken(token: String?) {
        accessToken = token?.takeIf { it.isNotBlank() }
    }

    fun clearAccessToken() {
        accessToken = null
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->

                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header(
                        "apikey",
                        SupabaseConfig.PUBLISHABLE_KEY
                    )

                // Endpoint autentikasi tidak menggunakan access token lama.
                val isAuthEndpoint =
                    originalRequest.url.encodedPath.startsWith("/auth/v1/")

                if (!isAuthEndpoint) {
                    accessToken?.let { token ->
                        requestBuilder.header(
                            "Authorization",
                            "Bearer $token"
                        )
                    }
                }

                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SupabaseConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    val api: SupabaseApiService by lazy {
        retrofit.create(SupabaseApiService::class.java)
    }

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}
