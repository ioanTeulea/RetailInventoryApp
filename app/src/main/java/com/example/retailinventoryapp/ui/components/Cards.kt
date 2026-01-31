package com.example.retailinventoryapp.ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.retailinventoryapp.ui.theme.RetailColors

// ========== STAT CARD ==========

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        color = RetailColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, RetailColors.BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = RetailColors.OnSurfaceLight
                )
                Icon(
                    icon,
                    contentDescription = title,
                    tint = RetailColors.Primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RetailColors.OnSurface
            )
        }
    }
}

// ========== KPI CARD ==========

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
                    if (trendPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
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

// ========== INFO CARD ==========

@Composable
fun InfoCard(
    title: String,
    value: String,
    description: String,
    modifier: Modifier = Modifier,
    color: Color = RetailColors.Primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = RetailColors.OnSurfaceLight
            )
        }
    }
}