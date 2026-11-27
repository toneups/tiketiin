package com.example.tiketiin.domain

import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.model.Hotel
import java.time.LocalDate
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// Catatan untuk tim: file ini berisi versi dasar logika Fungsi 1, 3, dan 4
// supaya tampilannya bisa berjalan. Pemilik tiap fungsi bebas menyempurnakannya.

// ---------------------------------------------------------------------------
// FUNGSI 1 - Deteksi Harga Tidak Wajar dan Rekomendasi Harga Wajar
// ---------------------------------------------------------------------------

enum class PriceStatus(val label: String) {
    WAJAR("Wajar"),
    PERLU_DITINJAU("Perlu ditinjau"),
    TERLALU_TINGGI("Terlalu tinggi")
}

data class FairPriceResult(
    val faceValue: Long,
    val sellPrice: Long,
    val markupPercent: Double,        // markup harga jual terhadap face value
    val rangeMin: Long,
    val rangeMax: Long,
    val recommendedPrice: Long,
    val overRangePercent: Double,     // berapa % di atas batas wajar (0 kalau masih wajar)
    val status: PriceStatus,
    val usedHistory: Boolean
)

object FairPriceCalculator {
    const val MIN_MARKUP = 0.0
    const val MAX_MARKUP = 0.22
    const val RECOMMENDED_MARKUP = 0.15
    const val REVIEW_MARKUP = 0.22
    const val REJECT_MARKUP = 0.50

    fun check(faceValue: Long, sellPrice: Long, history: List<Long>): FairPriceResult {
        // Rentang wajar: dari histori tiket sejenis kalau ada, kalau tidak dari markup 10%-45%.
        val defaultMin = roundTo(faceValue * (1 + MIN_MARKUP), 10_000)
        val defaultMax = roundTo(faceValue * (1 + MAX_MARKUP), 10_000)
        val useHistory = history.size >= 3
        val rangeMin = if (useHistory) history.min() else defaultMin
        val rangeMax = if (useHistory) history.max() else defaultMax

        val markup = if (faceValue > 0) (sellPrice - faceValue).toDouble() / faceValue else 0.0
        val over = if (sellPrice > rangeMax && rangeMax > 0) {
            (sellPrice - rangeMax).toDouble() / rangeMax * 100
        } else {
            0.0
        }

        val status = when {
            markup > REJECT_MARKUP -> PriceStatus.TERLALU_TINGGI
            markup > REVIEW_MARKUP || sellPrice > rangeMax -> PriceStatus.PERLU_DITINJAU
            else -> PriceStatus.WAJAR
        }

        val recommended = roundTo(faceValue * (1 + RECOMMENDED_MARKUP), 10_000)
            .coerceIn(rangeMin, rangeMax)

        return FairPriceResult(
            faceValue = faceValue,
            sellPrice = sellPrice,
            markupPercent = markup * 100,
            rangeMin = rangeMin,
            rangeMax = rangeMax,
            recommendedPrice = recommended,
            overRangePercent = over,
            status = status,
            usedHistory = useHistory
        )
    }

    private fun roundTo(value: Double, step: Long): Long {
        return (value / step).roundToInt() * step
    }
}

// ---------------------------------------------------------------------------
// FUNGSI 3 - Rekomendasi Event Personalisasi (For You)
// ---------------------------------------------------------------------------
object RecommendationCalculator {

    fun recommend(
        candidates: List<EventSummary>,
        purchasedCategories: List<EventCategory>,
        savedCategories: List<EventCategory>,
        purchasedEventIds: Set<Int>,
        userCity: String,
        today: LocalDate,
        nearbyEventIds: Set<Int>? = null
    ): List<EventSummary> {
        val active = candidates.filter {
            !it.event.date.isBefore(today) &&
                    it.event.id !in purchasedEventIds &&
                    it.ticketCount > 0
        }

        return active.sortedWith(
            compareByDescending<EventSummary> {
                matchScore(
                    event = it.event,
                    purchasedCategories = purchasedCategories,
                    savedCategories = savedCategories,
                    userCity = userCity,
                    nearbyEventIds = nearbyEventIds
                )
            }
                .thenByDescending { it.event.viewCount }
                .thenBy { it.event.date }
                .thenBy { it.event.id }
        )
    }

    fun matchScore(
        event: Event,
        purchasedCategories: List<EventCategory>,
        savedCategories: List<EventCategory>,
        userCity: String,
        nearbyEventIds: Set<Int>? = null
    ): Double {
        val bought = purchasedCategories.count {
            it == event.category
        } * 3.0

        val saved = savedCategories.count {
            it == event.category
        } * 2.0

        val sameCity = if (nearbyEventIds != null) {
            event.id in nearbyEventIds
        } else {
            LocationMatcher.isSameCity(
                profileCity = userCity,
                eventCity = event.city
            )
        }

        val near = if (sameCity) 2.0 else 0.0

        val popular = event.viewCount.coerceAtLeast(0) / 1000.0

        return bought + saved + near + popular
    }
}


// ---------------------------------------------------------------------------
// FUNGSI 4 - Rekomendasi Hotel Terdekat
// ---------------------------------------------------------------------------


data class NearbyHotel(
    val hotel: Hotel,
    val distanceKm: Double
)

object HotelFinder {
    const val FIRST_RADIUS_KM = 5.0
    const val SECOND_RADIUS_KM = 10.0
    const val MAX_RESULT = 5

    fun findNearby(
        venueLat: Double,
        venueLon: Double,
        hotels: List<Hotel>
    ): List<NearbyHotel> {

        require(venueLat.isFinite() && venueLat in -90.0..90.0) {
            "Latitude lokasi event tidak valid."
        }

        require(venueLon.isFinite() && venueLon in -180.0..180.0) {
            "Longitude lokasi event tidak valid."
        }

        val withDistance = hotels
            .filter {
                it.availableRooms > 0 &&
                        it.latitude.isFinite() &&
                        it.longitude.isFinite() &&
                        it.latitude in -90.0..90.0 &&
                        it.longitude in -180.0..180.0
            }
            .map {
                NearbyHotel(
                    hotel = it,
                    distanceKm = haversineKm(
                        venueLat,
                        venueLon,
                        it.latitude,
                        it.longitude
                    )
                )
            }

        val firstRadius = withDistance
            .filter { it.distanceKm <= FIRST_RADIUS_KM }
            .sortedBy { it.distanceKm }

        if (firstRadius.size >= MAX_RESULT) {
            return firstRadius.take(MAX_RESULT)
        }

        val secondRadius = withDistance
            .filter {
                it.distanceKm > FIRST_RADIUS_KM &&
                        it.distanceKm <= SECOND_RADIUS_KM
            }
            .sortedBy { it.distanceKm }

        return (firstRadius + secondRadius)
            .sortedBy { it.distanceKm }
            .take(MAX_RESULT)
    }

    fun haversineKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val safeA = a.coerceIn(0.0, 1.0)

        return 2 * earthRadius * asin(sqrt(safeA))
    }
}
