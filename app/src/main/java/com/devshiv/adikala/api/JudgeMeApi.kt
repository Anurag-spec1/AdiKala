package com.devshiv.adikala.api

import com.devshiv.adikala.model.JudgeMeReviewsResponse
import com.devshiv.adikala.model.JudgeMeSubmitReviewResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface JudgeMeApi {

    /**
     * Read PUBLISHED reviews (store-wide, paginated) with the public token.
     * The /reviews endpoint has NO product_handle filter — its only product filter is the
     * internal Judge.me product_id (which needs the private token to resolve), so we fetch
     * store-wide and filter by handle in the repository. Cached to keep this to a few calls.
     */
    @GET("api/v1/reviews")
    suspend fun getReviews(
        @Query("shop_domain") shopDomain: String,
        @Query("api_token") apiToken: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100   // max allowed is 100
    ): Response<JudgeMeReviewsResponse>

    /**
     * Create a review. NOTE: different host (judge.me, not api.judge.me), form-encoded,
     * and NO api_token. Required fields per Judge.me: shop_domain, platform, name, email,
     * rating, body. `id` is the EXTERNAL (Shopify numeric) product id.
     * Reviews created via the API are unverified and enter your moderation/publish queue.
     */
    @FormUrlEncoded
    @POST
    suspend fun createReview(
        @Url url: String = "https://judge.me/api/v1/reviews",
        @Field("shop_domain") shopDomain: String,
        @Field("platform") platform: String = "shopify",
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("rating") rating: Int,
        @Field("body") body: String,
        @Field("id") externalProductId: String,
        @Field("title") title: String? = null,
        @Field("picture_urls") pictureUrls: String? = null
    ): Response<JudgeMeSubmitReviewResponse>
}