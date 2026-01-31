package com.example.retailinventoryapp.ui.screens.casier


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.retailinventoryapp.ui.theme.RetailColors
import com.example.retailinventoryapp.viewmodel.CasierUiState
import com.example.retailinventoryapp.viewmodel.CasierViewModel
import com.example.retailinventoryapp.viewmodel.SaleUiModel

import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart



@Composable
fun CasierDashboardScreen(
    viewModel: CasierViewModel = hiltViewModel(),
    onNavigateToScan: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CasierTopBar(
                userName = uiState.userName,
                onProfileClick = onNavigateToProfile
            )
        },
        bottomBar = {
            CasierBottomBar(
                onScanClick = onNavigateToScan,
                onCartClick = { /* Navigate to cart */ },
                onHistoryClick = { /* Show history */ }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(RetailColors.Background)
                .verticalScroll(rememberScrollState())
        ) {

            // Quick Stats
            QuickStatsSection(uiState)

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            ActionButtonsSection(
                onScanClick = onNavigateToScan,
                onCheckoutClick = { /* Navigate */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Today's Sales
            TodaysSalesSection(uiState)

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Access
            QuickAccessSection()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CasierTopBar(
    userName: String,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        color = RetailColors.Surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Bună,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RetailColors.OnSurfaceLight
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onProfileClick) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = RetailColors.Primary
                )
            }
        }
    }
}

@Composable
fun CasierBottomBar(
    onScanClick: () -> Unit,
    onCartClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        containerColor = RetailColors.Surface,
        contentColor = RetailColors.Primary
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.QrCode2,
                    contentDescription = "Scan",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Scan", fontSize = 11.sp) },
            selected = true,
            onClick = onScanClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RetailColors.Primary,
                selectedTextColor = RetailColors.Primary,
                indicatorColor = RetailColors.Primary.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Cart"
                )
            },
            label = { Text("Coș", fontSize = 11.sp) },
            selected = false,
            onClick = onCartClick,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = RetailColors.OnSurfaceLight
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.History,
                    contentDescription = "History"
                )
            },
            label = { Text("Istoric", fontSize = 11.sp) },
            selected = false,
            onClick = onHistoryClick,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = RetailColors.OnSurfaceLight
            )
        )
    }
}

@Composable
fun QuickStatsSection(uiState: CasierUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Astazi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Tranzacții",
                value = "${uiState.todayTransactions}",
                icon = Icons.Default.Receipt,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Vânzări",
                value = "${uiState.todayRevenue} RON",
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Medie",
                value = "${uiState.avgTransaction} RON",
                icon = Icons.Default.BarChart,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        color = RetailColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RetailColors.BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
                Icon(
                    icon,
                    contentDescription = title,
                    tint = RetailColors.Primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RetailColors.OnSurface
            )
        }
    }
}

@Composable
fun ActionButtonsSection(
    onScanClick: () -> Unit,
    onCheckoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(
                text = "🔍 SCAN",
                onClick = onScanClick,
                modifier = Modifier.weight(1f)
            )

            SecondaryButton(
                text = "💳 CHECKOUT",
                onClick = onCheckoutClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RetailColors.Primary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp),
        border = BorderStroke(2.dp, RetailColors.Primary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = RetailColors.Primary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun TodaysSalesSection(uiState: CasierUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Vânzări Recente",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        uiState.recentSales.take(3).forEach { sale ->
            SaleItemCard(sale)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SaleItemCard(sale: SaleUiModel) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(10.dp),
        color = RetailColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RetailColors.BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vânzare #${sale.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = sale.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
            }

            Text(
                text = "${sale.amount} RON",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = RetailColors.Success
            )
        }
    }
}

@Composable
fun QuickAccessSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Acces Rapid",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAccessButton(
                icon = "📋",
                label = "Chitanță",
                modifier = Modifier.weight(1f)
            )
            QuickAccessButton(
                icon = "🔄",
                label = "Stoc",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickAccessButton(
    icon: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        color = RetailColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RetailColors.BorderLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

