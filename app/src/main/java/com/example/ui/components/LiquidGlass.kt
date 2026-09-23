package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GlassAzure
import com.example.ui.theme.GlassBackgroundDeep
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassCausticViolet
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassFrostInnerGlow
import com.example.ui.theme.GlassFrostPanelBg
import com.example.ui.theme.GlassIndigo
import com.example.ui.theme.LiquidGlassTheme
import com.example.ui.theme.GlassPurple
import com.example.ui.theme.GlassShadowDeep
import com.example.ui.theme.GlassSpecularCyan
import com.example.ui.theme.GlassSpecularEdge
import com.example.ui.theme.GlassSurfaceFrosted

/**
 * Ultra-smooth, high-performance Liquid Glass Background.
 * Performance Optimization: The floating liquid caustics read animation states
 * strictly inside the Draw phase (drawBehind), causing ZERO recompositions of the
 * view hierarchy during animation, preserving 60/120fps scrolling.
 */
@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_anim")
    
    // Orb motion 1 (Cyan/Azure Caustic)
    val orb1Offset = infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1_offset"
    )

    // Orb motion 2 (Violet/Indigo Caustic)
    val orb2Offset = infiniteTransition.animateFloat(
        initialValue = 35f,
        targetValue = -35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2_offset"
    )

    // Subtle caustic wave shimmer
    val causticPhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "caustic_phase"
    )

    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Apple Liquid Glass dynamic background wallpaper
        Image(
            painter = painterResource(id = if (isDark) R.drawable.img_wallpaper_dark else R.drawable.img_wallpaper_light),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Frosted acrylic diffusion layer with fluid caustic highlights
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xAA050914),
                                Color(0xB8091124),
                                Color(0xCC040710)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.20f),
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.22f)
                            )
                        )
                    }
                )
                .drawBehind {
                    val w = size.width
                    val h = size.height

                    val o1 = orb1Offset.value
                    val orb1Colors = if (isDark) {
                        listOf(
                            GlassCyan.copy(alpha = 0.22f),
                            GlassAzure.copy(alpha = 0.09f),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            Color(0xFF38BDF8).copy(alpha = 0.20f),
                            Color(0xFF007AFF).copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = orb1Colors,
                            center = Offset(w * 0.85f + o1, h * 0.12f + o1 * 0.5f),
                            radius = w * 0.85f
                        )
                    )

                    val o2 = orb2Offset.value
                    val orb2Colors = if (isDark) {
                        listOf(
                            GlassPurple.copy(alpha = 0.18f),
                            GlassIndigo.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            Color(0xFFA855F7).copy(alpha = 0.15f),
                            Color(0xFFEC4899).copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = orb2Colors,
                            center = Offset(w * 0.12f + o2, h * 0.82f - o2 * 0.4f),
                            radius = w * 0.88f
                        )
                    )

                    val cPhase = causticPhase.value
                    val centerOffset = (cPhase - 0.5f) * 60f
                    val ambientColors = if (isDark) {
                        listOf(
                            GlassCyan.copy(alpha = 0.06f),
                            GlassCausticViolet.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.40f),
                            Color(0xFF007AFF).copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = ambientColors,
                            center = Offset(w * 0.5f + centerOffset, h * 0.48f),
                            radius = w * 0.60f
                        )
                    )
                }
        )

        content()
    }
}

/**
 * Authentic Frosted & Liquid Glass Card with physical refraction bevels,
 * top-left specular highlights, subtle diffuse glare, and crisp tactile borders.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = colors.primaryAccent.copy(alpha = 0.35f)),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.cardBackgroundBrush)
            .drawBehind {
                // Internal frosted specular sheen (curved refraction on top half)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isDark) GlassFrostInnerGlow else Color.White.copy(alpha = 0.60f),
                            if (isDark) Color.White.copy(alpha = 0.02f) else Color.White.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.45f
                    )
                )

                // Top specular hairline highlight (Apple physical refraction edge)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (isDark) Color.White.copy(alpha = 0.70f) else Color.White,
                            if (isDark) colors.primaryAccent.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.90f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(size.width * 0.05f, 1f),
                    end = Offset(size.width * 0.95f, 1f),
                    strokeWidth = 1.5f
                )
            }
            .border(BorderStroke(borderWidth, colors.cardBorderBrush), shape)
            .then(clickModifier),
        content = content
    )
}

/**
 * Frosted Glass Panel designed for Top Bars, Navigation Bars, and Floating Bars.
 * Provides authentic acrylic frosting with top specular rim and deep translucent glass substrate.
 */
@Composable
fun FrostedGlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.panelBackgroundBrush)
            .drawBehind {
                // Top frosted rim light (Apple glass highlight)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (isDark) Color.White.copy(alpha = 0.75f) else Color.White,
                            if (isDark) colors.secondaryAccent.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.95f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(size.width * 0.04f, 1f),
                    end = Offset(size.width * 0.96f, 1f),
                    strokeWidth = 1.8f
                )
            }
            .border(BorderStroke(borderWidth, colors.panelBorderBrush), shape),
        content = content
    )
}

/**
 * Liquid Glass Surface for dialogs and modal sheets.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LiquidGlassTheme.colors

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.dialogBackgroundBrush)
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (colors.isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.65f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.35f
                    )
                )
            }
            .border(BorderStroke(1.2.dp, colors.dialogBorderBrush), shape),
        content = content
    )
}

/**
 * Luminous Liquid Glass Pill for category filters and interactive tabs.
 */
@Composable
fun LiquidGlassPill(
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pill_press_scale"
    )

    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    val bgBrush = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                colors.primaryAccent.copy(alpha = if (isDark) 0.38f else 0.90f),
                colors.secondaryAccent.copy(alpha = if (isDark) 0.28f else 0.85f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                if (isDark) Color.White.copy(alpha = 0.09f) else Color.White.copy(alpha = 0.88f),
                if (isDark) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.72f)
            )
        )
    }

    val borderBrush = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                colors.primaryAccent,
                colors.secondaryAccent
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                if (isDark) Color.White.copy(alpha = 0.35f) else Color.White,
                if (isDark) Color.White.copy(alpha = 0.10f) else Color(0x33000000)
            )
        )
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(color = colors.primaryAccent.copy(alpha = 0.4f)),
            onClick = onClick
        )
    } else Modifier

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(bgBrush)
            .border(BorderStroke(1.dp, borderBrush), CircleShape)
            .then(clickModifier)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Box(modifier = Modifier.padding(end = 6.dp))
        }
        Text(
            text = text,
            color = if (isSelected) {
                if (isDark) colors.primaryAccent else Color.White
            } else {
                colors.textPrimary // Solid black in light mode!
            },
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

/**
 * Liquid Progress Bar with animated caustic specular fluid wave and smooth fill.
 */
@Composable
fun LiquidProgressBar(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    barColor: Brush = Brush.horizontalGradient(listOf(GlassCyan, GlassAzure, GlassIndigo))
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "liquid_bar_progress"
    )

    val transition = rememberInfiniteTransition(label = "liquid_bar_wave")
    val waveOffset = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    val trackBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.07f)
    val trackBorder = if (isDark) Color.White.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(trackBg)
            .border(BorderStroke(0.6.dp, trackBorder), CircleShape)
    ) {
        if (animatedProgress > 0.005f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(barColor)
                    .drawBehind {
                        // Fluid wave shimmer overlay
                        val waveX = waveOffset.value * size.width
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.45f),
                                    Color.Transparent
                                ),
                                center = Offset(waveX, size.height * 0.5f),
                                radius = size.height * 3f
                            )
                        )
                    }
            )
        }
    }
}

/**
 * Circular Frosted Glass Icon Button with tactile ripple and bounce.
 */
@Composable
fun LiquidGlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    size: Dp = 40.dp
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark
    val actualTint = tint ?: if (isDark) colors.primaryAccent else colors.textPrimary

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btn_press_scale"
    )

    val borderBrush = remember(isDark, colors.primaryAccent) {
        Brush.linearGradient(
            listOf(
                if (isDark) Color.White.copy(alpha = 0.40f) else Color.White,
                if (isDark) colors.primaryAccent.copy(alpha = 0.30f) else Color(0x33000000),
                if (isDark) Color.White.copy(alpha = 0.08f) else Color(0x1A000000)
            )
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(if (isDark) Color.White.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.85f))
            .border(BorderStroke(1.dp, borderBrush), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = actualTint.copy(alpha = 0.35f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = actualTint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
