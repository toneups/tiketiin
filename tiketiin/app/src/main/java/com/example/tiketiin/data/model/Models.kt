package com.example.tiketiin.data.model

import java.time.LocalDate

// Kategori event. label = teks yang tampil di chip / tag.
enum class EventCategory(val label: String) {
    MUSIK("Musik"),
    OLAHRAGA("Olahraga"),
    SEMINAR("Seminar"),
    PAMERAN("Pameran"),
    SENI("Seni"),
    FILM("Film")
}

data class Event(
    val id: Int,
    val name: String,
    val category: EventCategory,
    val venue: String,
    val city: String,
    val date: LocalDate,
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    // Warna gradasi poster pengganti (format 0xAARRGGBB).
    val posterStart: Long,
    val posterEnd: Long,
    // Isi dengan R.drawable.nama_poster kalau gambar poster sudah ditaruh di res/drawable.
    val posterRes: Int? = null,
    val posterUrl: String? = null,
    // Sisa waktu penjualan (jam) untuk label "Berakhir ... lagi". null = bukan flash ticket.
    val flashEndsInHours: Int? = null,
    // Indikator permintaan (dipakai Hot Opportunity dan For You).
    val baseSold: Int = 0,
    val baseWishlist: Int = 0,
    val viewCount: Int = 0,
    val viewsLast7Days: Int = 0,
    // Id user yang membuat event (untuk Kelola Event). 0 = bawaan sistem.
    val createdBy: String? = null
)

enum class ListingStatus(val label: String) {
    AKTIF("Aktif"),
    TERJUAL("Terjual"),
    NONAKTIF("Nonaktif")
}

data class TicketListing(
    val id: Int,
    val eventId: Int,
    val sellerId: String,
    val seatCategory: String,
    val row: String,
    val faceValue: Long,
    val price: Long,
    val quantity: Int,
    val status: ListingStatus = ListingStatus.AKTIF,
    val views: Int = 0,
    val saves: Int = 0,
    val offers: Int = 0,
    val proofPath: String? = null
)


enum class OrderStatus(val label: String) {
    BELUM_BAYAR("Belum bayar"),
    DIPROSES("Diproses"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan"),
    KEDALUWARSA("Kedaluwarsa")
}

data class Order(
    val id: Int,
    val eventId: Int,
    val listingId: Int,
    val buyerId: String,
    val sellerId: String,
    val buyerName: String,
    val quantity: Int,
    val seatLabel: String,
    val ticketPrice: Long,
    val serviceFee: Long,
    val total: Long,
    val paymentMethod: String,
    val transactionNo: String,
    val createdAt: String,
    val status: OrderStatus,
    val qrCode: String? = null,
    val rating: Int = 0,
    val buyerEmail: String = "",
    val buyerPhone: String = "",
    val ticketUsedAt: String? = null,
    val paymentStatus: String = "PENDING",
    val escrowStatus: String = "PENDING",
)

data class Hotel(
    val id: Int,
    val name: String,
    val area: String,
    val latitude: Double,
    val longitude: Double,
    val pricePerNight: Long,
    val rating: Double,
    val availableRooms: Int,
    val colorStart: Long,
    val colorEnd: Long,
    val imageUrl: String? = null,
    val bookingUrl: String? = null,
    val address: String = "",
    val city: String = ""
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val city: String,
    val level: String,
    val referralCode: String,
    val isKycVerified: Boolean,
    val recommendationNotificationsEnabled: Boolean = true,
    val membershipPoints: Long = 0L,
    val membershipEligibleSpending: Long = 0L
)

// Event + ringkasan listing-nya, dipakai kartu event di Beranda, Cari, For You, dll.
data class EventSummary(
    val event: Event,
    val lowestPrice: Long,
    val ticketCount: Int,
    val isSaved: Boolean
)

// Data mentah permintaan per event, bahan hitung Hot Opportunity.
data class EventDemandStats(
    val event: Event,
    val soldCount: Int,
    val wishlistCount: Int,
    val viewCount: Int,
    val viewsLast7Days: Int,
    val availableStock: Int
)
