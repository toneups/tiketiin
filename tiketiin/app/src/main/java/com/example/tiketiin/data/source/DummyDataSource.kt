package com.example.tiketiin.data.source

import com.example.tiketiin.data.model.Event
import com.example.tiketiin.data.model.EventCategory
import com.example.tiketiin.data.model.Hotel
import com.example.tiketiin.data.model.ListingStatus
import com.example.tiketiin.data.model.Order
import com.example.tiketiin.data.model.OrderStatus
import com.example.tiketiin.data.model.TicketListing
import com.example.tiketiin.data.model.User
import java.time.LocalDate

/**
 * Sumber data SEMENTARA (data dummy di memori), perannya sama seperti
 * StudentLocalDataSource di tugas navigation.
 *
 * Nanti saat Supabase siap, buat SupabaseDataSource dengan fungsi yang sama,
 * lalu ganti di DefaultTiketiinRepository. Layar dan ViewModel tidak perlu diubah.
 *
 * Tanggal event dibuat relatif dari hari ini supaya demo selalu punya event aktif.
 */
object DummyDataSource {

    const val CURRENT_USER_ID =
        "00000000-0000-4000-8000-000000000001"

    private fun dummyUserId(id: Int): String {
        return "00000000-0000-4000-8000-" +
                id.toString().padStart(12, '0')
    }
    private val today: LocalDate = LocalDate.now()

    var isLoggedIn: Boolean = false

    var user = User(
        id = CURRENT_USER_ID,
        name = "Kayla Safania",
        email = "kaylania@gmail.com",
        phone = "085572934400",
        city = "Malang",
        level = "Silver",
        referralCode = "TKT_KYL01",
        isKycVerified = false
    )

    val events = mutableListOf(
        Event(
            1, "LALALA FESTIVAL 2027", EventCategory.MUSIK, "ICE BSD City", "Tangerang",
            today.plusDays(62), "14.30 - 23.00", -6.3003, 106.6362,
            "Festival musik tiga hari dengan deretan musisi internasional dan lokal di tengah suasana hutan kota.",
            0xFF0B1B4D, 0xFF7B2FF7, flashEndsInHours = null,
            baseSold = 240, baseWishlist = 96, viewCount = 1650, viewsLast7Days = 54
        ),
        Event(
            2, "GONG YOO ASIA FANMEETING", EventCategory.MUSIK, "Istora Senayan", "Jakarta",
            today.plusDays(12), "19.00 WIB", -6.2206, 106.8022,
            "Fanmeeting tur Asia dengan sesi bincang, permainan, dan hi-touch untuk penggemar.",
            0xFF111111, 0xFF4A4A4A, flashEndsInHours = 8,
            baseSold = 180, baseWishlist = 88, viewCount = 1800, viewsLast7Days = 61
        ),
        Event(
            3, "ROSETOPIA ASIA TOUR", EventCategory.MUSIK, "The Kasablanka", "Jakarta Selatan",
            today.plusDays(18), "19.30 WIB", -6.2241, 106.8432,
            "Konser tur Asia dengan tata panggung bertema taman mawar.",
            0xFFE96479, 0xFFF7C8A0, flashEndsInHours = 120,
            baseSold = 150, baseWishlist = 70, viewCount = 1200, viewsLast7Days = 40
        ),
        Event(
            4, "Raisa Handmade Night", EventCategory.MUSIK, "Balai Sarbini", "Jakarta",
            today.plusDays(30), "20.00 WIB", -6.2146, 106.8206,
            "Malam intim bersama Raisa dengan aransemen akustik.",
            0xFFD9486E, 0xFFF7B2C4, flashEndsInHours = 72,
            baseSold = 90, baseWishlist = 41, viewCount = 760, viewsLast7Days = 22
        ),
        Event(
            5, "LOMBA SIHIR: KONSER UJUNG TAHUN", EventCategory.MUSIK, "M Bloc Space", "Jakarta Pusat",
            today.plusDays(35), "19.00 WIB", -6.2416, 106.7996,
            "Konser penutup tahun Lomba Sihir bersama bintang tamu.",
            0xFF1A1A2E, 0xFFE94560,
            baseSold = 60, baseWishlist = 30, viewCount = 540, viewsLast7Days = 18
        ),
        Event(
            6, "Into the New World", EventCategory.SENI, "Ciputra Artpreneur", "Jakarta Selatan",
            today.plusDays(21), "16.00 WIB", -6.2238, 106.8239,
            "Pertunjukan orkestra simfoni dengan visual animasi.",
            0xFF1F6F50, 0xFFF2C14E,
            baseSold = 35, baseWishlist = 22, viewCount = 410, viewsLast7Days = 12
        ),
        Event(
            7, "Fun Walk 5K 2026", EventCategory.OLAHRAGA, "Wisma 46", "Jakarta",
            today.plusDays(15), "06.00 WIB", -6.2034, 106.8198,
            "Jalan santai 5K bareng keluarga, dapat jersey dan medali.",
            0xFF1976D2, 0xFF64B5F6,
            baseSold = 48, baseWishlist = 15, viewCount = 380, viewsLast7Days = 10
        ),
        Event(
            8, "Indonesia Golf Festival", EventCategory.OLAHRAGA, "ICE BSD City", "Tangerang",
            today.plusDays(8), "09.00 WIB", -6.3003, 106.6362,
            "Pameran dan festival golf terbesar di Indonesia.",
            0xFF2E7D32, 0xFFA5D6A7,
            baseSold = 25, baseWishlist = 9, viewCount = 260, viewsLast7Days = 7
        ),
        Event(
            9, "Rise & Bloom 2026: Midlife Renewal", EventCategory.SEMINAR, "Half Patiunus", "Jakarta Selatan",
            today.plusDays(44), "10.00 WIB", -6.2297, 106.8085,
            "Seminar sehari tentang memulai babak baru di usia matang.",
            0xFFF6D6C1, 0xFFE8A598,
            baseSold = 30, baseWishlist = 12, viewCount = 300, viewsLast7Days = 9
        ),
        Event(
            10, "Home Sweet Loan The Musical", EventCategory.FILM, "Teater Jakarta", "Jakarta Pusat",
            today.plusDays(26), "15.00 WIB", -6.1899, 106.8389,
            "Adaptasi panggung musikal dari film Home Sweet Loan.",
            0xFF5D4037, 0xFFD7CCC8,
            baseSold = 40, baseWishlist = 18, viewCount = 350, viewsLast7Days = 11
        ),
        Event(
            11, "Cooltura Festival 2026", EventCategory.MUSIK, "Graha Cakrawala", "Malang",
            today.plusDays(10), "15.00 WIB", -7.9609, 112.6189,
            "Festival musik dan budaya anak muda Malang.",
            0xFFFF6F00, 0xFFFFCA28, flashEndsInHours = 24,
            baseSold = 110, baseWishlist = 52, viewCount = 900, viewsLast7Days = 35
        ),
        Event(
            12, "FISTERIA 14", EventCategory.MUSIK, "Lapangan Rampal", "Malang",
            today.plusDays(16), "16.00 WIB", -7.9722, 112.6410,
            "Pentas seni tahunan dengan bintang tamu nasional.",
            0xFF6A1B9A, 0xFFCE93D8,
            baseSold = 70, baseWishlist = 28, viewCount = 520, viewsLast7Days = 20
        ),
        Event(
            13, "OPUS FEST VOL 5", EventCategory.SENI, "Gedung Kesenian Gajayana", "Malang",
            today.plusDays(4), "18.30 WIB", -7.9826, 112.6304,
            "Pertunjukan seni dan musik kolaborasi komunitas kampus.",
            0xFF00695C, 0xFF80CBC4,
            baseSold = 20, baseWishlist = 8, viewCount = 190, viewsLast7Days = 6
        ),
        // Event yang sudah lewat: harus tersaring dari semua rekomendasi.
        Event(
            14, "Jazz Kampus 2026", EventCategory.MUSIK, "Samantha Krida", "Malang",
            today.minusDays(3), "19.00 WIB", -7.9527, 112.6140,
            "Sudah selesai.",
            0xFF37474F, 0xFF90A4AE,
            baseSold = 300, baseWishlist = 120, viewCount = 2000, viewsLast7Days = 80
        )
    )

    val listings = mutableListOf(
        TicketListing(1, 1, dummyUserId(20), "CAT 1", "Row 1.12", 1_200_000, 1_350_000, 9),
        TicketListing(2, 1, dummyUserId(21), "CAT 2", "Row 4.03", 950_000, 1_400_000, 7),
        TicketListing(3, 2, dummyUserId(22), "CAT 3", "Row 5", 2_000_000, 2_500_000, 1),
        TicketListing(4, 3, dummyUserId(20), "CAT 2", "Row 8", 1_100_000, 1_300_000, 10),
        TicketListing(5, 4, dummyUserId(23), "Festival", "Free standing", 650_000, 800_000, 3),
        TicketListing(6, 5, dummyUserId(21), "Regular", "Free standing", 350_000, 450_000, 5),
        TicketListing(7, 6, dummyUserId(22), "Balcony", "Row C", 250_000, 300_000, 1),
        TicketListing(8, 7, dummyUserId(23), "5K", "-", 175_000, 190_000, 4),
        TicketListing(9, 8, CURRENT_USER_ID, "Day Pass", "-", 45_000, 50_000, 35, views = 37, saves = 4, offers = 1),
        TicketListing(10, 9, dummyUserId(20), "Regular", "Row F", 180_000, 200_000, 26),
        TicketListing(11, 10, dummyUserId(21), "Silver", "Row J", 240_000, 270_000, 3),
        TicketListing(12, 11, dummyUserId(22), "Presale 2", "Free standing", 100_000, 125_000, 2),
        TicketListing(13, 12, dummyUserId(23), "Regular", "Free standing", 75_000, 88_000, 4),
        TicketListing(14, 13, dummyUserId(20), "Regular", "Row B", 30_000, 35_000, 1),
        TicketListing(15, 14, dummyUserId(21), "Regular", "Free standing", 100_000, 120_000, 5)
    )

    // Histori harga tiket yang pernah terjual, per event (bahan Fungsi 1).
    val priceHistory: Map<Int, List<Long>> = mapOf(
        1 to listOf(1_300_000, 1_450_000, 1_500_000, 1_600_000, 1_750_000),
        2 to listOf(2_300_000, 2_450_000, 2_600_000, 2_750_000),
        3 to listOf(1_200_000, 1_300_000, 1_400_000)
    )

    val orders = mutableListOf(
        // Pesanan milik user (tab Order Saya)
        Order(
            1, 4, 5,
            CURRENT_USER_ID,
            dummyUserId(23),
            "Kayla Safania", 1, "Festival, Free standing",
            800_000, 40_000, 840_000,
            "QRIS", "000109102026", "09-10-2026 19.20",
            OrderStatus.DIPROSES
        ),

        // Pesanan masuk ke toko user (tab Order Toko)
        Order(
            2, 8, 9,
            dummyUserId(30),
            CURRENT_USER_ID,
            "Dimas Prasetyo", 2, "Day Pass",
            100_000, 5_000, 105_000,
            "Transfer Virtual Account",
            "000212102026", "12-10-2026 10.05",
            OrderStatus.DIPROSES
        ),

        Order(
            3, 8, 9,
            dummyUserId(31),
            CURRENT_USER_ID,
            "Sinta Maharani", 1, "Day Pass",
            50_000, 2_500, 52_500,
            "QRIS", "000314102026", "14-10-2026 08.41",
            OrderStatus.SELESAI,
            rating = 5
        )
    )

    val savedEventIds = mutableSetOf(1, 2, 3, 6)

    val hotels = mutableListOf(
        Hotel(1, "Citadines Sudirman", "Setiabudi, Jakarta", -6.2125, 106.8128, 1_600_000, 4.6, 5, 0xFF0D47A1, 0xFF42A5F5),
        Hotel(2, "Favehotel Gatot Subroto", "Gatot Subroto, Jakarta", -6.2262, 106.8145, 761_000, 4.2, 8, 0xFFAD1457, 0xFFF48FB1),
        Hotel(3, "Ambhara Hotel", "Blok M, Jakarta", -6.2431, 106.8009, 440_600, 4.0, 3, 0xFF6D4C41, 0xFFBCAAA4),
        Hotel(4, "Hotel Mulia Senayan", "Senayan, Jakarta", -6.2166, 106.7975, 3_200_000, 4.8, 0, 0xFF4E342E, 0xFFD7CCC8),
        Hotel(5, "The Sultan Hotel", "Gelora, Jakarta", -6.2159, 106.8085, 1_150_000, 4.4, 6, 0xFF1B5E20, 0xFF81C784),
        Hotel(6, "Ibis Styles Tanah Abang", "Tanah Abang, Jakarta", -6.1893, 106.8133, 520_000, 4.1, 4, 0xFFE65100, 0xFFFFB74D),
        Hotel(7, "Santika Premiere ICE BSD", "BSD City, Tangerang", -6.3015, 106.6384, 980_000, 4.5, 7, 0xFF283593, 0xFF7986CB),
        Hotel(8, "Hotel Tugu Malang", "Klojen, Malang", -7.9771, 112.6340, 1_250_000, 4.7, 2, 0xFF4A148C, 0xFFBA68C8),
        Hotel(9, "Ibis Styles Malang", "Blimbing, Malang", -7.9467, 112.6430, 480_000, 4.3, 9, 0xFF00838F, 0xFF4DD0E1)
    )

    fun nextEventId(): Int = (events.maxOfOrNull { it.id } ?: 0) + 1
    fun nextListingId(): Int = (listings.maxOfOrNull { it.id } ?: 0) + 1
    fun nextOrderId(): Int = (orders.maxOfOrNull { it.id } ?: 0) + 1

    fun activeListings(eventId: Int): List<TicketListing> {
        return listings.filter {
            it.eventId == eventId && it.status == ListingStatus.AKTIF && it.quantity > 0
        }
    }
}
