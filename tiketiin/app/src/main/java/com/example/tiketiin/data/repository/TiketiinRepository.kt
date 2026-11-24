package com.example.tiketiin.data.repository

import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventDemandStats
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.model.Hotel
import com.example.tiketiin.data.model.ListingStatus
import com.example.tiketiin.data.model.Order
import com.example.tiketiin.data.model.OrderStatus
import com.example.tiketiin.data.model.TicketListing
import com.example.tiketiin.data.model.User
import com.example.tiketiin.data.source.DummyDataSource
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.tiketiin.data.remote.HotelRemoteDataSource
import com.example.tiketiin.data.mapper.toHotelModels
import com.example.tiketiin.data.auth.AuthenticatedRequest
import com.example.tiketiin.data.remote.EventRemoteDataSource
import com.example.tiketiin.data.mapper.toModel
import com.example.tiketiin.data.remote.OrderRemoteDataSource
import com.example.tiketiin.data.local.HotelLocalDataSource
import com.example.tiketiin.data.mapper.toEntity
import com.example.tiketiin.data.mapper.toHotelModelsFromLocal
import kotlinx.coroutines.CancellationException
import com.example.tiketiin.data.local.OrderLocalDataSource
import com.example.tiketiin.data.mapper.toOrderModelsFromLocal
import com.example.tiketiin.data.mapper.toEntity
import com.example.tiketiin.data.remote.dto.CreateHotelRequestDto
import com.example.tiketiin.data.remote.dto.UpdateHotelRequestDto

/**
 * Satu-satunya pintu data untuk semua ViewModel (pola sama dengan StudentRepository).
 * Semua fungsi dibuat suspend supaya nanti bisa langsung diganti panggilan jaringan (Supabase).
 */
interface TiketiinRepository {
    // Akun
    suspend fun getUser(): User
    suspend fun login()
    suspend fun logout()
    suspend fun verifyKyc()

    // Event
    suspend fun getEventSummaries(): List<EventSummary>
    suspend fun getEventSummary(eventId: Int): EventSummary?
    suspend fun searchEvents(query: String, category: EventCategory?): List<EventSummary>
    suspend fun recordView(eventId: Int)
    suspend fun toggleSaved(eventId: Int)

    // Kelola Event (seller)
    suspend fun getMyEvents(): List<Event>
    suspend fun saveEvent(event: Event)
    suspend fun deleteEvent(eventId: Int)

    // Listing tiket
    suspend fun getListings(eventId: Int): List<TicketListing>
    suspend fun getCheapestListing(eventId: Int): TicketListing?
    suspend fun getListing(listingId: Int): TicketListing?
    suspend fun getMyListings(): List<TicketListing>
    suspend fun addListing(listing: TicketListing): Int
    suspend fun updateListingPrice(listingId: Int, price: Long)
    suspend fun deleteListing(listingId: Int)
    suspend fun getPriceHistory(eventId: Int): List<Long>

    // Hot Opportunity
    suspend fun getDemandStats(): List<EventDemandStats>

    // Pesanan
    suspend fun createOrder(
        listingId: Int,
        quantity: Int,
        buyerName: String,
        buyerEmail: String,
        buyerPhone: String,
        paymentMethod: String
    ): Order

    suspend fun payOrder(
        orderId: Int,
        paymentMethod: String
    ): Order
    suspend fun confirmTicket(orderId: Int): Order
    suspend fun validateTicket(qrCode: String): String
    suspend fun getOrder(orderId: Int): Order?
    suspend fun getMyOrders(): List<Order>
    suspend fun getStoreOrders(): List<Order>
    suspend fun updateOrderStatus(orderId: Int, status: OrderStatus)
    suspend fun rateOrder(orderId: Int, rating: Int)

    // Hotel
    suspend fun getHotels(eventId: Int): List<Hotel>

    // CRUD Hotel
    suspend fun getAllHotels(): List<Hotel>

    suspend fun createHotel(
        request: CreateHotelRequestDto
    ): Hotel

    suspend fun updateHotel(
        hotelId: Int,
        request: UpdateHotelRequestDto
    ): Hotel

    suspend fun deleteHotel(hotelId: Int)

}

class DefaultTiketiinRepository(
    private val source: DummyDataSource,
    private val hotelRemoteDataSource: HotelRemoteDataSource = HotelRemoteDataSource(),
    private val eventRemoteDataSource: EventRemoteDataSource = EventRemoteDataSource(),
    private val orderRemoteDataSource: OrderRemoteDataSource = OrderRemoteDataSource(),
    private val hotelLocalDataSource: HotelLocalDataSource? = null,
    private val orderLocalDataSource: OrderLocalDataSource? = null
) : TiketiinRepository {

    // Meniru jeda jaringan supaya state loading terlihat saat demo.
    private suspend fun fakeNetwork() = delay(350)

    private val me get() = DummyDataSource.CURRENT_USER_ID

    override suspend fun getUser(): User = source.user

    override suspend fun login() {
        fakeNetwork()
        source.isLoggedIn = true
    }

    override suspend fun logout() {
        source.isLoggedIn = false
    }

    override suspend fun verifyKyc() {
        fakeNetwork()
        source.user = source.user.copy(isKycVerified = true)
    }

    private fun toSummary(event: Event): EventSummary {
        val active = source.activeListings(event.id)
        return EventSummary(
            event = event,
            lowestPrice = active.minOfOrNull { it.price } ?: 0L,
            ticketCount = active.sumOf { it.quantity },
            isSaved = event.id in source.savedEventIds
        )
    }

    override suspend fun getEventSummaries(): List<EventSummary> {
        fakeNetwork()
        return source.events.map { toSummary(it) }
    }

    override suspend fun getEventSummary(eventId: Int): EventSummary? {
        return source.events.find { it.id == eventId }?.let { toSummary(it) }
    }

    override suspend fun searchEvents(query: String, category: EventCategory?): List<EventSummary> {
        fakeNetwork()
        val keywords = query.trim().lowercase().split(" ").filter { it.isNotBlank() }
        return source.events
            .filter { event ->
                val text = "${event.name} ${event.city} ${event.venue} ${event.category.label}".lowercase()
                val matchQuery = keywords.isEmpty() || keywords.any { text.contains(it) }
                val matchCategory = category == null || event.category == category
                matchQuery && matchCategory
            }
            .map { toSummary(it) }
    }

    override suspend fun recordView(eventId: Int) {
        val index = source.events.indexOfFirst { it.id == eventId }
        if (index >= 0) {
            val event = source.events[index]
            source.events[index] = event.copy(
                viewCount = event.viewCount + 1,
                viewsLast7Days = event.viewsLast7Days + 1
            )
        }
    }

    override suspend fun toggleSaved(eventId: Int) {
        if (!source.savedEventIds.add(eventId)) {
            source.savedEventIds.remove(eventId)
        }
    }

    override suspend fun getMyEvents(): List<Event> {
        fakeNetwork()
        return source.events.filter { it.createdBy == me }
    }

    override suspend fun saveEvent(event: Event) {
        val index = source.events.indexOfFirst { it.id == event.id }
        if (index >= 0) {
            source.events[index] = event
        } else {
            source.events.add(event.copy(id = source.nextEventId(), createdBy = me))
        }
    }

    override suspend fun deleteEvent(eventId: Int) {
        source.events.removeAll { it.id == eventId }
        source.listings.removeAll { it.eventId == eventId }
    }

    override suspend fun getListings(eventId: Int): List<TicketListing> {
        return AuthenticatedRequest.execute {
            val event = eventRemoteDataSource.getEventWithListings(eventId)

            event?.ticketListings
                .orEmpty()
                .filter {
                    it.status == "AKTIF" && it.quantity > 0
                }
                .map { it.toModel() }
        }
    }

    override suspend fun getCheapestListing(eventId: Int): TicketListing? {
        return getListings(eventId).minByOrNull { it.price }
    }

    override suspend fun getListing(listingId: Int): TicketListing? {
        return source.listings.find { it.id == listingId }
    }

    override suspend fun getMyListings(): List<TicketListing> {
        fakeNetwork()
        return source.listings.filter { it.sellerId == me }
    }

    override suspend fun addListing(listing: TicketListing): Int {
        fakeNetwork()
        val id = source.nextListingId()
        source.listings.add(listing.copy(id = id, sellerId = me))
        return id
    }

    override suspend fun updateListingPrice(listingId: Int, price: Long) {
        val index = source.listings.indexOfFirst { it.id == listingId }
        if (index >= 0) {
            source.listings[index] = source.listings[index].copy(price = price)
        }
    }

    override suspend fun deleteListing(listingId: Int) {
        source.listings.removeAll { it.id == listingId }
    }

    override suspend fun getPriceHistory(eventId: Int): List<Long> {
        return source.priceHistory[eventId] ?: emptyList()
    }

    override suspend fun getDemandStats(): List<EventDemandStats> {
        fakeNetwork()
        return source.events.map { event ->
            // Terjual = angka dasar + tiket dari pesanan yang sudah dibayar di aplikasi.
            val soldInApp = source.orders
                .filter { it.eventId == event.id && it.status != OrderStatus.BELUM_BAYAR }
                .sumOf { it.quantity }
            val savedByMe = if (event.id in source.savedEventIds) 1 else 0
            EventDemandStats(
                event = event,
                soldCount = event.baseSold + soldInApp,
                wishlistCount = event.baseWishlist + savedByMe,
                viewCount = event.viewCount,
                viewsLast7Days = event.viewsLast7Days,
                availableStock = source.activeListings(event.id).sumOf { it.quantity }
            )
        }
    }


    override suspend fun createOrder(
        listingId: Int,
        quantity: Int,
        buyerName: String,
        buyerEmail: String,
        buyerPhone: String,
        paymentMethod: String
    ): Order {
        return AuthenticatedRequest.execute {
            val orderId = orderRemoteDataSource.createOrder(
                listingId = listingId,
                quantity = quantity,
                buyerName = buyerName,
                buyerEmail = buyerEmail,
                buyerPhone = buyerPhone,
                paymentMethod = paymentMethod
            )

            val order = orderRemoteDataSource.getOrderById(orderId)
                ?: error("Pesanan berhasil dibuat, tetapi detail pesanan gagal diambil.")

            val model = order.toModel()

            orderLocalDataSource?.saveOrder(model.toEntity())

            model
        }
    }


    override suspend fun payOrder(
        orderId: Int,
        paymentMethod: String
    ): Order {
        return AuthenticatedRequest.execute {
            val result = orderRemoteDataSource.payOrder(
                orderId = orderId,
                paymentMethod = paymentMethod
            )

            check(result == "PAID") {
                "Pembayaran gagal diproses."
            }

            val order = orderRemoteDataSource.getOrderById(orderId)
                ?: error("Pembayaran berhasil, tetapi detail pesanan gagal diambil.")

            val model = order.toModel()

            orderLocalDataSource?.saveOrder(model.toEntity())

            model
        }
    }

    override suspend fun confirmTicket(orderId: Int): Order {
        return AuthenticatedRequest.execute {
            val result = orderRemoteDataSource.confirmTicket(orderId)

            check(result == "COMPLETED") {
                "Konfirmasi tiket gagal diproses."
            }

            val orderDto = orderRemoteDataSource.getOrderById(orderId)
                ?: error("Konfirmasi berhasil, tetapi detail pesanan gagal diambil.")

            val order = orderDto.toModel()

            orderLocalDataSource?.saveOrder(order.toEntity())

            order
        }
    }

    override suspend fun validateTicket(qrCode: String): String {
        return orderRemoteDataSource.validateTicket(qrCode)
    }

    override suspend fun getOrder(orderId: Int): Order? {
        return AuthenticatedRequest.execute {
            orderRemoteDataSource
                .getOrderById(orderId)
                ?.toModel()
        }
    }


    override suspend fun getMyOrders(): List<Order> {
        val local = orderLocalDataSource

        return try {
            val remoteOrders = AuthenticatedRequest.execute {
                orderRemoteDataSource
                    .getBuyerOrders(me)
                    .map { it.toModel() }
            }

            if (local != null) {
                local.replaceBuyerOrders(
                    buyerId = me,
                    orders = remoteOrders.map { it.toEntity() }
                )
            }

            remoteOrders

        } catch (error: CancellationException) {
            throw error

        } catch (remoteError: Exception) {
            if (local == null) throw remoteError

            val cachedOrders = local.getBuyerOrders(me)

            if (cachedOrders.isEmpty()) {
                throw remoteError
            }

            cachedOrders.toOrderModelsFromLocal()
        }
    }



    override suspend fun getStoreOrders(): List<Order> {
        val local = orderLocalDataSource

        return try {
            val remoteOrders = AuthenticatedRequest.execute {
                orderRemoteDataSource
                    .getSellerOrders(me)
                    .map { it.toModel() }
            }

            if (local != null) {
                local.replaceSellerOrders(
                    sellerId = me,
                    orders = remoteOrders.map { it.toEntity() }
                )
            }

            remoteOrders

        } catch (error: CancellationException) {
            throw error

        } catch (remoteError: Exception) {
            if (local == null) throw remoteError

            val cachedOrders = local.getSellerOrders(me)

            if (cachedOrders.isEmpty()) {
                throw remoteError
            }

            cachedOrders.toOrderModelsFromLocal()
        }
    }


    override suspend fun updateOrderStatus(orderId: Int, status: OrderStatus) {
        val index = source.orders.indexOfFirst { it.id == orderId }
        if (index >= 0) {
            source.orders[index] = source.orders[index].copy(status = status)
        }
    }

    override suspend fun rateOrder(orderId: Int, rating: Int) {
        val index = source.orders.indexOfFirst { it.id == orderId }
        if (index >= 0) {
            source.orders[index] = source.orders[index].copy(rating = rating)
        }
    }


    override suspend fun getHotels(eventId: Int): List<Hotel> {
        val localDataSource = hotelLocalDataSource

        return try {
            val remoteHotels = AuthenticatedRequest.execute {
                hotelRemoteDataSource
                    .getAvailableHotels(eventId)
                    .toHotelModels()
            }

            // Simpan snapshot hotel berdasarkan event.
            if (localDataSource != null) {
                val entities = remoteHotels.map { hotel ->
                    hotel.toEntity(eventId)
                }

                localDataSource.clearHotels(eventId)

                if (entities.isNotEmpty()) {
                    localDataSource.saveHotels(entities)
                }
            }

            remoteHotels

        } catch (error: CancellationException) {
            throw error

        } catch (remoteError: Exception) {

            if (localDataSource == null) {
                throw remoteError
            }

            val lastCachedAt =
                localDataSource.getLastCachedAt(eventId)

            val cacheAge =
                lastCachedAt?.let { System.currentTimeMillis() - it }

            // Cache hanya digunakan jika berumur maksimal 15 menit.
            val isCacheFresh =
                cacheAge != null &&
                        cacheAge >= 0 &&
                        cacheAge <= 15 * 60 * 1000L

            if (!isCacheFresh) {
                throw remoteError
            }

            val cachedHotels =
                localDataSource.getHotels(eventId)
                    .toHotelModelsFromLocal()

            if (cachedHotels.isEmpty()) {
                throw remoteError
            }

            cachedHotels
        }
    }


    override suspend fun getAllHotels(): List<Hotel> {
        return AuthenticatedRequest.execute {
            hotelRemoteDataSource
                .getAllHotels()
                .toHotelModels()
        }
    }


    override suspend fun createHotel(
        request: CreateHotelRequestDto
    ): Hotel {
        return AuthenticatedRequest.execute {
            val hotel = hotelRemoteDataSource
                .createHotel(request)
                .let { listOf(it).toHotelModels().first() }

            // Bersihkan cache setelah hotel berhasil dibuat.
            hotelLocalDataSource?.clearAllHotels()

            hotel
        }
    }

    override suspend fun updateHotel(
        hotelId: Int,
        request: UpdateHotelRequestDto
    ): Hotel {
        return AuthenticatedRequest.execute {
            val hotel = hotelRemoteDataSource
                .updateHotel(hotelId, request)
                .let { listOf(it).toHotelModels().first() }

            // Bersihkan cache setelah hotel berhasil diperbarui.
            hotelLocalDataSource?.clearAllHotels()

            hotel
        }
    }

    override suspend fun deleteHotel(hotelId: Int) {
        AuthenticatedRequest.execute {
            hotelRemoteDataSource.deleteHotel(hotelId)

            // Bersihkan cache setelah hotel berhasil dihapus.
            hotelLocalDataSource?.clearAllHotels()
        }
    }
}
