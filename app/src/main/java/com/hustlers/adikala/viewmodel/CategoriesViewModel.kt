package com.hustlers.adikala.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustlers.adikala.model.Category
import com.hustlers.adikala.model.SubCategory
import com.hustlers.adikala.repository.HomeRepository
import kotlinx.coroutines.launch

class CategoriesViewModel : ViewModel() {

    private val repository = HomeRepository()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _selectedCategory = MutableLiveData<Category>()
    val selectedCategory: LiveData<Category> = _selectedCategory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCategories().fold(
                onSuccess = { _categories.value = it },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun loadCategoryWithSubCategories(handle: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCategoryWithSubCategories(handle).fold(
                onSuccess = { _selectedCategory.value = it },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }
}