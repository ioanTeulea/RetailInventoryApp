package com.example.retailinventoryapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.retailinventoryapp.ui.screens.boss.BossDashboardScreen
import com.example.retailinventoryapp.ui.screens.boss.BossFinancialScreen
import com.example.retailinventoryapp.ui.screens.boss.BossStoreDetailScreen
import com.example.retailinventoryapp.ui.screens.casier.CasierCheckoutScreen
import com.example.retailinventoryapp.ui.screens.casier.CasierDashboardScreen
import com.example.retailinventoryapp.ui.screens.casier.CasierScanScreen
import com.example.retailinventoryapp.ui.screens.manager.ManagerDashboardScreen
import com.example.retailinventoryapp.ui.screens.manager.ManagerInventoryScreen
import com.example.retailinventoryapp.ui.screens.manager.ManagerReportsScreen
import com.example.retailinventoryapp.viewmodel.CasierScanViewModel
import com.example.retailinventoryapp.viewmodel.CasierCheckoutViewModel
import android.util.Log
import androidx.compose.runtime.collectAsState

private const val TAG = "AppNavigation"

/**
 * Main navigation graph for the app
 * Handles all screen transitions for 3 roles: Casier, Manager, Boss
 */
@Composable
fun AppNavigation(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Destinations.CasierDashboard.route
    ) {

        // ========== CASIER ROUTES ==========

        /**
         * Casier Dashboard - Main entry point for cashier
         * Shows today's transactions and revenue
         */
        composable(Destinations.CasierDashboard.route) {
            Log.d(TAG, "Composing CasierDashboard")

            CasierDashboardScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = { /* Navigarea către profil */ }
            )
        }

        /**
         * Casier Scan - Scan products and manage cart
         */
        composable(Destinations.CasierScan.route) {
            Log.d(TAG, "Composing CasierScan")

            CasierScanScreen(
                onNavigate = { route ->
                    if (route != Destinations.CasierScan.route) {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateToCheckout = { cartTotal ->
                    Log.d(TAG, "CasierScan → CasierCheckout (total: $cartTotal)")
                    navController.navigate(
                        Destinations.CasierCheckout.createRoute(cartTotal)
                    )
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        /**
         * Casier Checkout - Process payment
         * Parameters:
         * - cartTotal: Total amount to pay
         */
        composable(
            route = Destinations.CasierCheckout.route,
            arguments = listOf(
                navArgument("cartTotal") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            Log.d(TAG, "Composing CasierCheckout")

            val cartTotal = backStackEntry.arguments?.getInt("cartTotal") ?: 0
            val checkoutViewModel: CasierCheckoutViewModel = hiltViewModel()
            val scanViewModel: CasierScanViewModel = hiltViewModel()

            CasierCheckoutScreen(
                cartTotal = cartTotal.toDouble(),
                viewModel = checkoutViewModel,
                onNavigateBack = {
                    Log.d(TAG, "CasierCheckout → Back to Scan")
                    navController.popBackStack()
                },
                onPaymentSuccess = { receiptNumber ->
                    Log.i(TAG, "Payment successful! Receipt: $receiptNumber")
                    scanViewModel.clearCart()

                    // Navigate back to dashboard
                    navController.navigate(Destinations.CasierDashboard.route) {
                        popUpTo(Destinations.CasierDashboard.route) {
                            inclusive = false
                        }
                    }
                }
            )
        }

        // ========== MANAGER ROUTES ==========

        /**
         * Manager Dashboard - Main entry point for manager
         * Shows KPIs, low stock alerts, team performance
         */
        composable(Destinations.ManagerDashboard.route) {
            Log.d(TAG, "Composing ManagerDashboard")

            ManagerDashboardScreen(
                onNavigateToInventory = {
                    Log.d(TAG, "ManagerDashboard → ManagerInventory")
                    navController.navigate(Destinations.ManagerInventory.route)
                },
                onNavigateToReports = {
                    Log.d(TAG, "ManagerDashboard → ManagerReports")
                    navController.navigate(Destinations.ManagerReports.route)
                }
            )
        }

        /**
         * Manager Inventory - Product inventory management
         */
        composable(Destinations.ManagerInventory.route) {
            Log.d(TAG, "Composing ManagerInventory")

            ManagerInventoryScreen(
                onNavigateBack = {
                    Log.d(TAG, "ManagerInventory → Back to Dashboard")
                    navController.popBackStack()
                }
            )
        }

        /**
         * Manager Reports - Sales reports and analytics
         */
        composable(Destinations.ManagerReports.route) {
            Log.d(TAG, "Composing ManagerReports")

            ManagerReportsScreen(
                onNavigateBack = {
                    Log.d(TAG, "ManagerReports → Back to Dashboard")
                    navController.popBackStack()
                }
            )
        }

        // ========== BOSS ROUTES ==========

        /**
         * Boss Dashboard - Network overview
         * Shows all stores performance, top employees
         */
        composable(Destinations.BossDashboard.route) {
            Log.d(TAG, "Composing BossDashboard")

            BossDashboardScreen(
                onNavigateToStoreDetail = { storeId ->
                    Log.d(TAG, "BossDashboard → BossStoreDetail($storeId)")
                    navController.navigate(
                        Destinations.BossStoreDetail.createRoute(storeId)
                    )
                },
                onNavigateToFinancial = {
                    Log.d(TAG, "BossDashboard → BossFinancial (Network)")
                    navController.navigate(
                        Destinations.BossFinancial.createRoute(storeId = null)
                    )
                }
            )
        }

        /**
         * Boss Store Detail - Single store analytics
         * Parameters:
         * - storeId: The store to view
         */
        composable(
            route = Destinations.BossStoreDetail.route,
            arguments = listOf(
                navArgument("storeId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            Log.d(TAG, "Composing BossStoreDetail")

            val storeId = backStackEntry.arguments?.getLong("storeId") ?: 0L

            BossStoreDetailScreen(
                storeId = storeId,
                onNavigateBack = {
                    Log.d(TAG, "BossStoreDetail → Back to Dashboard")
                    navController.popBackStack()
                },
                onNavigateToFinancial = { selectedStoreId ->
                    Log.d(TAG, "BossStoreDetail → BossFinancial($selectedStoreId)")
                    navController.navigate(
                        Destinations.BossFinancial.createRoute(selectedStoreId)
                    )
                }
            )
        }

        /**
         * Boss Financial - Detailed financial analysis
         * Parameters:
         * - storeId: Store ID (0 = Network total, null = use parameter)
         */
        composable(
            route = Destinations.BossFinancial.route,
            arguments = listOf(
                navArgument("storeId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            Log.d(TAG, "Composing BossFinancial")

            val storeId = backStackEntry.arguments?.getLong("storeId")

            BossFinancialScreen(
                storeId = if (storeId == 0L) null else storeId,
                onNavigateBack = {
                    Log.d(TAG, "BossFinancial → Back")
                    navController.popBackStack()
                }
            )
        }
    }
}