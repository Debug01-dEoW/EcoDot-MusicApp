package com.example.ecodot.ui.components.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.ui.theme.EcoDotRed

@Composable
fun VinylArtworkView(
    imageUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    dominantColor: Color = Color.DarkGray,
    playbackProgress: Float = 0.25f,
    playbackProgressProvider: (() -> Float)? = null
) {
    // GPU-only continuous 33 1/3 RPM rotation without per-frame coroutine relaunches or recomposition
    val rotationAnim = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val start = rotationAnim.value % 360f
            rotationAnim.snapTo(start)
            rotationAnim.animateTo(
                targetValue = start + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotationAnim.stop()
        }
    }

    val targetTonearm by remember(isPlaying, playbackProgressProvider, playbackProgress) {
        derivedStateOf {
            val raw = (playbackProgressProvider?.invoke() ?: playbackProgress).coerceIn(0f, 1f)
            val quantized = (raw * 40f).toInt() / 40f
            PlayerStyleMath.computeVinylTonearmAngle(
                isPlaying = isPlaying,
                playbackFraction = quantized,
                restAngle = -24f,
                startPlayAngle = 14f,
                endPlayAngle = 29f
            )
        }
    }

    // Tonearm swing animation: lifts off when paused (-24 deg), tracks across grooves while playing
    val tonearmAngle by animateFloatAsState(
        targetValue = targetTonearm,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 220f
        ),
        label = "tonearm_angle"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Ambient glow beneath turntable disc
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.45f),
                            dominantColor.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // ── 1. The Vinyl Record Disc ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize(0.86f)
                .shadow(elevation = 16.dp, shape = CircleShape, spotColor = Color.Black.copy(alpha = 0.7f))
                .clip(CircleShape)
                .background(Color(0xFF0E0E12))
                .graphicsLayer { rotationZ = rotationAnim.value },
            contentAlignment = Alignment.Center
        ) {
            // Cached vinyl grooves texture with radial specular lighting
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxRadius = size.minDimension / 2f
                        val grooveStroke = Stroke(width = 1f)
                        val sheenBrush = Brush.sweepGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.08f)
                            ),
                            center = center
                        )
                        onDrawBehind {
                            val grooveSteps = 22
                            for (i in 1..grooveSteps) {
                                val radius = maxRadius * (0.42f + (i / grooveSteps.toFloat()) * 0.54f)
                                drawCircle(
                                    color = Color.White.copy(alpha = if (i % 3 == 0) 0.05f else 0.02f),
                                    radius = radius,
                                    center = center,
                                    style = grooveStroke
                                )
                            }
                            drawCircle(brush = sheenBrush, radius = maxRadius, center = center)
                        }
                    }
            )

            // ── 2. Center Album Art Label ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize(0.40f)
                    .clip(CircleShape)
                    .background(Color(0xFF1B1B22))
                    .border(2.5.dp, Color(0xFF2C2D3A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                var resolvedUrl by remember(imageUrl) {
                    mutableStateOf(
                        com.example.ecodot.data.remote.YouTubeParser.getHighResUrl(imageUrl) ?: imageUrl
                    )
                }
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(resolvedUrl)
                        .size(512, 512)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    onError = {
                        com.example.ecodot.data.remote.YouTubeParser.getFallbackThumbUrl(resolvedUrl)?.let { fallback ->
                            resolvedUrl = fallback
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Brass Center Spindle Hole
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFE5C07B), Color(0xFF6B5524), Color(0xFF1E1E24))
                            )
                        )
                        .border(1.dp, Color(0xFFD4AF37), CircleShape)
                )
            }
        }

        // ── 3. Tonearm / Needle Assembly ─────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = (-8).dp)
                .size(110.dp, 160.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = tonearmAngle
                        transformOrigin = TransformOrigin(0.85f, 0.15f)
                    }
            ) {
                val pivotX = size.width * 0.85f
                val pivotY = size.height * 0.15f

                // Tonearm Base / Pivot Ring
                drawCircle(
                    color = Color(0xFF24252E),
                    radius = 18f,
                    center = Offset(pivotX, pivotY)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF8E92A8), Color(0xFF333544))
                    ),
                    radius = 12f,
                    center = Offset(pivotX, pivotY)
                )

                // Counterweight behind pivot
                drawRoundRect(
                    color = Color(0xFF1A1B22),
                    topLeft = Offset(pivotX - 6f, pivotY - 26f),
                    size = androidx.compose.ui.geometry.Size(12f, 18f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )

                // Aluminum Tonearm Bar (curved vintage arm)
                val armPath = Path().apply {
                    moveTo(pivotX, pivotY)
                    lineTo(pivotX - 28f, pivotY + 70f)
                    lineTo(pivotX - 44f, pivotY + 115f)
                }
                drawPath(
                    path = armPath,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFE0E0E8), Color(0xFF888B98), Color(0xFFE0E0E8))
                    ),
                    style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Headshell & Cartridge at the needle tip
                val headX = pivotX - 44f
                val headY = pivotY + 115f
                drawRoundRect(
                    color = Color(0xFF1E202B),
                    topLeft = Offset(headX - 8f, headY - 4f),
                    size = androidx.compose.ui.geometry.Size(16f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
                // Cartridge accent dot (gold/red)
                drawCircle(
                    color = EcoDotRed,
                    radius = 2.5f,
                    center = Offset(headX, headY + 14f)
                )
            }
        }
    }
}
