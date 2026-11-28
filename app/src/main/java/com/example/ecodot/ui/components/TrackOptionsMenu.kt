package com.example.ecodot.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueuePlayNext
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Tune

private val SpotifyGreenAccent = Color(0xFF1ED760)
private val SheetSurface = Color(0xFF14151B)
private val SheetGroupCard = Color(0xFF1C1D24)
private val SheetBorderSubtle = Color.White.copy(alpha = 0.08f)

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
    onSetAsRingtone: (() -> Unit)? = null,
    onEqualizerClick: (() -> Unit)? = null,
    isCanvasActive: Boolean = false,
    isVideoMode: Boolean = false,
    onToggleVideoMode: (() -> Unit)? = null,
    onSwitchPlayerStyle: (() -> Unit)? = null,
    onPlayNow: (() -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onRemoveFromQueue: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSleepTimerChooser by remember { mutableStateOf(false) }

    val timerStatusText = remember(sleepTimerRemaining) {
        when (sleepTimerRemaining) {
            null -> "Off"
            0L -> "End of track"
            else -> String.format("%02d:%02d", sleepTimerRemaining / 60, sleepTimerRemaining % 60)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetSurface,
        contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.24f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            // Track summary header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(track.albumArtUri)
                        .size(160, 160)
                        .crossfade(160)
                        .build(),
                    contentDescription = track.title,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SheetGroupCard),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val subtitle = buildString {
                        append(track.artist.ifBlank { "Unknown Artist" })
                        if (track.album.isNotBlank() && track.album != "Unknown Album") {
                            append(" · ")
                            append(track.album)
                        }
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.62f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isDownloaded || track.isYouTube) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isDownloaded) {
                                Icon(
                                    imageVector = Icons.Rounded.DownloadDone,
                                    contentDescription = null,
                                    tint = SpotifyGreenAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Downloaded",
                                    fontSize = 11.sp,
                                    color = SpotifyGreenAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "Stream",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = onLikeToggle) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isLiked) "Remove from Liked Songs" else "Like",
                        tint = if (isLiked) EcoDotRed else Color.White.copy(alpha = 0.72f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedContent(
                targetState = showSleepTimerChooser,
                label = "track_sheet_content"
            ) { sleepChooserActive ->
                if (sleepChooserActive) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showSleepTimerChooser = false }
                                .padding(horizontal = 4.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sleep Timer",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SheetGroupCard)
                                .border(0.5.dp, SheetBorderSubtle, RoundedCornerShape(16.dp))
                        ) {
                            if (sleepTimerRemaining != null) {
                                SheetActionRow(
                                    icon = Icons.Rounded.Cancel,
                                    title = "Turn Off Sleep Timer",
                                    subtitle = "Active — $timerStatusText",
                                    iconTint = EcoDotRed,
                                    titleColor = EcoDotRed,
                                    onClick = {
                                        onCancelSleepTimer()
                                        onDismiss()
                                    }
                                )
                                SheetHairlineDivider()
                            }

                            val durations = listOf(5, 15, 30, 45, 60)
                            durations.forEach { minutes ->
                                SheetActionRow(
                                    icon = Icons.Rounded.Timer,
                                    title = "$minutes minutes",
                                    onClick = {
                                        onSetSleepTimer(minutes)
                                        onDismiss()
                                    }
                                )
                                SheetHairlineDivider()
                            }

                            SheetActionRow(
                                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                                title = "End of current track",
                                onClick = {
                                    onSetSleepTimerAtEndOfTrack()
                                    onDismiss()
                                }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Primary quick-action strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (onRemoveFromQueue != null) 8.dp else 10.dp)
                        ) {
                            QuickActionTile(
                                icon = Icons.Rounded.QueuePlayNext,
                                label = "Play Next",
                                onClick = {
                                    onPlayNext()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionTile(
                                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                                label = "Add Queue",
                                onClick = {
                                    onAddToQueue()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            if (onRemoveFromQueue != null) {
                                QuickActionTile(
                                    icon = Icons.Rounded.RemoveCircleOutline,
                                    label = "Remove",
                                    onClick = {
                                        onRemoveFromQueue()
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            QuickActionTile(
                                icon = Icons.Rounded.PlaylistAdd,
                                label = "Playlist",
                                onClick = { onAddToPlaylist() },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Group 1: Library, Playlist & Offline
                        SheetActionCard {
                            if (onPlayNow != null) {
                                SheetActionRow(
                                    icon = Icons.Rounded.PlayArrow,
                                    title = "Play Now",
                                    onClick = {
                                        onPlayNow()
                                        onDismiss()
                                    }
                                )
                                SheetHairlineDivider()
                            }

                            if (onRemoveFromQueue != null) {
                                SheetActionRow(
                                    icon = Icons.Rounded.RemoveCircleOutline,
                                    title = "Remove from Queue",
                                    onClick = {
                                        onRemoveFromQueue()
                                        onDismiss()
                                    }
                                )
                                SheetHairlineDivider()
                            }

                            SheetActionRow(
                                icon = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                iconTint = if (isLiked) EcoDotRed else Color.White,
                                title = if (isLiked) "Remove from Liked Songs" else "Save to Liked Songs",
                                onClick = {
                                    onLikeToggle()
                                    onDismiss()
                                }
                            )

                            SheetHairlineDivider()
                            SheetActionRow(
                                icon = Icons.Rounded.PlaylistAdd,
                                title = "Add to Playlist",
                                onClick = { onAddToPlaylist() }
                            )

                            if (track.isYouTube) {
                                SheetHairlineDivider()
                                when {
                                    isDownloaded -> {
                                        SheetActionRow(
                                            icon = Icons.Rounded.Delete,
                                            iconTint = Color(0xFFFF6B6B),
                                            title = "Remove Download",
                                            titleColor = Color(0xFFFF6B6B),
                                            subtitle = "Free offline storage for this song",
                                            onClick = {
                                                onDeleteDownload()
                                                onDismiss()
                                            }
                                        )
                                    }
                                    downloadProgress != null -> {
                                        val pct = (downloadProgress * 100).toInt()
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 13.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                progress = { downloadProgress },
                                                modifier = Modifier.size(22.dp),
                                                color = SpotifyGreenAccent,
                                                strokeWidth = 2.dp,
                                                trackColor = Color.White.copy(alpha = 0.16f)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "Downloading... $pct%",
                                                    color = Color.White,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "Saving high quality audio",
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        SheetActionRow(
                                            icon = Icons.Rounded.Download,
                                            title = "Download Track",
                                            subtitle = "Listen offline in high quality",
                                            onClick = {
                                                onDownload()
                                                onDismiss()
                                            }
                                        )
                                    }
                                }
                            }

                            if (onRemoveFromPlaylist != null) {
                                SheetHairlineDivider()
                                SheetActionRow(
                                    icon = Icons.Rounded.RemoveCircleOutline,
                                    iconTint = Color(0xFFFF6B6B),
                                    title = "Remove from this playlist",
                                    titleColor = Color(0xFFFF6B6B),
                                    onClick = {
                                        onRemoveFromPlaylist()
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        // Group 2: Playback, Player & Audio Tools
                        val showVideoSwitch = track.isYouTube && onToggleVideoMode != null
                        val showCanvasOption = onSetCanvasUrl != null
                        val showStyleOption = onSwitchPlayerStyle != null
                        val showEqOption = onEqualizerClick != null
                        val showRingtoneOption = onSetAsRingtone != null && (isDownloaded || !track.isYouTube)

                        SheetActionCard {
                            if (showVideoSwitch) {
                                SheetActionRow(
                                    icon = if (isVideoMode) Icons.Rounded.MusicNote else Icons.Rounded.PlayCircle,
                                    title = if (isVideoMode) "Switch to Song (Audio)" else "Switch to Music Video",
                                    onClick = {
                                        onToggleVideoMode?.invoke()
                                        onDismiss()
                                    }
                                )
                                SheetHairlineDivider()
                            }

                            SheetActionRow(
                                icon = Icons.Rounded.Timer,
                                iconTint = if (sleepTimerRemaining != null) SpotifyGreenAccent else Color.White,
                                title = "Sleep Timer",
                                trailingBadge = timerStatusText,
                                trailingBadgeColor = if (sleepTimerRemaining != null) SpotifyGreenAccent else Color.White.copy(alpha = 0.55f),
                                showChevron = true,
                                onClick = { showSleepTimerChooser = true }
                            )

                            if (showCanvasOption) {
                                SheetHairlineDivider()
                                val hasCanvas = isCanvasActive
                                SheetActionRow(
                                    icon = if (hasCanvas) Icons.Rounded.Cancel else Icons.Rounded.Movie,
                                    title = if (hasCanvas) "Disable Looping Canvas" else "Enable Looping Canvas",
                                    onClick = {
                                        if (hasCanvas) onSetCanvasUrl?.invoke("DISABLED")
                                        else onSetCanvasUrl?.invoke("FETCH_CANVAS")
                                        onDismiss()
                                    }
                                )
                            }

                            if (showStyleOption) {
                                SheetHairlineDivider()
                                SheetActionRow(
                                    icon = Icons.Rounded.Style,
                                    title = "Now Playing Style",
                                    subtitle = "Spotify, Vinyl, Apple Music & more",
                                    onClick = {
                                        onSwitchPlayerStyle?.invoke()
                                        onDismiss()
                                    }
                                )
                            }

                            if (showEqOption) {
                                SheetHairlineDivider()
                                SheetActionRow(
                                    icon = SimpIcons.Tune,
                                    title = "Equalizer",
                                    onClick = {
                                        onEqualizerClick?.invoke()
                                        onDismiss()
                                    }
                                )
                            }

                            if (showRingtoneOption) {
                                SheetHairlineDivider()
                                SheetActionRow(
                                    icon = Icons.Rounded.ContentCut,
                                    title = "Set as Ringtone",
                                    onClick = {
                                        onSetAsRingtone?.invoke()
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        // Group 3: Navigate to Album / Artist
                        val showAlbum = track.album.isNotBlank() && track.album != "Unknown Album"
                        val showArtist = track.artist.isNotBlank()
                        if (showAlbum || showArtist) {
                            SheetActionCard {
                                if (showAlbum) {
                                    SheetActionRow(
                                        icon = Icons.Rounded.Album,
                                        title = "Go to Album",
                                        subtitle = track.album,
                                        showChevron = true,
                                        onClick = {
                                            onGoToAlbum()
                                            onDismiss()
                                        }
                                    )
                                }
                                if (showAlbum && showArtist) {
                                    SheetHairlineDivider()
                                }
                                if (showArtist) {
                                    SheetActionRow(
                                        icon = Icons.Rounded.Person,
                                        title = "Go to Artist",
                                        subtitle = track.artist,
                                        showChevron = true,
                                        onClick = {
                                            onGoToArtist()
                                            onDismiss()
                                        }
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
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SheetGroupCard)
            .border(0.5.dp, SheetBorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(21.dp)
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SheetActionCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SheetGroupCard)
            .border(0.5.dp, SheetBorderSubtle, RoundedCornerShape(16.dp)),
        content = content
    )
}

@Composable
private fun SheetHairlineDivider() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.06f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 52.dp)
    )
}

@Composable
private fun SheetActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = Color.White.copy(alpha = 0.9f),
    titleColor: Color = Color.White,
    trailingBadge: String? = null,
    trailingBadgeColor: Color = Color.White.copy(alpha = 0.55f),
    showChevron: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.52f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (!trailingBadge.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = trailingBadge,
                color = trailingBadgeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        if (showChevron) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )
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
