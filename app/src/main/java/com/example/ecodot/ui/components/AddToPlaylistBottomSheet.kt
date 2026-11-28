package com.example.ecodot.ui.components
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Add
import com.example.ecodot.ui.theme.Check
import com.example.ecodot.ui.theme.Close

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistBottomSheet(
    track: Track,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }

    if (showCreateSheet) {
        CreatePlaylistBottomSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { name, description, customCoverJson ->
                viewModel.createPlaylistCustom(name, description, customCoverJson)
                // Add track to newly created playlist after it's in DB
                scope.launch {
                    kotlinx.coroutines.delay(200)
                    val playlist = viewModel.getPlaylistByName(name)
                    if (playlist != null) {
                        viewModel.addTrackToPlaylist(playlist.id, track)
                        Toast.makeText(context, "Added to \"$name\"", Toast.LENGTH_SHORT).show()
                    }
                }
                onDismiss()
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add to playlist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${track.title} • ${track.artist}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        SimpIcons.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // "+ New Playlist" action row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickable { showCreateSheet = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(EcoDotRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        SimpIcons.Add,
                        contentDescription = "New Playlist",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "New playlist",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            // Playlists list
            val userPlaylists = allPlaylists.filter { it.name != "Downloads" }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(userPlaylists, key = { it.id }) { playlist ->
                    val playlistTracksFlow = remember(playlist.id) {
                        viewModel.getTracksInPlaylistFlow(playlist.id)
                    }
                    val playlistTracks by playlistTracksFlow.collectAsState(initial = emptyList())
                    val containsTrack = playlistTracks.any { it.id == track.id }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (containsTrack) {
                                    viewModel.removeTrackFromPlaylist(playlist.id, track.id)
                                    Toast.makeText(context, "Removed from \"${playlist.name}\"", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addTrackToPlaylist(playlist.id, track)
                                    Toast.makeText(context, "Added to \"${playlist.name}\"", Toast.LENGTH_SHORT).show()
                                }
                                onDismiss()
                            }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlaylistMosaicThumbnail(
                            playlist = playlist,
                            tracks = playlistTracks,
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${playlistTracks.size} songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }

                        if (containsTrack) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(EcoDotRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    SimpIcons.Check,
                                    contentDescription = "Added",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
