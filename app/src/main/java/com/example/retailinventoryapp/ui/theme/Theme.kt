package com.example.retailinventoryapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = RetailColors.Primary,
    onPrimary = Color.White,
    primaryContainer = RetailColors.PrimaryLight,
    onPrimaryContainer = RetailColors.PrimaryDark,

    secondary = RetailColors.Primary,
    onSecondary = Color.White,

    error = RetailColors.Error,
    onError = Color.White,

    background = RetailColors.Background,
    onBackground = RetailColors.OnSurface,

    surface = RetailColors.Surface,
    onSurface = RetailColors.OnSurface
)

@Composable
fun RetailAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = RetailTypography,
        content = content
    )
}