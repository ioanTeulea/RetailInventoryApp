package com.example.retailinventoryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailinventoryapp.data.repository.SaleRepository
import com.example.retailinventoryapp.data.repository.ProductRepository
import com.example.retailinventoryapp.data.exception.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import android.util.Log

private const val TAG = "CasierCheckoutVM"

// ========== STATE ==========

data class CasierCheckoutUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val selectedPaymentMethod: String = "CASH",
    val isProcessing: Boolean = false,
    val paymentSuccess: Boolean = false,
    val receiptNumber: String? = null,
    val error: String? = null,
    val message: String? = null
)

// ========== VIEWMODEL ==========

@HiltViewModel
class CasierCheckoutViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CasierCheckoutUiState())
    val uiState: StateFlow<CasierCheckoutUiState> = _uiState.asStateFlow()

    private companion object {
        const val MAX_RETRIES = 3
        const val INITIAL_RETRY_DELAY_MS = 100L
    }

    /**
     * Initialize checkout with cart items
     */
    fun initializeCheckout(cartItems: List<CartItem>) {
        val totalAmount = cartItems.sumOf { it.subtotal }

        Log.i(TAG, "Initializing checkout with ${cartItems.size} items, total: $totalAmount")

        _uiState.value = CasierCheckoutUiState(
            cartItems = cartItems,
            totalAmount = totalAmount,
            selectedPaymentMethod = "CASH"
        )
    }

    /**
     * Select payment method
     */
    fun selectPaymentMethod(method: String) {
        Log.d(TAG, "Payment method selected: $method")
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    /**
     * Process sale with retry logic
     *
     * Strategy:
     * 1. Save to Room locally first (OFFLINE support)
     * 2. Try to sync to server (with retry)
     * 3. Update stock locally
     * 4. Success!
     */
    fun processSale() {
        if (_uiState.value.cartItems.isEmpty()) {
            Log.w(TAG, "Attempted to process empty cart")
            _uiState.update { it.copy(error = "❌ Coșul este gol") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }

            Log.i(TAG, "Starting sale processing: ${_uiState.value.cartItems.size} items")

            try {
                // ✅ STEP 1: Save to Room locally (OFFLINE support)
                val localSaleId = saleRepository.saveSaleLocally(
                    items = _uiState.value.cartItems,
                    totalAmount = _uiState.value.totalAmount,
                    paymentMethod = _uiState.value.selectedPaymentMethod
                )

                Log.d(TAG, "Sale saved locally with ID: $localSaleId")

                // ✅ STEP 2: Try to sync to server (with retry)
                val saleId = fetchWithRetry {
                    saleRepository.createSaleOnServer(
                        items = _uiState.value.cartItems,
                        totalAmount = _uiState.value.totalAmount,
                        paymentMethod = _uiState.value.selectedPaymentMethod
                    )
                }

                Log.i(TAG, "Sale synced to server with ID: $saleId")

                // ✅ STEP 3: Update local inventory
                updateLocalInventory(_uiState.value.cartItems)

                Log.d(TAG, "Local inventory updated")

                // ✅ STEP 4: Mark as synced in Room
                saleRepository.markSaleAsSynced(localSaleId, saleId)

                // Success!
                _uiState.update { state ->
                    state.copy(
                        isProcessing = false,
                        paymentSuccess = true,
                        receiptNumber = "RCP-$saleId",
                        message = "✅ Vânzare procesată cu succes"
                    )
                }

                Log.i(TAG, "Sale processing completed successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Sale processing failed: ${e.message}", e)
                handleError(e, "Eroare procesare")
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    /**
     * Fetch with retry logic
     */
    private suspend inline fun <T> fetchWithRetry(
        maxRetries: Int = MAX_RETRIES,
        initialDelayMs: Long = INITIAL_RETRY_DELAY_MS,
        crossinline operation: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                Log.d(TAG, "Fetch attempt ${attempt + 1}/$maxRetries")
                return operation()
            } catch (e: Exception) {
                lastException = e

                if (attempt < maxRetries - 1) {
                    Log.w(TAG, "Attempt ${attempt + 1} failed: ${e.message}, retrying...")
                    delay(currentDelay)
                    currentDelay = minOf(currentDelay * 2, 2000L)
                } else {
                    Log.e(TAG, "All $maxRetries attempts failed")
                }
            }
        }

        throw lastException ?: Exception("All retries failed")
    }

    /**
     * Update local inventory after successful sale
     */
    private suspend fun updateLocalInventory(items: List<CartItem>) {
        items.forEach { item ->
            try {
                productRepository.decreaseStockLocal(item.productId, item.quantity)
                Log.d(TAG, "Decreased stock for product ${item.productId} by ${item.quantity}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update inventory for ${item.productId}: ${e.message}")
            }
        }
    }

    /**
     * Handle errors
     */
    private fun handleError(exception: Exception, context: String) {
        val errorMessage = when (exception) {
            is ApiException -> {
                when (exception.code) {
                    400 -> "❌ Date invalide"
                    401 -> "❌ Neautorizat - Login din nou"
                    403 -> "❌ Acces interzis"
                    404 -> "❌ Produs nu mai disponibil"
                    500 -> "❌ Eroare server"
                    0 -> "❌ Eroare retea"
                    else -> "$context: Eroare ${exception.code}"
                }
            }
            is java.net.SocketTimeoutException -> "❌ Timeout - Server nu raspunde"
            is java.net.UnknownHostException -> "❌ Fara conexiune internet"
            else -> "❌ ${exception.message ?: "Eroare necunoscuta"}"
        }

        Log.e(TAG, "Error: $errorMessage", exception)
        _uiState.update { it.copy(error = errorMessage) }
    }

    /**
     * Reset for new sale
     */
    fun resetForNewSale() {
        Log.d(TAG, "Resetting for new sale")
        _uiState.value = CasierCheckoutUiState()
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel cleared")
        super.onCleared()
    }
}