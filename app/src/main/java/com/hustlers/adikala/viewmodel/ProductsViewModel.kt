package com.hustlers.adikala.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustlers.adikala.model.Product
import com.hustlers.adikala.model.SortOption
import com.hustlers.adikala.repository.HomeRepository
import kotlinx.coroutines.launch


class ProductsViewModel : ViewModel() {

    private val repository = HomeRepository()

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
        loadWithSort(currentSort)
    }

    fun applySort(sort: SortOption) {
        currentSort = sort
        loadWithSort(sort)
    }

    private fun loadWithSort(sort: SortOption) {
        viewModelScope.launch {
            _isLoading.value = true

            val sortKey = when (sort) {
                SortOption.PRICE_LOW_HIGH -> "PRICE"
                SortOption.PRICE_HIGH_LOW -> "PRICE"
                SortOption.NAME_ASC -> "TITLE"
                SortOption.NAME_DESC -> "TITLE"
                SortOption.DATE_NEW_OLD -> "CREATED"
                SortOption.DATE_OLD_NEW -> "CREATED"
                SortOption.BEST_SELLING -> "BEST_SELLING"
                SortOption.FEATURED -> "MANUAL"
                SortOption.MOST_RELEVANT -> "RELEVANCE"
            }

            repository.getProductsByCollectionSorted(currentHandle, sortKey).fold(
                onSuccess = { products ->
                    val finalProducts = when (sort) {
                        SortOption.PRICE_HIGH_LOW -> products.sortedByDescending { it.discountedPrice }
                        SortOption.PRICE_LOW_HIGH -> products
                        SortOption.NAME_DESC -> products.sortedByDescending { it.name.lowercase() }
                        SortOption.NAME_ASC -> products
                        SortOption.DATE_OLD_NEW -> products
                        SortOption.DATE_NEW_OLD -> products.sortedByDescending { it.createdAt }
                        else -> products
                    }
                    _products.value = finalProducts
                    _filteredProducts.value = finalProducts
                },
                onFailure = { _error.value = it.message }
            )

            _isLoading.value = false
        }
    }
}