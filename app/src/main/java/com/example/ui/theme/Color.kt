package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Liquid Glass & Frosted Acrylic Palette - Dark Mode
val GlassBackgroundDeep = Color(0xFF060913)
val GlassSurfaceDark = Color(0x14FFFFFF)
val GlassSurfaceElevated = Color(0x22FFFFFF)
val GlassSurfaceFrosted = Color(0x18FFFFFF)
val GlassFrostPanelBg = Color(0xD90B132B)
val GlassBorderLight = Color(0x38FFFFFF)
val GlassBorderHighlight = Color(0x7038BDF8)
val GlassSpecularEdge = Color(0xB3FFFFFF)
val GlassSpecularCyan = Color(0x8000E5FF)
val GlassCausticViolet = Color(0x60818CF8)
val GlassFrostInnerGlow = Color(0x26FFFFFF)
val GlassShadowDeep = Color(0x66000000)

// Vibrant Accent Palette (shared / vibrant on both dark and light)
val GlassCyan = Color(0xFF00E5FF)
val GlassAzure = Color(0xFF38BDF8)
val GlassIndigo = Color(0xFF818CF8)
val GlassPurple = Color(0xFFA855F7)
val GlassPink = Color(0xFFEC4899)
val GlassGreen = Color(0xFF10B981)
val GlassAmber = Color(0xFFF59E0B)
val GlassRed = Color(0xFFEF4444)

// Text tokens - Dark Mode
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextMutedDark = Color(0xFF64748B)

// Apple Liquid Glass & Frosted Acrylic Palette - Light Mode
val GlassBackgroundLight = Color(0xFFF1F5F9)
val GlassSurfaceLight = Color(0xEBFFFFFF)
val GlassSurfaceElevatedLight = Color(0xF5FFFFFF)
val GlassSurfaceFrostedLight = Color(0xEBFFFFFF)
val GlassFrostPanelBgLight = Color(0xF2FFFFFF)
val GlassBorderLight_Light = Color(0x99FFFFFF)
val GlassBorderHighlight_Light = Color(0xCC007AFF)
val GlassSpecularEdgeLight = Color(0xFFFFFFFF)
val GlassShadowLight = Color(0x140F172A)

// Text tokens - Light Mode (Crisp Black front color and rich dark slate)
val TextPrimaryLight = Color(0xFF000000) // Deep pure Black for primary text & icons
val TextSecondaryLight = Color(0xFF1E293B) // Dark slate for secondary labels & file sizes
val TextMutedLight = Color(0xFF475569) // Charcoal slate for subtitles & counters

val GlassOverlay = Color(0x99070D1E)
val GlassOverlayLight = Color(0x550F172A)
val GlassCardBackground = Color(0x14FFFFFF)

data class LiquidGlassColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val panelBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val borderLight: Color,
    val borderHighlight: Color,
    val specularEdge: Color,
    val shadowColor: Color,
    val overlayColor: Color,
    val cardBackgroundBrush: Brush,
    val cardBorderBrush: Brush,
    val panelBackgroundBrush: Brush,
    val panelBorderBrush: Brush,
    val dialogBackgroundBrush: Brush,
    val dialogBorderBrush: Brush
)

val DarkLiquidGlassColors = LiquidGlassColors(
    isDark = true,
    background = GlassBackgroundDeep,
    surface = Color(0x1A1E293B),
    surfaceElevated = Color(0x22FFFFFF),
    panelBackground = GlassFrostPanelBg,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textMuted = TextMutedDark,
    primaryAccent = GlassCyan,
    secondaryAccent = GlassAzure,
    borderLight = GlassBorderLight,
    borderHighlight = GlassBorderHighlight,
    specularEdge = GlassSpecularEdge,
    shadowColor = GlassShadowDeep,
    overlayColor = GlassOverlay,
    cardBackgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.16f),
            Color(0xFF1E293B).copy(alpha = 0.40f),
            Color(0xFF0F172A).copy(alpha = 0.60f),
            Color(0xFF070E1E).copy(alpha = 0.75f)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    ),
    cardBorderBrush = Brush.linearGradient(
        colors = listOf(
            GlassSpecularEdge,
            GlassSpecularCyan,
            Color.White.copy(alpha = 0.20f),
            GlassCausticViolet.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0.08f)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    ),
    panelBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF182647).copy(alpha = 0.85f),
            Color(0xFF0D162C).copy(alpha = 0.90f)
        )
    ),
    panelBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.55f),
            GlassCyan.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.12f)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    ),
    dialogBackgroundBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF14203D).copy(alpha = 0.94f),
            Color(0xFF0A1020).copy(alpha = 0.96f)
        )
    ),
    dialogBorderBrush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.60f),
            GlassCyan.copy(alpha = 0.40f),
            GlassCausticViolet.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0.10f)
        )
    )
)

val LightLiquidGlassColors = LiquidGlassColors(
    isDark = false,
    background = GlassBackgroundLight,
    surface = GlassSurfaceLight,
    surfaceElevated = GlassSurfaceElevatedLight,
    panelBackground = GlassFrostPanelBgLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textMuted = TextMutedLight,
    primaryAccent = Color(0xFF007AFF), // Iconic Apple Blue
    secondaryAccent = Color(0xFF5856D6), // Apple Purple
    borderLight = GlassBorderLight_Light,
    borderHighlight = GlassBorderHighlight_Light,
    specularEdge = GlassSpecularEdgeLight,
    shadowColor = GlassShadowLight,
    overlayColor = GlassOverlayLight,
    cardBackgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.90f),
            Color.White.copy(alpha = 0.78f),
            Color(0xFFF8FAFC).copy(alpha = 0.82f),
            Color.White.copy(alpha = 0.88f)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    ),
    cardBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White,
            Color(0xFF007AFF).copy(alpha = 0.30f),
            Color.White.copy(alpha = 0.90f),
            Color(0x180F172A)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    ),
    panelBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.92f),
            Color(0xFFF8FAFC).copy(alpha = 0.96f)
        )
    ),
    panelBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White,
            Color(0xFF007AFF).copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.85f),
            Color(0x200F172A)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    ),
    dialogBackgroundBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.96f),
            Color(0xFFF8FAFC).copy(alpha = 0.98f)
        )
    ),
    dialogBorderBrush = Brush.linearGradient(
        listOf(
            Color.White,
            Color(0xFF007AFF).copy(alpha = 0.40f),
            Color.White.copy(alpha = 0.80f),
            Color(0x250F172A)
        )
    )
)

val LocalLiquidGlassColors = staticCompositionLocalOf { DarkLiquidGlassColors }

object LiquidGlassTheme {
    val colors: LiquidGlassColors
        @Composable
        get() = LocalLiquidGlassColors.current
}


