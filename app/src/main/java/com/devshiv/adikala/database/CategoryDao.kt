package com.devshiv.adikala.database

import androidx.room.*
import com.devshiv.adikala.database.entity.CategoryEntity

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY lastUpdated DESC")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE category_type = 'chip' ORDER BY lastUpdated DESC")
    suspend fun getChipCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE category_type = 'collection' AND itemCount > 0 AND image != '' ORDER BY itemCount DESC")
    suspend fun getStoreCollections(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE handle = :handle LIMIT 1")
    suspend fun getCategoryByHandle(handle: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE category_type = 'chip'")
    suspend fun deleteAllChips()

    @Query("DELETE FROM categories WHERE category_type = 'collection'")
    suspend fun deleteAllCollections()

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Transaction
    suspend fun replaceAllCategories(categories: List<CategoryEntity>) {
        deleteAllCategories()
        insertCategories(categories)
    }

    @Transaction
    suspend fun replaceChips(categories: List<CategoryEntity>) {
        deleteAllChips()
        insertCategories(categories)
    }

    @Transaction
    suspend fun replaceCollections(categories: List<CategoryEntity>) {
        deleteAllCollections()
        insertCategories(categories)
    }
}