package com.example.ecodot.ui.components.player

import android.view.HapticFeedbackConstants
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ecodot.ui.theme.Pause
import com.example.ecodot.ui.theme.PlayArrow
import com.example.ecodot.ui.theme.PlaylistAdd
import com.example.ecodot.ui.theme.QueueMusic
import com.example.ecodot.ui.theme.Repeat
import com.example.ecodot.ui.theme.RepeatOne
import com.example.ecodot.ui.theme.Shuffle
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.SkipNext
import com.example.ecodot.ui.theme.SkipPrevious
import com.example.ecodot.ui.theme.Tune
import com.example.ecodot.ui.theme.poppinsFamily
import com.example.ecodot.ui.viewmodel.LyricLine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

private val SpotifyGreen = Color(0xFF1ED760)
private val SpotifyBackdrop = Color(0xFF0A0B0D)

/**
 * SimpMusic-inspired Spotify Now Playing Style:
 * - Signature palette-tinted gradient dissolving into #0A0B0D obsidian canvas
 * - "PLAYING FROM" context bar + instant Song/Video & Style switchers
 * - Swipeable sharp-corner album card (HorizontalPager) or responsive 16:9 / adaptive Video card
 * - Crisp emerald CheckCircle favorite toggle, white Play disc, continuous scrubber,
 *   output device indicator bar, plus vibrant Lyrics Card, Track Credits Card & Artist Card below.
 */
@Composable
fun SpotifyStyleContent(
    track: Track,
    queue: List<Track>,
    currentQueueIndex: Int,
    controller: MediaController?,
    isPlaying: Boolean,
    isLiked: Boolean,
    shuffleMode: Boolean,
    repeatMode: Int,
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
    artistHeaderImageUrl: String?,
    artistFollowersText: String,
    onBackClick: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSelectQueueIndex: (Int) -> Unit,
    onToggleLike: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSetVideoMode: (Boolean) -> Unit,
    onToggleVideoResizeMode: () -> Unit,
    onEnterVideoFullscreen: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenPlaylistPicker: () -> Unit,
    onOpenQueueSheet: () -> Unit,
    onOpenFullLyrics: () -> Unit,
    onOpenShareLyrics: () -> Unit,
    onOpenTrackInfo: () -> Unit,
    onOpenStylePicker: () -> Unit,
    onOpenOptionsMenu: (Track) -> Unit,
    onOpenArtist: () -> Unit,
    onOpenOutputSheet: () -> Unit,
    formatDuration: (Long) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val topPalette = if (dominantColor == Color.Black) Color(0xFF1F3B2C) else dominantColor
    val cardTint = if (dominantColor == Color.Black) Color(0xFF1E3264) else dominantColor
    val outputDeviceName by rememberActiveAudioOutputName()

    var showVideoOverlay by rememberSaveable { mutableStateOf(false) }
    var showCaptions by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(showVideoOverlay, isPlaying, isTrackAVideo) {
        if (showVideoOverlay && isPlaying && isTrackAVideo) {
            delay(3000)
            showVideoOverlay = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpotifyBackdrop)
    ) {
        // Signature Spotify gradient fading cleanly into dark canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(540.dp)
                .background(
                    Brush.verticalGradient(
                        0f to topPalette.copy(alpha = 0.72f),
                        0.55f to topPalette.copy(alpha = 0.25f),
                        1f to SpotifyBackdrop
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // ── 1. Top "PLAYING FROM" Context Bar ────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Minimize",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isTrackAVideo) "PLAYING VIDEO" else "PLAYING FROM ALBUM",
                            fontFamily = poppinsFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                        Text(
                            text = track.album.takeIf { it.isNotBlank() && it != "Unknown" } ?: track.artist,
                            fontFamily = poppinsFamily,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenStylePicker) {
                            Icon(
                                imageVector = Icons.Rounded.Style,
                                contentDescription = "Switch player style",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = { onOpenOptionsMenu(track) }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Options",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // ── 2. Optional Song / Video Mode Switcher ────────────────────────
            if (track.isYouTube) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AudioVideoModeTogglePill(
                            isVideoMode = isTrackAVideo,
                            accentColor = SpotifyGreen,
                            onSetVideoMode = onSetVideoMode
                        )
                    }
                }
            }

            // ── 3. Swipeable Cover Art OR Responsive Video Stage ─────────────
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Crossfade(
                    targetState = isTrackAVideo,
                    animationSpec = tween(280),
                    label = "spotifyStage"
                ) { videoActive ->
                    if (videoActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .shadow(18.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .aspectRatio(videoAspectRatio.coerceIn(0.6f, 2.0f))
                        ) {
                            ResponsivePlayerVideoSurface(
                                controller = controller,
                                trackId = track.id,
                                videoResizeMode = videoResizeMode,
                                showControls = showVideoOverlay,
                                showCaptions = showCaptions,
                                syncedLyrics = syncedLyrics,
                                positionMs = position,
                                onToggleControls = { showVideoOverlay = !showVideoOverlay },
                                onToggleCaptions = { showCaptions = !showCaptions },
                                onToggleResizeMode = onToggleVideoResizeMode,
                                onEnterFullscreen = onEnterVideoFullscreen,
                                onSeekBackward5s = { onSeekTo((position - 5000L).coerceAtLeast(0L)) },
                                onSeekForward5s = { onSeekTo((position + 5000L).coerceAtMost(duration.coerceAtLeast(1L))) },
                                fallbackArtUrl = track.albumArtUri,
                                modifier = Modifier.fillMaxSize()
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
                            beyondViewportPageCount = 1,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            val pageTrack = safeQueue.getOrNull(page) ?: track
                            val artUrl = remember(pageTrack.id, pageTrack.albumArtUri) {
                                YouTubeParser.getHighResUrl(pageTrack.albumArtUri, isWide = false) ?: pageTrack.albumArtUri
                            }
                            var activeUrl by remember(artUrl) { mutableStateOf(artUrl) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(activeUrl)
                                        .crossfade(350)
                                        .build(),
                                    contentDescription = pageTrack.title,
                                    contentScale = ContentScale.Crop,
                                    onError = {
                                        val fallback = YouTubeParser.getFallbackThumbUrl(activeUrl)
                                        if (fallback != null && fallback != activeUrl) activeUrl = fallback
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .shadow(20.dp, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }
            }

            // ── 4. Track Metadata & Emerald Like Toggle ───────────────────────
            item {
                Spacer(modifier = Modifier.height(22.dp))
                val likeBurst = rememberHeartBurstState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontFamily = poppinsFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (track.isExplicit) {
                                ExplicitBadge(modifier = Modifier.padding(end = 6.dp))
                            }
                            Text(
                                text = track.artist,
                                fontFamily = poppinsFamily,
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.72f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable(onClick = onOpenArtist)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .heartBurst(likeBurst)
                            .clip(CircleShape)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                if (!isLiked) likeBurst.fire()
                                onToggleLike()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Rounded.CheckCircle else Icons.Rounded.AddCircleOutline,
                            contentDescription = "Save to Liked Songs",
                            tint = if (isLiked) SpotifyGreen else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // ── 5. Seekbar + Spotify White Circle Transport ───────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContinuousSeekBar(
                    position = position,
                    duration = duration,
                    accentColor = Color.White,
                    onSeek = onSeekTo,
                    formatDuration = formatDuration,
                    isCrossfading = isCrossfading,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedControlIcon(
                        icon = SimpIcons.Shuffle,
                        active = shuffleMode,
                        activeColor = SpotifyGreen,
                        size = 24,
                        onClick = onToggleShuffle
                    )
                    AnimatedControlIcon(
                        icon = SimpIcons.SkipPrevious,
                        active = false,
                        activeColor = Color.White,
                        size = 38,
                        onClick = onPrevious
                    )

                    // Classic Spotify solid white 66.dp disc with black icon
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onPlayPause()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    AnimatedControlIcon(
                        icon = SimpIcons.SkipNext,
                        active = false,
                        activeColor = Color.White,
                        size = 38,
                        onClick = onNext
                    )
                    AnimatedControlIcon(
                        icon = if (repeatMode == Player.REPEAT_MODE_ONE) SimpIcons.RepeatOne else SimpIcons.Repeat,
                        active = repeatMode != Player.REPEAT_MODE_OFF,
                        activeColor = SpotifyGreen,
                        size = 24,
                        onClick = onToggleRepeat
                    )
                }
            }

            // ── 6. Bottom Utility Bar: Active Device Pill + EQ / Playlist / Queue ─
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onOpenOutputSheet)
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = SimpIcons.Headphones,
                            contentDescription = "Audio output",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = outputDeviceName,
                            fontFamily = poppinsFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SpotifyGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(onClick = onOpenEqualizer, modifier = Modifier.size(36.dp)) {
                            Icon(SimpIcons.Tune, contentDescription = "Equalizer", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onOpenPlaylistPicker, modifier = Modifier.size(36.dp)) {
                            Icon(SimpIcons.PlaylistAdd, contentDescription = "Add to playlist", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onOpenQueueSheet, modifier = Modifier.size(36.dp)) {
                            Icon(SimpIcons.QueueMusic, contentDescription = "Queue", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // ── 7. Spotify Vibrant Lyrics Card ──────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = cardTint.copy(alpha = 0.85f),
                    border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.14f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lyrics",
                                fontFamily = poppinsFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.30f),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable(onClick = onOpenShareLyrics)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Share, contentDescription = "Share lyrics", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.Black.copy(alpha = 0.30f),
                                    modifier = Modifier.clickable(onClick = onOpenFullLyrics)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Rounded.OpenInFull, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "EXPAND",
                                            fontFamily = poppinsFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(290.dp)
                        ) {
                            LyricsPanel(
                                syncedLyrics = syncedLyrics,
                                currentPosition = position,
                                plainLyrics = plainLyrics,
                                isLyricsLoading = isLyricsLoading,
                                onSeek = onSeekTo,
                                isFullScreen = false,
                                backgroundColor = Color.Transparent,
                                accentColor = Color.White
                            )
                        }
                    }
                }
            }

            // ── 8. Credits & Audio Stream Info Card ───────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF191A1F),
                    border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable(onClick = onOpenTrackInfo)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Credits & Audio Format",
                                fontFamily = poppinsFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${track.artist} • Main Artist",
                                fontFamily = poppinsFamily,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!streamBadgeLabel.isNullOrBlank()) {
                                Text(
                                    text = "Stream: $streamBadgeLabel",
                                    fontFamily = poppinsFamily,
                                    fontSize = 12.sp,
                                    color = SpotifyGreen
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            color = Color.Transparent
                        ) {
                            Text(
                                text = "Inspect",
                                fontFamily = poppinsFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // ── 9. About the Artist Card ──────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(0.75.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .clickable(onClick = onOpenArtist)
                ) {
                    if (!artistHeaderImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(artistHeaderImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = track.artist,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF1B1C22)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.size(84.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )

                    Text(
                        text = "About the artist",
                        fontFamily = poppinsFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = track.artist,
                            fontFamily = poppinsFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (artistFollowersText.isNotBlank()) {
                            Text(
                                text = artistFollowersText,
                                fontFamily = poppinsFamily,
                                fontSize = 12.5.sp,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        }
                    }
                }
            }
        }
    }
}
