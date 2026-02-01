package com.example.retailinventoryapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailinventoryapp.data.entities.ProductEntity
import com.example.retailinventoryapp.data.exception.ApiException
import com.example.retailinventoryapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.math.min
import com.example.retailinventoryapp.ui.screens.manager.FilterType

private const val TAG = "ManagerInventoryVM"

// ========== UI MODELS & STATE ==========

data class ProductInventoryUiModel(
    val id: Long,
    val name: String,
    val sku: String,
    val currentStock: Int,
    val threshold: Int,
    val price: Double,
    val isLowStock: Boolean = currentStock < threshold
)

data class ManagerInventoryUiState(
    val products: List<ProductInventoryUiModel> = emptyList(),
    val filteredProducts: List<ProductInventoryUiModel> = emptyList(),
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val showRestockDialog: Boolean = false,
    val selectedProduct: ProductInventoryUiModel? = null,
    val error: String? = null,
    val message: String? = null,
    val lastSyncTime: Long = 0L
)

// ========== VIEWMODEL ==========

@HiltViewModel
class ManagerInventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerInventoryUiState())
    val uiState: StateFlow<ManagerInventoryUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null
    private var currentFilter = FilterType.ALL

    init {
        Log.d(TAG, "Inventory ViewModel initialized")
        loadInventory()
    }

    /**
     * Main data entry point: Starts Room observation and background network sync.
     */
    fun loadInventory() {
        observationJob?.cancel()

        _uiState.update { it.copy(isLoading = true, error = null) }

        observationJob = viewModelScope.launch {
            // 1. REACTIVE LAYER: Observe Flow from Room database
            // The UI will auto-update whenever the local DB changes.
            launch {
                productRepository.getAllProductsFlow().collect { entities ->
                    mapEntitiesToUiState(entities)
                }
            }

            // 2. NETWORK LAYER: Background sync with server
            launch {
                performBackgroundSync()
            }
        }
    }

    /**
     * Background synchronization with Exponential Backoff retry logic.
     */
    private suspend fun performBackgroundSync() {
        var currentDelay = 100L
        val maxRetries = 3

        repeat(maxRetries) { attempt ->
            try {
                _uiState.update { it.copy(isSyncing = true) }

                Log.d(TAG, "Sync attempt ${attempt + 1}")

                // Apelăm repository-ul
                productRepository.syncAllProductsFromServer()

                // Dacă am ajuns aici, înseamnă că a reușit!
                _uiState.update { state ->
                    state.copy(
                        isSyncing = false,
                        lastSyncTime = System.currentTimeMillis(),
                        message = "✅ Catalog sincronizat"
                    )
                }

                return // <--- AICI ESTE MODIFICAREA. Ieșim din funcție direct.

            } catch (e: Exception) {
                if (attempt == maxRetries - 1) {
                    // Dacă este ultima încercare, raportăm eroarea
                    handleError(e, "Sync Inventory")
                } else {
                    Log.w(TAG, "Sync failed, retrying in ${currentDelay}ms...")
                    delay(currentDelay)
                    currentDelay = min(currentDelay * 2, 2000L)
                }
            }
        }
        // Ne asigurăm că starea de syncing este falsă dacă toate încercările eșuează
        _uiState.update { it.copy(isSyncing = false) }
    }

    /**
     * Maps database entities to UI-friendly models and calculates stats.
     */
    private fun mapEntitiesToUiState(entities: List<ProductEntity>) {
        val mapped = entities.map { entity ->
            ProductInventoryUiModel(
                id = entity.id ?: 0L,
                name = entity.name,
                sku = entity.internalCode ?: "N/A",
                currentStock = entity.quantityCurrent,
                threshold = entity.quantityThreshold,
                price = entity.sellPrice,
                isLowStock = entity.quantityCurrent < entity.quantityThreshold
            )
        }

        _uiState.update { state ->
            state.copy(
                products = mapped,
                filteredProducts = applyFilter(mapped, currentFilter),
                totalProducts = mapped.size,
                lowStockCount = mapped.count { it.isLowStock && it.currentStock > 0 },
                outOfStockCount = mapped.count { it.currentStock == 0 },
                isLoading = false
            )
        }
    }

    fun openRestockDialog(productId: Long) {
        val product = _uiState.value.products.find { it.id == productId }
        _uiState.update { it.copy(showRestockDialog = true, selectedProduct = product) }
    }

    fun closeRestockDialog() {
        _uiState.update { it.copy(showRestockDialog = false, selectedProduct = null) }
    }

    fun filter(type: FilterType) {
        currentFilter = type
        _uiState.update { it.copy(
            filteredProducts = applyFilter(it.products, type)
        )}
    }

    private fun applyFilter(list: List<ProductInventoryUiModel>, type: FilterType): List<ProductInventoryUiModel> {
        return when (type) {
            FilterType.ALL -> list
            FilterType.LOW_STOCK -> list.filter { it.isLowStock && it.currentStock > 0 }
            FilterType.OUT_OF_STOCK -> list.filter { it.currentStock == 0 }
            FilterType.HIGH_STOCK -> list.filter { !it.isLowStock }
        }
    }

    /**
     * Updates product stock with Local-First strategy.
     */
    fun updateStock(productId: Long, quantity: Int) {
        viewModelScope.launch {
            try {
                // Step 1: Update Room immediately (UI reflects this via Flow)
                productRepository.updateStockLocal(productId, quantity)

                // Step 2: Push update to server
                productRepository.updateStockRemote(productId, quantity)

                _uiState.update { it.copy(message = "✅ Stoc actualizat cu succes") }
            } catch (e: Exception) {
                handleError(e, "Update Stoc")
            }
        }
    }

    /**
     * Searches products in local cache and triggers a server search refresh.
     */
    fun searchProducts(query: String) {
        if (query.isBlank()) {
            loadInventory()
            return
        }

        viewModelScope.launch {
            try {
                // Search in local DB
                val results = productRepository.searchProductsLocal(query)
                mapEntitiesToUiState(results)

                // Background: Refresh specific query from server
                launch {
                    try {
                        val serverResults = productRepository.searchProductsServer(query)
                        serverResults.forEach { productRepository.saveProductLocal(it) }
                    } catch (e: Exception) {
                        Log.w(TAG, "Background search sync failed")
                    }
                }
            } catch (e: Exception) {
                handleError(e, "Căutare")
            }
        }
    }

    private fun handleError(e: Exception, context: String) {
        val msg = when (e) {
            is ApiException -> "❌ Eroare Server ($context): ${e.code}"
            is SocketTimeoutException -> "❌ Timeout: Serverul nu răspunde"
            is UnknownHostException -> "❌ Fără internet"
            else -> "❌ $context: ${e.message ?: "Eroare necunoscută"}"
        }
        Log.e(TAG, "Error handled: $msg", e)
        _uiState.update { it.copy(error = msg, isLoading = false, isSyncing = false) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    override fun onCleared() {
        observationJob?.cancel()
        super.onCleared()
    }
}