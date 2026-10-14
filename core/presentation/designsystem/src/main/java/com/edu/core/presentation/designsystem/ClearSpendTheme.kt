package com.edu.core.presentation.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = Surface,
    secondary = Mint,
    onSecondary = Navy,
    tertiary = Mint,
    onTertiary = Navy,
    error = Error,
    onError = Surface,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue,
    onPrimary = OnNavy,
    secondary = Mint,
    onSecondary = Navy,
    tertiary = Mint,
    onTertiary = Navy,
    error = Error,
    onError = OnNavy,
    background = Navy,
    onBackground = OnNavy,
    surface = Navy2,
    onSurface = OnNavy,
    surfaceVariant = Navy2,
    onSurfaceVariant = OnNavyMuted,
    outline = Navy2,
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
