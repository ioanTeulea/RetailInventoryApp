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
import com.example.retailinventoryapp.data.repository.FinancialEntity

private const val TAG = "BossFinancialVM"

// ========== STATE ==========

data class BossFinancialUiState(
    val totalRevenue: Int = 0,
    val cogs: Int = 0,
    val grossProfit: Int = 0,
    val expenses: Int = 0,
    val netProfit: Int = 0,
    val grossMargin: Int = 50,
    val netMargin: Int = 10,
    val operatingMargin: Int = 15,
    val salaries: Int = 0,
    val salariesPercent: Int = 20,
    val rent: Int = 0,
    val rentPercent: Int = 10,
    val utilities: Int = 0,
    val utilitiesPercent: Int = 4,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val lastSyncTime: Long = 0L
)

// ========== VIEWMODEL ==========

@HiltViewModel
class BossFinancialViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BossFinancialUiState())
    val uiState: StateFlow<BossFinancialUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    /**
     * Load store financial - OFFLINE FIRST with SYNC
     *
     * Strategy:
     * 1. Load cached financial data
     * 2. Background: Fetch from server
     * 3. Real-time: Observe Room updates
     */
    fun loadStoreFinancial(storeId: Long) {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            Log.i(TAG, "Loading financial data for store: $storeId")

            try {
                // ⚡ STEP 1: Load from cache
                val cachedFinancial = saleRepository.getStoreFinancialLocal(storeId)

                if (cachedFinancial != null) {
                    Log.d(TAG, "Financial data loaded from cache")
                    updateFinancialUI(cachedFinancial)
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    Log.w(TAG, "No cached financial data found")
                }

                // ✅ STEP 2: Sync from server
                syncFromServer(storeId)

                // ✅ STEP 3: Observe Room for real-time updates
                saleRepository.getStoreFinancialFlow(storeId).collect { data ->
                    updateFinancialUI(data)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load financial: ${e.message}", e)
                handleError(e)
            }
        }
    }

    /**
     * Load network financial - OFFLINE FIRST
     */
    fun loadNetworkFinancial() {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            Log.i(TAG, "Loading network financial data")

            try {
                // ⚡ STEP 1: Load from cache
                val cachedFinancial = saleRepository.getNetworkFinancialLocal()

                if (cachedFinancial != null) {
                    Log.d(TAG, "Network financial loaded from cache")
                    updateFinancialUI(cachedFinancial)
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    Log.w(TAG, "No cached network financial found")
                }

                // ✅ STEP 2: Sync from server
                syncNetworkFromServer()

                // ✅ STEP 3: Observe Room
                saleRepository.getNetworkFinancialFlow().collect { data ->
                    updateFinancialUI(data)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load network financial: ${e.message}", e)
                handleError(e)
            }
        }
    }

    /**
     * Background sync from server
     */
    private suspend fun syncFromServer(storeId: Long) {
        try {
            _uiState.update { it.copy(isSyncing = true) }

            Log.d(TAG, "Syncing financial from server for store: $storeId")

            saleRepository.fetchStoreFinancialFromServer(storeId)

            Log.i(TAG, "Store financial synced from server")

            _uiState.update { state ->
                state.copy(
                    isSyncing = false,
                    lastSyncTime = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Financial sync failed: ${e.message}")
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    /**
     * Background sync network from server
     */
    private suspend fun syncNetworkFromServer() {
        try {
            _uiState.update { it.copy(isSyncing = true) }

            Log.d(TAG, "Syncing network financial from server")

            saleRepository.fetchNetworkFinancialFromServer()

            Log.i(TAG, "Network financial synced from server")

            _uiState.update { state ->
                state.copy(
                    isSyncing = false,
                    lastSyncTime = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network financial sync failed: ${e.message}")
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    /**
     * Update financial UI
     */
    private fun updateFinancialUI(data: FinancialEntity) {
        val netMargin = if (data.totalRevenue > 0) {
            ((data.netProfit.toDouble() / data.totalRevenue) * 100).toInt()
        } else 0

        val operatingMargin = if (data.totalRevenue > 0) {
            (((data.totalRevenue - data.expenses).toDouble() / data.totalRevenue) * 100).toInt()
        } else 0

        _uiState.update {
            it.copy(
                totalRevenue = data.totalRevenue,
                cogs = data.cogs,
                grossProfit = data.totalRevenue - data.cogs,
                expenses = data.expenses,
                netProfit = data.netProfit,
                netMargin = netMargin,
                operatingMargin = operatingMargin,
                isLoading = false
            )
        }

        Log.d(TAG, "Financial UI updated: Revenue=$data.totalRevenue, Margin=$netMargin%")
    }

    /**
     * Refresh data
     */
    fun refreshData(storeId: Long? = null) {
        Log.i(TAG, "Manual refresh initiated")
        if (storeId != null) {
            loadStoreFinancial(storeId)
        } else {
            loadNetworkFinancial()
        }
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
        _uiState.update { it.copy(error = errorMessage, isLoading = false, isSyncing = false) }
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel cleared")
        observationJob?.cancel()
        super.onCleared()
    }
}
