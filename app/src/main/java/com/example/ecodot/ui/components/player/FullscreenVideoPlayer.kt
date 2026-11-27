package com.example.ecodot.ui.components.player

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.PlayerView
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.theme.*
import com.example.ecodot.ui.viewmodel.LyricLine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pixel-Perfect SimpMusic Fullscreen Video Player.
 * Matches exact UI:
 * - Top: [<] [Track Title] [:]
 * - Center: [|<<] [(5)] [|| / >] [(5)] [>>|] (bare white controls)
 * - Subtitles: Centered directly above the timeline slider
 * - Bottom: Full-width slider, [01:27 / 04:22], [CC/Subtitles], [Exit Fullscreen]
 * - Gestures: Double-tap left/right for 5s seek
 */
@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FullscreenVideoPlayer(
    track: Track,
    controller: MediaController?,
    position: Long,
    duration: Long,
    isPlaying: Boolean,
    syncedLyrics: List<LyricLine>,
    videoResizeMode: Int,
    onToggleResizeMode: () -> Unit,
    onExitFullscreen: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    formatDuration: (Long) -> String,
    modifier: Modifier = Modifier,
    positionProvider: (() -> Long)? = null
) {
    BackHandler(enabled = true) {
        onExitFullscreen()
    }

    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    var showOverlay by rememberSaveable { mutableStateOf(true) }
    var showCaptions by rememberSaveable { mutableStateOf(true) }
    var isSliding by rememberSaveable { mutableStateOf(false) }
    var sliderFraction by rememberSaveable { mutableFloatStateOf(0f) }

    // Double tap feedback
    var showBackwardFeedback by remember { mutableStateOf(false) }
    var showForwardFeedback by remember { mutableStateOf(false) }
    val backwardInteractionSource = remember { MutableInteractionSource() }
    val forwardInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(showBackwardFeedback) {
        if (showBackwardFeedback) {
            delay(1200)
            showBackwardFeedback = false
        }
    }

    LaunchedEffect(showForwardFeedback) {
        if (showForwardFeedback) {
            delay(1200)
            showForwardFeedback = false
        }
    }

    // Auto-hide controls overlay after 3.5s when playing
    LaunchedEffect(showOverlay, isSliding, isPlaying) {
        if (showOverlay && !isSliding && isPlaying) {
            delay(3500)
            showOverlay = false
        }
    }

    val readPosition = positionProvider ?: { position }
    val currentSubtitleLine by remember(syncedLyrics, positionProvider) {
        derivedStateOf {
            val activePos = if (isSliding) (sliderFraction * duration).toLong() else readPosition()
            if (syncedLyrics.isNotEmpty()) {
                syncedLyrics.lastOrNull { it.timeMs <= activePos }?.text.orEmpty()
            } else {
                ""
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── 1. Video Surface ──
        if (controller != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        resizeMode = videoResizeMode
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                },
                update = { playerView ->
                    playerView.player = controller
                    playerView.resizeMode = videoResizeMode
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── 2. Left / Right Double-Tap Gestures ──
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Half: Seek -5s
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .indication(backwardInteractionSource, ripple())
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { showOverlay = !showOverlay },
                            onDoubleTap = { offset ->
                                coroutineScope.launch {
                                    val press = PressInteraction.Press(offset)
                                    backwardInteractionSource.emit(press)
                                    controller?.let { c ->
                                        val target = (c.currentPosition - 5000L).coerceAtLeast(0L)
                                        c.seekTo(target)
                                    }
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    showBackwardFeedback = true
                                    backwardInteractionSource.emit(PressInteraction.Release(press))
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showBackwardFeedback,
                    enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.8f),
                    exit = fadeOut(tween(250))
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardDoubleArrowLeft,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "5 seconds",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Right Half: Seek +5s
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .indication(forwardInteractionSource, ripple())
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { showOverlay = !showOverlay },
                            onDoubleTap = { offset ->
                                coroutineScope.launch {
                                    val press = PressInteraction.Press(offset)
                                    forwardInteractionSource.emit(press)
                                    controller?.let { c ->
                                        val dur = if (c.duration > 0) c.duration else Long.MAX_VALUE
                                        val target = (c.currentPosition + 5000L).coerceAtMost(dur)
                                        c.seekTo(target)
                                    }
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    showForwardFeedback = true
                                    forwardInteractionSource.emit(PressInteraction.Release(press))
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showForwardFeedback,
                    enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.8f),
                    exit = fadeOut(tween(250))
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "5 seconds",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.KeyboardDoubleArrowRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── 3. Live Synced Subtitles (Placed above the slider) ──
        if (showCaptions && currentSubtitleLine.isNotBlank()) {
            Text(
                text = currentSubtitleLine,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.95f),
                        offset = Offset(0f, 2f),
                        blurRadius = 8f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (showOverlay) 54.dp else 20.dp)
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(0.85f)
            )
        }

        // ── 4. Controls Overlay (SimpMusic UI) ──
        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                // Top Bar: [<] [Title] [:]
                var showOptionsMenu by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.82f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onExitFullscreen,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .basicMarquee()
                        )

                        Spacer(Modifier.width(6.dp))

                        Box {
                            IconButton(
                                onClick = { showOptionsMenu = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false },
                                modifier = Modifier
                                    .background(Color(0xFF161722))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Aspect: " + when (videoResizeMode) {
                                                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "Fill"
                                                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL -> "Stretch"
                                                else -> "Fit (16:9)"
                                            },
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = {
                                        onToggleResizeMode()
                                        showOptionsMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (showCaptions) "Hide Captions" else "Show Captions",
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = {
                                        showCaptions = !showCaptions
                                        showOptionsMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Center Playback Row: [|<<]  [(5)]  [|| / >]  [(5)]  [>>|]
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip Previous
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            imageVector = SimpIcons.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Rewind 5s
                    IconButton(
                        onClick = {
                            controller?.let { c ->
                                val target = (c.currentPosition - 5000L).coerceAtLeast(0L)
                                c.seekTo(target)
                            }
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        },
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            imageVector = SimpIcons.Replay5,
                            contentDescription = "Rewind 5s",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Play / Pause (Bare white icon, no solid circle)
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    // Forward 5s
                    IconButton(
                        onClick = {
                            controller?.let { c ->
                                val dur = if (c.duration > 0) c.duration else Long.MAX_VALUE
                                val target = (c.currentPosition + 5000L).coerceAtMost(dur)
                                c.seekTo(target)
                            }
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        },
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            imageVector = SimpIcons.Forward5,
                            contentDescription = "Forward 5s",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Skip Next
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            imageVector = SimpIcons.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Bottom Bar: Full-width slider, [01:27 / 04:22] ... [Subtitles] [Exit Fullscreen]
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Sleek continuous scrub bar matching SimpMusic
                        ContinuousSeekBar(
                            position = position,
                            duration = duration,
                            accentColor = Color.White,
                            onSeek = onSeek,
                            formatDuration = formatDuration,
                            positionProvider = positionProvider
                        )

                        Spacer(Modifier.height(4.dp))

                        // Bottom Actions: [01:27 / 04:22] ... [Subtitles] [Exit]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val elapsedSec by remember(duration, positionProvider) {
                                derivedStateOf {
                                    val ms = if (isSliding) (sliderFraction * duration).toLong() else readPosition()
                                    (ms.coerceAtLeast(0L) / 1000L) * 1000L
                                }
                            }
                            Text(
                                text = "${formatDuration(elapsedSec)} / ${formatDuration(duration)}",
                                color = Color.White.copy(alpha = 0.90f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )

                            Spacer(Modifier.weight(1f))

                            // Subtitles toggle
                            IconButton(
                                onClick = { showCaptions = !showCaptions },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (showCaptions) SimpIcons.SubtitlesOff else SimpIcons.Subtitles,
                                    contentDescription = "Subtitles",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(10.dp))

                            // Exit Fullscreen
                            IconButton(
                                onClick = onExitFullscreen,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = SimpIcons.FullscreenExit,
                                    contentDescription = "Exit Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
