package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = AppAccent,
    onPrimary = Color.White,
    secondaryContainer = AppIndicator,
    onSecondaryContainer = Color(0xFF1B2E0A),
    background = AppBackground,
    onBackground = Color(0xFF1B1C18),
    surface = AppBackground,
    onSurface = Color(0xFF1B1C18),
    onSurfaceVariant = AppGray,
    surfaceContainer = AppBar,
    surfaceContainerHighest = AppTile
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}