package com.example.ecodot.ui.components.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.ui.theme.poppinsFamily
import kotlin.math.roundToInt

@Composable
fun ContinuousSeekBar(
    position: Long,
    duration: Long,
    accentColor: Color,
    onSeek: (Long) -> Unit,
    formatDuration: (Long) -> String,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    val currentProgress = if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    val displayProgress = if (isDragging) dragProgress else currentProgress

    // Smooth height expansion on touch (3.5dp -> 7.5dp)
    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) 7.5.dp else 3.5.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "scrubber_height"
    )

    // Thumb scale (0f -> 1f)
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0.4f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "thumb_scale"
    )

    val activeColor = Color.White
    val inactiveTrackColor = Color.White.copy(alpha = 0.22f)

    var lastHapticStep by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Floating Time Tooltip during scrub
        androidx.compose.animation.AnimatedVisibility(
            visible = isDragging,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(160))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                val tooltipOffset = with(density) {
                    ((displayProgress * trackWidthPx) - 24.dp.toPx())
                        .coerceIn(0f, (trackWidthPx - 48.dp.toPx()).coerceAtLeast(0f))
                }

                Box(
                    modifier = Modifier
                        .offset { IntOffset(tooltipOffset.roundToInt(), 0) }
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E1F2B).copy(alpha = 0.92f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    val scrubMs = (dragProgress * duration).toLong().coerceIn(0L, duration.coerceAtLeast(0L))
                    Text(
                        text = formatDuration(scrubMs),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = poppinsFamily,
                        color = Color.White
                    )
                }
            }
        }

        // Custom Interactive Canvas Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat() }
                .pointerInput(duration) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        if (trackWidthPx > 0f) {
                            isDragging = true
                            val fraction = (down.position.x / trackWidthPx).coerceIn(0f, 1f)
                            dragProgress = fraction
                            lastHapticStep = (fraction * 40).roundToInt()
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            change.consume()
                            if (!change.pressed) {
                                // Touch released
                                if (trackWidthPx > 0f && isDragging) {
                                    val targetPos = (dragProgress * duration).toLong().coerceIn(0L, duration)
                                    onSeek(targetPos)
                                }
                                isDragging = false
                                break
                            } else {
                                // Touch dragging
                                if (trackWidthPx > 0f) {
                                    val fraction = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                                    dragProgress = fraction
                                    val currentStep = (fraction * 40).roundToInt()
                                    if (currentStep != lastHapticStep) {
                                        lastHapticStep = currentStep
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
            ) {
                val width = size.width
                val height = size.height
                val cornerRadius = CornerRadius(height / 2f, height / 2f)

                // 1. Inactive Track Background
                drawRoundRect(
                    color = inactiveTrackColor,
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = cornerRadius
                )

                // 2. Active Track Fill with subtle gradient
                val activeWidth = (displayProgress * width).coerceIn(0f, width)
                if (activeWidth > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                activeColor.copy(alpha = 0.85f),
                                activeColor
                            ),
                            startX = 0f,
                            endX = activeWidth
                        ),
                        topLeft = Offset.Zero,
                        size = Size(activeWidth, height),
                        cornerRadius = cornerRadius
                    )
                }

                // 3. Glowing Scrubber Thumb
                val thumbRadiusPx = (6.5.dp * thumbScale).toPx()
                val thumbCenterX = activeWidth.coerceIn(thumbRadiusPx, width - thumbRadiusPx)
                val thumbCenterY = height / 2f

                if (thumbScale > 0.1f) {
                    // Outer soft glow
                    if (isDragging) {
                        drawCircle(
                            color = activeColor.copy(alpha = 0.35f),
                            radius = thumbRadiusPx * 1.8f,
                            center = Offset(thumbCenterX, thumbCenterY)
                        )
                    }
                    // Solid white core thumb
                    drawCircle(
                        color = Color.White,
                        radius = thumbRadiusPx,
                        center = Offset(thumbCenterX, thumbCenterY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Time Elapsed / Remaining Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayPos = if (isDragging) (dragProgress * duration).toLong() else position
            Text(
                text = formatDuration(displayPos.coerceAtLeast(0L)),
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.62f),
                fontFamily = poppinsFamily,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatDuration(duration.coerceAtLeast(0L)),
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.62f),
                fontFamily = poppinsFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
