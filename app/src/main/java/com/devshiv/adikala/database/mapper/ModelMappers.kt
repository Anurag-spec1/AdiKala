package com.devshiv.adikala.database.mapper

import com.devshiv.adikala.database.entity.CategoryEntity
import com.devshiv.adikala.database.entity.ProductEntity
import com.devshiv.adikala.model.Category
import com.devshiv.adikala.model.Product
import com.devshiv.adikala.model.ProductVariant
import com.devshiv.adikala.model.SubCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ModelMappers {

    val gson = Gson()

    fun CategoryEntity.toCategory(): Category {
        return Category(
            id = id,
            name = name,
            handle = handle,
            image = image,
            itemCount = itemCount,
            subCategories = fromJsonList<SubCategory>(subCategoriesJson)
        )
    }

    fun Category.toCategoryEntity(type: String = "collection"): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            handle = handle,
            image = image,
            itemCount = itemCount,
            type = type,
            subCategoriesJson = toJsonString(subCategories),
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun List<CategoryEntity>.toCategories(): List<Category> {
        return map { it.toCategory() }
    }

    fun List<Category>.toCategoryEntities(type: String = "collection"): List<CategoryEntity> {
        return map { it.toCategoryEntity(type) }
    }


    fun ProductEntity.toProduct(): Product {
        return Product(
            id = id,
            name = name,
            handle = handle,
            description = description,
            imageUrl = imageUrl,
            actualPrice = actualPrice,
            discountedPrice = discountedPrice,
            rating = rating,
            salesCount = salesCount,
            createdAt = createdAt,
            isWishlisted = isWishlisted,
            brand = brand,
            images = fromJsonList(imagesJson),
            fullDescription = fullDescription,
            features = fromJsonList(featuresJson),
            variants = fromJsonList(variantsJson),
            collections = fromJsonList(collectionsJson)
        )
    }

    fun Product.toProductEntity(collectionHandle: String): ProductEntity {
        return ProductEntity(
            id = id,
            name = name,
            handle = handle,
            description = description,
            imageUrl = imageUrl,
            actualPrice = actualPrice,
            discountedPrice = discountedPrice,
            rating = rating,
            salesCount = salesCount,
            createdAt = createdAt,
            isWishlisted = isWishlisted,
            brand = brand,
            fullDescription = fullDescription,
            collectionHandle = collectionHandle,
            imagesJson = toJsonString(images),
            featuresJson = toJsonString(features),
            variantsJson = toJsonString(variants),
            collectionsJson = toJsonString(collections),
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun List<ProductEntity>.toProducts(): List<Product> {
        return map { it.toProduct() }
    }

    fun List<Product>.toProductEntities(collectionHandle: String): List<ProductEntity> {
        return map { it.toProductEntity(collectionHandle) }
    }

    inline fun <reified T> fromJsonList(json: String): List<T> {
        return try {
            gson.fromJson(json, object : TypeToken<List<T>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun <T> toJsonString(list: List<T>): String {
        return gson.toJson(list)
    }
}