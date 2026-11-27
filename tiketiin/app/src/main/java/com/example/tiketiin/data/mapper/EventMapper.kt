package com.example.tiketiin.data.mapper

import com.example.tiketiin.data.remote.dto.TicketListingDto
import com.example.tiketiin.data.local.entity.CachedEventEntity
import com.example.tiketiin.data.remote.dto.EventDto
import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.EventSummary
import com.example.tiketiin.data.model.ListingStatus
import com.example.tiketiin.data.model.TicketListing
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Duration

fun EventDto.toEntity(): CachedEventEntity {

    val activeListings = ticketListings?.filter {
        it.status == "AKTIF" &&
                it.quantity > 0
    }

    return CachedEventEntity(
        id = id,
        name = name,
        category = category,
        venueId = venueId,
        venueName = venue?.name,
        city = venue?.city,
        latitude = venue?.latitude,
        longitude = venue?.longitude,
        date = date,
        time = time,
        description = description,
        posterUrl = posterUrl,
        posterStart = posterStart,
        posterEnd = posterEnd,
        flashEndsAt = flashEndsAt,
        baseSold = baseSold,
        baseWishlist = baseWishlist,
        viewCount = viewCount,
        viewsLast7Days = viewsLast7Days,

        lowestPrice = activeListings?.let { listings ->
            listings.minOfOrNull { it.price } ?: 0L
        },

        ticketCount = activeListings?.sumOf {
            it.quantity
        },

        cachedAt = System.currentTimeMillis()
    )
}

fun CachedEventEntity.toModel(): Event {

    val remainingHours = flashEndsAt?.let { timestamp ->

        val minutes = Duration.between(
            OffsetDateTime.now(),
            OffsetDateTime.parse(timestamp)
        ).toMinutes()

        if (minutes > 0) {
            ((minutes + 59) / 60).toInt()
        } else {
            null
        }
    }

    return Event(
        id = id,
        name = name,
        category = EventCategory.valueOf(category),
        venue = requireNotNull(venueName),
        city = requireNotNull(city),
        date = LocalDate.parse(date),
        time = time,
        latitude = requireNotNull(latitude),
        longitude = requireNotNull(longitude),
        description = description,
        posterStart = posterStart,
        posterEnd = posterEnd,
        posterUrl = posterUrl,
        posterRes = null,
        flashEndsInHours = remainingHours,
        baseSold = baseSold,
        baseWishlist = baseWishlist,
        viewCount = viewCount,
        viewsLast7Days = viewsLast7Days
    )
}

fun CachedEventEntity.toSummary(
    isSaved: Boolean = false
): EventSummary {

    return EventSummary(
        event = toModel(),
        lowestPrice = requireNotNull(lowestPrice),
        ticketCount = requireNotNull(ticketCount),
        isSaved = isSaved
    )
}

fun TicketListingDto.toModel(): TicketListing {
    return TicketListing(
        id = id,
        eventId = eventId,
        sellerId = sellerId,
        seatCategory = seatCategory,
        row = seatRow,
        faceValue = faceValue,
        price = price,
        quantity = quantity,
        status = ListingStatus.valueOf(status),
        views = views,
        saves = saves,
        offers = offers
    )
}