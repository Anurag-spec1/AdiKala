package com.devshiv.adikala.repository

import android.util.Log
import com.devshiv.adikala.api.JudgeMeClient
import com.devshiv.adikala.model.RatingSummary
import com.devshiv.adikala.model.Review
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RatingRepository {

    private val api = JudgeMeClient.api
    private val shopDomain = JudgeMeClient.SHOP_DOMAIN
    private val apiToken = JudgeMeClient.API_TOKEN

    suspend fun getProductReviews(productId: String): Result<Pair<RatingSummary, List<Review>>> = withContext(Dispatchers.IO) {
        try {
            val numericId = extractNumericId(productId)
            Log.d("RatingRepo", "Fetching reviews for product: $numericId")

            val response = api.getProductReviews(shopDomain, apiToken, numericId)

            if (response.isSuccessful) {
                val data = response.body()

                val reviews = data?.reviews?.map { reviewData ->
                    Review(
                        id = reviewData.id ?: 0,
                        rating = reviewData.rating ?: 0,
                        title = reviewData.title ?: "",
                        body = reviewData.body ?: "",
                        reviewerName = reviewData.reviewer_name ?: "Anonymous",
                        reviewerEmail = reviewData.reviewer_email ?: "",
                        createdAt = reviewData.created_at ?: "",
                        pictureUrls = reviewData.pictures?.mapNotNull { it.url } ?: emptyList(),
                        isVerifiedBuyer = reviewData.verified_buyer ?: false
                    )
                } ?: emptyList()

                val distribution = mutableMapOf(5 to 0, 4 to 0, 3 to 0, 2 to 0, 1 to 0)
                reviews.forEach { review ->
                    distribution[review.rating] = (distribution[review.rating] ?: 0) + 1
                }

                val summary = RatingSummary(
                    averageRating = data?.rating ?: 0.0,
                    totalReviews = data?.reviewNumber ?: 0,
                    distribution = distribution
                )

                Log.d("RatingRepo", "✅ Loaded ${reviews.size} reviews, rating: ${summary.averageRating}")
                Result.success(Pair(summary, reviews))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RatingRepo", "❌ Failed: $errorBody")
                Result.success(Pair(RatingSummary(), emptyList()))
            }
        } catch (e: Exception) {
            Log.e("RatingRepo", "❌ Exception: ${e.message}")
            Result.success(Pair(RatingSummary(), emptyList()))
        }
    }

    suspend fun submitReview(
        productId: String,
        rating: Int,
        title: String?,
        body: String?,
        reviewerName: String,
        reviewerEmail: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val numericId = extractNumericId(productId)
            Log.d("RatingRepo", "Submitting review for product: $numericId")

            val response = api.submitReview(
                shopDomain = shopDomain,
                apiToken = apiToken,
                productId = numericId,
                reviewerName = reviewerName,
                reviewerEmail = reviewerEmail,
                rating = rating,
                title = title,
                body = body
            )

            if (response.isSuccessful) {
                val data = response.body()
                Log.d("RatingRepo", "✅ Review submitted, id: ${data?.id}, status: ${data?.status}")
                Result.success(data?.status == "approved" || data?.status == "pending")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RatingRepo", "❌ Submit failed: $errorBody")
                Result.failure(Exception(errorBody ?: "Failed to submit review"))
            }
        } catch (e: Exception) {
            Log.e("RatingRepo", "❌ Exception: ${e.message}")
            Result.failure(e)
        }
    }

    private fun extractNumericId(productId: String): String {
        return productId.replace(Regex("[^0-9]"), "")
    }
}