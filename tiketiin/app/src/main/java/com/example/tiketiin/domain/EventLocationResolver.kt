
package com.example.tiketiin.domain

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.tiketiin.data.remote.dto.CityOptionDto
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class EventLocationResolver(context: Context) {

    private val geocoder = Geocoder(
        context.applicationContext,
        Locale("id", "ID")
    )

    suspend fun resolve(
        latitude: Double,
        longitude: Double,
        cityOptions: List<CityOptionDto>
    ): String? {
        if (
            !latitude.isFinite() ||
            !longitude.isFinite() ||
            latitude !in -90.0..90.0 ||
            longitude !in -180.0..180.0 ||
            cityOptions.isEmpty() ||
            !Geocoder.isPresent()
        ) {
            return null
        }

        val address = try {
            withTimeoutOrNull(5000L) {
                getAddress(latitude, longitude)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } ?: return null

        val possibleNames = listOfNotNull(
            address.subAdminArea,
            address.locality,
            address.adminArea
        )

        for (name in possibleNames) {
            val matches = cityOptions.filter { option ->
                normalize(name) == normalize(option.displayName)
            }

            if (matches.size == 1) {
                return matches.first().displayName
            }
        }

        return null
    }

    private suspend fun getAddress(
        latitude: Double,
        longitude: Double
    ): Address? = withContext(Dispatchers.IO) {
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(
                            addresses: MutableList<Address>
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(
                                    addresses.firstOrNull()
                                )
                            }
                        }

                        override fun onError(
                            errorMessage: String?
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        }
                    }
                )
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(
                latitude,
                longitude,
                1
            )?.firstOrNull()
        }
    }

    private fun normalize(value: String): String {
        return value
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
            .replace(Regex("^kab\\.?\\s+"), "kabupaten ")
            .replace(Regex("^kot\\.?\\s+"), "kota ")
    }
}