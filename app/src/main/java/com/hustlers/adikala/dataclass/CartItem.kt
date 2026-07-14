package com.hustlers.adikala.dataclass

import java.io.Serializable

data class CartItem(
    val id: String,
    val name: String,
    val imageUrl: String,
    val price: Int,
    var quantity: Int,
    val size: String = ""
) : Serializable