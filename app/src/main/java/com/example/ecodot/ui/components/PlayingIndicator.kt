package com.example.ecodot.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import com.example.ecodot.ui.theme.EcoDotRed

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
                    .fillMaxHeight(height.value)
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun ExplicitBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(13.dp)
            .background(Color(0xFF7F7F7F), RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "E",
            color = Color(0xFF121212),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 9.sp
        )
    }
}
