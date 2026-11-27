package com.example.tiketiin.data.remote.api

import com.example.tiketiin.data.remote.dto.ActiveListingDto
import com.example.tiketiin.data.remote.dto.CityOptionDto
import com.example.tiketiin.data.remote.dto.ConfirmTicketRequestDto
import com.example.tiketiin.data.remote.dto.CreateEventDto
import com.example.tiketiin.data.remote.dto.CreateHotelRequestDto
import com.example.tiketiin.data.remote.dto.CreateListingRequest
import com.example.tiketiin.data.remote.dto.CreateOrderRequestDto
import com.example.tiketiin.data.remote.dto.CreateReviewRequestDto
import com.example.tiketiin.data.remote.dto.CreateVenueDto
import com.example.tiketiin.data.remote.dto.CreateWishlistRequest
import com.example.tiketiin.data.remote.dto.EventDemandStatsDto
import com.example.tiketiin.data.remote.dto.EventDto
import com.example.tiketiin.data.remote.dto.EventViewDto
import com.example.tiketiin.data.remote.dto.GetAvailableHotelsRequestDto
import com.example.tiketiin.data.remote.dto.HotelDto
import com.example.tiketiin.data.remote.dto.ListingDto
import com.example.tiketiin.data.remote.dto.ListingRowDto
import com.example.tiketiin.data.remote.dto.NotificationDto
import com.example.tiketiin.data.remote.dto.NotificationReadUpdate
import com.example.tiketiin.data.remote.dto.OrderDto
import com.example.tiketiin.data.remote.dto.PayOrderRequestDto
import com.example.tiketiin.data.remote.dto.PriceHistoryDto
import com.example.tiketiin.data.remote.dto.PriceHistoryRequest
import com.example.tiketiin.data.remote.dto.ProfileDto
import com.example.tiketiin.data.remote.dto.PurchaseHistoryDto
import com.example.tiketiin.data.remote.dto.ReviewDto
import com.example.tiketiin.data.remote.dto.SellerSalesOrderDto
import com.example.tiketiin.data.remote.dto.TicketListingDto
import com.example.tiketiin.data.remote.dto.UpdateEventDto
import com.example.tiketiin.data.remote.dto.UpdateHotelRequestDto
import com.example.tiketiin.data.remote.dto.UpdateListingRequest
import com.example.tiketiin.data.remote.dto.ValidateTicketRequestDto
import com.example.tiketiin.data.remote.dto.VenueDto
import com.example.tiketiin.data.remote.dto.WishlistDto

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SupabaseApiService {

    // =========================
    // EVENT DAN VENUE
    // =========================

    @GET("rest/v1/events")
    suspend fun getEvents(
        @Query("select")
        select: String = "*,venue:venues(*)",
        @Query("order")
        order: String = "date.asc"
    ): List<EventDto>

    @GET("rest/v1/events")
    suspend fun getEventsByOwner(
        @Query("created_by") ownerFilter: String,
        @Query("select") select: String = "*,venue:venues(*)",
        @Query("order") order: String = "date.asc"
    ): List<EventDto>

    // Mengambil detail event
    @GET("rest/v1/events")
    suspend fun getEventById(
        @Query("id")
        id: String,
        @Query("select")
        select: String = "*,venue:venues(*)"
    ): List<EventDto>

    @GET("rest/v1/venues")
    suspend fun getVenues(
        @Query("select")
        select: String = "*"
    ): List<VenueDto>

    @GET("rest/v1/events")
    suspend fun getEventsWithListings(
        @Query("select")
        select: String =
            "*,venue:venues(*),ticket_listings(id,event_id,seller_id,seat_category,seat_row,face_value,price,quantity,status,views,saves,offers,proof_path)",
        @Query("ticket_listings.status")
        status: String = "eq.AKTIF",
        @Query("ticket_listings.quantity")
        quantity: String = "gt.0",
        @Query("order")
        order: String = "date.asc"
    ): List<EventDto>

    @GET("rest/v1/events")
    suspend fun getEventWithListings(
        @Query("id")
        id: String,
        @Query("select")
        select: String =
            "*,venue:venues(*),ticket_listings(id,event_id,seller_id,seat_category,seat_row,face_value,price,quantity,status,views,saves,offers,proof_path)",
        @Query("ticket_listings.status")
        status: String = "eq.AKTIF",
        @Query("ticket_listings.quantity")
        quantity: String = "gt.0"
    ): List<EventDto>


    // =========================
    // PROFILE
    // =========================

    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Query("id")
        id: String,
        @Query("select")
        select: String = "*"
    ): List<ProfileDto>

    @GET("rest/v1/profiles")
    suspend fun getAccessibleProfiles(
        @Query("select")
        select: String = "*"
    ): List<ProfileDto>

    // ===== Hot Opportunity, kelola event, dan dashboard seller =====

    @POST("rest/v1/events")
    suspend fun createEvent(@Body event: CreateEventDto)

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/events")
    suspend fun updateEvent(
        @Query("id") idFilter: String,
        @Body event: UpdateEventDto
    ): List<EventDto>

    @Headers("Prefer: return=representation")
    @DELETE("rest/v1/events")
    suspend fun deleteEvent(@Query("id") idFilter: String): List<EventDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/venues")
    suspend fun createVenue(@Body venue: CreateVenueDto): List<VenueDto>

    @GET("rest/v1/ticket_listings")
    suspend fun getCheapestActiveListing(
        @Query("event_id") eventFilter: String,
        @Query("status") status: String = "eq.AKTIF",
        @Query("quantity") quantity: String = "gt.0",
        @Query("order") order: String = "price.asc,id.asc",
        @Query("limit") limit: Int = 1
    ): List<ActiveListingDto>

    @Headers("Prefer: resolution=ignore-duplicates,return=minimal")
    @POST("rest/v1/event_views")
    suspend fun recordEventView(
        @Body view: EventViewDto,
        @Query("on_conflict") conflictColumns: String = "user_id,event_id,viewed_on"
    )

    @GET("rest/v1/rpc/get_event_demand_stats")
    suspend fun getEventDemandStats(): List<EventDemandStatsDto>

    @GET("rest/v1/orders")
    suspend fun getPaidSellerOrders(
        @Query("seller_id") sellerFilter: String,
        @Query("status") orderStatus: String = "in.(PAID,COMPLETED)",
        @Query("payments.status") paymentStatus: String = "eq.SUCCESS",
        @Query("select") select: String =
            "event_id,unit_price,quantity,event:events(name),payments!inner(status)",
        @Query("order") order: String = "id.asc",
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): List<SellerSalesOrderDto>

    @retrofit2.http.POST("storage/v1/object/event-posters/{path}")
    suspend fun uploadEventPoster(
        @retrofit2.http.Path("path", encoded = true) path: String,
        @retrofit2.http.Body image: okhttp3.RequestBody
    ): Unit

    // ===== Profil dan preferensi (auth-for-you) =====

    @PATCH("rest/v1/profiles")
    @Headers("Prefer: return=representation")
    suspend fun updateProfile(
        @Query("id")
        id: String,
        @Body updates: Map<String, String>
    ): List<ProfileDto>

    @PATCH("rest/v1/profiles")
    @Headers("Prefer: return=representation")
    suspend fun updateRecommendationNotificationPreference(
        @Query("id")
        id: String,
        @Body updates: Map<String, Boolean>
    ): List<ProfileDto>


    // =========================
    // HOTEL
    // =========================

    @POST("rest/v1/rpc/get_available_hotels")
    suspend fun getAvailableHotels(
        @Body request: GetAvailableHotelsRequestDto
    ): List<HotelDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/hotels")
    suspend fun createHotel(
        @Body request: CreateHotelRequestDto
    ): List<HotelDto>

    @GET("rest/v1/hotels")
    suspend fun getAllHotels(
        @Query("select")
        select: String = "*",
        @Query("order")
        order: String = "hotel_id.desc"
    ): List<HotelDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/hotels")
    suspend fun updateHotel(
        @Query("hotel_id")
        hotelId: String,
        @Body request: UpdateHotelRequestDto
    ): List<HotelDto>

    @DELETE("rest/v1/hotels")
    suspend fun deleteHotel(
        @Query("hotel_id")
        hotelId: String
    )


    // =========================
    // ORDER DAN PEMBAYARAN
    // =========================

    @POST("rest/v1/rpc/create_order")
    suspend fun createOrder(
        @Body request: CreateOrderRequestDto
    ): Int

    @GET("rest/v1/orders")
    suspend fun getOrderById(
        @Query("id")
        id: String,
        @Query("select")
        select: String =
            "*,listing:ticket_listings!orders_listing_id_fkey(seat_category,seat_row),payments(*)"
    ): List<OrderDto>

    @POST("rest/v1/rpc/pay_order")
    suspend fun payOrder(
        @Body request: PayOrderRequestDto
    ): String

    @GET("rest/v1/orders")
    suspend fun getBuyerOrders(
        @Query("buyer_id")
        buyerId: String,
        @Query("select")
        select: String =
            "*,listing:ticket_listings!orders_listing_id_fkey(seat_category,seat_row),payments(*)",
        @Query("order")
        order: String = "created_at.desc"
    ): List<OrderDto>

    @GET("rest/v1/orders")
    suspend fun getSellerOrders(
        @Query("seller_id")
        sellerId: String,
        @Query("select")
        select: String =
            "*,listing:ticket_listings!orders_listing_id_fkey(seat_category,seat_row),payments(*)",
        @Query("order")
        order: String = "created_at.desc"
    ): List<OrderDto>

    @POST("rest/v1/rpc/confirm_ticket")
    suspend fun confirmTicket(
        @Body request: ConfirmTicketRequestDto
    ): String

    @POST("rest/v1/rpc/validate_ticket")
    suspend fun validateTicket(
        @Body request: ValidateTicketRequestDto
    ): String


    // =========================
    // WISHLIST
    // =========================

    @GET("rest/v1/wishlists")
    suspend fun getWishlists(
        @Query("user_id")
        userId: String,
        @Query("select")
        select: String = "id,user_id,event_id"
    ): List<WishlistDto>

    // Menambahkan event ke wishlist
    @POST("rest/v1/wishlists")
    @Headers("Prefer: return=minimal")
    suspend fun addWishlist(
        @Body request: CreateWishlistRequest
    ): Response<Unit>

    @DELETE("rest/v1/wishlists")
    suspend fun deleteWishlist(
        @Query("user_id")
        userId: String,
        @Query("event_id")
        eventId: String
    ): Response<Unit>


    // =========================
    // TICKET LISTING
    // =========================

    @GET("rest/v1/ticket_listings")
    suspend fun getActiveTicketListings(
        @Query("event_id")
        eventId: String,
        @Query("status")
        status: String = "eq.AKTIF",
        @Query("quantity")
        quantity: String = "gt.0",
        @Query("select")
        select: String =
            "id,event_id,seller_id,seat_category,seat_row,face_value,price,quantity,status,views,saves,offers",
        @Query("order")
        order: String = "price.asc"
    ): List<TicketListingDto>


    // =========================
    // PURCHASE HISTORY
    // =========================

    @GET("rest/v1/orders")
    suspend fun getCompletedBuyerOrders(
        @Query("buyer_id")
        buyerId: String,

        @Query("status")
        status: String = "in.(PAID,COMPLETED)",

        @Query("paid_at")
        paidAt: String = "not.is.null",

        @Query("select")
        select: String =
            "id,buyer_id,event_id,status,quantity,unit_price,original_unit_price,service_fee,total_price,membership_level,membership_discount_percent,membership_discount_amount,membership_eligible_amount,paid_at"
    ): List<PurchaseHistoryDto>



    // =========================
    // NOTIFICATION
    // =========================

    @GET("rest/v1/notifications")
    suspend fun getMyNotifications(
        @Query("user_id")
        userId: String,
        @Query("select")
        select: String =
            "id,user_id,title,message,type,event_id,order_id,is_read,created_at",
        @Query("order")
        order: String = "created_at.desc",
        @Query("limit")
        limit: Int = 50
    ): List<NotificationDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/notifications")
    suspend fun markNotificationAsRead(
        @Query("id")
        id: String,
        @Query("user_id")
        userId: String,
        @Query("select")
        select: String =
            "id,user_id,title,message,type,event_id,order_id,is_read,created_at",
        @Body body: NotificationReadUpdate =
            NotificationReadUpdate()
    ): List<NotificationDto>

    @HEAD("rest/v1/notifications")
    @Headers("Prefer: count=exact")
    suspend fun countUnreadNotifications(
        @Query("user_id")
        userId: String,
        @Query("is_read")
        isRead: String = "eq.false"
    ): Response<Void>


    // =========================
    // CITY OPTIONS
    // =========================

    @GET("rest/v1/city_options")
    suspend fun getCityOptions(
        @Query("select")
        select: String =
            "id,name,administrative_type,province",
        @Query("order")
        order: String = "province.asc,name.asc"
    ): List<CityOptionDto>


    // =========================
    // FAIR PRICE (Raisya)
    // =========================

    @POST("rest/v1/rpc/get_price_history")
    suspend fun getPriceHistory(
        @Body body: PriceHistoryRequest
    ): List<PriceHistoryDto>

    @POST("rest/v1/ticket_listings")
    suspend fun createListing(
        @Header("Prefer") prefer: String = "return=representation",
        @Body body: CreateListingRequest
    ): List<ListingRowDto>

    @GET("rest/v1/ticket_listings")
    suspend fun getListingById(
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): List<ListingDto>

    // Listing milik seller yang sedang login (RLS "Read own listings")
    @GET("rest/v1/ticket_listings")
    suspend fun getMyListings(
        @Query("seller_id") sellerId: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<ListingDto>

    // Ubah harga atau status satu listing
    @PATCH("rest/v1/ticket_listings")
    suspend fun updateListing(
        @Query("id") id: String,
        @Header("Prefer") prefer: String = "return=minimal",
        @Body body: UpdateListingRequest
    )

    // Upload bukti tiket ke bucket private ticket-proofs
    // path wajib diawali UUID akun: "<userId>/<namaFile>"
    @POST("storage/v1/object/ticket-proofs/{path}")
    suspend fun uploadTicketProof(
        @Path("path", encoded = true) path: String,
        @Header("Content-Type") contentType: String,
        @Header("x-upsert") upsert: String = "true",
        @Body body: RequestBody
    ): retrofit2.Response<okhttp3.ResponseBody>


    // =========================
    // REVIEW / RATING
    // =========================

    @GET("rest/v1/reviews")
    suspend fun getReviews(
        @Query("order_id")
        orderId: String,
        @Query("select")
        select: String = "*"
    ): List<ReviewDto>

    @POST("rest/v1/reviews")
    @Headers("Prefer: return=representation")
    suspend fun createReview(
        @Body request: CreateReviewRequestDto
    ): List<ReviewDto>

    @POST("rest/v1/rpc/refund_order")
    suspend fun refundOrder(
        @Body request: Map<String, Int>
    ): Boolean

}