package com.hustlers.adikala.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustlers.adikala.model.Product
import com.hustlers.adikala.model.RatingSummary
import com.hustlers.adikala.model.Review
import com.hustlers.adikala.repository.RatingRepository
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {

    private val ratingRepository = RatingRepository()

    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product> = _product

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    private val _ratingSummary = MutableLiveData<RatingSummary>()
    val ratingSummary: LiveData<RatingSummary> = _ratingSummary

    private val _isLoadingReviews = MutableLiveData<Boolean>()
    val isLoadingReviews: LiveData<Boolean> = _isLoadingReviews

    private val _isSubmitting = MutableLiveData<Boolean>()
    val isSubmitting: LiveData<Boolean> = _isSubmitting

    private val _submitResult = MutableLiveData<String>()
    val submitResult: LiveData<String> = _submitResult

    fun loadProduct(product: Product) {
        _product.value = product
        loadReviews(product.id)
    }

    private fun loadReviews(productId: String) {
        viewModelScope.launch {
            _isLoadingReviews.value = true

            ratingRepository.getProductReviews(productId).fold(  // Changed from getProductRating
                onSuccess = { (summary, reviews) ->
                    _ratingSummary.value = summary
                    _reviews.value = reviews
                },
                onFailure = {
                    _ratingSummary.value = RatingSummary()
                    _reviews.value = emptyList()
                }
            )

            _isLoadingReviews.value = false
        }
    }

    fun submitReview(
        rating: Int,
        title: String?,
        body: String?,
        name: String,
        email: String
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true

            val productId = _product.value?.id ?: return@launch

            ratingRepository.submitReview(productId, rating, title, body, name, email).fold(
                onSuccess = {
                    _submitResult.value = "Review submitted successfully!"
                    loadReviews(productId)
                },
                onFailure = {
                    _submitResult.value = it.message ?: "Failed to submit review"
                }
            )

            _isSubmitting.value = false
        }
    }
}