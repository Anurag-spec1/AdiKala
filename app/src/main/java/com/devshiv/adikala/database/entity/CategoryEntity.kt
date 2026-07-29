package com.devshiv.adikala.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val handle: String,
    val image: String = "",
    val itemCount: Int = 0,
    @ColumnInfo(name = "category_type")
    val type: String = "collection",
    val subCategoriesJson: String = "[]",
    val lastUpdated: Long = System.currentTimeMillis()
)