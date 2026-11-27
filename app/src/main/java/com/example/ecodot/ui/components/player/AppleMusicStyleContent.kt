package com.example.ecodot.ui.components.player

import android.graphics.Bitmap
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.remote.YouTubeParser
import com.example.ecodot.ui.components.ExplicitBadge
import com.example.ecodot.ui.components.heartBurst
import com.example.ecodot.ui.components.rememberHeartBurstState
import com.example.ecodot.ui.theme.Headphones
import com.example.ecodot.ui.theme.Info
import com.example.ecodot.ui.theme.PlaylistAdd
import com.example.ecodot.ui.theme.QueueMusic
import com.example.ecodot.ui.theme.Repeat
import com.example.ecodot.ui.theme.RepeatOne
import com.example.ecodot.ui.theme.Shuffle
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Tune
import com.example.ecodot.ui.theme.poppinsFamily
import com.example.ecodot.ui.viewmodel.LyricLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

enum class AppleMusicTab { MAIN, LYRICS, QUEUE }

private val AppleTrackInactive = Color.White.copy(alpha = 0.24f)
private val AppleCapsuleBg = Color.White.copy(alpha = 0.14f)

/**
 * SimpMusic-exact Apple Music Now Playing Style:
 * - Warm artwork-derived 6x6 smoothstep mesh + frosted album art backdrop
 * - Uncluttered top edge with single centered drag-handle grabber (zero floating buttons over art)
 * - Edge-to-edge swipeable artwork (HorizontalPager) dissolving gently via DstIn mask,
 *   switching seamlessly to responsive framed Video mode with subtitles & overlay controls
 * - Bare white Star / More actions, inline glowing Lyric Strip, expanding thin scrubber,
 *   live Headphones + real-time "140 kbps · 48 kHz" telemetry row, double-arrow transport,
 *   system media volume bar, SimpMusic dock [Lyrics | (Headphones | EQ | Info) | Queue],
 *   and device model footer (e.g. V2334).
 */
@Composable
fun AppleMusicStyleContent(
    track: Track,
    queue: List<Track>,
    currentQueueIndex: Int,
    queueSourceTitle: String,
    controller: MediaController?,
    isPlaying: Boolean,
    isLiked: Boolean,
    shuffleMode: Boolean,
    repeatMode: Int,
    endlessMode: Boolean,
    position: Long,
    duration: Long,
    dominantColor: Color,
    accentColor: Color,
    syncedLyrics: List<LyricLine>,
    plainLyrics: String?,
    isLyricsLoading: Boolean,
    isCrossfading: Boolean,
    isTrackAVideo: Boolean,
    videoAspectRatio: Float,
    videoResizeMode: Int,
    streamBadgeLabel: String?,
    onBackClick: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSelectQueueIndex: (Int) -> Unit,
    onToggleLike: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleEndlessMode: () -> Unit,
    onToggleVideoResizeMode: () -> Unit,
    onEnterVideoFullscreen: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenPlaylistPicker: () -> Unit,
    onOpenFullLyrics: () -> Unit,
    onOpenShareLyrics: () -> Unit,
    onOpenTrackInfo: () -> Unit,
    onOpenOptionsMenu: (Track) -> Unit,
    onOpenArtist: () -> Unit,
    onOpenOutputSheet: () -> Unit,
    formatDuration: (Long) -> String,
    modifier: Modifier = Modifier,
    positionProvider: (() -> Long)? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val view = LocalView.current
    val readPosition = positionProvider ?: { position }

    var activeTab by rememberSaveable { mutableStateOf(AppleMusicTab.MAIN) }
    var showVideoOverlay by rememberSaveable { mutableStateOf(false) }
    var showCaptions by rememberSaveable { mutableStateOf(true) }
    var coverBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(showVideoOverlay, isPlaying, isTrackAVideo) {
        if (showVideoOverlay && isPlaying && isTrackAVideo) {
            delay(3000)
            showVideoOverlay = false
        }
    }

    val outputDeviceName by rememberActiveAudioOutputName()
    val seedColor = if (dominantColor == Color.Black) Color(0xFF42211D) else dominantColor

    val backdropArtUrl = remember(track.id, track.albumArtUri, isTrackAVideo) {
        YouTubeParser.getHighResUrl(track.albumArtUri, isWide = isTrackAVideo) ?: track.albumArtUri
    }

    // Build smoothstepped 32x32 mesh ImageBitmap off UI thread when coverBitmap changes
    val meshBitmap by produceState<ImageBitmap?>(initialValue = null, coverBitmap, track.id) {
        val bmp = coverBitmap
        if (bmp == null || bmp.isRecycled) {
            value = null
            return@produceState
        }
        value = withContext(Dispatchers.Default) {
            runCatching {
                val srcPx = 60
                val scaled = Bitmap.createScaledBitmap(bmp, srcPx, srcPx, true)
                val pixels = IntArray(srcPx * srcPx)
                scaled.getPixels(pixels, 0, srcPx, 0, 0, srcPx, srcPx)
                val outPixels = PlayerStyleMath.buildSmoothedMeshPixels(pixels, srcPx, srcPx, gridSize = 6, outSize = 32)
                val mesh = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
                mesh.setPixels(outPixels, 0, 32, 0, 0, 32, 32)
                mesh.asImageBitmap()
            }.getOrNull()
        }
    }

    val backdropBrush = remember(seedColor) {
        Brush.verticalGradient(
            0f to PlayerStyleMath.appleMusicGradientColorAt(seedColor, 0f),
            0.52f to PlayerStyleMath.appleMusicGradientColorAt(seedColor, 0.42f),
            1f to PlayerStyleMath.appleMusicGradientColorAt(seedColor, 0.85f)
        )
    }

    var bottomClusterHeightDp by remember { mutableIntStateOf(355) }
    val statusBarHeight = with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF100E10))
    ) {
        // ── 1. Warm Frosted Cover Art & 6x6 Smoothstep Mesh Backdrop ──────────
        Box(modifier = Modifier.fillMaxSize()) {
            var currentBackdropUrl by remember(backdropArtUrl) { mutableStateOf(backdropArtUrl) }
            if (!currentBackdropUrl.isNullOrBlank()) {
                // Downsampled 128x128 art with lightweight 14.dp blur scales cleanly for GPU 120Hz bokeh wash
                val blurMod = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.blur(14.dp)
                } else Modifier
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentBackdropUrl)
                        .size(128, 128)
                        .allowHardware(false)
                        .crossfade(450)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onSuccess = { result ->
                        coverBitmap = result.result.drawable.toBitmap()
                    },
                    onError = {
                        val fallback = YouTubeParser.getFallbackThumbUrl(currentBackdropUrl)
                        if (fallback != null && fallback != currentBackdropUrl) {
                            currentBackdropUrl = fallback
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurMod)
                )
            }

            Crossfade(targetState = meshBitmap, animationSpec = tween(750), label = "meshFade") { bmp ->
                if (bmp != null) {
                    val meshMod = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(10.dp)
                    } else Modifier
                    Image(
                        bitmap = bmp,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.High,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.60f)
                            .then(meshMod)
                    )
                }
            }

            // Warm palette overlay keeping vibrant artwork hues intact from top to footer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backdropBrush)
                    .alpha(0.56f)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        PlayerStyleMath.smoothScrimBrush(
                            from = Color.Black.copy(alpha = 0.04f),
                            to = Color.Black.copy(alpha = 0.22f),
                            startFraction = 0.50f,
                            endFraction = 1f
                        )
                    )
            )
        }

        // ── 2. Body Content Crossfade (MAIN / LYRICS / QUEUE) ─────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = activeTab,
                animationSpec = tween(260),
                label = "appleMusicBodyTab",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    AppleMusicTab.MAIN -> {
                        AppleMusicMainArtworkOrVideoLayer(
                            track = track,
                            queue = queue,
                            currentQueueIndex = currentQueueIndex,
                            controller = controller,
                            isTrackAVideo = isTrackAVideo,
                            videoAspectRatio = videoAspectRatio,
                            videoResizeMode = videoResizeMode,
                            showVideoOverlay = showVideoOverlay,
                            showCaptions = showCaptions,
                            syncedLyrics = syncedLyrics,
                            position = position,
                            positionProvider = readPosition,
                            statusBarHeight = statusBarHeight,
                            bottomClusterHeightDp = bottomClusterHeightDp,
                            onSelectQueueIndex = onSelectQueueIndex,
                            onToggleVideoOverlay = { showVideoOverlay = !showVideoOverlay },
                            onToggleCaptions = { showCaptions = !showCaptions },
                            onToggleVideoResizeMode = onToggleVideoResizeMode,
                            onEnterVideoFullscreen = onEnterVideoFullscreen,
                            onSeekBackward5s = { onSeekTo((readPosition() - 5000L).coerceAtLeast(0L)) },
                            onSeekForward5s = { onSeekTo((readPosition() + 5000L).coerceAtMost(duration.coerceAtLeast(1L))) }
                        )
                    }

                    AppleMusicTab.LYRICS -> {
                        AppleMusicLyricsBody(
                            track = track,
                            isLiked = isLiked,
                            syncedLyrics = syncedLyrics,
                            plainLyrics = plainLyrics,
                            isLyricsLoading = isLyricsLoading,
                            position = position,
                            positionProvider = readPosition,
                            accentColor = accentColor,
                            bottomPaddingDp = bottomClusterHeightDp,
                            onToggleLike = onToggleLike,
                            onOpenOptions = { onOpenOptionsMenu(track) },
                            onOpenArtist = onOpenArtist,
                            onSeekTo = onSeekTo,
                            onOpenFullLyrics = onOpenFullLyrics,
                            onOpenShareLyrics = onOpenShareLyrics
                        )
                    }

                    AppleMusicTab.QUEUE -> {
                        AppleMusicQueueBody(
                            track = track,
                            queue = queue,
                            currentQueueIndex = currentQueueIndex,
                            queueSourceTitle = queueSourceTitle,
                            isLiked = isLiked,
                            shuffleMode = shuffleMode,
                            repeatMode = repeatMode,
                            endlessMode = endlessMode,
                            bottomPaddingDp = bottomClusterHeightDp,
                            onToggleLike = onToggleLike,
                            onOpenOptions = onOpenOptionsMenu,
                            onOpenArtist = onOpenArtist,
                            onOpenTrackInfo = onOpenTrackInfo,
                            onOpenPlaylistPicker = onOpenPlaylistPicker,
                            onToggleShuffle = onToggleShuffle,
                            onToggleRepeat = onToggleRepeat,
                            onToggleEndlessMode = onToggleEndlessMode,
                            onSelectQueueIndex = onSelectQueueIndex
                        )
                    }
                }
            }

            // ── 3. Single Centered Grabber Handle (Clean SimpMusic parity) ─────
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 6.dp)
                    .clip(CircleShape)
                    .clickable { onBackClick() }
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.42f))
                )
            }

            // ── 4. Pinned Bottom Controls Cluster & Apple Music Dock ──────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        bottomClusterHeightDp = with(density) { coords.size.height.toDp().value.toInt() }
                    }
                    .navigationBarsPadding()
                    .padding(bottom = 14.dp)
            ) {
                if (activeTab == AppleMusicTab.MAIN) {
                    AppleMusicTitleAndActionsRow(
                        track = track,
                        isLiked = isLiked,
                        onToggleLike = onToggleLike,
                        onOpenArtist = onOpenArtist,
                        onOpenOptions = { onOpenOptionsMenu(track) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AppleMusicInlineLyricStrip(
                        syncedLyrics = syncedLyrics,
                        positionProvider = readPosition,
                        onClick = { if (syncedLyrics.isNotEmpty()) activeTab = AppleMusicTab.LYRICS }
                    )
                }

                AppleMusicBottomControlsCluster(
                    positionProvider = readPosition,
                    duration = duration,
                    isPlaying = isPlaying,
                    isCrossfading = isCrossfading,
                    streamBadgeLabel = streamBadgeLabel,
                    activeTab = activeTab,
                    hasLyrics = syncedLyrics.isNotEmpty() || !plainLyrics.isNullOrBlank(),
                    outputDeviceName = outputDeviceName,
                    onSelectTab = { target ->
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        activeTab = if (activeTab == target) AppleMusicTab.MAIN else target
                    },
                    onSeekTo = onSeekTo,
                    onPlayPause = onPlayPause,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onOpenOutputSheet = onOpenOutputSheet,
                    onOpenEqualizer = onOpenEqualizer,
                    onOpenTrackInfo = onOpenTrackInfo,
                    formatDuration = formatDuration
                )
            }
        }
    }
}

@Composable
private fun AppleMusicMainArtworkOrVideoLayer(
    track: Track,
    queue: List<Track>,
    currentQueueIndex: Int,
    controller: MediaController?,
    isTrackAVideo: Boolean,
    videoAspectRatio: Float,
    videoResizeMode: Int,
    showVideoOverlay: Boolean,
    showCaptions: Boolean,
    syncedLyrics: List<LyricLine>,
    position: Long,
    positionProvider: (() -> Long)? = null,
    statusBarHeight: Dp,
    bottomClusterHeightDp: Int,
    onSelectQueueIndex: (Int) -> Unit,
    onToggleVideoOverlay: () -> Unit,
    onToggleCaptions: () -> Unit,
    onToggleVideoResizeMode: () -> Unit,
    onEnterVideoFullscreen: () -> Unit,
    onSeekBackward5s: () -> Unit,
    onSeekForward5s: () -> Unit
) {
    val context = LocalContext.current
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val artworkHeight = minOf(maxWidth, maxHeight * 0.62f)
        val topInset = statusBarHeight

        if (isTrackAVideo) {
            BoxWithConstraints(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .padding(bottom = (bottomClusterHeightDp + 14).dp)
            ) {
                val topChrome = statusBarHeight + 28.dp
                val bounds = PlayerStyleMath.computeResponsiveVideoFrame(
                    maxWidthDp = maxWidth.value,
                    maxHeightDp = maxHeight.value,
                    topChromeDp = topChrome.value,
                    bottomGapDp = 12f,
                    aspectRatio = videoAspectRatio
                )
                ResponsivePlayerVideoSurface(
                    controller = controller,
                    trackId = track.id,
                    videoResizeMode = videoResizeMode,
                    showControls = showVideoOverlay,
                    showCaptions = showCaptions,
                    syncedLyrics = syncedLyrics,
                    positionMs = position,
                    positionProvider = positionProvider ?: { position },
                    onToggleControls = onToggleVideoOverlay,
                    onToggleCaptions = onToggleCaptions,
                    onToggleResizeMode = onToggleVideoResizeMode,
                    onEnterFullscreen = onEnterVideoFullscreen,
                    onSeekBackward5s = onSeekBackward5s,
                    onSeekForward5s = onSeekForward5s,
                    fallbackArtUrl = track.albumArtUri,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = bounds.topDp.dp)
                        .size(width = bounds.widthDp.dp, height = bounds.heightDp.dp)
                        .then(if (bounds.isPortrait) Modifier.clip(RoundedCornerShape(16.dp)) else Modifier)
                )
            }
        } else {
            val safeQueue = remember(queue, track) { if (queue.isNotEmpty()) queue else listOf(track) }
            val safeIndex = remember(safeQueue, currentQueueIndex) {
                currentQueueIndex.coerceIn(0, (safeQueue.size - 1).coerceAtLeast(0))
            }
            val pagerState = rememberPagerState(initialPage = safeIndex) { safeQueue.size }

            LaunchedEffect(safeIndex, safeQueue.size) {
                if (safeQueue.isNotEmpty() && pagerState.currentPage != safeIndex && !pagerState.isScrollInProgress) {
                    pagerState.scrollToPage(safeIndex)
                }
            }
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.settledPage }
                    .distinctUntilChanged()
                    .collect { settled ->
                        if (settled != safeIndex && settled in safeQueue.indices) {
                            onSelectQueueIndex(settled)
                        }
                    }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                key = { idx -> "${safeQueue.getOrNull(idx)?.id.orEmpty()}_$idx" }
            ) { page ->
                val pageTrack = safeQueue.getOrNull(page) ?: track
                val pageUrl = remember(pageTrack.id, pageTrack.albumArtUri) {
                    YouTubeParser.getHighResUrl(pageTrack.albumArtUri, isWide = false) ?: pageTrack.albumArtUri
                }
                var activeUrl by remember(pageUrl) { mutableStateOf(pageUrl) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topInset)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(activeUrl)
                            .size(720, 720)
                            .crossfade(400)
                            .build(),
                        contentDescription = pageTrack.title,
                        contentScale = ContentScale.Crop,
                        onError = {
                            val fallback = YouTubeParser.getFallbackThumbUrl(activeUrl)
                            if (fallback != null && fallback != activeUrl) activeUrl = fallback
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(artworkHeight)
                            .appleMusicDissolveEdges(topFraction = 0.08f, bottomFraction = 0.42f)
                    )
                }
            }

            // Gentle wash under status bar and top grabber
            val washHeight = topInset + 36.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(washHeight)
                    .background(
                        PlayerStyleMath.smoothScrimBrush(
                            from = Color.Black.copy(alpha = 0.36f),
                            to = Color.Transparent,
                            startFraction = 0f,
                            endFraction = 1f
                        )
                    )
            )
        }
    }
}

private fun Modifier.appleMusicDissolveEdges(
    topFraction: Float,
    bottomFraction: Float
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val topSteps = 10
        val stops = mutableListOf<Pair<Float, Color>>()
        for (i in 0..topSteps) {
            val p = i / topSteps.toFloat()
            stops.add((p * topFraction) to Color.Black.copy(alpha = PlayerStyleMath.smoothstep(p)))
        }
        val bottomStart = (1f - bottomFraction).coerceIn(topFraction + 0.05f, 0.95f)
        val botSteps = 14
        for (i in 0..botSteps) {
            val p = i / botSteps.toFloat()
            val pos = bottomStart + (1f - bottomStart) * p
            stops.add(pos to Color.Black.copy(alpha = 1f - PlayerStyleMath.smoothstep(p)))
        }
        drawRect(
            brush = Brush.verticalGradient(colorStops = stops.toTypedArray()),
            blendMode = BlendMode.DstIn
        )
    }

@Composable
private fun AppleMusicTitleAndActionsRow(
    track: Track,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    onOpenArtist: () -> Unit,
    onOpenOptions: () -> Unit
) {
    val heartBurst = rememberHeartBurstState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.title,
                    fontFamily = poppinsFamily,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .basicMarquee()
                )
                if (track.isExplicit || track.title.contains("explicit", ignoreCase = true)) {
                    Spacer(modifier = Modifier.width(6.dp))
                    ExplicitBadge()
                }
            }
            Text(
                text = track.artist,
                fontFamily = poppinsFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable(onClick = onOpenArtist)
                    .padding(top = 1.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Bare white icons matching SimpMusic Image 2
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .heartBurst(heartBurst)
                    .clip(CircleShape)
                    .clickable {
                        if (!isLiked) heartBurst.fire()
                        onToggleLike()
                    },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = isLiked, label = "appleStarLike") { liked ->
                    Icon(
                        imageVector = if (liked) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = "Favorite",
                        tint = Color.White,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            IconButton(
                onClick = onOpenOptions,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun AppleMusicInlineLyricStrip(
    syncedLyrics: List<LyricLine>,
    positionProvider: () -> Long,
    onClick: () -> Unit
) {
    val activeLine by remember(syncedLyrics, positionProvider) {
        derivedStateOf {
            val pos = positionProvider()
            if (syncedLyrics.isEmpty()) null
            else syncedLyrics.lastOrNull { it.timeMs <= pos }?.text
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(horizontal = 24.dp)
            .clickable(enabled = syncedLyrics.isNotEmpty(), onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedContent(
            targetState = activeLine,
            transitionSpec = {
                (slideInVertically { it / 2 } + fadeIn(tween(240))) togetherWith
                    (slideOutVertically { -it / 2 } + fadeOut(tween(180))) using
                    SizeTransform(clip = false)
            },
            label = "appleLyricStrip"
        ) { lineText ->
            if (!lineText.isNullOrBlank()) {
                Text(
                    text = lineText,
                    fontFamily = poppinsFamily,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee()
                )
            } else if (syncedLyrics.isNotEmpty()) {
                Text(
                    text = "• • •",
                    fontFamily = poppinsFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
private fun AppleMusicBottomControlsCluster(
    positionProvider: () -> Long,
    duration: Long,
    isPlaying: Boolean,
    isCrossfading: Boolean,
    streamBadgeLabel: String?,
    activeTab: AppleMusicTab,
    hasLyrics: Boolean,
    outputDeviceName: String,
    onSelectTab: (AppleMusicTab) -> Unit,
    onSeekTo: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenOutputSheet: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenTrackInfo: () -> Unit,
    formatDuration: (Long) -> String
) {
    val (volumeFraction, setVolumeFraction) = rememberSystemMediaVolumeController()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        AppleMusicScrubberSection(
            positionProvider = positionProvider,
            duration = duration,
            isCrossfading = isCrossfading,
            streamBadgeLabel = streamBadgeLabel,
            onSeekTo = onSeekTo,
            formatDuration = formatDuration
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Apple Music Double-Triangle FastRewind / Play-Pause / FastForward
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppleMusicPressButton(
                icon = Icons.Rounded.FastRewind,
                iconSize = 40.dp,
                contentDescription = "Previous",
                onClick = onPrevious
            )

            AppleMusicPressButton(
                icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                iconSize = 48.dp,
                contentDescription = if (isPlaying) "Pause" else "Play",
                onClick = onPlayPause
            )

            AppleMusicPressButton(
                icon = Icons.Rounded.FastForward,
                iconSize = 40.dp,
                contentDescription = "Next",
                onClick = onNext
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Real hardware media volume slider on MAIN, LYRICS & QUEUE matching Images 2 & 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeMute,
                contentDescription = "Mute volume",
                tint = Color.White.copy(alpha = 0.68f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AppleMusicThinSlider(
                value = volumeFraction,
                activeColor = Color.White.copy(alpha = 0.90f),
                onValueChange = setVolumeFraction,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = "Increase volume",
                tint = Color.White.copy(alpha = 0.68f),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SimpMusic Apple Dock: Bare Lyrics (highlighted ONLY when active) | [Headphones | EQ | Info] | Bare Queue (highlighted ONLY when active)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppleMusicDockButton(
                icon = Icons.Rounded.Lyrics,
                selected = activeTab == AppleMusicTab.LYRICS,
                enabled = hasLyrics || activeTab == AppleMusicTab.LYRICS,
                contentDescription = "Lyrics",
                onClick = { onSelectTab(AppleMusicTab.LYRICS) }
            )

            Surface(
                shape = CircleShape,
                color = AppleCapsuleBg,
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onOpenOutputSheet,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = SimpIcons.Headphones,
                            contentDescription = "Audio output",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(Color.White.copy(alpha = 0.18f))
                    )
                    IconButton(
                        onClick = onOpenEqualizer,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = SimpIcons.Tune,
                            contentDescription = "Equalizer",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(Color.White.copy(alpha = 0.18f))
                    )
                    IconButton(
                        onClick = onOpenTrackInfo,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = SimpIcons.Info,
                            contentDescription = "Track details",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            AppleMusicDockButton(
                icon = SimpIcons.QueueMusic,
                selected = activeTab == AppleMusicTab.QUEUE,
                enabled = true,
                contentDescription = "Queue",
                onClick = { onSelectTab(AppleMusicTab.QUEUE) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Output Device Model Name Footer (e.g. V2334)
        Crossfade(
            targetState = outputDeviceName,
            animationSpec = tween(250),
            label = "outputFooter",
            modifier = Modifier.fillMaxWidth()
        ) { devName ->
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = devName,
                    fontFamily = poppinsFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenOutputSheet)
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AppleMusicDockButton(
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    val container by animateColorAsState(
        targetValue = if (selected) Color.White.copy(alpha = 0.90f) else Color.Transparent,
        label = "dockBg"
    )
    val tint by animateColorAsState(
        targetValue = when {
            selected -> Color(0xFF1A1414)
            enabled -> Color.White.copy(alpha = 0.90f)
            else -> Color.White.copy(alpha = 0.38f)
        },
        label = "dockTint"
    )
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun AppleMusicPressButton(
    icon: ImageVector,
    iconSize: Dp,
    contentDescription: String,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.18f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "applePressScale"
    )
    Box(
        modifier = Modifier
            .size(iconSize + 20.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(bounded = false, radius = 34.dp),
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppleMusicThinSlider(
    value: Float,
    activeColor: Color,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val dragged by interactionSource.collectIsDraggedAsState()
    val trackHeight by animateDpAsState(
        targetValue = if (pressed || dragged) 14.dp else 7.5.dp,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 320f),
        label = "appleThinSliderHeight"
    )
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Slider(
            value = value.coerceIn(0f, 1f),
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            interactionSource = interactionSource,
            modifier = modifier.height(22.dp),
            track = {
                val fraction = value.coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(trackHeight)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(AppleTrackInactive)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .background(activeColor)
                    )
                }
            },
            thumb = {
                Spacer(Modifier.size(0.dp))
            }
        )
    }
}

@Composable
private fun AppleMusicCompactTrackHeader(
    track: Track,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    onOpenArtist: () -> Unit,
    onOpenOptions: () -> Unit
) {
    val context = LocalContext.current
    val thumbUrl = remember(track.id, track.albumArtUri) {
        YouTubeParser.getHighResUrl(track.albumArtUri, isWide = false) ?: track.albumArtUri
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 24.dp, start = 20.dp, end = 16.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(thumbUrl)
                .size(160, 160)
                .crossfade(true)
                .build(),
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.title,
                    fontFamily = poppinsFamily,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (track.isExplicit) {
                    Spacer(modifier = Modifier.width(6.dp))
                    ExplicitBadge()
                }
            }
            Text(
                text = track.artist,
                fontFamily = poppinsFamily,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.68f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onOpenArtist)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        IconButton(onClick = onToggleLike, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (isLiked) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = "Favorite",
                tint = Color.White,
                modifier = Modifier.size(23.dp)
            )
        }
        IconButton(onClick = onOpenOptions, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun AppleMusicLyricsBody(
    track: Track,
    isLiked: Boolean,
    syncedLyrics: List<LyricLine>,
    plainLyrics: String?,
    isLyricsLoading: Boolean,
    position: Long,
    accentColor: Color,
    bottomPaddingDp: Int,
    onToggleLike: () -> Unit,
    onOpenOptions: () -> Unit,
    onOpenArtist: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onOpenFullLyrics: () -> Unit,
    onOpenShareLyrics: () -> Unit,
    positionProvider: (() -> Long)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = (bottomPaddingDp + 8).dp)
    ) {
        AppleMusicCompactTrackHeader(
            track = track,
            isLiked = isLiked,
            onToggleLike = onToggleLike,
            onOpenArtist = onOpenArtist,
            onOpenOptions = onOpenOptions
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            LyricsPanel(
                syncedLyrics = syncedLyrics,
                currentPosition = position,
                plainLyrics = plainLyrics,
                isLyricsLoading = isLyricsLoading,
                onSeek = onSeekTo,
                isFullScreen = false,
                backgroundColor = Color.Transparent,
                accentColor = if (accentColor == Color.Black) Color.White else accentColor,
                positionProvider = positionProvider
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AppleCapsuleBg,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable(onClick = onOpenShareLyrics)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share lyrics",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = AppleCapsuleBg,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable(onClick = onOpenFullLyrics)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInFull,
                            contentDescription = "Fullscreen lyrics",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppleMusicScrubberSection(
    positionProvider: () -> Long,
    duration: Long,
    isCrossfading: Boolean,
    streamBadgeLabel: String?,
    onSeekTo: (Long) -> Unit,
    formatDuration: (Long) -> String
) {
    val safeDuration = duration.coerceAtLeast(1L)
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableFloatStateOf(0f) }
    val liveFraction by remember(safeDuration, positionProvider) {
        derivedStateOf {
            if (isScrubbing) scrubFraction else (positionProvider().toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
        }
    }
    val displaySec by remember(safeDuration, positionProvider) {
        derivedStateOf {
            val ms = if (isScrubbing) (scrubFraction * safeDuration).toLong() else positionProvider()
            (ms.coerceAtLeast(0L) / 1000L) * 1000L
        }
    }
    val remainingSec = (safeDuration - displaySec).coerceAtLeast(0L)
    val (_, shimmerSweep) = rememberCrossfadeVisuals(isCrossfading)

    Column(modifier = Modifier.fillMaxWidth()) {
        AppleMusicThinSlider(
            value = liveFraction,
            activeColor = Color.White,
            onValueChange = {
                isScrubbing = true
                scrubFraction = it
            },
            onValueChangeFinished = {
                onSeekTo((scrubFraction * safeDuration).toLong())
                isScrubbing = false
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(displaySec),
                fontFamily = poppinsFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.65f)
            )

            Box(contentAlignment = Alignment.Center) {
                if (isCrossfading) {
                    CrossfadingShimmerLabel(
                        isCrossfading = true,
                        shimmerSweep = shimmerSweep,
                        fontSize = 11.5.sp
                    )
                } else if (!streamBadgeLabel.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = SimpIcons.Headphones,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.60f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = streamBadgeLabel,
                            fontFamily = poppinsFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.68f),
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                text = "-${formatDuration(remainingSec)}",
                fontFamily = poppinsFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun AppleMusicQueueBody(
    track: Track,
    queue: List<Track>,
    currentQueueIndex: Int,
    queueSourceTitle: String,
    isLiked: Boolean,
    shuffleMode: Boolean,
    repeatMode: Int,
    endlessMode: Boolean,
    bottomPaddingDp: Int,
    onToggleLike: () -> Unit,
    onOpenOptions: (Track) -> Unit,
    onOpenArtist: () -> Unit,
    onOpenTrackInfo: () -> Unit,
    onOpenPlaylistPicker: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleEndlessMode: () -> Unit,
    onSelectQueueIndex: (Int) -> Unit
) {
    val context = LocalContext.current
    val upcoming = remember(queue, currentQueueIndex) {
        if (currentQueueIndex in queue.indices) {
            queue.subList(currentQueueIndex + 1, queue.size)
        } else queue
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = (bottomPaddingDp + 8).dp)
    ) {
        AppleMusicCompactTrackHeader(
            track = track,
            isLiked = isLiked,
            onToggleLike = onToggleLike,
            onOpenArtist = onOpenArtist,
            onOpenOptions = { onOpenOptions(track) }
        )

        // 4 Frosted Capsule Pills: Info | Add to Playlist | Shuffle | Repeat (Image 3 exact)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QueueActionChip(
                icon = SimpIcons.Info,
                active = false,
                modifier = Modifier.weight(1f),
                onClick = onOpenTrackInfo
            )
            QueueActionChip(
                icon = SimpIcons.PlaylistAdd,
                active = false,
                modifier = Modifier.weight(1f),
                onClick = onOpenPlaylistPicker
            )
            QueueActionChip(
                icon = SimpIcons.Shuffle,
                active = shuffleMode,
                modifier = Modifier.weight(1f),
                onClick = onToggleShuffle
            )
            QueueActionChip(
                icon = if (repeatMode == Player.REPEAT_MODE_ONE) SimpIcons.RepeatOne else SimpIcons.Repeat,
                active = repeatMode != Player.REPEAT_MODE_OFF,
                modifier = Modifier.weight(1f),
                onClick = onToggleRepeat
            )
        }

        // Now Playing / Playlist Name + Endless Queue Switch (Image 3 exact)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Now Playing",
                    fontFamily = poppinsFamily,
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.70f)
                )
                Text(
                    text = queueSourceTitle.ifBlank { track.album.takeIf { it.isNotBlank() && it != "Unknown" } ?: "Up Next" },
                    fontFamily = poppinsFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Endless queue",
                    fontFamily = poppinsFamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.78f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = endlessMode,
                    onCheckedChange = { onToggleEndlessMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF1E1A1A),
                        checkedTrackColor = Color.White.copy(alpha = 0.88f),
                        uncheckedThumbColor = Color(0xFF1E1A1A),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.78f)
                    )
                )
            }
        }

        // Flush, borderless queue track rows sitting directly over the frosted mesh (Image 3 exact)
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (upcoming.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No upcoming tracks in queue",
                            fontFamily = poppinsFamily,
                            fontSize = 13.5.sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    }
                }
            } else {
                itemsIndexed(upcoming, key = { idx, t -> "${t.id}_$idx" }) { idx, qTrack ->
                    val absoluteIdx = currentQueueIndex + 1 + idx
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectQueueIndex(absoluteIdx) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(YouTubeParser.getHighResUrl(qTrack.albumArtUri, isWide = false) ?: qTrack.albumArtUri)
                                .size(160, 160)
                                .crossfade(true)
                                .build(),
                            contentDescription = qTrack.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = qTrack.title,
                                fontFamily = poppinsFamily,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = qTrack.artist,
                                fontFamily = poppinsFamily,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.68f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { onOpenOptions(qTrack) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Track options",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueActionChip(
    icon: ImageVector,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (active) Color.White.copy(alpha = 0.90f) else AppleCapsuleBg,
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) Color(0xFF1A1414) else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
