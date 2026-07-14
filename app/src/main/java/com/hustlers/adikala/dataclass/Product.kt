package com.hustlers.adikala.dataclass

import java.io.Serializable

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val actualPrice: Int,
    val discountedPrice: Int,
    val rating: Double = 0.0,
    val salesCount: Int = 0,
    val createdAt: Long = 0L,
    var isWishlisted: Boolean = false,
    val brand: String = "adikala.in",
    val images: List<String> = emptyList(),
    val fullDescription: String = "",
    val features: List<String> = emptyList()
) : Serializable