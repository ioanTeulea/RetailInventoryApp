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
import com.example.retailinventoryapp.ui.theme.RetailColors
import com.example.retailinventoryapp.viewmodel.BossStoreDetailUiState
import com.example.retailinventoryapp.viewmodel.BossStoreDetailViewModel
import com.example.retailinventoryapp.viewmodel.CasierPerformanceUiModel

@Composable
fun BossStoreDetailScreen(
    storeId: Long,
    viewModel: BossStoreDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToFinancial: (storeId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(storeId) {
        viewModel.loadStoreDetail(storeId)
    }

    Scaffold(
        topBar = {
            StoreDetailTopBar(
                storeName = uiState.storeName,
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

            // Store Header
            item {
                StoreHeaderSection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // KPI Cards
            item {
                StoreKPICards(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Manager Info
            item {
                ManagerInfoSection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Team Performance
            item {
                TeamPerformanceSection(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Actions
            item {
                ActionsSection(
                    onFinancialClick = { onNavigateToFinancial(storeId) }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StoreDetailTopBar(
    storeName: String,
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

                Column {
                    Text(
                        text = storeName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Detalii Magazin",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Icon(
                Icons.Default.Store,
                contentDescription = "Store",
                tint = Color.White
            )
        }
    }
}

@Composable
fun StoreHeaderSection(uiState: com.example.retailinventoryapp.viewmodel.BossStoreDetailUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            color = RetailColors.Primary.copy(alpha = 0.1f),
            border = BorderStroke(1.5.dp, RetailColors.Primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Locatie",
                            style = MaterialTheme.typography.labelSmall,
                            color = RetailColors.OnSurfaceLight
                        )
                        Text(
                            uiState.address,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Tel",
                            style = MaterialTheme.typography.labelSmall,
                            color = RetailColors.OnSurfaceLight
                        )
                        Text(
                            uiState.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusBadge(
                        label = "Status",
                        value = "Activ",
                        color = RetailColors.Success
                    )

                    StatusBadge(
                        label = "Ore",
                        value = uiState.hours,
                        color = RetailColors.Primary
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.OnSurfaceLight,
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun StoreKPICards(uiState: BossStoreDetailUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Performance",
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
            StoreKPICard(
                title = "Revenue",
                value = "${uiState.monthlyRevenue} RON",
                trend = "+${uiState.revenueGrowth}%",
                color = RetailColors.Success,
                modifier = Modifier.weight(1f)
            )

            StoreKPICard(
                title = "Marja",
                value = "${uiState.profitMargin}%",
                trend = "Target",
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
            StoreKPICard(
                title = "Tranzactii",
                value = "${uiState.monthlyTransactions}",
                trend = "Actuale",
                color = RetailColors.Warning,
                modifier = Modifier.weight(1f)
            )

            StoreKPICard(
                title = "Rating",
                value = "${uiState.storeRating}",
                trend = "/5",
                color = RetailColors.Accent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StoreKPICard(
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
fun ManagerInfoSection(uiState: BossStoreDetailUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Manager",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
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
                        text = uiState.managerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = uiState.managerEmail,
                        style = MaterialTheme.typography.labelSmall,
                        color = RetailColors.OnSurfaceLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tenure: ${uiState.managerTenure}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RetailColors.OnSurfaceLight
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "⭐${uiState.managerRating}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RetailColors.Warning
                    )
                }
            }
        }
    }
}

@Composable
fun TeamPerformanceSection(uiState: BossStoreDetailUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Top Casieri",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        uiState.topCasiers.forEach { casier ->
            CasierPerformanceCard(casier)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CasierPerformanceCard(casier: CasierPerformanceUiModel) {
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
                    text = casier.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${casier.sales} RON • ${casier.transactions} tranzactii",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { index ->
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Star",
                        tint = if (index < casier.rating.toInt()) RetailColors.Warning else RetailColors.BorderLight,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = "${casier.rating}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActionsSection(onFinancialClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Actiuni",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onFinancialClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RetailColors.Primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.MoreHoriz, contentDescription = "Financial", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Vezi Detalii Financiare", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
