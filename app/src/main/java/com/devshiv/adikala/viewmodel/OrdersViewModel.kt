package com.devshiv.adikala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devshiv.adikala.auth.ShopifyAuthManager
import com.devshiv.adikala.auth.ShopifyAuthService
import com.devshiv.adikala.model.OrderDisplay
import com.devshiv.adikala.model.ShopifyOrder
import com.devshiv.adikala.repository.CustomerRepository
import kotlinx.coroutines.launch

class OrdersViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = ShopifyAuthManager(application)
    private val authService = ShopifyAuthService(application)
    private val repository = CustomerRepository()

    private val _ongoingOrders = MutableLiveData<List<OrderDisplay>>()
    val ongoingOrders: LiveData<List<OrderDisplay>> = _ongoingOrders

    private val _pastOrders = MutableLiveData<List<OrderDisplay>>()
    val pastOrders: LiveData<List<OrderDisplay>> = _pastOrders

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var endCursor: String? = null
    private var hasNextPage = false

    init { loadOrders() }

    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            val token = authService.getValidAccessToken() ?: run {
                _error.value = "Please login to view orders"
                _isLoading.value = false
                return@launch
            }
            val response = repository.getOrders(token)
            endCursor = response.endCursor
            hasNextPage = response.hasNextPage
            val allOrders = response.orders.map { mapToDisplay(it) }
            _ongoingOrders.value = allOrders.filter { it.uiLifecycleStatus == "ONGOING" }
            _pastOrders.value = allOrders.filter { it.uiLifecycleStatus == "PAST" }
            _isLoading.value = false
        }
    }

    fun loadMoreOrders() {
        if (!hasNextPage || _isLoadingMore.value == true) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val token = authService.getValidAccessToken() ?: return@launch
            val response = repository.getOrders(token, after = endCursor)
            endCursor = response.endCursor
            hasNextPage = response.hasNextPage
            val newOrders = response.orders.map { mapToDisplay(it) }
            _ongoingOrders.value = (_ongoingOrders.value ?: emptyList()) + newOrders.filter { it.uiLifecycleStatus == "ONGOING" }
            _pastOrders.value = (_pastOrders.value ?: emptyList()) + newOrders.filter { it.uiLifecycleStatus == "PAST" }
            _isLoadingMore.value = false
        }
    }

    private fun mapToDisplay(order: ShopifyOrder): OrderDisplay {
        val fulfillmentStatus = order.fulfillmentStatus
        val financialStatus = order.financialStatus
        val hasTracking = order.trackingInfo?.number != null || order.trackingInfo?.url != null

        // Lifecycle status
        val uiLifecycleStatus = when (fulfillmentStatus) {
            "FULFILLED", "CANCELLED", "RESTOCKED" -> "PAST"
            else -> when (financialStatus) {
                "REFUNDED", "VOIDED" -> "PAST"
                else -> "ONGOING"
            }
        }

        // Display status text
        val uiDisplayStatus = when {
            order.cancelledAt != null -> "Cancelled"
            financialStatus == "REFUNDED" -> "Refunded"
            financialStatus == "PARTIALLY_REFUNDED" -> "Partially Refunded"
            financialStatus == "PENDING" || financialStatus == "AUTHORIZED" -> "Payment Pending"
            hasTracking && fulfillmentStatus == "FULFILLED" -> "Out for Delivery"
            hasTracking -> "Shipped"
            fulfillmentStatus == "FULFILLED" -> "Delivered"
            fulfillmentStatus == "PARTIALLY_FULFILLED" -> "Partially Shipped"
            fulfillmentStatus == "IN_PROGRESS" -> "Processing"
            fulfillmentStatus == "UNFULFILLED" -> "Confirmed"
            fulfillmentStatus == "ON_HOLD" -> "On Hold"
            fulfillmentStatus == "SCHEDULED" -> "Scheduled"
            else -> fulfillmentStatus ?: "Processing"
        }

        // Progress tracker step (0=Placed, 1=Packed, 2=Shipped, 3=Out)
        val progressStep = when {
            hasTracking && fulfillmentStatus == "FULFILLED" -> 3
            hasTracking -> 2
            fulfillmentStatus == "PARTIALLY_FULFILLED" || fulfillmentStatus == "IN_PROGRESS" -> 1
            else -> 0
        }

        // Show tracker for non-terminal ongoing orders
        val showTracker = uiLifecycleStatus == "ONGOING" &&
                fulfillmentStatus !in listOf("CANCELLED", "RESTOCKED") &&
                financialStatus !in listOf("REFUNDED", "VOIDED")

        // Show track button when tracking info exists
        val showTrackButton = hasTracking

        // Payment badge
        val paymentStatus = when (financialStatus) {
            "PENDING", "AUTHORIZED" -> "Payment Pending"
            "PARTIALLY_REFUNDED" -> "Partially Refunded"
            else -> null
        }

        // Paused state
        val isPaused = fulfillmentStatus in listOf("ON_HOLD", "SCHEDULED")

        return OrderDisplay(
            orderId = order.id,
            orderNumber = order.orderNumber,
            name = order.name,
            uiLifecycleStatus = uiLifecycleStatus,
            uiDisplayStatus = uiDisplayStatus,
            date = order.processedAt?.take(10) ?: "",
            totalPrice = order.totalPrice,
            items = order.items,
            imageUrl = order.items.firstOrNull()?.imageUrl,
            trackingNumber = order.trackingInfo?.number,
            trackingUrl = order.trackingInfo?.url,
            trackingCompany = order.trackingInfo?.company,
            estimatedDelivery = order.trackingInfo?.estimatedDelivery?.take(10),
            isOngoing = uiLifecycleStatus == "ONGOING",
            progressStep = progressStep,
            showTracker = showTracker,
            showTrackButton = showTrackButton,
            paymentStatus = paymentStatus,
            isPaused = isPaused
        )
    }
}