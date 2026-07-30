package com.devshiv.adikala.model

import com.devshiv.adikala.repository.CustomerRepository
import java.io.Serializable

data class OrderResponse(
    val orders: List<ShopifyOrder>,
    val hasNextPage: Boolean,
    val endCursor: String?
)

data class ShopifyOrder(
    val id: String,
    val orderNumber: Int,
    val name: String,
    val financialStatus: String?,
    val fulfillmentStatus: String?,
    val processedAt: String?,
    val cancelledAt: String?,
    val totalPrice: String,
    val currencyCode: String,
    val totalTax: String? = null,
    val email: String? = null,
    val shippingAddress: ShippingAddress? = null,
    val items: List<OrderLineItem>,
    val trackingInfo: TrackingInfo? = null
)

data class ShippingAddress(
    val name: String?,
    val address1: String?,
    val address2: String?,
    val city: String?,
    val province: String?,
    val zip: String?,
    val country: String?
)

data class OrderLineItem(
    val title: String,
    val quantity: Int,
    val price: String,
    val imageUrl: String?,
    val productHandle: String?
)

data class TrackingInfo(
    val number: String?,
    val url: String?,
    val company: String?,
    val estimatedDelivery: String?
)

data class OrderDisplay(
    val orderId: String,
    val orderNumber: Int,
    val name: String,
    val uiLifecycleStatus: String,
    val uiDisplayStatus: String,
    val date: String,
    val totalPrice: String,
    val items: List<OrderLineItem>,
    val imageUrl: String?,
    val trackingNumber: String?,
    val trackingUrl: String?,
    val trackingCompany: String?,
    val estimatedDelivery: String?,
    val isOngoing: Boolean,
    val progressStep: Int,
    val showTracker: Boolean,
    val showTrackButton: Boolean,
    val paymentStatus: String?,
    val isPaused: Boolean
)

data class Order(
    val orderId: String,
    val name: String,
    val imageUrl: String,
    val quantity: Int,
    val price: Int,
    val date: String,
    val status: OrderStatus,
    val estimatedDelivery: String = "",
    val deliveredOn: String = "",
    val trackingNumber: String? = null,
    val trackingUrl: String? = null,
    val isOngoing: Boolean = true,
    val progressStep: Int = 0,
    val showTracker: Boolean = true,
    val showTrackButton: Boolean = false,
    val isPaused: Boolean = false,
    val email: String? = null,
    val shippingAddress: String? = null,
    val totalTax: String? = null,
    val paymentMethod: String? = null
)

enum class OrderStatus(val label: String) {
    CONFIRMED("Confirmed"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    RETURNED("Returned")
}