package com.example.tiketiin.data.repository

import com.example.tiketiin.data.model.EventSummary

class SavedEventsRepository(
    private val wishlistRepository: WishlistRepository,
    private val eventCatalogRepository: EventCatalogRepository
) {

    suspend fun getSavedEvents(): List<EventSummary> {

        // Ambil event ID yang disimpan oleh akun aktif.
        val savedIds = wishlistRepository.getSavedEventIds()

        if (savedIds.isEmpty()) {
            return emptyList()
        }

        // Perbarui katalog event dari Supabase.
        eventCatalogRepository.refreshEvents()

        // Ambil katalog terbaru dari Room.
        val catalog = eventCatalogRepository.getCachedEvents()

        // Gabungkan berdasarkan event ID.
        return catalog
            .filter { summary ->
                summary.event.id in savedIds
            }
            .map { summary ->
                summary.copy(isSaved = true)
            }
    }

    suspend fun removeSavedEvent(eventId: Int) {
        wishlistRepository.removeSavedEvent(eventId)
    }
}
