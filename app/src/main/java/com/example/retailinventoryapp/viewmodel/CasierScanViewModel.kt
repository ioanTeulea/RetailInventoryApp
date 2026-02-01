package com.example.retailinventoryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailinventoryapp.data.repository.ProductRepository
import com.example.retailinventoryapp.data.repository.SaleRepository
import com.example.retailinventoryapp.data.exception.ApiException  // ✅ Correct import
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import kotlinx.coroutines.delay
import kotlin.math.min

// ========== CONSTANTS ==========

private const val TAG = "CasierScanVM"
private const val CACHE_MAX_AGE_MS = 5 * 60 * 1000L  // 5 minutes

// ========== STATE CLASSES ==========

data class CartItem(
    val productId: Long,
    val productName: String,
    val barcode: String,
    val unitPrice: Double,
    val quantity: Int = 1,
    val subtotal: Double = unitPrice * quantity,
    val syncStatus: String = "LOCAL"  // LOCAL, PENDING, SYNCED
)

data class CasierScanUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val itemCount: Int = 0,
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val isSyncing: Boolean = false,  // ✅ NEW: Track background sync
    val error: String? = null,
    val syncMessage: String? = null,
    val lastScannedBarcode: String? = null,
    val lastSyncTime: Long = 0L  // ✅ NEW: Track when data was synced
)

// ========== VIEWMODEL ==========

@HiltViewModel
class CasierScanViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CasierScanUiState())
    val uiState: StateFlow<CasierScanUiState> = _uiState.asStateFlow()

    init {
        // Inițializăm catalogul local prin sincronizare în fundal
        syncCatalog()
    }

    /**
     * Sincronizează întreg catalogul de produse (Exponential Backoff included).
     */
    fun syncCatalog() {
        viewModelScope.launch {
            var currentDelay = 100L
            repeat(3) { attempt ->
                try {
                    _uiState.update { it.copy(isSyncing = true, error = null) }
                    productRepository.syncAllProductsFromServer()
                    _uiState.update { it.copy(isSyncing = false, lastSyncTime = System.currentTimeMillis()) }
                    return@launch
                } catch (e: Exception) {
                    if (attempt == 2) handleError(e, "Sync Catalog")
                    delay(currentDelay)
                    currentDelay = min(currentDelay * 2, 2000L)
                }
            }
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    /**
     * Strategie Scanare: Local First -> Server fallback -> Background Refresh
     */
    fun scanProduct(barcode: String) {
        if (barcode.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }

            try {
                // 1. Încercăm Local (Instant)
                var product = productRepository.getProductByBarcodeLocal(barcode)

                if (product != null) {
                    addProductToState(product)
                    // Pornim un sync silențios în fundal pentru a vedea dacă s-a schimbat prețul/stocul
                    launch { refreshProductDataSilently(barcode) }
                } else {
                    // 2. Fallback pe Server dacă nu e în cache
                    product = productRepository.getProductByBarcodeFromServer(barcode)
                    if (product != null) {
                        productRepository.saveProductLocal(product) // Îl punem în cache
                        addProductToState(product)
                    } else {
                        _uiState.update { it.copy(error = "❌ Produsul nu există în sistem") }
                    }
                }
            } catch (e: Exception) {
                handleError(e, "Scanare")
            } finally {
                _uiState.update { it.copy(isScanning = false) }
            }
        }
    }

    private suspend fun refreshProductDataSilently(barcode: String) {
        try {
            val freshProduct = productRepository.getProductByBarcodeFromServer(barcode)
            if (freshProduct != null) {
                productRepository.updateProductLocal(freshProduct)
                // Dacă prețul s-a schimbat pe server, am putea notifica UI-ul aici
            }
        } catch (e: Exception) {
            Log.w(TAG, "Silent refresh failed for $barcode")
        }
    }

    private fun addProductToState(product: com.example.retailinventoryapp.data.entities.ProductEntity) {
        _uiState.update { state ->
            val items = state.cartItems.toMutableList()
            val index = items.indexOfFirst { it.productId == product.id }

            if (index != -1) {
                val existing = items[index]
                items[index] = existing.copy(
                    quantity = existing.quantity + 1,
                    subtotal = (existing.quantity + 1) * existing.unitPrice
                )
            } else {
                items.add(CartItem(
                    productId = product.id,
                    productName = product.name,
                    barcode = product.barcode,
                    unitPrice = product.sellPrice,
                    quantity = 1,
                    subtotal = product.sellPrice
                ))
            }

            // Calculăm totalurile automat la fiecare update
            val total = items.sumOf { it.subtotal }
            val count = items.sumOf { it.quantity }

            state.copy(
                cartItems = items,
                totalAmount = total,
                itemCount = count,
                syncMessage = "Adăugat: ${product.name}"
            )
        }
    }

    fun updateQuantity(productId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(productId)
        } else {
            // Re-utilizăm logica de recalculare
            val updatedItems = _uiState.value.cartItems.map {
                if (it.productId == productId) it.copy(quantity = newQuantity, subtotal = newQuantity * it.unitPrice)
                else it
            }
            updateCartState(updatedItems)
        }
    }

    fun removeItem(productId: Long) {
        val updatedItems = _uiState.value.cartItems.filter { it.productId != productId }
        updateCartState(updatedItems)
    }

    private fun updateCartState(items: List<CartItem>) {
        _uiState.update { it.copy(
            cartItems = items,
            totalAmount = items.sumOf { item -> item.subtotal },
            itemCount = items.sumOf { item -> item.quantity }
        ) }
    }

    private fun handleError(e: Exception, context: String) {
        val msg = when (e) {
            is ApiException -> "Eroare Server ($context): ${e.code}"
            is java.net.UnknownHostException -> "Fără conexiune internet"
            else -> "Eroare: ${e.message}"
        }
        _uiState.update { it.copy(error = msg) }
    }

    fun clearCart() = _uiState.update { CasierScanUiState() }
}