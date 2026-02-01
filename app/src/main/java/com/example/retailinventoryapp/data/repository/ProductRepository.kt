package com.example.retailinventoryapp.data.repository


import com.example.retailinventoryapp.data.entities.ProductEntity
import com.example.retailinventoryapp.data.network.RetailApiService
import kotlinx.coroutines.flow.Flow
import android.util.Log
import com.example.retailinventoryapp.data.daos.ProductDao
import com.example.retailinventoryapp.data.exception.ApiException
import javax.inject.Inject

private const val TAG = "ProductRepository"


class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val apiService: RetailApiService
) {

    // ========== LOCAL OPERATIONS ==========

    /**
     * Get all products from local cache
     */
    suspend fun getAllProductsLocal(): List<ProductEntity> {
        return try {
            Log.d(TAG, "Getting all products from local cache")
            productDao.getAllProducts()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting products from local: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get product by barcode from local cache
     */
    suspend fun getProductByBarcodeLocal(barcode: String): ProductEntity? {
        return try {
            Log.d(TAG, "Getting product by barcode from local: $barcode")
            productDao.getProductByBarcode(barcode)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product by barcode from local: ${e.message}", e)
            null
        }
    }

    /**
     * Search products in local cache
     */
    suspend fun searchProductsLocal(query: String): List<ProductEntity> {
        return try {
            Log.d(TAG, "Searching products in local cache: $query")
            productDao.searchProducts("%$query%")
        } catch (e: Exception) {
            Log.e(TAG, "Error searching products in local: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get all products as Flow (for real-time updates)
     */
    fun getAllProductsFlow(): Flow<List<ProductEntity>> {
        Log.d(TAG, "Getting all products as Flow")
        return productDao.getAllProductsFlow()
    }

    /**
     * Save product to local cache
     */
    suspend fun saveProductLocal(product: ProductEntity) {
        try {
            Log.d(TAG, "Saving product to local cache: ${product.name}")
            productDao.upsertProduct(product)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving product to local: ${e.message}", e)
        }
    }

    /**
     * Save multiple products to local cache
     */
    suspend fun saveProductsLocal(products: List<ProductEntity>) {
        try {
            Log.d(TAG, "Saving ${products.size} products to local cache")
            products.forEach { productDao.upsertProduct(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving products to local: ${e.message}", e)
        }
    }

    /**
     * Update product in local cache
     */
    suspend fun updateProductLocal(product: ProductEntity) {
        try {
            Log.d(TAG, "Updating product in local cache: ${product.name}")
            productDao.updateProduct(product)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product in local: ${e.message}", e)
        }
    }

    /**
     * Update stock for product in local cache
     */
    suspend fun updateStockLocal(productId: Long, newStock: Int) {
        try {
            Log.d(TAG, "Updating stock for product $productId to $newStock")
            productDao.updateStockLevel(productId, newStock)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stock in local: ${e.message}", e)
        }
    }

    /**
     * Decrease stock in local cache
     */
    suspend fun decreaseStockLocal(productId: Long, quantity: Int) {
        try {
            Log.d(TAG, "Decreasing stock for product $productId by $quantity")
            val product = productDao.getProductById(productId)
            if (product != null) {
                val newStock = maxOf(0, product.quantityCurrent - quantity)
                productDao.updateStockLevel(productId, newStock)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decreasing stock in local: ${e.message}", e)
        }
    }

    /**
     * Get low stock products from local cache
     */
    suspend fun getLowStockProductsLocal(): List<ProductEntity> {
        return try {
            Log.d(TAG, "Getting low stock products from local cache")
            productDao.getLowStockProducts()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting low stock products: ${e.message}", e)
            emptyList()
        }
    }

    // ========== REMOTE OPERATIONS ==========

    /**
     * Sync all products from server
     */
    suspend fun syncAllProductsFromServer() {
        try {
            Log.d(TAG, "Starting sync all products from server")

            val response = apiService.getAllProducts()

            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.data

                Log.i(TAG, "Synced ${products.size} products from server")

                // Convert API response to local entities and save
                val entities = products.map { product ->
                    ProductEntity(
                        id = product.id,
                        name = product.name,
                        barcode = product.barcode,
                        category = product.category,
                        sellPrice = product.price.toDouble(),
                        quantityCurrent = product.currentStock,
                        quantityThreshold = product.threshold,
                        internalCode = product.barcode,
                        syncStatus = "SYNCED",
                        lastSyncedAt = System.currentTimeMillis()
                    )
                }

                saveProductsLocal(entities)
            } else {
                throw ApiException(
                    response.code(),
                    "Server returned ${response.code()}: ${response.message()}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing products from server: ${e.message}", e)
            if (e !is ApiException) {
                throw ApiException(0, e.message, e)
            } else {
                throw e
            }
        }
    }

    /**
     * Get product by barcode from server
     */
    suspend fun getProductByBarcodeFromServer(barcode: String): ProductEntity? {
        return try {
            Log.d(TAG, "Getting product by barcode from server: $barcode")

            val response = apiService.getProductByBarcode(barcode)

            if (response.isSuccessful && response.body() != null) {
                val product = response.body()!!.data

                Log.i(TAG, "Got product from server: ${product.name}")

                return ProductEntity(
                    id = product.id,
                    name = product.name,
                    barcode = product.barcode,
                    category = product.category,
                    sellPrice = product.price.toDouble(),
                    quantityCurrent = product.currentStock,
                    quantityThreshold = product.threshold,
                    internalCode = product.barcode,
                    syncStatus = "SYNCED",
                    lastSyncedAt = System.currentTimeMillis()
                )
            } else {
                Log.w(TAG, "Product not found on server: $barcode")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product from server: ${e.message}", e)
            if (e !is ApiException) {
                throw ApiException(0, e.message, e)
            } else {
                throw e
            }
        }
    }

    /**
     * Search products on server
     */
    suspend fun searchProductsServer(query: String): List<ProductEntity> {
        return try {
            Log.d(TAG, "Searching products on server: $query")

            val response = apiService.searchProducts(query)

            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.data

                Log.i(TAG, "Found ${products.size} products on server")

                return products.map { product ->
                    ProductEntity(
                        id = product.id,
                        name = product.name,
                        barcode = product.barcode,
                        category = product.category,
                        sellPrice = product.price.toDouble(),
                        quantityCurrent = product.currentStock,
                        quantityThreshold = product.threshold,
                        internalCode = product.barcode,
                        syncStatus = "SYNCED",
                        lastSyncedAt = System.currentTimeMillis()
                    )
                }
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching products on server: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * Fetch all products from server
     */
    suspend fun fetchAllProductsFromServer(): List<ProductEntity> {
        return try {
            Log.d(TAG, "Fetching all products from server")

            val response = apiService.getAllProducts()

            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.data

                Log.i(TAG, "Fetched ${products.size} products from server")

                return products.map { product ->
                    ProductEntity(
                        id = product.id,
                        name = product.name,
                        barcode = product.barcode,
                        category = product.category,
                        sellPrice = product.price.toDouble(),
                        quantityCurrent = product.currentStock,
                        quantityThreshold = product.threshold,
                        internalCode = product.barcode,
                        syncStatus = "SYNCED",
                        lastSyncedAt = System.currentTimeMillis()
                    )
                }
            } else {
                throw ApiException(response.code(), "Failed to fetch products")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching products from server: ${e.message}", e)
            throw if (e is ApiException) e else ApiException(0, e.message, e)
        }
    }

    /**
     * Update stock on server
     */
    suspend fun updateStockRemote(productId: Long, quantity: Int) {
        try {
            Log.d(TAG, "Updating stock on server for product $productId: +$quantity")

            val request = StockUpdateRequest(
                productId = productId,
                quantity = quantity,
                reason = "Restock"
            )

            val response = apiService.updateStock(productId, request)

            if (response.isSuccessful) {
                Log.i(TAG, "Stock updated on server for product $productId")
            } else {
                throw ApiException(response.code(), "Failed to update stock on server")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stock on server: ${e.message}", e)
            throw if (e is ApiException) e else ApiException(0, e.message, e)
        }
    }

    /**
     * Fetch low stock alerts from server
     */
    suspend fun fetchLowStockAlertsFromServer(): List<StockAlertUiModel> {
        return try {
            Log.d(TAG, "Fetching low stock alerts from server")

            // This would typically be a specific endpoint
            val allProducts = fetchAllProductsFromServer()

            val alerts = allProducts
                .filter { it.quantityCurrent < it.quantityThreshold }
                .map { product ->
                    StockAlertUiModel(
                        id = product.id,
                        productId = product.id,
                        productName = product.name,
                        currentStock = product.quantityCurrent,
                        threshold = product.quantityThreshold,
                        sku = product.internalCode ?: ""
                    )
                }

            Log.i(TAG, "Fetched ${alerts.size} low stock alerts")
            return alerts
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching low stock alerts: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * Save low stock alerts to local cache
     */
    suspend fun saveLowStockAlertsLocal(alerts: List<StockAlertUiModel>) {
        try {
            Log.d(TAG, "Saving ${alerts.size} low stock alerts to local cache")
            // In a real app, you might have a separate table for this
            // For now, the data is derived from products
        } catch (e: Exception) {
            Log.e(TAG, "Error saving low stock alerts: ${e.message}", e)
        }
    }
}

// Data models for API responses
data class ProductResponse(
    val id: Long,
    val barcode: String,
    val name: String,
    val category: String,
    val currentStock: Int,
    val threshold: Int,
    val price: Int,
    val isLowStock: Boolean
)

data class StockUpdateRequest(
    val productId: Long,
    val quantity: Int,
    val reason: String
)

data class StockAlertUiModel(
    val id: Long,
    val productId: Long,
    val productName: String,
    val currentStock: Int,
    val threshold: Int,
    val sku: String = ""
)