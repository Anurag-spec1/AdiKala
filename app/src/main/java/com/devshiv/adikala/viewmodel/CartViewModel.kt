package com.devshiv.adikala.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.auth.ShopifyAuthManager
import com.devshiv.adikala.model.CartItemDisplay
import com.devshiv.adikala.repository.CartRepository
import com.devshiv.adikala.utils.CartManager
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CartRepository()
    private val app = application
    private val authManager = ShopifyAuthManager(application)

    private val _cartItems = MutableLiveData<List<CartItemDisplay>>()
    val cartItems: LiveData<List<CartItemDisplay>> = _cartItems

    private val _isEmpty = MutableLiveData(true)
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _totalAmount = MutableLiveData("₹0")
    val totalAmount: LiveData<String> = _totalAmount

    private val _checkoutUrl = MutableLiveData<String?>()
    val checkoutUrl: LiveData<String?> = _checkoutUrl

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _operationMessage = MutableLiveData<String?>()
    val operationMessage: LiveData<String?> = _operationMessage

    private val _itemAdded = MutableLiveData<Boolean>()
    val itemAdded: LiveData<Boolean> = _itemAdded

    private val _cartCount = MutableLiveData(CartManager.getCartCount(application))
    val cartCount: LiveData<Int> = _cartCount

    private val _suggestions = MutableLiveData<List<CartRepository.ProductSuggestion>>()
    val suggestions: LiveData<List<CartRepository.ProductSuggestion>> = _suggestions

    init {
        loadCart()
        loadSuggestions()
    }

    fun loadCart() {
        val cartId = CartManager.getCartId(app) ?: run {
            _isEmpty.value = true
            _cartCount.value = CartManager.getCartCount(app)  // Use saved count
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val cart = repository.getCart(cartId)
            if (cart != null && cart.items.isNotEmpty()) {
                _cartItems.value = cart.items.map { line ->
                    val productName = CartManager.getProductName(app, line.variantId)
                    CartItemDisplay(
                        lineId = line.lineId,
                        variantId = line.variantId,
                        name = productName ?: line.title,
                        imageUrl = line.imageUrl ?: "",
                        price = line.price,
                        size = line.size,
                        quantity = line.quantity
                    )
                }
                _totalAmount.value = cart.totalAmount
                _checkoutUrl.value = cart.checkoutUrl
                _isEmpty.value = false
                _cartCount.value = cart.totalQuantity
                CartManager.saveCartCount(app, cart.totalQuantity)

                loadDynamicSuggestions(cart.items.map { it.variantId })
            } else {
                CartManager.clear(app)
                CartManager.clearCartCount(app)
                _isEmpty.value = true
                _cartCount.value = 0
                loadBestSellers()
            }
            _isLoading.value = false
        }
    }

    private fun loadDynamicSuggestions(productIds: List<String>) {
        Log.d("CartVM", "=== LOADING DYNAMIC SUGGESTIONS ===")
        Log.d("CartVM", "Product IDs in cart: $productIds")
        viewModelScope.launch {
            val suggestions = repository.getRecommendedProducts(productIds)
            Log.d("CartVM", "Got ${suggestions.size} suggestions")
            _suggestions.value = suggestions
        }
    }

    private fun loadBestSellers() {
        viewModelScope.launch {
            _suggestions.value = repository.getBestSellers()
        }
    }

    fun addItem(variantId: String, productName: String, quantity: Int = 1, onComplete: (() -> Unit)? = null) {
        CartManager.saveProductName(app, variantId, productName)
        viewModelScope.launch {
            _isLoading.value = true
            _operationMessage.value = "Adding to cart..."

            val cartId = CartManager.getCartId(app)
            val token = authManager.getAccessToken()

            if (cartId == null) {
                val cart = repository.createCart(variantId, quantity)
                cart?.id?.let {
                    CartManager.saveCartId(app, it)
                    if (token != null) {
                        repository.setCartBuyerIdentity(it, token)
                    }
                }
            } else {
                repository.addToCart(cartId, variantId, quantity)
            }

            loadCart()
            _itemAdded.value = true
            _operationMessage.value = "Added to cart"
            _isLoading.value = false
            onComplete?.invoke()
        }
    }

    fun removeItem(lineId: String, variantId: String) {
        val cartId = CartManager.getCartId(app) ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _operationMessage.value = "Removing item..."

            val result = repository.removeFromCart(cartId, listOf(lineId))
            CartManager.removeProductName(app, variantId)

            if (result != null) {
                _cartItems.value = result.items.map { line ->
                    val productName = CartManager.getProductName(app, line.variantId)
                    CartItemDisplay(
                        lineId = line.lineId,
                        variantId = line.variantId,
                        name = productName ?: line.title,
                        imageUrl = line.imageUrl ?: "",
                        price = line.price,
                        size = line.size,
                        quantity = line.quantity
                    )
                }
                _totalAmount.value = result.totalAmount
                _checkoutUrl.value = result.checkoutUrl
                _isEmpty.value = result.items.isEmpty()
                _cartCount.value = result.totalQuantity
                CartManager.saveCartCount(app, result.totalQuantity)
            } else {
                loadCart()
            }

            _operationMessage.value = "Item removed"
            _isLoading.value = false
        }
    }

    fun updateQuantity(lineId: String, quantity: Int) {
        val cartId = CartManager.getCartId(app) ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateQuantity(cartId, lineId, quantity)

            if (result != null) {
                _cartItems.value = result.items.map { line ->
                    val productName = CartManager.getProductName(app, line.variantId)
                    CartItemDisplay(
                        lineId = line.lineId,
                        variantId = line.variantId,
                        name = productName ?: line.title,
                        imageUrl = line.imageUrl ?: "",
                        price = line.price,
                        size = line.size,
                        quantity = line.quantity
                    )
                }
                _totalAmount.value = result.totalAmount
                _checkoutUrl.value = result.checkoutUrl
                _cartCount.value = result.totalQuantity
                CartManager.saveCartCount(app, result.totalQuantity)
            } else {
                loadCart()
            }
            _isLoading.value = false
        }
    }

    fun getCheckoutUrl(): String? {
        return _checkoutUrl.value
    }

    fun getAuthenticatedCheckoutUrl(onResult: (String?) -> Unit) {
        val cartId = CartManager.getCartId(app)
        val token = authManager.getAccessToken()

        if (cartId == null || token == null) {
            onResult(null)
            return
        }

        viewModelScope.launch {
            val url = repository.getCheckoutUrlWithAuth(cartId, token)
            onResult(url)
        }
    }

    fun clearItemAdded() { _itemAdded.value = false }

    private fun loadSuggestions() {
        viewModelScope.launch {
            _suggestions.value = repository.getProductSuggestions()
        }
    }
}