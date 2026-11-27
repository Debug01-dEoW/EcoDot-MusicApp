package com.example.ecodot.ui.components.player

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ecodot.ui.components.TrackOptionsBottomSheet
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.screens.shareLyricsCard
import com.example.ecodot.ui.viewmodel.LyricLine
import com.example.ecodot.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// Apple Music Lyrics design constants from SimpMusic
val AppleMusicLyricFontSize = 28.sp
val AppleMusicLyricLineHeight = 34.sp
val AppleMusicLyricGap = 16.dp
val AppleMusicInactiveLineColor = Color(0xFF9B9B9B)

private const val BLUR_PER_LINE_EM = 0.095f
private const val BLUR_MAX_EM = 0.45f
private const val ACTIVE_LINE_ALPHA = 1f
private const val ALPHA_FALLOFF_PER_LINE = 0.25f
private const val MIN_LINE_ALPHA = 0.25f
private const val PRE_ROLL_LINE_ALPHA = 0.6f
private const val CLUSTER_AUTO_HIDE_MS = 8000L

/**
 * Depth-of-Field focus treatment for one lyric line.
 * When dragging or scrolling, blur temporarily drops to 0.dp so text is immediately sharp.
 */
@Composable
fun Modifier.appleMusicLyricFocus(
    distanceFromCurrent: Int,
    isDragging: Boolean,
    hasActiveLine: Boolean = true,
    fontSize: TextUnit = AppleMusicLyricFontSize
): Modifier {
    val distance = when {
        distanceFromCurrent < 0 -> abs(distanceFromCurrent) + 1
        else -> distanceFromCurrent
    }
    val fontSizeDp = with(LocalDensity.current) { fontSize.toDp() }
    val targetBlur: Dp = if (isDragging || !hasActiveLine || distanceFromCurrent == 0) {
        0.dp
    } else {
        fontSizeDp * (distance * BLUR_PER_LINE_EM).coerceAtMost(BLUR_MAX_EM)
    }
    val targetAlpha = when {
        !hasActiveLine -> PRE_ROLL_LINE_ALPHA
        distanceFromCurrent == 0 -> ACTIVE_LINE_ALPHA
        else -> (1f - distance * ALPHA_FALLOFF_PER_LINE).coerceAtLeast(MIN_LINE_ALPHA)
    }

    val blurRadius by animateDpAsState(
        targetValue = targetBlur,
        animationSpec = tween(350),
        label = "appleMusicLyricBlur"
    )
    val lineAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(350),
        label = "appleMusicLyricAlpha"
    )

    return this
        .alpha(lineAlpha)
        .then(
            if (blurRadius > 0.dp) {
                Modifier.blur(blurRadius, BlurredEdgeTreatment.Unbounded)
            } else {
                Modifier
            }
        )
}

/**
 * Smooth top and bottom dissolve edges so lyrics seamlessly blend into ambient background.
 */
fun Modifier.appleMusicVerticalFadeEdges(
    topFade: Dp = 32.dp,
    bottomFade: Dp = 24.dp
): Modifier = this
    .graphicsLayer(alpha = 0.99f)
    .drawWithContent {
        drawContent()
        val topPx = topFade.toPx()
        val bottomPx = bottomFade.toPx()
        if (topPx > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = topPx
                ),
                blendMode = BlendMode.DstIn
            )
        }
        if (bottomPx > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = size.height - bottomPx,
                    endY = size.height
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }

/**
 * SimpMusic animated ambient gradient background:
 * Drifts and rotates continuously across multiple angles and coordinates.
 */
@Composable
fun AnimatedLyricsGradientBackground(
    color: Color,
    modifier: Modifier = Modifier
) {
    val startColor = remember { Animatable(color) }
    val midColor1 = remember { Animatable(color.copy(alpha = 0.95f)) }
    val midColor2 = remember { Animatable(color.copy(alpha = 0.85f)) }
    val endColor = remember { Animatable(Color.Black) }

    val gradientTransition = rememberInfiniteTransition(label = "lyricsGradient")
    val animatedAngle by gradientTransition.animateFloat(
        initialValue = -45f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lyricsGradientAngle"
    )
    val animatedOffsetX by gradientTransition.animateFloat(
        initialValue = -1500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lyricsGradientOffsetX"
    )
    val animatedOffsetY by gradientTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lyricsGradientOffsetY"
    )

    LaunchedEffect(color) {
        launch {
            startColor.animateTo(
                targetValue = color,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
        launch {
            midColor1.animateTo(
                targetValue = color.copy(alpha = 0.95f),
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
        launch {
            midColor2.animateTo(
                targetValue = color.copy(alpha = 0.85f),
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
        launch {
            endColor.animateTo(
                targetValue = Color.Black,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val radAngle = animatedAngle * PI.toFloat() / 180f
                val radOpp = (animatedAngle + 180f) * PI.toFloat() / 180f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            startColor.value,
                            midColor1.value,
                            midColor2.value,
                            endColor.value.copy(alpha = 0.9f),
                            endColor.value
                        ),
                        start = Offset(
                            x = animatedOffsetX + (cos(radAngle) * 800f),
                            y = animatedOffsetY + (sin(radAngle) * 800f)
                        ),
                        end = Offset(
                            x = animatedOffsetX + 2500f + (cos(radOpp) * 800f),
                            y = animatedOffsetY + 2500f + (sin(radOpp) * 800f)
                        )
                    )
                )
            }
    )
}

/**
 * LyricsPanel for inline now playing card and fallback lyrics display.
 * Renders with SimpMusic's Apple Music blurred line focus and tap-to-seek.
 */
@Composable
fun LyricsPanel(
    syncedLyrics: List<LyricLine>,
    currentPosition: Long,
    plainLyrics: String?,
    isLyricsLoading: Boolean,
    onSeek: (Long) -> Unit,
    isFullScreen: Boolean = false,
    backgroundColor: Color = Color.Transparent,
    accentColor: Color = Color(0xFF79DA9F),
    positionProvider: (() -> Long)? = null
) {
    val listState = rememberLazyListState()
    val isDragging by listState.interactionSource.collectIsDraggedAsState()
    val currentPositionState = rememberUpdatedState(currentPosition)
    val currentLineIndex by remember(syncedLyrics, positionProvider) {
        derivedStateOf {
            val pos = positionProvider?.invoke() ?: currentPositionState.value
            syncedLyrics.indexOfLast { it.timeMs <= pos }
        }
    }

    LaunchedEffect(currentLineIndex, isDragging) {
        if (currentLineIndex >= 0 && !isDragging) {
            delay(16)
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val itemInfo = visibleItems.firstOrNull { it.index == currentLineIndex }
            if (itemInfo != null) {
                val center = layoutInfo.viewportEndOffset / 2
                val childCenter = itemInfo.offset + itemInfo.size / 2
                val scrollDelta = (childCenter - center).toFloat()
                if (abs(scrollDelta) > 1f) {
                    listState.animateScrollBy(scrollDelta, tween(500, easing = FastOutSlowInEasing))
                }
            } else {
                listState.animateScrollToItem(currentLineIndex)
            }
        }
    }

    val fontSize = if (isFullScreen) AppleMusicLyricFontSize else 22.sp
    val lineHeight = if (isFullScreen) AppleMusicLyricLineHeight else 28.sp
    val gap = if (isFullScreen) AppleMusicLyricGap else 12.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .appleMusicVerticalFadeEdges(topFade = 24.dp, bottomFade = 24.dp)
    ) {
        if (isLyricsLoading && syncedLyrics.isEmpty() && plainLyrics.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.5.dp
                )
            }
        } else if (syncedLyrics.isEmpty()) {
            val scrollState = rememberScrollState()
            Text(
                text = if (!plainLyrics.isNullOrEmpty()) plainLyrics
                else if (isLyricsLoading) "Searching for lyrics..."
                else "No lyrics available.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = fontSize,
                    lineHeight = lineHeight
                ),
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .verticalScroll(scrollState),
                textAlign = TextAlign.Start
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = if (isFullScreen) 280.dp else 40.dp),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                itemsIndexed(syncedLyrics, key = { index, _ -> "lyric_$index" }) { index, line ->
                    val isCurrent = index == currentLineIndex
                    val distanceFromCurrent = if (currentLineIndex >= 0) index - currentLineIndex else 0

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .appleMusicLyricFocus(
                                distanceFromCurrent = distanceFromCurrent,
                                isDragging = isDragging,
                                hasActiveLine = currentLineIndex >= 0,
                                fontSize = fontSize
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSeek(line.timeMs) }
                            .padding(vertical = 4.dp, horizontal = 20.dp),
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
                            color = if (isCurrent) Color.White else AppleMusicInactiveLineColor,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

/**
 * SimpMusic Audio Waveform Seekbar matching Image 2:
 * Dynamic vertical rounded bars with glowing thumb, real-time scrubbing, and crisp responsive feedback.
 */
@Composable
fun AudioWaveformSeekBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    barCount: Int = 48,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.28f),
    seed: Int = 42
) {
    val barHeights = remember(seed, barCount) {
        val random = kotlin.random.Random(seed)
        FloatArray(barCount) { i ->
            val pos = i.toFloat() / barCount
            val envelope = kotlin.math.sin(pos * kotlin.math.PI.toFloat()).coerceIn(0.35f, 1.0f)
            val noise = (0.35f + random.nextFloat() * 0.65f)
            (envelope * noise).coerceIn(0.20f, 1.0f)
        }
    }

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    val displayProgress = if (isDragging) dragProgress else progress

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(newProgress)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(dragProgress)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek(dragProgress)
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeek(dragProgress)
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val totalBars = barHeights.size
            val barSpacingPx = 3.dp.toPx()
            val totalSpacing = barSpacingPx * (totalBars - 1)
            val barWidthPx = ((size.width - totalSpacing) / totalBars).coerceAtLeast(2.dp.toPx())

            for (i in 0 until totalBars) {
                val x = i * (barWidthPx + barSpacingPx) + barWidthPx / 2f
                val barFraction = i.toFloat() / (totalBars - 1)
                val barH = (barHeights[i] * size.height * 0.85f).coerceAtLeast(4.dp.toPx())
                val topY = (size.height - barH) / 2f
                val bottomY = topY + barH

                val isPlayed = barFraction <= displayProgress
                val color = if (isPlayed) activeColor else inactiveColor

                drawLine(
                    color = color,
                    start = Offset(x, topY),
                    end = Offset(x, bottomY),
                    strokeWidth = barWidthPx,
                    cap = StrokeCap.Round
                )
            }

            // Draw glowing thumb at the current playhead
            val thumbXPx = (displayProgress * size.width).coerceIn(8.dp.toPx(), size.width - 8.dp.toPx())
            val thumbYPx = size.height / 2f

            // Outer soft glow halo
            drawCircle(
                color = Color.White.copy(alpha = 0.28f),
                radius = 14.dp.toPx(),
                center = Offset(thumbXPx, thumbYPx)
            )
            // Inner crisp white core
            drawCircle(
                color = Color.White,
                radius = 6.5.dp.toPx(),
                center = Offset(thumbXPx, thumbYPx)
            )
        }
    }
}

/**
 * Fullscreen lyrics content matching SimpMusic:
 * - Animated ambient gradient background mesh based on track dominant color
 * - Apple Music header (45dp artwork + marquee title & artist + like button + share + close)
 * - Apple Music lyrics list (depth-of-field blur, bold white active, grey inactive, tap to seek)
 * - Auto-centering smoothly follows active line
 * - Audio Waveform progress seekbar with glowing thumb and elapsed & remaining timestamps (Image 2 exact)
 * - Transport controls (Share, Previous, 56dp Play/Pause Circle, Next, More options) with auto-hide
 *   after 8s of inactivity and waking on scroll or touch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenLyricsView(
    viewModel: MusicViewModel,
    currentTrack: Track?,
    dominantColor: Color,
    syncedLyrics: List<LyricLine>,
    plainLyrics: String?,
    isLyricsLoading: Boolean,
    currentPosition: Long,
    onClose: () -> Unit,
    positionProvider: (() -> Long)? = null
) {
    val accentColor by viewModel.accentColor.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val shuffleMode by viewModel.shuffleMode.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isLiked by viewModel.isCurrentTrackLiked.collectAsState()
    val isCrossfading by viewModel.isCrossfading.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedLines by rememberSaveable { mutableStateOf<Set<Int>>(emptySet()) }
    var isSharing by remember { mutableStateOf(false) }
    var showTrackOptions by rememberSaveable { mutableStateOf(false) }

    // Auto-hide controls state: 8 seconds timer
    var showControlButtons by rememberSaveable { mutableStateOf(true) }
    var interactionTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(showControlButtons, interactionTick) {
        if (showControlButtons) {
            delay(CLUSTER_AUTO_HIDE_MS)
            showControlButtons = false
        }
    }

    // Scroll waking controls
    val scrollWakesControls = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y != 0f) {
                    showControlButtons = true
                    interactionTick++
                }
                return Offset.Zero
            }
        }
    }

    val listState = rememberLazyListState()
    val isDragging by listState.interactionSource.collectIsDraggedAsState()
    val currentPositionState = rememberUpdatedState(currentPosition)
    val readPosition = positionProvider ?: { currentPositionState.value }
    val currentLineIndex by remember(syncedLyrics, positionProvider) {
        derivedStateOf {
            syncedLyrics.indexOfLast { it.timeMs <= readPosition() }
        }
    }

    // Smooth auto-centering
    LaunchedEffect(currentLineIndex, isDragging, isSelectionMode) {
        if (currentLineIndex >= 0 && !isDragging && !isSelectionMode) {
            delay(16)
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val itemInfo = visibleItems.firstOrNull { it.index == currentLineIndex }
            if (itemInfo != null) {
                val center = layoutInfo.viewportEndOffset / 2
                val childCenter = itemInfo.offset + itemInfo.size / 2
                val scrollDelta = (childCenter - center).toFloat()
                if (abs(scrollDelta) > 1f) {
                    listState.animateScrollBy(scrollDelta, tween(500, easing = FastOutSlowInEasing))
                }
            } else {
                listState.animateScrollToItem(currentLineIndex)
            }
        }
    }

    // Effective ambient color: ensure songs with dark covers still get a rich ambient hue
    val effectiveBgColor = remember(dominantColor, accentColor) {
        val lum = 0.2126f * dominantColor.red + 0.7152f * dominantColor.green + 0.0722f * dominantColor.blue
        if (lum < 0.12f) {
            if (accentColor != Color.Unspecified && accentColor != Color.Transparent) {
                accentColor.copy(alpha = 0.75f)
            } else {
                Color(0xFF332244)
            }
        } else {
            dominantColor
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Animated gradient background mesh
        AnimatedLyricsGradientBackground(
            color = effectiveBgColor
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Top Header (Apple Music style) ─────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Song Poster (Small 45dp)
                currentTrack?.let { track ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(track.albumArtUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Song Title & Artist marquee
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTrack?.title.orEmpty(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately
                            )
                            .focusable()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentTrack?.artist.orEmpty(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        modifier = Modifier
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately
                            )
                            .focusable()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Close button (downward chevron)
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Helper banner when entering share selection mode
            AnimatedVisibility(
                visible = isSelectionMode && selectedLines.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = accentColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tap lines to select (up to 5), then tap Share card below",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // ── Center Lyrics Body ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .nestedScroll(scrollWakesControls)
                    .appleMusicVerticalFadeEdges(topFade = 32.dp, bottomFade = 24.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        showControlButtons = !showControlButtons
                        interactionTick++
                    }
            ) {
                if (isLyricsLoading && syncedLyrics.isEmpty() && plainLyrics.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.5.dp
                        )
                    }
                } else if (syncedLyrics.isEmpty()) {
                    val scrollState = rememberScrollState()
                    Text(
                        text = if (!plainLyrics.isNullOrEmpty()) plainLyrics
                        else if (isLyricsLoading) "Searching for lyrics..."
                        else "No lyrics available.",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = AppleMusicLyricFontSize,
                            lineHeight = AppleMusicLyricLineHeight
                        ),
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp, vertical = 40.dp)
                            .verticalScroll(scrollState),
                        textAlign = TextAlign.Start
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 220.dp,
                            bottom = 260.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(AppleMusicLyricGap)
                    ) {
                        itemsIndexed(syncedLyrics, key = { index, _ -> "lyric_$index" }) { index, line ->
                            val isCurrent = index == currentLineIndex
                            val isSelected = index in selectedLines
                            val distanceFromCurrent = if (currentLineIndex >= 0) index - currentLineIndex else 0

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .appleMusicLyricFocus(
                                        distanceFromCurrent = distanceFromCurrent,
                                        isDragging = isDragging,
                                        hasActiveLine = currentLineIndex >= 0,
                                        fontSize = AppleMusicLyricFontSize
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isSelected -> accentColor.copy(alpha = 0.16f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                        else Modifier
                                    )
                                    .clickable {
                                        interactionTick++
                                        showControlButtons = true
                                        if (isSelectionMode) {
                                            selectedLines = if (index in selectedLines) {
                                                selectedLines - index
                                            } else {
                                                (selectedLines + index).take(5).toSet()
                                            }
                                        } else {
                                            viewModel.seekTo(line.timeMs)
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 28.dp),
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
                                            fontSize = AppleMusicLyricFontSize,
                                            lineHeight = AppleMusicLyricLineHeight,
                                            letterSpacing = (-0.2).sp
                                        ),
                                        color = when {
                                            isSelected -> accentColor
                                            isCurrent -> Color.White
                                            else -> AppleMusicInactiveLineColor
                                        },
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Start
                                    )
                                    if (isSelectionMode && isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
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

                        // Caption Footer: line synced info
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 28.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = if (syncedLyrics.isNotEmpty()) "Line synced" else "Unsynced",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.White.copy(alpha = 0.5f),
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Lyrics provided by LRCLIB",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.35f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            // ── Bottom Section: Sleek Seekbar & Auto-Hiding Controls ───────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        showControlButtons = true
                        interactionTick++
                    }
                    .padding(bottom = 12.dp)
            ) {
                // SimpMusic Audio Waveform Seekbar with Rainbow Crossfade Glow
                val (waveActiveColor, lyricsShimmerSweep) = rememberCrossfadeVisuals(isCrossfading)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    val liveProgress by remember(duration, readPosition) {
                        derivedStateOf {
                            if (duration > 0L) (readPosition().toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
                        }
                    }
                    AudioWaveformSeekBar(
                        progress = liveProgress,
                        onSeek = { fraction ->
                            interactionTick++
                            showControlButtons = true
                            viewModel.seekTo((fraction * duration).toLong())
                        },
                        activeColor = waveActiveColor,
                        seed = currentTrack?.id?.hashCode() ?: 42,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Time labels: Left elapsed ("02:03"), Center "Crossfading" shimmer, Right duration ("04:02")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val elapsedSec by remember(readPosition) {
                            derivedStateOf { (readPosition().coerceAtLeast(0L) / 1000L) * 1000L }
                        }
                        Text(
                            text = viewModel.formatDuration(elapsedSec),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = viewModel.formatDuration(duration),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    CrossfadingShimmerLabel(
                        isCrossfading = isCrossfading,
                        shimmerSweep = lyricsShimmerSweep,
                        fontSize = 12.sp
                    )
                }

                // Transport controls row: Share, Prev, 56dp Play/Pause Circle, Next, More (Image 2 exact)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 10.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Far Left: Share button (Image 2 exact)
                    IconButton(
                        onClick = {
                            isSelectionMode = !isSelectionMode
                            if (!isSelectionMode) selectedLines = emptySet()
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share",
                            tint = if (isSelectionMode) accentColor else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Skip Previous (Image 2 exact)
                    IconButton(
                        onClick = { viewModel.skipPrevious() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Center: Circular Play / Pause (56dp solid white circle with black icon - Image 2 exact)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { viewModel.togglePlayback() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Skip Next (Image 2 exact)
                    IconButton(
                        onClick = { viewModel.skipNext() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Far Right: More options 3-dots button (Image 2 exact)
                    IconButton(
                        onClick = { showTrackOptions = true },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // Selection Mode: Floating Share Card Button
        AnimatedVisibility(
            visible = isSelectionMode && selectedLines.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 76.dp),
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
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
                                shareLyricsCard(context, currentTrack, lines, effectiveBgColor, accentColor)
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

        // Track Options Bottom Sheet
        if (showTrackOptions && currentTrack != null) {
            TrackOptionsBottomSheet(
                track = currentTrack,
                onDismiss = { showTrackOptions = false },
                onPlayNext = {
                    viewModel.playNext(currentTrack)
                    showTrackOptions = false
                },
                onAddToQueue = {
                    viewModel.addToQueue(currentTrack)
                    showTrackOptions = false
                },
                onGoToAlbum = {
                    showTrackOptions = false
                },
                onGoToArtist = {
                    showTrackOptions = false
                },
                onDownload = {
                    viewModel.downloadTrack(currentTrack)
                    showTrackOptions = false
                },
                onLikeToggle = {
                    viewModel.toggleTrackLike(currentTrack)
                },
                isLiked = isLiked
            )
        }
    }
}
