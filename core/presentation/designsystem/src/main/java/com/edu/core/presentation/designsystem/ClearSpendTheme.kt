package com.edu.core.presentation.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = SurfaceWhite,
    secondary = MintGreen,
    onSecondary = NavyBackground,
    tertiary = MintGreen,
    onTertiary = NavyBackground,
    error = ErrorRed,
    onError = SurfaceWhite,
    background = SurfaceWhite,
    onBackground = NavyBackground,
    surface = SurfaceWhite,
    onSurface = NavyBackground,
    surfaceVariant = SurfaceWhite,
    onSurfaceVariant = TextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextPrimary,
    secondary = MintGreen,
    onSecondary = NavyBackground,
    tertiary = MintGreen,
    onTertiary = NavyBackground,
    error = ErrorRed,
    onError = TextPrimary,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = CardNavy,
    onSurface = TextPrimary,
    surfaceVariant = CardNavy,
    onSurfaceVariant = TextSecondary
)

@Composable
fun ClearSpendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ClearSpendTypography,
        content = content
    )
}
