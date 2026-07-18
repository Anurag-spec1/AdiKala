package com.hustlers.adikala.repository

import com.hustlers.adikala.api.ShopifyApiService
import com.hustlers.adikala.model.Category
import com.hustlers.adikala.model.ModelMapper
import com.hustlers.adikala.model.PopularCollection
import com.hustlers.adikala.model.Product
import com.hustlers.adikala.model.SubCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeRepository {

    private val apiService = ShopifyApiService()

    suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCollections()
            val categories = response.collections?.edges?.mapNotNull { edge ->
                edge.node?.let { collection ->
                    Category(
                        id = collection.id.hashCode(),
                        name = collection.title ?: "",
                        handle = collection.handle ?: "",
                        image = collection.image?.url ?: ""
                    )
                }
            } ?: emptyList()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularCollections(): Result<List<PopularCollection>> = withContext(Dispatchers.IO) {
        try {
            val productsResponse = apiService.getProducts()
            val products = productsResponse.products?.edges?.mapNotNull { edge ->
                edge.node?.let { ModelMapper.mapToProduct(it) }
            }?.shuffled()?.take(6) ?: emptyList()

            val popular = products.map { product ->
                PopularCollection(
                    id = product.id.hashCode(),
                    name = product.name,
                    image = product.imageUrl,
                    handle = product.handle
                )
            }
            Result.success(popular)
        } catch (e: Exception) {
            try {
                val response = apiService.getCollections()
                val popular = response.collections?.edges?.mapNotNull { edge ->
                    edge.node?.let { collection ->
                        PopularCollection(
                            id = collection.id.hashCode(),
                            name = collection.title ?: "",
                            image = collection.image?.url ?: "",
                            handle = collection.handle ?: ""
                        )
                    }
                }?.take(6) ?: emptyList()
                Result.success(popular)
            } catch (fallbackError: Exception) {
                Result.failure(fallbackError)
            }
        }
    }

    suspend fun getCategoryWithSubCategories(collectionHandle: String): Result<Category> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCollectionByHandle(collectionHandle)
            val collection = response.collectionByHandle
                ?: throw Exception("Collection not found")

            val products = collection.products?.edges?.mapNotNull { edge ->
                edge.node?.let { ModelMapper.mapToProduct(it) }
            } ?: emptyList()

            val subCategories = products
                .flatMap { it.collections }
                .filter { it != collection.title }
                .distinct()
                .map { name ->
                    SubCategory(
                        id = name.hashCode(),
                        name = name,
                        image = products.firstOrNull { it.collections.contains(name) }?.imageUrl ?: "",
                        itemCount = products.count { it.collections.contains(name) },
                        handle = name.lowercase().replace(" ", "-")
                    )
                }
                .filter { it.itemCount > 0 }
                .take(15)

            val category = Category(
                id = collection.id.hashCode(),
                name = collection.title ?: "",
                handle = collection.handle ?: "",
                image = collection.image?.url ?: "",
                itemCount = products.size,
                subCategories = subCategories
            )

            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsByCollection(collectionHandle: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCollectionByHandle(collectionHandle)
            val products = response.collectionByHandle?.products?.edges?.mapNotNull { edge ->
                edge.node?.let { ModelMapper.mapToProduct(it) }
            } ?: emptyList()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsBySubCategory(
        collectionHandle: String,
        subCategoryName: String
    ): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCollectionByHandle(collectionHandle)
            val products = response.collectionByHandle?.products?.edges?.mapNotNull { edge ->
                edge.node?.let { ModelMapper.mapToProduct(it) }
            }?.filter { product ->
                product.collections.contains(subCategoryName)
            } ?: emptyList()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}