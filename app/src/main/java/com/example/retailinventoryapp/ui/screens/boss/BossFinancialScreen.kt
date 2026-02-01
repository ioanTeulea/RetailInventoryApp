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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.retailinventoryapp.ui.theme.RetailColors
import com.example.retailinventoryapp.viewmodel.BossFinancialUiState
import com.example.retailinventoryapp.viewmodel.BossFinancialViewModel


@Composable
fun BossFinancialScreen(
    storeId: Long?,
    viewModel: BossFinancialViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(storeId) {
        if (storeId != null) {
            viewModel.loadStoreFinancial(storeId)
        } else {
            viewModel.loadNetworkFinancial()
        }
    }

    Scaffold(
        topBar = {
            FinancialTopBar(
                title = storeId?.let { "Financiar Magazin" } ?: "Financiar Retea",
                onBackClick = onNavigateBack
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

            // Income Statement
            item {
                IncomeStatementSection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Breakdown
            item {
                ExpenseBreakdownSection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Margin Analysis
            item {
                MarginAnalysisSection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Cash Flow
            item {
                CashFlowSection(uiState)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FinancialTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        color = RetailColors.Primary,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                Icons.Default.TrendingUp,
                contentDescription = "Financial",
                tint = Color.White
            )
        }
    }
}

@Composable
fun IncomeStatementSection(uiState: com.example.retailinventoryapp.viewmodel.BossFinancialUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Declaratia de Venit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = RetailColors.Surface,
            border = BorderStroke(1.dp, RetailColors.BorderLight),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinancialRow(
                    label = "Venituri",
                    value = "${uiState.totalRevenue} RON",
                    percentage = "100%"
                )

                Divider(color = RetailColors.BorderLight)

                FinancialRow(
                    label = "Cost of Goods Sold",
                    value = "${uiState.cogs} RON",
                    percentage = "${uiState.cogs}%"
                )

                FinancialRow(
                    label = "Gross Profit",
                    value = "${uiState.grossProfit} RON",
                    percentage = "${uiState.grossMargin}%",
                    highlight = true
                )

                Divider(color = RetailColors.BorderLight)

                FinancialRow(
                    label = "Operating Expenses",
                    value = "${uiState.expenses} RON",
                    percentage = "${uiState.expenses}%"
                )

                Divider(color = RetailColors.BorderLight)

                FinancialRow(
                    label = "Net Profit",
                    value = "${uiState.netProfit} RON",
                    percentage = "${uiState.netMargin}%",
                    highlight = true,
                    color = RetailColors.Success
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
    highlight: Boolean = false,
    color: Color = RetailColors.Primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) color else RetailColors.OnSurface
        )

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
                color = if (highlight) color else RetailColors.OnSurface
            )
            Text(
                text = percentage,
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.OnSurfaceLight
            )
        }
    }
}

@Composable
fun ExpenseBreakdownSection(uiState: com.example.retailinventoryapp.viewmodel.BossFinancialUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Dezagregarea Cheltuielilor",
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
            ExpenseCard(
                label = "Salarii",
                amount = uiState.salaries,
                percentage = uiState.salariesPercent,
                color = RetailColors.Primary,
                modifier = Modifier.weight(1f)
            )

            ExpenseCard(
                label = "Chirie",
                amount = uiState.rent,
                percentage = uiState.rentPercent,
                color = RetailColors.Warning,
                modifier = Modifier.weight(1f)
            )

            ExpenseCard(
                label = "Utilitati",
                amount = uiState.utilities,
                percentage = uiState.utilitiesPercent,
                color = RetailColors.Accent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ExpenseCard(
    label: String,
    amount: Int,
    percentage: Int,
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
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.OnSurfaceLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$amount RON",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.OnSurfaceLight
            )
        }
    }
}

@Composable
fun MarginAnalysisSection(uiState: com.example.retailinventoryapp.viewmodel.BossFinancialUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Analiza Marginilor",
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
            MarginCard(
                label = "Gross Margin",
                value = "${uiState.grossMargin}%",
                status = "Target",
                color = RetailColors.Success,
                modifier = Modifier.weight(1f)
            )

            MarginCard(
                label = "Operating Margin",
                value = "${uiState.operatingMargin}%",
                status = "Good",
                color = RetailColors.Primary,
                modifier = Modifier.weight(1f)
            )

            MarginCard(
                label = "Net Margin",
                value = "${uiState.netMargin}%",
                status = "Good",
                color = RetailColors.Success,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MarginCard(
    label: String,
    value: String,
    status: String,
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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.OnSurfaceLight,
                textAlign = TextAlign.Center
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.OnSurfaceLight
            )
        }
    }
}

@Composable
fun CashFlowSection(uiState: BossFinancialUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Flux de Numerar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(12.dp),
            color = RetailColors.Surface,
            border = BorderStroke(1.dp, RetailColors.BorderLight),
            shadowElevation = 1.dp
        ) {
            // Placeholder for cash flow chart
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Cash Flow Chart\n(Integreaza cu MPAndroidChart)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RetailColors.OnSurfaceLight
                )
            }
        }
    }
}

// ========== DATA CLASSES ==========

data class BossFinancialUiState(
    // Income
    val totalRevenue: Int = 2750000,
    val cogs: Int = 1375000,
    val cogsPercent: Int = 50,
    val grossProfit: Int = 1375000,
    val grossMargin: Int = 50,

    // Expenses
    val expenses: Int = 1095000,
    val expensesPercent: Int = 40,
    val salaries: Int = 550000,
    val salariesPercent: Int = 20,
    val rent: Int = 275000,
    val rentPercent: Int = 10,
    val utilities: Int = 110000,
    val utilitiesPercent: Int = 4,
    val other: Int = 160000,
    val otherPercent: Int = 6,

    // Profit
    val netProfit: Int = 280000,
    val netMargin: Int = 10,
    val operatingMargin: Int = 15,

    val isLoading: Boolean = false
)