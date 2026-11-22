package com.example.ecodot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.theme.EcoDotRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryPlaylistOptionsBottomSheet(
    playlist: Playlist,
    tracks: List<Track>,
    isPinned: Boolean,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isSystemPlaylist = playlist.name == "Liked Songs" || playlist.name == "Downloads"
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1E1E24),
            title = {
                Text(
                    text = "Delete playlist?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${playlist.name}\"? This action cannot be undone.",
                    color = Color.White.copy(alpha = 0.75f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                        onDismiss()
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF141418),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.25f))
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Artwork + Title + Song count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PlaylistMosaicThumbnail(
                    playlist = playlist,
                    tracks = tracks,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(10.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Playlist • ${tracks.size} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Play option
            LibraryPlaylistOptionRow(
                icon = Icons.Rounded.PlayArrow,
                title = "Play",
                onClick = {
                    onPlay()
                    onDismiss()
                }
            )

            // Play next
            LibraryPlaylistOptionRow(
                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                title = "Play Next",
                onClick = {
                    onPlayNext()
                    onDismiss()
                }
            )

            // Add to queue
            LibraryPlaylistOptionRow(
                icon = Icons.Rounded.AddToQueue,
                title = "Add to Queue",
                onClick = {
                    onAddToQueue()
                    onDismiss()
                }
            )

            // Pin / Unpin
            LibraryPlaylistOptionRow(
                icon = Icons.Rounded.PushPin,
                title = if (isPinned) "Unpin from top" else "Pin to top",
                tint = if (isPinned) EcoDotRed else Color.White,
                onClick = {
                    onTogglePin()
                    onDismiss()
                }
            )

            // Edit & Delete (only for custom playlists)
            if (!isSystemPlaylist) {
                LibraryPlaylistOptionRow(
                    icon = Icons.Rounded.Edit,
                    title = "Edit Playlist",
                    onClick = {
                        onDismiss()
                        onEdit()
                    }
                )

                LibraryPlaylistOptionRow(
                    icon = Icons.Rounded.Delete,
                    title = "Delete Playlist",
                    tint = Color(0xFFFF5252),
                    onClick = {
                        showDeleteConfirm = true
                    }
                )
            }
        }
    }
}

@Composable
private fun LibraryPlaylistOptionRow(
    icon: ImageVector,
    title: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}
