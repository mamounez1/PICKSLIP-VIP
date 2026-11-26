package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = NavyBackground,
    primaryContainer = GoldDark,
    onPrimaryContainer = TextPrimary,
    secondary = GoldLight,
    onSecondary = NavyBackground,
    secondaryContainer = NavySurface,
    onSecondaryContainer = TextPrimary,
    tertiary = WinGreen,
    onTertiary = TextPrimary,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavyCard,
    onSurface = TextPrimary,
    surfaceVariant = NavySurface,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    error = LossRed,
    onError = TextPrimary
)

@Composable
fun PickSlipTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // PickSlip strictly embraces the sports-betting dark aesthetic
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = PickSlipTypography,
        content = content
    )
}

// Backward compatibility alias for tests
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PickSlipTheme(content = content)
}
