package com.hustlers.adikala.api

import com.hustlers.adikala.model.GraphQLRequest
import com.hustlers.adikala.model.GraphQLResponse
import com.hustlers.adikala.model.ShopifyData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShopifyApiService {

    private val api = RetrofitClient.storefrontApi

    suspend fun executeGraphQL(query: String): ShopifyData = withContext(Dispatchers.IO) {
        val request = GraphQLRequest(query = query)
        val response = api.executeGraphQL(request)
        if (response.isSuccessful) {
            response.body()?.data ?: ShopifyData()
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            throw Exception(errorBody)
        }
    }

    suspend fun getProducts() = executeGraphQL(GraphQLQueries.GET_PRODUCTS)

    suspend fun getProductByHandle(handle: String) = executeGraphQL(GraphQLQueries.getProductByHandle(handle))

    suspend fun getCollections() = executeGraphQL(GraphQLQueries.GET_COLLECTIONS)

    suspend fun getCollectionByHandle(handle: String) = executeGraphQL(GraphQLQueries.getCollectionByHandle(handle))

    suspend fun searchProducts(query: String) = executeGraphQL(GraphQLQueries.searchProducts(query))

    suspend fun getProductRecommendations(productId: String) = executeGraphQL(GraphQLQueries.getProductRecommendations(productId))

    suspend fun getMenu() = executeGraphQL(GraphQLQueries.GET_MENU)

    suspend fun getShopInfo() = executeGraphQL(GraphQLQueries.GET_SHOP_INFO)
}