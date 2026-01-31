package com.example.retailinventoryapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.retailinventoryapp.ui.screens.casier.CasierDashboardScreen
import com.example.retailinventoryapp.ui.screens.manager.ManagerDashboardScreen

// Importa si celelalte ecrane (BossDashboardScreen, ManagerDashboardScreen etc.)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Start destination should ideally be Login, but we use Casier for testing
    NavHost(
        navController = navController,
        startDestination = Destinations.CasierDashboard.route
    ) {

        // --- CASIER ROUTES ---
        composable(Destinations.CasierDashboard.route) {
            CasierDashboardScreen(
                onNavigateToScan = { navController.navigate(Destinations.CasierScan.route) },
                onNavigateToProfile = { /* Navigate to Profile */ }
            )
        }

        composable(Destinations.CasierScan.route) {
            // Placeholder for Scan Screen
            // ScanScreen(onBack = { navController.popBackStack() })
        }

        // --- MANAGER ROUTES ---
        composable(Destinations.ManagerDashboard.route) {
             ManagerDashboardScreen(
                onNavigateToInventory = { navController.navigate(Destinations.ManagerInventory.route) }
             )
        }

        // --- BOSS ROUTES ---
        composable(Destinations.BossDashboard.route) {
            // BossDashboardScreen(
            //    onStoreClick = { id ->
            //        navController.navigate(Destinations.BossStoreDetail.createRoute(id))
            //    }
            // )
        }

        // Route with Arguments: Boss Store Detail
        composable(
            route = Destinations.BossStoreDetail.route,
            arguments = listOf(navArgument("storeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getLong("storeId") ?: 0L
            // StoreDetailScreen(storeId = storeId)
        }
    }
}