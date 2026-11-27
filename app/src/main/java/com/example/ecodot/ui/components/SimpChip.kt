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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop

/**
 * SimpMusic-style Liquid Glass Filter Capsule.
 *
 * Selected chips stay an opaque high-contrast lavender pill (without reflowing icons) and show
 * an optional animated sweep loading ring (`homeLoadingRing`) while results load. Unselected chips
 * render live Liquid Glass over sibling [backdrop] when supplied.
 */
@Composable
fun SimpChip(
    isAnimated: Boolean = false,
    isSelected: Boolean = false,
    text: String,
    backdrop: Backdrop? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 560f),
        label = "chip_scale"
    )

    val chipShape = CircleShape

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFF0E3FF) else Color(0x1EFFFFFF),
        animationSpec = tween(180),
        label = "chip_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF1A102B) else Color.White.copy(alpha = 0.90f),
        animationSpec = tween(180),
        label = "chip_text"
    )

    val glassSurfaceModifier = if (!isSelected && backdrop != null) {
        Modifier.homeLiquidGlass(
            backdrop = backdrop,
            shape = chipShape,
            surfaceTint = Color(0xFF0E1018),
            surfaceAlpha = 0.14f,
            specularAlpha = 0.84f,
            frostSheenAlpha = 0.12f,
            blurDp = 1.5.dp,
            pressedScaleTarget = 0.94f,
            isPressed = isPressed
        )
    } else {
        Modifier
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = chipShape,
                        clip = false,
                        ambientColor = Color(0xFFF0E3FF).copy(alpha = 0.28f),
                        spotColor = Color.Black.copy(alpha = 0.45f)
                    )
                } else Modifier
            )
            .clip(chipShape)
            .background(bgColor, chipShape)
            .border(
                width = 0.85.dp,
                color = if (isSelected) Color.White.copy(alpha = 0.80f) else Color.White.copy(alpha = 0.22f),
                shape = chipShape
            )
    }

    Box(
        modifier = modifier
            .homeLoadingRing(enabled = isAnimated && isSelected, shape = chipShape)
            .then(glassSurfaceModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
