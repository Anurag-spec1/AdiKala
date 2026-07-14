package com.hustlers.adikala.dataclass

import java.io.Serializable

enum class OrderStatus(val label: String) {
    PLACED("Order Placed"),
    PACKED("Packed"),
    SHIPPED("Shipped"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    RETURNED("Returned")
}

data class Order(
    val orderId: String,
    val productName: String,
    val imageUrl: String,
    val itemCount: Int,
    val totalPrice: Int,
    val placedOn: String,
    val status: OrderStatus,
    val expectedDelivery: String = "",
    val deliveredOn: String = ""
) : Serializable {

    val isOngoing: Boolean
        get() = status == OrderStatus.PLACED ||
                status == OrderStatus.PACKED ||
                status == OrderStatus.SHIPPED ||
                status == OrderStatus.OUT_FOR_DELIVERY
}