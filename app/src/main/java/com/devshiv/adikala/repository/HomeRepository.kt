package com.devshiv.adikala.repository

import android.util.Log
import com.devshiv.adikala.api.ShopifyApiService
import com.devshiv.adikala.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class HomeRepository {

    private val apiService = ShopifyApiService()

    private val excludedHandles = listOf(
        "all", "all-products", "best-selling", "new-arrivals", "craft-store",
        "frontpage", "discount", "sale", "featured", "home-page"
    )

    private val excludedTitles = listOf(
        "new arrivals", "craft store", "all products", "our collections", "discount",
        "discount code"
    )

    private var cachedCollectionNodes: List<ShopifyCollection>? = null

    private suspend fun getCachedCollectionNodes(): List<ShopifyCollection> {
        if (cachedCollectionNodes != null) return cachedCollectionNodes!!
        val response = apiService.getCollections()
        cachedCollectionNodes = response.collections?.edges?.mapNotNull { it.node } ?: emptyList()
        return cachedCollectionNodes!!
    }

    suspend fun getCategoriesForChips(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val menuDeferred = async { apiService.getMainMenu() }
            val menuResponse = menuDeferred.await()

            val categories = menuResponse.menu?.items
                ?.filter { item ->
                    val title = item.title?.lowercase()?.trim() ?: ""
                    title.isNotEmpty() && title !in excludedTitles && item.type == "COLLECTION"
                }
                ?.take(15)
                ?.mapNotNull { item ->
                    val handle = extractHandle(item)
                    Category(
                        id = item.id.hashCode(),
                        name = item.title ?: "",
                        handle = handle,
                        image = "", // Will load below
                        subCategories = emptyList()
                    )
                } ?: emptyList()

            // Load images in PARALLEL for all categories
            val categoriesWithImages = categories.map { category ->
                async {
                    if (category.handle.isNotEmpty()) {
                        try {
                            val data = apiService.getCollectionByHandle(category.handle, first = 1)
                            val imageUrl = data.collectionByHandle?.products?.edges
                                ?.firstOrNull()?.node?.images?.edges
                                ?.firstOrNull()?.node?.url
                                ?: ""
                            category.copy(image = imageUrl)
                        } catch (e: Exception) {
                            category
                        }
                    } else category
                }
            }.map { it.await() }

            Log.d("HomeRepository", "Categories loaded with images: ${categoriesWithImages.size}")
            Result.success(categoriesWithImages)
        } catch (e: Exception) {
            Log.e("HomeRepository", "Failed to load categories", e)
            Result.failure(e)
        }
    }

    suspend fun getFirstProductImage(handle: String): String = withContext(Dispatchers.IO) {
        try {
            val data = apiService.getCollectionByHandle(handle, first = 1)
            data.collectionByHandle?.products?.edges?.firstOrNull()?.node?.let { product ->
                product.featuredImage?.url
                    ?: product.images?.edges?.firstOrNull()?.node?.url
                    ?: ""
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun getProductsByCollectionPaginated(
        collectionHandle: String,
        sortKey: String,
        reverse: Boolean,
        first: Int = 20,
        after: String? = null
    ): Result<Pair<List<Product>, String?>> = withContext(Dispatchers.IO) {
        try {
            val (shopifyProducts, cursor) = apiService.getProductsByCollectionPaginated(
                collectionHandle, sortKey, reverse, first, after
            )
            val products = shopifyProducts.map { ModelMapper.mapToProduct(it) }
            Result.success(Pair(products, cursor))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdikalaStoreCollections(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val allCollections = getCachedCollectionNodes()

            val categories = allCollections
                .filter { collection ->
                    val handle = collection.handle?.lowercase()?.trim() ?: ""
                    val imageUrl = collection.image?.url
                    val productCount = collection.products?.edges?.size ?: 0
                    handle !in excludedHandles && !imageUrl.isNullOrBlank() && productCount > 0
                }
                .map { collection ->
                    Category(
                        id = collection.id.hashCode(),
                        name = collection.title ?: "",
                        handle = collection.handle ?: "",
                        image = collection.image?.url ?: "",
                        itemCount = collection.products?.edges?.size ?: 0
                    )
                }
                .sortedByDescending { it.itemCount }

            Log.d("HomeRepository", "Adikala Store Collections: ${categories.size}")
            Result.success(categories)
        } catch (e: Exception) {
            Log.e("HomeRepository", "getAdikalaStoreCollections FAILED", e)
            Result.failure(e)
        }
    }

    suspend fun searchProducts(query: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchProducts(query)
            val products = response.products?.edges?.mapNotNull { edge ->
                edge.node?.let { ModelMapper.mapToProduct(it) }
            } ?: emptyList()
            Result.success(products.take(50))
        } catch (e: Exception) {
            Log.e("HomeRepository", "Search failed for: $query", e)
            Result.failure(e)
        }
    }

    private fun extractHandle(item: ShopifyMenuItem): String {
        item.url?.let { url ->
            val cleanUrl = url.split("?").first()
            if (cleanUrl.contains("/collections/")) {
                return cleanUrl.substringAfter("/collections/").trimEnd('/')
            }
        }
        item.resourceId?.let { id ->
            if (id.contains("gid://shopify/Collection/")) {
                return id.substringAfter("gid://shopify/Collection/")
            }
        }
        return item.title?.lowercase()?.trim()?.replace(" ", "-")?.replace("&", "and") ?: ""
    }
}