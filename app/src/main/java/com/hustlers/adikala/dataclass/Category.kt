package com.hustlers.adikala.dataclass

import java.io.Serializable

data class SubCategory(
    val id: Int,
    val name: String
) : Serializable

data class Category(
    val id: Int,
    val name: String,
    val image: String,
    val itemCount: Int = 0,
    val subCategories: List<SubCategory> = emptyList()
) : Serializable