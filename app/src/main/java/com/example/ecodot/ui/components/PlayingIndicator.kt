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
            .clip(RoundedCornerShape(3.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 3.5.dp, vertical = 0.5.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "E",
            style = androidx.compose.ui.text.TextStyle(
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Default
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
