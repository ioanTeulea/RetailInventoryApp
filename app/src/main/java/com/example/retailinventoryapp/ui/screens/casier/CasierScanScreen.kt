package com.example.retailinventoryapp.ui.screens.casier


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.retailinventoryapp.ui.navigation.Destinations
import com.example.retailinventoryapp.ui.theme.RetailColors
import com.example.retailinventoryapp.viewmodel.CartItem
import com.example.retailinventoryapp.viewmodel.CasierScanViewModel

@Composable
fun CasierScanScreen(
    viewModel: CasierScanViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
    onNavigateToCheckout: (Double) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var barcodInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ScanTopBar(
                cartCount = uiState.cartItems.size,
                onBackClick = onNavigateBack,
                totalAmount = uiState.totalAmount
            )
        },
        bottomBar = {
            Column {
                ScanBottomBar(
                    itemCount = uiState.cartItems.size,
                    total = uiState.totalAmount,
                    onCheckoutClick = { onNavigateToCheckout(uiState.totalAmount) },
                    enabled = uiState.cartItems.isNotEmpty()
                )

                CasierBottomBar(
                    currentRoute = Destinations.CasierScan.route,
                    onNavigate = onNavigate
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(RetailColors.Background)
        ) {
            // Search/Barcode Input
            ScanInputSection(
                barcodeInput = barcodInput,
                onBarcodeChange = { newValue ->
                    barcodInput = newValue
                    if (newValue.length >= 8) {  // EAN minimum
                        viewModel.scanProduct(newValue)
                        barcodInput = ""
                    }
                },
                onSearchClick = {
                    viewModel.scanProduct(barcodInput)
                    barcodInput = ""
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RetailColors.Primary)
                }
            } else if (uiState.cartItems.isEmpty()) {
                EmptyCartMessage()
            } else {
                // Cart Items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(uiState.cartItems) { item ->
                        CartItemCard(
                            item = item,
                            onQuantityChange = { newQty ->
                                viewModel.updateQuantity(item.productId, newQty)
                            },
                            onRemove = {
                                viewModel.removeItem(item.productId)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Summary
                CartSummarySection(uiState.totalAmount)
            }

            if (uiState.error != null) {
                ErrorBanner(message = uiState.error!!)
            }
        }
    }
}

@Composable
fun ScanTopBar(
    cartCount: Int,
    onBackClick: () -> Unit,
    totalAmount: Double
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        color = RetailColors.Primary,
        shadowElevation = 4.dp
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
                        text = "Scan Produse",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$cartCount articole",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Badge(
                containerColor = RetailColors.Accent,
                contentColor = Color.White
            ) {
                Text(
                    text = "$totalAmount RON",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ScanInputSection(
    barcodeInput: String,
    onBarcodeChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = barcodeInput,
                onValueChange = onBarcodeChange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                placeholder = {
                    Text("Scaneaza cod bare...", fontSize = 12.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.QrCode2, contentDescription = "Scan")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RetailColors.Primary,
                    unfocusedBorderColor = RetailColors.BorderLight
                )
            )

            Button(
                onClick = onSearchClick,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetailColors.Primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
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
            // Product Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = item.barcode,
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.unitPrice} RON",
                    style = MaterialTheme.typography.titleSmall,
                    color = RetailColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quantity Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                IconButton(
                    onClick = { if (item.quantity > 1) onQuantityChange(item.quantity - 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = RetailColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RetailColors.Primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${item.quantity}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { if (item.quantity < 999) onQuantityChange(item.quantity + 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = RetailColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = RetailColors.Error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CartSummarySection(totalAmount: Double) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = RetailColors.Surface,
        border = BorderStroke(1.5.dp, RetailColors.Primary.copy(alpha = 0.2f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total de plată:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RetailColors.Primary
            )

            Text(
                // ✅ Acum formatarea va funcționa corect
                text = String.format("%.2f RON", totalAmount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RetailColors.Primary
            )
        }
    }
}

@Composable
fun ScanBottomBar(
    itemCount: Int,
    total: Double,
    onCheckoutClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = RetailColors.Surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$itemCount articole",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
                Text(
                    text = "$total RON",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RetailColors.Primary
                )
            }

            Button(
                onClick = onCheckoutClick,
                modifier = Modifier
                    .height(48.dp)
                    .width(150.dp),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetailColors.Success
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "💳 CHECKOUT",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyCartMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🛒",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Coșul este gol",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Scaneaza coduri de bare pentru a adauga produse",
            style = MaterialTheme.typography.bodyMedium,
            color = RetailColors.OnSurfaceLight
        )
    }
}

@Composable
fun ErrorBanner(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = RetailColors.Error.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, RetailColors.Error.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Error",
                tint = RetailColors.Error,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = RetailColors.Error,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ========== DATA CLASSES ==========
