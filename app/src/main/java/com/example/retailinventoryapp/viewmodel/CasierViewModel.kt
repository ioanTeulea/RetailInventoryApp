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

data class CasierUiState(
    val userName: String = "Maria",
    val todayTransactions: Int = 0,
    val todayRevenue: Int = 0,
    val avgTransaction: Int = 0,
    val recentSales: List<SaleUiModel> = emptyList(),
    val isLoading: Boolean = false
)

data class SaleUiModel(
    val id: Long,
    val amount: Int,
    val time: String
)

// ========== VIEWMODEL ==========

@HiltViewModel
class CasierViewModel @Inject constructor(
    // private val saleRepository: SaleRepository // ❌ Comentează asta pentru test
) : ViewModel() {

    private val _uiState = MutableStateFlow(CasierUiState())
    val uiState: StateFlow<CasierUiState> = _uiState.asStateFlow()

    init {
        // În loc de loadTodayStats(), apelăm o funcție cu date "fake"
        generateMockStats()
    }

    /**
     * Date de test pentru a vedea cum arată dashboard-ul casierului
     */
    fun generateMockStats() {
        _uiState.value = CasierUiState(
            userName = "Maria Popescu",
            todayTransactions = 24,
            todayRevenue = 1250,
            avgTransaction = (1250 / 24), // $$Avg = \frac{Total}{Count}$$
            recentSales = listOf(
                SaleUiModel(1001, 45, "12:30"),
                SaleUiModel(1002, 120, "12:45"),
                SaleUiModel(1003, 15, "13:05"),
                SaleUiModel(1004, 250, "13:10")
            ),
            isLoading = false
        )
    }

    fun loadTodayStats() { generateMockStats() }
    fun refreshStats() { generateMockStats() }

    // Comentează sau șterge temporar logica ce folosește repository-ul
    /*
    fun processSale(items: List<SaleItem>, paymentMethod: String) {
        // Logică goală pentru build
    }
    */
}