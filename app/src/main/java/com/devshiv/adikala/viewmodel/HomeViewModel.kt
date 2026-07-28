package com.devshiv.adikala.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.AdikalaApplication
import com.devshiv.adikala.model.Category
import com.devshiv.adikala.repository.HomeRepository
import com.devshiv.adikala.sync.BackgroundSyncManager
import com.devshiv.adikala.sync.DataChangeEvent
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HomeRepository(application)
    private val syncManager = BackgroundSyncManager.getInstance(application)

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _adikalaStoreCollections = MutableLiveData<List<Category>>()
    val adikalaStoreCollections: LiveData<List<Category>> = _adikalaStoreCollections

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
                        Log.d("HomeVM", "Background data changed, reloading...")
                        loadHomeDataSilently()
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true

            val categoriesJob = launch {
                repository.getCategoriesForChips().fold(
                    onSuccess = { _categories.value = it },
                    onFailure = { _error.value = it.message }
                )
            }
            val collectionsJob = launch {
                repository.getAdikalaStoreCollections().fold(
                    onSuccess = { _adikalaStoreCollections.value = it },
                    onFailure = { _error.value = it.message }
                )
            }

            categoriesJob.join()
            collectionsJob.join()
            _isLoading.value = false
        }
    }

    private fun loadHomeDataSilently() {
        viewModelScope.launch {
            repository.getCategoriesForChips().fold(
                onSuccess = { _categories.postValue(it) },
                onFailure = {

                }
            )
            repository.getAdikalaStoreCollections().fold(
                onSuccess = { _adikalaStoreCollections.postValue(it) },
                onFailure = {

                }
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