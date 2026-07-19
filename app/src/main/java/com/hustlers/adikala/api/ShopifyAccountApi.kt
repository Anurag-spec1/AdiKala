package com.hustlers.adikala.api

import com.hustlers.adikala.model.GraphQLRequest
import com.hustlers.adikala.model.GraphQLResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ShopifyAccountApi {

    @POST("customer/api/unstable/graphql")
    suspend fun executeCustomerGraphQL(
        @Query("operation") operationName: String,
        @Header("Authorization") authorization: String,
        @Body request: GraphQLRequest
    ): Response<GraphQLResponse>
}