package com.example.ecodot.ui.screens
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.ArrowBack
import com.example.ecodot.ui.theme.Check
import com.example.ecodot.ui.theme.Close
import com.example.ecodot.ui.theme.CloudDownload
import com.example.ecodot.ui.theme.DeleteOutline
import com.example.ecodot.ui.theme.MoreVert
import com.example.ecodot.ui.theme.MusicNote
import com.example.ecodot.ui.theme.PlayArrow
import com.example.ecodot.ui.theme.QueueMusic
import com.example.ecodot.ui.theme.Search
import com.example.ecodot.ui.theme.Shuffle
import com.example.ecodot.ui.theme.Tune

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.components.PlayingIndicator
import com.example.ecodot.ui.components.SleeveFan
import com.example.ecodot.ui.components.SwipeToQueueBox
import com.example.ecodot.ui.theme.EcoDotBlack
import com.example.ecodot.ui.viewmodel.MusicViewModel

/**
 * Dedicated Cached Songs Screen.
 * Displays all locally cached songs with playback controls, search filter,
 * storage usage analytics, and cache management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CachedSongsScreen(
    tracks: List<Track>,
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    onOptionsClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val cacheSize by viewModel.cacheSize.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var selectedTrackForMenu by remember { mutableStateOf<Track?>(null) }

    val prefsManager = remember { com.example.ecodot.data.local.prefs.UserPreferencesManager(context) }
    var currentLimitMb by remember { mutableLongStateOf(prefsManager.cacheLimitMb) }
    var showLimitDialog by remember { mutableStateOf(false) }

    val filteredTracks = remember(tracks, searchQuery) {
        if (searchQuery.isBlank()) {
            tracks
        } else {
            val q = searchQuery.trim().lowercase()
            tracks.filter {
                it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
            }
        }
    }

    val cacheSizeMb = remember(tracks.size, cacheSize) {
        if (tracks.isEmpty()) 0
        else (cacheSize / (1024 * 1024)).coerceAtLeast((tracks.size * 3.2).toLong()).toInt()
    }

    BackHandler(onBack = onBack)

    if (showLimitDialog) {
        val limitOptions = listOf(
            0L to "Dynamic (Unlimited / Free Space)",
            1024L to "1 GB (~250 songs)",
            2048L to "2 GB (~500 songs)",
            5120L to "5 GB (~1,200 songs)",
            10240L to "10 GB (~2,500 songs)",
            20480L to "20 GB (~5,000 songs)"
        )
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = {
                Text(
                    text = "Cache Storage Limit",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose how much space EcoDot may use for offline song caching. When the limit is reached, least recently played songs are automatically cleared.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    limitOptions.forEach { (mb, label) ->
                        val isSelected = currentLimitMb == mb
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFE7D7FD).copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    currentLimitMb = mb
                                    prefsManager.cacheLimitMb = mb
                                    showLimitDialog = false
                                    Toast.makeText(context, "Cache limit set to: $label", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color(0xFFE7D7FD) else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = SimpIcons.Check,
                                    contentDescription = null,
                                    tint = Color(0xFFE7D7FD),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text("Close", color = Color(0xFFE7D7FD))
                }
            },
            containerColor = Color(0xFF1E202A),
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = {
                Text(
                    text = "Clear Cache?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will remove all ${tracks.size} cached tracks (~$cacheSizeMb MB) from local storage.",
                    color = Color.White.copy(alpha = 0.75f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCache()
                        showClearConfirm = false
                        Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear All", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = Color(0xFF1E202A),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Individual Track Context Menu Sheet
    selectedTrackForMenu?.let { track ->
        ModalBottomSheet(
            onDismissRequest = { selectedTrackForMenu = null },
            containerColor = Color(0xFF1A1B24),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                // Track header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF222430))
                    ) {
                        if (!track.albumArtUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(track.albumArtUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = Color.White.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Play Next Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.playNext(track)
                            selectedTrackForMenu = null
                            Toast.makeText(context, "Playing next: ${track.title}", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = SimpIcons.QueueMusic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Play Next",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Remove from Cache Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val trackId = track.id
                            selectedTrackForMenu = null
                            viewModel.removeTrackFromCache(trackId)
                            Toast.makeText(context, "Removed from cache", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = SimpIcons.DeleteOutline,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Remove from Cache",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EcoDotBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Top Navigation Bar ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = SimpIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Cached Songs",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showLimitDialog = true }) {
                        Icon(
                            imageVector = SimpIcons.Tune,
                            contentDescription = "Cache Limit Settings",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (tracks.isNotEmpty()) {
                                showClearConfirm = true
                            } else {
                                Toast.makeText(context, "Cache is already empty", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = SimpIcons.DeleteOutline,
                            contentDescription = "Clear Cache",
                            tint = if (tracks.isNotEmpty()) Color.White.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // ── Hero Overview Header ────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Fanned Artwork Sleeves
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val sampleThumbs = remember(tracks) {
                                tracks.mapNotNull { it.albumArtUri }.take(3)
                            }
                            SleeveFan(
                                thumbnails = sampleThumbs,
                                slots = 3,
                                sleeve = 80.dp,
                                spread = 28.dp,
                                tilt = 12f,
                                round = false,
                                ring = Color(0xFF1B1C23),
                                modifier = Modifier.size(160.dp, 100.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${tracks.size} Songs Cached",
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "~$cacheSizeMb MB used • Auto-cached for instant offline streaming",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                            color = Color.White.copy(alpha = 0.55f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(percent = 50))
                                .clickable { showLimitDialog = true }
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = SimpIcons.Tune,
                                contentDescription = null,
                                tint = Color(0xFFE7D7FD),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLimitMb == 0L) "Limit: Dynamic (Unlimited)" else "Limit: ${currentLimitMb / 1024} GB",
                                color = Color(0xFFE7D7FD),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Action Buttons: Play All & Shuffle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (filteredTracks.isNotEmpty()) {
                                        viewModel.playTracks(filteredTracks, startIndex = 0)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE7D7FD)),
                                shape = RoundedCornerShape(percent = 50),
                                enabled = filteredTracks.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = SimpIcons.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(0xFF1E1035),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Play All",
                                    color = Color(0xFF1E1035),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    if (filteredTracks.isNotEmpty()) {
                                        viewModel.playTracks(filteredTracks.shuffled(), startIndex = 0)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                                shape = RoundedCornerShape(percent = 50),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF1A1B24)),
                                enabled = filteredTracks.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = SimpIcons.Shuffle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Shuffle",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // In-line Search Field
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(Color(0xFF191A23))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(percent = 50))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = SimpIcons.Search,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.45f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = SolidColor(Color(0xFFE7D7FD)),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search cached tracks...",
                                                color = Color.White.copy(alpha = 0.35f),
                                                fontSize = 14.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = SimpIcons.Close,
                                            contentDescription = "Clear search",
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Track Count or Empty State ──────────────────────────────
                if (filteredTracks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp, bottom = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.06f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = SimpIcons.CloudDownload,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No matching tracks found" else "No cached songs yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "Try searching for a different title or artist"
                                else "Songs you stream are automatically cached here for offline listening.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "${filteredTracks.size} TRACKS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 6.dp)
                        )
                    }

                    itemsIndexed(filteredTracks, key = { _, track -> "cached_${track.id}" }) { index, track ->
                        val isThisPlaying = isPlaying && currentTrack?.id == track.id
                        CachedTrackRow(
                            index = index + 1,
                            track = track,
                            isPlaying = isThisPlaying,
                            onClick = {
                                viewModel.playTracks(filteredTracks, startIndex = index)
                            },
                            onOptionsClick = { selectedTrackForMenu = track },
                            onAddToQueue = { viewModel.addToQueue(track) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single cached song item row with track number, cover artwork, title, artist, and options menu.
 */
@Composable
private fun CachedTrackRow(
    index: Int,
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onAddToQueue: () -> Unit = {}
) {
    SwipeToQueueBox(onAddToQueue = onAddToQueue) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Track Index Number
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = if (isPlaying) Color(0xFFE7D7FD) else Color.White.copy(alpha = 0.40f),
            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.width(26.dp)
        )

        // 48dp Square Cover Artwork
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1F2028))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
        ) {
            if (!track.albumArtUri.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(track.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        SimpIcons.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    PlayingIndicator(color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Subtitle
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp),
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) Color(0xFFE7D7FD) else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 3-Dots More Options
        IconButton(
            onClick = onOptionsClick,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = SimpIcons.MoreVert,
                contentDescription = "Options",
                tint = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
}
