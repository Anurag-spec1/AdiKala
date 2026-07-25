package com.devshiv.adikala.model

import java.io.Serializable

data class Category(
    val id: Int,
    val name: String,
    val image: String,
    val itemCount: Int = 0,
    val subCategories: List<SubCategory> = emptyList(),
    val handle: String = ""
) : Serializable

data class SubCategory(
    val id: Int,
    val name: String,
    val image: String = "",
    val itemCount: Int = 0,
    val handle: String = ""
) : Serializable