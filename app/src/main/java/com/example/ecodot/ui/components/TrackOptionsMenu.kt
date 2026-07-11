package com.example.ecodot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
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
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.theme.EcoDotRed
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.ContentCut

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOptionsBottomSheet(
    track: Track,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onGoToAlbum: () -> Unit,
    onGoToArtist: () -> Unit,
    isDownloaded: Boolean = false,
    downloadProgress: Float? = null,
    onDownload: () -> Unit = {},
    onDeleteDownload: () -> Unit = {},
    isLiked: Boolean = false,
    onLikeToggle: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    onSetCanvasUrl: ((String?) -> Unit)? = null,
    onSetSleepTimer: (Int) -> Unit = {},
    onSetSleepTimerAtEndOfTrack: () -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    sleepTimerRemaining: Long? = null,
    onSetAsRingtone: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header with Track Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
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
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

            var showSleepTimerChooser by remember { mutableStateOf(false) }

            if (showSleepTimerChooser) {
                // Sleep Timer Sub-options
                ListItem(
                    headlineContent = { Text("Go Back", color = Color.White) },
                    leadingContent = { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = Color.White) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { showSleepTimerChooser = false }
                )

                if (sleepTimerRemaining != null) {
                    val timerText = if (sleepTimerRemaining == 0L) {
                        "Active — End of Track"
                    } else {
                        val m = sleepTimerRemaining / 60
                        val s = sleepTimerRemaining % 60
                        String.format("Active — %02d:%02d left", m, s)
                    }
                    ListItem(
                        headlineContent = { Text("Turn Off Sleep Timer", color = EcoDotRed) },
                        supportingContent = { Text(timerText, color = Color.White.copy(alpha = 0.5f)) },
                        leadingContent = { Icon(Icons.Rounded.Timer, contentDescription = null, tint = EcoDotRed) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            onCancelSleepTimer()
                            onDismiss()
                        }
                    )
                }

                listOf(5, 15, 30, 45, 60).forEach { minutes ->
                    ListItem(
                        headlineContent = { Text("$minutes minutes", color = Color.White) },
                        leadingContent = { Icon(Icons.Rounded.Timer, contentDescription = null, tint = Color.White) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            onSetSleepTimer(minutes)
                            onDismiss()
                        }
                    )
                }

                ListItem(
                    headlineContent = { Text("End of Track", color = Color.White) },
                    leadingContent = { Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = Color.White) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onSetSleepTimerAtEndOfTrack()
                        onDismiss()
                    }
                )
            } else {
                // Actions
                ListItem(
                    headlineContent = { Text(if (isLiked) "Remove from Liked Songs" else "Like", color = Color.White) },
                    leadingContent = { 
                        Icon(
                            imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, 
                            contentDescription = null, 
                            tint = if (isLiked) EcoDotRed else Color.White
                        ) 
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onLikeToggle(); onDismiss() }
                )
                ListItem(
                    headlineContent = { Text("Add to Playlist", color = Color.White) },
                    leadingContent = { Icon(Icons.Rounded.PlaylistAdd, contentDescription = null, tint = Color.White) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onAddToPlaylist() }
                )
                ListItem(
                    headlineContent = { Text("Play Next", color = Color.White) },
                    leadingContent = { Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onPlayNext(); onDismiss() }
                )
                ListItem(
                    headlineContent = { Text("Add to Queue", color = Color.White) },
                    leadingContent = { Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = Color.White) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onAddToQueue(); onDismiss() }
                )
                
                // Sleep Timer Trigger
                val timerStatusText = if (sleepTimerRemaining != null) {
                    if (sleepTimerRemaining == 0L) "Active: End of Track"
                    else {
                        val min = sleepTimerRemaining / 60
                        val sec = sleepTimerRemaining % 60
                        String.format("Active — %02d:%02d left", min, sec)
                    }
                } else "Off"
                ListItem(
                    headlineContent = { Text("Sleep Timer", color = Color.White) },
                    supportingContent = { Text(timerStatusText, color = Color.White.copy(alpha = 0.5f)) },
                    leadingContent = { Icon(Icons.Rounded.Timer, contentDescription = null, tint = Color.White) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { showSleepTimerChooser = true }
                )

                if (track.album.isNotBlank() && track.album != "Unknown Album") {
                    ListItem(
                        headlineContent = { Text("Go to Album", color = Color.White) },
                        leadingContent = { Icon(Icons.Rounded.Album, contentDescription = null, tint = Color.White) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onGoToAlbum(); onDismiss() }
                    )
                }
                if (track.artist.isNotBlank()) {
                    ListItem(
                        headlineContent = { Text("Go to Artist", color = Color.White) },
                        leadingContent = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onGoToArtist(); onDismiss() }
                    )
                }
                if (onSetCanvasUrl != null) {
                    val hasCanvas = !track.canvasUrl.isNullOrEmpty() && track.canvasUrl != "DISABLED"
                    ListItem(
                        headlineContent = { Text(if (hasCanvas) "Disable Looping Canvas" else "Enable Looping Canvas", color = Color.White) },
                        leadingContent = { 
                            Icon(
                                imageVector = if (hasCanvas) Icons.Rounded.Cancel else Icons.Rounded.Movie,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { 
                            if (hasCanvas) {
                                onSetCanvasUrl("DISABLED")
                            } else {
                                onSetCanvasUrl("FETCH_CANVAS")
                            }
                            onDismiss()
                        }
                    )
                }
                if (track.isYouTube) {
                    if (isDownloaded) {
                        ListItem(
                            headlineContent = { Text("Set as Ringtone", color = Color.White) },
                            leadingContent = { Icon(Icons.Rounded.ContentCut, contentDescription = null, tint = Color.White) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { onSetAsRingtone(); onDismiss() }
                        )
                        ListItem(
                            headlineContent = { Text("Delete Download", color = EcoDotRed) },
                            leadingContent = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = EcoDotRed) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { onDeleteDownload(); onDismiss() }
                        )
                    } else if (downloadProgress != null) {
                        val percentage = (downloadProgress * 100).toInt()
                        ListItem(
                            headlineContent = { Text("Downloading... $percentage%", color = Color.White) },
                            leadingContent = { CircularProgressIndicator(progress = { downloadProgress }, modifier = Modifier.size(24.dp), color = EcoDotRed, trackColor = Color.White.copy(alpha=0.2f)) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    } else {
                        ListItem(
                            headlineContent = { Text("Download (High Quality)", color = Color.White) },
                            leadingContent = { Icon(Icons.Rounded.Download, contentDescription = null, tint = Color.White) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { onDownload(); onDismiss() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistDialog(
    track: Track,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onCreatePlaylist: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("New Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    singleLine = true,
                    label = { Text("Playlist Name", color = Color.White.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EcoDotRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        onCreatePlaylist(newPlaylistName.trim())
                        showCreateDialog = false
                        newPlaylistName = ""
                    }
                }) { Text("Create", color = EcoDotRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                text = "Add to Playlist",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                // "+ Create Playlist" Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCreateDialog = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(EcoDotRed.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = EcoDotRed)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Create New Playlist", color = EcoDotRed, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                // Playlists List
                val displayPlaylists = playlists.filter { it.name != "Downloads" }
                if (displayPlaylists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No playlists yet", color = Color.White.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayPlaylists, key = { it.id }) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPlaylistSelected(playlist) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.QueueMusic,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = playlist.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = playlist.description.ifEmpty { "User playlist" },
                                        color = Color.White.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumOptionsBottomSheet(
    albumTitle: String,
    albumArtist: String,
    albumArtUri: String?,
    isSaved: Boolean,
    onDismiss: () -> Unit,
    onPlayAlbum: () -> Unit,
    onSaveToggle: () -> Unit,
    onGoToArtist: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header with Album Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = albumArtUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = albumTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = albumArtist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

            // Actions
            ListItem(
                headlineContent = { Text("Play Album", color = Color.White) },
                leadingContent = { Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { onPlayAlbum(); onDismiss() }
            )

            ListItem(
                headlineContent = { Text(if (isSaved) "Remove from Library" else "Save Album", color = Color.White) },
                leadingContent = { 
                    Icon(
                        imageVector = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, 
                        contentDescription = null, 
                        tint = if (isSaved) EcoDotRed else Color.White
                    ) 
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { onSaveToggle(); onDismiss() }
            )

            ListItem(
                headlineContent = { Text("Go to Artist", color = Color.White) },
                leadingContent = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { onGoToArtist(); onDismiss() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistOptionsBottomSheet(
    playlistName: String,
    playlistCoverUri: String?,
    isCustom: Boolean,
    onDismiss: () -> Unit,
    onPlayPlaylist: () -> Unit,
    onAddToQueue: () -> Unit,
    onEditPlaylist: (() -> Unit)? = null,
    onDeletePlaylist: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header with Playlist Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val parsedCustom = com.example.ecodot.util.PlaylistCustomizer.parseCustomCover(playlistCoverUri)
                if (parsedCustom != null) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        Color(android.graphics.Color.parseColor(parsedCustom.startColor)),
                                        Color(android.graphics.Color.parseColor(parsedCustom.endColor))
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.QueueMusic,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = playlistCoverUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = playlistName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Playlist",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

            // Actions
            ListItem(
                headlineContent = { Text("Play Playlist", color = Color.White) },
                leadingContent = { Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { onPlayPlaylist(); onDismiss() }
            )

            ListItem(
                headlineContent = { Text("Add to Queue", color = Color.White) },
                leadingContent = { Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = Color.White) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { onAddToQueue(); onDismiss() }
            )

            if (isCustom) {
                if (onEditPlaylist != null) {
                    ListItem(
                        headlineContent = { Text("Edit Playlist", color = Color.White) },
                        leadingContent = { Icon(Icons.Rounded.PlaylistAdd, contentDescription = null, tint = Color.White) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onEditPlaylist(); onDismiss() }
                    )
                }
                if (onDeletePlaylist != null) {
                    ListItem(
                        headlineContent = { Text("Delete Playlist", color = EcoDotRed) },
                        leadingContent = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = EcoDotRed) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onDeletePlaylist(); onDismiss() }
                    )
                }
            }
        }
    }
}
