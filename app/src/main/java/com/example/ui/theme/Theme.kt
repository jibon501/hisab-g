package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HisabColorScheme = lightColorScheme(
    primary = Emerald600,
    onPrimary = Color.White,
    primaryContainer = Emerald50,
    onPrimaryContainer = Emerald800,
    secondary = Blue600,
    onSecondary = Color.White,
    secondaryContainer = Blue50,
    onSecondaryContainer = Blue700,
    tertiary = Amber600,
    onTertiary = Color.White,
    tertiaryContainer = Amber50,
    onTertiaryContainer = Amber900,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    outlineVariant = Slate300,
    error = Red600,
    onError = Color.White,
    errorContainer = Red50,
    onErrorContainer = Red700
)

@Composable
fun HisabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HisabColorScheme,
        typography = HisabTypography,
        content = content
    )
}
