package com.example.retailinventoryapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE // Dacă ștergem vânzarea, ștergem și produsele ei
        )
    ],
    indices = [Index("saleId")]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val saleId: Long,        // Legătura cu SaleEntity
    val productId: Long,     // Legătura cu ProductEntity
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)