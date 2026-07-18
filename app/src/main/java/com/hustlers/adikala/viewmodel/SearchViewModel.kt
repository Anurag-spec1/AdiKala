package com.hustlers.adikala.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustlers.adikala.model.Product
import com.hustlers.adikala.repository.HomeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = HomeRepository()

    private val _searchResults = MutableLiveData<List<Product>>()
    val searchResults: LiveData<List<Product>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private var searchJob: Job? = null

    fun searchProducts(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isEmpty.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            _isLoading.value = true

            repository.searchProducts(query).fold(
                onSuccess = { products ->
                    _searchResults.value = products
                    _isEmpty.value = products.isEmpty()
                },
                onFailure = {
                    _searchResults.value = emptyList()
                    _isEmpty.value = true
                }
            )

            _isLoading.value = false
        }
    }
}