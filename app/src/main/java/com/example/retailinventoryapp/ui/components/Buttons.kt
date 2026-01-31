package com.example.retailinventoryapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.retailinventoryapp.ui.theme.RetailColors

// ========== PRIMARY BUTTON ==========

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RetailColors.Primary,
            disabledContainerColor = RetailColors.Primary.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// ========== SECONDARY BUTTON ==========

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        border = BorderStroke(
            2.dp,
            if (enabled) RetailColors.Primary else RetailColors.Primary.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    ) {
        Text(
            text = text,
            color = if (enabled) RetailColors.Primary else RetailColors.Primary.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// ========== DANGER BUTTON ==========

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RetailColors.Error
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

// ========== SUCCESS BUTTON ==========

@Composable
fun SuccessButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RetailColors.Success
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

// ========== USAGE EXAMPLES ==========

/*
// In any Screen:

PrimaryButton(
    text = "Save",
    onClick = { viewModel.saveData() }
)

SecondaryButton(
    text = "Cancel",
    onClick = { navController.popBackStack() }
)

DangerButton(
    text = "Delete",
    onClick = { viewModel.deleteItem() }
)
*/