package com.example.retailinventoryapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val serverId: Long? = null, // ID-ul primit de la Spring Boot după sync
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String, // e.g., COMPLETED, REFUNDED
    val syncStatus: String,    // e.g., PENDING, SYNCED
    val createdAt: LocalDateTime,
    val syncedAt: LocalDateTime? = null
)