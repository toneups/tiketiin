package com.example.tiketiin.data.repository

import com.example.tiketiin.data.local.EventLocalDataSource
import com.example.tiketiin.data.remote.EventRemoteDataSource
import com.example.tiketiin.data.mapper.toEntity
import com.example.tiketiin.data.mapper.toSummary
import com.example.tiketiin.data.model.EventSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

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

    // Mengambil satu event berdasarkan ID dari cache Room
    suspend fun getEventSummary(eventId: Int): EventSummary? {
        var cachedEvent = localDataSource.getEvents()
            .firstOrNull { it.id == eventId }

        if (cachedEvent == null) {
            refreshEvents()

            cachedEvent = localDataSource.getEvents()
                .firstOrNull { it.id == eventId }
        }

        return cachedEvent?.toSummary()
    }

}