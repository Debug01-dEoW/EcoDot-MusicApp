package com.example.ecodot.ui.components.player
import com.example.ecodot.ui.theme.Favorite
import com.example.ecodot.ui.theme.FavoriteBorder
import com.example.ecodot.ui.theme.KeyboardArrowDown
import com.example.ecodot.ui.theme.MoreVert
import com.example.ecodot.ui.theme.OpenInFull
import com.example.ecodot.ui.theme.Style

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.remote.YouTubeParser
import com.example.ecodot.ui.components.ExplicitBadge
import com.example.ecodot.ui.components.PlayingIndicator
import com.example.ecodot.ui.theme.AccessAlarm
import com.example.ecodot.ui.theme.Headphones
import com.example.ecodot.ui.theme.Info
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

/**
 * Rich Analog Vinyl Turntable Now Playing Style:
 * - Warm studio plinth gradient infused with sleeve colors + brushed brass accents
 * - 33 1/3 RPM turntable record with real playback-tracking aluminum tonearm
 * - Seamless Crossfade to responsive framed Video Monitor when watching MVs
 * - Inline synced lyrics preview, continuous seekbar, studio deck switches & scrollable Lyrics card.
 */
@Composable
fun VinylStyleContent(
    track: Track,
    controller: MediaController?,
    isPlaying: Boolean,
    isLiked: Boolean,
    shuffleMode: Boolean,
    repeatMode: Int,
    sleepTimerRemaining: Long?,
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
    onOpenTrackInfo: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenStylePicker: () -> Unit,
    onOpenOptionsMenu: (Track) -> Unit,
    onOpenArtist: () -> Unit,
    onOpenOutputSheet: () -> Unit,
    formatDuration: (Long) -> String,
    modifier: Modifier = Modifier,
    positionProvider: (() -> Long)? = null
) {
    val view = LocalView.current
    val brassGold = Color(0xFFE5C07B)
    val studioAccent = if (accentColor == Color.Black) brassGold else accentColor
    val baseTint = if (dominantColor == Color.Black) Color(0xFF281F1A) else dominantColor
    val readPosition = positionProvider ?: { position }

    var showVideoOverlay by rememberSaveable { mutableStateOf(false) }
    var showCaptions by rememberSaveable { mutableStateOf(true) }
    val outputDeviceName by rememberActiveAudioOutputName()
    val activeLine by remember(syncedLyrics, positionProvider) {
        derivedStateOf {
            val pos = readPosition()
            syncedLyrics.lastOrNull { it.timeMs <= pos }?.text
        }
    }

    LaunchedEffect(showVideoOverlay, isPlaying, isTrackAVideo) {
        if (showVideoOverlay && isPlaying && isTrackAVideo) {
            delay(3000)
            showVideoOverlay = false
        }
    }

    val safeDuration = duration.coerceAtLeast(1L)
    val playbackProgressProvider = remember(safeDuration, readPosition) {
        { (readPosition().toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f) }
    }
    val labelUrl = remember(track.id, track.albumArtUri) {
        YouTubeParser.getHighResUrl(track.albumArtUri, isWide = false) ?: track.albumArtUri
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        baseTint.copy(alpha = 0.55f),
                        Color(0xFF141216),
                        Color(0xFF070709)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── 1. Top Turntable Plinth Header ────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = SimpIcons.KeyboardArrowDown,
                            contentDescription = "Minimize",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTrackAVideo) "STUDIO MONITOR" else "VINYL TURNTABLE · 33⅓ RPM",
                            fontFamily = poppinsFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = brassGold
                        )
                        Text(
                            text = track.album.takeIf { it.isNotBlank() && it != "Unknown" } ?: track.artist,
                            fontFamily = poppinsFamily,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenStylePicker) {
                            Icon(
                                imageVector = SimpIcons.Style,
                                contentDescription = "Switch Style",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        IconButton(onClick = { onOpenOptionsMenu(track) }) {
                            Icon(
                                imageVector = SimpIcons.MoreVert,
                                contentDescription = "More Options",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // ── 2. Telemetry Badge Row ──────────────────────────────────────
            if (!streamBadgeLabel.isNullOrBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = brassGold.copy(alpha = 0.14f),
                            border = BorderStroke(0.75.dp, brassGold.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = streamBadgeLabel,
                                fontFamily = poppinsFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = brassGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // ── 3. Vinyl Turntable Platter OR Framed Responsive Video Monitor ─
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Crossfade(
                    targetState = isTrackAVideo,
                    animationSpec = tween(320),
                    label = "vinylOrVideoStage"
                ) { videoActive ->
                    if (videoActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .shadow(18.dp, RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.5.dp, brassGold.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
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
                                positionProvider = readPosition,
                                onToggleControls = { showVideoOverlay = !showVideoOverlay },
                                onToggleCaptions = { showCaptions = !showCaptions },
                                onToggleResizeMode = onToggleVideoResizeMode,
                                onEnterFullscreen = onEnterVideoFullscreen,
                                onSeekBackward5s = { onSeekTo((readPosition() - 5000L).coerceAtLeast(0L)) },
                                onSeekForward5s = { onSeekTo((readPosition() + 5000L).coerceAtMost(safeDuration)) },
                                fallbackArtUrl = track.albumArtUri,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = Color.White.copy(alpha = 0.04f),
                                border = BorderStroke(1.dp, brassGold.copy(alpha = 0.22f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(14.dp)) {
                                    VinylArtworkView(
                                        imageUrl = labelUrl,
                                        isPlaying = isPlaying,
                                        dominantColor = studioAccent,
                                        playbackProgressProvider = playbackProgressProvider
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 4. Track Metadata & Actions ───────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPlaying) {
                        PlayingIndicator(
                            color = brassGold,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = track.title,
                                fontFamily = poppinsFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .basicMarquee()
                            )
                            if (track.isExplicit) {
                                Spacer(modifier = Modifier.width(6.dp))
                                ExplicitBadge()
                            }
                        }
                        Text(
                            text = track.artist,
                            fontFamily = poppinsFamily,
                            fontSize = 14.5.sp,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable(onClick = onOpenArtist)
                        )
                    }

                    IconButton(onClick = onOpenPlaylistPicker) {
                        Icon(
                            imageVector = SimpIcons.PlaylistAdd,
                            contentDescription = "Add to playlist",
                            tint = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    IconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onToggleLike()
                    }) {
                        Icon(
                            imageVector = if (isLiked) SimpIcons.Favorite else SimpIcons.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isLiked) Color(0xFFFF4D6D) else Color.White
                        )
                    }
                }
            }

            // ── 5. Inline Synced Lyric Strip ──────────────────────────────────
            val currentLyric = activeLine
            if (!currentLyric.isNullOrBlank()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.06f),
                        border = BorderStroke(0.75.dp, brassGold.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clickable(onClick = onOpenFullLyrics)
                    ) {
                        Text(
                            text = currentLyric,
                            fontFamily = poppinsFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = brassGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                                .basicMarquee()
                        )
                    }
                }
            }

            // ── 6. Continuous Seekbar & Transport ─────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContinuousSeekBar(
                    position = position,
                    duration = duration,
                    accentColor = brassGold,
                    onSeek = onSeekTo,
                    formatDuration = formatDuration,
                    isCrossfading = isCrossfading,
                    positionProvider = readPosition,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
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
                        activeColor = brassGold,
                        size = 22,
                        onClick = onToggleShuffle
                    )
                    AnimatedControlIcon(
                        icon = SimpIcons.SkipPrevious,
                        active = false,
                        activeColor = Color.White,
                        size = 34,
                        onClick = onPrevious
                    )
                    PremiumPlayButton(
                        isPlaying = isPlaying,
                        accentColor = brassGold,
                        onClick = onPlayPause
                    )
                    AnimatedControlIcon(
                        icon = SimpIcons.SkipNext,
                        active = false,
                        activeColor = Color.White,
                        size = 34,
                        onClick = onNext
                    )
                    AnimatedControlIcon(
                        icon = if (repeatMode == Player.REPEAT_MODE_ONE) SimpIcons.RepeatOne else SimpIcons.Repeat,
                        active = repeatMode != Player.REPEAT_MODE_OFF,
                        activeColor = brassGold,
                        size = 22,
                        onClick = onToggleRepeat
                    )
                }
            }

            // ── 7. Turntable Console Quick Row ────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.055f),
                    border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onOpenOutputSheet) {
                            Icon(SimpIcons.Headphones, contentDescription = outputDeviceName, tint = Color.White.copy(alpha = 0.85f))
                        }
                        IconButton(onClick = onOpenEqualizer) {
                            Icon(SimpIcons.Tune, contentDescription = "Equalizer", tint = brassGold)
                        }
                        IconButton(onClick = onOpenTrackInfo) {
                            Icon(SimpIcons.Info, contentDescription = "Track info", tint = Color.White.copy(alpha = 0.85f))
                        }
                        IconButton(onClick = onOpenSleepTimer) {
                            Icon(
                                SimpIcons.AccessAlarm,
                                contentDescription = "Sleep Timer",
                                tint = if (sleepTimerRemaining != null) brassGold else Color.White.copy(alpha = 0.85f)
                            )
                        }
                        IconButton(onClick = onOpenQueueSheet) {
                            Icon(SimpIcons.QueueMusic, contentDescription = "Queue", tint = Color.White.copy(alpha = 0.85f))
                        }
                    }
                }
            }

            // ── 8. Synced Lyrics Card Below Plinth ────────────────────────────
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF17161B),
                    border = BorderStroke(1.dp, brassGold.copy(alpha = 0.22f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sleeve Notes & Lyrics",
                                fontFamily = poppinsFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = CircleShape,
                                color = brassGold.copy(alpha = 0.16f),
                                modifier = Modifier.clickable(onClick = onOpenFullLyrics)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = SimpIcons.OpenInFull,
                                        contentDescription = "Expand",
                                        tint = brassGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Expand",
                                        fontFamily = poppinsFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = brassGold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) {
                            LyricsPanel(
                                syncedLyrics = syncedLyrics,
                                currentPosition = position,
                                plainLyrics = plainLyrics,
                                isLyricsLoading = isLyricsLoading,
                                onSeek = onSeekTo,
                                isFullScreen = false,
                                backgroundColor = Color.Transparent,
                                accentColor = brassGold,
                                positionProvider = readPosition
                            )
                        }
                    }
                }
            }
        }
    }
}
