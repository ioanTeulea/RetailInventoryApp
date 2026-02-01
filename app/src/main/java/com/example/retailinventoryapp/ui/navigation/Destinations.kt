package com.example.retailinventoryapp.ui.navigation

/**
 * Type-safe navigation destinations with route creation
 */
sealed class Destinations(val route: String) {

    // ========== CASIER ==========
    data object CasierDashboard : Destinations("casier_dashboard")

    data object CasierScan : Destinations("casier_scan")

    data object CasierCheckout : Destinations("casier_checkout/{cartTotal}") {
        fun createRoute(cartTotal: Double) = "casier_checkout/${cartTotal.toInt()}"
    }

    // ========== MANAGER ==========
    data object ManagerDashboard : Destinations("manager_dashboard")

    data object ManagerInventory : Destinations("manager_inventory")

    data object ManagerReports : Destinations("manager_reports")

    // ========== BOSS ==========
    data object BossDashboard : Destinations("boss_dashboard")

    data object BossFinancial : Destinations("boss_financial/{storeId}") {
        fun createRoute(storeId: Long? = null): String {
            val id = storeId ?: 0L
            return "boss_financial/$id"
        }
    }

    data object BossStoreDetail : Destinations("boss_store_detail/{storeId}") {
        fun createRoute(storeId: Long) = "boss_store_detail/$storeId"
    }
}