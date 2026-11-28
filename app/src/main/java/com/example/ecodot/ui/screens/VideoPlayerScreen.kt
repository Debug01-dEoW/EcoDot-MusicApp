package com.example.ecodot.ui.screens
import com.example.ecodot.ui.theme.Check
import com.example.ecodot.ui.theme.ClosedCaption
import com.example.ecodot.ui.theme.ClosedCaptionOff
import com.example.ecodot.ui.theme.Fullscreen
import com.example.ecodot.ui.theme.FullscreenExit
import com.example.ecodot.ui.theme.KeyboardArrowDown
import com.example.ecodot.ui.theme.PictureInPictureAlt
import com.example.ecodot.ui.theme.Settings
import com.example.ecodot.ui.theme.SkipNext
import com.example.ecodot.ui.theme.SkipPrevious

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Rational
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.ecodot.LocalHazeState
import com.example.ecodot.data.local.prefs.VideoQuality
import com.example.ecodot.ui.components.LocalBackdrop
import com.example.ecodot.ui.components.LocalTiltState
import com.example.ecodot.ui.components.createSpecularRimBrush
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Pause
import com.example.ecodot.ui.theme.PlayArrow
import com.example.ecodot.ui.theme.Replay5
import com.example.ecodot.ui.theme.Forward5
import com.example.ecodot.ui.viewmodel.MusicViewModel
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
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Helper to get Activity from Context safely
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

enum class VideoResizeOption(val mode: Int, val label: String) {
    FIT(AspectRatioFrameLayout.RESIZE_MODE_FIT, "Fit"),
    FILL(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, "Fill"),
    STRETCH(AspectRatioFrameLayout.RESIZE_MODE_FILL, "Stretch")
}

/**
 * Authentic SimpMusic High-Performance Video Player with Liquid Glass Controls.
 * Features:
 *  - Fullscreen orientation lock & responsive PiP (Picture-in-Picture).
 *  - Double-tap left/right to seek ±10s with visual ripple feedback.
 *  - Quality selector menu (1080p / 720p / 360p).
 *  - Aspect ratio switcher (Fit / Fill / Stretch).
 *  - Real-time synced lyrics/captions overlay on video.
 *  - Automatic Video Mode stream negotiation (unthrottled muxed MP4/HLS).
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    viewModel: MusicViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val position by viewModel.position.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val controller by viewModel.controller.collectAsState()
    val syncedLyrics by viewModel.syncedLyrics.collectAsState()
    val tilt = LocalTiltState.current
    val backdrop = LocalBackdrop.current

    var showCaptions by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var resizeOption by remember { mutableStateOf(VideoResizeOption.FIT) }

    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedDragOffset by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
        label = "drag_dismiss_offset"
    )

    // Double-tap visual feedback state: "left" (-10s), "right" (+10s), or null
    var doubleTapFeedback by remember { mutableStateOf<String?>(null) }
    var doubleTapCount by remember { mutableIntStateOf(0) }

    // Ensure video stream is requested from YouTube service
    LaunchedEffect(Unit) {
        viewModel.setVideoMode(true)
    }

    LaunchedEffect(currentTrack?.id) {
        if (currentTrack != null) {
            viewModel.setVideoMode(true)
        }
    }

    // Auto-hide controls after 3.5s of no interaction while playing
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    // Clear double-tap feedback after brief delay
    LaunchedEffect(doubleTapCount) {
        if (doubleTapCount > 0) {
            delay(750)
            doubleTapFeedback = null
        }
    }

    // Manage screen orientation for fullscreen landscape
    LaunchedEffect(isFullscreen) {
        if (activity != null) {
            if (isFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(activity.window, false)
                androidx.core.view.WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                    hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(activity.window, true)
                androidx.core.view.WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                    show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                }
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    // Restore orientation and restore audio mode when leaving
    DisposableEffect(Unit) {
        onDispose {
            if (activity != null) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(activity.window, true)
                androidx.core.view.WindowInsetsControllerCompat(activity.window, activity.window.decorView).show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            viewModel.setVideoMode(false)
        }
    }

    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            viewModel.setVideoMode(false)
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, animatedDragOffset.toInt()) }
            .background(Color.Black)
            .pointerInput(isFullscreen) {
                if (!isFullscreen) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragOffsetY > 180f) {
                                viewModel.setVideoMode(false)
                                onClose()
                            } else {
                                dragOffsetY = 0f
                            }
                        },
                        onDragCancel = { dragOffsetY = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount > 0 || dragOffsetY > 0) {
                                dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                            }
                        }
                    )
                }
            }
    ) {
        // ── 1. Video Surface (ExoPlayer PlayerView) ───────────────────────────
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = resizeOption.mode
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { playerView ->
                playerView.resizeMode = resizeOption.mode
                if (controller != null && playerView.player != controller) {
                    playerView.player = controller
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── 2. Gesture Detector Layer (Double-tap & Single-tap) ───────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            val width = size.width
                            if (offset.x < width * 0.35f) {
                                // Double-tap left side: Rewind 5s
                                val newPos = (position - 5000L).coerceAtLeast(0L)
                                viewModel.seekTo(newPos)
                                doubleTapFeedback = "left"
                                doubleTapCount++
                            } else if (offset.x > width * 0.65f) {
                                // Double-tap right side: Forward 5s
                                val newPos = (position + 5000L).coerceAtMost(duration)
                                viewModel.seekTo(newPos)
                                doubleTapFeedback = "right"
                                doubleTapCount++
                            } else {
                                // Double-tap center: Play/Pause
                                viewModel.togglePlayback()
                            }
                        },
                        onTap = {
                            showControls = !showControls
                        }
                    )
                }
        )

        // ── 3. Double-Tap Ripple Feedback Overlays ────────────────────────────
        AnimatedVisibility(
            visible = doubleTapFeedback == "left",
            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = SimpIcons.Replay5,
                        contentDescription = "Rewind 5s",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "-5s",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = doubleTapFeedback == "right",
            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = SimpIcons.Forward5,
                        contentDescription = "Forward 5s",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "+5s",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── 4. Buffering / Loading Indicator ──────────────────────────────────
        val playbackState = controller?.playbackState
        val isBuffering = playbackState == Player.STATE_BUFFERING
        if (isBuffering) {
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = EcoDotRed,
                        strokeWidth = 3.dp
                    )
                }
            }
        }

        // ── 5. Synced Lyrics Overlay (Captions) ────────────────────────────────
        val activeLyric = syncedLyrics?.lastOrNull { it.timeMs <= position }?.text
        if (showCaptions && !activeLyric.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = if (showControls) {
                            if (isFullscreen) 110.dp else 150.dp
                        } else {
                            if (isFullscreen) 24.dp else 48.dp
                        },
                        start = 32.dp,
                        end = 32.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(0.75.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = activeLyric,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ── 6. Liquid Glass Controls Overlay ──────────────────────────────────
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(350))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.72f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.82f)
                            )
                        )
                    )
            ) {
                // ── Top Liquid Glass Bar ──────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .then(
                            if (!isFullscreen) Modifier.statusBarsPadding()
                            else Modifier.padding(top = 10.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close / Minimize Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable {
                                if (isFullscreen) isFullscreen = false else {
                                    viewModel.setVideoMode(false)
                                    onClose()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = SimpIcons.KeyboardArrowDown,
                            contentDescription = "Close Video",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Artist
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTrack?.title ?: "Music Video",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                        if (!currentTrack?.artist.isNullOrBlank()) {
                            Text(
                                text = currentTrack?.artist.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // ── Aspect Ratio Switcher (Fit / Fill / Stretch) ──────────
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable {
                                resizeOption = when (resizeOption) {
                                    VideoResizeOption.FIT -> VideoResizeOption.FILL
                                    VideoResizeOption.FILL -> VideoResizeOption.STRETCH
                                    VideoResizeOption.STRETCH -> VideoResizeOption.FIT
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = resizeOption.label,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // ── Picture-in-Picture Button ─────────────────────────────
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        IconButton(
                            onClick = {
                                activity?.enterPictureInPictureMode(
                                    PictureInPictureParams.Builder()
                                        .setAspectRatio(Rational(16, 9))
                                        .build()
                                )
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = SimpIcons.PictureInPictureAlt,
                                contentDescription = "PiP Mode",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // ── Quality Settings Dropdown Menu ─────────────────────────
                    var showQualityMenu by remember { mutableStateOf(false) }
                    val currentVideoQuality by viewModel.videoQuality.collectAsState()

                    Box {
                        IconButton(
                            onClick = { showQualityMenu = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = SimpIcons.Settings,
                                contentDescription = "Quality",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showQualityMenu,
                            onDismissRequest = { showQualityMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF181924))
                                .border(0.75.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = "STREAM QUALITY",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                            val qualities = listOf(
                                VideoQuality.HIGH to "High (1080p+)",
                                VideoQuality.NORMAL to "Normal (720p)",
                                VideoQuality.LOW to "Data Saver (360p)"
                            )
                            qualities.forEach { (q, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (q == currentVideoQuality) {
                                                Icon(
                                                    SimpIcons.Check,
                                                    null,
                                                    tint = EcoDotRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                            } else {
                                                Spacer(Modifier.width(24.dp))
                                            }
                                            Text(
                                                text = label,
                                                color = if (q == currentVideoQuality) Color.White else Color.White.copy(alpha = 0.7f),
                                                fontWeight = if (q == currentVideoQuality) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.setVideoQuality(q)
                                        viewModel.reloadCurrentTrack()
                                        showQualityMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ── Central Transport Controls (SimpMusic Style) ──────────────
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Track
                    VideoGlassButton(size = 44, onClick = { viewModel.skipPrevious() }) {
                        Icon(
                            imageVector = SimpIcons.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Rewind 5s
                    VideoGlassButton(size = 48, onClick = { viewModel.seekTo((position - 5000).coerceAtLeast(0)) }) {
                        Icon(
                            imageVector = SimpIcons.Replay5,
                            contentDescription = "Rewind 5s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Main Center Play / Pause Liquid Glass Button (74dp)
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(
                                width = 1.dp,
                                brush = createSpecularRimBrush(tilt.tiltX, tilt.tiltY, baseAlpha = 0.7f),
                                shape = CircleShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.togglePlayback() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = {
                                (scaleIn(initialScale = 0.6f) + fadeIn()).togetherWith(scaleOut(targetScale = 0.6f) + fadeOut())
                            },
                            label = "video_play_toggle"
                        ) { playing ->
                            Icon(
                                imageVector = if (playing) SimpIcons.Pause else SimpIcons.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    // Forward 5s
                    VideoGlassButton(size = 48, onClick = { viewModel.seekTo((position + 5000).coerceAtMost(duration)) }) {
                        Icon(
                            imageVector = SimpIcons.Forward5,
                            contentDescription = "Forward 5s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Next Track
                    VideoGlassButton(size = 44, onClick = { viewModel.skipNext() }) {
                        Icon(
                            imageVector = SimpIcons.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // ── Bottom Liquid Glass Transport Bar ─────────────────────────
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .then(
                            if (!isFullscreen) Modifier.navigationBarsPadding()
                            else Modifier.padding(bottom = 12.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    // Sleek Liquid Glass Seekbar
                    VideoSeekBar(
                        position = position,
                        duration = duration,
                        accentColor = EcoDotRed,
                        onSeek = { viewModel.seekTo(it) },
                        formatDuration = { viewModel.formatDuration(it) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current / Total Time
                        Text(
                            text = "${viewModel.formatDuration(position)} / ${viewModel.formatDuration(duration)}",
                            color = Color.White.copy(alpha = 0.90f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Subtitles / Captions Toggle
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (showCaptions) EcoDotRed.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f))
                                    .clickable { showCaptions = !showCaptions }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (showCaptions) SimpIcons.ClosedCaption else SimpIcons.ClosedCaptionOff,
                                    contentDescription = "Toggle Captions",
                                    tint = if (showCaptions) Color.White else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Fullscreen / Landscape Toggle Button
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .clickable { isFullscreen = !isFullscreen }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) SimpIcons.FullscreenExit else SimpIcons.Fullscreen,
                                    contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoGlassButton(
    size: Int,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.14f))
            .border(0.85.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun VideoSeekBar(
    position: Long,
    duration: Long,
    accentColor: Color,
    onSeek: (Long) -> Unit,
    formatDuration: (Long) -> String
) {
    val safeDuration = duration.coerceAtLeast(1L)
    val fraction = (position.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(fraction) }
    val displayFraction = if (isDragging) dragFraction else fraction

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(safeDuration) {
                detectTapGestures(
                    onTap = { offset ->
                        val newFrac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeek((newFrac * safeDuration).toLong())
                    }
                )
            }
            .pointerInput(safeDuration) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        onSeek((dragFraction * safeDuration).toLong())
                        isDragging = false
                    },
                    onDragCancel = { isDragging = false },
                    onHorizontalDrag = { _, dragAmount ->
                        dragFraction = (dragFraction + dragAmount / size.width.toFloat()).coerceIn(0f, 1f)
                    }
                )
            }
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val cy = size.height / 2f
            val trackHeight = 3.5.dp.toPx()
            val activeHeight = if (isDragging) 5.dp.toPx() else trackHeight

            // Background Track
            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(0f, cy - trackHeight / 2f),
                size = androidx.compose.ui.geometry.Size(size.width, trackHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
            )

            // Active Track
            val filledWidth = displayFraction * size.width
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF3B5C),
                        Color(0xFFFF6584)
                    )
                ),
                topLeft = Offset(0f, cy - activeHeight / 2f),
                size = androidx.compose.ui.geometry.Size(filledWidth, activeHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(activeHeight / 2f)
            )

            // Thumb
            val thumbRadius = if (isDragging) 8.dp.toPx() else 6.dp.toPx()
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(filledWidth, cy)
            )
        }
    }
}
