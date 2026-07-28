package com.devshiv.adikala.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.model.Product
import com.devshiv.adikala.model.SortOption
import com.devshiv.adikala.repository.HomeRepository
import com.devshiv.adikala.repository.RatingRepository
import kotlinx.coroutines.launch

class ProductsViewModel : ViewModel() {

    private val repository = HomeRepository()
    private val ratingRepository = RatingRepository()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var allProducts = mutableListOf<Product>()
    private var endCursor: String? = null
    private var hasNextPage = true
    private var currentSort = SortOption.FEATURED
    private var currentHandle = ""

    fun loadProductsByCollection(handle: String) {
        currentHandle = handle
        allProducts.clear()
        endCursor = null
        hasNextPage = true
        loadFirstPage()
    }

    fun applySort(sort: SortOption) {
        currentSort = sort
        allProducts.clear()
        endCursor = null
        hasNextPage = true
        loadFirstPage()
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            _isLoading.value = true
            val (sortKey, reverse) = getSortParams(currentSort)

            repository.getProductsByCollectionPaginated(
                currentHandle, sortKey, reverse, first = 20
            ).fold(
                onSuccess = { (products, cursor) ->
                    allProducts = products.toMutableList()
                    endCursor = cursor
                    hasNextPage = cursor != null

                    // SHOW PRODUCTS IMMEDIATELY
                    _products.value = allProducts
                    _filteredProducts.value = allProducts
                    _isLoading.value = false

                    // Load ratings in background
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

    private suspend fun loadRatingsInBackground() {
        // Preload cache if needed (runs once)
        ratingRepository.preloadReviewsIfNeeded()

        // Update ratings from cache (fast)
        val updated = allProducts.map { product ->
            val rating = ratingRepository.getProductRatingFast(product.handle)
            product.copy(
                rating = rating.averageRating,
                salesCount = rating.totalReviews
            )
        }
        allProducts = updated.toMutableList()
        _products.postValue(allProducts)
        _filteredProducts.postValue(allProducts)
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
}