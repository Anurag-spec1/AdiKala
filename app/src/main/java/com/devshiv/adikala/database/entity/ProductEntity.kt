// com/devshiv/adikala/database/entity/ProductEntity.kt
package com.devshiv.adikala.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val handle: String,
    val description: String,
    val imageUrl: String,
    val actualPrice: Int,
    val discountedPrice: Int,
    val rating: Double = 0.0,
    val salesCount: Int = 0,
    val createdAt: Long = 0L,
    val isWishlisted: Boolean = false,
    val brand: String = "adikala.in",
    val fullDescription: String = "",
    val collectionHandle: String,
    val imagesJson: String = "[]",
    val featuresJson: String = "[]",
    val variantsJson: String = "[]",
    val collectionsJson: String = "[]",
    val lastUpdated: Long = System.currentTimeMillis()
)