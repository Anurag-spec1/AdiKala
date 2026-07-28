// com/devshiv/adikala/repository/HomeRepository.kt
package com.devshiv.adikala.repository

import android.content.Context
import android.util.Log
import com.devshiv.adikala.api.ShopifyApiService
import com.devshiv.adikala.database.AppDatabase
import com.devshiv.adikala.database.entity.CategoryEntity
import com.devshiv.adikala.database.entity.ProductEntity
import com.devshiv.adikala.database.entity.SyncMetadata
import com.devshiv.adikala.database.mapper.ModelMappers
import com.devshiv.adikala.database.mapper.ModelMappers.toCategories
import com.devshiv.adikala.database.mapper.ModelMappers.toEntities
import com.devshiv.adikala.database.mapper.ModelMappers.toEntity
import com.devshiv.adikala.database.mapper.ModelMappers.toProduct
import com.devshiv.adikala.database.mapper.ModelMappers.toProducts
import com.devshiv.adikala.model.*
import kotlinx.coroutines.*

class HomeRepository(context: Context) {

    private val apiService = ShopifyApiService()
    private val db = AppDatabase.getInstance(context)
    private val categoryDao = db.categoryDao()
    private val productDao = db.productDao()
    private val syncMetadataDao = db.syncMetadataDao()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "HomeRepository"
        private const val CACHE_DURATION_MS = 15 * 60 * 1000L // 15 minutes
        private const val SYNC_KEY_CATEGORIES = "categories_sync"
        private const val SYNC_KEY_COLLECTION_PREFIX = "collection_sync_"

        private val excludedHandles = listOf(
            "all", "all-products", "best-selling", "new-arrivals", "craft-store",
            "frontpage", "discount", "sale", "featured", "home-page"
        )

        private val excludedTitles = listOf(
            "new arrivals", "craft store", "all products", "our collections", "discount",
            "discount code"
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // CATEGORIES (Cache-First Strategy)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    suspend fun getCategoriesForChips(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            // STEP 1: Load from cache immediately
            val cachedCategories = categoryDao.getAllCategories()

            // STEP 2: Trigger background refresh if needed
            if (shouldRefresh(SYNC_KEY_CATEGORIES)) {
                scope.launch {
                    try {
                        refreshCategoriesInBackground()
                    } catch (e: Exception) {
                        Log.e(TAG, "Background category refresh failed", e)
                    }
                }
            }

            // STEP 3: Return cached data (or empty if no cache)
            if (cachedCategories.isNotEmpty()) {
                Log.d(TAG, "Returning ${cachedCategories.size} categories from cache")
                Result.success(cachedCategories.toCategories())
            } else {
                // No cache exists, fetch from network synchronously
                Log.d(TAG, "No cache, fetching categories from network")
                val networkCategories = fetchCategoriesFromNetwork()
                if (networkCategories.isSuccess) {
                    val categories = networkCategories.getOrDefault(emptyList())
                    // Save to cache
                    categoryDao.replaceAllCategories(categories.toEntities())
                    updateSyncTimestamp(SYNC_KEY_CATEGORIES)
                    Result.success(categories)
                } else {
                    Result.failure(networkCategories.exceptionOrNull() ?: Exception("Failed to load categories"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getCategoriesForChips", e)
            // Ultimate fallback - try cache one more time
            val fallbackCache = categoryDao.getAllCategories()
            if (fallbackCache.isNotEmpty()) {
                Result.success(fallbackCache.toCategories())
            } else {
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchCategoriesFromNetwork(): Result<List<Category>> {
        return try {
            val menuDeferred = coroutineScope { async { apiService.getMainMenu() } }
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
                        image = "",
                        subCategories = emptyList()
                    )
                } ?: emptyList()

            // Load images in parallel
            val categoriesWithImages = coroutineScope {
                categories.map { category ->
                    async {
                        if (category.handle.isNotEmpty()) {
                            try {
                                val data = apiService.getCollectionByHandle(category.handle, first = 1)
                                val imageUrl = data.collectionByHandle?.products?.edges
                                    ?.firstOrNull()?.node?.images?.edges
                                    ?.firstOrNull()?.node?.url ?: ""
                                category.copy(image = imageUrl)
                            } catch (e: Exception) {
                                category
                            }
                        } else category
                    }
                }.awaitAll()
            }

            Log.d(TAG, "Fetched ${categoriesWithImages.size} categories from network")
            Result.success(categoriesWithImages)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch categories from network", e)
            Result.failure(e)
        }
    }

    private suspend fun refreshCategoriesInBackground() {
        val networkCategories = fetchCategoriesFromNetwork()
        if (networkCategories.isSuccess) {
            val categories = networkCategories.getOrDefault(emptyList())
            categoryDao.replaceAllCategories(categories.toEntities())
            updateSyncTimestamp(SYNC_KEY_CATEGORIES)
            Log.d(TAG, "Background refresh: Updated ${categories.size} categories")
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // PRODUCTS (Cache-First Strategy)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    suspend fun getProductsByCollectionPaginated(
        collectionHandle: String,
        sortKey: String = "MANUAL",
        reverse: Boolean = false,
        first: Int = 20,
        after: String? = null
    ): Result<Pair<List<Product>, String?>> = withContext(Dispatchers.IO) {
        try {
            // STEP 1: For initial load (no cursor), try cache first
            if (after == null) {
                val cachedProducts = productDao.getProductsByCollection(collectionHandle)

                // Trigger background refresh if needed
                if (shouldRefresh(SYNC_KEY_COLLECTION_PREFIX + collectionHandle)) {
                    scope.launch {
                        try {
                            refreshProductsInBackground(collectionHandle, sortKey, reverse)
                        } catch (e: Exception) {
                            Log.e(TAG, "Background product refresh failed", e)
                        }
                    }
                }

                // Return cached data if available
                if (cachedProducts.isNotEmpty()) {
                    val products = cachedProducts.map { it.toProduct() }
                    val hasMorePages = cachedProducts.size >= first
                    val cursor = if (hasMorePages) "cached_cursor" else null

                    Log.d(TAG, "Returning ${products.size} products from cache for $collectionHandle")
                    return@withContext Result.success(Pair(products, cursor))
                }
            }

            // STEP 2: No cache or pagination - fetch from network
            val result = fetchProductsFromNetwork(collectionHandle, sortKey, reverse, first, after)

            // STEP 3: Save first page to cache
            if (after == null && result.isSuccess) {
                val (products, _) = result.getOrDefault(Pair(emptyList(), null))
                if (products.isNotEmpty()) {
                    // FIXED: Convert Product list to ProductEntity list using extension
                    val entities = products.map { product ->
                        product.toEntity(collectionHandle)
                    }
                    productDao.replaceProductsForCollection(collectionHandle, entities)
                    updateSyncTimestamp(SYNC_KEY_COLLECTION_PREFIX + collectionHandle)
                }
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting products for collection: $collectionHandle", e)
            // Fallback to cache
            if (after == null) {
                val cachedProducts = productDao.getProductsByCollection(collectionHandle)
                if (cachedProducts.isNotEmpty()) {
                    val products = cachedProducts.map { it.toProduct() }
                    Result.success(Pair(products, null))
                } else {
                    Result.failure(e)
                }
            } else {
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchProductsFromNetwork(
        collectionHandle: String,
        sortKey: String,
        reverse: Boolean,
        first: Int,
        after: String?
    ): Result<Pair<List<Product>, String?>> {
        return try {
            // Skip cursor if it's our cached placeholder
            val actualAfter = if (after == "cached_cursor") null else after

            val (shopifyProducts, cursor) = apiService.getProductsByCollectionPaginated(
                collectionHandle, sortKey, reverse, first, actualAfter
            )
            // FIXED: Use your existing ModelMapper (from api package, not database mapper)
            val products = shopifyProducts.map { ModelMapper.mapToProduct(it) }

            Log.d(TAG, "Fetched ${products.size} products from network for $collectionHandle")
            Result.success(Pair(products, cursor))
        } catch (e: Exception) {
            Log.e(TAG, "Network fetch failed for collection: $collectionHandle", e)
            Result.failure(e)
        }
    }

    private suspend fun refreshProductsInBackground(
        collectionHandle: String,
        sortKey: String,
        reverse: Boolean
    ) {
        val result = fetchProductsFromNetwork(collectionHandle, sortKey, reverse, 50, null)
        if (result.isSuccess) {
            val (products, _) = result.getOrDefault(Pair(emptyList(), null))
            if (products.isNotEmpty()) {
                // FIXED: Convert Product list to ProductEntity list
                val entities = products.map { product ->
                    product.toEntity(collectionHandle)
                }
                productDao.replaceProductsForCollection(collectionHandle, entities)
                updateSyncTimestamp(SYNC_KEY_COLLECTION_PREFIX + collectionHandle)
                Log.d(TAG, "Background refresh: Updated ${products.size} products for $collectionHandle")
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SEARCH (Network-first with Cache Fallback)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    suspend fun searchProducts(query: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            // Try network first for search (need fresh results)
            val networkResult = fetchSearchFromNetwork(query)
            if (networkResult.isSuccess) {
                val products = networkResult.getOrDefault(emptyList())
                // Cache search results
                if (products.isNotEmpty()) {
                    // FIXED: Convert to entities
                    val entities = products.map { it.toEntity("search") }
                    productDao.insertProducts(entities)
                }
                return@withContext Result.success(products)
            }

            // Network failed, try local search
            val localResults = productDao.searchProducts(query)
            if (localResults.isNotEmpty()) {
                Result.success(localResults.toProducts())
            } else {
                networkResult // Return the error
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search failed for: $query", e)
            // Final fallback
            val localResults = productDao.searchProducts(query)
            if (localResults.isNotEmpty()) {
                Result.success(localResults.toProducts())
            } else {
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchSearchFromNetwork(query: String): Result<List<Product>> {
        return try {
            val response = apiService.searchProducts(query)
            // FIXED: Use your existing ModelMapper
            val products = response.products?.edges?.mapNotNull { edge ->
                edge.node?.let { ModelMapper.mapToProduct(it) }
            } ?: emptyList()
            Result.success(products.take(50))
        } catch (e: Exception) {
            Log.e(TAG, "Network search failed for: $query", e)
            Result.failure(e)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // COLLECTIONS (Cache-First Strategy)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    suspend fun getAdikalaStoreCollections(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            // Try cache first
            val cachedCollections = categoryDao.getAllCategories()
                .filter { it.itemCount > 0 && it.image.isNotBlank() }

            if (cachedCollections.isNotEmpty() && !shouldRefresh(SYNC_KEY_CATEGORIES)) {
                Log.d(TAG, "Returning ${cachedCollections.size} collections from cache")
                return@withContext Result.success(cachedCollections.toCategories())
            }

            // Refresh from network
            val result = fetchCollectionsFromNetwork()
            if (result.isSuccess) {
                val collections = result.getOrDefault(emptyList())
                if (collections.isNotEmpty()) {
                    categoryDao.replaceAllCategories(collections.toEntities())
                    updateSyncTimestamp(SYNC_KEY_CATEGORIES)
                }
                Result.success(collections)
            } else {
                // Fallback to cache
                if (cachedCollections.isNotEmpty()) {
                    Result.success(cachedCollections.toCategories())
                } else {
                    result
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting store collections", e)
            val fallbackCache = categoryDao.getAllCategories()
            if (fallbackCache.isNotEmpty()) {
                Result.success(fallbackCache.toCategories())
            } else {
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchCollectionsFromNetwork(): Result<List<Category>> {
        return try {
            val response = apiService.getCollections()
            val collectionNodes = response.collections?.edges?.mapNotNull { it.node } ?: emptyList()

            val categories = collectionNodes
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

            Log.d(TAG, "Fetched ${categories.size} collections from network")
            Result.success(categories)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch collections from network", e)
            Result.failure(e)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // RATING UPDATES
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    suspend fun updateProductRating(productId: String, rating: Double, reviewCount: Int) {
        withContext(Dispatchers.IO) {
            try {
                productDao.updateProductRating(
                    productId = productId,
                    rating = rating,
                    reviewCount = reviewCount
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update product rating", e)
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // UTILITY METHODS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private suspend fun shouldRefresh(syncKey: String): Boolean {
        val metadata = syncMetadataDao.getSyncMetadata(syncKey)
        if (metadata == null) return true // Never synced

        return System.currentTimeMillis() - metadata.lastSynced > CACHE_DURATION_MS
    }

    private suspend fun updateSyncTimestamp(syncKey: String) {
        syncMetadataDao.insertSyncMetadata(
            SyncMetadata(
                syncKey = syncKey,  // Changed from key to syncKey
                value = "synced",
                lastSynced = System.currentTimeMillis()
            )
        )
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


    fun cleanup() {
        scope.cancel()
    }
}