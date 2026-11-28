package com.example.ecodot.ui.components
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.PlaylistAdd

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.ui.theme.EcoDotRed
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Universal Swipe-Left-To-Right gesture container to add any song to the playback queue.
 * Swiping past the threshold triggers haptic feedback, displays the "Add to Queue" indicator,
 * and calls [onAddToQueue]. The item smoothly snaps back with a spring animation.
 */
@Composable
fun SwipeToQueueBox(
    onAddToQueue: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val maxOffsetPx = with(density) { 108.dp.toPx() }
    val triggerThresholdPx = with(density) { 68.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    var triggeredHaptic by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        // Queue action badge revealed behind the swiped item
        if (offsetX.value > 6f) {
            val progress = (offsetX.value / triggerThresholdPx).coerceIn(0f, 1f)
            val isReady = offsetX.value >= triggerThresholdPx

            Row(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        alpha = progress
                        scaleX = 0.7f + 0.3f * progress
                        scaleY = 0.7f + 0.3f * progress
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isReady) EcoDotRed else Color.White.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SimpIcons.PlaylistAdd,
                        contentDescription = "Add to Queue",
                        tint = if (isReady) Color.White else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (isReady) {
                    Text(
                        text = "Add to Queue",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Forefront item content sliding horizontally on drag
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            if (offsetX.value + dragAmount > 0f) {
                                change.consume()
                                coroutineScope.launch {
                                    val newOffset = (offsetX.value + dragAmount * 0.8f).coerceIn(0f, maxOffsetPx)
                                    offsetX.snapTo(newOffset)
                                    if (newOffset >= triggerThresholdPx && !triggeredHaptic) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        triggeredHaptic = true
                                    } else if (newOffset < triggerThresholdPx && triggeredHaptic) {
                                        triggeredHaptic = false
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            if (offsetX.value >= triggerThresholdPx) {
                                onAddToQueue()
                            }
                            triggeredHaptic = false
                            coroutineScope.launch {
                                offsetX.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow))
                            }
                        },
                        onDragCancel = {
                            triggeredHaptic = false
                            coroutineScope.launch {
                                offsetX.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow))
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}
