package com.example.retailinventoryapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailinventoryapp.data.exception.ApiException
import com.example.retailinventoryapp.data.repository.ManagerDashboardData
import com.example.retailinventoryapp.data.repository.ProductRepository
import com.example.retailinventoryapp.data.repository.SaleRepository
import com.example.retailinventoryapp.data.repository.StockAlertUiModel // Import din Repository
import com.example.retailinventoryapp.data.repository.TeamMemberUiModel // Import din Repository
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

private const val TAG = "ManagerVM"

// ========== UI STATE ==========

data class ManagerUiState(
    val storeName: String = "",
    val todayRevenue: Int = 0,
    val totalTransactions: Int = 0,
    val lowStockAlerts: List<StockAlertUiModel> = emptyList(),
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val teamPerformance: List<TeamMemberUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val lastSyncTime: Long = 0L
)

// ========== VIEWMODEL ==========

@HiltViewModel
class ManagerViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerUiState())
    val uiState: StateFlow<ManagerUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadDashboardData()
    }

    /**
     * Load dashboard data - Offline-First Strategy
     */
    fun loadDashboardData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Stratul Local (Instant)
            val cachedData = saleRepository.getManagerDashboardLocal()
            if (cachedData != null) {
                updateManagerUI(cachedData)
                _uiState.update { it.copy(isLoading = false) }
            }

            // 2. Stratul de Network (Background Sync)
            performBackgroundSync()
        }
    }

    /**
     * Background sync with Exponential Backoff (The Golden Standard)
     */
    private suspend fun performBackgroundSync() {
        var currentDelay = 100L
        val maxRetries = 3

        repeat(maxRetries) { attempt ->
            try {
                _uiState.update { it.copy(isSyncing = true) }

                Log.d(TAG, "Sync attempt ${attempt + 1}")

                // Fetch fresh data
                val dashboardData = saleRepository.fetchManagerDashboardFromServer()
                val lowStockAlerts = productRepository.fetchLowStockAlertsFromServer()
                val teamPerformance = saleRepository.fetchTeamPerformanceFromServer()

                // Persist to local cache
                saleRepository.saveManagerDashboardLocal(dashboardData)
                productRepository.saveLowStockAlertsLocal(lowStockAlerts)
                saleRepository.saveTeamPerformanceLocal(teamPerformance)

                // Update UI - asamblează datele proaspete
                updateManagerUI(
                    dashboardData.copy(
                        lowStockAlerts = lowStockAlerts,
                        teamPerformance = teamPerformance
                    )
                )

                _uiState.update { state ->
                    state.copy(
                        isSyncing = false,
                        message = "✅ Dashboard actualizat",
                        lastSyncTime = System.currentTimeMillis()
                    )
                }
                return // Ieșim din funcție la succes
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) {
                    handleError(e)
                } else {
                    delay(currentDelay)
                    currentDelay = min(currentDelay * 2, 2000L)
                }
            }
        }
        _uiState.update { it.copy(isSyncing = false) }
    }

    private fun updateManagerUI(data: ManagerDashboardData) {
        _uiState.update { state ->
            state.copy(
                storeName = data.storeName,
                todayRevenue = data.todayRevenue,
                totalTransactions = data.totalTransactions,
                lowStockAlerts = data.lowStockAlerts,
                totalProducts = data.totalProducts,
                lowStockCount = data.lowStockCount,
                outOfStockCount = data.outOfStockCount,
                teamPerformance = data.teamPerformance,
                isLoading = false
            )
        }
    }

    fun processRefund(saleId: Long, reason: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSyncing = true) }
                saleRepository.refundSale(saleId, reason)
                _uiState.update { it.copy(message = "✅ Refund procesat", isSyncing = false) }
                loadDashboardData()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(e: Exception) {
        val msg = when (e) {
            is ApiException -> "❌ Server Error: ${e.code}"
            is SocketTimeoutException -> "❌ Timeout: Serverul nu răspunde"
            is UnknownHostException -> "❌ Fără internet"
            else -> "❌ Eroare: ${e.message}"
        }
        _uiState.update { it.copy(error = msg, isLoading = false, isSyncing = false) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }

    override fun onCleared() {
        loadJob?.cancel()
        super.onCleared()
    }
}