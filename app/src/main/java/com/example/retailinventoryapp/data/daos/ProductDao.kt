package com.example.retailinventoryapp.data.daos

import androidx.room.*
import com.example.retailinventoryapp.data.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM products")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE :query OR barcode LIKE :query")
    suspend fun searchProducts(query: String): List<ProductEntity>

    @Upsert
    suspend fun upsertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET quantityCurrent = :newStock WHERE id = :productId")
    suspend fun updateStockLevel(productId: Long, newStock: Int)

    @Query("SELECT * FROM products WHERE quantityCurrent < quantityThreshold")
    suspend fun getLowStockProducts(): List<ProductEntity>

    @Delete
    suspend fun deleteProduct(product: ProductEntity)
}