package com.devshiv.adikala.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.model.Product
import com.devshiv.adikala.model.SortOption
import com.devshiv.adikala.repository.HomeRepository
import com.devshiv.adikala.repository.RatingRepository
import com.devshiv.adikala.sync.BackgroundSyncManager
import com.devshiv.adikala.sync.DataChangeEvent
import kotlinx.coroutines.launch

class ProductsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HomeRepository(application)
    private val ratingRepository = RatingRepository()
    private val syncManager = BackgroundSyncManager.getInstance(application)

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allProducts = mutableListOf<Product>()
    private var endCursor: String? = null
    private var hasNextPage = true
    private var currentSort = SortOption.FEATURED
    private var currentHandle = ""

    init {
        viewModelScope.launch {
            syncManager.dataChanged.collect { event ->
                when (event) {
                    is DataChangeEvent.ProductsChanged -> {
                        if (event.collectionHandle == currentHandle) {
                            Log.d("ProductsVM", "Background products changed, reloading...")
                            reloadProductsSilently()
                        }
                    }
                    else -> { /* Ignore other events */ }
                }
            }
        }
    }

    fun loadProductsByCollection(handle: String) {
        currentHandle = handle
        allProducts.clear()
        endCursor = null
        hasNextPage = true
        currentSort = SortOption.FEATURED
        loadFirstPage()
    }

    fun applySort(sort: SortOption) {
        if (currentSort == sort && allProducts.isNotEmpty()) return

        currentSort = sort

        if (sort == SortOption.MOST_RELEVANT ||
            sort == SortOption.DATE_NEW_OLD ||
            sort == SortOption.DATE_OLD_NEW) {

            allProducts.clear()
            endCursor = null
            hasNextPage = true
            loadFirstPage()
            return
        }


        if (allProducts.isNotEmpty()) {
            val sorted = sortProductsLocally(allProducts, sort)
            allProducts = sorted.toMutableList()
            _products.value = allProducts
            _filteredProducts.value = allProducts
            Log.d("ProductsVM", "Sorted locally: ${sort.label}")
        } else {
            loadFirstPage()
        }
    }



    private fun sortProductsLocally(products: List<Product>, sort: SortOption): List<Product> {
        return when (sort) {
            SortOption.FEATURED -> products
            SortOption.BEST_SELLING -> products.sortedByDescending { it.salesCount }
            SortOption.NAME_ASC -> products.sortedBy { it.name.lowercase().trim() }
            SortOption.NAME_DESC -> products.sortedByDescending { it.name.lowercase().trim() }
            SortOption.PRICE_LOW_HIGH -> products.sortedBy { it.discountedPrice }
            SortOption.PRICE_HIGH_LOW -> products.sortedByDescending { it.discountedPrice }

            SortOption.DATE_NEW_OLD -> {
                val sorted = products.sortedByDescending { product ->
                    if (product.createdAt > 0) product.createdAt
                    else product.id.toLongOrNull() ?: 0L
                }
                Log.d("ProductsVM", "DATE_NEW_OLD - First: ${sorted.firstOrNull()?.name}, createdAt: ${java.util.Date(sorted.firstOrNull()?.createdAt ?: 0)}")
                sorted
            }
            SortOption.DATE_OLD_NEW -> {
                val sorted = products.sortedBy { product ->
                    if (product.createdAt > 0) product.createdAt
                    else product.id.toLongOrNull() ?: 0L
                }
                Log.d("ProductsVM", "DATE_OLD_NEW - First: ${sorted.firstOrNull()?.name}, createdAt: ${java.util.Date(sorted.firstOrNull()?.createdAt ?: 0)}")
                sorted
            }
            SortOption.MOST_RELEVANT -> products
        }
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            _isLoading.value = true
            val (sortKey, reverse) = getSortParams(currentSort)

            repository.getProductsByCollectionPaginated(
                currentHandle, sortKey, reverse, first = 20
            ).fold(
                onSuccess = { (products, cursor) ->

                    products.take(3).forEach { product ->
                        Log.d("ProductsVM", "Product: ${product.name}, createdAt: ${product.createdAt}, date: ${java.util.Date(product.createdAt)}")
                    }
                    allProducts = products.toMutableList()
                    endCursor = cursor
                    hasNextPage = cursor != null


                    if (currentSort != SortOption.MOST_RELEVANT &&
                        currentSort != SortOption.FEATURED) {

                        if (currentSort != SortOption.DATE_NEW_OLD &&
                            currentSort != SortOption.DATE_OLD_NEW) {
                            val sorted = sortProductsLocally(allProducts, currentSort)
                            allProducts = sorted.toMutableList()
                        }
                    }

                    _products.value = allProducts
                    _filteredProducts.value = allProducts
                    _isLoading.value = false
                    loadRatingsInBackground()
                },
                onFailure = {
                    _error.value = it.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun loadMoreProducts() {
        if (!hasNextPage || _isLoadingMore.value == true) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            val (sortKey, reverse) = getSortParams(currentSort)

            repository.getProductsByCollectionPaginated(
                currentHandle, sortKey, reverse, first = 20, after = endCursor
            ).fold(
                onSuccess = { (products, cursor) ->
                    allProducts.addAll(products)
                    endCursor = cursor
                    hasNextPage = cursor != null


                    val sorted = sortProductsLocally(allProducts, currentSort)
                    allProducts = sorted.toMutableList()

                    _products.value = allProducts
                    _filteredProducts.value = allProducts
                    _isLoadingMore.value = false
                    loadRatingsInBackground()
                },
                onFailure = {
                    _error.value = it.message
                    _isLoadingMore.value = false
                }
            )
        }
    }

    private suspend fun reloadProductsSilently() {
        val (sortKey, reverse) = getSortParams(currentSort)
        repository.getProductsByCollectionPaginated(
            currentHandle, sortKey, reverse, first = 20
        ).fold(
            onSuccess = { (products, _) ->
                allProducts = products.toMutableList()


                val sorted = sortProductsLocally(allProducts, currentSort)
                allProducts = sorted.toMutableList()

                _products.postValue(allProducts)
                _filteredProducts.postValue(allProducts)
                loadRatingsInBackground()
            },
            onFailure = {

            }
        )
    }

    private suspend fun loadRatingsInBackground() {
        try {
            ratingRepository.preloadReviewsIfNeeded()
            val updated = allProducts.map { product ->
                val rating = ratingRepository.getProductRatingFast(product.handle)
                product.copy(
                    rating = rating.averageRating,
                    salesCount = rating.totalReviews
                )
            }
            allProducts = updated.toMutableList()


            val sorted = sortProductsLocally(allProducts, currentSort)
            allProducts = sorted.toMutableList()

            _products.postValue(allProducts)
            _filteredProducts.postValue(allProducts)
        } catch (e: Exception) {

        }
    }


    private fun getSortParams(sort: SortOption): Pair<String, Boolean> = when (sort) {
        SortOption.FEATURED -> "MANUAL" to false
        SortOption.BEST_SELLING -> "BEST_SELLING" to false
        SortOption.NAME_ASC -> "TITLE" to false
        SortOption.NAME_DESC -> "TITLE" to true
        SortOption.PRICE_LOW_HIGH -> "PRICE" to false
        SortOption.PRICE_HIGH_LOW -> "PRICE" to true
        SortOption.DATE_NEW_OLD -> "CREATED" to true
        SortOption.DATE_OLD_NEW -> "CREATED" to false
        SortOption.MOST_RELEVANT -> "RELEVANCE" to false
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}