package com.example.ecodot.ui.components

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * An angular gradient background matching SimpMusic's exact vector rotation.
 */
fun Modifier.angledGradientBackground(
    colors: List<Color>,
    degrees: Float,
): Modifier = this.then(
    if (colors.size < 2) {
        Modifier
    } else {
        Modifier.drawBehind {
            val (x, y) = size
            val gamma = atan2(y, x)

            if (gamma == 0f || gamma == (PI / 2).toFloat()) {
                return@drawBehind
            }

            val degreesNormalised = (degrees % 360).let { if (it < 0) it + 360 else it }
            val alpha = (degreesNormalised * PI / 180).toFloat()

            val gradientLength =
                when (alpha) {
                    in 0f..gamma, in (2 * PI - gamma)..2 * PI -> x / cos(alpha)
                    in gamma..(PI - gamma).toFloat() -> y / sin(alpha)
                    in (PI - gamma)..(PI + gamma) -> x / -cos(alpha)
                    in (PI + gamma)..(2 * PI - gamma) -> y / -sin(alpha)
                    else -> hypot(x, y)
                }

            val centerOffsetX = cos(alpha) * gradientLength / 2
            val centerOffsetY = sin(alpha) * gradientLength / 2

            drawRect(
                brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(center.x - centerOffsetX, center.y - centerOffsetY),
                    end = Offset(center.x + centerOffsetX, center.y + centerOffsetY),
                ),
                size = size,
            )
        }
    }
)

/**
 * Piecewise S-curve scrim easing with 24 stops to prevent 8-bit banding on dark backgrounds.
 */
fun smoothScrimBrush(
    from: Color,
    to: Color,
    startFraction: Float = 0f,
    endFraction: Float = 1f,
    startY: Float = 0f,
    endY: Float = Float.POSITIVE_INFINITY,
    steps: Int = 24,
): Brush =
    Brush.verticalGradient(
        colorStops = Array(steps + 1) { i ->
            val t = i / steps.toFloat()
            val position = startFraction + (endFraction - startFraction) * t
            position to lerp(from, to, t * t * (3f - 2f * t))
        },
        startY = startY,
        endY = endY,
        tileMode = TileMode.Clamp
    )

/**
 * Bottom scrim that melts artwork into the page background.
 */
fun artworkScrimBrush(
    color: Color,
    steps: Int = 24,
): Brush = smoothScrimBrush(from = color.copy(alpha = 0f), to = color, steps = steps)

/**
 * Shimmer effect brush for loading placeholdes.
 */
fun Modifier.simpShimmer(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "Shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(1000)),
        label = "ShimmerOffset"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E202C),
                Color(0xFF2D3042),
                Color(0xFF1E202C),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat()),
        )
    ).onGloballyPositioned {
        size = it.size
    }
}
