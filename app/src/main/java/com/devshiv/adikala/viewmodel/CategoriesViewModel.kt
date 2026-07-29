package com.devshiv.adikala.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.model.Category
import com.devshiv.adikala.repository.HomeRepository
import com.devshiv.adikala.sync.BackgroundSyncManager
import com.devshiv.adikala.sync.DataChangeEvent
import kotlinx.coroutines.launch

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HomeRepository(application)
    private val syncManager = BackgroundSyncManager.getInstance(application)

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        viewModelScope.launch {
            syncManager.dataChanged.collect { event ->
                when (event) {
                    is DataChangeEvent.CategoriesChanged,
                    is DataChangeEvent.CollectionsChanged -> {
                        Log.d("CategoriesVM", "Background data changed, reloading silently...")
                        loadCategoriesSilently()
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getCategoriesForChips().fold(
                onSuccess = { categories ->
                    _categories.value = categories
                },
                onFailure = { error ->
                    _error.value = error.message
                }
            )

            _isLoading.value = false
        }
    }

    fun refreshCategories() {
        loadCategories()
    }

    private fun loadCategoriesSilently() {
        viewModelScope.launch {
            repository.getCategoriesForChips().fold(
                onSuccess = { categories ->
                    _categories.postValue(categories)
                },
                onFailure = { }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}