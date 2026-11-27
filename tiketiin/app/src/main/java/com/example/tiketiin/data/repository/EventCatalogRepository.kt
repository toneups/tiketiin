package com.example.tiketiin.data.repository

import com.example.tiketiin.data.local.EventLocalDataSource
import com.example.tiketiin.data.remote.EventRemoteDataSource
import com.example.tiketiin.data.mapper.toEntity
import com.example.tiketiin.data.mapper.toSummary
import com.example.tiketiin.data.model.EventSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.tiketiin.data.model.TicketListing
import com.example.tiketiin.data.model.ListingStatus

class EventCatalogRepository(
    private val localDataSource: EventLocalDataSource,
    private val remoteDataSource: EventRemoteDataSource
) {

    // Mengamati perubahan cache secara otomatis
    fun observeEvents(): Flow<List<EventSummary>> {

        return localDataSource.observeEvents().map { events ->

            events.filter {
                it.lowestPrice != null &&
                        it.ticketCount != null &&
                        it.latitude != null &&
                        it.longitude != null &&
                        it.venueName != null &&
                        it.city != null
            }.map {
                it.toSummary()
            }
        }
    }

    // Membaca data yang sudah tersimpan di Room
    suspend fun getCachedEvents(): List<EventSummary> {

        return localDataSource.getEvents()
            .filter {
                it.lowestPrice != null &&
                        it.ticketCount != null &&
                        it.latitude != null &&
                        it.longitude != null &&
                        it.venueName != null &&
                        it.city != null
            }
            .map {
                it.toSummary()
            }
    }

    // Memperbarui data dari Supabase
    suspend fun refreshEvents() {

        val remoteEvents =
            remoteDataSource.getEventsWithListings()

        check(remoteEvents.all {
            it.ticketListings != null
        }) {
            "Data listing belum lengkap"
        }

        val entities = remoteEvents.map {
            it.toEntity()
        }

        // Validasi model sebelum mengganti cache lama
        entities.forEach {
            it.toSummary()
        }

        localDataSource.saveEvents(entities)
    }


    suspend fun getActiveTicketListings(
        eventId: Int
    ): List<TicketListing> {

        require(eventId > 0) {
            "ID event tidak valid."
        }

        // Mengambil data dari Remote Data Source
        val remoteListings =
            remoteDataSource.getActiveTicketListings(eventId)

        // Mengubah DTO Supabase menjadi model aplikasi
        return remoteListings
            .filter {
                it.eventId == eventId &&
                        it.status == "AKTIF" &&
                        it.quantity > 0
            }
            .map { dto ->
                TicketListing(
                    id = dto.id,
                    eventId = dto.eventId,
                    sellerId = dto.sellerId,
                    seatCategory = dto.seatCategory,
                    row = dto.seatRow,
                    faceValue = dto.faceValue,
                    price = dto.price,
                    quantity = dto.quantity,
                    status = ListingStatus.AKTIF,
                    views = dto.views,
                    saves = dto.saves,
                    offers = dto.offers
                )
            }
            .sortedBy { it.price }
    }
}