package com.example.retailinventoryapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: Long, // We use the ID from Spring Boot as Primary Key
    val name: String,
    val barcode: String,
    val category: String,
    val sellPrice: Double,
    val quantityCurrent: Int,
    val quantityThreshold: Int,
    val internalCode: String?,
    val syncStatus: String = "SYNCED", // LOCAL, SYNCED, PENDING
    val lastSyncedAt: Long = System.currentTimeMillis()
)