package com.devshiv.adikala.api

import com.devshiv.adikala.model.GraphQLRequest
import com.devshiv.adikala.model.GraphQLResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ShopifyStorefrontApi {

    @POST("api/2026-01/graphql.json")
    suspend fun executeGraphQL(@Body request: GraphQLRequest): Response<GraphQLResponse>
}