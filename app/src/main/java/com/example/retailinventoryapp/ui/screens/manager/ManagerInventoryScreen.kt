package com.example.retailinventoryapp.ui.screens.manager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.retailinventoryapp.viewmodel.ManagerInventoryUiState
import com.example.retailinventoryapp.viewmodel.ManagerInventoryViewModel
import com.example.retailinventoryapp.viewmodel.ProductInventoryUiModel

@Composable
fun ManagerInventoryScreen(
    viewModel: ManagerInventoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf(FilterType.ALL) }

    Scaffold(
        topBar = {
            InventoryTopBar(onBackClick = onNavigateBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(RetailColors.Background)
        ) {
            // Search & Filter
            SearchFilterSection(
                searchQuery = searchQuery,
                onSearchChange = {
                    searchQuery = it
                    viewModel.searchProducts(it)
                },
                selectedFilter = filterType,
                onFilterChange = {
                    filterType = it
                    viewModel.filter(it)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Cards
            InventoryStatsCards(uiState)

            Spacer(modifier = Modifier.height(16.dp))

            // Products List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RetailColors.Primary)
                }
            } else if (uiState.products.isEmpty()) {
                EmptyProductsList()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(uiState.products) { product ->
                        ProductInventoryCard(
                            product = product,
                            onEditClick = { /* TODO: Edit product */ },
                            onRestockClick = { viewModel.openRestockDialog(product.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Restock Dialog
    if (uiState.showRestockDialog && uiState.selectedProduct != null) {
        RestockDialog(
            product = uiState.selectedProduct!!,
            onConfirm = { quantity ->
                viewModel.updateStock(uiState.selectedProduct!!.id, quantity)
            },
            onDismiss = { viewModel.closeRestockDialog() }
        )
    }
}

@Composable
fun InventoryTopBar(onBackClick: () -> Unit) {
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
                    text = "Inventar",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { /* TODO: Add product */ }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Product",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SearchFilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: FilterType,
    onFilterChange: (FilterType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            placeholder = { Text("Cautati produs...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RetailColors.Primary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterType.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter.label) },
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
fun InventoryStatsCards(uiState: ManagerInventoryUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsCard(
            title = "Total",
            value = "${uiState.totalProducts}",
            color = RetailColors.Primary,
            modifier = Modifier.weight(1f)
        )

        StatsCard(
            title = "⚠️ Scăzut",
            value = "${uiState.lowStockCount}",
            color = RetailColors.Warning,
            modifier = Modifier.weight(1f)
        )

        StatsCard(
            title = "🔴 Epuizat",
            value = "${uiState.outOfStockCount}",
            color = RetailColors.Error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(80.dp),
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
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.OnSurfaceLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ProductInventoryCard(
    product: ProductInventoryUiModel,
    onEditClick: () -> Unit,
    onRestockClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        color = RetailColors.Surface,
        border = BorderStroke(
            1.dp,
            if (product.isLowStock) RetailColors.Warning else RetailColors.BorderLight
        ),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Product Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = product.sku,
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            "Stoc:",
                            style = MaterialTheme.typography.labelSmall,
                            color = RetailColors.OnSurfaceLight
                        )
                        Text(
                            "${product.currentStock}",
                            fontWeight = FontWeight.Bold,
                            color = if (product.isLowStock) RetailColors.Warning else RetailColors.Primary
                        )
                    }

                    Column {
                        Text(
                            "Pret:",
                            style = MaterialTheme.typography.labelSmall,
                            color = RetailColors.OnSurfaceLight
                        )
                        Text(
                            "${product.price} RON",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Actions
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = RetailColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onRestockClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.AddBox,
                        contentDescription = "Restock",
                        tint = RetailColors.Success,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyProductsList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📦", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Niciun produs gasit",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RestockDialog(
    product: ProductInventoryUiModel,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reincarca Stoc - ${product.name}") },
        text = {
            Column {
                Text("Cantitate actuala: ${product.currentStock}")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantitate adaugata") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    quantity.toIntOrNull()?.let {
                        onConfirm(it)
                        onDismiss()
                    }
                }
            ) {
                Text("Confirma")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Anuleaza")
            }
        }
    )
}

// ========== ENUMS & DATA CLASSES ==========

enum class FilterType(val label: String) {
    ALL("Toti"),
    LOW_STOCK("⚠️ Scăzut"),
    OUT_OF_STOCK("🔴 Epuizat"),
    HIGH_STOCK("✅ Plin")
}
