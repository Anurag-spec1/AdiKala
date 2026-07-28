package com.devshiv.adikala.repository

import android.util.Log
import com.devshiv.adikala.api.JudgeMeClient
import com.devshiv.adikala.model.JudgeMeReviewData
import com.devshiv.adikala.model.RatingSummary
import com.devshiv.adikala.model.Review
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class RatingRepository {

    private val api = JudgeMeClient.api
    private val shopDomain = JudgeMeClient.SHOP_DOMAIN
    private val apiToken = JudgeMeClient.API_TOKEN

    companion object {
        private const val TAG = "RatingRepo"
        private const val PER_PAGE = 100
        private const val MAX_PAGES = 30
        private const val CACHE_TTL_MS = 5 * 60 * 1000L

        @Volatile private var cache: List<JudgeMeReviewData> = emptyList()
        @Volatile private var cacheTime = 0L
        @Volatile private var isCacheLoading = false
    }

    // ── Public API ──

    suspend fun getProductReviews(
        productHandle: String,
        productId: String
    ): Result<Pair<RatingSummary, List<Review>>> = withContext(Dispatchers.IO) {
        try {
            val handle = productHandle.trim()
            if (handle.isEmpty()) {
                return@withContext Result.success(RatingSummary() to emptyList())
            }
            val all = ensureReviews(force = false)
            val matching = all.filter { it.product_handle == handle }
            Result.success(buildSummary(matching))
        } catch (e: Exception) {
            Log.e(TAG, "getProductReviews failed: ${e.message}", e)
            Result.success(RatingSummary() to emptyList())
        }
    }

    suspend fun getProductRatingFast(productHandle: String): RatingSummary = withContext(Dispatchers.IO) {
        try {
            val all = ensureReviews(force = false)
            val matching = all.filter { it.product_handle == productHandle }
            val avg = if (matching.isNotEmpty()) matching.map { it.rating ?: 0 }.average() else 0.0
            RatingSummary(averageRating = avg, totalReviews = matching.size, distribution = emptyMap())
        } catch (e: Exception) {
            RatingSummary()
        }
    }

    suspend fun preloadReviewsIfNeeded() {
        if (isCacheExpired() && !isCacheLoading) {
            withContext(Dispatchers.IO) {
                ensureReviews(force = true)
            }
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
            val reviewBody = body?.trim().orEmpty()
            if (reviewBody.isEmpty()) {
                return@withContext Result.failure(Exception("Review text is required"))
            }
            val externalId = productId.replace(Regex("[^0-9]"), "")
            if (externalId.isEmpty()) {
                return@withContext Result.failure(Exception("Invalid product id"))
            }

            val response = api.createReview(
                shopDomain = shopDomain,
                name = reviewerName,
                email = reviewerEmail,
                rating = rating,
                body = reviewBody,
                externalProductId = externalId,
                title = title?.trim()?.ifEmpty { null }
            )

            if (response.isSuccessful) {
                Log.d(TAG, "Review submitted (pending moderation): id=${response.body()?.id}")
                invalidateCache()
                Result.success(true)
            } else {
                val err = response.errorBody()?.string()
                Log.e(TAG, "Submit failed ${response.code()}: $err")
                Result.failure(Exception(err ?: "Failed to submit review"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Submit exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun invalidateCache() {
        cacheTime = 0L
    }

    // ── Private Helpers ──

    private suspend fun ensureReviews(force: Boolean): List<JudgeMeReviewData> {
        if (!force && !isCacheExpired()) {
            return cache
        }

        if (isCacheLoading) {
            delay(500)
            if (!isCacheExpired()) return cache
        }

        isCacheLoading = true
        try {
            val all = mutableListOf<JudgeMeReviewData>()
            var page = 1
            while (page <= MAX_PAGES) {
                val resp = api.getReviews(shopDomain, apiToken, page, PER_PAGE)
                if (!resp.isSuccessful) {
                    if (page == 1) throw Exception("Reviews fetch failed: HTTP ${resp.code()}")
                    break
                }
                val batch = resp.body()?.reviews ?: emptyList()
                all += batch
                if (batch.size < PER_PAGE) break
                page++
            }
            cache = all
            cacheTime = System.currentTimeMillis()
            Log.d(TAG, "Cached ${all.size} reviews across $page page(s)")
        } finally {
            isCacheLoading = false
        }
        return cache
    }

    private fun isCacheExpired(): Boolean {
        return cache.isEmpty() || (System.currentTimeMillis() - cacheTime) > CACHE_TTL_MS
    }

    private fun buildSummary(data: List<JudgeMeReviewData>): Pair<RatingSummary, List<Review>> {
        val reviews = data.map { r ->
            Review(
                id = r.id?.toLong() ?: 0L,
                rating = r.rating ?: 0,
                title = r.title ?: "",
                body = r.body ?: "",
                reviewerName = r.reviewer?.name ?: "Anonymous",
                reviewerEmail = r.reviewer?.email ?: "",
                createdAt = r.created_at ?: "",
                pictureUrls = r.pictures?.mapNotNull { it.url ?: it.urls?.original } ?: emptyList(),
                isVerifiedBuyer = r.verified_buyer ?: false
            )
        }.sortedByDescending { it.createdAt }

        val distribution = mutableMapOf(5 to 0, 4 to 0, 3 to 0, 2 to 0, 1 to 0)
        reviews.forEach { distribution[it.rating] = (distribution[it.rating] ?: 0) + 1 }

        val avg = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0

        return Pair(
            RatingSummary(averageRating = avg, totalReviews = reviews.size, distribution = distribution),
            reviews
        )
    }
}