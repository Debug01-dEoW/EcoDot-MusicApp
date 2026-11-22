package com.example.ecodot.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.ui.theme.EcoDotRed

/**
 * Liquid Glass Filter Capsule (SimpMusic Style).
 * Uses tactile spring squashing on touch, specular glass rim, and smooth selection transition.
 */
@Composable
fun SimpChip(
    isAnimated: Boolean = false,
    isSelected: Boolean = false,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "chip_scale"
    )

    val chipShape = RoundedCornerShape(20.dp)

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0x18FFFFFF),
        animationSpec = tween(180),
        label = "chip_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF0F1016) else Color.White.copy(alpha = 0.78f),
        animationSpec = tween(180),
        label = "chip_text"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.16f),
        animationSpec = tween(180),
        label = "chip_border"
    )

    val rimBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(
                Color.White,
                Color.White.copy(alpha = 0.8f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.08f),
                Color.Black.copy(alpha = 0.20f)
            ),
            start = Offset(0f, 0f),
            end = Offset(100f, 100f)
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = chipShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.35f),
                        spotColor = Color.Black.copy(alpha = 0.45f)
                    )
                } else Modifier
            )
            .clip(chipShape)
            .background(bgColor, chipShape)
            .border(
                width = 0.85.dp,
                brush = rimBrush,
                shape = chipShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(EcoDotRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
