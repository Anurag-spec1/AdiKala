package com.devshiv.adikala.repository

import android.content.Context
import android.util.Log
import com.devshiv.adikala.api.ShopifyApiService
import com.devshiv.adikala.database.AppDatabase
import com.devshiv.adikala.database.entity.CategoryEntity
import com.devshiv.adikala.database.entity.SyncMetadata
import com.devshiv.adikala.database.mapper.ModelMappers.toCategories
import com.devshiv.adikala.database.mapper.ModelMappers.toCategoryEntity
import com.devshiv.adikala.database.mapper.ModelMappers.toProduct
import com.devshiv.adikala.database.mapper.ModelMappers.toProductEntity
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
        private const val CACHE_DURATION_MS = 15 * 60 * 1000L

        private const val SYNC_KEY_CATEGORIES_CHIPS = "categories_chips_sync"
        private const val SYNC_KEY_COLLECTIONS_STORE = "collections_store_sync"
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


    suspend fun getCategoriesForChips(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val cachedChips = categoryDao.getChipCategories()

            if (shouldRefresh(SYNC_KEY_CATEGORIES_CHIPS)) {
                scope.launch {
                    try {
                        refreshCategoriesChipsInBackground()
                    } catch (e: Exception) {
                        Log.e(TAG, "Background chips refresh failed", e)
                    }
                }
            }

            if (cachedChips.isNotEmpty()) {
                Log.d(TAG, "Returning ${cachedChips.size} chips from cache (instant)")
                return@withContext Result.success(cachedChips.toCategories())
            }

            Log.d(TAG, "No cache, fetching chips from network")
            fetchCategoriesChipsFromNetwork()

        } catch (e: Exception) {
            Log.e(TAG, "Error in getCategoriesForChips", e)
            val fallbackCache = categoryDao.getChipCategories()
            if (fallbackCache.isNotEmpty()) {
                Result.success(fallbackCache.toCategories())
            } else {
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchCategoriesChipsFromNetwork(): Result<List<Category>> {
        return try {
            val menuDeferred = coroutineScope { async { apiService.getMainMenu() } }
            val menuResponse = menuDeferred.await()

            val categories = menuResponse.menu?.items
                ?.filter { item ->
                    val title = item.title?.lowercase()?.trim() ?: ""
                    title.isNotEmpty() && title !in excludedTitles && item.type == "COLLECTION"
                }
                ?.mapNotNull { item ->
                    val handle = extractHandle(item)

                    val subCategories = item.items?.mapNotNull { subItem ->
                        if (subItem.type == "COLLECTION") {
                            SubCategory(
                                id = subItem.id.hashCode(),
                                name = subItem.title ?: "",
                                handle = extractHandle(subItem),
                                image = "",
                                itemCount = 0
                            )
                        } else null
                    } ?: emptyList()

                    Category(
                        id = item.id.hashCode(),
                        name = item.title ?: "",
                        handle = handle,
                        image = "",
                        subCategories = subCategories
                    )
                } ?: emptyList()

            if (categories.isNotEmpty()) {
                val entities = categories.map { it.toCategoryEntity("chip") }
                categoryDao.replaceChips(entities)
                updateSyncTimestamp(SYNC_KEY_CATEGORIES_CHIPS)
                Log.d(TAG, "Saved ${categories.size} chips without images (fast)")
            }

            scope.launch {
                try {
                    loadChipImagesInBackground(categories)
                } catch (e: Exception) {
                    Log.e(TAG, "Background image load failed", e)
                }
            }

            Log.d(TAG, "Fetched ${categories.size} chips from menu (fast)")
            Result.success(categories)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch chips", e)
            Result.failure(e)
        }
    }

    private suspend fun loadChipImagesInBackground(categories: List<Category>) {
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

        if (categoriesWithImages.isNotEmpty()) {
            val entities = categoriesWithImages.map { it.toCategoryEntity("chip") }
            categoryDao.replaceChips(entities)
            updateSyncTimestamp(SYNC_KEY_CATEGORIES_CHIPS)
            Log.d(TAG, "Background: Updated ${categoriesWithImages.size} chips with images")
        }
    }

    private suspend fun refreshCategoriesChipsInBackground() {
        val result = fetchCategoriesChipsFromNetwork()
        if (result.isSuccess) {
            Log.d(TAG, "Chips refreshed in background")
        }
    }


    suspend fun getAdikalaStoreCollections(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val cachedCollections = categoryDao.getStoreCollections()

            if (shouldRefresh(SYNC_KEY_COLLECTIONS_STORE)) {
                scope.launch {
                    try {
                        refreshCollectionsStoreInBackground()
                    } catch (e: Exception) {
                        Log.e(TAG, "Background collections refresh failed", e)
                    }
                }
            }

            if (cachedCollections.isNotEmpty()) {
                Log.d(TAG, "Returning ${cachedCollections.size} store collections from cache")
                return@withContext Result.success(cachedCollections.toCategories())
            }

            Log.d(TAG, "No cache, fetching collections from network")
            fetchCollectionsStoreFromNetwork()

        } catch (e: Exception) {
            Log.e(TAG, "Error getting store collections", e)
            val fallbackCache = categoryDao.getStoreCollections()
            if (fallbackCache.isNotEmpty()) {
                Result.success(fallbackCache.toCategories())
            } else {
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchCollectionsStoreFromNetwork(): Result<List<Category>> {
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

            if (categories.isNotEmpty()) {
                val entities = categories.map { it.toCategoryEntity("collection") }
                categoryDao.replaceCollections(entities)
                updateSyncTimestamp(SYNC_KEY_COLLECTIONS_STORE)
            }

            Log.d(TAG, "Fetched ${categories.size} store collections from API")
            Result.success(categories)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch collections", e)
            Result.failure(e)
        }
    }

    private suspend fun refreshCollectionsStoreInBackground() {
        val result = fetchCollectionsStoreFromNetwork()
        if (result.isSuccess) {
            Log.d(TAG, "Store collections refreshed")
        }
    }


    suspend fun getProductsByCollectionPaginated(
        collectionHandle: String,
        sortKey: String = "MANUAL",
        reverse: Boolean = false,
        first: Int = 20,
        after: String? = null
    ): Result<Pair<List<Product>, String?>> = withContext(Dispatchers.IO) {
        try {
            if (after == null) {
                val cachedProducts = productDao.getProductsByCollection(collectionHandle)

                if (shouldRefresh(SYNC_KEY_COLLECTION_PREFIX + collectionHandle)) {
                    scope.launch {
                        try {
                            refreshProductsInBackground(collectionHandle, sortKey, reverse)
                        } catch (e: Exception) {
                            Log.e(TAG, "Background product refresh failed", e)
                        }
                    }
                }

                if (cachedProducts.isNotEmpty()) {
                    val products = cachedProducts.map { it.toProduct() }
                    val hasMorePages = cachedProducts.size >= first
                    val cursor = if (hasMorePages) "cached_cursor" else null
                    Log.d(TAG, "Returning ${products.size} products from cache for $collectionHandle")
                    return@withContext Result.success(Pair(products, cursor))
                }
            }

            val result = fetchProductsFromNetwork(collectionHandle, sortKey, reverse, first, after)

            if (after == null && result.isSuccess) {
                val (products, _) = result.getOrDefault(Pair(emptyList(), null))
                if (products.isNotEmpty()) {
                    val entities = products.map { it.toProductEntity(collectionHandle) }
                    productDao.replaceProductsForCollection(collectionHandle, entities)
                    updateSyncTimestamp(SYNC_KEY_COLLECTION_PREFIX + collectionHandle)
                }
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting products for collection: $collectionHandle", e)
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
            val actualAfter = if (after == "cached_cursor") null else after
            val (shopifyProducts, cursor) = apiService.getProductsByCollectionPaginated(
                collectionHandle, sortKey, reverse, first, actualAfter
            )
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
                val entities = products.map { it.toProductEntity(collectionHandle) }
                productDao.replaceProductsForCollection(collectionHandle, entities)
                updateSyncTimestamp(SYNC_KEY_COLLECTION_PREFIX + collectionHandle)
                Log.d(TAG, "Background refresh: Updated ${products.size} products for $collectionHandle")
            }
        }
    }



    suspend fun searchProducts(query: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val networkResult = fetchSearchFromNetwork(query)
            if (networkResult.isSuccess) {
                val products = networkResult.getOrDefault(emptyList())
                if (products.isNotEmpty()) {
                    val entities = products.map { it.toProductEntity("search") }
                    productDao.insertProducts(entities)
                }
                return@withContext Result.success(products)
            }
            val localResults = productDao.searchProducts(query)
            if (localResults.isNotEmpty()) {
                Result.success(localResults.toProducts())
            } else {
                networkResult
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search failed for: $query", e)
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
            val products = response.products?.edges?.mapNotNull { edge ->
                edge.node?.let { ModelMapper.mapToProduct(it) }
            } ?: emptyList()
            Result.success(products.take(50))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun updateProductRating(productId: String, rating: Double, reviewCount: Int) {
        withContext(Dispatchers.IO) {
            try {
                productDao.updateProductRating(productId, rating, reviewCount)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update product rating", e)
            }
        }
    }



    private suspend fun shouldRefresh(syncKey: String): Boolean {
        val metadata = syncMetadataDao.getSyncMetadata(syncKey)
        if (metadata == null) return true
        return System.currentTimeMillis() - metadata.lastSynced > CACHE_DURATION_MS
    }

    private suspend fun updateSyncTimestamp(syncKey: String) {
        syncMetadataDao.insertSyncMetadata(
            SyncMetadata(syncKey = syncKey, value = "synced", lastSynced = System.currentTimeMillis())
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