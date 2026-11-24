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
import com.example.tiketiin.data.auth.AuthenticatedRequest
import com.example.tiketiin.data.remote.RetrofitClient
import com.example.tiketiin.data.remote.dto.PriceHistoryRequest
import kotlinx.coroutines.CancellationException
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.remote.dto.CreateListingRequest
import com.example.tiketiin.data.remote.dto.ListingDto
import com.example.tiketiin.data.remote.dto.UpdateListingRequest

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
    suspend fun getCheapestListing(eventId: Int): TicketListing?
    suspend fun getListing(listingId: Int): TicketListing?
    suspend fun getMyListings(): List<TicketListing>
    suspend fun addListing(listing: TicketListing): Int
    suspend fun updateListingPrice(listingId: Int, price: Long)
    suspend fun deleteListing(listingId: Int)
    suspend fun getPriceHistory(eventId: Int, seatCategory: String): List<Long>

    // Hot Opportunity
    suspend fun getDemandStats(): List<EventDemandStats>

    // Pesanan
    suspend fun createOrder(listingId: Int, quantity: Int, buyerName: String, paymentMethod: String): Order
    suspend fun getOrder(orderId: Int): Order?
    suspend fun getMyOrders(): List<Order>
    suspend fun getStoreOrders(): List<Order>
    suspend fun updateOrderStatus(orderId: Int, status: OrderStatus)
    suspend fun rateOrder(orderId: Int, rating: Int)

    // Hotel
    suspend fun getHotels(): List<Hotel>
}

class DefaultTiketiinRepository(
    private val source: DummyDataSource
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

    override suspend fun getCheapestListing(eventId: Int): TicketListing? {
        return source.activeListings(eventId).minByOrNull { it.price }
    }

    override suspend fun getListing(listingId: Int): TicketListing? {
        // Coba ambil dari Supabase dulu (listing yang baru diterbitkan).
        val remote = try {
            AuthenticatedRequest.execute {
                RetrofitClient.api.getListingById(id = "eq.$listingId")
            }.firstOrNull()?.toModel()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        // Kalau tidak ketemu, pakai data dummy seperti sebelumnya.
        return remote ?: source.listings.find { it.id == listingId }
    }

    private fun ListingDto.toModel() = TicketListing(
        id = id,
        eventId = eventId,
        sellerId = sellerId,
        seatCategory = seatCategory,
        row = seatRow,
        faceValue = faceValue,
        price = price,
        quantity = quantity,
        status = runCatching { ListingStatus.valueOf(status) }
            .getOrDefault(ListingStatus.AKTIF),
        views = views,
        saves = saves,
        offers = offers
    )

    override suspend fun getMyListings(): List<TicketListing> {
        val sellerId = SessionManager.userId
            ?: return source.listings.filter { it.sellerId == me }
        return try {
            AuthenticatedRequest.execute {
                RetrofitClient.api.getMyListings(sellerId = "eq.$sellerId")
            }.map { it.toModel() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            source.listings.filter { it.sellerId == me }
        }
    }

    override suspend fun addListing(listing: TicketListing): Int {
        val sellerId = SessionManager.userId
            ?: error("Silakan login dengan akun Supabase terlebih dahulu.")

        val created = AuthenticatedRequest.execute {
            RetrofitClient.api.createListing(
                body = CreateListingRequest(
                    eventId = listing.eventId,
                    sellerId = sellerId,
                    seatCategory = listing.seatCategory,
                    seatRow = listing.row,
                    faceValue = listing.faceValue,
                    price = listing.price,
                    quantity = listing.quantity
                )
            )
        }
        return created.first().id
    }

    override suspend fun updateListingPrice(listingId: Int, price: Long) {
        AuthenticatedRequest.execute {
            RetrofitClient.api.updateListing(
                id = "eq.$listingId",
                body = UpdateListingRequest(price = price)
            )
        }
    }

    override suspend fun deleteListing(listingId: Int) {
        AuthenticatedRequest.execute {
            RetrofitClient.api.updateListing(
                id = "eq.$listingId",
                body = UpdateListingRequest(status = "NONAKTIF")
            )
        }
    }

    override suspend fun getPriceHistory(eventId: Int, seatCategory: String): List<Long> {
        return try {
            AuthenticatedRequest.execute {
                RetrofitClient.api.getPriceHistory(
                    PriceHistoryRequest(eventId, seatCategory)
                )
            }.map { it.unitPrice }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
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
        paymentMethod: String
    ): Order {
        fakeNetwork()
        val index = source.listings.indexOfFirst { it.id == listingId }
        check(index >= 0) { "Tiket tidak ditemukan" }
        val listing = source.listings[index]
        check(listing.quantity >= quantity) { "Stok tiket tidak cukup" }

        // Kurangi stok; kalau habis listing otomatis berstatus Terjual.
        val remaining = listing.quantity - quantity
        source.listings[index] = listing.copy(
            quantity = remaining,
            status = if (remaining == 0) ListingStatus.TERJUAL else listing.status
        )

        val ticketPrice = listing.price * quantity
        val fee = ticketPrice * 5 / 100
        val id = source.nextOrderId()
        val now = LocalDateTime.now()
        val order = Order(
            id = id,
            eventId = listing.eventId,
            listingId = listing.id,
            buyerId = me,
            sellerId = listing.sellerId,
            buyerName = buyerName,
            quantity = quantity,
            seatLabel = "${listing.seatCategory}, ${listing.row}",
            ticketPrice = ticketPrice,
            serviceFee = fee,
            total = ticketPrice + fee,
            paymentMethod = paymentMethod,
            transactionNo = "%04d".format(id) + now.format(DateTimeFormatter.ofPattern("ddMMyyyy")),
            createdAt = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH.mm")),
            status = OrderStatus.DIPROSES
        )
        source.orders.add(order)
        return order
    }

    override suspend fun getOrder(orderId: Int): Order? {
        return source.orders.find { it.id == orderId }
    }

    override suspend fun getMyOrders(): List<Order> {
        fakeNetwork()
        return source.orders.filter { it.buyerId == me }.sortedByDescending { it.id }
    }

    override suspend fun getStoreOrders(): List<Order> {
        fakeNetwork()
        return source.orders.filter { it.sellerId == me }.sortedByDescending { it.id }
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

    override suspend fun getHotels(): List<Hotel> {
        fakeNetwork()
        return source.hotels
    }
}
