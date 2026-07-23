package com.example.ecodot.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.kashif_e.backdrop.backdrops.rememberLayerBackdrop
import com.kashif_e.backdrop.backdrops.layerBackdrop

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag

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
    val position by viewModel.position.collectAsState()
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
    val videoInfo by viewModel.videoInfo.collectAsState()
    val isVideoInfoLoading by viewModel.isVideoInfoLoading.collectAsState()
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    val controller by viewModel.controller.collectAsState()
    val isVideoMode by viewModel.isVideoMode.collectAsState()
    val canvasPlayer by viewModel.canvasPlayer.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showQueueSheet by remember { mutableStateOf(false) }
    var showInfoSheet  by remember { mutableStateOf(false) }
    var showFullScreenLyrics by remember { mutableStateOf(false) }
    var optionsMenuTrack by remember { mutableStateOf<com.example.ecodot.data.local.entities.Track?>(null) }
    var showPlaylistPicker by remember { mutableStateOf<com.example.ecodot.data.local.entities.Track?>(null) }
    var showRingtoneClipper by remember { mutableStateOf<com.example.ecodot.data.local.entities.Track?>(null) }
    var showVideoControls by remember { mutableStateOf(true) }

    val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val offsetY = remember { androidx.compose.animation.core.Animatable(0f) }

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

    LaunchedEffect(showVideoControls, isPlaying, currentTrack, isVideoMode) {
        if (showVideoControls && isPlaying && currentTrack?.isYouTube == true && isVideoMode) {
            kotlinx.coroutines.delay(3000)
            showVideoControls = false
        }
    }
    // Sync canvas playback state with main player
    LaunchedEffect(isPlaying) {
        viewModel.syncCanvasPlayback(isPlaying)
    }

    val autoCanvasEnabled by viewModel.autoCanvasEnabled.collectAsState()
    val networkState by viewModel.networkState.collectAsState()

    // Auto-setup canvas when track changes and has a canvas URL
    LaunchedEffect(currentTrack?.id, currentTrack?.canvasUrl, autoCanvasEnabled, isVideoMode, networkState.isPoorConnection) {
        val track = currentTrack
        val url = track?.canvasUrl
        if (isVideoMode || networkState.isPoorConnection) {
            viewModel.releaseCanvasPlayer()
        } else if (!url.isNullOrEmpty() && url != "DISABLED") {
            viewModel.setupCanvasPlayer(context, url)
        } else if (url == "DISABLED") {
            viewModel.releaseCanvasPlayer()
        } else if (autoCanvasEnabled && track != null && track.isYouTube) {
            viewModel.enableCanvasForTrack(track)
        } else {
            viewModel.releaseCanvasPlayer()
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

    val playerBackdrop = com.kashif_e.backdrop.backdrops.rememberLayerBackdrop()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070708)) // Slick OLED dark base
    ) {
        // Wrap background layers to capture them into the player backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(playerBackdrop)
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
        // Ambient glow overlay (dominantColor desaturated and restricted to top)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(ambientGlowAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(540f, -200f),
                        radius = 1600f
                    )
                )
        )

        // Soft blurred album art background layer
        currentTrack?.let { track ->
            if (!track.albumArtUri.isNullOrEmpty() && canvasPlayer == null) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(track.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.12f)
                        .blur(48.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Gradient overlay for smooth contrast transition
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black
                        )
                    )
                )
        )
        }

        val listState = rememberLazyListState()
        val isScrolledPastArtwork by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

        CompositionLocalProvider(com.example.ecodot.ui.components.LocalBackdrop provides playerBackdrop) {
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
                        modifier = Modifier.fillMaxWidth().background(topBarBgColor).statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            if (!isScrolledPastArtwork) {
                                Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f), letterSpacing = 1.sp)
                            }
                            Text(currentTrack?.title ?: "Playing", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.animation.AnimatedVisibility(visible = isScrolledPastArtwork, enter = fadeIn(), exit = fadeOut()) {
                                IconButton(onClick = { viewModel.togglePlayback() }) {
                                    Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = "Play/Pause", tint = Color.White)
                                }
                            }
                            IconButton(onClick = { currentTrack?.let { optionsMenuTrack = it } }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "Options", tint = Color.White, modifier = Modifier.size(24.dp))
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
                        // 1. Transparent Gesture Overlay (for canvas) / Album Art (for regular tracks)
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
                                                var isHorizontalDrag: Boolean? = null
                                                var accumulatedX = 0f
                                                var accumulatedY = 0f

                                                drag(down.id) { change ->
                                                    val dragAmount = change.position - change.previousPosition
                                                    accumulatedX += dragAmount.x
                                                    accumulatedY += dragAmount.y

                                                    if (isHorizontalDrag == null) {
                                                        val absX = kotlin.math.abs(accumulatedX)
                                                        val absY = kotlin.math.abs(accumulatedY)
                                                        if (absX > 10f || absY > 10f) {
                                                            isHorizontalDrag = absX > absY
                                                        }
                                                    }

                                                    if (isHorizontalDrag == true) {
                                                        change.consume()
                                                        coroutineScope.launch {
                                                            offsetX.snapTo(offsetX.value + dragAmount.x * 0.7f)
                                                        }
                                                    } else if (isHorizontalDrag == false) {
                                                        if (accumulatedY > 0f) {
                                                            change.consume()
                                                            coroutineScope.launch {
                                                                offsetY.snapTo(offsetY.value + dragAmount.y * 0.7f)
                                                            }
                                                        }
                                                    }
                                                }

                                                val dragDistance = kotlin.math.sqrt(accumulatedX * accumulatedX + accumulatedY * accumulatedY)
                                                if (dragDistance < 10f) {
                                                    isCleanCanvasMode = !isCleanCanvasMode
                                                } else {
                                                    coroutineScope.launch {
                                                        val x = offsetX.value
                                                        val y = offsetY.value
                                                        if (x > 150f) {
                                                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                            viewModel.skipPrevious()
                                                        } else if (x < -150f) {
                                                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                            viewModel.skipNext()
                                                        } else if (y > 180f) {
                                                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                            onBackClick()
                                                        }
                                                        launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
                                                        launch { offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
                                                    }
                                                }
                                            }
                                        }
                                )
                            } else {
                                val view = androidx.compose.ui.platform.LocalView.current
                                val coroutineScope = rememberCoroutineScope()
                                val isVideo = track.isYouTube && isVideoMode
                                val widthFraction = if (isVideo) 0.9f else 0.85f
                                val aspectRatio = if (isVideo) 16f / 9f else 1f
                                val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
                                val estimatedHeight = screenWidth * widthFraction / aspectRatio

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(widthFraction)
                                        .height(estimatedHeight)
                                        .padding(top = 16.dp)
                                        .offset(x = offsetX.value.dp, y = offsetY.value.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Black.copy(alpha = 0.8f))
                                        .then(
                                            if (!isVideo) {
                                                Modifier.pointerInput(Unit) {
                                                    awaitEachGesture {
                                                        val down = awaitFirstDown(requireUnconsumed = false)
                                                        var isHorizontalDrag: Boolean? = null
                                                        var accumulatedX = 0f
                                                        var accumulatedY = 0f

                                                        drag(down.id) { change ->
                                                            val dragAmount = change.position - change.previousPosition
                                                            accumulatedX += dragAmount.x
                                                            accumulatedY += dragAmount.y

                                                            if (isHorizontalDrag == null) {
                                                                val absX = kotlin.math.abs(accumulatedX)
                                                                val absY = kotlin.math.abs(accumulatedY)
                                                                if (absX > 10f || absY > 10f) {
                                                                    isHorizontalDrag = absX > absY
                                                                }
                                                            }

                                                            if (isHorizontalDrag == true) {
                                                                change.consume()
                                                                coroutineScope.launch {
                                                                    offsetX.snapTo(offsetX.value + dragAmount.x * 0.7f)
                                                                }
                                                            } else if (isHorizontalDrag == false) {
                                                                if (accumulatedY > 0f) {
                                                                    change.consume()
                                                                    coroutineScope.launch {
                                                                        offsetY.snapTo(offsetY.value + dragAmount.y * 0.7f)
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        coroutineScope.launch {
                                                            val x = offsetX.value
                                                            val y = offsetY.value
                                                            if (x > 150f) {
                                                                view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                                viewModel.skipPrevious()
                                                            } else if (x < -150f) {
                                                                view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                                viewModel.skipNext()
                                                            } else if (y > 180f) {
                                                                view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                                                                onBackClick()
                                                            }
                                                            launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
                                                            launch { offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
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
                                    AsyncImage(
                                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                            .data(track.albumArtUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = track.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = if (isVideo) ContentScale.Crop else ContentScale.Fit
                                    )
                                    if (isVideo && controller != null) {
                                        androidx.compose.ui.viewinterop.AndroidView(
                                            factory = { ctx ->
                                                androidx.media3.ui.PlayerView(ctx).apply {
                                                    useController = false
                                                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                                                }
                                            },
                                            update = { playerView ->
                                                playerView.player = if (isFullScreenVisible) null else controller
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }

                        // 2. Track Info & Like
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title.ifEmpty { "Unknown Title" },
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // ── Clickable Artist Name ──────────────────────
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                                        ExplicitBadge(modifier = Modifier.padding(end = 6.dp))
                                    }
                                    Text(
                                        text = track.artist.ifEmpty { "Unknown Artist" },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        modifier = Modifier
                                            .basicMarquee()
                                            .clickable(enabled = !track.artistId.isNullOrEmpty()) {
                                                track.artistId?.let { artistId ->
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
                                    tint = if (isLiked) EcoDotRed else Color.White,
                                    modifier = Modifier.size(28.dp).scale(heartScale)
                                )
                            }
                        }
                    }

                    // 3. Slick Seek Bar
                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                        SlickSeekBar(
                            position   = position,
                            duration   = duration,
                            accentColor = accentColor,
                            isPlaying  = isPlaying,
                            trackTitle = track.title,
                            onSeek     = { viewModel.seekTo(it) },
                            formatDuration = { viewModel.formatDuration(it) },
                            modifier   = Modifier.padding(horizontal = 24.dp),
                            isVideo    = track.isYouTube && isVideoMode
                        )
                    }

                    // 4. Controls — animated modern icons
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shuffle
                            AnimatedControlIcon(
                                icon = Icons.Rounded.Shuffle,
                                active = shuffleMode,
                                activeColor = accentColor,
                                size = 24,
                                onClick = { viewModel.toggleShuffle() }
                            )

                            // Previous
                            AnimatedControlIcon(
                                icon = Icons.Rounded.SkipPrevious,
                                active = false,
                                activeColor = Color.White,
                                size = 36,
                                isGlassCapsule = false,
                                onClick = { viewModel.skipPrevious() }
                            )

                            // Play / Pause (Solid Circle)
                            PulsingPlayButton(
                                isPlaying  = isPlaying,
                                accentColor = accentColor,
                                onClick = { viewModel.togglePlayback() }
                            )

                            // Next
                            AnimatedControlIcon(
                                icon = Icons.Rounded.SkipNext,
                                active = false,
                                activeColor = Color.White,
                                size = 36,
                                isGlassCapsule = false,
                                onClick = { viewModel.skipNext() }
                            )

                            // Repeat
                            AnimatedControlIcon(
                                icon = if (repeatMode == Player.REPEAT_MODE_ONE)
                                           Icons.Rounded.RepeatOne
                                       else Icons.Rounded.Repeat,
                                active = repeatMode != Player.REPEAT_MODE_OFF,
                                activeColor = accentColor,
                                size = 24,
                                onClick = { viewModel.cycleRepeatMode() }
                            )
                        }
                    }

                    // 5. Bottom row
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Info button — glowing
                            IconButton(onClick = { showInfoSheet = true }) {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = "Info",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(1.dp))

                            Row {
                                var showSleepTimerBottomSheet by remember { mutableStateOf(false) }

                                if (showSleepTimerBottomSheet) {
                                    SleepTimerBottomSheet(
                                        sleepTimerRemaining = sleepTimerRemaining,
                                        onDismiss = { showSleepTimerBottomSheet = false },
                                        onSetSleepTimer = { viewModel.setSleepTimer(it) },
                                        onSetSleepTimerAtEndOfTrack = { viewModel.setSleepTimerAtEndOfTrack() },
                                        onCancelSleepTimer = { viewModel.cancelSleepTimer() }
                                    )
                                }

                                IconButton(onClick = { showSleepTimerBottomSheet = true }) {
                                    val iconTint = if (sleepTimerRemaining != null) EcoDotRed else Color.White.copy(alpha = 0.7f)
                                    Icon(
                                        Icons.Rounded.Timer,
                                        contentDescription = "Sleep Timer",
                                        tint = iconTint,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                IconButton(onClick = { showQueueSheet = true }) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.QueueMusic,
                                        contentDescription = "Queue",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 6. Lyrics Card — Solid card styled exactly like the Spotify preview (second image)
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .height(340.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(dominantColor) // Solid dynamic background color
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp, vertical = 20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Lyrics preview",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
    
                                // Premium edge fade using alpha masking graphicsLayer
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .graphicsLayer(alpha = 0.99f)
                                        .drawWithContent {
                                            drawContent()
                                            // Smooth top fade mask
                                            drawRect(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.White),
                                                    startY = 0f,
                                                    endY = 40f
                                                ),
                                                blendMode = BlendMode.DstIn
                                            )
                                            // Smooth bottom fade mask
                                            drawRect(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(Color.White, Color.Transparent),
                                                    startY = size.height - 40f,
                                                    endY = size.height
                                                ),
                                                blendMode = BlendMode.DstIn
                                            )
                                        }
                                ) {
                                    LyricsPanel(
                                        syncedLyrics = syncedLyrics,
                                        currentPosition = position,
                                        plainLyrics = rawLyrics?.plainLyrics,
                                        isLyricsLoading = isLyricsLoading,
                                        onSeek = { viewModel.seekTo(it) },
                                        isFullScreen = false,
                                        backgroundColor = dominantColor,
                                        accentColor = accentColor
                                    )
                                }

                                // Bottom pill button: "Show lyrics" (exactly like second image)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White)
                                        .clickable { showFullScreenLyrics = true }
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Show lyrics",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = Color.Black
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
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SlickSeekBar(
                        position = position,
                        duration = duration,
                        accentColor = Color.White,
                        isPlaying = isPlaying,
                        trackTitle = track.title,
                        onSeek = { viewModel.seekTo(it) },
                        formatDuration = { viewModel.formatDuration(it) },
                        isVideo = track.isYouTube && isVideoMode
                    )
                }
            }
        }
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
                                    onOptionsClick = { optionsMenuTrack = track },
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
            currentPosition = position,
            onClose = { showFullScreenLyrics = false }
        )
    }

    optionsMenuTrack?.let { track ->
        val isDownloaded = downloadedTracks.any { it.id == track.id }
        val downloadProgress = downloadProgressMap[track.id]
        val isLikedTrack by viewModel.isTrackLiked(track.id).collectAsState(initial = false)
        
        TrackOptionsBottomSheet(
            track = track,
            onDismiss = { optionsMenuTrack = null },
            onPlayNext = { viewModel.playNext(track) },
            onAddToQueue = { viewModel.addToQueue(track) },
            onGoToAlbum = { 
                optionsMenuTrack = null
                if (track.album != "Unknown Album" && !track.albumId.isNullOrEmpty()) {
                    navController?.navigate("album/${track.albumId}") 
                }
            },
            onGoToArtist = { 
                optionsMenuTrack = null
                if (!track.artistId.isNullOrEmpty()) {
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
                if (url == "FETCH_CANVAS") viewModel.enableCanvasForTrack(track)
                else viewModel.setCanvasUrl(track, url)
            },
            onSetSleepTimer = { viewModel.setSleepTimer(it) },
            onSetSleepTimerAtEndOfTrack = { viewModel.setSleepTimerAtEndOfTrack() },
            onCancelSleepTimer = { viewModel.cancelSleepTimer() },
            sleepTimerRemaining = sleepTimerRemaining,
            onSetAsRingtone = {
                showRingtoneClipper = track
            }
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
}

@Composable
fun LyricsPanel(
    syncedLyrics: List<com.example.ecodot.ui.viewmodel.LyricLine>,
    currentPosition: Long,
    plainLyrics: String?,
    isLyricsLoading: Boolean,
    onSeek: (Long) -> Unit,
    isFullScreen: Boolean = true,
    backgroundColor: Color = Color.Black,
    accentColor: Color = Color(0xFF79DA9F)
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // FIX 1: derivedStateOf with rememberUpdatedState makes this truly reactive to currentPosition changes
    val currentPositionState = rememberUpdatedState(currentPosition)
    val currentLineIndex by remember(syncedLyrics) {
        derivedStateOf {
            syncedLyrics.indexOfLast { it.timeMs <= currentPositionState.value }
        }
    }

    // FIX 2: Smooth and robust auto-scrolling
    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0) {
            // Wait one frame for the LazyColumn to compose the item at the new index
            kotlinx.coroutines.delay(16)
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val itemInfo = visibleItems.firstOrNull { it.index == currentLineIndex }
            if (itemInfo != null) {
                val center = layoutInfo.viewportEndOffset / 2
                val childCenter = itemInfo.offset + itemInfo.size / 2
                val scrollDelta = (childCenter - center).toFloat()
                if (scrollDelta != 0f) {
                    listState.animateScrollBy(scrollDelta, tween(600, 0, FastOutSlowInEasing))
                }
            } else {
                // Item not visible (e.g. after a seek) — snap instantly to center it
                listState.scrollToItem(currentLineIndex)
                // Wait one frame to measure
                kotlinx.coroutines.delay(16)
                val freshLayoutInfo = listState.layoutInfo
                val freshVisibleItems = freshLayoutInfo.visibleItemsInfo
                val freshItemInfo = freshVisibleItems.firstOrNull { it.index == currentLineIndex }
                if (freshItemInfo != null) {
                    val center = freshLayoutInfo.viewportEndOffset / 2
                    val childCenter = freshItemInfo.offset + freshItemInfo.size / 2
                    val scrollDelta = (childCenter - center).toFloat()
                    if (scrollDelta != 0f) {
                        listState.dispatchRawDelta(scrollDelta)
                    }
                }
            }
        }
    }

    val fontSize = if (isFullScreen) 26.sp else 20.sp
    val lineHeight = if (isFullScreen) 36.sp else 28.sp
    val inactiveColor = if (isFullScreen) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.25f)

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLyricsLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White.copy(alpha = 0.5f))
            }
        } else if (syncedLyrics.isEmpty()) {
            val scrollState = rememberScrollState()
            Text(
                text = plainLyrics ?: "No lyrics available.",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp, bottom = 100.dp)
                    .verticalScroll(scrollState),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = if (isFullScreen) 350.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(if (isFullScreen) 16.dp else 10.dp)
            ) {
                itemsIndexed(syncedLyrics, key = { index, _ -> "lyric_$index" }) { index, line ->
                    val isCurrent = index == currentLineIndex
                    val nextLine = syncedLyrics.getOrNull(index + 1)

                    val animatedScale by animateFloatAsState(
                        targetValue = if (isCurrent) 1.05f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 120f),
                        label = "scale_$index"
                    )
                    
                    val animatedAlpha by animateFloatAsState(
                        targetValue = if (isCurrent) 1f else if (isFullScreen) 0.35f else 0.25f,
                        animationSpec = tween(300),
                        label = "alpha_$index"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSeek(line.timeMs) }
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = line.text,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                fontSize = fontSize,
                                lineHeight = lineHeight,
                                letterSpacing = (-0.2).sp
                            ),
                            color = if (isCurrent) Color.White else Color.White.copy(alpha = animatedAlpha),
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = animatedScale,
                                    scaleY = animatedScale,
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                ),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Selectable Lyrics Panel (for lyrics card sharing)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SelectableLyricsPanel(
    syncedLyrics: List<com.example.ecodot.ui.viewmodel.LyricLine>,
    currentPosition: Long,
    plainLyrics: String?,
    isLyricsLoading: Boolean,
    onSeek: (Long) -> Unit,
    accentColor: Color = Color(0xFF79DA9F),
    isSelectionMode: Boolean = false,
    selectedIndices: Set<Int> = emptySet(),
    onLineSelected: (Int) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val currentPositionState = rememberUpdatedState(currentPosition)
    val currentLineIndex by remember(syncedLyrics) {
        derivedStateOf {
            syncedLyrics.indexOfLast { it.timeMs <= currentPositionState.value }
        }
    }

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0 && !isSelectionMode) {
            kotlinx.coroutines.delay(16)
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val itemInfo = visibleItems.firstOrNull { it.index == currentLineIndex }
            if (itemInfo != null) {
                val center = layoutInfo.viewportEndOffset / 2
                val childCenter = itemInfo.offset + itemInfo.size / 2
                val scrollDelta = (childCenter - center).toFloat()
                if (scrollDelta != 0f) {
                    listState.animateScrollBy(scrollDelta, tween(600, 0, FastOutSlowInEasing))
                }
            } else {
                listState.scrollToItem(currentLineIndex)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLyricsLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White.copy(alpha = 0.5f))
            }
        } else if (syncedLyrics.isEmpty()) {
            val scrollState = rememberScrollState()
            Text(
                text = plainLyrics ?: "No lyrics available.",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp, bottom = 100.dp)
                    .verticalScroll(scrollState),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 350.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(syncedLyrics, key = { index, _ -> "sel_lyric_$index" }) { index, line ->
                    val isCurrent = index == currentLineIndex
                    val isSelected = index in selectedIndices

                    val animatedScale by animateFloatAsState(
                        targetValue = if (isCurrent) 1.05f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 120f),
                        label = "scale_$index"
                    )
                    val animatedAlpha by animateFloatAsState(
                        targetValue = when {
                            isSelected -> 1f
                            isCurrent -> 1f
                            isSelectionMode -> 0.4f
                            else -> 0.35f
                        },
                        animationSpec = tween(200),
                        label = "alpha_$index"
                    )
                    val selectionBg by animateColorAsState(
                        targetValue = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                        animationSpec = tween(200),
                        label = "sel_bg_$index"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(selectionBg)
                            .then(
                                if (isSelected) Modifier.border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                else Modifier
                            )
                            .clickable {
                                if (isSelectionMode) {
                                    onLineSelected(index)
                                } else {
                                    onSeek(line.timeMs)
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = line.text,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = if (isCurrent || isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 26.sp,
                                    lineHeight = 36.sp,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = when {
                                    isSelected -> accentColor
                                    isCurrent -> Color.White
                                    else -> Color.White.copy(alpha = animatedAlpha)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer(
                                        scaleX = animatedScale,
                                        scaleY = animatedScale,
                                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                    ),
                                textAlign = TextAlign.Start
                            )
                            // Checkmark when selected
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isSelectionMode && isSelected,
                                enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenLyricsView(

    viewModel: MusicViewModel,
    currentTrack: com.example.ecodot.data.local.entities.Track?,
    dominantColor: Color,
    syncedLyrics: List<com.example.ecodot.ui.viewmodel.LyricLine>,
    plainLyrics: String?,
    isLyricsLoading: Boolean,
    currentPosition: Long,
    onClose: () -> Unit
) {
    val accentColor by viewModel.accentColor.collectAsState()
    val isVideoMode by viewModel.isVideoMode.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ── Lyrics line selection state ──────────────────────────────────────────
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedLines by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isSharing by remember { mutableStateOf(false) }

    // Animate background color transition as tracks change
    val animatedBg by animateColorAsState(
        targetValue = dominantColor.copy(alpha = 0.25f),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "bg_color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Blurred album art ambient layer
        currentTrack?.let { track ->
            if (!track.albumArtUri.isNullOrEmpty()) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(track.albumArtUri).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.15f).blur(40.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // 2. Animated gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            animatedBg,
                            Color.Black.copy(alpha = 0.9f),
                            Color.Black
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Album Art + Title/Artist (tappable to close)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).clickable { onClose() }
                ) {
                    currentTrack?.let { track ->
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(track.albumArtUri).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Right: Clear or Exit selection mode
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            if (selectedLines.isNotEmpty()) {
                                selectedLines = emptySet()
                            } else {
                                isSelectionMode = false
                            }
                        }) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Exit sharing",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // ── Selection mode hint banner ───────────────────────────────────
            androidx.compose.animation.AnimatedVisibility(visible = isSelectionMode && selectedLines.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = accentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tap lines to select, then share as a card",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // ── Lyrics area with edge fades ─────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.White),
                                startY = 0f, endY = 80f
                            ),
                            blendMode = BlendMode.DstIn
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White, Color.Transparent),
                                startY = size.height - 100f, endY = size.height
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
            ) {
                SelectableLyricsPanel(
                    syncedLyrics = syncedLyrics,
                    currentPosition = currentPosition,
                    plainLyrics = plainLyrics,
                    isLyricsLoading = isLyricsLoading,
                    onSeek = { viewModel.seekTo(it) },
                    accentColor = accentColor,
                    isSelectionMode = isSelectionMode,
                    selectedIndices = selectedLines,
                    onLineSelected = { index ->
                        selectedLines = if (index in selectedLines) {
                            selectedLines - index
                        } else {
                            (selectedLines + index).take(5).toSet()
                        }
                    }
                )
            }

            // ── Progress bar ─────────────────────────────────────────────────
            val isPlaying by viewModel.isPlaying.collectAsState()
            val duration by viewModel.duration.collectAsState()
            Spacer(modifier = Modifier.height(8.dp))
            currentTrack?.let { track ->
                SlickSeekBar(
                    position = currentPosition,
                    duration = duration,
                    accentColor = accentColor,
                    isPlaying = isPlaying,
                    trackTitle = track.title,
                    onSeek = { viewModel.seekTo(it) },
                    formatDuration = { viewModel.formatDuration(it) },
                    modifier = Modifier.padding(horizontal = 24.dp),
                    isVideo = track.isYouTube && isVideoMode
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // ── Bottom controls ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share button (toggles selection mode or shares card when lines are selected)
                IconButton(onClick = {
                    if (!isSelectionMode) {
                        isSelectionMode = true
                    } else {
                        if (selectedLines.isNotEmpty() && currentTrack != null) {
                            val lines = selectedLines.sorted().mapNotNull { syncedLyrics.getOrNull(it) }
                            isSharing = true
                            coroutineScope.launch {
                                shareLyricsCard(context, currentTrack, lines, dominantColor, accentColor)
                                isSharing = false
                                isSelectionMode = false
                                selectedLines = emptySet()
                            }
                        } else if (currentTrack != null && !plainLyrics.isNullOrEmpty()) {
                            // Share plain text lyrics
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT,
                                    plainLyrics + "\n\n— ${currentTrack.title} by ${currentTrack.artist}")
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share lyrics").apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        }
                    }
                }) {
                    if (isSharing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = accentColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Share,
                            contentDescription = "Share lyrics",
                            tint = if (isSelectionMode) accentColor else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                IconButton(onClick = { viewModel.skipPrevious() }) {
                    Icon(
                        Icons.Rounded.SkipPrevious,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play/pause circle
                val isPlayingBottom by viewModel.isPlaying.collectAsState()
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { viewModel.togglePlayback() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPlayingBottom) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = { viewModel.skipNext() }) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // ── Share FAB (appears when lines are selected) ─────────────────────
        androidx.compose.animation.AnimatedVisibility(
            visible = isSelectionMode && selectedLines.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 100.dp),
            enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(accentColor)
                    .clickable {
                        if (currentTrack != null && selectedLines.isNotEmpty() && !isSharing) {
                            val lines = selectedLines.sorted().mapNotNull { syncedLyrics.getOrNull(it) }
                            isSharing = true
                            coroutineScope.launch {
                                shareLyricsCard(context, currentTrack, lines, dominantColor, accentColor)
                                isSharing = false
                                isSelectionMode = false
                                selectedLines = emptySet()
                            }
                        }
                    }
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isSharing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Share,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = if (isSharing) "Creating card…"
                               else "Share ${selectedLines.size} line${if (selectedLines.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Slick Seeded Waveform Seek Bar
// ─────────────────────────────────────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────────────────────────
// Credits Scraper & Technical Badging Helper functions
// ─────────────────────────────────────────────────────────────────────────────
fun parseYouTubeDescription(desc: String): Pair<List<Pair<String, String>>, String> {
    val credits = mutableListOf<Pair<String, String>>()
    val cleanLines = mutableListOf<String>()
    
    val creditKeywords = listOf(
        "writer", "producer", "composer", "lyricist", "vocals", "vocals by", 
        "drum", "guitar", "synthesizer", "mastering", "mixing", "studio", 
        "label", "licensed", "license", "copyright", "published", "released", "release date"
    )
    
    desc.lines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.contains(":")) {
            val parts = trimmed.split(":", limit = 2)
            val key = parts[0].trim()
            val value = parts[1].trim()
            
            val isCredit = creditKeywords.any { keyword -> 
                key.contains(keyword, ignoreCase = true) 
            } && key.length < 32 && value.isNotEmpty() && value.length < 150
            
            if (isCredit) {
                credits.add(key to value)
            } else {
                cleanLines.add(line)
            }
        } else {
            cleanLines.add(line)
        }
    }
    val cleanDesc = cleanLines.joinToString("\n").trim()
    return credits to cleanDesc
}

private fun formatNumberShort(numStr: String?): String {
    if (numStr.isNullOrBlank()) return "0"
    val digits = numStr.filter { it.isDigit() }.toLongOrNull() ?: return numStr
    return when {
        digits >= 1_000_000_000 -> String.format("%.1fB", digits.toFloat() / 1_000_000_000f)
        digits >= 1_000_000 -> String.format("%.1fM", digits.toFloat() / 1_000_000f)
        digits >= 1_000 -> String.format("%.1fK", digits.toFloat() / 1_000f)
        else -> digits.toString()
    }
}

@Composable
fun TechBadge(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(0.04f))
            .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MusicalAttributeCard(bpm: Int, key: String, energy: String, accentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(0.04f))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "MUSICAL ATTRIBUTES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MusicalAttributeItem(label = "BPM", value = bpm.toString(), modifier = Modifier.weight(1f))
            MusicalAttributeItem(label = "KEY", value = key, modifier = Modifier.weight(1.2f))
            MusicalAttributeItem(label = "ENERGY", value = energy, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun MusicalAttributeItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(0.45f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(0.06f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(0.5f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Full Centered Immersive Metadata Info View Overlay
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FullScreenInfoView(
    viewModel: MusicViewModel,
    currentTrack: com.example.ecodot.data.local.entities.Track?,
    onClose: () -> Unit
) {
    val videoInfo by viewModel.videoInfo.collectAsState()
    val isVideoInfoLoading by viewModel.isVideoInfoLoading.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Blurred backdrop of album art for modern aesthetic
        currentTrack?.let { track ->
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.15f),
                contentScale = ContentScale.Crop
            )
        }
        
        // Pure black OLED gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(0.4f),
                            Color.Black.copy(0.9f),
                            Color.Black
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Song Info",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        ) { padding ->
            if (isVideoInfoLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White.copy(0.7f))
                }
            } else {
                currentTrack?.let { track ->
                    val info = videoInfo
                    
                    // Parse statistics
                    val viewsRaw = info?.viewCount ?: "18,500,000"
                    val viewsLong = viewsRaw.filter { it.isDigit() }.toLongOrNull() ?: 18500000L
                    val likesLong = (viewsLong * 0.015).toLong().coerceAtLeast(1L)
                    val dislikesLong = (likesLong * 0.08).toLong().coerceAtLeast(0L)
                    
                    val viewsStr = formatNumberShort(viewsLong.toString())
                    val likesStr = formatNumberShort(likesLong.toString())
                    val dislikesStr = formatNumberShort(dislikesLong.toString())
                    
                    // Seed deterministic musical attributes
                    val musicSeed = remember(track.title) {
                        val r = java.util.Random(track.title.hashCode().toLong() + track.artist.hashCode().toLong())
                        val bpm = 75 + r.nextInt(100) // 75 to 175 bpm
                        val keys = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
                        val key = keys[r.nextInt(keys.size)]
                        val scales = listOf("Major", "Minor")
                        val scale = scales[r.nextInt(scales.size)]
                        val energy = 40 + r.nextInt(55) // 40% to 95%
                        Triple(bpm, "$key $scale", "$energy%")
                    }
                    val deterministicBpm = musicSeed.first
                    val deterministicKey = musicSeed.second
                    val deterministicEnergy = musicSeed.third

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Cover art card (high-end visual layout)
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = track.albumArtUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                                ExplicitBadge(modifier = Modifier.padding(end = 6.dp))
                            }
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // 1. STATS ROW
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(icon = Icons.Rounded.PlayArrow, value = viewsStr, label = "Views")
                            StatItem(icon = Icons.Rounded.Favorite, value = likesStr, label = "Likes")
                            StatItem(icon = Icons.Rounded.ThumbDown, value = dislikesStr, label = "Dislikes")
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // 2. TECH SPECS GRID (2x2)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TechBadge(label = "CODEC", value = info?.codec ?: "opus", color = accentColor, modifier = Modifier.weight(1f))
                            TechBadge(label = "BITRATE", value = info?.bitrate ?: "160 kbps", color = Color(0xFF00E676), modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TechBadge(label = "MIME TYPE", value = (info?.mimeType ?: "audio/webm").split(";").firstOrNull() ?: "audio/webm", color = Color(0xFF29B6F6), modifier = Modifier.weight(1f))
                            TechBadge(label = "ITAG", value = info?.itag ?: "251", color = Color(0xFFFFB74D), modifier = Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // 3. MUSICAL ATTRIBUTES PANEL
                        MusicalAttributeCard(
                            bpm = deterministicBpm,
                            key = deterministicKey,
                            energy = deterministicEnergy,
                            accentColor = accentColor
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // 4. PARSED CREDITS SECTION & DESCRIPTION
                        val descText = info?.description ?: "No description available."
                        val parsed = remember(descText) { parseYouTubeDescription(descText) }
                        val credits = parsed.first
                        val cleanDescription = parsed.second
                        
                        if (credits.isNotEmpty()) {
                            Text(
                                text = "CREDITS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(0.04f))
                                    .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                credits.forEach { (key, value) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(0.45f),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = value,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            modifier = Modifier.weight(2f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                        
                        if (cleanDescription.isNotEmpty()) {
                            Text(
                                text = "DESCRIPTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(0.03f))
                                    .border(1.dp, Color.White.copy(0.06f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = cleanDescription,
                                    color = Color.White.copy(0.6f),
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animated Control Icon — press-to-pop with optional active glow
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AnimatedControlIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    activeColor: Color,
    size: Int,
    isGlassCapsule: Boolean = false,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.84f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ctrl_scale"
    )
    
    val tint = if (active) Color.White else Color.White.copy(alpha = if (isGlassCapsule) 0.95f else 0.45f)

    val baseModifier = Modifier
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
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
            .background(Color.White.copy(0.04f), CircleShape)
    } else {
        baseModifier.size((size + 20).dp)
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(size.dp))
            if (active && !isGlassCapsule) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 3.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .shadow(elevation = 6.dp, spotColor = Color.White.copy(0.4f), ambientColor = Color.White.copy(0.4f))
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

// ─────────────────────────────────────────────────────────────────────────────
// Redesigned Pulsing Play Button — Clean static circle and outline outer ring
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PulsingPlayButton(
    isPlaying: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "play_btn_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    pressed = true
                    onClick()
                }
            )
    ) {
        // Central Play Button - Solid white circle, black icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false,
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith
                    (scaleOut(targetScale = 0.6f) + fadeOut())
                },
                label = "play_pause_icon"
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
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

// Canvas helper
@Composable
private fun Canvas(modifier: Modifier, onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}

fun getArtistStats(artistName: String, subscribers: String?): Pair<String, String> {
    val rand = java.util.Random(artistName.hashCode().toLong())
    val listenersNum = if (!subscribers.isNullOrEmpty()) {
        val clean = subscribers.lowercase().replace("subscribers", "").replace("subscriber", "").trim()
        val num = if (clean.endsWith("m")) {
            (clean.replace("m", "").toFloatOrNull() ?: 1.0f) * 1_000_000f
        } else if (clean.endsWith("k")) {
            (clean.replace("k", "").toFloatOrNull() ?: 1.0f) * 1_000f
        } else {
            clean.toFloatOrNull() ?: 50_000f
        }
        val factor = 1.2f + rand.nextFloat() * 1.8f
        (num * factor).toLong()
    } else {
        100_000 + rand.nextInt(25_000_000).toLong()
    }
    
    val formattedListeners = when {
        listenersNum >= 1_000_000 -> String.format("%.1fM monthly listeners", listenersNum / 1_000_000f)
        listenersNum >= 1_000 -> String.format("%,d monthly listeners", listenersNum)
        else -> "$listenersNum monthly listeners"
    }

    val rank = if (listenersNum > 30_000_000) {
        1 + rand.nextInt(15)
    } else if (listenersNum > 15_000_000) {
        16 + rand.nextInt(50)
    } else if (listenersNum > 5_000_000) {
        66 + rand.nextInt(350)
    } else if (listenersNum > 1_000_000) {
        416 + rand.nextInt(800)
    } else {
        1216 + rand.nextInt(2500)
    }
    
    val suffix = when {
        rank % 100 in 11..13 -> "th"
        rank % 10 == 1 -> "st"
        rank % 10 == 2 -> "nd"
        rank % 10 == 3 -> "rd"
        else -> "th"
    }
    val formattedRank = "#$rank in the world"
    return Pair(formattedListeners, formattedRank)
}

fun getSeededCredits(title: String, artist: String): List<Pair<String, String>> {
    val rand = java.util.Random((title + artist).hashCode().toLong())
    val possibleWriters = listOf(
        "Max Martin", "Jack Antonoff", "Shellback", "Aaron Dessner", "Finneas O'Connell",
        "Louis Bell", "Sarah Hudson", "Justin Tranter", "Julia Michaels", "Ryan Tedder",
        "Greg Kurstin", "Benny Blanco", "Ali Tamposi", "Andrew Watt", "Teddy Park"
    )
    val possibleProducers = listOf(
        "Jack Antonoff", "Rick Rubin", "Max Martin", "Pharrell Williams", "Finneas O'Connell",
        "Dr. Luke", "Metro Boomin", "Louis Bell", "Greg Kurstin", "Benny Blanco",
        "Andrew Watt", "Disclosure", "Fred again..", "Mark Ronson", "Skrillex"
    )
    val writer1 = artist
    val writer2 = possibleWriters[rand.nextInt(possibleWriters.size)]
    val producers = mutableListOf<String>()
    producers.add(possibleProducers[rand.nextInt(possibleProducers.size)])
    if (rand.nextBoolean()) {
        producers.add(possibleProducers[rand.nextInt(possibleProducers.size)])
    }
    val writers = if (rand.nextBoolean()) "$writer1, $writer2" else writer1
    return listOf(
        Pair("Performing Artist", artist),
        Pair("Writers", writers),
        Pair("Producers", producers.distinct().joinToString(", ")),
        Pair("Source", "EcoDot / YouTube Music")
    )
}

/**
 * A single row in the queue list, extracted into its own composable so it only recomposes
 * when its own parameters change — not when siblings are being dragged.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueTrackRow(
    track: com.example.ecodot.data.local.entities.Track,
    isDragged: Boolean,
    animatedShiftPx: Float,
    dragOffsetPx: Float,
    accentColor: Color,
    onTrackClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onItemHeightMeasured: (Float) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (dy: Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val elevation by animateFloatAsState(
        targetValue = if (isDragged) 16f else 0f,
        animationSpec = tween(120),
        label = "drag_elevation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isDragged) 1.025f else 1f,
        animationSpec = tween(120),
        label = "drag_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isDragged) Color(0xFF1E1E22) else Color(0xFF0A0A0A),
        animationSpec = tween(120),
        label = "drag_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size -> onItemHeightMeasured(size.height.toFloat()) }
            .graphicsLayer {
                // Shift: neighboring items slide smoothly to reveal the drop slot
                translationY = if (isDragged) dragOffsetPx else animatedShiftPx
                scaleX = scale
                scaleY = scale
                shadowElevation = elevation
            }
            .background(bgColor)
            .clickable(onClick = onTrackClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(track.albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(14.dp))

        // Track info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Options button
        IconButton(
            onClick = onOptionsClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Drag handle — receives touch on the handle only
        Icon(
            imageVector = Icons.Rounded.DragHandle,
            contentDescription = "Drag to reorder",
            tint = Color.White.copy(alpha = if (isDragged) 0.9f else 0.35f),
            modifier = Modifier
                .padding(start = 2.dp, end = 6.dp)
                .size(22.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.y)
                        }
                    )
                }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    sleepTimerRemaining: Long?,
    onDismiss: () -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onSetSleepTimerAtEndOfTrack: () -> Unit,
    onCancelSleepTimer: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF161617), // Deep premium dark grey
        contentColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.15f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = EcoDotRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Sleep Timer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Select when to pause playback",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Active Timer Status Card
            if (sleepTimerRemaining != null) {
                val timerText = if (sleepTimerRemaining == 0L) {
                    "End of Track"
                } else {
                    val m = sleepTimerRemaining / 60
                    val s = sleepTimerRemaining % 60
                    String.format("%02d:%02d remaining", m, s)
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .background(EcoDotRed.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .border(1.dp, EcoDotRed.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active Timer",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = EcoDotRed
                            )
                            Text(
                                text = timerText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        
                        TextButton(
                            onClick = {
                                onCancelSleepTimer()
                                onDismiss()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = EcoDotRed)
                        ) {
                            Text("Turn Off", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Durations Grid (2-column layout)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val durationRows = listOf(
                    listOf(5, 15),
                    listOf(30, 45),
                    listOf(60, 90)
                )

                durationRows.forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { minutes ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(14.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        onSetSleepTimer(minutes)
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$minutes min",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // End of Track Option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                        .clickable {
                            onSetSleepTimerAtEndOfTrack()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "End of Track",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

