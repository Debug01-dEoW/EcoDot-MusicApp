package com.example.ecodot.ui.screens

import android.app.Activity
import androidx.compose.ui.text.style.TextAlign
import android.content.pm.ActivityInfo
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.ecodot.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.delay

// Helper to get Activity from Context
fun android.content.Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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
    val accentColor by viewModel.accentColor.collectAsState()
    val controller by viewModel.controller.collectAsState()
    val syncedLyrics by viewModel.syncedLyrics.collectAsState()

    var showCaptions by remember { mutableStateOf(false) }

    var isFullscreen by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    // Auto-hide controls after 3s of no interaction
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3000)
            showControls = false
        }
    }

    // Manage screen orientation for fullscreen
    LaunchedEffect(isFullscreen) {
        if (activity != null) {
            if (isFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                // Use WindowInsetsControllerCompat for modern hiding of system bars
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

    // Restore orientation when composable leaves
    DisposableEffect(Unit) {
        onDispose {
            if (activity != null) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(activity.window, true)
                androidx.core.view.WindowInsetsControllerCompat(activity.window, activity.window.decorView).show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── Video Surface ──────────────────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false // We draw our own controls overlay
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { playerView ->
                if (controller != null) {
                    playerView.player = controller
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                }
        )

        // ── Controls Overlay ───────────────────────────────────────────────────
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // ── Top Bar ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .then(
                            if (!isFullscreen) Modifier.statusBarsPadding()
                            else Modifier.padding(top = 8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close / Back button
                    IconButton(
                        onClick = {
                            if (isFullscreen) isFullscreen = false else onClose()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Rounded.KeyboardArrowLeft,
                            contentDescription = "Close Video",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = currentTrack?.title ?: "Video",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // ── Quality Settings Menu ──────────────────────────────────────
                    var showQualityMenu by remember { mutableStateOf(false) }
                    val currentVideoQuality by viewModel.videoQuality.collectAsState()

                    Box {
                        IconButton(
                            onClick = { showQualityMenu = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.MoreVert, "Quality Settings", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showQualityMenu,
                            onDismissRequest = { showQualityMenu = false },
                            modifier = Modifier.background(Color(0xFF1E1E1E))
                        ) {
                            Text(
                                "Video Quality",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            
                            val qualities = listOf(
                                com.example.ecodot.data.local.prefs.VideoQuality.HIGH to "High (1080p+)",
                                com.example.ecodot.data.local.prefs.VideoQuality.NORMAL to "Normal (720p)",
                                com.example.ecodot.data.local.prefs.VideoQuality.LOW to "Low (360p)"
                            )
                            
                            qualities.forEach { (q, label) ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (q == currentVideoQuality) {
                                                Icon(Icons.Rounded.Check, null, tint = com.example.ecodot.ui.theme.EcoDotRed, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                            } else {
                                                Spacer(Modifier.width(24.dp))
                                            }
                                            Text(label, color = if (q == currentVideoQuality) Color.White else Color.White.copy(0.7f))
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

                // ── Centre Transport Controls ──────────────────────────────────
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous
                    VideoControlButton(size = 40, onClick = { viewModel.skipPrevious() }) {
                        Icon(
                            Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Rewind 5s
                    VideoControlButton(size = 48, onClick = { viewModel.seekTo((position - 5000).coerceAtLeast(0)) }) {
                        Icon(
                            Icons.Rounded.Replay5,
                            contentDescription = "Rewind 5s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play / Pause (bigger, central)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { viewModel.togglePlayback() },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = {
                                (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith
                                (scaleOut(targetScale = 0.6f) + fadeOut())
                            },
                            label = "video_play_pause"
                        ) { playing ->
                            Icon(
                                imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    // Forward 5s
                    VideoControlButton(size = 48, onClick = { viewModel.seekTo((position + 5000).coerceAtMost(duration)) }) {
                        Icon(
                            Icons.Rounded.Forward5,
                            contentDescription = "Forward 5s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Next
                    VideoControlButton(size = 40, onClick = { viewModel.skipNext() }) {
                        Icon(
                            Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // ── Bottom Bar — Seek + Controls ─────────────────────────────
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .then(
                            if (!isFullscreen) Modifier.navigationBarsPadding()
                            else Modifier.padding(bottom = 8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Seek bar
                    VideoSeekBar(
                        position = position,
                        duration = duration,
                        accentColor = Color.White, // Making the seek bar white per the screenshot
                        onSeek = { viewModel.seekTo(it) },
                        formatDuration = { viewModel.formatDuration(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time labels
                        Text(
                            text = "${viewModel.formatDuration(position)} / ${viewModel.formatDuration(duration)}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Subtitles toggle
                            IconButton(
                                onClick = { showCaptions = !showCaptions },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (showCaptions) Icons.Rounded.ClosedCaption else Icons.Rounded.ClosedCaptionOff,
                                    contentDescription = "Toggle Captions",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            // Fullscreen toggle
                            IconButton(
                                onClick = { isFullscreen = !isFullscreen },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                                    contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Captions Overlay (Stays visible even when controls hide) ───────────
        val activeLyric = syncedLyrics?.lastOrNull { it.timeMs <= position }?.text
        if (showCaptions && activeLyric != null && activeLyric.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = if (showControls) {
                            if (isFullscreen) 110.dp else 150.dp
                        } else {
                            if (isFullscreen) 24.dp else 56.dp
                        },
                        start = 32.dp,
                        end = 32.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeLyric,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Tapping anywhere while controls hidden → show them
        if (!showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showControls = true }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VideoControlButton(
    size: Int,
    onClick: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
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

    Column(modifier = Modifier.fillMaxWidth()) {
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(if (isDragging) (dragFraction * safeDuration).toLong() else position),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = formatDuration(duration),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Custom Sleek linear seekbar
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(safeDuration) {
                    detectTapGestures(
                        onTap = { offset ->
                            dragFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            onSeek((dragFraction * safeDuration).toLong())
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
                val trackHeight = 4.dp.toPx()   
                val activeHeight = if (isDragging) 6.dp.toPx() else trackHeight
                
                // Inactive track
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.25f),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, cy - trackHeight / 2f),
                    size = androidx.compose.ui.geometry.Size(size.width, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
                )
                
                // Active track
                val filledWidth = displayFraction * size.width
                drawRoundRect(
                    color = accentColor,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, cy - activeHeight / 2f),
                    size = androidx.compose.ui.geometry.Size(filledWidth, activeHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(activeHeight / 2f)
                )
                
                // Thumb
                val thumbRadius = if (isDragging) 8.dp.toPx() else 6.dp.toPx()
                drawCircle(
                    color = Color.White,
                    radius = thumbRadius,
                    center = androidx.compose.ui.geometry.Offset(filledWidth, cy)
                )
            }
        }
    }
}
