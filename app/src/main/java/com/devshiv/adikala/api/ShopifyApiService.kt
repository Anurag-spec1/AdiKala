package com.devshiv.adikala.api

import com.devshiv.adikala.model.GraphQLRequest
import com.devshiv.adikala.model.ShopifyData
import com.devshiv.adikala.model.ShopifyProduct
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


    suspend fun getProductsByCollectionPaginated(
        handle: String,
        sortKey: String,
        reverse: Boolean,
        first: Int = 20,
        after: String? = null
    ): Pair<List<ShopifyProduct>, String?> = withContext(Dispatchers.IO) {
        val query = GraphQLQueries.getCollectionByHandleSorted(handle, sortKey, reverse, first, after)
        val response = executeGraphQL(query)
        val productsData = response.collectionByHandle?.products
        val list = productsData?.edges?.mapNotNull { it.node } ?: emptyList()
        val cursor = productsData?.pageInfo?.endCursor
        Pair(list, cursor)
    }


    suspend fun getAllProductsByCollectionSorted(
        handle: String,
        sortKey: String,
        reverse: Boolean
    ): List<ShopifyProduct> = withContext(Dispatchers.IO) {
        val allProducts = mutableListOf<ShopifyProduct>()
        var hasNextPage = true
        var cursor: String? = null

        while (hasNextPage) {
            val query =
                GraphQLQueries.getCollectionByHandleSorted(handle, sortKey, reverse, 250, cursor)
            val response = executeGraphQL(query)
            val products = response.collectionByHandle?.products
            allProducts.addAll(products?.edges?.mapNotNull { it.node } ?: emptyList())
            hasNextPage = products?.pageInfo?.hasNextPage ?: false
            cursor = products?.pageInfo?.endCursor
        }
        allProducts
    }


    suspend fun getCollections() =
        executeGraphQL(GraphQLQueries.GET_COLLECTIONS)

    suspend fun getCollectionByHandle(handle: String, first: Int = 1) =
        executeGraphQL(GraphQLQueries.getCollectionByHandle(handle, first))
    suspend fun getMainMenu() =
        executeGraphQL(GraphQLQueries.GET_MAIN_MENU)

    suspend fun searchProducts(query: String) =
        executeGraphQL(GraphQLQueries.searchProducts(query))
}