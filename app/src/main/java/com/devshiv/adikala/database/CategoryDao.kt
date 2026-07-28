package com.devshiv.adikala.database

import androidx.room.*
import com.devshiv.adikala.database.entity.CategoryEntity

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY itemCount DESC")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE handle = :handle LIMIT 1")
    suspend fun getCategoryByHandle(handle: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Query("DELETE FROM categories WHERE lastUpdated < :staleTime")
    suspend fun deleteStaleCategories(staleTime: Long)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Transaction
    suspend fun replaceAllCategories(categories: List<CategoryEntity>) {
        deleteAllCategories()
        insertCategories(categories)
    }
}