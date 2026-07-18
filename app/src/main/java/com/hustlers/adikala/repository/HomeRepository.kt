package com.hustlers.adikala.repository

import com.hustlers.adikala.api.ShopifyApiService
import com.hustlers.adikala.model.Category
import com.hustlers.adikala.model.ModelMapper
import com.hustlers.adikala.model.PopularCollection
import com.hustlers.adikala.model.Product
import com.hustlers.adikala.model.ShopifyData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeRepository {

    private val apiService = ShopifyApiService()

    suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCollections()
            val collections = response.collections?.edges?.mapNotNull { edge ->
                edge.node?.let { collection ->
                    ModelMapper.mapToCategory(collection, edge.node.id.hashCode())
                }
            } ?: emptyList()
            Result.success(collections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularCollections(): Result<List<PopularCollection>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCollections()
            val popular = response.collections?.edges?.mapNotNull { edge ->
                edge.node?.let { collection ->
                    ModelMapper.mapToPopularCollection(collection, edge.node.id.hashCode())
                }
            }?.take(8) ?: emptyList()
            Result.success(popular)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNewArrivals(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCollectionByHandle("new-arrivals")
            val products = response.collectionByHandle?.products?.edges?.mapNotNull { edge ->
                edge.node?.let { ModelMapper.mapToProduct(it) }
            } ?: emptyList()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBestSelling(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCollectionByHandle("best-selling")
            val products = response.collectionByHandle?.products?.edges?.mapNotNull { edge ->
                edge.node?.let { ModelMapper.mapToProduct(it) }
            } ?: emptyList()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}