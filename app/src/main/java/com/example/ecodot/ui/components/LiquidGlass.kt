package com.example.ecodot.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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

/**
 * Custom Liquid Glass background and specular highlight modifier inspired by liquid-glass aesthetics.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0x2B1F202E),
    specularAlpha: Float = 0.35f,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = Color.Black.copy(alpha = 0.5f),
        spotColor = Color.Black.copy(alpha = 0.7f)
    )
    .clip(shape)
    .background(backgroundColor)
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = specularAlpha),
                Color.White.copy(alpha = specularAlpha * 0.2f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.4f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = shape
    )

/**
 * A reusable Liquid Glass container card with interactive tactile bounce.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color(0x30222436),
    specularAlpha: Float = 0.35f,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cardModifier = modifier
        .liquidGlass(
            shape = shape,
            backgroundColor = backgroundColor,
            specularAlpha = specularAlpha,
            elevation = elevation
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
 * Animated liquid mesh background with fluid shifting color blobs.
 */
@Composable
fun LiquidMeshBackground(
    modifier: Modifier = Modifier,
    dominantColor: Color = Color(0xFF7C3AED),
    accentColor: Color = Color(0xFF10B981),
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_mesh")
    
    val offsetX1 by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_x"
    )
    val offsetY1 by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_y"
    )

    val offsetX2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = -0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_x"
    )
    val offsetY2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = -0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_y"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07070C))
            .drawWithCache {
                val width = size.width
                val height = size.height

                onDrawBehind {
                    // Blob 1 (Dominant theme color)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                dominantColor.copy(alpha = 0.35f),
                                dominantColor.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(width * (0.3f + offsetX1), height * (0.25f + offsetY1)),
                            radius = width * 0.85f
                        ),
                        radius = width * 0.85f,
                        center = Offset(width * (0.3f + offsetX1), height * (0.25f + offsetY1))
                    )

                    // Blob 2 (Accent color)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.28f),
                                accentColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(width * (0.7f + offsetX2), height * (0.65f + offsetY2)),
                            radius = width * 0.75f
                        ),
                        radius = width * 0.75f,
                        center = Offset(width * (0.7f + offsetX2), height * (0.65f + offsetY2))
                    )
                }
            }
    ) {
        content()
    }
}
