package com.example.tiketiin

// Daftar alamat layar (sama seperti Routes di tugas navigation).
object Routes {
    // Autentikasi
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val OTP = "otp"

    // 5 tab utama
    const val HOME = "home"
    const val FOR_YOU = "for_you"
    const val SELL = "sell"
    const val ORDERS = "orders"
    const val PROFILE = "profile"
    val TABS = setOf(HOME, FOR_YOU, SELL, ORDERS, PROFILE)

    // Alur pembeli
    const val SEARCH = "search/{category}"
    fun search(category: String = "all"): String = "search/$category"
    const val SAVED = "saved"
    const val DETAIL = "detail/{eventId}"
    fun detail(eventId: Int): String = "detail/$eventId"
    const val ORDER_FORM = "order_form"
    const val PAYMENT = "payment"
    const val PAYMENT_SUCCESS = "payment_success/{orderId}"
    fun paymentSuccess(orderId: Int): String = "payment_success/$orderId"
    const val TICKET = "ticket/{orderId}"
    fun ticket(orderId: Int): String = "ticket/$orderId"

    // Alur penjual
    const val HOT_LIST = "hot_opportunity_list"
    const val EKYC = "ekyc"
    const val SELL_FORM = "sell_form"
    const val PRICE_CHECK = "price_check"
    const val LISTING_PUBLISHED = "listing_published"
    const val MANAGE_EVENTS = "manage_events"
    const val MY_LISTINGS = "my_listings"
    const val SALES_REPORT = "sales_report"
    const val NOTIFICATIONS = "notifications"
}
