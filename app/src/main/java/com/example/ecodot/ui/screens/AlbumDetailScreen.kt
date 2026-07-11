package com.example.ecodot.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecodot.ui.viewmodel.AlbumViewModel
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.ui.theme.*
import com.example.ecodot.ui.components.*
import kotlin.math.min
import androidx.compose.material.icons.rounded.DownloadDone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: String,
    musicViewModel: MusicViewModel,
    navController: NavController,
    viewModel: AlbumViewModel = viewModel()
) {
    val downloadProgressMap by musicViewModel.downloadProgress.collectAsState()
    val downloadedTracks by musicViewModel.downloadedTracks.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var optionsMenuTrack by remember { mutableStateOf<com.example.ecodot.data.local.entities.Track?>(null) }
    var showPlaylistPicker by remember { mutableStateOf<com.example.ecodot.data.local.entities.Track?>(null) }

    // Calculate scroll progress for fading the top bar and parallax
    val scrollOffset = listState.firstVisibleItemScrollOffset
    val firstItemIndex = listState.firstVisibleItemIndex
    val progress = min(1f, if (firstItemIndex > 0) 1f else scrollOffset / 600f)

    LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }

    Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            if (!uiState.isLoading && uiState.tracks.isNotEmpty()) {
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val fabScale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f))

                FloatingActionButton(
                    onClick = { musicViewModel.playQueue(uiState.tracks, startIndex = 0) },
                    containerColor = EcoDotRed,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 90.dp)
                        .scale(fabScale),
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play All", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Immersive background blur layer
            if (!uiState.thumbnailUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                        .graphicsLayer {
                            alpha = 1f - (progress * 0.8f)
                            translationY = -(scrollOffset * 0.4f)
                        }
                ) {
                    AsyncImage(
                        model = uiState.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.2f)
                            .scale(1.2f),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay to blend into the black background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Black.copy(alpha = 0.8f),
                                        Color.Black
                                    ),
                                    startY = 0f,
                                    endY = 1200f
                                )
                            )
                    )
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EcoDotRed)
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error ?: "Unknown error", color = EcoDotRed)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Album Art with heavy shadow
                            Box(
                                modifier = Modifier
                                    .size(260.dp)
                                    .padding(16.dp)
                            ) {
                                AsyncImage(
                                    model = uiState.thumbnailUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = uiState.title,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                letterSpacing = (-0.5).sp
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(
                                    text = uiState.artistName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EcoDotRed.copy(0.9f),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { 
                                        uiState.artistId?.let { navController.navigate("artist/$it") }
                                    }
                                )
                                Text(
                                    text = " • ${uiState.year ?: "Album"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val savedAlbums by musicViewModel.allAlbums.collectAsState()
                            val isAlbumSaved = remember(uiState.title, savedAlbums) {
                                savedAlbums.any { it.title.equals(uiState.title, ignoreCase = true) }
                            }
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (isAlbumSaved) {
                                            musicViewModel.unsaveAlbum(uiState.tracks)
                                        } else {
                                            musicViewModel.saveAlbum(uiState.tracks)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAlbumSaved) Color.White.copy(alpha = 0.1f) else EcoDotRed
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAlbumSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (isAlbumSaved) EcoDotRed else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isAlbumSaved) "Saved" else "Save Album",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    itemsIndexed(uiState.tracks, key = { _, track -> track.id }) { index, track ->
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val rowScale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f))

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it != SwipeToDismissBoxValue.Settled) {
                                    musicViewModel.addToQueue(track)
                                    false
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) EcoDotRed else Color.Transparent
                                )
                                val iconAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = iconAlignment
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = "Add to Queue", tint = Color.White)
                                }
                            }
                        ) {
                            val currentTrack by musicViewModel.currentTrack.collectAsState()
                            val isPlaying = currentTrack?.id == track.id
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .scale(rowScale)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isPressed) Color.White.copy(0.05f) else Color.Transparent)
                                    .clickable(
                                        onClick = { musicViewModel.playQueue(uiState.tracks, startIndex = index) },
                                        interactionSource = interactionSource,
                                        indication = LocalIndication.current
                                    )
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isPlaying) {
                                    Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                        com.example.ecodot.ui.components.PlayingIndicator()
                                    }
                                } else {
                                    Text(
                                        text = (index + 1).toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.width(36.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        track.title, 
                                        color = if (isPlaying) EcoDotRed else Color.White, 
                                        maxLines = 1, 
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                                            ExplicitBadge(modifier = Modifier.padding(end = 6.dp))
                                        }
                                        Text(
                                            track.artist, 
                                            fontSize = 13.sp, 
                                            color = Color.White.copy(alpha = 0.5f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                
                                val isDownloaded = downloadedTracks.any { it.id == track.id }
                                val progress = downloadProgressMap[track.id]
                                
                                if (progress != null) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                                        CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(20.dp), color = EcoDotRed, strokeWidth = 2.dp, trackColor = Color.White.copy(0.1f))
                                        Text("${(progress * 100).toInt()}%", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else if (isDownloaded) {
                                    Icon(Icons.Rounded.DownloadDone, contentDescription = "Downloaded", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                }
                                
                                IconButton(onClick = { optionsMenuTrack = track }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }

            // Glassmorphic Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFF101010).copy(alpha = progress * 0.85f))
                    .padding(top = 36.dp, start = 8.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.background(Color.Black.copy(0.3f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Text(
                        text = uiState.title,
                        color = Color.White.copy(alpha = progress),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    optionsMenuTrack?.let { track ->
        val isDownloaded = downloadedTracks.any { it.id == track.id }
        val downloadProgress = downloadProgressMap[track.id]
        val isLiked by musicViewModel.isTrackLiked(track.id).collectAsState(initial = false)
        
        com.example.ecodot.ui.components.TrackOptionsBottomSheet(
            track = track,
            onDismiss = { optionsMenuTrack = null },
            onPlayNext = { musicViewModel.playNext(track) },
            onAddToQueue = { musicViewModel.addToQueue(track) },
            onGoToAlbum = { 
                optionsMenuTrack = null
                if (track.album != "Unknown Album" && !track.albumId.isNullOrEmpty()) {
                    navController.navigate("album/${track.albumId}")
                }
            },
            onGoToArtist = { 
                optionsMenuTrack = null
                track.artistId?.let { navController.navigate("artist/$it") }
            },
            isDownloaded = isDownloaded,
            downloadProgress = downloadProgress,
            onDownload = { musicViewModel.downloadTrack(track) },
            onDeleteDownload = { musicViewModel.deleteDownload(track) },
            isLiked = isLiked,
            onLikeToggle = { musicViewModel.toggleTrackLike(track) },
            onAddToPlaylist = {
                optionsMenuTrack = null
                showPlaylistPicker = track
            },
            onSetCanvasUrl = { url -> 
                if (url == "FETCH_CANVAS") musicViewModel.enableCanvasForTrack(track)
                else musicViewModel.setCanvasUrl(track, url)
            }
        )
    }

    showPlaylistPicker?.let { track ->
        val playlists by musicViewModel.allPlaylists.collectAsState()
        com.example.ecodot.ui.components.AddToPlaylistDialog(
            track = track,
            playlists = playlists,
            onDismiss = { showPlaylistPicker = null },
            onPlaylistSelected = { playlist ->
                musicViewModel.addTrackToPlaylist(playlist.id, track)
                showPlaylistPicker = null
            },
            onCreatePlaylist = { name ->
                musicViewModel.createPlaylist(name)
            }
        )
    }
}