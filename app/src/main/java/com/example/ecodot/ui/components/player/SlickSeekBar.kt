package com.example.ecodot.ui.components.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SlickSeekBar(
    position: Long,
    duration: Long,
    accentColor: Color,
    isPlaying: Boolean,
    trackTitle: String,
    onSeek: (Long) -> Unit,
    formatDuration: (Long) -> String,
    modifier: Modifier = Modifier,
    isVideo: Boolean = false
) {
    val safeDuration = duration.coerceAtLeast(1L)
    val fraction = (position.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(fraction) }

    val displayFraction = if (isDragging) dragFraction else fraction

    // Spring animation scale for visualizer bars on drag
    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "drag_scale"
    )

    // Waveform breath amplitude animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_breathe")
    val waveOscillation by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveform_amplitude"
    )

    // Animated running wave phase angle
    val phaseAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * kotlin.math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_phase"
    )

    // Deterministic seeded height multipliers for the 42 waveform bars
    val totalBars = 42
    val barHeights = remember(trackTitle) {
        val rand = java.util.Random(trackTitle.hashCode().toLong())
        List(totalBars) { 0.15f + rand.nextFloat() * 0.85f }
    }

    val cursorIndex = displayFraction * totalBars

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp) // Substantial premium height for waveform
                .pointerInput(safeDuration, trackTitle) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        isDragging = true
                        dragFraction = (down.position.x / size.width.toFloat()).coerceIn(0f, 1f)

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            change.consume()
                            if (!change.pressed) {
                                onSeek((dragFraction * safeDuration).toLong())
                                isDragging = false
                                break
                            } else {
                                dragFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cy = size.height / 2f
                val filledW = displayFraction * size.width

                // 42 vertical bars visualizer layout details
                val spacingRatio = 0.4f
                val totalUnits = totalBars + (totalBars - 1) * spacingRatio
                val barWidth = size.width / totalUnits
                val spacing = barWidth * spacingRatio

                for (i in 0 until totalBars) {
                    val x = i * (barWidth + spacing) + barWidth / 2f
                    
                    // Tactile Fisheye Bulging
                    val distance = kotlin.math.abs(i - cursorIndex)
                    val bulgeFactor = if (isDragging) {
                        1.0f + 0.45f * kotlin.math.exp(- (distance * distance) / 10.0f)
                    } else {
                        1.0f
                    }

                    // Live phase ripple shift
                    val phaseOffset = i * (2 * kotlin.math.PI.toFloat() / totalBars)
                    val rippleFactor = if (isPlaying) {
                        0.85f + 0.2f * kotlin.math.sin(phaseAngle + phaseOffset)
                    } else {
                        1.0f
                    }

                    val baseH = barHeights[i] * size.height * 0.72f
                    val animatedH = baseH * rippleFactor * waveOscillation * dragScale * bulgeFactor
                    val finalH = animatedH.coerceIn(4.dp.toPx(), size.height)

                    val left = x - barWidth / 2f
                    val top = cy - finalH / 2f
                    
                    val barSize = androidx.compose.ui.geometry.Size(barWidth, finalH)
                    val cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)

                    // Draw filled active bar vs dimmed inactive bar
                    if (x <= filledW) {
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(left, top),
                            size = barSize,
                            cornerRadius = cornerRadius
                        )
                    } else {
                        drawRoundRect(
                            color = Color.White.copy(0.18f),
                            topLeft = Offset(left, top),
                            size = barSize,
                            cornerRadius = cornerRadius
                        )
                    }
                }

                // Glowing minimal indicator dot following progress
                val thumbRadiusPx = 5.dp.toPx() * dragScale
                
                // Ambient backdrop radial gradient glow
                val glowBrush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(filledW, cy),
                    radius = thumbRadiusPx * 6.5f
                )
                drawCircle(
                    brush = glowBrush,
                    radius = thumbRadiusPx * 6.5f,
                    center = Offset(filledW, cy)
                )

                // Energy Orb Glow
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = thumbRadiusPx * 3.5f,
                    center = Offset(filledW, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = thumbRadiusPx * 2.0f,
                    center = Offset(filledW, cy)
                )
                drawCircle(
                    color = Color.White,
                    radius = thumbRadiusPx,
                    center = Offset(filledW, cy)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatDuration(if (isDragging) (dragFraction * safeDuration).toLong() else position),
                color = Color.White.copy(0.55f),
                fontSize = 12.sp
            )
            Text(
                formatDuration(duration),
                color = Color.White.copy(0.55f),
                fontSize = 12.sp
            )
        }
    }
}
