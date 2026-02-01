package com.example.retailinventoryapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailinventoryapp.data.exception.ApiException
import com.example.retailinventoryapp.data.repository.ReportData
import com.example.retailinventoryapp.data.repository.SaleRepository
import com.example.retailinventoryapp.ui.screens.manager.ReportPeriod
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

private const val TAG = "ManagerReportsVM"

// ========== UI MODELS & STATE ==========

data class TopProductUiModel(
    val name: String,
    val quantity: Int,
    val revenue: Int
)

data class ManagerReportsUiState(
    val totalRevenue: Double = 0.0,
    val revenueGrowth: Int = 0,
    val totalTransactions: Int = 0,
    val transactionGrowth: Int = 0,
    val avgTransaction: Number = 0,
    val profitMargin: Int = 18,
    val topProducts: List<TopProductUiModel> = emptyList(),
    val cashPayments: Double = 0.0,
    val cardPayments: Double = 0.0,
    val checkPayments: Double = 0.0,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val lastSyncTime: Long = 0L
)

// ========== VIEWMODEL ==========

@HiltViewModel
class ManagerReportsViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerReportsUiState())
    val uiState: StateFlow<ManagerReportsUiState> = _uiState.asStateFlow()

    private var reportJob: Job? = null

    init {
        Log.d(TAG, "Reports ViewModel initialized")
        // Încărcăm automat raportul pentru astăzi la start
        loadReportData(ReportPeriod.TODAY)
    }

    /**
     * Standard Reactive Load: Offline-First approach.
     */
    fun loadReportData(period: ReportPeriod) {
        reportJob?.cancel()

        _uiState.update { it.copy(isLoading = true, error = null) }

        reportJob = viewModelScope.launch {
            // 1. LAYER LOCAL: Încărcăm din cache imediat pentru UX rapid
            val cachedReport = saleRepository.getReportLocal(period.name)
            if (cachedReport != null) {
                mapReportToUiState(cachedReport)
            }

            // 2. LAYER NETWORK: Sincronizare cu Exponential Backoff (ca în Inventory)
            launch {
                performBackgroundSync(period)
            }
        }
    }

    /**
     * Background sync with retry logic (Golden Standard).
     */
    private suspend fun performBackgroundSync(period: ReportPeriod) {
        var currentDelay = 100L
        repeat(3) { attempt ->
            try {
                _uiState.update { it.copy(isSyncing = true) }
                val freshReport = saleRepository.fetchReportFromServer(period.name)
                saleRepository.saveReportLocal(freshReport, period.name)
                mapReportToUiState(freshReport)
                _uiState.update { it.copy(isSyncing = false, message = "✅ Raport actualizat") }
                return
            } catch (e: Exception) {
                if (attempt == 2) handleError(e, "Sync")
                delay(currentDelay)
                currentDelay = min(currentDelay * 2, 2000L)
            }
        }
    }

    private fun mapReportToUiState(report: ReportData) {
        _uiState.update { state ->
            state.copy(
                totalRevenue = report.totalRevenue,
                revenueGrowth = report.revenueGrowth, // ✅ Acum se va mapa corect
                totalTransactions = report.totalTransactions,
                transactionGrowth = report.transactionGrowth, // ✅ Acum se va mapa corect
                avgTransaction = if (report.totalTransactions > 0) report.totalRevenue / report.totalTransactions else 0,
                profitMargin = report.profitMargin,
                topProducts = report.topProducts.map { TopProductUiModel(it.name, it.quantity, it.revenue) },
                cashPayments = report.cashPayments,
                cardPayments = report.cardPayments,
                checkPayments = report.checkPayments,
                isLoading = false
            )
        }
    }

    fun exportToPdf(period: String) {
        viewModelScope.launch {
            try {
                val pdfUrl = saleRepository.exportReportToPdf(period)
                _uiState.update { it.copy(message = "✅ PDF exportat: $pdfUrl") }
            } catch (e: Exception) {
                handleError(e, "Export PDF")
            }
        }
    }

    fun exportToCsv(period: String) {
        viewModelScope.launch {
            try {
                val csvUrl = saleRepository.exportReportToCsv(period)
                _uiState.update { it.copy(message = "✅ CSV exportat: $csvUrl") }
            } catch (e: Exception) {
                handleError(e, "Export CSV")
            }
        }
    }

    private fun handleError(e: Exception, context: String) {
        val msg = when (e) {
            is ApiException -> "❌ Server ($context): ${e.code}"
            is SocketTimeoutException -> "❌ Timeout: Serverul nu răspunde"
            is UnknownHostException -> "❌ Fără internet"
            else -> "❌ $context: ${e.message}"
        }
        Log.e(TAG, msg)
        _uiState.update { it.copy(error = msg, isLoading = false, isSyncing = false) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }

    override fun onCleared() {
        reportJob?.cancel()
        super.onCleared()
    }
}