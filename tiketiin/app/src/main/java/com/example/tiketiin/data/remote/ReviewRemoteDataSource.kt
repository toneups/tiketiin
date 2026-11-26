
package com.example.tiketiin.data.remote

import com.example.tiketiin.data.remote.api.SupabaseApiService
import com.example.tiketiin.data.remote.dto.CreateReviewRequestDto
import com.example.tiketiin.data.remote.dto.ReviewDto

class ReviewRemoteDataSource(
    private val api: SupabaseApiService = RetrofitClient.api
) {

    suspend fun createReview(
        orderId: Int,
        buyerId: String,
        sellerId: String,
        rating: Int
    ): ReviewDto {

        require(orderId > 0) {
            "ID pesanan tidak valid."
        }

        require(buyerId.isNotBlank()) {
            "ID pembeli tidak valid."
        }

        require(sellerId.isNotBlank()) {
            "ID penjual tidak valid."
        }

        require(rating in 1..5) {
            "Rating harus antara 1 sampai 5."
        }

        val response = api.createReview(
            CreateReviewRequestDto(
                orderId = orderId,
                buyerId = buyerId,
                sellerId = sellerId,
                rating = rating
            )
        )

        return response.firstOrNull()
            ?: error("Supabase tidak mengembalikan data review.")
    }

    suspend fun getReviewByOrderId(
        orderId: Int
    ): ReviewDto? {

        require(orderId > 0) {
            "ID pesanan tidak valid."
        }

        return api.getReviews(
            orderId = "eq.$orderId"
        ).firstOrNull()
    }
}
