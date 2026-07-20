package com.hustlers.adikala.api

import com.hustlers.adikala.model.AccountGraphQLResponse
import com.hustlers.adikala.model.GraphQLRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ShopifyAccountApi {

    @POST("graphql")
    suspend fun executeCustomerGraphQL(
        @Header("Authorization") accessToken: String,
        @Body request: GraphQLRequest
    ): Response<AccountGraphQLResponse>
}