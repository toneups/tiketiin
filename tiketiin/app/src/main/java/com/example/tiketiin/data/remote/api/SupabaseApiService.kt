package com.example.tiketiin.data.remote.api

import com.example.tiketiin.data.remote.dto.EventDto
import com.example.tiketiin.data.remote.dto.VenueDto
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.tiketiin.data.remote.dto.ProfileDto
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.tiketiin.data.remote.dto.PriceHistoryDto
import com.example.tiketiin.data.remote.dto.PriceHistoryRequest

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
            "*,venue:venues(*),ticket_listings(id,price,quantity,status)",

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
            "*,venue:venues(*),ticket_listings(id,price,quantity,status)",

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
    // ===== Fair Price (Raisya) =====
    @POST("rest/v1/rpc/get_price_history")
    suspend fun getPriceHistory(
        @Body body: PriceHistoryRequest
    ): List<PriceHistoryDto>
}