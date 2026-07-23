package com.example.ecodot.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ecodot.ui.theme.animatedCombinedClickable
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

/**
 * Liquid Glass modifier using Haze library for real frosted-glass blur.
 * Pass [hazeState] from a parent `haze {}` modifier to enable real blurring.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0x22FFFFFF),
    specularAlpha: Float = 0.30f,
    borderWidth: Dp = 0.8.dp,
    elevation: Dp = 8.dp,
    hazeState: HazeState? = null,
    tintColor: Color = Color(0xFF141620),
    blurRadius: Dp = 20.dp
): Modifier {
    val base = this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.4f),
            spotColor = Color.Black.copy(alpha = 0.6f)
        )
        .clip(shape)

    val glassStyle = HazeStyle(
        backgroundColor = tintColor.copy(alpha = 0.50f),
        tint = HazeDefaults.tint(tintColor.copy(alpha = 0.30f)),
        blurRadius = blurRadius,
        noiseFactor = 0.06f
    )

    val withBackground = if (hazeState != null) {
        base.hazeEffect(state = hazeState, style = glassStyle)
    } else {
        base.background(
            color = tintColor.copy(alpha = 0.78f),
            shape = shape
        )
    }

    return withBackground.border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = specularAlpha),
                Color.White.copy(alpha = specularAlpha * 0.6f),
                Color.White.copy(alpha = 0.03f),
                Color.Black.copy(alpha = 0.20f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = shape
    )
}

/**
 * A reusable Liquid Glass container card with optional click interaction.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color(0x22FFFFFF),
    tintColor: Color = Color(0xFF141620),
    specularAlpha: Float = 0.30f,
    elevation: Dp = 6.dp,
    hazeState: HazeState? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cardModifier = modifier
        .liquidGlass(
            shape = shape,
            backgroundColor = backgroundColor,
            tintColor = tintColor,
            specularAlpha = specularAlpha,
            elevation = elevation,
            hazeState = hazeState
        )

    val interactiveModifier = if (onClick != null || onLongClick != null) {
        cardModifier.animatedCombinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick
        )
    } else {
        cardModifier
    }

    Box(
        modifier = interactiveModifier,
        content = content
    )
}

/**
 * Animated liquid mesh background with subtle shifting ambient color blobs.
 * Simulates iOS 26 Liquid Glass ambient light behavior.
 */
@Composable
fun LiquidMeshBackground(
    modifier: Modifier = Modifier,
    dominantColor: Color = Color(0xFF6B21A8),
    accentColor: Color = Color(0xFF0EA5E9),
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_mesh")

    val offsetX1 by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_x"
    )
    val offsetY1 by infiniteTransition.animateFloat(
        initialValue = -0.08f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_y"
    )
    val offsetX2 by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = -0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_x"
    )
    val offsetY2 by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_y"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060810))
            .drawWithCache {
                val w = size.width
                val h = size.height
                onDrawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                dominantColor.copy(alpha = 0.26f),
                                dominantColor.copy(alpha = 0.09f),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.25f + offsetX1), h * (0.20f + offsetY1)),
                            radius = w * 0.85f
                        ),
                        radius = w * 0.85f,
                        center = Offset(w * (0.25f + offsetX1), h * (0.20f + offsetY1))
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.20f),
                                accentColor.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.75f + offsetX2), h * (0.70f + offsetY2)),
                            radius = w * 0.70f
                        ),
                        radius = w * 0.70f,
                        center = Offset(w * (0.75f + offsetX2), h * (0.70f + offsetY2))
                    )
                }
            }
    ) {
        content()
    }
}
