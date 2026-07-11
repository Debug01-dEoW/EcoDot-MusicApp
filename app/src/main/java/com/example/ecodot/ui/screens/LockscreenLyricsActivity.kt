package com.example.ecodot.ui.screens

import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.example.ecodot.R
import com.example.ecodot.playback.EcoDotSessionService
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.EcoDotTheme
import com.example.ecodot.ui.viewmodel.LyricLine
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class LockscreenLyricsActivity : ComponentActivity() {
    private val TAG = "LockscreenLyrics"
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    // Player State
    private var titleState = mutableStateOf("EcoDot")
    private var artistState = mutableStateOf("No Track Playing")
    private var artUrlState = mutableStateOf<String?>(null)
    private var isPlayingState = mutableStateOf(false)
    private var positionState = mutableStateOf(0L)
    private var durationState = mutableStateOf(0L)
    private var syncedLyricsState = mutableStateOf<List<LyricLine>>(emptyList())
    private var isLyricsLoadingState = mutableStateOf(false)

    private val lyricsRepository = com.example.ecodot.data.remote.LyricsRepository()
    private var lyricsJob: Job? = null
    private var positionJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Fullscreen layout flags
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Connect to Media3 Controller
        val sessionToken = SessionToken(this, ComponentName(this, EcoDotSessionService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                val mediaController = controllerFuture?.get()
                controller = mediaController
                setupController(mediaController)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to MediaController", e)
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(this))

        setContent {
            val title by titleState
            val artist by artistState
            val artUrl by artUrlState
            val isPlaying by isPlayingState
            val position by positionState
            val duration by durationState
            val syncedLyrics by syncedLyricsState
            val isLyricsLoading by isLyricsLoadingState

            EcoDotTheme(dominantColor = Color.Black, accentColor = EcoDotRed) {
                LockscreenLyricsScreen(
                    title = title,
                    artist = artist,
                    artUrl = artUrl,
                    isPlaying = isPlaying,
                    position = position,
                    duration = duration,
                    syncedLyrics = syncedLyrics,
                    isLyricsLoading = isLyricsLoading,
                    onPlayPauseToggle = {
                        val c = controller ?: return@LockscreenLyricsScreen
                        if (c.isPlaying) c.pause() else c.play()
                    },
                    onSkipNext = { controller?.seekToNext() },
                    onSkipPrevious = { controller?.seekToPrevious() },
                    onClose = { finish() }
                )
            }
        }
    }

    private fun setupController(controller: MediaController?) {
        val c = controller ?: return
        
        // Initial sync
        isPlayingState.value = c.isPlaying
        positionState.value = c.currentPosition
        durationState.value = c.duration.coerceAtLeast(0L)
        updateTrackInfo(c.currentMediaItem)

        c.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isPlayingState.value = isPlaying
                if (isPlaying) startPositionUpdater() else stopPositionUpdater()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateTrackInfo(mediaItem)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    durationState.value = c.duration.coerceAtLeast(0L)
                }
            }
        })

        if (c.isPlaying) startPositionUpdater()
    }

    private fun updateTrackInfo(mediaItem: MediaItem?) {
        if (mediaItem == null) {
            titleState.value = "EcoDot"
            artistState.value = "No Track Playing"
            artUrlState.value = null
            syncedLyricsState.value = emptyList()
            return
        }

        titleState.value = mediaItem.mediaMetadata.title?.toString() ?: "Unknown"
        artistState.value = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown"
        
        val newArtUrl = mediaItem.mediaMetadata.artworkUri?.toString()
        artUrlState.value = newArtUrl

        // Fetch Lyrics for this track
        fetchLyrics(
            mediaId = mediaItem.mediaId,
            artist = artistState.value,
            title = titleState.value,
            duration = durationState.value
        )
    }

    private fun fetchLyrics(mediaId: String, artist: String, title: String, duration: Long) {
        lyricsJob?.cancel()
        lyricsJob = lifecycleScope.launch(Dispatchers.IO) {
            isLyricsLoadingState.value = true
            syncedLyricsState.value = emptyList()
            try {
                val response = lyricsRepository.fetchLyrics(mediaId, artist, title, duration)
                val rawLrc = response?.syncedLyrics
                if (rawLrc != null) {
                    val parsed = parseLrc(rawLrc)
                    withContext(Dispatchers.Main) {
                        syncedLyricsState.value = parsed
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch lyrics", e)
            } finally {
                isLyricsLoadingState.value = false
            }
        }
    }

    private fun parseLrc(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val timeRegex = Regex("\\[(\\d+):(\\d{2})(?:[.:](\\d{2,3}))?\\]")
        lrcContent.lines().forEach { line ->
            val matches = timeRegex.findAll(line).toList()
            if (matches.isNotEmpty()) {
                val lastMatch = matches.last()
                val text = line.substring(lastMatch.range.last + 1).trim()
                matches.forEach { match ->
                    val min = match.groupValues[1].toLongOrNull() ?: 0L
                    val sec = match.groupValues[2].toLongOrNull() ?: 0L
                    val msStr = match.groupValues[3]
                    val ms = when (msStr.length) {
                        2 -> (msStr.toLongOrNull() ?: 0L) * 10
                        3 -> msStr.toLongOrNull() ?: 0L
                        else -> 0L
                    }
                    val time = min * 60 * 1000L + sec * 1000L + ms
                    lines.add(LyricLine(time, text))
                }
            }
        }
        return lines.sortedBy { it.timeMs }
    }

    private fun startPositionUpdater() {
        positionJob?.cancel()
        positionJob = lifecycleScope.launch {
            while (true) {
                controller?.let {
                    positionState.value = it.currentPosition
                }
                delay(250)
            }
        }
    }

    private fun stopPositionUpdater() {
        positionJob?.cancel()
        positionJob = null
    }

    override fun onDestroy() {
        stopPositionUpdater()
        lyricsJob?.cancel()
        controller?.run {
            // Note: Don't pause or stop player on lockscreen destroy, just release controller
        }
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onDestroy()
    }
}

@Composable
fun LockscreenLyricsScreen(
    title: String,
    artist: String,
    artUrl: String?,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    syncedLyrics: List<LyricLine>,
    isLyricsLoading: Boolean,
    onPlayPauseToggle: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. Fullscreen Blurred Cover Art
        if (!artUrl.isNullOrEmpty()) {
            AsyncImage(
                model = artUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(64.dp)
                    .graphicsLayer(alpha = 0.45f)
            )
        }

        // 2. Linear Black Gradient Overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(vertical = 24.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Track Details & Dismiss Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artist,
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Swipe down / Unlock Hint button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Unlock",
                        tint = Color.White
                    )
                }
            }

            // Sync Scrolling Lyrics display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLyricsLoading) {
                    CircularProgressIndicator(color = EcoDotRed)
                } else if (syncedLyrics.isEmpty()) {
                    Text(
                        text = "No synced lyrics available for this track",
                        color = Color.White.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                } else {
                    val activeIndex = syncedLyrics.indexOfLast { position >= it.timeMs }
                    val listState = rememberLazyListState()

                    // Auto scroll to active lyric line
                    LaunchedEffect(activeIndex) {
                        if (activeIndex >= 0) {
                            listState.animateScrollToItem(activeIndex)
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(28.dp),
                        contentPadding = PaddingValues(vertical = 120.dp)
                    ) {
                        itemsIndexed(syncedLyrics) { index, line ->
                            val isActive = index == activeIndex
                            val targetAlpha by animateFloatAsState(if (isActive) 1f else 0.35f, label = "lyric_alpha")
                            val targetScale by animateFloatAsState(if (isActive) 1.08f else 1.0f, label = "lyric_scale")

                            Text(
                                text = line.text,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    lineHeight = 34.sp
                                ),
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        alpha = targetAlpha
                                        scaleX = targetScale
                                        scaleY = targetScale
                                    }
                            )
                        }
                    }
                }
            }

            // Bottom Bar: Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(52.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Play / Pause (Solid Circle style)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onPlayPauseToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(52.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
