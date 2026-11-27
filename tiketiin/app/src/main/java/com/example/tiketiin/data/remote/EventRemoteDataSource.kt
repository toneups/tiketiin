package com.example.tiketiin.data.remote

import com.example.tiketiin.data.remote.api.SupabaseApiService
import com.example.tiketiin.data.remote.dto.ActiveListingDto
import com.example.tiketiin.data.remote.dto.CreateEventDto
import com.example.tiketiin.data.remote.dto.CreateVenueDto
import com.example.tiketiin.data.remote.dto.EventDemandStatsDto
import com.example.tiketiin.data.remote.dto.EventDto
import com.example.tiketiin.data.remote.dto.EventViewDto
import com.example.tiketiin.data.remote.dto.SellerSalesOrderDto
import com.example.tiketiin.data.remote.dto.TicketListingDto
import com.example.tiketiin.data.remote.dto.UpdateEventDto
import com.example.tiketiin.data.remote.dto.VenueDto

class EventRemoteDataSource(
    private val api: SupabaseApiService = RetrofitClient.api
) {

    suspend fun getEvents(): List<EventDto> {
        return api.getEvents()
    }

    suspend fun getEventsByOwner(userId: String): List<EventDto> {
        return api.getEventsByOwner(ownerFilter = "eq.$userId")
    }

    suspend fun getEventById(eventId: Int): EventDto? {
        return api.getEventById(
            id = "eq.$eventId"
        ).firstOrNull()
    }

    suspend fun getVenues(): List<VenueDto> {
        return api.getVenues()
    }

    suspend fun getEventsWithListings(): List<EventDto> {
        return api.getEventsWithListings()
    }

    suspend fun getCheapestActiveListing(eventId: Int): ActiveListingDto? {
        return api.getCheapestActiveListing(eventFilter = "eq.$eventId")
            .firstOrNull()
    }

    suspend fun getEventWithListings(
        eventId: Int
    ): EventDto? {
        return api.getEventWithListings(
            id = "eq.$eventId"
        ).firstOrNull()
    }

    suspend fun createEvent(event: CreateEventDto) {
        api.createEvent(event)
    }

    suspend fun updateEvent(eventId: Int, event: UpdateEventDto) {
        val updated = api.updateEvent("eq.$eventId", event)
        check(updated.size == 1) { "Event tidak bisa diubah." }
    }

    suspend fun getActiveTicketListings(
        eventId: Int
    ): List<TicketListingDto> {
        require(eventId > 0) {
            "ID event tidak valid."
        }

        return api.getActiveTicketListings(
            eventId = "eq.$eventId"
        )
    }

    suspend fun deleteEvent(eventId: Int) {
        val deleted = api.deleteEvent("eq.$eventId")
        check(deleted.size == 1) {
            "Event tidak ditemukan atau tidak bisa dihapus."
        }
    }

    suspend fun createVenue(venue: CreateVenueDto): VenueDto {
        return api.createVenue(venue).single()
    }

    suspend fun recordEventView(eventId: Int, userId: String) {
        api.recordEventView(EventViewDto(eventId = eventId, userId = userId))
    }

    suspend fun getEventDemandStats(): List<EventDemandStatsDto> {
        return api.getEventDemandStats()
    }

    suspend fun getPaidSellerOrders(userId: String): List<SellerSalesOrderDto> {
        val orders = mutableListOf<SellerSalesOrderDto>()
        val pageSize = 100

        do {
            val page = api.getPaidSellerOrders(
                sellerFilter = "eq.$userId",
                limit = pageSize,
                offset = orders.size
            )
            orders.addAll(page)
        } while (page.size == pageSize)

        return orders
    }
}