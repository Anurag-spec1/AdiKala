package com.devshiv.adikala.database

import androidx.room.*
import com.devshiv.adikala.database.entity.ProductEntity

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE collectionHandle = :collectionHandle ORDER BY lastUpdated DESC")
    suspend fun getProductsByCollection(collectionHandle: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE handle = :handle LIMIT 1")
    suspend fun getProductByHandle(handle: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE collectionHandle = :collectionHandle")
    suspend fun deleteProductsByCollection(collectionHandle: String)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("""
        UPDATE products 
        SET rating = :rating, salesCount = :reviewCount, lastUpdated = :lastUpdated 
        WHERE id = :productId
    """)
    suspend fun updateProductRating(
        productId: String,
        rating: Double,
        reviewCount: Int,
        lastUpdated: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchProducts(query: String): List<ProductEntity>

    @Transaction
    suspend fun replaceProductsForCollection(collectionHandle: String, products: List<ProductEntity>) {
        deleteProductsByCollection(collectionHandle)
        insertProducts(products)
    }
}