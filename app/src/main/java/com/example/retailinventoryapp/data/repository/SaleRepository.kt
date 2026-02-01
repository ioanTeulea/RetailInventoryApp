package com.example.retailinventoryapp.data.repository

import com.example.retailinventoryapp.data.daos.SaleDao
import com.example.retailinventoryapp.data.entities.SaleEntity
import com.example.retailinventoryapp.data.entities.SaleItemEntity
import com.example.retailinventoryapp.data.network.RetailApiService
import com.example.retailinventoryapp.viewmodel.CartItem
import kotlinx.coroutines.flow.Flow
import android.util.Log
import com.example.retailinventoryapp.data.exception.ApiException
import javax.inject.Inject
import java.time.LocalDateTime

private const val TAG = "SaleRepository"

/**
 * Repository for Sale data
 * Handles sales creation, checkout, and reporting
 */
class SaleRepository @Inject constructor(
    private val saleDao: SaleDao,
    private val apiService: RetailApiService
) {

    // ========== LOCAL OPERATIONS ==========

    /**
     * Get today's stats from local cache
     */
    suspend fun getTodayStatsLocal(): TodayStatsData? {
        return try {
            val sales = saleDao.getTodaysSales()
            if (sales.isEmpty()) return null

            // SCHIMBAT: Folosim sumOf pe Double, fără .toInt()
            val totalRevenue = sales.sumOf { it.totalAmount }
            val totalTransactions = sales.size

            val recentSales = sales.take(5).map { sale ->
                RecentSaleData(
                    id = sale.id ?: 0L,
                    amount = sale.totalAmount, // SCHIMBAT: Double
                    time = sale.createdAt?.toString() ?: "N/A",
                    paymentMethod = sale.paymentMethod
                )
            }

            return TodayStatsData(
                userName = "Casier",
                totalTransactions = totalTransactions,
                totalRevenue = totalRevenue, // SCHIMBAT: Double
                recentSales = recentSales
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            null
        }
    }

    /**
     * Save sale to local cache
     */
    suspend fun saveSaleLocally(
        items: List<CartItem>,
        totalAmount: Double,
        paymentMethod: String
    ): Long {
        return try {
            Log.d(TAG, "Saving sale to local cache: $totalAmount RON")

            val sale = SaleEntity(
                id = null,
                serverId = null,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                paymentStatus = "COMPLETED",
                syncStatus = "PENDING",
                createdAt = LocalDateTime.now(),
                syncedAt = null
            )

            val saleId = saleDao.insertSale(sale)

            // Save sale items
            items.forEach { item ->
                val saleItem = SaleItemEntity(
                    id = null,
                    saleId = saleId,
                    productId = item.productId,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    subtotal = item.subtotal
                )
                saleDao.insertSaleItem(saleItem)
            }

            Log.i(TAG, "Sale saved to local with ID: $saleId")
            return saleId
        } catch (e: Exception) {
            Log.e(TAG, "Error saving sale to local: ${e.message}", e)
            throw ApiException(0, "Failed to save sale locally", e)
        }
    }

    /**
     * Mark sale as synced
     */
    suspend fun markSaleAsSynced(localSaleId: Long, serverId: Long) {
        try {
            Log.d(TAG, "Marking sale $localSaleId as synced with server ID: $serverId")
            saleDao.updateSaleSyncStatus(localSaleId, "SYNCED", serverId)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking sale as synced: ${e.message}", e)
        }
    }

    /**
     * Save today stats to local cache
     */
    suspend fun saveTodayStatsLocal(stats: TodayStatsData) {
        try {
            Log.d(TAG, "Saving today stats to local cache")
            // Stats are derived from sales, so no direct save needed
        } catch (e: Exception) {
            Log.e(TAG, "Error saving today stats: ${e.message}", e)
        }
    }

    /**
     * Get network overview from local cache
     */
    suspend fun getNetworkOverviewLocal(): NetworkOverviewData? {
        return try {
            Log.d(TAG, "Getting network overview from local cache")

            val sales = saleDao.getAllSales()

            if (sales.isEmpty()) {
                return null
            }

            val totalRevenue = sales.sumOf { it.totalAmount.toInt() }
            val totalProfit = (totalRevenue * 0.18).toInt()
            val expenses = (totalRevenue * 0.82).toInt()

            return NetworkOverviewData(
                totalRevenue = totalRevenue,
                totalProfit = totalProfit,
                growthPercent = 15,
                expenses = expenses,
                storePerformance = emptyList(),
                topEmployees = emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network overview from local: ${e.message}", e)
            null
        }
    }

    /**
     * Save network overview to local cache
     */
    suspend fun saveNetworkOverviewLocal(data: NetworkOverviewData) {
        try {
            Log.d(TAG, "Saving network overview to local cache")
            // Data is derived from sales, so no direct save needed
        } catch (e: Exception) {
            Log.e(TAG, "Error saving network overview: ${e.message}", e)
        }
    }

    /**
     * Get manager dashboard from local cache
     */
    suspend fun getManagerDashboardLocal(): ManagerDashboardData? {
        return try {
            Log.d(TAG, "Getting manager dashboard from local cache")

            val sales = saleDao.getTodaysSales()
            val products = saleDao.getProductStats()

            val totalRevenue = sales.sumOf { it.totalAmount.toInt() }

            return ManagerDashboardData(
                storeName = "Magazin Centru",  // TODO: Get from config
                todayRevenue = totalRevenue,
                totalTransactions = sales.size,
                totalProducts = products.size,
                lowStockCount = products.count { it.quantityCurrent < it.quantityThreshold },
                outOfStockCount = products.count { it.quantityCurrent == 0 },
                lowStockAlerts = emptyList(),
                teamPerformance = emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting manager dashboard from local: ${e.message}", e)
            null
        }
    }

    /**
     * Save manager dashboard to local cache
     */
    suspend fun saveManagerDashboardLocal(data: ManagerDashboardData) {
        try {
            Log.d(TAG, "Saving manager dashboard to local cache")
            // Data is derived from other tables, so no direct save needed
        } catch (e: Exception) {
            Log.e(TAG, "Error saving manager dashboard: ${e.message}", e)
        }
    }

    /**
     * Get report from local cache
     */
    suspend fun getReportLocal(period: String): ReportData? {
        return try {
            val sales = saleDao.getTodaysSales()
            val totalRevenue = sales.sumOf { it.totalAmount }
            val totalTransactions = sales.size

            return ReportData(
                totalRevenue = totalRevenue, // SCHIMBAT: Double
                revenueGrowth = 12,           // ADĂUGAT: câmp necesar pentru UI
                totalTransactions = totalTransactions,
                transactionGrowth = 5,        // ADĂUGAT: câmp necesar pentru UI
                profitMargin = 18,
                topProducts = emptyList(),
                cashPayments = totalRevenue * 0.6, // SCHIMBAT: Double
                cardPayments = totalRevenue * 0.35,
                checkPayments = totalRevenue * 0.05
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Save report to local cache
     */
    suspend fun saveReportLocal(data: ReportData, period: String) {
        try {
            Log.d(TAG, "Saving report to local cache for period: $period")
            // Report is derived from sales, so no direct save needed
        } catch (e: Exception) {
            Log.e(TAG, "Error saving report: ${e.message}", e)
        }
    }

    /**
     * Get team performance from local cache
     */
    suspend fun getTeamPerformanceLocal(): List<TeamMemberUiModel> {
        return try {
            Log.d(TAG, "Getting team performance from local cache")

            // TODO: Implement when user data is available
            return emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting team performance from local: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get store financial data from local cache
     */
    suspend fun getStoreFinancialLocal(storeId: Long): FinancialEntity? {
        return try {
            Log.d(TAG, "Getting store financial from local cache for store: $storeId")

            val sales = saleDao.getAllSales()
            val totalRevenue = sales.sumOf { it.totalAmount.toInt() }
            val cogs = (totalRevenue * 0.5).toInt()
            val expenses = (totalRevenue * 0.4).toInt()
            val netProfit = totalRevenue - cogs - expenses

            return FinancialEntity(
                totalRevenue = totalRevenue,
                cogs = cogs,
                expenses = expenses,
                netProfit = netProfit
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting store financial from local: ${e.message}", e)
            null
        }
    }

    /**
     * Get network financial data from local cache
     */
    suspend fun getNetworkFinancialLocal(): FinancialEntity? {
        return try {
            Log.d(TAG, "Getting network financial from local cache")
            return getStoreFinancialLocal(0)  // 0 = network total
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network financial from local: ${e.message}", e)
            null
        }
    }

    /**
     * Get store financial as Flow (real-time updates)
     */
    fun getStoreFinancialFlow(storeId: Long): Flow<FinancialEntity> {
        Log.d(TAG, "Getting store financial as Flow for store: $storeId")
        return saleDao.getStoreFinancialFlow()
    }

    /**
     * Get network financial as Flow (real-time updates)
     */
    fun getNetworkFinancialFlow(): Flow<FinancialEntity> {
        Log.d(TAG, "Getting network financial as Flow")
        return saleDao.getNetworkFinancialFlow()
    }

    // ========== REMOTE OPERATIONS ==========

    /**
     * Fetch today stats from server
     */
    suspend fun fetchTodayStatsFromServer(): TodayStatsData {
        return try {
            Log.d(TAG, "Fetching today stats from server")

            val response = apiService.getTodayStats()

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                Log.i(TAG, "Fetched today stats from server")
                return data
            } else {
                throw ApiException(response.code(), "Failed to fetch today stats")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching today stats from server: ${e.message}", e)
            throw if (e is ApiException) e else ApiException(0, e.message, e)
        }
    }

    /**
     * Create sale on server
     */
    suspend fun createSaleOnServer(
        items: List<CartItem>,
        totalAmount: Double,
        paymentMethod: String
    ): Long {
        return try {
            Log.d(TAG, "Creating sale on server: $totalAmount RON")

            val request = SaleRequest(
                items = items.map { item ->
                    SaleItemRequest(
                        productId = item.productId,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        subtotal = item.subtotal
                    )
                },
                totalAmount = totalAmount,
                paymentMethod = paymentMethod
            )

            val response = apiService.createSale(request)

            if (response.isSuccessful && response.body() != null) {
                val saleId = response.body()!!.data.id
                Log.i(TAG, "Sale created on server with ID: $saleId")
                return saleId
            } else {
                throw ApiException(response.code(), "Failed to create sale on server")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating sale on server: ${e.message}", e)
            throw if (e is ApiException) e else ApiException(0, e.message, e)
        }
    }

    /**
     * Refund sale on server
     */
    suspend fun refundSale(saleId: Long, reason: String) {
        try {
            Log.d(TAG, "Refunding sale $saleId: $reason")

            val request = RefundRequest(saleId = saleId, reason = reason)
            val response = apiService.refundSale(saleId, request)

            if (response.isSuccessful) {
                Log.i(TAG, "Sale refunded successfully: $saleId")
            } else {
                throw ApiException(response.code(), "Failed to refund sale")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refunding sale: ${e.message}", e)
            throw if (e is ApiException) e else ApiException(0, e.message, e)
        }
    }

    // ========== MORE REMOTE OPERATIONS ==========

    suspend fun fetchNetworkOverviewFromServer(): NetworkOverviewData {
        Log.d(TAG, "Fetching network overview from server")
        // Implement based on your API
        return NetworkOverviewData(0, 0, 0, 0, emptyList(), emptyList())
    }

    suspend fun fetchStoresPerformanceFromServer(): List<StorePerformanceUiModel> {
        Log.d(TAG, "Fetching stores performance from server")
        return emptyList()
    }

    suspend fun fetchTopEmployeesFromServer(): List<EmployeeUiModel> {
        Log.d(TAG, "Fetching top employees from server")
        return emptyList()
    }

    suspend fun saveTopEmployeesLocal(employees: List<EmployeeUiModel>) {
        Log.d(TAG, "Saving top employees to local cache")
    }

    suspend fun fetchManagerDashboardFromServer(): ManagerDashboardData {
        Log.d(TAG, "Fetching manager dashboard from server")
        return ManagerDashboardData("Store", 0, 0, 0, 0, 0, emptyList(), emptyList())
    }

    suspend fun fetchTeamPerformanceFromServer(): List<TeamMemberUiModel> {
        Log.d(TAG, "Fetching team performance from server")
        return emptyList()
    }

    suspend fun saveTeamPerformanceLocal(team: List<TeamMemberUiModel>) {
        Log.d(TAG, "Saving team performance to local cache")
    }

    suspend fun fetchReportsFromServer(period: String): ReportData {
        Log.d(TAG, "Fetching report from server for period: $period")

        // Placeholder return cu toate cele 9 câmpuri necesare
        return ReportData(
            totalRevenue = 0.0,
            revenueGrowth = 0,        // ✅ Adăugat
            totalTransactions = 0,
            transactionGrowth = 0,    // ✅ Adăugat
            profitMargin = 18,
            topProducts = emptyList(),
            cashPayments = 0.0,
            cardPayments = 0.0,
            checkPayments = 0.0
        )
    }

    suspend fun fetchReportFromServer(period: String): ReportData {
        Log.d(TAG, "Fetching report from server for period: $period")
        return fetchReportsFromServer(period)
    }

    suspend fun exportReportToPdf(period: String): String {
        Log.d(TAG, "Exporting report to PDF for period: $period")
        return "http://example.com/report.pdf"
    }

    suspend fun exportReportToCsv(period: String): String {
        Log.d(TAG, "Exporting report to CSV for period: $period")
        return "http://example.com/report.csv"
    }

    suspend fun fetchStoreFinancialFromServer(storeId: Long) {
        Log.d(TAG, "Fetching store financial from server for store: $storeId")
    }

    suspend fun fetchNetworkFinancialFromServer() {
        Log.d(TAG, "Fetching network financial from server")
    }

    suspend fun getReportFromServer(period: String): ReportData {
        Log.d(TAG, "Getting report from server for period: $period")
        return fetchReportFromServer(period)
    }
}

// Data models
data class TodayStatsData(
    val userName: String,
    val totalTransactions: Int,
    val totalRevenue: Double,
    val recentSales: List<RecentSaleData> = emptyList()
)

data class RecentSaleData(
    val id: Long,
    val amount: Double,
    val time: String,
    val paymentMethod: String
)

data class NetworkOverviewData(
    val totalRevenue: Int,
    val totalProfit: Int,
    val growthPercent: Int,
    val expenses: Int,
    val storePerformance: List<StorePerformanceUiModel> = emptyList(),
    val topEmployees: List<EmployeeUiModel> = emptyList()
)

data class ManagerDashboardData(
    val storeName: String,
    val todayRevenue: Int,
    val totalTransactions: Int,
    val totalProducts: Int,
    val lowStockCount: Int,
    val outOfStockCount: Int,
    val lowStockAlerts: List<StockAlertUiModel> = emptyList(),
    val teamPerformance: List<TeamMemberUiModel> = emptyList()
)

data class ReportData(
    val totalRevenue: Double,
    val revenueGrowth: Int,
    val totalTransactions: Int,
    val transactionGrowth: Int,
    val profitMargin: Int,
    val topProducts: List<TopProductData>,
    val cashPayments: Double,
    val cardPayments: Double,
    val checkPayments: Double
)

data class TopProductData(
    val name: String,
    val quantity: Int,
    val revenue: Int
)

data class FinancialEntity(
    val totalRevenue: Int,
    val cogs: Int,
    val expenses: Int,
    val netProfit: Int
)

data class SaleRequest(
    val items: List<SaleItemRequest>,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String = "COMPLETED"
)

data class SaleItemRequest(
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)

data class RefundRequest(
    val saleId: Long,
    val reason: String
)

data class StorePerformanceUiModel(
    val storeId: Long,
    val storeName: String,
    val revenue: Int,
    val growth: Int,
    val profit: Int = 0,
    val rating: Double = 4.5
)

data class EmployeeUiModel(
    val id: Long,
    val name: String,
    val store: String,
    val sales: Int,
    val rating: Double
)

data class TeamMemberUiModel(
    val id: Long,
    val name: String,
    val sales: Int,
    val rating: Double,
    val transactions: Int = 0
)