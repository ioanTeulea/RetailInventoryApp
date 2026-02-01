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
import com.example.retailinventoryapp.viewmodel.ManagerReportsUiState
import com.example.retailinventoryapp.viewmodel.ManagerReportsViewModel

@Composable
fun ManagerReportsScreen(
    viewModel: ManagerReportsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.TODAY) }

    LaunchedEffect(selectedPeriod) {
        viewModel.loadReportData(selectedPeriod)
    }

    Scaffold(
        topBar = {
            ReportsTopBar(onBackClick = onNavigateBack)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(RetailColors.Background)
        ) {
            item {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = { selectedPeriod = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                ReportSummaryCards(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                RevenueChartSection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                TopProductsSection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                PaymentMethodsSection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                ExportButtonSection(
                    onPdfClick = { viewModel.exportToPdf(selectedPeriod.name) },
                    onCsvClick = { viewModel.exportToCsv(selectedPeriod.name) }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ReportsTopBar(onBackClick: () -> Unit) {
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
                    text = "Rapoarte",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: ReportPeriod,
    onPeriodChange: (ReportPeriod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Perioada",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportPeriod.values().forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodChange(period) },
                    label = { Text(period.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RetailColors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun ReportSummaryCards(uiState: ManagerReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Sumar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Venituri",
                value = "${uiState.totalRevenue} RON",
                trend = "+${uiState.revenueGrowth}%",
                color = RetailColors.Success,
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Tranzactii",
                value = "${uiState.totalTransactions}",
                trend = "+${uiState.transactionGrowth}%",
                color = RetailColors.Primary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Avg/Vânzare",
                value = "${uiState.avgTransaction} RON",
                trend = "Normal",
                color = RetailColors.Warning,
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Marje",
                value = "${uiState.profitMargin}%",
                trend = "Target",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    trend: String,
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.OnSurfaceLight
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = trend,
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.OnSurfaceLight
            )
        }
    }
}

@Composable
fun RevenueChartSection(uiState: ManagerReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Venituri pe Zile",
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
            // Placeholder for chart (TODO: Use real charting library)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Chart Placeholder\n(Integreaza cu MPAndroidChart)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RetailColors.OnSurfaceLight
                )
            }
        }
    }
}

@Composable
fun TopProductsSection(uiState: ManagerReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Top Produse",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        uiState.topProducts.forEachIndexed { index, product ->
            TopProductCard(
                rank = index + 1,
                name = product.name,
                quantity = product.quantity,
                revenue = product.revenue
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TopProductCard(
    rank: Int,
    name: String,
    quantity: Int,
    revenue: Int
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = RetailColors.Primary.copy(alpha = 0.2f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "$rank",
                            fontWeight = FontWeight.Bold,
                            color = RetailColors.Primary
                        )
                    }
                }

                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "$quantity bucati",
                        style = MaterialTheme.typography.labelSmall,
                        color = RetailColors.OnSurfaceLight
                    )
                }
            }

            Text(
                text = "$revenue RON",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = RetailColors.Primary
            )
        }
    }
}

@Composable
fun PaymentMethodsSection(uiState: ManagerReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Metode de Plată",
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
            PaymentMethodCard(
                icon = "💵",
                method = "Numerar",
                amount = uiState.cashPayments,
                modifier = Modifier.weight(1f)
            )

            PaymentMethodCard(
                icon = "💳",
                method = "Card",
                amount = uiState.cardPayments,
                modifier = Modifier.weight(1f)
            )

            PaymentMethodCard(
                icon = "📋",
                method = "Cec",
                amount = uiState.checkPayments,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PaymentMethodCard(
    icon: String,
    method: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = RetailColors.Surface,
        border = BorderStroke(1.dp, RetailColors.BorderLight),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = method,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$amount RON",
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.Primary
            )
        }
    }
}

@Composable
fun ExportButtonSection(
    onPdfClick: () -> Unit,
    onCsvClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Exporta Raport",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onPdfClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetailColors.Error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = "PDF")
                Spacer(modifier = Modifier.width(8.dp))
                Text("PDF", color = Color.White)
            }

            Button(
                onClick = onCsvClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetailColors.Success
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = "CSV")
                Spacer(modifier = Modifier.width(8.dp))
                Text("CSV", color = Color.White)
            }
        }
    }
}

// ========== ENUMS & DATA CLASSES ==========

enum class ReportPeriod(val label: String) {
    TODAY("Astazi"),
    THIS_WEEK("Saptamana"),
    THIS_MONTH("Luna"),
    THIS_YEAR("An")
}
