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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var currentSort = SortOption.FEATURED
    private var currentHandle = ""

    fun loadProductsByCollection(handle: String) {
        currentHandle = handle
        applySort(SortOption.FEATURED)
    }

    fun applySort(sort: SortOption) {
        currentSort = sort
        viewModelScope.launch {
            _isLoading.value = true

            val (sortKey, reverse) = when (sort) {
                SortOption.FEATURED -> "MANUAL" to false
                SortOption.MOST_RELEVANT -> "RELEVANCE" to false
                SortOption.BEST_SELLING -> "BEST_SELLING" to false
                SortOption.NAME_ASC -> "TITLE" to false
                SortOption.NAME_DESC -> "TITLE" to true
                SortOption.PRICE_LOW_HIGH -> "PRICE" to false
                SortOption.PRICE_HIGH_LOW -> "PRICE" to true
                SortOption.DATE_NEW_OLD -> "CREATED" to true
                SortOption.DATE_OLD_NEW -> "CREATED" to false
            }

            repository.getProductsByCollectionSorted(currentHandle, sortKey, reverse).fold(
                onSuccess = { products ->
                    _products.value = products
                    _filteredProducts.value = products
                    loadRatingsForProducts(products)
                },
                onFailure = { _error.value = it.message }
            )

            _isLoading.value = false
        }
    }

    private suspend fun loadRatingsForProducts(products: List<Product>) {
        val updatedProducts = products.map { product ->
            ratingRepository.getProductReviews(product.handle, product.id).fold(
                onSuccess = { (summary, _) ->
                    product.copy(
                        rating = summary.averageRating,
                        salesCount = summary.totalReviews
                    )
                },
                onFailure = { product }
            )
        }
        _products.value = updatedProducts
        _filteredProducts.value = updatedProducts
    }
}