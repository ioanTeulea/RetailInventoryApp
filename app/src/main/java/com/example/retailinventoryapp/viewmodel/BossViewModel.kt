package com.example.retailinventoryapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailinventoryapp.data.exception.ApiException
import com.example.retailinventoryapp.data.repository.EmployeeUiModel
import com.example.retailinventoryapp.data.repository.NetworkOverviewData
import com.example.retailinventoryapp.data.repository.SaleRepository
import com.example.retailinventoryapp.data.repository.StorePerformanceUiModel
import com.example.retailinventoryapp.data.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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

private const val TAG = "BossVM"

// ========== UI STATE ==========

data class BossUiState(
    val totalRevenue: Int = 0,
    val totalProfit: Int = 0,
    val growthPercent: Int = 0,
    val totalStores: Int = 0,
    val expenses: Int = 0,
    val storePerformance: List<StorePerformanceUiModel> = emptyList(),
    val topEmployees: List<EmployeeUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val lastSyncTime: Long = 0L
)

// ========== VIEWMODEL ==========

@HiltViewModel
class BossViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BossUiState())
    val uiState: StateFlow<BossUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        Log.d(TAG, "BossViewModel initialized")
        loadDashboardData()
    }

    /**
     * Load dashboard data - OFFLINE FIRST strategy.
     * 1. Instant load from local cache.
     * 2. Background resilient sync from server.
     */
    fun loadDashboardData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // ⚡ STEP 1: Load from local cache (Instant UX)
                val cachedData = saleRepository.getNetworkOverviewLocal()
                if (cachedData != null) {
                    Log.d(TAG, "Loaded data from local cache")
                    updateDashboardUI(cachedData)
                    // We can stop main loading if we have cached data to show
                    _uiState.update { it.copy(isLoading = false) }
                }

                // ✅ STEP 2: Resilient Sync from Server
                performParallelSync()

            } catch (e: Exception) {
                handleError(e, "Initial Load")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Executes parallel network requests with Exponential Backoff retry logic.
     */
    private suspend fun performParallelSync() {
        var currentDelay = 100L
        val maxRetries = 3

        repeat(maxRetries) { attempt ->
            try {
                _uiState.update { it.copy(isSyncing = true) }

                Log.d(TAG, "Sync attempt ${attempt + 1} started")

                // Launch all requests in parallel to save time
                val networkDataDeferred = viewModelScope.async { saleRepository.fetchNetworkOverviewFromServer() }
                val performanceDeferred = viewModelScope.async { storeRepository.fetchStoresPerformanceFromServer() }
                val employeesDeferred = viewModelScope.async { saleRepository.fetchTopEmployeesFromServer() }

                // Await all results
                val networkData = networkDataDeferred.await()
                val storePerformance = performanceDeferred.await()
                val topEmployees = employeesDeferred.await()

                // Persist fresh data to local database
                saleRepository.saveNetworkOverviewLocal(networkData)
                storeRepository.saveStoresPerformanceLocal(storePerformance)
                saleRepository.saveTopEmployeesLocal(topEmployees)

                // Update UI state with combined fresh data
                updateDashboardUI(
                    networkData.copy(
                        storePerformance = storePerformance,
                        topEmployees = topEmployees
                    )
                )

                _uiState.update { state ->
                    state.copy(
                        isSyncing = false,
                        message = "✅ Dashboard actualizat",
                        lastSyncTime = System.currentTimeMillis()
                    )
                }

                Log.i(TAG, "Dashboard parallel sync completed successfully")
                return // Success: exit the sync function

            } catch (e: Exception) {
                if (attempt == maxRetries - 1) {
                    handleError(e, "Sync Dashboard")
                } else {
                    Log.w(TAG, "Attempt ${attempt + 1} failed, retrying in ${currentDelay}ms...")
                    delay(currentDelay)
                    currentDelay = min(currentDelay * 2, 2000L)
                }
            }
        }
        _uiState.update { it.copy(isSyncing = false) }
    }

    private fun updateDashboardUI(data: NetworkOverviewData) {
        _uiState.update { state ->
            state.copy(
                totalRevenue = data.totalRevenue,
                totalProfit = data.totalProfit,
                growthPercent = data.growthPercent,
                totalStores = data.storePerformance.size,
                expenses = data.expenses,
                storePerformance = data.storePerformance,
                topEmployees = data.topEmployees
            )
        }
    }

    private fun handleError(exception: Exception, context: String) {
        val errorMessage = when (exception) {
            is ApiException -> "❌ Server Error ($context): ${exception.code}"
            is SocketTimeoutException -> "❌ Timeout: Serverul nu răspunde"
            is UnknownHostException -> "❌ Fără conexiune la internet"
            else -> "❌ $context: ${exception.message ?: "Eroare"}"
        }

        Log.e(TAG, "Error: $errorMessage", exception)
        _uiState.update { it.copy(error = errorMessage, isLoading = false, isSyncing = false) }
    }

    fun refreshData() = loadDashboardData()

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    override fun onCleared() {
        loadJob?.cancel()
        super.onCleared()
    }
}