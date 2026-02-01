package com.example.retailinventoryapp.ui.screens.casier


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.example.retailinventoryapp.viewmodel.CasierCheckoutViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions




@Composable
fun CasierCheckoutScreen(
    cartTotal: Double,
    viewModel: CasierCheckoutViewModel = hiltViewModel(),
    onPaymentSuccess: (receiptNumber: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var amountPaid by remember { mutableStateOf("") }

    if (uiState.isProcessing) {
        ProcessingDialog()
    }

    if (uiState.paymentSuccess) {
        LaunchedEffect(Unit) {
            onPaymentSuccess(uiState.receiptNumber ?: "REC-${System.currentTimeMillis()}")
        }
    }

    Scaffold(
        topBar = {
            CheckoutTopBar(onBackClick = onNavigateBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(RetailColors.Background)
                .verticalScroll(rememberScrollState())
        ) {
            // Order Summary
            OrderSummarySection(
                total = cartTotal,
                itemCount = 1  // TODO: Pass from previous screen
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Methods
            PaymentMethodsSection(
                selectedMethod = selectedPaymentMethod,
                onMethodSelect = { selectedPaymentMethod = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Details
            if (selectedPaymentMethod == PaymentMethod.CASH) {
                CashPaymentSection(
                    total = cartTotal,
                    amountPaid = amountPaid,
                    onAmountChange = { amountPaid = it }
                )
            } else if (selectedPaymentMethod == PaymentMethod.CARD) {
                CardPaymentSection()
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Process Button
            if (selectedPaymentMethod != null) {
                PaymentButton(
                    paymentMethod = selectedPaymentMethod!!,
                    total = cartTotal,
                    onClick = {
                        viewModel.selectPaymentMethod(selectedPaymentMethod!!.name)
                        viewModel.processSale()
                    },
                    enabled = !uiState.isProcessing,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }

            if (uiState.error != null) {
                ErrorBanner(message = uiState.error!!)
            }
        }
    }
}

@Composable
fun CheckoutTopBar(onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
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
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Checkout",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = Color.White
            )
        }
    }
}

@Composable
fun OrderSummarySection(total: Double, itemCount: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = RetailColors.Primary.copy(alpha = 0.1f),
        border = BorderStroke(1.5.dp, RetailColors.Primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Rezumat Comanda",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Numarul de articole:", style = MaterialTheme.typography.bodyMedium)
                Text("$itemCount", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = RetailColors.BorderLight)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total de Platit:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetailColors.Primary
                )
                Text(
                    "$total RON",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RetailColors.Primary
                )
            }
        }
    }
}

@Composable
fun PaymentMethodsSection(
    selectedMethod: PaymentMethod?,
    onMethodSelect: (PaymentMethod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Metoda de Plată",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PaymentMethodCard(
                method = PaymentMethod.CASH,
                icon = "💵",
                label = "Numerar",
                isSelected = selectedMethod == PaymentMethod.CASH,
                onClick = { onMethodSelect(PaymentMethod.CASH) },
                modifier = Modifier.weight(1f)
            )

            PaymentMethodCard(
                method = PaymentMethod.CARD,
                icon = "💳",
                label = "Card",
                isSelected = selectedMethod == PaymentMethod.CARD,
                onClick = { onMethodSelect(PaymentMethod.CARD) },
                modifier = Modifier.weight(1f)
            )

            PaymentMethodCard(
                method = PaymentMethod.CHECK,
                icon = "📋",
                label = "Cec",
                isSelected = selectedMethod == PaymentMethod.CHECK,
                onClick = { onMethodSelect(PaymentMethod.CHECK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) RetailColors.Primary.copy(alpha = 0.15f) else RetailColors.Surface,
        border = BorderStroke(
            2.dp,
            if (isSelected) RetailColors.Primary else RetailColors.BorderLight
        ),
        shadowElevation = if (isSelected) 2.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) RetailColors.Primary else RetailColors.OnSurface
            )
        }
    }
}

@Composable
fun CashPaymentSection(
    total: Double,
    amountPaid: String,
    onAmountChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Detalii Plată cu Numerar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amountPaid,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            label = { Text("Suma incasata") },
            prefix = { Text("RON ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RetailColors.Primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        val amountValue = amountPaid.toIntOrNull() ?: 0
        val change = amountValue - total

        if (change >= 0) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                color = RetailColors.Success.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, RetailColors.Success.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Rest de incasat:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "$change RON",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RetailColors.Success
                    )
                }
            }
        } else if (amountValue > 0) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                color = RetailColors.Error.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, RetailColors.Error.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Suma insuficienta:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RetailColors.Error
                    )
                    Text(
                        "${-change} RON",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RetailColors.Error
                    )
                }
            }
        }
    }
}

@Composable
fun CardPaymentSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Detalii Plată cu Card",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
            color = RetailColors.Primary.copy(alpha = 0.05f),
            border = BorderStroke(2.dp, RetailColors.Primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = "Card",
                    tint = RetailColors.Primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Citeste cardul...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Apropie cardul de cititor",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
            }
        }
    }
}

@Composable
fun PaymentButton(
    paymentMethod: PaymentMethod,
    total: Double,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RetailColors.Success
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Check, contentDescription = "Pay", tint = Color.White)
            Text(
                text = "CONFIRMA PLATA - $total RON",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProcessingDialog() {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = RetailColors.Surface,
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = RetailColors.Primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Se proceseaza plata...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ========== ENUMS & DATA CLASSES ==========

enum class PaymentMethod {
    CASH, CARD, CHECK
}

