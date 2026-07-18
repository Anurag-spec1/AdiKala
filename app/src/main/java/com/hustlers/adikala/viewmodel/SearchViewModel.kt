package com.hustlers.adikala.viewmodel

import android.util.Log
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
            delay(200)
            _isLoading.value = true

            val formattedQuery = query.trim().replace(Regex("\\s+"), " ")
            Log.d("SearchVM", "Searching: '$formattedQuery'")

            repository.searchProducts(formattedQuery).fold(
                onSuccess = { products ->
                    val sorted = products.sortedByDescending { product ->
                        var score = 0
                        val name = product.name.lowercase()
                        val q = formattedQuery.lowercase()

                        if (name == q) score += 100
                        else if (name.startsWith(q)) score += 80
                        else if (name.contains(q)) score += 60
                        else if (product.collections.any { it.lowercase().contains(q) }) score += 40
                        else if (product.description.lowercase().contains(q)) score += 20

                        score
                    }
                    _searchResults.value = sorted
                    _isEmpty.value = sorted.isEmpty()
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