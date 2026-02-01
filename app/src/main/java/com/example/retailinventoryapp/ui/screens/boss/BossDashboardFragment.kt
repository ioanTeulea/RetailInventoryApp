package com.example.retailinventoryapp.ui.screens.boss

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
import com.example.retailinventoryapp.data.repository.EmployeeUiModel
import com.example.retailinventoryapp.data.repository.StorePerformanceUiModel
import com.example.retailinventoryapp.ui.theme.RetailColors
import com.example.retailinventoryapp.viewmodel.BossUiState
import com.example.retailinventoryapp.viewmodel.BossViewModel

@Composable
fun BossDashboardScreen(
    viewModel: BossViewModel = hiltViewModel(),
    onNavigateToStoreDetail: (storeId: Long) -> Unit,
    onNavigateToFinancial: () -> Unit  // ✅ Already have this!
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            BossTopBar(onNavigateToFinancial = onNavigateToFinancial)  // ✅ Pass callback
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

            // Network Overview
            item {
                NetworkOverviewSection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Stores Ranking
            item {
                StoresRankingSection(
                    stores = uiState.storePerformance,
                    onNavigateToStoreDetail = onNavigateToStoreDetail
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Financial Summary
            item {
                FinancialSummarySection(
                    uiState = uiState,
                    onNavigateToFinancial = onNavigateToFinancial  // ✅ Pass callback
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Top Employees
            item {
                TopEmployeesSection(uiState)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ✅ UPDATED: Add Financial Button to TopBar
@Composable
fun BossTopBar(onNavigateToFinancial: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        color = RetailColors.Primary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Header Info
            Column {
                Text(
                    text = "Network Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "3 Magazine Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Right: Financial Button ✅
            Button(
                onClick = { onNavigateToFinancial() },
                modifier = Modifier
                    .height(40.dp)
                    .width(130.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = "Financial",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Text(
                        "Financiar",
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
fun NetworkOverviewSection(uiState: BossUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Sumar Rețea",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NetworkMetricCard(
                    title = "Venituri Totale",
                    value = "${uiState.totalRevenue} RON",
                    subtitle = "Luna aceasta",
                    color = RetailColors.Success,
                    modifier = Modifier.weight(1f)
                )

                NetworkMetricCard(
                    title = "Profit",
                    value = "${uiState.totalProfit} RON",
                    subtitle = "18% margin",
                    color = RetailColors.Primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NetworkMetricCard(
                    title = "Creștere",
                    value = "${uiState.growthPercent}%",
                    subtitle = "vs luna trecută",
                    color = RetailColors.Warning,
                    modifier = Modifier.weight(1f)
                )

                NetworkMetricCard(
                    title = "Magazine",
                    value = "${uiState.totalStores}",
                    subtitle = "Active",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NetworkMetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.2f))
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

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
            }
        }
    }
}

@Composable
fun StoresRankingSection(
    stores: List<StorePerformanceUiModel>,
    onNavigateToStoreDetail: (storeId: Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "🏆 Clasament Magazine",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        stores.forEachIndexed { index, store ->
            StoreRankingCard(
                rank = index + 1,
                store = store,
                onClick = { onNavigateToStoreDetail(store.storeId) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun StoreRankingCard(
    rank: Int,
    store: StorePerformanceUiModel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .clickable(onClick = onClick),
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = when (rank) {
                    1 -> Color(0xFFFFD700)
                    2 -> Color(0xFFC0C0C0)
                    3 -> Color(0xFFCD7F32)
                    else -> RetailColors.Primary
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = store.storeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${store.revenue} RON • ${store.growth}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    if (store.growth > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = "Trend",
                    tint = if (store.growth > 0) RetailColors.Success else RetailColors.Error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ✅ UPDATED: Add Navigation Button to Financial Section
@Composable
fun FinancialSummarySection(
    uiState: BossUiState,
    onNavigateToFinancial: () -> Unit
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
                text = "Analiză Financiară",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { onNavigateToFinancial() }) {
                Text("Detalii", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(16.dp),
            color = RetailColors.Surface,
            border = BorderStroke(1.dp, RetailColors.BorderLight),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                FinancialRow(
                    label = "Venituri",
                    value = "${uiState.totalRevenue} RON",
                    percentage = "100%"
                )
                Divider(color = RetailColors.BorderLight)
                FinancialRow(
                    label = "Cheltuieli",
                    value = "${uiState.expenses} RON",
                    percentage = "82%"
                )
                Divider(color = RetailColors.BorderLight)
                FinancialRow(
                    label = "Profit Net",
                    value = "${uiState.totalProfit} RON",
                    percentage = "18%",
                    highlight = true
                )
            }
        }
    }
}

@Composable
fun FinancialRow(
    label: String,
    value: String,
    percentage: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (highlight) RetailColors.Primary else RetailColors.OnSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (highlight) RetailColors.Primary else RetailColors.OnSurface
            )
        }

        Surface(
            modifier = Modifier
                .height(28.dp)
                .width(60.dp),
            shape = RoundedCornerShape(6.dp),
            color = if (highlight) RetailColors.Primary.copy(alpha = 0.1f)
            else RetailColors.BorderLight
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = percentage,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (highlight) RetailColors.Primary else RetailColors.OnSurfaceLight
                )
            }
        }
    }
}

@Composable
fun TopEmployeesSection(uiState: BossUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "⭐ Top Performeri",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        uiState.topEmployees.forEach { employee ->
            EmployeeCard(employee)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun EmployeeCard(employee: EmployeeUiModel) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
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
                    text = employee.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${employee.store} • ${employee.sales} RON vânzări",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
            }

            Badge(
                containerColor = RetailColors.Warning.copy(alpha = 0.2f),
                contentColor = RetailColors.Warning
            ) {
                Text(
                    text = "⭐${employee.rating}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}