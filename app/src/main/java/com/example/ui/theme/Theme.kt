package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LiquidGlassDarkColorScheme = darkColorScheme(
    primary = GlassCyan,
    onPrimary = Color(0xFF041E2B),
    primaryContainer = Color(0xFF083344),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = GlassAzure,
    onSecondary = Color(0xFF0C243C),
    secondaryContainer = Color(0xFF072740),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = GlassIndigo,
    onTertiary = Color(0xFF1E1B4B),
    background = GlassBackgroundDeep,
    onBackground = TextPrimaryDark,
    surface = Color(0x1A1E293B),
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0x22334155),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0x40FFFFFF),
    outlineVariant = Color(0x20FFFFFF),
    error = GlassRed,
    onError = Color.White
)

private val LiquidGlassLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF0EA5E9),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0F9FF),
    onSecondaryContainer = Color(0xFF0284C7),
    tertiary = GlassIndigo,
    onTertiary = Color.White,
    background = GlassBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0x33CBD5E1),
    outlineVariant = Color(0x1ACBD5E1),
    error = GlassRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LiquidGlassDarkColorScheme else LiquidGlassLightColorScheme
    val liquidGlassColors = if (darkTheme) DarkLiquidGlassColors else LightLiquidGlassColors

    CompositionLocalProvider(LocalLiquidGlassColors provides liquidGlassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

