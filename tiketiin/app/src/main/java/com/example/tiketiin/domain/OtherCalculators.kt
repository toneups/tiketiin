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

    /**
     * @param purchasedCategories kategori event yang pernah dibeli user
     * @param savedCategories kategori event yang disimpan user
     * @param purchasedEventIds event yang sudah dibeli (disaring dari hasil)
     */
    fun recommend(
        candidates: List<EventSummary>,
        purchasedCategories: List<EventCategory>,
        savedCategories: List<EventCategory>,
        purchasedEventIds: Set<Int>,
        userCity: String,
        today: LocalDate
    ): List<EventSummary> {
        val active = candidates.filter {
            !it.event.date.isBefore(today) && it.event.id !in purchasedEventIds && it.ticketCount > 0
        }
        val hasHistory = purchasedCategories.isNotEmpty() || savedCategories.isNotEmpty()

        // User baru: pakai event trending (paling banyak dilihat) sebagai fallback.
        if (!hasHistory) {
            return active.sortedByDescending { it.event.viewCount }
        }

        return active
            .map { it to matchScore(it.event, purchasedCategories, savedCategories, userCity) }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    fun matchScore(
        event: Event,
        purchasedCategories: List<EventCategory>,
        savedCategories: List<EventCategory>,
        userCity: String
    ): Double {
        val bought = purchasedCategories.count { it == event.category } * 3.0
        val saved = savedCategories.count { it == event.category } * 2.0
        val near = if (event.city.equals(userCity, ignoreCase = true)) 2.0 else 0.0
        val popular = event.viewCount / 1000.0
        return bought + saved + near + popular
    }
}

// ---------------------------------------------------------------------------
// FUNGSI 4 - Rekomendasi Hotel Terdekat
// ---------------------------------------------------------------------------

data class NearbyHotel(val hotel: Hotel, val distanceKm: Double)

object HotelFinder {
    const val FIRST_RADIUS_KM = 5.0
    const val SECOND_RADIUS_KM = 10.0
    const val MAX_RESULT = 5

    fun findNearby(venueLat: Double, venueLon: Double, hotels: List<Hotel>): List<NearbyHotel> {
        val withDistance = hotels
            .filter { it.availableRooms > 0 }
            .map { NearbyHotel(it, haversineKm(venueLat, venueLon, it.latitude, it.longitude)) }

        // Radius 5 km dulu; kalau kosong diperluas sekali ke 10 km.
        var result = withDistance.filter { it.distanceKm <= FIRST_RADIUS_KM }
        if (result.isEmpty()) {
            result = withDistance.filter { it.distanceKm <= SECOND_RADIUS_KM }
        }
        return result.sortedBy { it.distanceKm }.take(MAX_RESULT)
    }

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * earthRadius * asin(sqrt(a))
    }
}
