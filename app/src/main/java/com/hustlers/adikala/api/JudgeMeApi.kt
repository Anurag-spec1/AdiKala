package com.hustlers.adikala.api

import com.hustlers.adikala.model.JudgeMeReviewsResponse
import com.hustlers.adikala.model.JudgeMeSubmitReviewResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface JudgeMeApi {

    @GET("api/v1/reviews")
    suspend fun getProductReviews(
        @Query("shop_domain") shopDomain: String,
        @Query("api_token") apiToken: String,
        @Query("product_id") productId: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<JudgeMeReviewsResponse>

    @FormUrlEncoded
    @POST("api/v1/reviews")
    suspend fun submitReview(
        @Field("shop_domain") shopDomain: String,
        @Field("api_token") apiToken: String,
        @Field("product_id") productId: String,
        @Field("reviewer_name") reviewerName: String,
        @Field("reviewer_email") reviewerEmail: String,
        @Field("rating") rating: Int,
        @Field("title") title: String? = null,
        @Field("body") body: String? = null
    ): Response<JudgeMeSubmitReviewResponse>

    @GET("api/v1/reviews/curated")
    suspend fun getCuratedReviews(
        @Query("shop_domain") shopDomain: String,
        @Query("api_token") apiToken: String,
        @Query("product_id") productId: String
    ): Response<JudgeMeReviewsResponse>

    @Multipart
    @POST("api/v1/reviews")
    suspend fun submitReviewWithPhoto(
        @Part("shop_domain") shopDomain: RequestBody,
        @Part("api_token") apiToken: RequestBody,
        @Part("product_id") productId: RequestBody,
        @Part("reviewer_name") reviewerName: RequestBody,
        @Part("reviewer_email") reviewerEmail: RequestBody,
        @Part("rating") rating: RequestBody,
        @Part("title") title: RequestBody?,
        @Part("body") body: RequestBody?,
        @Part photo: MultipartBody.Part?
    ): Response<JudgeMeSubmitReviewResponse>
}