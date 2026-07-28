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

    @GET("api/v1/reviews")
    suspend fun getReviews(
        @Query("shop_domain") shopDomain: String,
        @Query("api_token") apiToken: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100
    ): Response<JudgeMeReviewsResponse>

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