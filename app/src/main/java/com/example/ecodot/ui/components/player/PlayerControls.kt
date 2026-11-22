package com.example.ecodot.ui.components.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Pause
import com.example.ecodot.ui.theme.PlayArrow

@Composable
fun AnimatedControlIcon(
    icon: ImageVector,
    active: Boolean,
    activeColor: Color,
    size: Int,
    isGlassCapsule: Boolean = false,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.82f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ctrl_scale"
    )
    
    val tint = if (active) activeColor else Color.White.copy(alpha = if (isGlassCapsule) 0.95f else 0.55f)

    val baseModifier = Modifier
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = false, radius = (size + 14).dp / 2),
            onClick = {
                pressed = true
                onClick()
            }
        )

    val boxModifier = if (isGlassCapsule) {
        baseModifier
            .size(52.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(0.12f),
                shape = CircleShape
            )
            .background(Color.White.copy(0.06f), CircleShape)
    } else {
        baseModifier.size((size + 18).dp)
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size.dp)
            )
            if (active && !isGlassCapsule) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 3.dp)
                        .clip(CircleShape)
                        .background(activeColor)
                        .shadow(elevation = 6.dp, spotColor = activeColor, ambientColor = activeColor)
                )
            }
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(120)
            pressed = false
        }
    }
}

@Composable
fun PremiumPlayButton(
    isPlaying: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Int = 68,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "play_btn_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                spotColor = accentColor.copy(alpha = 0.35f),
                ambientColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(CircleShape)
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.Black.copy(alpha = 0.2f)),
                onClick = {
                    pressed = true
                    onClick()
                }
            )
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                (scaleIn(initialScale = 0.7f, animationSpec = tween(180)) + fadeIn(animationSpec = tween(180))) togetherWith
                (scaleOut(targetScale = 0.7f, animationSpec = tween(180)) + fadeOut(animationSpec = tween(180)))
            },
            label = "play_pause_icon"
        ) { playing ->
            val icon = if (playing) SimpIcons.Pause else SimpIcons.PlayArrow
            // Optical centering offset for the triangle Play icon
            val iconModifier = Modifier
                .size((size * 0.52f).dp)
                .then(if (!playing) Modifier.offset(x = 1.5.dp) else Modifier)

            Icon(
                imageVector = icon,
                contentDescription = if (playing) "Pause" else "Play",
                tint = Color.Black,
                modifier = iconModifier
            )
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(120)
            pressed = false
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean = false,
    activeColor: Color = Color.White,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.84f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "quick_action_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(CircleShape)
            .background(
                if (isActive) activeColor.copy(alpha = 0.16f)
                else Color.White.copy(alpha = 0.07f)
            )
            .border(
                width = 1.dp,
                color = if (isActive) activeColor.copy(alpha = 0.45f)
                else Color.White.copy(alpha = 0.10f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.25f)),
                onClick = {
                    pressed = true
                    onClick()
                }
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) activeColor else Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(20.dp)
        )

        // Indicator dot if active badge
        if (badgeText != null || isActive) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 6.dp)
                    .clip(CircleShape)
                    .background(activeColor)
            )
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(120)
            pressed = false
        }
    }
}
