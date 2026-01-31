package com.example.retailinventoryapp.ui.navigation

sealed class Destinations(val route: String) {
    data object CasierDashboard : Destinations("casier_dashboard")
    data object CasierScan : Destinations("casier_scan")
    data object CasierCart : Destinations("casier_cart")

    data object ManagerDashboard : Destinations("manager_dashboard")
    data object ManagerInventory : Destinations("manager_inventory")
    data object ManagerReports : Destinations("manager_reports")

    data object BossDashboard : Destinations("boss_dashboard")
    data object BossStoreDetail : Destinations("boss_store_detail/{storeId}") {
        fun createRoute(storeId: Long) = "boss_store_detail/$storeId"
    }
}