package com.example.retailinventoryapp.data.daos

import androidx.room.*
import com.example.retailinventoryapp.data.entities.SaleEntity
import com.example.retailinventoryapp.data.entities.SaleItemEntity

import com.example.retailinventoryapp.data.entities.ProductEntity
import com.example.retailinventoryapp.data.repository.FinancialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(item: SaleItemEntity)

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    suspend fun getAllSales(): List<SaleEntity>

    // Obține vânzările de azi (folosind conversia de dată în String pentru SQLite)
    @Query("SELECT * FROM sales WHERE date(createdAt) = date('now') ORDER BY createdAt DESC")
    suspend fun getTodaysSales(): List<SaleEntity>

    @Query("UPDATE sales SET syncStatus = :status, serverId = :serverId, syncedAt = datetime('now') WHERE id = :localId")
    suspend fun updateSaleSyncStatus(localId: Long, status: String, serverId: Long)

    // Agregare pentru Dashboard-ul de Manager (necesită acces și la tabelul de produse)
    @Query("SELECT * FROM products")
    suspend fun getProductStats(): List<ProductEntity>

    // Agregare financiară returnată ca Flow
    // Calculează automat veniturile, COGS (Cost of Goods Sold) estimat și profitul
    @Query("""
        SELECT 
            SUM(totalAmount) as totalRevenue,
            CAST(SUM(totalAmount) * 0.5 AS INTEGER) as cogs,
            CAST(SUM(totalAmount) * 0.4 AS INTEGER) as expenses,
            CAST(SUM(totalAmount) * 0.1 AS INTEGER) as netProfit
        FROM sales
    """)
    fun getStoreFinancialFlow(): Flow<FinancialEntity>

    @Query("""
        SELECT 
            SUM(totalAmount) as totalRevenue,
            CAST(SUM(totalAmount) * 0.5 AS INTEGER) as cogs,
            CAST(SUM(totalAmount) * 0.4 AS INTEGER) as expenses,
            CAST(SUM(totalAmount) * 0.1 AS INTEGER) as netProfit
        FROM sales
    """)
    fun getNetworkFinancialFlow(): Flow<FinancialEntity>
}