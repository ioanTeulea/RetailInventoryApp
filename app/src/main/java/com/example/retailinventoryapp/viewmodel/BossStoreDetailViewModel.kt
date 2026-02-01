package com.example.retailinventoryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailinventoryapp.data.repository.StoreRepository
import com.example.retailinventoryapp.data.repository.SaleRepository
import com.example.retailinventoryapp.data.exception.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import android.util.Log
import com.example.retailinventoryapp.data.repository.StoreDetailData
import kotlin.math.min

private const val TAG = "BossStoreDetailVM"

// ========== STATE ==========

data class CasierPerformanceUiModel(
    val id: Long,
    val name: String,
    val sales: Int,
    val transactions: Int,
    val rating: Double
)

data class BossStoreDetailUiState(
    val storeId: Long = 0,
    val storeName: String = "",
    val address: String = "",
    val phone: String = "",
    val hours: String = "08:00 - 20:00",
    val monthlyRevenue: Int = 0,
    val monthlyTransactions: Int = 0,
    val revenueGrowth: Int = 0,
    val profitMargin: Int = 18,
    val storeRating: Double = 4.5,
    val managerName: String = "",
    val managerEmail: String = "",
    val managerTenure: String = "",
    val managerRating: Double = 4.5,
    val topCasiers: List<CasierPerformanceUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val lastSyncTime: Long = 0L
)

// ========== VIEWMODEL ==========

@HiltViewModel
class BossStoreDetailViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BossStoreDetailUiState())
    val uiState: StateFlow<BossStoreDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /**
     * Load store detail - OFFLINE FIRST
     *
     * Strategy:
     * 1. Load from cache (instant)
     * 2. Fetch from server (background)
     */
    fun loadStoreDetail(storeId: Long) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            Log.i(TAG, "Loading store detail for storeId: $storeId")

            try {
                // ⚡ STEP 1: Load from cache
                val cachedStore = storeRepository.getStoreDetailLocal(storeId)

                if (cachedStore != null) {
                    Log.d(TAG, "Store loaded from cache")
                    updateStoreDetailUI(cachedStore)
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    Log.w(TAG, "No cached store found")
                }

                // ✅ STEP 2: Fetch from server with retry
                fetchWithRetry {
                    val storeData = storeRepository.fetchStoreDetailFromServer(storeId)

                    // Save to cache
                    storeRepository.saveStoreDetailLocal(storeData)

                    // Update UI
                    updateStoreDetailUI(storeData)

                    Log.i(TAG, "Store detail synced from server")

                    _uiState.update { state ->
                        state.copy(
                            isSyncing = false,
                            lastSyncTime = System.currentTimeMillis()
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load store detail: ${e.message}", e)
                handleError(e)
            }
        }
    }

    /**
     * Update store detail UI
     */
    private fun updateStoreDetailUI(storeData: StoreDetailData) {
        _uiState.update {
            it.copy(
                storeId = storeData.id,
                storeName = storeData.name,
                address = storeData.address,
                phone = storeData.phone ?: "N/A",
                hours = storeData.hours ?: "08:00 - 20:00",
                monthlyRevenue = storeData.monthlyRevenue,
                monthlyTransactions = storeData.monthlyTransactions,
                revenueGrowth = storeData.revenueGrowth,
                profitMargin = storeData.profitMargin,
                storeRating = storeData.rating,
                managerName = storeData.manager.fullName,
                managerEmail = storeData.manager.email,
                managerTenure = "${storeData.manager.yearsOfService} ani",
                managerRating = storeData.manager.rating,
                topCasiers = storeData.topCasiers.map { casier ->
                    CasierPerformanceUiModel(
                        id = casier.id,
                        name = casier.name,
                        sales = casier.monthlySales,
                        transactions = casier.monthlyTransactions,
                        rating = casier.rating
                    )
                }
            )
        }

        Log.d(TAG, "Store detail UI updated: ${storeData.name}")
    }

    /**
     * Fetch with retry logic
     */
    private suspend inline fun fetchWithRetry(
        maxRetries: Int = 3,
        initialDelayMs: Long = 100L,
        crossinline operation: suspend () -> Unit
    ) {
        var currentDelay = initialDelayMs

        repeat(maxRetries) { attempt ->
            try {
                Log.d(TAG, "Fetch attempt ${attempt + 1}/$maxRetries")
                _uiState.update { it.copy(isSyncing = true) }
                operation()
                return
            } catch (e: Exception) {
                if (attempt < maxRetries - 1) {
                    Log.w(TAG, "Attempt ${attempt + 1} failed: ${e.message}")
                    delay(currentDelay)
                    currentDelay = min(currentDelay * 2, 2000L)
                } else {
                    Log.e(TAG, "All $maxRetries attempts failed")
                    throw e
                }
            }
        }
    }

    /**
     * Refresh data
     */
    fun refreshData(storeId: Long) {
        Log.i(TAG, "Manual refresh initiated for store: $storeId")
        loadStoreDetail(storeId)
    }

    /**
     * Handle errors
     */
    private fun handleError(exception: Exception) {
        val errorMessage = when (exception) {
            is ApiException -> {
                when (exception.code) {
                    404 -> "❌ Store not found"
                    401 -> "❌ Neautorizat"
                    500 -> "❌ Eroare server"
                    else -> "❌ Error ${exception.code}"
                }
            }
            is java.net.SocketTimeoutException -> "❌ Timeout"
            is java.net.UnknownHostException -> "❌ Fara internet"
            else -> "❌ ${exception.message ?: "Eroare"}"
        }

        Log.e(TAG, "Error: $errorMessage", exception)
        _uiState.update { it.copy(error = errorMessage, isLoading = false, isSyncing = false) }
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel cleared")
        loadJob?.cancel()
        super.onCleared()
    }
}

