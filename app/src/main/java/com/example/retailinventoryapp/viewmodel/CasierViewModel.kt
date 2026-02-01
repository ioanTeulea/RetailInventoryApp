package com.example.retailinventoryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailinventoryapp.data.repository.SaleRepository
import com.example.retailinventoryapp.data.exception.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.example.retailinventoryapp.data.repository.TodayStatsData

private const val TAG = "CasierVM"

// ========== STATE CLASSES ==========

data class SaleUiModel(
    val id: Long,
    val amount: Double,
    val time: String,
    val paymentMethod: String = "CASH"
)

data class CasierUiState(
    val userName: String = "",
    val todayTransactions: Int = 0,
    val todayRevenue: Double = 0.0,
    val avgTransaction: Number = 0,
    val recentSales: List<SaleUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val lastSyncTime: Long = 0L
)

// ========== VIEWMODEL ==========

@HiltViewModel
class CasierViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CasierUiState())
    val uiState: StateFlow<CasierUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        Log.d(TAG, "CasierViewModel initialized")
        loadTodayStats()
    }

    /**
     * Load today's stats - OFFLINE FIRST
     *
     * Strategy:
     * 1. Load from cache (instant)
     * 2. Background: Sync from server
     */
    fun loadTodayStats() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            Log.i(TAG, "Loading today stats")

            try {
                // ⚡ STEP 1: Load from cache
                val cachedStats = saleRepository.getTodayStatsLocal()

                if (cachedStats != null) {
                    Log.d(TAG, "Today stats loaded from cache")
                    updateCasierUI(cachedStats)
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    Log.w(TAG, "No cached today stats found")
                }

                // ✅ STEP 2: Sync from server
                syncTodayStatsFromServer()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load today stats: ${e.message}", e)
                handleError(e)
            }
        }
    }

    /**
     * Background sync from server
     */
    private suspend fun syncTodayStatsFromServer() {
        try {
            _uiState.update { it.copy(isSyncing = true) }

            Log.d(TAG, "Starting today stats sync from server")

            val freshStats = saleRepository.fetchTodayStatsFromServer()

            Log.i(TAG, "Today stats synced from server")

            // Save to cache
            saleRepository.saveTodayStatsLocal(freshStats)

            // Update UI
            updateCasierUI(freshStats)

            _uiState.update { state ->
                state.copy(
                    isSyncing = false,
                    message = "✅ Stats actualizate",
                    lastSyncTime = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Today stats sync failed: ${e.message}")
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    /**
     * Update casier UI
     */
    private fun updateCasierUI(stats: TodayStatsData) {
        val avgTransaction = if (stats.totalTransactions > 0) {
            stats.totalRevenue / stats.totalTransactions
        } else 0

        _uiState.update { state ->
            state.copy(
                userName = stats.userName,
                todayTransactions = stats.totalTransactions,
                todayRevenue = stats.totalRevenue,
                avgTransaction = avgTransaction,
                recentSales = stats.recentSales.map { sale ->
                    SaleUiModel(
                        id = sale.id,
                        amount = sale.amount,
                        time = sale.time,
                        paymentMethod = sale.paymentMethod
                    )
                }
            )
        }

        Log.d(TAG, "Casier UI updated: $stats.totalTransactions transactions, Revenue=${stats.totalRevenue}")
    }

    /**
     * Refresh stats
     */
    fun refreshStats() {
        Log.i(TAG, "Manual refresh initiated")
        loadTodayStats()
    }

    /**
     * Process sale (called from checkout)
     */
    fun processSale() {
        Log.d(TAG, "Sale processed, refreshing stats")
        loadTodayStats()
    }

    /**
     * Handle errors
     */
    private fun handleError(exception: Exception) {
        val errorMessage = when (exception) {
            is ApiException -> "❌ Server error: ${exception.code}"
            is java.net.SocketTimeoutException -> "❌ Timeout"
            is java.net.UnknownHostException -> "❌ Fara internet"
            else -> "❌ ${exception.message ?: "Eroare"}"
        }

        Log.e(TAG, "Error: $errorMessage", exception)
        _uiState.update { it.copy(error = errorMessage, isLoading = false) }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel cleared")
        loadJob?.cancel()
        super.onCleared()
    }
}
