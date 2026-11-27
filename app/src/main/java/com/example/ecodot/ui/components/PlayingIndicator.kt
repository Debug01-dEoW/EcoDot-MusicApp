package com.example.ecodot.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Explicit

@Composable
fun PlayingIndicator(modifier: Modifier = Modifier, color: Color = EcoDotRed) {
    val infiniteTransition = rememberInfiniteTransition(label = "PlayingAnimation")
    val heights = List(3) { i ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 350 + (i * 100), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$i"
        )
    }

    Row(
        modifier = modifier.height(16.dp).width(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { height ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 1f)
                        scaleY = height.value
                    }
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(color)
            )
        }
    }
}

/**
 * Authentic SimpMusic Explicit Badge using the exact SimpIcons.Explicit vector icon.
 */
@Composable
fun ExplicitBadge(
    modifier: Modifier = Modifier,
    tint: Color = Color.White.copy(alpha = 0.70f)
) {
    Icon(
        imageVector = SimpIcons.Explicit,
        contentDescription = "Explicit",
        tint = tint,
        modifier = modifier.size(15.dp)
    )
}
