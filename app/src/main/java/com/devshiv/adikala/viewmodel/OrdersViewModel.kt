package com.devshiv.adikala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.auth.ShopifyAuthManager
import com.devshiv.adikala.model.ShopifyOrder
import com.devshiv.adikala.repository.CustomerRepository
import kotlinx.coroutines.launch

class OrdersViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CustomerRepository()
    private val authManager = ShopifyAuthManager(application)

    private val _ongoingOrders = MutableLiveData<List<ShopifyOrder>>()
    val ongoingOrders: LiveData<List<ShopifyOrder>> = _ongoingOrders

    private val _pastOrders = MutableLiveData<List<ShopifyOrder>>()
    val pastOrders: LiveData<List<ShopifyOrder>> = _pastOrders

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadOrders() {
        val token = authManager.getAccessToken() ?: run {
            _error.value = "Please login to view orders"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allOrders = repository.getCustomerOrders(token)

                _ongoingOrders.value = allOrders.filter {
                    it.financialStatus == "PENDING" || it.financialStatus == "PAID"
                }

                _pastOrders.value = allOrders.filter {
                    it.financialStatus == "VOIDED" ||
                            it.financialStatus == "CANCELLED" ||
                            it.financialStatus == "REFUNDED" ||
                            it.fulfillmentStatus == "FULFILLED"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load orders"
            }
            _isLoading.value = false
        }
    }
}