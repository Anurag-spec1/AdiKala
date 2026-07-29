package com.devshiv.adikala.sync

import android.content.Context
import android.util.Log
import com.devshiv.adikala.api.ShopifyApiService
import com.devshiv.adikala.database.AppDatabase
import com.devshiv.adikala.database.entity.CategoryEntity
import com.devshiv.adikala.database.entity.SyncMetadata
import com.devshiv.adikala.database.mapper.ModelMappers
import com.devshiv.adikala.database.mapper.ModelMappers.toProductEntity
import com.devshiv.adikala.model.Category
import com.devshiv.adikala.model.ModelMapper
import com.devshiv.adikala.model.SubCategory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BackgroundSyncManager(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val apiService = ShopifyApiService()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val categoryDao = db.categoryDao()
    private val productDao = db.productDao()
    private val syncMetadataDao = db.syncMetadataDao()

    private val _dataChanged = MutableSharedFlow<DataChangeEvent>()
    val dataChanged: SharedFlow<DataChangeEvent> = _dataChanged.asSharedFlow()

    companion object {
        private const val TAG = "BackgroundSync"
        private const val CHECK_INTERVAL = 2 * 60 * 1000L

        private val excludedHandles = listOf(
            "all", "all-products", "best-selling", "new-arrivals", "craft-store",
            "frontpage", "discount", "sale", "featured", "home-page"
        )

        private val excludedTitles = listOf(
            "new arrivals", "craft store", "all products", "our collections", "discount",
            "discount code"
        )

        @Volatile
        private var INSTANCE: BackgroundSyncManager? = null

        fun getInstance(context: Context): BackgroundSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BackgroundSyncManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun startMonitoring() {
        scope.launch {
            delay(15_000)
            Log.d(TAG, "Background sync started")
            while (isActive) {
                try {
                    checkForChanges()
                } catch (e: Exception) {
                    Log.e(TAG, "Background sync error", e)
                }
                delay(CHECK_INTERVAL)
            }
        }
    }

    private suspend fun checkForChanges() {
        Log.d(TAG, "Checking for data changes...")
        checkCategoriesChanged()
        checkCollectionsChanged()
        checkAllProductsChanged()
    }


    // In BackgroundSyncManager.kt - Replace checkCategoriesChanged()

    private suspend fun checkCategoriesChanged() {
        try {
            val menuResponse = apiService.getMainMenu()
            val menuItems = menuResponse.menu?.items ?: emptyList()

            val networkCategories = menuItems
                .filter { item ->
                    val title = item.title?.lowercase()?.trim() ?: ""
                    title.isNotEmpty() && title !in excludedTitles && item.type == "COLLECTION"
                }
                .mapNotNull { item ->
                    val handle = extractHandle(item)

                    // ✅ Preserve subcategories from existing cache
                    val existingChip = categoryDao.getCategoryByHandle(handle)
                    val existingSubs = if (existingChip != null && existingChip.subCategoriesJson != "[]") {
                        ModelMappers.fromJsonList<SubCategory>(existingChip.subCategoriesJson)
                    } else {
                        // Parse from menu item if no cache
                        item.items?.mapNotNull { subItem ->
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
                    }

                    Category(
                        id = item.id.hashCode(),
                        name = item.title ?: "",
                        handle = handle,
                        image = "",
                        subCategories = existingSubs  // ✅ Use preserved subs
                    )
                }

            val cachedChips = categoryDao.getChipCategories()

            if (networkCategories.size != cachedChips.size) {
                Log.d(TAG, "Chips count changed (${cachedChips.size} → ${networkCategories.size})")
                updateChipsInCache(networkCategories)
                return
            }

            val networkFirst = networkCategories.firstOrNull()
            val cachedFirst = cachedChips.firstOrNull()

            if (networkFirst?.name != cachedFirst?.name ||
                networkFirst?.handle != cachedFirst?.handle) {
                Log.d(TAG, "Chips changed, updating...")
                updateChipsInCache(networkCategories)
            } else {
                Log.d(TAG, "Chips unchanged")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to check chips", e)
        }
    }


    private suspend fun checkCollectionsChanged() {
        try {
            val response = apiService.getCollections()
            val collectionNodes = response.collections?.edges?.mapNotNull { it.node } ?: emptyList()

            val networkCollections = collectionNodes
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

            val cachedCollections = categoryDao.getStoreCollections()

            if (networkCollections.size != cachedCollections.size) {
                Log.d(TAG, "Collections count changed, updating...")
                updateCollectionsInCache(networkCollections)
                return
            }

            val networkFirst = networkCollections.firstOrNull()
            val cachedFirst = cachedCollections.firstOrNull()

            if (networkFirst?.handle != cachedFirst?.handle) {
                Log.d(TAG, "Collections changed, updating...")
                updateCollectionsInCache(networkCollections)
            } else {
                Log.d(TAG, "Collections unchanged")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to check collections", e)
        }
    }


    private suspend fun checkAllProductsChanged() {
        try {
            val cachedCollections = categoryDao.getStoreCollections()

            for (collection in cachedCollections) {
                if (collection.handle.isEmpty()) continue

                try {
                    val (networkProducts, _) = apiService.getProductsByCollectionPaginated(
                        collection.handle, "MANUAL", false, 3, null
                    )

                    val cachedProducts = productDao.getProductsByCollection(collection.handle)

                    if (networkProducts.size != cachedProducts.size) {
                        Log.d(TAG, "Products changed in '${collection.name}', updating...")
                        updateProductsInCache(collection.handle)
                        _dataChanged.emit(DataChangeEvent.ProductsChanged(collection.handle))
                        continue
                    }

                    if (networkProducts.isNotEmpty() && cachedProducts.isNotEmpty()) {
                        val networkFirst = networkProducts.firstOrNull()
                        val cachedFirst = cachedProducts.firstOrNull()

                        if (networkFirst?.id != cachedFirst?.id ||
                            networkFirst?.title != cachedFirst?.name) {
                            Log.d(TAG, "Products changed in '${collection.name}', updating...")
                            updateProductsInCache(collection.handle)
                            _dataChanged.emit(DataChangeEvent.ProductsChanged(collection.handle))
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to check products for ${collection.name}", e)
                }
            }

            Log.d(TAG, "All products check completed")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to check all products", e)
        }
    }


// In BackgroundSyncManager.kt - Replace this function

    private suspend fun updateChipsInCache(networkCategories: List<Category>) {
        try {
            val categoriesWithImages = coroutineScope {
                networkCategories.map { category ->
                    async {
                        var updatedCategory = category

                        // Load image
                        if (category.handle.isNotEmpty()) {
                            try {
                                val data = apiService.getCollectionByHandle(category.handle, first = 1)
                                val imageUrl = data.collectionByHandle?.products?.edges
                                    ?.firstOrNull()?.node?.images?.edges
                                    ?.firstOrNull()?.node?.url ?: ""
                                updatedCategory = updatedCategory.copy(image = imageUrl)
                            } catch (e: Exception) {
                                // Keep existing
                            }
                        }

                        // Preserve subcategories from existing cache
                        val existingChip = categoryDao.getCategoryByHandle(category.handle)
                        if (existingChip != null && existingChip.subCategoriesJson != "[]") {
                            val existingSubs = ModelMappers.fromJsonList<SubCategory>(existingChip.subCategoriesJson)
                            if (existingSubs.isNotEmpty()) {
                                updatedCategory = updatedCategory.copy(subCategories = existingSubs)
                            }
                        }

                        updatedCategory
                    }
                }.awaitAll()
            }

            val entities = categoriesWithImages.map { category ->
                CategoryEntity(
                    id = category.id,
                    name = category.name,
                    handle = category.handle,
                    image = category.image,
                    itemCount = category.itemCount,
                    type = "chip",
                    subCategoriesJson = ModelMappers.toJsonString(category.subCategories),
                    lastUpdated = System.currentTimeMillis()
                )
            }
            categoryDao.replaceChips(entities)

            syncMetadataDao.insertSyncMetadata(
                SyncMetadata(
                    syncKey = "categories_chips_sync",
                    value = "synced",
                    lastSynced = System.currentTimeMillis()
                )
            )

            _dataChanged.emit(DataChangeEvent.CategoriesChanged)
            Log.d(TAG, "Chips updated in cache (${entities.size} items)")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update chips in cache", e)
        }
    }

    private suspend fun updateCollectionsInCache(networkCollections: List<Category>) {
        try {
            val entities = networkCollections.map { category ->
                CategoryEntity(
                    id = category.id,
                    name = category.name,
                    handle = category.handle,
                    image = category.image,
                    itemCount = category.itemCount,
                    type = "collection",
                    lastUpdated = System.currentTimeMillis()
                )
            }

            categoryDao.replaceCollections(entities)

            syncMetadataDao.insertSyncMetadata(
                SyncMetadata(
                    syncKey = "collections_store_sync",
                    value = "synced",
                    lastSynced = System.currentTimeMillis()
                )
            )

            _dataChanged.emit(DataChangeEvent.CollectionsChanged)
            Log.d(TAG, "Collections updated in cache (${entities.size} items)")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update collections in cache", e)
        }
    }

    private suspend fun updateProductsInCache(collectionHandle: String) {
        try {
            val (networkProducts, _) = apiService.getProductsByCollectionPaginated(
                collectionHandle, "MANUAL", false, 250, null
            )

            val products = networkProducts.map { ModelMapper.mapToProduct(it) }
            val entities = products.map { product ->
                product.toProductEntity(collectionHandle)
            }

            productDao.replaceProductsForCollection(collectionHandle, entities)

            syncMetadataDao.insertSyncMetadata(
                SyncMetadata(
                    syncKey = "collection_sync_$collectionHandle",
                    value = "synced",
                    lastSynced = System.currentTimeMillis()
                )
            )

            Log.d(TAG, "Products updated in cache for '$collectionHandle' (${entities.size} items)")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update products for $collectionHandle", e)
        }
    }

    private fun extractHandle(item: com.devshiv.adikala.model.ShopifyMenuItem): String {
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

    fun stop() {
        scope.cancel()
        Log.d(TAG, "Background sync stopped")
    }
}

sealed class DataChangeEvent {
    object CategoriesChanged : DataChangeEvent()
    object CollectionsChanged : DataChangeEvent()
    data class ProductsChanged(val collectionHandle: String) : DataChangeEvent()
}