package com.example.retailinventoryapp.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDateTime

// ========== DATA CLASSES ==========

data class ManagerUiState(
    val storeName: String = "Store 1",
    val todayRevenue: Int = 0,
    val totalTransactions: Int = 0,
    val lowStockAlerts: List<StockAlertUiModel> = emptyList(),
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val teamPerformance: List<TeamMemberUiModel> = emptyList(),
    val isLoading: Boolean = false
)

data class StockAlertUiModel(
    val id: Long,
    val productName: String,
    val currentStock: Int,
    val threshold: Int
)

data class TeamMemberUiModel(
    val id: Long,
    val name: String,
    val sales: Int,
    val rating: Double
)

// ========== VIEWMODEL ==========

@HiltViewModel
class ManagerViewModel @Inject constructor(
    // private val saleRepository: SaleRepository,    // ❌ Dezactivat temporar
    // private val productRepository: ProductRepository,
    // private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerUiState())
    val uiState: StateFlow<ManagerUiState> = _uiState.asStateFlow()

    init {
        generateMockDashboardData()
    }

    /**
     * Generează date fictive pentru a testa UI-ul de Manager
     */
    private fun generateMockDashboardData() {
        _uiState.value = ManagerUiState(
            storeName = "Magazin Centru",
            todayRevenue = 15400,
            totalTransactions = 142,
            totalProducts = 1250,
            lowStockCount = 5,
            outOfStockCount = 2,
            lowStockAlerts = listOf(
                StockAlertUiModel(1, "Apă Minerală 2L", 8, 20),
                StockAlertUiModel(2, "Pâine Secară", 3, 15),
                StockAlertUiModel(3, "Ciocolată cu Lapte", 12, 25)
            ),
            teamPerformance = listOf(
                TeamMemberUiModel(101, "Ion Ionescu", 4500, 4.8),
                TeamMemberUiModel(102, "Elena Popa", 3900, 4.9),
                TeamMemberUiModel(103, "Marius Dan", 3100, 4.2)
            ),
            isLoading = false
        )
    }

    // Funcții goale pentru a nu da erori în restul codului
    fun loadDashboardData() { generateMockDashboardData() }
    fun refreshData() { generateMockDashboardData() }
    fun processRefund(saleId: Long, reason: String) { /* Mock action */ }
}