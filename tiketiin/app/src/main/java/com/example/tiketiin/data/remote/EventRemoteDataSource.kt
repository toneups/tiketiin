package com.example.tiketiin.data.remote

import com.example.tiketiin.data.remote.api.SupabaseApiService
import com.example.tiketiin.data.remote.dto.EventDto
import com.example.tiketiin.data.remote.dto.VenueDto
import com.example.tiketiin.data.remote.dto.TicketListingDto

class EventRemoteDataSource(
    private val api: SupabaseApiService = RetrofitClient.api
) {

    suspend fun getEvents(): List<EventDto> {
        return api.getEvents()
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

    suspend fun getEventWithListings(
        eventId: Int
    ): EventDto? {
        return api.getEventWithListings(
            id = "eq.$eventId"
        ).firstOrNull()
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

}