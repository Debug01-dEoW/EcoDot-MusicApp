package com.example.ecodot.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MusicNote
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Pause
import com.example.ecodot.ui.theme.PlayArrow
import com.example.ecodot.ui.theme.Explicit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ecodot.LocalHazeState
import com.example.ecodot.data.local.entities.Track
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MiniPlayer(
    track: Track?,
    isPlaying: Boolean,
    progress: Float = 0f,
    progressProvider: (() -> Float)? = null,
    onTogglePlayback: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit = {},
    onLike: (() -> Unit)? = null,
    isLiked: Boolean = false,
    onClick: () -> Unit,
    onClose: (() -> Unit)? = null,
    dominantColor: Color = Color(0xFF1E1F2A),
    hazeState: HazeState? = null,
    backdrop: Backdrop? = LocalBackdrop.current,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val burstState = rememberHeartBurstState()
    val tilt = LocalTiltState.current

    // Authentic SimpMusic Liquid Glass Capsule Shape (RoundedCornerShape 26dp)
    val miniPlayerShape = RoundedCornerShape(26.dp)

    // Crystal-Clear Liquid Glass Modifier with pristine optical refraction and zero hazy fog
    val glassModifier = if (backdrop != null) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { miniPlayerShape },
            highlight = {
                Highlight(
                    style = com.kyant.backdrop.highlight.HighlightStyle.Default(
                        angle = 45f + (tilt.tiltX * 35f) + (tilt.tiltY * 20f),
                        falloff = 1.0f
                    ),
                    alpha = 0.88f
                )
            },
            shadow = { Shadow(radius = 8.dp, color = Color.Black.copy(alpha = 0.22f)) },
            innerShadow = { InnerShadow(radius = 6.dp, alpha = 0.25f, color = Color.White) },
            effects = {
                blur(2.dp.toPx())
                colorControls(
                    brightness = 0.04f,
                    contrast = 1.05f,
                    saturation = 1.25f
                )
            },
            onDrawSurface = {
                // Ultra-clean transparent glass: subtle darkening keeps text readable with zero blur fog
                drawRect(Color.Black.copy(alpha = 0.14f))
                drawRect(Color.White.copy(alpha = 0.05f))
            }
        )
    } else {
        Modifier
            .hazeEffect(
                state = hazeState ?: LocalHazeState.current,
                style = HazeStyle(
                    backgroundColor = Color.Transparent,
                    tint = HazeDefaults.tint(Color.White.copy(alpha = 0.03f)),
                    blurRadius = 4.dp,
                    noiseFactor = 0f
                )
            )
            .background(Color.Black.copy(alpha = 0.20f))
    }

    Box(
        modifier = modifier
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .then(glassModifier)
            .border(
                width = 0.85.dp,
                brush = createSpecularRimBrush(
                    tiltX = tilt.tiltX,
                    tiltY = tilt.tiltY,
                    baseAlpha = 0.55f,
                    secondaryAlpha = 0.25f
                ),
                shape = miniPlayerShape
            )
            .clip(miniPlayerShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        if (offsetY.value + dragAmount > 0) {
                            coroutineScope.launch {
                                change.consume()
                                offsetY.animateTo(offsetY.value + 2 * dragAmount)
                            }
                        } else if (dragAmount < -15f) {
                            onClick()
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch { offsetY.animateTo(0f) }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetY.value > 70f) {
                                onClose?.invoke() ?: onTogglePlayback()
                            }
                            offsetY.animateTo(0f)
                        }
                    }
                )
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                // Track Artwork + Title/Artist container with horizontal swipe
                Box(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { change, dragAmount ->
                                        coroutineScope.launch {
                                            change.consume()
                                            offsetX.animateTo(offsetX.value + dragAmount * 2)
                                        }
                                    },
                                    onDragCancel = {
                                        coroutineScope.launch {
                                            if (offsetX.value > 150f) {
                                                onSkipPrevious()
                                            } else if (offsetX.value < -120f) {
                                                onSkipNext()
                                            }
                                            offsetX.animateTo(0f)
                                        }
                                    },
                                    onDragEnd = {
                                        coroutineScope.launch {
                                            if (offsetX.value > 150f) {
                                                onSkipPrevious()
                                            } else if (offsetX.value < -120f) {
                                                onSkipNext()
                                            }
                                            offsetX.animateTo(0f)
                                        }
                                    }
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 40dp Artwork (SimpMusic ArtworkShape 8dp)
                        var miniArtworkUrl by remember(track.id, track.albumArtUri) {
                            mutableStateOf(track.albumArtUri)
                        }
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(miniArtworkUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.MusicNote),
                            error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.MusicNote),
                            onError = {
                                val fallback = miniArtworkUrl?.replace("maxresdefault", "hqdefault")
                                if (fallback != null && fallback != miniArtworkUrl) {
                                    miniArtworkUrl = fallback
                                }
                            },
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF222432))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Title & Artist with continuous marquee (SimpMusic style)
                        AnimatedContent(
                            targetState = track,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterStart,
                            transitionSpec = {
                                (slideInHorizontally { it } + fadeIn()).togetherWith(
                                    slideOutHorizontally { -it } + fadeOut()
                                )
                            },
                            label = "mini_track_info"
                        ) { curTrack ->
                            Column(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .padding(end = 4.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = curTrack.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            animationMode = MarqueeAnimationMode.Immediately,
                                            repeatDelayMillis = 2000,
                                            initialDelayMillis = 2000,
                                            spacing = androidx.compose.foundation.MarqueeSpacing(40.dp)
                                        )
                                        .focusable()
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (curTrack.isExplicit || curTrack.title.contains("explicit", true) || curTrack.album.contains("explicit", true)) {
                                        ExplicitBadge(
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = curTrack.artist,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.5.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.70f),
                                        maxLines = 1,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                animationMode = MarqueeAnimationMode.Immediately
                                            )
                                            .focusable()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Heart like button with HeartBurst particle animation (SimpMusic exact)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .heartBurst(burstState)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isLiked) burstState.fire()
                            onLike?.invoke()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = isLiked, label = "mini_like_crossfade") { liked ->
                        Icon(
                            imageVector = if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (liked) Color(0xFFFF3B5C) else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Play / Pause Button (Image 5 exact - subtle glass circle button)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTogglePlayback
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
            }

            // SimpMusic Exact Bottom Progress Line (1.5dp, hairline, transparent track)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter)
            ) {
                LinearProgressIndicator(
                    progress = { (progressProvider?.invoke() ?: progress).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = Color.White.copy(alpha = 0.85f),
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round,
                    drawStopIndicator = {}
                )
            }
        }
    }
}
