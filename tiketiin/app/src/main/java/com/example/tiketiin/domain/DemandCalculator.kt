package com.example.tiketiin.domain

import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.EventDemandStats
import java.time.LocalDate
import kotlin.math.roundToInt

// Hasil perhitungan Hot Opportunity untuk satu event.
data class HotOpportunity(
    val event: Event,
    val demandScore: Int,
    val activeSeekers: Int,
    val availableStock: Int,
    val sellChancePercent: Int,
    val isHighDemand: Boolean,
    val isLowStock: Boolean
) {
    val stockLabel: String
        get() = if (isLowStock) "<${DemandCalculator.LOW_STOCK_LIMIT}" else availableStock.toString()
}

object DemandCalculator {

    const val WEIGHT_SOLD = 0.5
    const val WEIGHT_WISHLIST = 0.3
    const val WEIGHT_VIEW = 0.2
    const val HIGH_DEMAND_THRESHOLD = 50
    const val LOW_STOCK_LIMIT = 20

    fun calculate(
        stats: List<EventDemandStats>,
        today: LocalDate
    ): List<HotOpportunity> {
        val upcoming = stats.filter { !it.event.date.isBefore(today) }
        if (upcoming.isEmpty()) return emptyList()

        val maxSold = upcoming.maxOf { it.soldCount }.coerceAtLeast(1)
        val maxWishlist = upcoming.maxOf { it.wishlistCount }.coerceAtLeast(1)
        val maxView = upcoming.maxOf { it.viewCount }.coerceAtLeast(1)

        return upcoming
            .map { item ->
                val sold = item.soldCount.toDouble() / maxSold
                val wishlist = item.wishlistCount.toDouble() / maxWishlist
                val view = item.viewCount.toDouble() / maxView

                val score = (100 * (WEIGHT_SOLD * sold + WEIGHT_WISHLIST * wishlist + WEIGHT_VIEW * view))
                    .roundToInt()
                    .coerceIn(0, 100)

                val seekers = item.wishlistCount + item.viewsLast7Days

                HotOpportunity(
                    event = item.event,
                    demandScore = score,
                    activeSeekers = seekers,
                    availableStock = item.availableStock,
                    sellChancePercent = sellChance(seekers, item.availableStock),
                    isHighDemand = score >= HIGH_DEMAND_THRESHOLD,
                    isLowStock = item.availableStock < LOW_STOCK_LIMIT
                )
            }
            .sortedWith(
                compareByDescending<HotOpportunity> { it.demandScore }
                    .thenBy { it.event.date }
            )
    }

    fun highDemandOnly(
        stats: List<EventDemandStats>,
        today: LocalDate
    ): List<HotOpportunity> {
        return calculate(stats, today).filter { it.isHighDemand }
    }

    // Peluang laku = pencari / (pencari + stok). Makin banyak pencari dibanding stok, makin besar.
    fun sellChance(seekers: Int, stock: Int): Int {
        if (seekers <= 0) return 0
        val chance = 100.0 * seekers / (seekers + stock.coerceAtLeast(0))
        return chance.roundToInt().coerceIn(0, 99)
    }
}
