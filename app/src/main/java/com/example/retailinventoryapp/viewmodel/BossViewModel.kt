package com.example.retailinventoryapp.viewmodel


import android.os.Build
import androidx.annotation.RequiresApi
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

data class BossUiState(
    val totalRevenue: Int = 2750000,      // Total venituri luna
    val totalProfit: Int = 495000,        // Profit net
    val growthPercent: Int = 15,          // Growth vs luna trecuta
    val totalStores: Int = 3,             // Numarul de magazine
    val expenses: Int = 2255000,          // Cheltuieli totale
    val storePerformance: List<StorePerformanceUiModel> = emptyList(),  // Ranking magazine
    val topEmployees: List<EmployeeUiModel> = emptyList(),             // Top performeri
    val isLoading: Boolean = false,       // Loading indicator
    val error: String? = null             // Error message
)

data class StorePerformanceUiModel(
    val storeId: Long,
    val storeName: String,
    val revenue: Int,
    val growth: Int
)

data class EmployeeUiModel(
    val id: Long,
    val name: String,
    val store: String,
    val sales: Int,
    val rating: Double
)

// ========== VIEWMODEL ==========

@HiltViewModel
class BossViewModel @Inject constructor() : ViewModel() { // Am scos repo-urile din paranteză

    private val _uiState = MutableStateFlow(BossUiState())
    val uiState: StateFlow<BossUiState> = _uiState.asStateFlow()

    init {
        // În loc de loadDashboardData() care ar pica, punem date de test direct
        simulateTestData()
    }

    private fun simulateTestData() {
        // Date "hardcoded" pentru a vedea cum arată UI-ul
        _uiState.value = BossUiState(
            totalRevenue = 2750000,
            totalProfit = (2750000 * 0.18).toInt(), // Profit calculated as $$TotalProfit = Revenue \times 0.18$$
            growthPercent = 15,
            totalStores = 3,
            expenses = (2750000 * 0.82).toInt(),
            storePerformance = listOf(
                StorePerformanceUiModel(1, "Magazin Central", 1200000, 12),
                StorePerformanceUiModel(2, "Magazin Sud", 850000, -5),
                StorePerformanceUiModel(3, "Mall Vitan", 700000, 8)
            ),
            topEmployees = listOf(
                EmployeeUiModel(1, "Andrei Ionescu", "Magazin Central", 450, 4.9),
                EmployeeUiModel(2, "Maria Popescu", "Mall Vitan", 380, 4.7)
            ),
            isLoading = false
        )
    }

    // Restul functiilor raman goale momentan
    fun refreshData() { simulateTestData() }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}