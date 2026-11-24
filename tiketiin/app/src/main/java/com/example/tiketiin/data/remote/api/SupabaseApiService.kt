package com.example.tiketiin.data.remote.api

import com.example.tiketiin.data.remote.dto.ConfirmTicketRequestDto
import com.example.tiketiin.data.remote.dto.EventDto
import com.example.tiketiin.data.remote.dto.VenueDto
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.tiketiin.data.remote.dto.ProfileDto
import com.example.tiketiin.data.remote.dto.GetAvailableHotelsRequestDto
import com.example.tiketiin.data.remote.dto.HotelDto
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.tiketiin.data.remote.dto.CreateOrderRequestDto
import com.example.tiketiin.data.remote.dto.OrderDto
import com.example.tiketiin.data.remote.dto.PayOrderRequestDto
import com.example.tiketiin.data.remote.dto.CreateHotelRequestDto
import com.example.tiketiin.data.remote.dto.UpdateHotelRequestDto
import com.example.tiketiin.data.remote.dto.ValidateTicketRequestDto
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.Headers

interface SupabaseApiService {

    // Mengambil seluruh event
    @GET("rest/v1/events")
    suspend fun getEvents(
        @Query("select")
        select: String = "*,venue:venues(*)",

        @Query("order")
        order: String = "date.asc"
    ): List<EventDto>


    // Mengambil detail event
    @GET("rest/v1/events")
    suspend fun getEventById(
        @Query("id")
        id: String,

        @Query("select")
        select: String = "*,venue:venues(*)"
    ): List<EventDto>


    // Mengambil venue
    @GET("rest/v1/venues")
    suspend fun getVenues(
        @Query("select")
        select: String = "*"
    ): List<VenueDto>


    // Mengambil event beserta listing tiket aktif
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


    // Mengambil satu event beserta listing tiket aktif
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

    // Mengambil profil berdasarkan UUID
    @GET("rest/v1/profiles")
    suspend fun getProfile(

        @Query("id")
        id: String,

        @Query("select")
        select: String = "*"

    ): List<ProfileDto>


    // Menguji akses profil berdasarkan RLS
    @GET("rest/v1/profiles")
    suspend fun getAccessibleProfiles(

        @Query("select")
        select: String = "*"

    ): List<ProfileDto>


    // Mengambil rekomendasi hotel berdasarkan event
    @POST("rest/v1/rpc/get_available_hotels")
    suspend fun getAvailableHotels(
        @Body request: GetAvailableHotelsRequestDto
    ): List<HotelDto>

    // Membuat pesanan melalui RPC Supabase
    @POST("rest/v1/rpc/create_order")
    suspend fun createOrder(
        @Body request: CreateOrderRequestDto
    ): Int

    @GET("rest/v1/orders")
    suspend fun getOrderById(
        @Query("id") id: String,
        @Query("select") select: String =
            "*,listing:ticket_listings!orders_listing_id_fkey(seat_category,seat_row),payments(*)"
    ): List<OrderDto>

    @POST("rest/v1/rpc/pay_order")
    suspend fun payOrder(
        @Body request: PayOrderRequestDto
    ): String

    @GET("rest/v1/orders")
    suspend fun getBuyerOrders(
        @Query("buyer_id") buyerId: String,
        @Query("select") select: String =
            "*,listing:ticket_listings!orders_listing_id_fkey(seat_category,seat_row),payments(*)",
        @Query("order") order: String = "created_at.desc"
    ): List<OrderDto>

    @GET("rest/v1/orders")
    suspend fun getSellerOrders(
        @Query("seller_id") sellerId: String,
        @Query("select") select: String =
            "*,listing:ticket_listings!orders_listing_id_fkey(seat_category,seat_row),payments(*)",
        @Query("order") order: String = "created_at.desc"
    ): List<OrderDto>

    @POST("rest/v1/rpc/confirm_ticket")
    suspend fun confirmTicket(
        @Body request: ConfirmTicketRequestDto
    ): String


// CRUD HOTEL

    // CREATE
    @Headers("Prefer: return=representation")
    @POST("rest/v1/hotels")
    suspend fun createHotel(
        @Body request: CreateHotelRequestDto
    ): List<HotelDto>

    // READ
    @GET("rest/v1/hotels")
    suspend fun getAllHotels(
        @Query("select") select: String = "*",
        @Query("order") order: String = "hotel_id.desc"
    ): List<HotelDto>

    // UPDATE
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/hotels")
    suspend fun updateHotel(
        @Query("hotel_id") hotelId: String,
        @Body request: UpdateHotelRequestDto
    ): List<HotelDto>

    // DELETE
    @DELETE("rest/v1/hotels")
    suspend fun deleteHotel(
        @Query("hotel_id") hotelId: String
    )

    @POST("rest/v1/rpc/validate_ticket")
    suspend fun validateTicket(
        @Body request: ValidateTicketRequestDto
    ): String

}