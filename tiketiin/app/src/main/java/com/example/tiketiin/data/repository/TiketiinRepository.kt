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
import com.example.tiketiin.data.auth.SessionManager
import com.example.tiketiin.data.local.HotelLocalDataSource
import com.example.tiketiin.data.local.OrderLocalDataSource
import com.example.tiketiin.data.local.dao.ListingDao
import com.example.tiketiin.data.local.entity.CachedListingEntity
import com.example.tiketiin.data.mapper.toEntity
import com.example.tiketiin.data.mapper.toHotelModels
import com.example.tiketiin.data.mapper.toHotelModelsFromLocal
import com.example.tiketiin.data.mapper.toModel
import com.example.tiketiin.data.mapper.toOrderModelsFromLocal
import com.example.tiketiin.data.mapper.toSummary
import com.example.tiketiin.data.remote.EventRemoteDataSource
import com.example.tiketiin.data.remote.HotelRemoteDataSource
import com.example.tiketiin.data.remote.OrderRemoteDataSource
import com.example.tiketiin.data.remote.RetrofitClient
import com.example.tiketiin.data.remote.ReviewRemoteDataSource
import com.example.tiketiin.data.remote.dto.CreateEventDto
import com.example.tiketiin.data.remote.dto.CreateHotelRequestDto
import com.example.tiketiin.data.remote.dto.CreateListingRequest
import com.example.tiketiin.data.remote.dto.CreateVenueDto
import com.example.tiketiin.data.remote.dto.ListingDto
import com.example.tiketiin.data.remote.dto.PriceHistoryRequest
import com.example.tiketiin.data.remote.dto.SellerSalesOrderDto
import com.example.tiketiin.data.remote.dto.UpdateEventDto
import com.example.tiketiin.data.remote.dto.UpdateHotelRequestDto
import com.example.tiketiin.data.remote.dto.UpdateListingRequest
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

interface TiketiinRepository {
    // Akun
    suspend fun getUser(): User
    suspend fun login()
    suspend fun logout()
    suspend fun verifyKyc()
    suspend fun uploadTicketProof(bytes: ByteArray, mimeType: String, fileName: String): String

    // Event
    suspend fun getEventSummaries(): List<EventSummary>
    suspend fun getEventSummary(eventId: Int): EventSummary?
    suspend fun searchEvents(query: String, category: EventCategory?): List<EventSummary>
    suspend fun getRemoteEventSummary(eventId: Int): EventSummary?
    suspend fun recordView(eventId: Int)
    suspend fun recordRemoteView(eventId: Int)
    suspend fun toggleSaved(eventId: Int)

    // Kelola Event (seller)
    suspend fun getMyEvents(): List<Event>
    suspend fun saveEvent(
        event: Event,
        latitude: Double? = null,
        longitude: Double? = null,
        uploadPoster: (suspend (String) -> String)? = null
    )
    suspend fun deleteEvent(eventId: Int)

    // Listing tiket
    suspend fun getListings(eventId: Int): List<TicketListing>
    suspend fun getCheapestListing(eventId: Int): TicketListing?
    suspend fun getRemoteCheapestListing(eventId: Int): TicketListing?
    suspend fun getListing(listingId: Int): TicketListing?
    suspend fun getMyListings(): List<TicketListing>
    suspend fun addListing(listing: TicketListing): Int
    suspend fun updateListingPrice(listingId: Int, price: Long)
    suspend fun deleteListing(listingId: Int)
    suspend fun getPriceHistory(eventId: Int, seatCategory: String): List<Long>

    // Hot Opportunity
    suspend fun getDemandStats(): List<EventDemandStats>
    suspend fun getRemoteDemandStats(): List<EventDemandStats>

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

    suspend fun refundOrder(orderId: Int): Order

    suspend fun confirmTicket(orderId: Int): Order
    suspend fun validateTicket(qrCode: String): String
    suspend fun getOrder(orderId: Int): Order?
    suspend fun getMyOrders(): List<Order>
    suspend fun getStoreOrders(): List<Order>
    suspend fun getRemotePaidSellerOrders(): List<SellerSalesOrderDto>
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
    private val listingDao: ListingDao? = null,
    private val hotelRemoteDataSource: HotelRemoteDataSource = HotelRemoteDataSource(),
    private val eventRemoteDataSource: EventRemoteDataSource = EventRemoteDataSource(),
    private val orderRemoteDataSource: OrderRemoteDataSource = OrderRemoteDataSource(),
    private val reviewRemoteDataSource: ReviewRemoteDataSource = ReviewRemoteDataSource(),
    private val hotelLocalDataSource: HotelLocalDataSource? = null,
    private val orderLocalDataSource: OrderLocalDataSource? = null,
    private val profileRepository: ProfileRepository = ProfileRepository()
) : TiketiinRepository {
    override suspend fun uploadTicketProof(
        bytes: ByteArray,
        mimeType: String,
        fileName: String
    ): String {
        val userId = SessionManager.userId
            ?: error("Silakan login dengan akun Supabase terlebih dahulu.")
        val path = "$userId/$fileName"
        val response = AuthenticatedRequest.execute {
            RetrofitClient.api.uploadTicketProof(
                path = path,
                contentType = mimeType,
                body = bytes.toRequestBody(mimeType.toMediaType())
            )
        }
        check(response.isSuccessful) {
            "Upload gagal (HTTP ${response.code()})"
        }
        return path
    }

    // Meniru jeda jaringan supaya state loading terlihat saat demo.
    private suspend fun fakeNetwork() = delay(350)

    private val me get() = DummyDataSource.CURRENT_USER_ID

    override suspend fun getUser(): User {
        return if (SessionManager.hasSession) {
            profileRepository.getMyProfile()
        } else {
            source.user
        }
    }

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

    override suspend fun recordRemoteView(eventId: Int) {
        if (!SessionManager.hasSession) return

        AuthenticatedRequest.execute {
            val userId = checkNotNull(SessionManager.userId)
            EventRemoteDataSource().recordEventView(eventId, userId)
        }
    }

    override suspend fun toggleSaved(eventId: Int) {
        if (!source.savedEventIds.add(eventId)) {
            source.savedEventIds.remove(eventId)
        }
    }

    override suspend fun getMyEvents(): List<Event> {
        check(SessionManager.hasSession) { "Silakan masuk untuk mengelola event." }
        return AuthenticatedRequest.execute {
            val userId = checkNotNull(SessionManager.userId)
            EventRemoteDataSource().getEventsByOwner(userId)
                .map { it.toEntity().toModel() }
        }
    }

    override suspend fun saveEvent(
        event: Event,
        latitude: Double?,
        longitude: Double?,
        uploadPoster: (suspend (String) -> String)?
    ) {
        check(SessionManager.hasSession) {
            "Silakan masuk untuk mengelola event."
        }

        AuthenticatedRequest.execute {
            val userId = checkNotNull(SessionManager.userId)
            val remote = EventRemoteDataSource()

            val venue = remote.getVenues().firstOrNull {
                it.name.equals(event.venue.trim(), ignoreCase = true) &&
                        it.city.equals(event.city.trim(), ignoreCase = true)
            } ?: run {
                if (latitude == null || longitude == null) {
                    throw VenueCoordinatesRequired()
                }

                require(latitude in -90.0..90.0 && longitude in -180.0..180.0) {
                    "Latitude harus antara -90 dan 90, dan longitude antara -180 dan 180."
                }

                remote.createVenue(
                    CreateVenueDto(
                        name = event.venue.trim(),
                        city = event.city.trim(),
                        latitude = latitude,
                        longitude = longitude,
                        createdBy = userId
                    )
                )
            }

            if (event.id == 0) {
                val posterUrl = uploadPoster?.invoke(userId)

                remote.createEvent(
                    CreateEventDto(
                        name = event.name.trim(),
                        category = event.category.name,
                        venueId = venue.id,
                        date = event.date.toString(),
                        time = event.time,
                        createdBy = userId,
                        posterUrl = posterUrl
                    )
                )
            } else {
                remote.updateEvent(
                    event.id,
                    UpdateEventDto(
                        name = event.name.trim(),
                        category = event.category.name,
                        venueId = venue.id,
                        date = event.date.toString(),
                        time = event.time
                    )
                )
            }
        }
    }

    override suspend fun deleteEvent(eventId: Int) {
        check(SessionManager.hasSession) {
            "Silakan masuk untuk mengelola event."
        }

        AuthenticatedRequest.execute {
            EventRemoteDataSource().deleteEvent(eventId)
        }
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

    override suspend fun getRemoteCheapestListing(eventId: Int): TicketListing? {
        val listing = EventRemoteDataSource().getCheapestActiveListing(eventId)
            ?: return null

        return TicketListing(
            id = listing.id,
            eventId = listing.eventId,
            sellerId = listing.sellerId,
            seatCategory = listing.seatCategory,
            row = listing.seatRow,
            faceValue = listing.faceValue,
            price = listing.price,
            quantity = listing.quantity
        )
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
            val remote = AuthenticatedRequest.execute {
                RetrofitClient.api.getMyListings(sellerId = "eq.$sellerId")
            }
            // Simpan salinan ke Room (cache)
            try {
                listingDao?.replaceListings(sellerId, remote.map { it.toEntity() })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // gagal menyimpan cache tidak boleh menggagalkan daftar
            }
            remote.map { it.toModel() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Offline / token kedaluwarsa: pakai cache Room, lalu dummy sebagai cadangan terakhir
            val cached = try {
                listingDao?.getListingsBySeller(sellerId)?.map { it.toModel() }
            } catch (ex: Exception) {
                null
            }
            if (!cached.isNullOrEmpty()) cached
            else source.listings.filter { it.sellerId == me }
        }
    }

    private fun ListingDto.toEntity() = CachedListingEntity(
        id = id,
        eventId = eventId,
        sellerId = sellerId,
        seatCategory = seatCategory,
        seatRow = seatRow,
        faceValue = faceValue,
        price = price,
        quantity = quantity,
        status = status,
        views = views,
        saves = saves,
        offers = offers
    )

    private fun CachedListingEntity.toModel() = TicketListing(
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

    override suspend fun getRemoteDemandStats(): List<EventDemandStats> {
        check(SessionManager.hasSession) {
            "Silakan masuk untuk melihat Hot Opportunity."
        }

        return AuthenticatedRequest.execute {
            val remote = EventRemoteDataSource()
            val stats = remote.getEventDemandStats()
            val eventsById = remote.getEvents().associateBy { it.id }

            stats.mapNotNull { row ->
                val event = eventsById[row.eventId]
                    ?.toEntity()
                    ?.toModel()
                    ?: return@mapNotNull null

                EventDemandStats(
                    event = event,
                    soldCount = row.soldCount,
                    wishlistCount = row.wishlistCount,
                    viewCount = row.viewCount,
                    viewsLast7Days = row.viewsLast7Days,
                    availableStock = row.availableStock
                )
            }
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

    override suspend fun refundOrder(orderId: Int): Order {
        return AuthenticatedRequest.execute {
            val result = orderRemoteDataSource.refundOrder(orderId)

            check(result) {
                "Pengembalian dana gagal diproses."
            }

            val orderDto = orderRemoteDataSource.getOrderById(orderId)
                ?: error("Refund berhasil, tetapi detail pesanan gagal diambil.")

            val order = orderDto.toModel()

            orderLocalDataSource?.saveOrder(order.toEntity())

            order
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

        // Ambil UUID asli dari pengguna yang sedang login.
        val userId = requireNotNull(SessionManager.userId) {
            "Sesi login tidak ditemukan. Silakan login kembali."
        }

        return try {
            val remoteOrders = AuthenticatedRequest.execute {
                orderRemoteDataSource.getBuyerOrders(userId).map { dto ->
                    val review = reviewRemoteDataSource.getReviewByOrderId(dto.id)

                    dto.toModel().copy(
                        rating = review?.rating ?: 0
                    )
                }
            }

            // Simpan hasil terbaru ke cache berdasarkan UUID pengguna.
            if (local != null) {
                local.replaceBuyerOrders(
                    buyerId = userId,
                    orders = remoteOrders.map { it.toEntity() }
                )
            }

            remoteOrders

        } catch (error: CancellationException) {
            throw error

        } catch (remoteError: Exception) {
            if (local == null) throw remoteError

            // Ambil cache milik pengguna yang sedang login.
            val cachedOrders = local.getBuyerOrders(userId)

            if (cachedOrders.isEmpty()) {
                throw remoteError
            }

            cachedOrders.toOrderModelsFromLocal()
        }
    }


    override suspend fun getStoreOrders(): List<Order> {
        val local = orderLocalDataSource

        // Ambil UUID asli dari pengguna yang sedang login.
        val userId = requireNotNull(SessionManager.userId) {
            "Sesi login tidak ditemukan. Silakan login kembali."
        }

        return try {
            val remoteOrders = AuthenticatedRequest.execute {
                orderRemoteDataSource
                    .getSellerOrders(userId)
                    .map { it.toModel() }
            }

            // Simpan hasil terbaru ke cache berdasarkan UUID pengguna.
            if (local != null) {
                local.replaceSellerOrders(
                    sellerId = userId,
                    orders = remoteOrders.map { it.toEntity() }
                )
            }

            remoteOrders

        } catch (error: CancellationException) {
            throw error

        } catch (remoteError: Exception) {
            if (local == null) throw remoteError

            // Ambil cache milik pengguna yang sedang login.
            val cachedOrders = local.getSellerOrders(userId)

            if (cachedOrders.isEmpty()) {
                throw remoteError
            }

            cachedOrders.toOrderModelsFromLocal()
        }
    }

    override suspend fun getRemotePaidSellerOrders(): List<SellerSalesOrderDto> {
        check(SessionManager.hasSession) {
            "Silakan masuk untuk melihat data penjualan."
        }

        return AuthenticatedRequest.execute {
            val userId = checkNotNull(SessionManager.userId)
            EventRemoteDataSource().getPaidSellerOrders(userId)
        }
    }

    override suspend fun updateOrderStatus(orderId: Int, status: OrderStatus) {
        val index = source.orders.indexOfFirst { it.id == orderId }
        if (index >= 0) {
            source.orders[index] = source.orders[index].copy(status = status)
        }
    }


    override suspend fun rateOrder(orderId: Int, rating: Int) {
        require(orderId > 0) {
            "ID pesanan tidak valid."
        }

        require(rating in 1..5) {
            "Rating harus antara 1 sampai 5."
        }

        val buyerId = requireNotNull(SessionManager.userId) {
            "Sesi login tidak ditemukan. Silakan login kembali."
        }

        AuthenticatedRequest.execute {
            // Ambil data pesanan asli dari Supabase.
            val order = requireNotNull(
                orderRemoteDataSource.getOrderById(orderId)
            ) {
                "Pesanan tidak ditemukan."
            }

            // Pastikan pesanan milik pembeli yang sedang login.
            check(order.buyerId == buyerId) {
                "Kamu tidak memiliki akses untuk menilai pesanan ini."
            }

            // Rating hanya boleh diberikan setelah pesanan selesai.
            check(order.status.equals("COMPLETED", ignoreCase = true)) {
                "Pesanan harus selesai sebelum dapat dinilai."
            }

            // Simpan rating ke Supabase.
            reviewRemoteDataSource.createReview(
                orderId = order.id,
                buyerId = buyerId,
                sellerId = order.sellerId,
                rating = rating
            )
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

    override suspend fun getRemoteEventSummary(eventId: Int): EventSummary? {
        return EventRemoteDataSource()
            .getEventWithListings(eventId)
            ?.toEntity()
            ?.toSummary()
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

class VenueCoordinatesRequired : Exception(
    "Lengkapi koordinat untuk menambahkan lokasi baru."
)