package com.devshiv.adikala.model

import java.io.Serializable

data class CartItem(
    val id: String,
    val name: String,
    val imageUrl: String,
    val price: Int,
    var quantity: Int,
    val size: String = ""
) : Serializable

data class CartItemDisplay(
    val lineId: String,
    val variantId: String,
    val name: String,
    val imageUrl: String,
    val price: String,
    val size: String?,
    var quantity: Int
)