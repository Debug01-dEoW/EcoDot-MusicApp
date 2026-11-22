package com.example.ecodot.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun InfiniteBorderAnimationView(
    isAnimated: Boolean = false,
    brush: Brush = Brush.sweepGradient(listOf(Color(0xFF888888), Color.White, Color(0xFF888888))),
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentPadding: Dp = 0.dp,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    oneCircleDurationMillis: Int = 3000,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite Color Animation")
    val degrees by infiniteTransition.animateFloat(
        initialValue = 90f,
        targetValue = 450f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = oneCircleDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "Infinite Colors",
    )
    val scaleAnimationValue by animateFloatAsState(
        targetValue = if (isAnimated) 1f else 0f,
        animationSpec = tween(800),
        label = "BorderScale"
    )
    Surface(
        modifier = Modifier
            .clip(shape)
            .padding(borderWidth)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawBehind {
                scale(scale = scaleAnimationValue) {
                    rotate(degrees = degrees) {
                        drawCircle(
                            brush = brush,
                            radius = size.width,
                            blendMode = BlendMode.SrcIn,
                        )
                    }
                }
            }
            .animateContentSize(),
        color = backgroundColor,
        shape = shape,
    ) {
        Box(
            modifier = Modifier
                .background(color = if (isAnimated) Color(0xFF14151E) else backgroundColor)
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
