package com.example.ecodot.ui.screens

import android.content.pm.ActivityInfo
import com.example.ecodot.ui.components.player.*
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Shuffle
import com.example.ecodot.ui.theme.SkipPrevious
import com.example.ecodot.ui.theme.PlayArrow
import com.example.ecodot.ui.theme.Pause
import com.example.ecodot.ui.theme.SkipNext
import com.example.ecodot.ui.theme.Repeat
import com.example.ecodot.ui.theme.RepeatOne
import com.example.ecodot.ui.theme.Tune
import com.example.ecodot.ui.theme.Info
import com.example.ecodot.ui.theme.PictureInPictureAlt
import com.example.ecodot.ui.theme.PlaylistAdd
import com.example.ecodot.ui.theme.AccessAlarm
import com.example.ecodot.ui.theme.QueueMusic
import com.example.ecodot.ui.theme.Replay5
import com.example.ecodot.ui.theme.Forward5
import com.example.ecodot.ui.theme.Fullscreen
import com.example.ecodot.ui.theme.Subtitles
import com.example.ecodot.ui.theme.SubtitlesOff
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.ui.viewmodel.ArtistViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecodot.ui.theme.*
import com.example.ecodot.ui.components.*
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun NowPlayingAmbientBreathingBackdrop(
    isVinyl: Boolean,
    dominantColor: Color,
    accentColor: Color,
    ambientGlowAlpha: Float
) {
    val ambientTransition = rememberInfiniteTransition(label = "ambient_drift")
    val ambientDriftX = ambientTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_drift_x"
    )
    val ambientAlpha = ambientTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 0.96f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_alpha"
    )

    if (!isVinyl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val alphaMul = ambientAlpha.value
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.0f to dominantColor.copy(alpha = 0.80f * alphaMul),
                            0.36f to dominantColor.copy(alpha = 0.48f * alphaMul),
                            0.72f to Color(0xFF10111A),
                            1.0f to Color(0xFF08090E)
                        )
                    )
                    if (ambientGlowAlpha > 0.01f) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.40f * alphaMul * ambientGlowAlpha),
                                    dominantColor.copy(alpha = 0.25f * alphaMul * ambientGlowAlpha),
                                    Color.Transparent
                                ),
                                center = Offset(540f + ambientDriftX.value, 360f),
                                radius = 1200f
                            )
                        )
                    }
                }
        )
    } else {
        val staticVinylBg = remember(dominantColor) {
            Brush.verticalGradient(
                0.0f to Color(0xFF191716),
                0.45f to dominantColor.copy(alpha = 0.35f),
                0.85f to Color(0xFF0D0D10),
                1.0f to Color(0xFF070708)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(staticVinylBg)
        ) {
            if (ambientGlowAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .drawBehind {
                            val glowAlpha = ambientAlpha.value * ambientGlowAlpha * 0.85f
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        dominantColor.copy(alpha = 0.45f * glowAlpha),
                                        accentColor.copy(alpha = 0.20f * glowAlpha),
                                        Color.Transparent
                                    ),
                                    center = Offset(540f + ambientDriftX.value, 320f),
                                    radius = 1100f
                                )
                            )
                        }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun NowPlayingScreen(
    viewModel: MusicViewModel,
    navController: NavController? = null,
    onBackClick: () -> Unit,
    onEqualizerClick: () -> Unit,
    onWatchVideoClick: () -> Unit = {},
    isFullScreenVisible: Boolean = false,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val positionState = viewModel.position.collectAsState()
    val positionProvider: () -> Long = remember(positionState) { { positionState.value } }
    val duration by viewModel.duration.collectAsState()
    val shuffleMode by viewModel.shuffleMode.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val syncedLyrics by viewModel.syncedLyrics.collectAsState()
    val rawLyrics by viewModel.lyrics.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val isLiked by viewModel.isCurrentTrackLiked.collectAsState()
    val playbackQueue by viewModel.playbackQueue.collectAsState()
    val currentQueueIndex by viewModel.currentQueueIndex.collectAsState()
    val isLyricsLoading by viewModel.isLyricsLoading.collectAsState()
    val historyRecommendations by viewModel.historyRecommendations.collectAsState()
    val endlessMode by viewModel.endlessMode.collectAsState()
    val isCrossfading by viewModel.isCrossfading.collectAsState()
    val videoInfo by viewModel.videoInfo.collectAsState()
    val isVideoInfoLoading by viewModel.isVideoInfoLoading.collectAsState()
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    val controller by viewModel.controller.collectAsState()
    val isVideoMode by viewModel.isVideoMode.collectAsState()
    val videoAspectRatio by viewModel.videoAspectRatio.collectAsState()
    val activeStreamLabel by viewModel.activeStreamLabel.collectAsState()
    val playbackSource by viewModel.playbackSource.collectAsState()
    val audioQuality by viewModel.audioQuality.collectAsState()
    val canvasPlayer by viewModel.canvasPlayer.collectAsState()
    val playerStyle by viewModel.playerStyle.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showPlayerStyleSheet by remember { mutableStateOf(false) }
    var showAudioOutputSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showInfoSheet  by remember { mutableStateOf(false) }
    var showFullScreenLyrics by remember { mutableStateOf(false) }
    var showShareStorySheet by remember { mutableStateOf(false) }
    var optionsMenuTrack by remember { mutableStateOf<com.example.ecodot.data.local.entities.Track?>(null) }
    var optionsMenuQueueIndex by remember { mutableStateOf<Int?>(null) }
    var showPlaylistPicker by remember { mutableStateOf<com.example.ecodot.data.local.entities.Track?>(null) }
    var showRingtoneClipper by remember { mutableStateOf<com.example.ecodot.data.local.entities.Track?>(null) }
    var showVideoControls by remember { mutableStateOf(false) }
    var showCaptions by remember { mutableStateOf(true) }
    var isVideoFullscreen by remember { mutableStateOf(false) }
    var videoResizeMode by remember { mutableIntStateOf(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    val screenOffsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    val artworkOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val dismissOrReset: (Float, Float) -> Unit = { currentY, velocityY ->
        if (currentY > 100f || velocityY > 500f) {
            onBackClick()
            coroutineScope.launch {
                screenOffsetY.animateTo(
                    targetValue = 2200f,
                    animationSpec = tween(180, easing = FastOutLinearInEasing)
                )
                screenOffsetY.snapTo(0f)
            }
        } else {
            coroutineScope.launch {
                screenOffsetY.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                )
            }
        }
    }

    // Clean reset on screen appear
    LaunchedEffect(Unit) {
        screenOffsetY.snapTo(0f)
        artworkOffsetX.snapTo(0f)
    }

    // Watchdog: whenever list stops scrolling/dragging, if screen is pulled down, dismiss or spring back
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && screenOffsetY.value > 0f) {
            dismissOrReset(screenOffsetY.value, 0f)
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0 && screenOffsetY.value > 0f) {
                    val newOffset = (screenOffsetY.value + available.y).coerceAtLeast(0f)
                    val consumed = screenOffsetY.value - newOffset
                    coroutineScope.launch { screenOffsetY.snapTo(newOffset) }
                    return Offset(0f, -consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 0 && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                    val damping = (1f - (screenOffsetY.value / 3200f)).coerceIn(0.4f, 0.85f)
                    coroutineScope.launch {
                        screenOffsetY.snapTo((screenOffsetY.value + available.y * damping).coerceAtLeast(0f))
                    }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (screenOffsetY.value > 0f) {
                    dismissOrReset(screenOffsetY.value, available.y)
                    return available
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (screenOffsetY.value > 0f) {
                    dismissOrReset(screenOffsetY.value, available.y)
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    val artistViewModel: ArtistViewModel = viewModel()
    val artistUiState by artistViewModel.uiState.collectAsState()

    val currentArtistId = currentTrack?.artistId
    val currentArtistName = currentTrack?.artist
    LaunchedEffect(currentArtistId, currentArtistName) {
        if (!currentArtistId.isNullOrEmpty()) {
            artistViewModel.loadArtist(currentArtistId)
        } else if (!currentArtistName.isNullOrEmpty()) {
            artistViewModel.loadArtistByName(currentArtistName)
        }
    }

    val activity = remember(context) { context.findActivity() }

    LaunchedEffect(currentTrack, isVideoMode) {
        if (currentTrack != null && !viewModel.isTrackVideo(currentTrack) && !isVideoMode) {
            isVideoFullscreen = false
        }
    }

    LaunchedEffect(isVideoFullscreen) {
        if (activity != null) {
            if (isVideoFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(activity.window, false)
                androidx.core.view.WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                    hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(activity.window, true)
                androidx.core.view.WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                    show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                }
                activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (activity != null) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(activity.window, true)
                androidx.core.view.WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                    show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                }
                activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    LaunchedEffect(showVideoControls, isPlaying, currentTrack, isVideoMode) {
        if (showVideoControls && isPlaying && currentTrack?.isYouTube == true && isVideoMode) {
            kotlinx.coroutines.delay(3500)
            showVideoControls = false
        }
    }
    // Sync canvas playback state with main player
    LaunchedEffect(isPlaying) {
        viewModel.syncCanvasPlayback(isPlaying)
    }

    val isCanvasEnabledByUser by viewModel.isCanvasEnabledByUser.collectAsState()
    val networkState by viewModel.networkState.collectAsState()

    // Canvas ONLY sets up when explicitly enabled by the user; otherwise remains closed
    LaunchedEffect(currentTrack?.id, isCanvasEnabledByUser, isVideoMode, networkState.isPoorConnection) {
        val track = currentTrack
        if (!isCanvasEnabledByUser || isVideoMode || networkState.isPoorConnection) {
            viewModel.releaseCanvasPlayer()
        } else {
            val url = track?.canvasUrl
            if (!url.isNullOrEmpty() && url != "DISABLED") {
                viewModel.setupCanvasPlayer(context, url)
            } else if (track != null) {
                viewModel.enableCanvasForTrack(track)
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    var isCleanCanvasMode by remember { mutableStateOf(false) }

    LaunchedEffect(canvasPlayer) {
        if (canvasPlayer == null) isCleanCanvasMode = false
    }

    val screenDismissProgress by remember {
        derivedStateOf { (screenOffsetY.value / 1200f).coerceIn(0f, 1f) }
    }

    val isTrackAVideo = isVideoMode || viewModel.isTrackVideo(currentTrack)

    if (isVideoFullscreen && isTrackAVideo && currentTrack != null) {
        FullscreenVideoPlayer(
            track = currentTrack!!,
            controller = controller,
            position = 0L,
            positionProvider = positionProvider,
            duration = duration,
            isPlaying = isPlaying,
            syncedLyrics = syncedLyrics,
            videoResizeMode = videoResizeMode,
            onToggleResizeMode = {
                videoResizeMode = if (videoResizeMode == androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                } else {
                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            onExitFullscreen = { isVideoFullscreen = false },
            onPrevious = { viewModel.skipPrevious() },
            onNext = { viewModel.skipNext() },
            onPlayPause = { viewModel.togglePlayback() },
            onSeek = { viewModel.seekTo(it) },
            formatDuration = { viewModel.formatDuration(it) },
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, screenOffsetY.value.roundToInt().coerceAtLeast(0)) }
            .graphicsLayer {
                alpha = 1f - (screenDismissProgress * 0.20f)
                scaleX = 1f - (screenDismissProgress * 0.04f)
                scaleY = 1f - (screenDismissProgress * 0.04f)
            }
            .nestedScroll(nestedScrollConnection)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    if (screenOffsetY.value > 0f) {
                        dismissOrReset(screenOffsetY.value, 0f)
                    }
                }
            }
            .background(Color(0xFF070708)) // Slick OLED dark base
    ) {
        val activeTrackForStyle = currentTrack
        val resolvedStreamBadge = remember(activeStreamLabel, audioQuality, activeTrackForStyle?.id, downloadedTracks) {
            activeStreamLabel ?: PlayerStyleMath.formatAudioStreamBadge(
                bitrateBps = 0,
                sampleRateHz = 0,
                mimeType = null,
                codecs = null,
                fallbackQuality = audioQuality,
                isLocalOrDownloaded = activeTrackForStyle?.let { t ->
                    t.path.startsWith("/") || downloadedTracks.any { it.id == t.id }
                } ?: false
            )
        }
        val safeSeekBackward5s: () -> Unit = { viewModel.seekRelative(-5000L) }
        val safeSeekForward5s: () -> Unit = { viewModel.seekRelative(5000L) }
        val toggleVideoResizeMode: () -> Unit = {
            videoResizeMode = if (videoResizeMode == androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
            } else {
                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        }
        val navigateArtistFromPlayer: () -> Unit = {
            currentTrack?.artistId?.let { aid ->
                onBackClick()
                navController?.navigate("artist/$aid")
            }
        }

        if (activeTrackForStyle != null && playerStyle == com.example.ecodot.data.local.prefs.PlayerStyle.APPLE_MUSIC) {
            AppleMusicStyleContent(
                track = activeTrackForStyle,
                queue = playbackQueue,
                currentQueueIndex = currentQueueIndex,
                queueSourceTitle = playbackSource,
                controller = controller,
                isPlaying = isPlaying,
                isLiked = isLiked,
                shuffleMode = shuffleMode,
                repeatMode = repeatMode,
                endlessMode = endlessMode,
                position = 0L,
                positionProvider = positionProvider,
                duration = duration,
                dominantColor = dominantColor,
                accentColor = accentColor,
                syncedLyrics = syncedLyrics,
                plainLyrics = rawLyrics?.plainLyrics,
                isLyricsLoading = isLyricsLoading,
                isCrossfading = isCrossfading,
                isTrackAVideo = isTrackAVideo,
                videoAspectRatio = videoAspectRatio,
                videoResizeMode = videoResizeMode,
                streamBadgeLabel = activeStreamLabel,
                onBackClick = {
                    coroutineScope.launch { screenOffsetY.snapTo(0f) }
                    onBackClick()
                },
                onPlayPause = { viewModel.togglePlayback() },
                onPrevious = { viewModel.skipPrevious() },
                onNext = { viewModel.skipNext() },
                onSeekTo = { viewModel.seekTo(it) },
                onSelectQueueIndex = { idx -> viewModel.skipToQueueItem(idx) },
                onToggleLike = { viewModel.likeCurrentTrack() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.cycleRepeatMode() },
                onToggleEndlessMode = { viewModel.toggleEndlessMode() },
                onToggleVideoResizeMode = toggleVideoResizeMode,
                onEnterVideoFullscreen = { isVideoFullscreen = true },
                onOpenEqualizer = onEqualizerClick,
                onOpenPlaylistPicker = { showPlaylistPicker = activeTrackForStyle },
                onOpenFullLyrics = { showFullScreenLyrics = true },
                onOpenShareLyrics = { showShareStorySheet = true },
                onOpenTrackInfo = { showInfoSheet = true },
                onOpenOptionsMenu = {
                    optionsMenuQueueIndex = null
                    optionsMenuTrack = it
                },
                onOpenArtist = navigateArtistFromPlayer,
                onOpenOutputSheet = { showAudioOutputSheet = true },
                formatDuration = { viewModel.formatDuration(it) },
                modifier = Modifier.fillMaxSize()
            )
        } else if (activeTrackForStyle != null && playerStyle == com.example.ecodot.data.local.prefs.PlayerStyle.VINYL) {
            VinylStyleContent(
                track = activeTrackForStyle,
                controller = controller,
                isPlaying = isPlaying,
                isLiked = isLiked,
                shuffleMode = shuffleMode,
                repeatMode = repeatMode,
                sleepTimerRemaining = sleepTimerRemaining,
                position = 0L,
                positionProvider = positionProvider,
                duration = duration,
                dominantColor = dominantColor,
                accentColor = accentColor,
                syncedLyrics = syncedLyrics,
                plainLyrics = rawLyrics?.plainLyrics,
                isLyricsLoading = isLyricsLoading,
                isCrossfading = isCrossfading,
                isTrackAVideo = isTrackAVideo,
                videoAspectRatio = videoAspectRatio,
                videoResizeMode = videoResizeMode,
                streamBadgeLabel = resolvedStreamBadge,
                onBackClick = {
                    coroutineScope.launch { screenOffsetY.snapTo(0f) }
                    onBackClick()
                },
                onPlayPause = { viewModel.togglePlayback() },
                onPrevious = { viewModel.skipPrevious() },
                onNext = { viewModel.skipNext() },
                onSeekTo = { viewModel.seekTo(it) },
                onToggleLike = { viewModel.likeCurrentTrack() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.cycleRepeatMode() },
                onSetVideoMode = { viewModel.setVideoMode(it) },
                onToggleVideoResizeMode = toggleVideoResizeMode,
                onEnterVideoFullscreen = { isVideoFullscreen = true },
                onOpenEqualizer = onEqualizerClick,
                onOpenPlaylistPicker = { showPlaylistPicker = activeTrackForStyle },
                onOpenQueueSheet = { showQueueSheet = true },
                onOpenFullLyrics = { showFullScreenLyrics = true },
                onOpenTrackInfo = { showInfoSheet = true },
                onOpenSleepTimer = { showSleepTimerSheet = true },
                onOpenStylePicker = { showPlayerStyleSheet = true },
                onOpenOptionsMenu = {
                    optionsMenuQueueIndex = null
                    optionsMenuTrack = it
                },
                onOpenArtist = navigateArtistFromPlayer,
                onOpenOutputSheet = { showAudioOutputSheet = true },
                formatDuration = { viewModel.formatDuration(it) },
                modifier = Modifier.fillMaxSize()
            )
        } else if (activeTrackForStyle != null && playerStyle == com.example.ecodot.data.local.prefs.PlayerStyle.SPOTIFY) {
            val artistStatsSummary = remember(activeTrackForStyle.artist, artistUiState.subscribers) {
                getArtistStats(activeTrackForStyle.artist, artistUiState.subscribers).let { (monthly, _) ->
                    "$monthly monthly listeners"
                }
            }
            SpotifyStyleContent(
                track = activeTrackForStyle,
                queue = playbackQueue,
                currentQueueIndex = currentQueueIndex,
                controller = controller,
                isPlaying = isPlaying,
                isLiked = isLiked,
                shuffleMode = shuffleMode,
                repeatMode = repeatMode,
                position = 0L,
                positionProvider = positionProvider,
                duration = duration,
                dominantColor = dominantColor,
                accentColor = accentColor,
                syncedLyrics = syncedLyrics,
                plainLyrics = rawLyrics?.plainLyrics,
                isLyricsLoading = isLyricsLoading,
                isCrossfading = isCrossfading,
                isTrackAVideo = isTrackAVideo,
                videoAspectRatio = videoAspectRatio,
                videoResizeMode = videoResizeMode,
                streamBadgeLabel = resolvedStreamBadge,
                artistHeaderImageUrl = artistUiState.headerImageUrl,
                artistFollowersText = artistStatsSummary,
                onBackClick = {
                    coroutineScope.launch { screenOffsetY.snapTo(0f) }
                    onBackClick()
                },
                onPlayPause = { viewModel.togglePlayback() },
                onPrevious = { viewModel.skipPrevious() },
                onNext = { viewModel.skipNext() },
                onSeekTo = { viewModel.seekTo(it) },
                onSelectQueueIndex = { idx -> viewModel.skipToQueueItem(idx) },
                onToggleLike = { viewModel.likeCurrentTrack() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.cycleRepeatMode() },
                onSetVideoMode = { viewModel.setVideoMode(it) },
                onToggleVideoResizeMode = toggleVideoResizeMode,
                onEnterVideoFullscreen = { isVideoFullscreen = true },
                onOpenEqualizer = onEqualizerClick,
                onOpenPlaylistPicker = { showPlaylistPicker = activeTrackForStyle },
                onOpenQueueSheet = { showQueueSheet = true },
                onOpenFullLyrics = { showFullScreenLyrics = true },
                onOpenShareLyrics = { showShareStorySheet = true },
                onOpenTrackInfo = { showInfoSheet = true },
                onOpenStylePicker = { showPlayerStyleSheet = true },
                onOpenOptionsMenu = {
                    optionsMenuQueueIndex = null
                    optionsMenuTrack = it
                },
                onOpenArtist = navigateArtistFromPlayer,
                onOpenOutputSheet = { showAudioOutputSheet = true },
                formatDuration = { viewModel.formatDuration(it) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
        // Background layers
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
        // ── CANVAS: Looping background video (Spotify-style) ──────────────────
        // ── CANVAS: Looping background video (Spotify-style) ──────────────────
        androidx.compose.animation.AnimatedVisibility(
            visible = canvasPlayer != null,
            enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(800)),
            exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(400))
        ) {
            val canvasAlpha by animateFloatAsState(if (isCleanCanvasMode) 1f else 0.55f)
            val scrimAlpha by animateFloatAsState(if (isCleanCanvasMode) 0f else 1f)

            canvasPlayer?.let { player ->
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.view.LayoutInflater.from(ctx).inflate(
                            com.example.ecodot.R.layout.canvas_player_view,
                            null
                        ) as androidx.media3.ui.PlayerView
                    },
                    update = { playerView -> playerView.player = player },
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(canvasAlpha) // 1.0 in clean mode, 0.55 in normal mode
                )
            }
            // Extra dark scrim so UI elements remain readable (hidden in clean mode)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(scrimAlpha)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }

        val ambientGlowAlpha by animateFloatAsState(if (isCleanCanvasMode) 0f else 1f)

        when (playerStyle) {
            com.example.ecodot.data.local.prefs.PlayerStyle.APPLE_MUSIC -> {
                // Smooth fallback Apple gradient (uses 18dp lightweight blur instead of 90dp full-screen stall)
                val blurMod = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    Modifier.blur(18.dp)
                } else Modifier
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    dominantColor.copy(alpha = 0.85f),
                                    accentColor.copy(alpha = 0.65f),
                                    Color(0xFF1E1035),
                                    accentColor.copy(alpha = 0.65f),
                                    dominantColor.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .then(blurMod)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                )
            }
            com.example.ecodot.data.local.prefs.PlayerStyle.SPOTIFY -> {
                // Minimal Spotify pitch black with top tint
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    dominantColor.copy(alpha = 0.45f),
                                    dominantColor.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            com.example.ecodot.data.local.prefs.PlayerStyle.ECODOT -> {
                // Isolated drawBehind ambient breathing glow (0 parent recompositions at 120Hz)
                NowPlayingAmbientBreathingBackdrop(
                    isVinyl = false,
                    dominantColor = dominantColor,
                    accentColor = accentColor,
                    ambientGlowAlpha = ambientGlowAlpha
                )
            }
            com.example.ecodot.data.local.prefs.PlayerStyle.VINYL -> {
                // Vintage vinyl turntable ambiance with warm radial glow
                NowPlayingAmbientBreathingBackdrop(
                    isVinyl = true,
                    dominantColor = dominantColor,
                    accentColor = accentColor,
                    ambientGlowAlpha = ambientGlowAlpha
                )
            }
        }
        }

        val isScrolledPastArtwork by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

        CompositionLocalProvider(com.example.ecodot.ui.components.LocalBackdrop provides null) {
            Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isCleanCanvasMode,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut()
                ) {
                    val topBarBgColor by animateColorAsState(
                        targetValue = if (isScrolledPastArtwork) Color(0xFF070708) else Color.Transparent,
                        animationSpec = tween(300),
                        label = "topBarBg"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(topBarBgColor)
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .pointerInput(Unit) {
                                val view = this
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val velocityTracker = VelocityTracker()
                                    velocityTracker.addPosition(down.uptimeMillis, down.position)

                                    drag(down.id) { change ->
                                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                                        val dragDelta = change.position - change.previousPosition
                                        if (dragDelta.y > 0 || screenOffsetY.value > 0) {
                                            change.consume()
                                            val damping = (1f - (screenOffsetY.value / 3200f)).coerceIn(0.4f, 0.9f)
                                            coroutineScope.launch {
                                                screenOffsetY.snapTo((screenOffsetY.value + dragDelta.y * damping).coerceAtLeast(0f))
                                            }
                                        }
                                    }

                                    val velocityY = velocityTracker.calculateVelocity().y
                                    val currentY = screenOffsetY.value
                                    dismissOrReset(currentY, velocityY)
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            coroutineScope.launch { screenOffsetY.snapTo(0f) }
                            onBackClick()
                        }) {
                            Icon(
                                Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Minimize",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "NOW PLAYING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = poppinsFamily,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val topBarSubtitle = remember(currentTrack, isTrackAVideo) {
                                when {
                                    isTrackAVideo -> "Music Video"
                                    else -> {
                                        val album = currentTrack?.album.orEmpty()
                                        val isViewCount = album.contains("view", ignoreCase = true)
                                        val isLikeCount = Regex("""\b\d[\d.,]*\s*[kmb]?\s*likes?\b""", RegexOption.IGNORE_CASE).containsMatchIn(album)
                                        val isGenericVideo = album.equals("YouTube Video", ignoreCase = true) ||
                                            album.equals("Video", ignoreCase = true) ||
                                            album.equals("Music Video", ignoreCase = true) ||
                                            album.equals("Video Chart", ignoreCase = true)
                                        val isGeneric = album.startsWith("Search", ignoreCase = true) ||
                                            album.equals("Top Result", ignoreCase = true) ||
                                            album.equals("Unknown", ignoreCase = true) ||
                                            album.equals("Up Next", ignoreCase = true) ||
                                            album.equals("Cached", ignoreCase = true) ||
                                            album.equals("Cache", ignoreCase = true) ||
                                            album.equals("YouTube Music", ignoreCase = true) ||
                                            album.isBlank()

                                        if (!isViewCount && !isLikeCount && !isGenericVideo && !isGeneric) album
                                        else currentTrack?.artist?.takeIf { it.isNotBlank() && it != "Unknown" }
                                            ?: "EcoDot Music"
                                    }
                                }
                            }
                            Text(
                                text = topBarSubtitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = poppinsFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee()
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.animation.AnimatedVisibility(visible = isScrolledPastArtwork, enter = fadeIn(), exit = fadeOut()) {
                                IconButton(onClick = { viewModel.togglePlayback() }) {
                                    Icon(
                                        imageVector = if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { showPlayerStyleSheet = true }) {
                                Icon(
                                    Icons.Rounded.Style,
                                    contentDescription = "Switch player style",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                            IconButton(onClick = { currentTrack?.let { optionsMenuTrack = it } }) {
                                Icon(
                                    Icons.Rounded.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            currentTrack?.let { track ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        userScrollEnabled = !isCleanCanvasMode
                    ) {

                        // 1. Artwork Squircle with horizontal swipe to skip & drag down to minimize
                        item {
                            if (canvasPlayer != null) {
                                val view = androidx.compose.ui.platform.LocalView.current
                                val coroutineScope = rememberCoroutineScope()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isCleanCanvasMode) 1000.dp else 340.dp)
                                        .pointerInput(Unit) {
                                            awaitEachGesture {
                                                val down = awaitFirstDown(requireUnconsumed = false)
                                                val velocityTracker = VelocityTracker()
                                                velocityTracker.addPosition(down.uptimeMillis, down.position)
                                                var isHorizontalDrag: Boolean? = null
                                                var accumulatedX = 0f
                                                var accumulatedY = 0f

                                                drag(down.id) { change ->
                                                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                                                    val dragDelta = change.position - change.previousPosition
                                                    accumulatedX += dragDelta.x
                                                    accumulatedY += dragDelta.y

                                                    if (isHorizontalDrag == null) {
                                                        val absX = kotlin.math.abs(accumulatedX)
                                                        val absY = kotlin.math.abs(accumulatedY)
                                                        if (absX > 10f || absY > 10f) {
                                                            isHorizontalDrag = absX >= absY
                                                        }
                                                    }

                                                    if (isHorizontalDrag == true) {
                                                        change.consume()
                                                        val damping = (1f - (kotlin.math.abs(artworkOffsetX.value) / 2200f)).coerceIn(0.45f, 0.95f)
                                                        coroutineScope.launch {
                                                            artworkOffsetX.snapTo(artworkOffsetX.value + dragDelta.x * damping)
                                                        }
                                                    } else if (isHorizontalDrag == false) {
                                                        if (accumulatedY > 0f || screenOffsetY.value > 0f) {
                                                            change.consume()
                                                            val damping = (1f - (screenOffsetY.value / 3200f)).coerceIn(0.4f, 0.9f)
                                                            coroutineScope.launch {
                                                                screenOffsetY.snapTo((screenOffsetY.value + dragDelta.y * damping).coerceAtLeast(0f))
                                                            }
                                                        }
                                                    }
                                                }

                                                val totalDist = kotlin.math.sqrt(accumulatedX * accumulatedX + accumulatedY * accumulatedY)
                                                if (totalDist < 12f) {
                                                    isCleanCanvasMode = !isCleanCanvasMode
                                                } else if (isHorizontalDrag == true) {
                                                    val velocityX = velocityTracker.calculateVelocity().x
                                                    val curX = artworkOffsetX.value
                                                    coroutineScope.launch {
                                                        if (curX < -90f || velocityX < -750f) {
                                                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                            artworkOffsetX.animateTo(-600f, tween(130, easing = FastOutLinearInEasing))
                                                            viewModel.skipNext()
                                                            artworkOffsetX.snapTo(600f)
                                                            artworkOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                                                        } else if (curX > 90f || velocityX > 750f) {
                                                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                            artworkOffsetX.animateTo(600f, tween(130, easing = FastOutLinearInEasing))
                                                            viewModel.skipPrevious()
                                                            artworkOffsetX.snapTo(-600f)
                                                            artworkOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                                                        } else {
                                                            artworkOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                                                        }
                                                    }
                                                } else if (isHorizontalDrag == false) {
                                                    val velocityY = velocityTracker.calculateVelocity().y
                                                    val curY = screenOffsetY.value
                                                    if (curY > 120f || velocityY > 700f) {
                                                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                    }
                                                    dismissOrReset(curY, velocityY)
                                                }
                                            }
                                        }
                                )
                            } else {
                                val view = androidx.compose.ui.platform.LocalView.current
                                val coroutineScope = rememberCoroutineScope()
                                val isVideo = isTrackAVideo
                                val widthFraction = if (isVideo) 0.94f else 0.88f
                                val aspectRatio = if (isVideo) 16f / 9f else 1f
                                val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
                                val estimatedHeight = screenWidth * widthFraction / aspectRatio
                                val cornerRadius = if (isVideo) 16.dp else 12.dp
                                val cardShape = RoundedCornerShape(cornerRadius)

                                val artworkScale by animateFloatAsState(
                                    targetValue = if (isPlaying) 1.0f else 0.94f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "artwork_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (isVideo) 2.dp else 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                            // Ambient Light Aura directly behind 16:9 Video Card (soft radial gradient — zero per-frame RenderEffect blur)
                                    if (isVideo) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth((widthFraction + 0.08f).coerceAtMost(1f))
                                                .height(estimatedHeight + 36.dp)
                                                .offset { IntOffset(artworkOffsetX.value.roundToInt(), 0) }
                                                .drawBehind {
                                                    drawRoundRect(
                                                        brush = Brush.radialGradient(
                                                            colors = listOf(
                                                                dominantColor.copy(alpha = 0.42f),
                                                                accentColor.copy(alpha = 0.18f),
                                                                Color.Transparent
                                                            )
                                                        ),
                                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx(), 32.dp.toPx())
                                                    )
                                                }
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(widthFraction)
                                            .height(estimatedHeight)
                                            .offset { IntOffset(artworkOffsetX.value.roundToInt(), 0) }
                                            .graphicsLayer {
                                                scaleX = artworkScale * (1f - (kotlin.math.abs(artworkOffsetX.value) / 3000f)).coerceIn(0.92f, 1f)
                                                scaleY = artworkScale * (1f - (kotlin.math.abs(artworkOffsetX.value) / 3000f)).coerceIn(0.92f, 1f)
                                                alpha = (1f - (kotlin.math.abs(artworkOffsetX.value) / 900f) * 0.40f).coerceIn(0.60f, 1f)
                                            }
                                            .shadow(
                                                elevation = 16.dp,
                                                shape = cardShape,
                                                spotColor = Color.Black.copy(alpha = 0.65f),
                                                ambientColor = Color.Transparent
                                            )
                                            .clip(cardShape)
                                            .background(Color.Black)
                                        .then(
                                            if (!isVideo) {
                                                Modifier.pointerInput(Unit) {
                                                    awaitEachGesture {
                                                        val down = awaitFirstDown(requireUnconsumed = false)
                                                        val velocityTracker = VelocityTracker()
                                                        velocityTracker.addPosition(down.uptimeMillis, down.position)
                                                        var isHorizontalDrag: Boolean? = null
                                                        var accumulatedX = 0f
                                                        var accumulatedY = 0f

                                                        drag(down.id) { change ->
                                                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                                                            val dragDelta = change.position - change.previousPosition
                                                            accumulatedX += dragDelta.x
                                                            accumulatedY += dragDelta.y

                                                            if (isHorizontalDrag == null) {
                                                                val absX = kotlin.math.abs(accumulatedX)
                                                                val absY = kotlin.math.abs(accumulatedY)
                                                                if (absX > 10f || absY > 10f) {
                                                                    isHorizontalDrag = absX >= absY
                                                                }
                                                            }

                                                            if (isHorizontalDrag == true) {
                                                                change.consume()
                                                                val damping = (1f - (kotlin.math.abs(artworkOffsetX.value) / 2200f)).coerceIn(0.45f, 0.95f)
                                                                coroutineScope.launch {
                                                                    artworkOffsetX.snapTo(artworkOffsetX.value + dragDelta.x * damping)
                                                                }
                                                            } else if (isHorizontalDrag == false) {
                                                                if (accumulatedY > 0f || screenOffsetY.value > 0f) {
                                                                    change.consume()
                                                                    val damping = (1f - (screenOffsetY.value / 3200f)).coerceIn(0.4f, 0.9f)
                                                                    coroutineScope.launch {
                                                                        screenOffsetY.snapTo((screenOffsetY.value + dragDelta.y * damping).coerceAtLeast(0f))
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        val totalDist = kotlin.math.sqrt(accumulatedX * accumulatedX + accumulatedY * accumulatedY)
                                                        if (totalDist < 12f) {
                                                            // Tap on artwork
                                                        } else if (isHorizontalDrag == true) {
                                                            val velocityX = velocityTracker.calculateVelocity().x
                                                            val curX = artworkOffsetX.value
                                                            coroutineScope.launch {
                                                                if (curX < -90f || velocityX < -750f) {
                                                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                                    artworkOffsetX.animateTo(-600f, tween(130, easing = FastOutLinearInEasing))
                                                                    viewModel.skipNext()
                                                                    artworkOffsetX.snapTo(600f)
                                                                    artworkOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                                                                } else if (curX > 90f || velocityX > 750f) {
                                                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                                    artworkOffsetX.animateTo(600f, tween(130, easing = FastOutLinearInEasing))
                                                                    viewModel.skipPrevious()
                                                                    artworkOffsetX.snapTo(-600f)
                                                                    artworkOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                                                                } else {
                                                                    artworkOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                                                                }
                                                            }
                                                        } else if (isHorizontalDrag == false) {
                                                            val velocityY = velocityTracker.calculateVelocity().y
                                                            val curY = screenOffsetY.value
                                                            if (curY > 120f || velocityY > 700f) {
                                                                view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                            }
                                                            dismissOrReset(curY, velocityY)
                                                        }
                                                    }
                                                }
                                            } else {
                                                Modifier.clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    showVideoControls = !showVideoControls
                                                }
                                            }
                                        )
                                ) {
                                    var currentArtworkUrl by remember(track.id, track.albumArtUri) {
                                        mutableStateOf(
                                            com.example.ecodot.data.remote.YouTubeParser.getHighResUrl(
                                                track.albumArtUri,
                                                isWide = isVideo
                                            ) ?: track.albumArtUri
                                        )
                                    }
                                    if (playerStyle == com.example.ecodot.data.local.prefs.PlayerStyle.VINYL && !isVideo) {
                                        VinylArtworkView(
                                            imageUrl = currentArtworkUrl,
                                            isPlaying = isPlaying,
                                            dominantColor = dominantColor,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        AsyncImage(
                                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                .data(currentArtworkUrl)
                                                .size(960, 960)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = track.title,
                                            placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.MusicNote),
                                            error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.MusicNote),
                                            onError = {
                                                com.example.ecodot.data.remote.YouTubeParser.getFallbackThumbUrl(currentArtworkUrl)?.let { fallback ->
                                                    currentArtworkUrl = fallback
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    if (isVideo && controller != null) {
                                        androidx.compose.ui.viewinterop.AndroidView(
                                            factory = { ctx ->
                                                (android.view.LayoutInflater.from(ctx).inflate(
                                                    com.example.ecodot.R.layout.canvas_player_view,
                                                    null
                                                ) as androidx.media3.ui.PlayerView).apply {
                                                    useController = false
                                                    resizeMode = videoResizeMode
                                                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                                                    keepScreenOn = true
                                                }
                                            },
                                            update = { playerView ->
                                                playerView.player = controller
                                                playerView.resizeMode = videoResizeMode
                                            },
                                            onRelease = { playerView ->
                                                playerView.player = null
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // In-video Controls Overlay (SimpMusic: Fullscreen, Replay5, Forward5, Subtitles)
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = showVideoControls,
                                            enter = androidx.compose.animation.fadeIn(tween(180)),
                                            exit = androidx.compose.animation.fadeOut(tween(220)),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.45f))
                                            ) {
                                                // Top-Right: Fullscreen orientation toggle
                                                IconButton(
                                                    onClick = {
                                                        isVideoFullscreen = !isVideoFullscreen
                                                    },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(6.dp)
                                                        .size(42.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = SimpIcons.Fullscreen,
                                                        contentDescription = "Fullscreen",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }

                                                // Center: Rewind 5s and Fast-forward 5s
                                                Row(
                                                    modifier = Modifier
                                                        .align(Alignment.Center)
                                                        .fillMaxWidth(0.68f),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            controller?.let { c ->
                                                                val target = (c.currentPosition - 5000L).coerceAtLeast(0L)
                                                                c.seekTo(target)
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(CircleShape)
                                                    ) {
                                                        Icon(
                                                            imageVector = SimpIcons.Replay5,
                                                            contentDescription = "Rewind 5s",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(36.dp)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            controller?.let { c ->
                                                                val dur = if (c.duration > 0) c.duration else Long.MAX_VALUE
                                                                val target = (c.currentPosition + 5000L).coerceAtMost(dur)
                                                                c.seekTo(target)
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(CircleShape)
                                                    ) {
                                                        Icon(
                                                            imageVector = SimpIcons.Forward5,
                                                            contentDescription = "Forward 5s",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(36.dp)
                                                        )
                                                    }
                                                }

                                                // Bottom-Right: Subtitles toggle
                                                IconButton(
                                                    onClick = {
                                                        showCaptions = !showCaptions
                                                    },
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(6.dp)
                                                        .size(42.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (showCaptions) SimpIcons.SubtitlesOff else SimpIcons.Subtitles,
                                                        contentDescription = "Subtitles",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Live Synced Subtitles/Captions pill on video when enabled
                                        if (showCaptions) {
                                            val currentCapLine by remember(syncedLyrics, positionProvider) {
                                                derivedStateOf {
                                                    val pos = positionProvider()
                                                    syncedLyrics.lastOrNull { it.timeMs <= pos }?.text.orEmpty()
                                                }
                                            }
                                            if (currentCapLine.isNotBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .padding(bottom = 12.dp, start = 14.dp, end = 14.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.Black.copy(alpha = 0.65f))
                                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                                ) {
                                                    Text(
                                                        text = currentCapLine,
                                                        color = Color.White,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontFamily = poppinsFamily,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 2
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                } // Closes outer centering Box for ambient halo & video card
                            }
                        }

                        // 2. Inline Lyric Line (SimpMusic: Only when NOT in video mode)
                        if (!isTrackAVideo) {
                            item {
                                val activeLyricLine by remember(syncedLyrics, positionProvider) {
                                    derivedStateOf {
                                        val pos = positionProvider()
                                        syncedLyrics.lastOrNull { it.timeMs <= pos }?.text.orEmpty()
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .padding(horizontal = 24.dp)
                                        .clickable { showFullScreenLyrics = true },
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Crossfade(
                                        targetState = activeLyricLine,
                                        animationSpec = tween(durationMillis = 280),
                                        label = "inline_lyric_crossfade"
                                    ) { lineText ->
                                        if (lineText.isNotBlank()) {
                                            Text(
                                                text = lineText,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontFamily = poppinsFamily,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = Color.White,
                                                textAlign = TextAlign.Start,
                                                maxLines = 1,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .basicMarquee()
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }

                        // 3. Track Info Row & Like Button (SimpMusic Exact)
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title.ifEmpty { "Unknown Title" },
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = poppinsFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        modifier = Modifier.basicMarquee()
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                                            ExplicitBadge(modifier = Modifier.padding(end = 6.dp))
                                        }
                                        Text(
                                            text = track.artist.ifEmpty { "Unknown Artist" },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = poppinsFamily,
                                                fontSize = 14.sp
                                            ),
                                            color = Color.White.copy(alpha = 0.65f),
                                            maxLines = 1,
                                            modifier = Modifier
                                                .basicMarquee()
                                                .clickable(enabled = !track.artistId.isNullOrEmpty()) {
                                                    track.artistId?.let { artistId ->
                                                        onBackClick()
                                                        navController?.navigate("artist/$artistId")
                                                    }
                                                }
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.likeCurrentTrack() }) {
                                    val heartScale by animateFloatAsState(
                                        targetValue = if (isLiked) 1.2f else 1f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        label = "heart_scale"
                                    )
                                    Icon(
                                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isLiked) Color(0xFFFF1744) else Color.White,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .scale(heartScale)
                                    )
                                }
                            }
                        }

                        // 4. Progress Seek Bar (SimpMusic Continuous Slider)
                        item {
                            Spacer(modifier = Modifier.height(18.dp))
                            ContinuousSeekBar(
                                position = 0L,
                                positionProvider = positionProvider,
                                duration = duration,
                                accentColor = accentColor,
                                onSeek = { viewModel.seekTo(it) },
                                formatDuration = { viewModel.formatDuration(it) },
                                isCrossfading = isCrossfading,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }

                        // 5. Playback Controls Row (SimpMusic Layout: Shuffle, Previous, Large Play Button, Next, Repeat)
                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Shuffle
                                AnimatedControlIcon(
                                    icon = SimpIcons.Shuffle,
                                    active = shuffleMode,
                                    activeColor = accentColor,
                                    size = 24,
                                    onClick = { viewModel.toggleShuffle() }
                                )

                                // Previous
                                AnimatedControlIcon(
                                    icon = SimpIcons.SkipPrevious,
                                    active = false,
                                    activeColor = Color.White,
                                    size = 38,
                                    isGlassCapsule = false,
                                    onClick = { viewModel.skipPrevious() }
                                )

                                // Large White Circular Play/Pause Button (SimpMusic Exact)
                                PremiumPlayButton(
                                    isPlaying = isPlaying,
                                    accentColor = accentColor,
                                    size = 70,
                                    onClick = { viewModel.togglePlayback() }
                                )

                                // Next
                                AnimatedControlIcon(
                                    icon = SimpIcons.SkipNext,
                                    active = false,
                                    activeColor = Color.White,
                                    size = 38,
                                    isGlassCapsule = false,
                                    onClick = { viewModel.skipNext() }
                                )

                                // Repeat
                                AnimatedControlIcon(
                                    icon = if (repeatMode == Player.REPEAT_MODE_ONE)
                                        SimpIcons.RepeatOne
                                    else SimpIcons.Repeat,
                                    active = repeatMode != Player.REPEAT_MODE_OFF,
                                    activeColor = accentColor,
                                    size = 24,
                                    onClick = { viewModel.cycleRepeatMode() }
                                )
                            }
                        }

                        // 6. Bottom Action Quick Row (SimpMusic Exact: Info, Headphones, Lyrics on Left | PlaylistAdd, Queue on Right)
                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .height(36.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left cluster: Info + Audio Output/Equalizer + Lyrics
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { showInfoSheet = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = SimpIcons.Info,
                                            contentDescription = "Track Info",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = onEqualizerClick,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = SimpIcons.Headphones,
                                            contentDescription = "Audio Output / Equalizer",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { showFullScreenLyrics = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = SimpIcons.CommentOutline,
                                            contentDescription = "Lyrics / Subtitles",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                // Right cluster: PlaylistAdd + Queue
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { showPlaylistPicker = track },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = SimpIcons.PlaylistAdd,
                                            contentDescription = "Add to Playlist",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { showQueueSheet = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = SimpIcons.QueueMusic,
                                            contentDescription = "Queue",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 7. SimpMusic-Style Synced Lyrics Card
                        item {
                            Spacer(modifier = Modifier.height(48.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                dominantColor.copy(alpha = 0.52f),
                                                Color(0xFF13141D)
                                            )
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                    .clickable { showFullScreenLyrics = true }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp, vertical = 14.dp)
                                ) {
                                    // Subtle SimpMusic pill drag handle
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .width(36.dp)
                                            .height(4.dp)
                                            .background(Color.White.copy(alpha = 0.35f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Header: Lyrics, Share, and "SHOW" button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Lyrics",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = poppinsFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = Color.White
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = { showShareStorySheet = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Share,
                                                    contentDescription = "Share",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(Color.White.copy(alpha = 0.16f))
                                                    .clickable { showFullScreenLyrics = true }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Rounded.Fullscreen,
                                                        contentDescription = "Expand",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Text(
                                                        text = "EXPAND",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontFamily = poppinsFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            letterSpacing = 0.8.sp
                                                        ),
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Lyrics Content
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(310.dp)
                                    ) {
                                        LyricsPanel(
                                            syncedLyrics = syncedLyrics,
                                            currentPosition = 0L,
                                            positionProvider = positionProvider,
                                            plainLyrics = rawLyrics?.plainLyrics,
                                            isLyricsLoading = isLyricsLoading,
                                            onSeek = { viewModel.seekTo(it) },
                                            isFullScreen = false,
                                            backgroundColor = Color.Transparent,
                                            accentColor = accentColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Card Footer: Line Synced • Synced by LRCLIB
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (syncedLyrics.isNotEmpty()) "Line synced • Synced by LRCLIB" else "Lyrics provided by LRCLIB",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = poppinsFamily,
                                                fontSize = 11.sp
                                            ),
                                            color = Color.White.copy(alpha = 0.45f)
                                        )
                                    }
                                }
                            }
                        }

                    // 7. About the Artist Card
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        val stats = remember(track.artist, artistUiState.subscribers) {
                            getArtistStats(track.artist, artistUiState.subscribers)
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .height(280.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                                .clickable {
                                    track.artistId?.let { aid ->
                                        onBackClick()
                                        navController?.navigate("artist/$aid")
                                    }
                                }
                        ) {
                            // Background profile photo or dark placeholder fallback
                            val imageUrl = artistUiState.headerImageUrl
                            if (!imageUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                        .data(imageUrl).crossfade(true).build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White.copy(alpha = 0.02f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.06f),
                                        modifier = Modifier.size(100.dp)
                                    )
                                }
                            }
                            
                            // Dark gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.3f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Seeded World Rank Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(percent = 50))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = stats.second.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                    
                                    Text(
                                        text = "ABOUT THE ARTIST",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp,
                                            letterSpacing = 1.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                                
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = track.artist,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = stats.first,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (artistUiState.bio.isNotEmpty()) {
                                        Text(
                                            text = artistUiState.bio,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.55f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 8. Credits Card
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "CREDITS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 1.5.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        
                        val songCredits = remember(track.title, track.artist) {
                            getSeededCredits(track.title, track.artist)
                        }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(0.04f))
                                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(20.dp))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            songCredits.forEach { (label, name) ->
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(0.45f)
                                    )
                                    Text(
                                        text = name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        // ── Clean UI Overlay (Enabled when isCleanCanvasMode == true) ──
        androidx.compose.animation.AnimatedVisibility(
            visible = isCleanCanvasMode,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { it / 2 }),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            currentTrack?.let { track ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp, top = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                    .data(track.albumArtUri).crossfade(true).build(),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (track.isExplicit || track.title.contains("explicit", true)) {
                                        ExplicitBadge(modifier = Modifier.padding(end = 6.dp))
                                    }
                                    Text(
                                        text = track.artist,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.likeCurrentTrack() }) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (isLiked) EcoDotRed else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.togglePlayback() }) {
                                Icon(
                                    imageVector = if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SlickSeekBar(
                        position = 0L,
                        positionProvider = positionProvider,
                        duration = duration,
                        accentColor = Color.White,
                        isPlaying = isPlaying,
                        trackTitle = track.title,
                        onSeek = { viewModel.seekTo(it) },
                        formatDuration = { viewModel.formatDuration(it) },
                        isVideo = isTrackAVideo
                    )
                }
            }
        }
        } // end else (EcoDot Classic player)
    }
}

    // FULL SCREEN INFO OVERLAY
    AnimatedVisibility(
        visible = showInfoSheet,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        FullScreenInfoView(
            viewModel = viewModel,
            currentTrack = currentTrack,
            onClose = { showInfoSheet = false }
        )
    }

    // QUEUE BOTTOM SHEET
    if (showQueueSheet) {
        // Local mutable copy for instant visual reordering without waiting for ViewModel
        val upcomingBase = playbackQueue.drop(currentQueueIndex + 1)
        // draggedIndex: index within upcomingBase of the item being dragged (-1 = none)
        var draggedFromIndex by remember { mutableIntStateOf(-1) }
        var draggedToIndex by remember { mutableIntStateOf(-1) }
        // Raw cumulative drag in pixels
        var dragAccumPx by remember { mutableFloatStateOf(0f) }
        // Item height in px, measured once
        var itemHeightPx by remember { mutableFloatStateOf(72f) }
        val queueListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF0A0A0A),
            scrimColor = Color.Black.copy(alpha = 0.7f),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) },
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = { WindowInsets(0) }
        ) {
            LazyColumn(
                state = queueListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    Text("Now Playing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
                    
                    // Current track
                    currentTrack?.let { track ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current).data(track.albumArtUri).crossfade(true).build(),
                                contentDescription = null,
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(track.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                                        ExplicitBadge(modifier = Modifier.padding(end = 6.dp))
                                    }
                                    Text(track.artist, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            IconButton(onClick = { optionsMenuTrack = track }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Queue", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Endless queue", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(end = 8.dp))
                            Switch(
                                checked = endlessMode,
                                onCheckedChange = { viewModel.toggleEndlessMode() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor, uncheckedThumbColor = Color.Gray)
                            )
                        }
                    }
                }
                
                // Queue Items
                if (upcomingBase.isNotEmpty()) {
                    val displayCount = if (endlessMode) 2000 else upcomingBase.size
                    items(
                        count = displayCount,
                        key = { i ->
                            val relativeIndex = i % upcomingBase.size
                            val occurrence = i / upcomingBase.size
                            "${upcomingBase[relativeIndex].id}_${relativeIndex}_${occurrence}"
                        }
                    ) { i ->
                        val relativeIndex = i % upcomingBase.size
                        val track = upcomingBase[relativeIndex]
                        // Only the first cycle (occurrence=0) is the real queue; rest are endless repeats
                        val isEndlessRepeat = (i / upcomingBase.size) > 0
                        // absoluteIndex only valid for first cycle
                        val absoluteIndex = currentQueueIndex + 1 + relativeIndex

                        // Drag visual: this item is "floated" if it's the dragged one
                        val isDragged = draggedFromIndex == relativeIndex && !isEndlessRepeat

                        // Items between from..to should visually shift to make room
                        val isShifted = !isEndlessRepeat && draggedFromIndex != -1 && draggedToIndex != -1 && run {
                            val lo = minOf(draggedFromIndex, draggedToIndex)
                            val hi = maxOf(draggedFromIndex, draggedToIndex)
                            relativeIndex in lo..hi && relativeIndex != draggedFromIndex
                        }
                        val shiftAmount = if (isShifted) {
                            if (draggedToIndex > draggedFromIndex) -itemHeightPx else itemHeightPx
                        } else 0f
                        val animatedShift by animateFloatAsState(
                            targetValue = shiftAmount,
                            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                            label = "shift_anim_$relativeIndex"
                        )

                        val uniqueItemKey = "${track.id}_${relativeIndex}_${i / upcomingBase.size}"
                        key(uniqueItemKey) {
                            // Swipe state — created once per unique key.
                            // isDragged captured via rememberUpdatedState so the lambda always sees the latest value.
                            val isDraggedState = rememberUpdatedState<Boolean>(isDragged)
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (!isEndlessRepeat && !isDraggedState.value &&
                                        (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd)
                                    ) {
                                        viewModel.removeFromQueue(absoluteIndex, track)
                                        true
                                    } else false
                                },
                                positionalThreshold = { total -> total * 0.35f }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                modifier = Modifier.animateItem(),
                                backgroundContent = {
                                    val fraction = runCatching { dismissState.requireOffset() }
                                        .getOrDefault(0f)
                                        .let { (kotlin.math.abs(it) / 300f).coerceIn(0f, 1f) }
                                    val bgColor = Color(
                                        red = (0.17f * fraction).coerceIn(0f, 1f),
                                        green = 0f,
                                        blue = 0f,
                                        alpha = fraction
                                    )
                                    val iconAlpha = fraction.coerceIn(0f, 1f)
                                    val iconAlign = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                                        Alignment.CenterStart else Alignment.CenterEnd
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(bgColor)
                                            .padding(horizontal = 24.dp),
                                        contentAlignment = iconAlign
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Remove from Queue",
                                            tint = Color.White.copy(alpha = iconAlpha)
                                        )
                                    }
                                }
                            ) {
                                QueueTrackRow(
                                    track = track,
                                    isDragged = isDragged,
                                    animatedShiftPx = animatedShift,
                                    dragOffsetPx = if (isDragged) dragAccumPx else 0f,
                                    accentColor = accentColor,
                                    onTrackClick = { if (!isEndlessRepeat) viewModel.skipToQueueItem(absoluteIndex) },
                                    onOptionsClick = {
                                        optionsMenuQueueIndex = if (!isEndlessRepeat) absoluteIndex else null
                                        optionsMenuTrack = track
                                    },
                                    onItemHeightMeasured = { h -> if (h > 0f) itemHeightPx = h },
                                    onDragStart = {
                                        if (!isEndlessRepeat) {
                                            draggedFromIndex = relativeIndex
                                            draggedToIndex = relativeIndex
                                            dragAccumPx = 0f
                                        }
                                    },
                                    onDrag = { dy ->
                                        if (draggedFromIndex != -1) {
                                            dragAccumPx += dy
                                            val rawSteps = dragAccumPx / itemHeightPx
                                            val steps = if (rawSteps > 0) (rawSteps + 0.5f).toInt()
                                                        else (rawSteps - 0.5f).toInt()
                                            val newTarget = (draggedFromIndex + steps)
                                                .coerceIn(0, upcomingBase.size - 1)
                                            draggedToIndex = newTarget
                                        }
                                    },
                                    onDragEnd = {
                                        val from = draggedFromIndex
                                        val to = draggedToIndex
                                        draggedFromIndex = -1
                                        draggedToIndex = -1
                                        dragAccumPx = 0f
                                        if (from != -1 && from != to) {
                                            viewModel.moveQueueItem(
                                                currentQueueIndex + 1 + from,
                                                currentQueueIndex + 1 + to
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Taste-matched Recommendations in the Queue
                if (historyRecommendations.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Recommended for You",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    items(historyRecommendations, key = { "q_rec_${it.id}" }) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.addToQueue(track) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                    .data(track.albumArtUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                                        ExplicitBadge(modifier = Modifier.padding(end = 6.dp))
                                    }
                                    Text(
                                        text = track.artist,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.addToQueue(track) }) {
                                Icon(
                                    Icons.Rounded.AddCircleOutline,
                                    contentDescription = "Add to Queue",
                                    tint = accentColor
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

    // FULL SCREEN LYRICS OVERLAY
    AnimatedVisibility(
        visible = showFullScreenLyrics,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        FullScreenLyricsView(
            viewModel = viewModel,
            currentTrack = currentTrack,
            dominantColor = dominantColor,
            syncedLyrics = syncedLyrics,
            plainLyrics = rawLyrics?.plainLyrics,
            isLyricsLoading = isLyricsLoading,
            currentPosition = 0L,
            positionProvider = positionProvider,
            onClose = { showFullScreenLyrics = false },
            onOpenOptionsMenu = {
                currentTrack?.let {
                    optionsMenuQueueIndex = null
                    optionsMenuTrack = it
                }
            }
        )
    }

    optionsMenuTrack?.let { track ->
        val isDownloaded = downloadedTracks.any { it.id == track.id }
        val downloadProgress = downloadProgressMap[track.id]
        val isLikedTrack by viewModel.isTrackLiked(track.id).collectAsState(initial = false)
        val isInQueue = remember(playbackQueue, track.id) { playbackQueue.any { it.id == track.id } }

        TrackOptionsBottomSheet(
            track = track,
            onDismiss = {
                optionsMenuTrack = null
                optionsMenuQueueIndex = null
            },
            onPlayNext = { viewModel.playNext(track) },
            onAddToQueue = { viewModel.addToQueue(track) },
            onRemoveFromQueue = if (isInQueue) {
                { viewModel.removeTrackFromQueue(track, preferredIndex = optionsMenuQueueIndex) }
            } else null,
            onGoToAlbum = { 
                optionsMenuTrack = null
                if (track.album != "Unknown Album" && !track.albumId.isNullOrEmpty()) {
                    onBackClick()
                    navController?.navigate("album/${track.albumId}") 
                }
            },
            onGoToArtist = { 
                optionsMenuTrack = null
                if (!track.artistId.isNullOrEmpty()) {
                    onBackClick()
                    navController?.navigate("artist/${track.artistId}") 
                }
            },
            isDownloaded = isDownloaded,
            downloadProgress = downloadProgress,
            onDownload = { viewModel.downloadTrack(track) },
            onDeleteDownload = { viewModel.deleteDownload(track) },
            isLiked = isLikedTrack,
            onLikeToggle = { viewModel.toggleTrackLike(track) },
            onAddToPlaylist = {
                optionsMenuTrack = null
                showPlaylistPicker = track
            },
            onSetCanvasUrl = { url -> 
                if (url == "DISABLED") {
                    viewModel.disableCanvas()
                } else {
                    viewModel.enableCanvasForCurrentTrack(context)
                }
            },
            isCanvasActive = canvasPlayer != null || isCanvasEnabledByUser,
            onEqualizerClick = onEqualizerClick,
            onSetSleepTimer = { viewModel.setSleepTimer(it) },
            onSetSleepTimerAtEndOfTrack = { viewModel.setSleepTimerAtEndOfTrack() },
            onCancelSleepTimer = { viewModel.cancelSleepTimer() },
            sleepTimerRemaining = sleepTimerRemaining,
            onSetAsRingtone = {
                showRingtoneClipper = track
            },
            isVideoMode = isTrackAVideo,
            onToggleVideoMode = { viewModel.setVideoMode(!isTrackAVideo) },
            onSwitchPlayerStyle = { showPlayerStyleSheet = true }
        )
    }

    showPlaylistPicker?.let { track ->
        val playlists by viewModel.allPlaylists.collectAsState()
        AddToPlaylistDialog(
            track = track,
            playlists = playlists,
            onDismiss = { showPlaylistPicker = null },
            onPlaylistSelected = { playlist ->
                viewModel.addTrackToPlaylist(playlist.id, track)
                showPlaylistPicker = null
            },
            onCreatePlaylist = { name ->
                viewModel.createPlaylist(name)
            }
        )
    }

    showRingtoneClipper?.let { track ->
        com.example.ecodot.ui.components.RingtoneClipperBottomSheet(
            track = track,
            viewModel = viewModel,
            onDismiss = { showRingtoneClipper = null }
        )
    }

    if (showPlayerStyleSheet) {
        PlayerStyleBottomSheet(
            currentStyle = playerStyle,
            accentColor = accentColor,
            onStyleSelected = { viewModel.setPlayerStyle(it) },
            onDismiss = { showPlayerStyleSheet = false }
        )
    }

    if (showAudioOutputSheet) {
        QuickAudioOutputSheet(
            currentDeviceName = queryActiveAudioOutputLabel(context),
            accentColor = if (accentColor == Color.Black) Color(0xFF8C6EFF) else accentColor,
            onOpenEqualizer = onEqualizerClick,
            onOpenSystemBluetooth = {
                runCatching {
                    context.startActivity(
                        android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            },
            onDismiss = { showAudioOutputSheet = false }
        )
    }

    if (showSleepTimerSheet) {
        SleepTimerBottomSheet(
            sleepTimerRemaining = sleepTimerRemaining,
            onDismiss = { showSleepTimerSheet = false },
            onSetSleepTimer = { minutes ->
                viewModel.setSleepTimer(minutes)
                showSleepTimerSheet = false
            },
            onSetSleepTimerAtEndOfTrack = {
                viewModel.setSleepTimerAtEndOfTrack()
                showSleepTimerSheet = false
            },
            onCancelSleepTimer = {
                viewModel.cancelSleepTimer()
                showSleepTimerSheet = false
            }
        )
    }

    if (showShareStorySheet) {
        currentTrack?.let { track ->
            ShareStoryCardBottomSheet(
                track = track,
                syncedLyrics = syncedLyrics,
                dominantColor = dominantColor,
                accentColor = accentColor,
                onDismiss = { showShareStorySheet = false }
            )
        }
    }
    }
}
