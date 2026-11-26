
package com.example.tiketiin.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.Order
import com.example.tiketiin.data.model.OrderStatus
import com.example.tiketiin.data.repository.TiketiinRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class OrderItem(
    val order: Order,
    val event: Event?,
    val daysToEvent: Long
)

data class OrdersUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isConfirming: Boolean = false,
    val isRefunding: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
    val selectedTab: Int = 0,
    val selectedStatus: OrderStatus = OrderStatus.DIPROSES,
    val myOrders: List<OrderItem> = emptyList(),
    val storeOrders: List<OrderItem> = emptyList()
) {
    val visibleOrders: List<OrderItem>
        get() {
            val source = if (selectedTab == 0) myOrders else storeOrders
            return source.filter {
                it.order.status == selectedStatus
            }
        }

    fun countFor(status: OrderStatus): Int {
        val source = if (selectedTab == 0) myOrders else storeOrders
        return source.count {
            it.order.status == status
        }
    }
}

class OrdersViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            refreshOrders()
        }
    }

    private suspend fun refreshOrders() {
        val hasData = _uiState.value.myOrders.isNotEmpty() ||
                _uiState.value.storeOrders.isNotEmpty()

        _uiState.update {
            it.copy(
                isLoading = !hasData,
                isRefreshing = hasData,
                error = null
            )
        }

        try {
            val myOrders = repository.getMyOrders().map {
                toItem(it)
            }

            val storeOrders = repository.getStoreOrders().map {
                toItem(it)
            }

            _uiState.update {
                it.copy(
                    myOrders = myOrders,
                    storeOrders = storeOrders
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = e.message ?: "Gagal memuat pesanan."
                )
            }
        } finally {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false
                )
            }
        }
    }

    private suspend fun toItem(order: Order): OrderItem {
        val event = repository
            .getEventSummary(order.eventId)
            ?.event

        val days = if (event != null) {
            ChronoUnit.DAYS.between(
                LocalDate.now(),
                event.date
            )
        } else {
            0L
        }

        return OrderItem(
            order = order,
            event = event,
            daysToEvent = days
        )
    }

    fun selectTab(tab: Int) {
        if (tab !in 0..1) return

        _uiState.update {
            it.copy(selectedTab = tab)
        }
    }

    fun selectStatus(status: OrderStatus) {
        _uiState.update {
            it.copy(selectedStatus = status)
        }
    }

    fun rate(orderId: Int, rating: Int) {
        if (rating !in 1..5) return

        viewModelScope.launch {
            try {
                repository.rateOrder(orderId, rating)
                refreshOrders()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Gagal memberikan penilaian."
                    )
                }
            }
        }
    }

    fun confirmTicket(orderId: Int) {
        if (_uiState.value.isConfirming) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isConfirming = true,
                    error = null
                )
            }

            try {
                repository.confirmTicket(orderId)
                refreshOrders()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Gagal mengonfirmasi tiket."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isConfirming = false)
                }
            }
        }
    }

    fun refundOrder(orderId: Int) {
        if (orderId <= 0 || _uiState.value.isRefunding) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRefunding = true,
                    error = null,
                    successMessage = null
                )
            }

            try {
                repository.refundOrder(orderId)

                refreshOrders()

                _uiState.update {
                    it.copy(
                        successMessage = "Refund berhasil diproses."
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Gagal memproses refund."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isRefunding = false)
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update {
            it.copy(
                error = null,
                successMessage = null
            )
        }
    }
}

data class TicketUiState(
    val isLoading: Boolean = true,
    val item: OrderItem? = null,
    val error: String? = null
)

class TicketViewModel(
    private val repository: TiketiinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketUiState())
    val uiState: StateFlow<TicketUiState> = _uiState.asStateFlow()

    fun load(orderId: Int) {
        viewModelScope.launch {
            _uiState.value = TicketUiState(isLoading = true)

            try {
                val order = repository.getOrder(orderId)

                val event = order?.let {
                    repository.getEventSummary(it.eventId)?.event
                }

                val days = if (event != null) {
                    ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        event.date
                    )
                } else {
                    0L
                }

                _uiState.value = TicketUiState(
                    isLoading = false,
                    item = order?.let {
                        OrderItem(it, event, days)
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = TicketUiState(
                    isLoading = false,
                    error = e.message ?: "Gagal memuat detail tiket."
                )
            }
        }
    }
}
