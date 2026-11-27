package com.example.tiketiin.ui.sell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.repository.TiketiinRepository
import com.example.tiketiin.domain.DemandCalculator
import com.example.tiketiin.domain.HotOpportunity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

data class HotOpportunityUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Semua event yang akan datang, sudah diberi skor dan diurutkan.
    val allEvents: List<HotOpportunity> = emptyList(),
    // Filter pada layar "Lihat Semua".
    val highDemandOnly: Boolean = true
) {
    // Hanya yang skornya melewati threshold.
    val highDemand: List<HotOpportunity> get() = allEvents.filter { it.isHighDemand }

    // Rekomendasi nomor 1, tampil sebagai kartu besar di tab Jual Tiket.
    val topOpportunity: HotOpportunity? get() = highDemand.firstOrNull()

    val visibleList: List<HotOpportunity> get() = if (highDemandOnly) highDemand else allEvents
}


class HotOpportunityViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HotOpportunityUiState())
    val uiState: StateFlow<HotOpportunityUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.allEvents.isEmpty(), errorMessage = null) }
            try {
                val stats = repository.getRemoteDemandStats()
                val result = withContext(Dispatchers.Default) {
                    DemandCalculator.calculate(stats, LocalDate.now())
                }
                _uiState.update { it.copy(isLoading = false, allEvents = result) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat Hot Opportunity")
                }
            }
        }
    }

    fun toggleFilter() {
        _uiState.update { it.copy(highDemandOnly = !it.highDemandOnly) }
    }
}
