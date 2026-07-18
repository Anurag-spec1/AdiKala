package com.hustlers.adikala.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustlers.adikala.model.Category
import com.hustlers.adikala.repository.HomeRepository
import kotlinx.coroutines.launch

class CategoriesViewModel : ViewModel() {

    private val repository = HomeRepository()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCategoriesForChips().fold(
                onSuccess = { categories ->
                    _categories.value = categories
                    loadMissingImages(categories)
                },
                onFailure = { Log.e("CategoriesVM", "Failed", it) }
            )
            _isLoading.value = false
        }
    }

    private suspend fun loadMissingImages(categories: List<Category>) {
        val updatedCategories = categories.map { category ->
            if (category.image.isBlank() && category.handle.isNotEmpty()) {
                val imageUrl = repository.getFirstProductImage(category.handle)
                category.copy(image = imageUrl)
            } else category
        }.map { category ->
            if (category.subCategories.isNotEmpty()) {
                val updatedSubs = category.subCategories.map { sub ->
                    if (sub.image.isBlank() && sub.handle.isNotEmpty()) {
                        val imageUrl = repository.getFirstProductImage(sub.handle)
                        sub.copy(image = imageUrl)
                    } else sub
                }
                category.copy(subCategories = updatedSubs)
            } else category
        }
        _categories.value = updatedCategories
    }
}