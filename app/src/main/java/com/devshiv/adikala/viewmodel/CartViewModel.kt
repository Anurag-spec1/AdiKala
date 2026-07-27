package com.devshiv.adikala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.repository.CartRepository
import com.devshiv.adikala.utils.CartManager
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CartRepository()
    private val app = application

    private val _cartData = MutableLiveData<CartRepository.CartData?>()
    val cartData: LiveData<CartRepository.CartData?> = _cartData

    private val _isEmpty = MutableLiveData(true)
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _itemAdded = MutableLiveData<Boolean>()
    val itemAdded: LiveData<Boolean> = _itemAdded

    init { loadCart() }

    fun loadCart() {
        val cartId = CartManager.getCartId(app) ?: run {
            _isEmpty.value = true
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val cart = repository.getCart(cartId)
            if (cart != null && cart.items.isNotEmpty()) {
                _cartData.value = cart
                _isEmpty.value = false
            } else {
                CartManager.clear(app)
                _isEmpty.value = true
            }
            _isLoading.value = false
        }
    }

    fun addItem(variantId: String, quantity: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            val cartId = CartManager.getCartId(app)

            val cart = if (cartId == null) {
                repository.createCart(variantId, quantity)?.also {
                    CartManager.saveCartId(app, it.id)
                }
            } else {
                repository.addToCart(cartId, variantId, quantity)
            }

            if (cart != null) {
                _cartData.value = cart
                _isEmpty.value = false
                _itemAdded.value = true
            } else {
                _error.value = "Failed to add item"
            }
            _isLoading.value = false
        }
    }

    fun updateQuantity(lineId: String, quantity: Int) {
        val cartId = CartManager.getCartId(app) ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val cart = repository.updateQuantity(cartId, lineId, quantity)
            if (cart != null) {
                _cartData.value = cart
                _isEmpty.value = cart.items.isEmpty()
            }
            _isLoading.value = false
        }
    }

    fun removeItem(lineId: String) {
        val cartId = CartManager.getCartId(app) ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val cart = repository.removeFromCart(cartId, listOf(lineId))
            if (cart != null) {
                if (cart.items.isEmpty()) {
                    CartManager.clear(app)
                    _cartData.value = null
                    _isEmpty.value = true
                } else {
                    _cartData.value = cart
                    _isEmpty.value = false
                }
            }
            _isLoading.value = false
        }
    }

    fun getCheckoutUrl(): String? = _cartData.value?.checkoutUrl

    fun clearError() { _error.value = null }
    fun clearItemAdded() { _itemAdded.value = false }
}