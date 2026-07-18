package com.hustlers.adikala.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustlers.adikala.model.Category
import com.hustlers.adikala.model.PopularCollection
import com.hustlers.adikala.repository.HomeRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = HomeRepository()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _popularCollections = MutableLiveData<List<PopularCollection>>()
    val popularCollections: LiveData<List<PopularCollection>> = _popularCollections

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true

            repository.getCategories().fold(
                onSuccess = { _categories.value = it },
                onFailure = { _error.value = it.message }
            )

            repository.getPopularCollections().fold(
                onSuccess = { _popularCollections.value = it },
                onFailure = { _error.value = it.message }
            )

            _isLoading.value = false
        }
    }
}