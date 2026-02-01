package com.example.retailinventoryapp.data


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.retailinventoryapp.data.daos.ProductDao
import com.example.retailinventoryapp.data.daos.SaleDao
import com.example.retailinventoryapp.data.entities.SaleEntity
import com.example.retailinventoryapp.data.entities.SaleItemEntity
import com.example.retailinventoryapp.data.entities.ProductEntity
import com.example.retailinventoryapp.data.util.Converters

// We list all entities here. When you add Sales or Users, add them to this list.
@Database(
    entities = [ProductEntity::class, SaleEntity::class, SaleItemEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao

}