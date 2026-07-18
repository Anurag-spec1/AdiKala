package com.hustlers.adikala.api

import com.hustlers.adikala.model.GraphQLRequest
import com.hustlers.adikala.model.ShopifyData
import com.hustlers.adikala.model.ShopifyProduct
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
            throw Exception(response.errorBody()?.string() ?: "Unknown error")
        }
    }

    suspend fun getProducts(first: Int = 250, after: String? = null) =
        executeGraphQL(GraphQLQueries.getProducts(first, after))

    suspend fun getAllProducts(): List<ShopifyProduct> = withContext(Dispatchers.IO) {
        val allProducts = mutableListOf<ShopifyProduct>()
        var hasNextPage = true
        var cursor: String? = null

        while (hasNextPage) {
            val response = getProducts(first = 250, after = cursor)
            val products = response.products
            allProducts.addAll(products?.edges?.mapNotNull { it.node } ?: emptyList())
            hasNextPage = products?.pageInfo?.hasNextPage ?: false
            cursor = products?.pageInfo?.endCursor
        }
        allProducts
    }

    suspend fun getCollectionByHandleSorted(handle: String, sortKey: String) =
        executeGraphQL(GraphQLQueries.getCollectionWithSort(handle, sortKey))

    suspend fun getProductByHandle(handle: String) =
        executeGraphQL(GraphQLQueries.getProductByHandle(handle))

    suspend fun getCollections() =
        executeGraphQL(GraphQLQueries.GET_COLLECTIONS)

    suspend fun getCollectionByHandle(handle: String, first: Int = 250, after: String? = null) =
        executeGraphQL(GraphQLQueries.getCollectionByHandle(handle, first, after))

    suspend fun getAllProductsByCollection(handle: String): List<ShopifyProduct> = withContext(Dispatchers.IO) {
        val allProducts = mutableListOf<ShopifyProduct>()
        var hasNextPage = true
        var cursor: String? = null

        while (hasNextPage) {
            val response = getCollectionByHandle(handle, first = 250, after = cursor)
            val products = response.collectionByHandle?.products
            allProducts.addAll(products?.edges?.mapNotNull { it.node } ?: emptyList())
            hasNextPage = products?.pageInfo?.hasNextPage ?: false
            cursor = products?.pageInfo?.endCursor
        }
        allProducts
    }

    suspend fun searchProducts(query: String) =
        executeGraphQL(GraphQLQueries.searchProducts(query))

    suspend fun getMainMenu() =
        executeGraphQL(GraphQLQueries.GET_MAIN_MENU)

    suspend fun getShopInfo() =
        executeGraphQL(GraphQLQueries.GET_SHOP_INFO)
}