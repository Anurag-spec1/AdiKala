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

    fun loadProductsByCollection(handle: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getProductsByCollection(handle).fold(
                onSuccess = { products ->
                    _products.value = products
                    applySort(currentSort)
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun loadProductsBySubCategory(collectionHandle: String, subCategoryName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getProductsBySubCategory(collectionHandle, subCategoryName).fold(
                onSuccess = { products ->
                    _products.value = products
                    applySort(currentSort)
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun applySort(sort: SortOption) {
        currentSort = sort
        val sorted = when (sort) {
            SortOption.NAME_ASC -> _products.value?.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> _products.value?.sortedByDescending { it.name.lowercase() }
            SortOption.PRICE_LOW_HIGH -> _products.value?.sortedBy { it.discountedPrice }
            SortOption.PRICE_HIGH_LOW -> _products.value?.sortedByDescending { it.discountedPrice }
            SortOption.DATE_NEW_OLD -> _products.value?.sortedByDescending { it.createdAt }
            SortOption.DATE_OLD_NEW -> _products.value?.sortedBy { it.createdAt }
            SortOption.BEST_SELLING -> _products.value?.sortedByDescending { it.salesCount }
            SortOption.FEATURED, SortOption.MOST_RELEVANT -> _products.value
        }
        _filteredProducts.value = sorted ?: emptyList()
    }
}