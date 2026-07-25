package com.devshiv.adikala.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.model.Category
import com.devshiv.adikala.repository.HomeRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = HomeRepository()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _adikalaStoreCollections = MutableLiveData<List<Category>>()
    val adikalaStoreCollections: LiveData<List<Category>> = _adikalaStoreCollections

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true

            repository.getCategoriesForChips().fold(
                onSuccess = { _categories.value = it },
                onFailure = { _error.value = it.message }
            )

            repository.getAdikalaStoreCollections().fold(
                onSuccess = { _adikalaStoreCollections.value = it },
                onFailure = { _error.value = it.message }
            )

            _isLoading.value = false
        }
    }
}