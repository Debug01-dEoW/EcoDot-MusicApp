package com.example.ecodot.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ecodot.data.local.entities.PlaybackHistoryWithTrack
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.theme.EcoDotCard
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.animatedClickable
import com.example.ecodot.ui.components.*
import com.example.ecodot.ui.viewmodel.MusicViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val historyItems by viewModel.playbackHistory.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = EcoDotCard,
            title = {
                Text("Clear Playback History", color = Color.White, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text("Are you sure you want to clear your entire playback history? This action cannot be undone.",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearPlaybackHistory()
                        showClearDialog = false
                    }
                ) { Text("Clear All", color = EcoDotRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Playback History",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (historyItems.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear History", tint = EcoDotRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Playback History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Songs you play will show up here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            val groupedHistory = remember(historyItems) {
                historyItems.groupBy { item ->
                    dateFormat.format(Date(item.history.timestamp))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                groupedHistory.forEach { (dateStr, itemsForDate) ->
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.85f))
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EcoDotRed
                            )
                        }
                    }

                    items(
                        items = itemsForDate,
                        key = { "history_${it.history.id}" }
                    ) { item ->
                        val isPlaying = currentTrack?.id == item.track.id
                        val downloadProgress = downloadProgressMap[item.track.id]
                        val isDownloaded = downloadedTracks.any { it.id == item.track.id }
                        HistoryTrackItem(
                            item = item,
                            isPlaying = isPlaying,
                            downloadProgress = downloadProgress,
                            isDownloaded = isDownloaded,
                            viewModel = viewModel,
                            allPlaylists = allPlaylists,
                            timeFormatted = timeFormat.format(Date(item.history.timestamp)),
                            onClick = {
                                viewModel.playTrack(item.track)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTrackItem(
    item: PlaybackHistoryWithTrack,
    isPlaying: Boolean,
    downloadProgress: Float?,
    isDownloaded: Boolean,
    viewModel: MusicViewModel,
    allPlaylists: List<Playlist>,
    timeFormatted: String,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showAddToPlaylist by remember { mutableStateOf(false) }
    val track = item.track

    // Sub-menu for "Add to playlist"
    if (showAddToPlaylist) {
        AlertDialog(
            onDismissRequest = { showAddToPlaylist = false },
            containerColor = EcoDotCard,
            title = { Text("Add to Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (allPlaylists.isEmpty()) {
                        Text("No playlists yet.\nCreate one from the Library.",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium)
                    } else {
                        allPlaylists.forEach { playlist ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .animatedClickable(onClick = {
                                        viewModel.addTrackToPlaylist(playlist.id, track)
                                        showAddToPlaylist = false
                                    })
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    if (playlist.name == "Liked Songs") Icons.Rounded.Favorite
                                    else Icons.Rounded.QueueMusic,
                                    contentDescription = null,
                                    tint = if (playlist.name == "Liked Songs") EcoDotRed else Color.White.copy(0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(playlist.name, color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium)
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToPlaylist = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animatedClickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(track.albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(EcoDotCard),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isPlaying) EcoDotRed else Color.White
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                    ExplicitBadge(modifier = Modifier.padding(end = 2.dp))
                }
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color.White.copy(alpha = 0.3f))
                )
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }

        val progress = downloadProgress

        if (isPlaying) {
            com.example.ecodot.ui.components.PlayingIndicator(modifier = Modifier.padding(end = 8.dp))
        }

        if (progress != null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(20.dp), color = EcoDotRed, strokeWidth = 2.dp, trackColor = Color.White.copy(0.1f))
                Text("${(progress * 100).toInt()}%", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else if (isDownloaded) {
            Icon(Icons.Rounded.DownloadDone, contentDescription = "Downloaded", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
        }

        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "More",
                tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
        }

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false },
            modifier = Modifier.background(EcoDotCard)) {
            DropdownMenuItem(
                text = { Text("Play Next", color = Color.White) },
                onClick = { viewModel.playNext(track); showMenu = false },
                leadingIcon = { Icon(Icons.Rounded.QueuePlayNext, "Play Next", tint = Color.White) }
            )
            DropdownMenuItem(
                text = { Text("Add to Queue", color = Color.White) },
                onClick = { viewModel.addToQueue(track); showMenu = false },
                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.QueueMusic, "Add to Queue", tint = Color.White) }
            )
            DropdownMenuItem(
                text = { Text("Add to Playlist", color = Color.White) },
                onClick = { showAddToPlaylist = true; showMenu = false },
                leadingIcon = { Icon(Icons.Rounded.PlaylistAdd, "Playlist", tint = Color.White) }
            )
            DropdownMenuItem(
                text = { Text("Like", color = Color.White) },
                onClick = {
                    viewModel.toggleTrackLike(track)
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Rounded.FavoriteBorder, "Like", tint = EcoDotRed) }
            )
        }
    }
}
