package com.example.retailinventoryapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.retailinventoryapp.ui.theme.RetailColors

// ========== CONFIRMATION DIALOG ==========

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDangerous: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDangerous) RetailColors.Error else RetailColors.Primary
                )
            ) {
                Text(confirmText, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        containerColor = RetailColors.Surface
    )
}

// ========== SUCCESS DIALOG ==========

@Composable
fun SuccessDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "✅ $title",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        containerColor = RetailColors.Surface
    )
}

// ========== ERROR DIALOG ==========

@Composable
fun ErrorDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "❌ $title",
                fontWeight = FontWeight.Bold,
                color = RetailColors.Error
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetailColors.Error
                )
            ) {
                Text("OK", color = Color.White)
            }
        },
        containerColor = RetailColors.Surface
    )
}

// ========== USAGE ==========

/*
// In any Screen:

var showDialog by remember { mutableStateOf(false) }

if (showDialog) {
    ConfirmationDialog(
        title = "Delete Item?",
        message = "Are you sure you want to delete this item?",
        onConfirm = {
            viewModel.deleteItem()
            showDialog = false
        },
        onDismiss = { showDialog = false },
        isDangerous = true
    )
}

Button(onClick = { showDialog = true }) {
    Text("Delete")
}
*/