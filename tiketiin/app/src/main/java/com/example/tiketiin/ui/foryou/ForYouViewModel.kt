
package com.example.tiketiin.ui.foryou

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.remote.dto.CityOptionDto
import com.example.tiketiin.data.repository.EventCatalogRepository
import com.example.tiketiin.data.repository.ProfileRepository
import com.example.tiketiin.data.repository.PurchaseHistoryRepository
import com.example.tiketiin.data.repository.WishlistRepository
import com.example.tiketiin.domain.EventLocationResolver
import com.example.tiketiin.domain.LocationMatcher
import com.example.tiketiin.domain.RecommendationCalculator
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

data class ForYouUiState(
    val userName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0,
    val selectedCategory: EventCategory? = null,
    val recommended: List<EventSummary> = emptyList(),
    val nearby: List<EventSummary> = emptyList(),
    val isShowingCache: Boolean = false
) {
    val visibleEvents: List<EventSummary>
        get() {
            val source = if (selectedTab == 0) {
                recommended
            } else {
                nearby
            }

            return if (selectedCategory == null) {
                source
            } else {
                source.filter {
                    it.event.category == selectedCategory
                }
            }
        }
}

class ForYouViewModel(
    private val eventRepository: EventCatalogRepository,
    private val profileRepository: ProfileRepository,
    private val wishlistRepository: WishlistRepository,
    private val purchaseHistoryRepository: PurchaseHistoryRepository,
    private val eventLocationResolver: EventLocationResolver
) : ViewModel() {

    companion object {
        private const val CATALOG_REFRESH_INTERVAL_MS =
            5 * 60 * 1000L
    }

    private val _uiState = MutableStateFlow(ForYouUiState())

    val uiState: StateFlow<ForYouUiState> =
        _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var lastCatalogRefreshMs: Long? = null

    private val resolvedCityCache =
        mutableMapOf<Pair<Double, Double>, String>()

    private suspend fun findNearbyEvents(
        candidates: List<EventSummary>,
        userCity: String,
        cityOptions: List<CityOptionDto>
    ): List<EventSummary> = coroutineScope {
        if (userCity.isBlank()) {
            return@coroutineScope emptyList()
        }

        val geocodeSemaphore = Semaphore(3)

        candidates.map { summary ->
            async {
                val event = summary.event
                val eventCity = event.city.trim()

                val directMatch = LocationMatcher.isSameCity(
                    profileCity = userCity,
                    eventCity = eventCity
                )

                if (directMatch) {
                    return@async summary
                }

                val hasAdministrativeType = Regex(
                    "^(kota|kabupaten|kab\\.)\\s+",
                    RegexOption.IGNORE_CASE
                ).containsMatchIn(eventCity)

                if (
                    hasAdministrativeType ||
                    cityOptions.isEmpty()
                ) {
                    return@async null
                }

                val coordinates =
                    event.latitude to event.longitude

                val resolvedCity = geocodeSemaphore.withPermit {
                    resolvedCityCache[coordinates]
                        ?: eventLocationResolver.resolve(
                            latitude = event.latitude,
                            longitude = event.longitude,
                            cityOptions = cityOptions
                        )?.also { result ->
                            resolvedCityCache[coordinates] = result
                        }
                }

                val matches = resolvedCity != null &&
                        LocationMatcher.isSameCity(
                            profileCity = userCity,
                            eventCity = eventCity,
                            resolvedEventCity = resolvedCity
                        )

                if (matches) summary else null
            }
        }
            .awaitAll()
            .filterNotNull()
            .sortedWith(
                compareBy<EventSummary> { it.event.date }
                    .thenBy { it.event.id }
            )
    }

    fun load() {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val user = profileRepository.getMyProfile()
                val savedIds = wishlistRepository.getSavedEventIds()

                val purchasedEventIds =
                    purchaseHistoryRepository.getPurchasedEventIds()

                var allEvents = eventRepository.getCachedEvents()

                val now = SystemClock.elapsedRealtime()

                val shouldRefresh =
                    allEvents.isEmpty() ||
                            lastCatalogRefreshMs == null ||
                            now - (lastCatalogRefreshMs ?: 0L) >=
                            CATALOG_REFRESH_INTERVAL_MS

                var isShowingCache = false

                if (shouldRefresh) {
                    try {
                        eventRepository.refreshEvents()
                        allEvents = eventRepository.getCachedEvents()

                        lastCatalogRefreshMs =
                            SystemClock.elapsedRealtime()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (allEvents.isEmpty()) {
                            throw e
                        }

                        isShowingCache = true
                    }
                }

                val today = LocalDate.now()

                val purchasedCategories = allEvents
                    .filter {
                        it.event.id in purchasedEventIds
                    }
                    .map {
                        it.event.category
                    }

                val candidates = allEvents
                    .filter {
                        !it.event.date.isBefore(today) &&
                                it.ticketCount > 0 &&
                                it.event.id !in purchasedEventIds
                    }
                    .map { summary ->
                        summary.copy(
                            isSaved = summary.event.id in savedIds
                        )
                    }

                val savedCategories = allEvents
                    .filter {
                        it.event.id in savedIds
                    }
                    .map {
                        it.event.category
                    }

                val userCity = user.city.trim()

                val cityOptions = try {
                    profileRepository.getCityOptions()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    emptyList()
                }

                val nearby = findNearbyEvents(
                    candidates = candidates,
                    userCity = userCity,
                    cityOptions = cityOptions
                )

                val nearbyEventIds = nearby
                    .map { it.event.id }
                    .toSet()

                val recommended = if (
                    savedCategories.isEmpty() &&
                    purchasedCategories.isEmpty()
                ) {
                    candidates.sortedWith(
                        compareByDescending<EventSummary> {
                            it.event.viewCount
                        }
                            .thenBy { it.event.date }
                            .thenBy { it.event.id }
                    )
                } else {
                    RecommendationCalculator.recommend(
                        candidates = candidates,
                        purchasedCategories = purchasedCategories,
                        savedCategories = savedCategories,
                        purchasedEventIds = purchasedEventIds,
                        userCity = userCity,
                        today = today,
                        nearbyEventIds = nearbyEventIds
                    )
                }

                _uiState.update {
                    it.copy(
                        userName = user.name
                            .trim()
                            .substringBefore(" "),
                        isLoading = false,
                        errorMessage = null,
                        recommended = recommended,
                        nearby = nearby,
                        isShowingCache = isShowingCache
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = if (e is IOException) {
                    "Koneksi bermasalah. Periksa internet."
                } else {
                    "Gagal memuat rekomendasi. Coba lagi."
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = message,
                        recommended = emptyList(),
                        nearby = emptyList(),
                        isShowingCache = false
                    )
                }
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update {
            it.copy(
                selectedTab = tab.coerceIn(0, 1)
            )
        }
    }

    fun selectCategory(category: EventCategory?) {
        _uiState.update {
            it.copy(
                selectedCategory = category
            )
        }
    }
}
