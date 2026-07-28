package com.devshiv.adikala.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.model.Product
import com.devshiv.adikala.repository.HomeRepository
import com.devshiv.adikala.repository.RatingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = HomeRepository()
    private val ratingRepository = RatingRepository()

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
            delay(200) // debounce
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

                    // 1) Show results immediately so search feels instant.
                    _searchResults.value = sorted
                    _isEmpty.value = sorted.isEmpty()
                    _isLoading.value = false

                    // 2) Backfill ratings, then re-emit the enriched list.
                    if (sorted.isNotEmpty()) {
                        val withRatings = loadRatings(sorted)
                        // Guard against a newer search having replaced this one.
                        if (searchJob?.isActive == true) {
                            _searchResults.value = withRatings
                        }
                    }
                },
                onFailure = { error ->
                    Log.e("SearchVM", "Search failed: ${error.message}")
                    _searchResults.value = emptyList()
                    _isEmpty.value = true
                    _isLoading.value = false
                }
            )
        }
    }

    private suspend fun loadRatings(products: List<Product>): List<Product> =
        products.map { product ->
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
}