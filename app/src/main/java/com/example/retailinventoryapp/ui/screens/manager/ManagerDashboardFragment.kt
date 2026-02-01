package com.example.retailinventoryapp.ui.screens.manager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.retailinventoryapp.ui.theme.RetailColors
import com.example.retailinventoryapp.viewmodel.ManagerUiState
import com.example.retailinventoryapp.viewmodel.ManagerViewModel
import androidx.compose.ui.text.style.TextAlign
import com.example.retailinventoryapp.data.repository.StockAlertUiModel
import com.example.retailinventoryapp.data.repository.TeamMemberUiModel

@Composable
fun ManagerDashboardScreen(
    viewModel: ManagerViewModel = hiltViewModel(),
    onNavigateToInventory: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ManagerTopBar(
                storeName = "Store 1",
                onNavigateToReports = onNavigateToReports
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(RetailColors.Background)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // KPI Cards
            item {
                ManagerKPISection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Alerts Section
            item {
                LowStockAlertsSection(
                    alerts = uiState.lowStockAlerts,
                    onNavigateToInventory = onNavigateToInventory
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Inventory Status
            item {
                InventoryStatusSection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Team Performance
            item {
                TeamPerformanceSection(uiState)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ✅ UPDATED: Add Reports Button to TopBar
@Composable
fun ManagerTopBar(
    storeName: String,
    onNavigateToReports: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = RetailColors.Surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Store Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Manager",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RetailColors.OnSurfaceLight
                )
                Text(
                    text = storeName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right: Reports Button ✅
            Button(
                onClick = { onNavigateToReports() },
                modifier = Modifier
                    .height(40.dp)
                    .width(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetailColors.Primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = "Reports",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Text(
                        "Rapoarte",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ManagerKPISection(uiState: ManagerUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "KPI Astazi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KPICard(
                title = "Vânzări",
                value = "${uiState.todayRevenue} RON",
                trend = "+15%",
                trendPositive = true,
                modifier = Modifier.weight(1f)
            )

            KPICard(
                title = "Tranzacții",
                value = "${uiState.totalTransactions}",
                trend = "+8%",
                trendPositive = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun KPICard(
    title: String,
    value: String,
    trend: String,
    trendPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        color = RetailColors.Primary.copy(alpha = 0.05f),
        border = BorderStroke(1.5.dp, RetailColors.Primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = RetailColors.OnSurfaceLight,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RetailColors.Primary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    if (trendPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = "Trend",
                    tint = if (trendPositive) RetailColors.Success else RetailColors.Error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = trend,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (trendPositive) RetailColors.Success else RetailColors.Error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LowStockAlertsSection(
    alerts: List<StockAlertUiModel>,
    onNavigateToInventory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "⚠️ Stoc Scăzut",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onNavigateToInventory) {
                Text("Vezi tot", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (alerts.isNotEmpty()) {
            alerts.take(3).forEach { alert ->
                LowStockAlertItem(alert)
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text(
                text = "Toate produsele sunt în stoc",
                style = MaterialTheme.typography.bodyMedium,
                color = RetailColors.OnSurfaceLight,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
fun LowStockAlertItem(alert: StockAlertUiModel) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(12.dp),
        color = RetailColors.Warning.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, RetailColors.Warning.copy(alpha = 0.3f))
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
                    text = alert.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${alert.currentStock} / ${alert.threshold} unități",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
            }

            Button(
                onClick = { /* Order */ },
                modifier = Modifier
                    .height(36.dp)
                    .width(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetailColors.Warning
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Comanda",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun InventoryStatusSection(uiState: ManagerUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Status Inventar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InventoryStatusCard(
                title = "Total Produse",
                value = "${uiState.totalProducts}",
                color = RetailColors.Primary,
                modifier = Modifier.weight(1f)
            )

            InventoryStatusCard(
                title = "Stoc Scăzut",
                value = "${uiState.lowStockCount}",
                color = RetailColors.Warning,
                modifier = Modifier.weight(1f)
            )

            InventoryStatusCard(
                title = "Epuizat",
                value = "${uiState.outOfStockCount}",
                color = RetailColors.Error,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun InventoryStatusCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.OnSurfaceLight,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TeamPerformanceSection(uiState: ManagerUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Performance Echipă",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        uiState.teamPerformance.forEach { member ->
            TeamMemberCard(member)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TeamMemberCard(member: TeamMemberUiModel) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp),
        shape = RoundedCornerShape(12.dp),
        color = RetailColors.Surface,
        border = BorderStroke(1.dp, RetailColors.BorderLight),
        shadowElevation = 1.dp
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
                    text = member.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Vânzări: ${member.sales} RON",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatingStars(member.rating)
                Text(
                    text = member.rating.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RatingStars(rating: Double) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { index ->
            Icon(
                Icons.Default.Star,
                contentDescription = "Star",
                tint = if (index < rating.toInt()) RetailColors.Warning else RetailColors.BorderLight,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}