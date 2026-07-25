package com.devshiv.adikala.repository

import android.util.Log
import com.devshiv.adikala.api.ShopifyApiService
import com.devshiv.adikala.model.*
import kotlinx.coroutines.Dispatchers
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


    suspend fun getCategoriesForChips(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val menuResponse = apiService.getMainMenu()
            val collectionsResponse = apiService.getCollections()

            val collectionMap = collectionsResponse.collections?.edges
                ?.mapNotNull { it.node }
                ?.associateBy { it.title?.lowercase()?.trim() ?: "" } ?: emptyMap()

            val categories = menuResponse.menu?.items
                ?.filter { item ->
                    val title = item.title?.lowercase()?.trim() ?: ""
                    val url = item.url?.lowercase() ?: ""
                    title.isNotEmpty() &&
                            title !in excludedTitles &&
                            !url.contains("discount") &&
                            !url.contains("pages/") &&
                            item.type == "COLLECTION"
                }
                ?.mapNotNull { item ->
                    val handle = extractHandle(item)
                    val collection = collectionMap[item.title?.lowercase()?.trim() ?: ""]
                    var imageUrl = collection?.image?.url ?: ""

                    if (imageUrl.isBlank() && handle.isNotEmpty()) {
                        try {
                            val collectionData = apiService.getCollectionByHandle(handle, first = 1)
                            val firstProduct = collectionData.collectionByHandle?.products?.edges?.firstOrNull()?.node
                            imageUrl = firstProduct?.featuredImage?.url
                                ?: firstProduct?.images?.edges?.firstOrNull()?.node?.url
                                        ?: ""
                        } catch (e: Exception) {
                        }
                    }

                    val subCategories = item.items?.mapNotNull { subItem ->
                        val subHandle = extractHandle(subItem)
                        if (subHandle.isNotEmpty()) {
                            var subImageUrl = ""
                            try {
                                val subCollectionData = apiService.getCollectionByHandle(subHandle, first = 1)
                                val firstProduct = subCollectionData.collectionByHandle?.products?.edges?.firstOrNull()?.node
                                subImageUrl = firstProduct?.featuredImage?.url
                                    ?: firstProduct?.images?.edges?.firstOrNull()?.node?.url
                                            ?: ""
                            } catch (e: Exception) {
                            }

                            SubCategory(
                                id = subItem.id.hashCode(),
                                name = subItem.title ?: "",
                                handle = subHandle,
                                image = subImageUrl
                            )
                        } else null
                    } ?: emptyList()

                    Category(
                        id = item.id.hashCode(),
                        name = item.title ?: "",
                        handle = handle,
                        image = imageUrl,
                        subCategories = subCategories
                    )
                } ?: emptyList()

            Log.d("HomeRepository", "Categories loaded: ${categories.size}")
            Result.success(categories)
        } catch (e: Exception) {
            Log.e("HomeRepository", "Failed to load categories", e)
            Result.failure(e)
        }
    }

    suspend fun getFirstProductImage(handle: String): String {
        return try {
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

    suspend fun getProductsByCollectionSorted(
        collectionHandle: String,
        sortKey: String,
        reverse: Boolean
    ): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val allProducts = apiService.getAllProductsByCollectionSorted(collectionHandle, sortKey, reverse)
            val products = allProducts.map { ModelMapper.mapToProduct(it) }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdikalaStoreCollections(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val collectionsResponse = apiService.getCollections()
            val allCollections = collectionsResponse.collections?.edges?.mapNotNull { it.node } ?: emptyList()

            val categories = allCollections
                .filter { collection ->
                    val handle = collection.handle?.lowercase()?.trim() ?: ""
                    val imageUrl = collection.image?.url
                    val notExcluded = handle !in excludedHandles
                    val hasValidImage = !imageUrl.isNullOrBlank()
                    val productCount = collection.products?.edges?.size ?: 0
                    notExcluded && hasValidImage && productCount > 0
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

    suspend fun searchProducts(query: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchProducts(query)
            val products = response.products?.edges?.mapNotNull { edge ->
                edge.node?.let { ModelMapper.mapToProduct(it) }
            } ?: emptyList()
            Result.success(products)
        } catch (e: Exception) {
            Log.e("HomeRepository", "Search failed for: $query", e)
            Result.failure(e)
        }
    }

}