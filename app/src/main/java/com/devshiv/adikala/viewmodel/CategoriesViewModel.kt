package com.devshiv.adikala.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.model.Category
import com.devshiv.adikala.repository.HomeRepository
import kotlinx.coroutines.launch

class CategoriesViewModel : ViewModel() {

    private val repository = HomeRepository()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var allCategories = listOf<Category>()

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCategoriesForChips().fold(
                onSuccess = { categories ->
                    _categories.value = categories
                    // Images already loaded from cache!
                },
                onFailure = { Log.e("CategoriesVM", "Failed", it) }
            )
            _isLoading.value = false
        }
    }

    fun loadImageForCategory(position: Int) {
        if (position >= allCategories.size) return
        val category = allCategories[position]
        if (category.image.isNotBlank()) return // Already has image

        viewModelScope.launch {
            val imageUrl = repository.getFirstProductImage(category.handle)
            if (imageUrl.isNotEmpty()) {
                val updated = allCategories.toMutableList()
                updated[position] = category.copy(image = imageUrl)
                allCategories = updated
                _categories.value = allCategories
            }
        }
    }
}