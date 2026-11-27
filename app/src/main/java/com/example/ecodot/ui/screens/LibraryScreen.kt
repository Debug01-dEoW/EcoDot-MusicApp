package com.example.ecodot.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.FollowedArtist
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.components.*
import com.example.ecodot.ui.theme.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.example.ecodot.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    onEqualizerClick: () -> Unit = {},
    onWatchVideo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Active Filter Chip ──────────────────────────────────────────────
    // Matches SimpMusic's exact filter categories:
    val filterTabs = remember {
        listOf(
            "Your library",
            "Your Local Playlists",
            "Favorite Playlists",
            "Downloaded Playlists"
        )
    }
    var selectedFilter by remember { mutableStateOf("Your library") }

    // ── Data from ViewModel ─────────────────────────────────────────────
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val followedArtists by viewModel.followedArtists.collectAsState()
    val mostPlayed by viewModel.mostPlayed.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val allTracks by viewModel.tracks.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isVideoMode by viewModel.isVideoMode.collectAsState()
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsState()
    val playerStyle by viewModel.playerStyle.collectAsState()
    val isCanvasEnabledByUser by viewModel.isCanvasEnabledByUser.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    val pinnedPlaylistIds by viewModel.pinnedPlaylistIds.collectAsState()
    val cachedTracks by viewModel.cachedTracks.collectAsState()
    val cachedTrackIds by viewModel.cachedTrackIds.collectAsState()

    // Liked Songs playlist resolution
    val likedPlaylist = allPlaylists.find { it.name == "Liked Songs" }
    val likedTracksFlow = remember(likedPlaylist?.id) {
        if (likedPlaylist != null) viewModel.getTracksInPlaylistFlow(likedPlaylist.id)
        else kotlinx.coroutines.flow.flowOf(emptyList())
    }
    val likedTracks by likedTracksFlow.collectAsState(initial = emptyList())
    val likedCount = likedTracks.size

    // Downloads playlist resolution
    val downloadsPlaylist = allPlaylists.find { it.name == "Downloads" }

    // User's custom playlists (excluding system ones)
    val userPlaylists = remember(allPlaylists, pinnedPlaylistIds) {
        val filtered = allPlaylists.filter { it.name != "Liked Songs" && it.name != "Downloads" }
        filtered.sortedByDescending { pinnedPlaylistIds.contains(it.id.toString()) }
    }

    // Favorite / Pinned playlists
    val favoritePlaylists = remember(userPlaylists, pinnedPlaylistIds) {
        userPlaylists.filter { pinnedPlaylistIds.contains(it.id.toString()) }
    }

    // Downloaded playlists
    val downloadedPlaylists = remember(allPlaylists, downloadedTracks) {
        allPlaylists.filter { it.name == "Downloads" || it.id in downloadedTracks.mapNotNull { t -> t.albumId?.toLongOrNull() } }
    }

    // Recently added songs list for "Your library"
    val recentlyAddedSongs = remember(recentlyPlayed, allTracks) {
        val base = if (recentlyPlayed.isNotEmpty()) recentlyPlayed else allTracks
        base.distinctBy { it.id }.take(35)
    }

    // ── Navigation & Sheet Overlays ──────────────────────────────────────
    var openPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showCachedSongsScreen by remember { mutableStateOf(false) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showListenTogetherSheet by remember { mutableStateOf(false) }
    var playlistForOptions by remember { mutableStateOf<Pair<Playlist, List<Track>>?>(null) }
    var playlistToEdit by remember { mutableStateOf<Playlist?>(null) }
    var trackForOptions by remember { mutableStateOf<Track?>(null) }
    var trackForAddToPlaylist by remember { mutableStateOf<Track?>(null) }
    var ringtoneClipperTrack by remember { mutableStateOf<Track?>(null) }
    var showPlayerStyleSheet by remember { mutableStateOf(false) }

    // Listen for bottom-bar tab reset when user taps "Library" while already on Library
    LaunchedEffect(Unit) {
        viewModel.libraryResetEvent.collect {
            if (openPlaylist != null) {
                openPlaylist = null
            } else if (showCachedSongsScreen) {
                showCachedSongsScreen = false
            } else {
                selectedFilter = "Your library"
            }
        }
    }

    // Intercept back navigation hierarchically for sheets, dialogs, and detail screen
    BackHandler(
        enabled = openPlaylist != null || showCachedSongsScreen || showCreateSheet || showListenTogetherSheet || 
                  playlistForOptions != null || playlistToEdit != null || 
                  trackForOptions != null || trackForAddToPlaylist != null
    ) {
        when {
            showListenTogetherSheet -> showListenTogetherSheet = false
            showCreateSheet -> showCreateSheet = false
            playlistToEdit != null -> playlistToEdit = null
            playlistForOptions != null -> playlistForOptions = null
            trackForAddToPlaylist != null -> trackForAddToPlaylist = null
            trackForOptions != null -> trackForOptions = null
            showCachedSongsScreen -> showCachedSongsScreen = false
            openPlaylist != null -> openPlaylist = null
        }
    }

    // ── Cached Songs Screen Overlay ─────────────────────────────────────
    if (showCachedSongsScreen) {
        CachedSongsScreen(
            tracks = cachedTracks,
            viewModel = viewModel,
            onBack = { showCachedSongsScreen = false },
            onOptionsClick = { trackForOptions = it },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    // ── Detail Screen Overlay ───────────────────────────────────────────
    openPlaylist?.let { playlist ->
        PlaylistDetailScreen(
            playlist = playlist,
            viewModel = viewModel,
            onBack = { openPlaylist = null },
            modifier = Modifier.fillMaxSize(),
            onGoToAlbum = { track ->
                openPlaylist = null
                if (track.album.isNotBlank() && !track.albumId.isNullOrEmpty()) {
                    navController.navigate("album/${track.albumId}")
                } else if (track.album.isNotBlank()) {
                    viewModel.performSearch(track.album)
                    navController.navigate("search")
                }
            },
            onGoToArtist = { track ->
                openPlaylist = null
                if (!track.artistId.isNullOrEmpty()) {
                    navController.navigate("artist/${track.artistId}")
                } else if (track.artist.isNotBlank()) {
                    viewModel.performSearch(track.artist)
                    navController.navigate("search")
                }
            },
            onEqualizerClick = onEqualizerClick,
            onWatchVideo = { onWatchVideo() }
        )
        return
    }

    // ── Create Playlist Bottom Sheet ────────────────────────────────────
    if (showCreateSheet) {
        CreatePlaylistBottomSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { name, description, customCoverJson ->
                viewModel.createPlaylistCustom(name, description, customCoverJson)
                Toast.makeText(context, "Created playlist \"$name\"", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ── Edit Playlist Bottom Sheet ──────────────────────────────────────
    playlistToEdit?.let { playlist ->
        EditPlaylistBottomSheet(
            playlist = playlist,
            onDismiss = { playlistToEdit = null },
            onSave = { newName, newDesc, newCoverJson ->
                viewModel.updatePlaylistDetails(playlist, newName, newDesc, newCoverJson)
                Toast.makeText(context, "Playlist updated", Toast.LENGTH_SHORT).show()
                playlistToEdit = null
            }
        )
    }

    // ── Playlist Contextual Options Bottom Sheet ────────────────────────
    playlistForOptions?.let { (playlist, tracks) ->
        val isPinned = pinnedPlaylistIds.contains(playlist.id.toString())
        LibraryPlaylistOptionsBottomSheet(
            playlist = playlist,
            tracks = tracks,
            isPinned = isPinned,
            onDismiss = { playlistForOptions = null },
            onPlay = { viewModel.playPlaylist(playlist) },
            onPlayNext = { viewModel.playPlaylistNext(playlist) },
            onAddToQueue = { viewModel.addPlaylistToQueue(playlist) },
            onTogglePin = {
                val pinned = viewModel.togglePinPlaylist(playlist.id)
                Toast.makeText(context, if (pinned) "Pinned to top" else "Unpinned", Toast.LENGTH_SHORT).show()
            },
            onEdit = {
                playlistToEdit = playlist
            },
            onDelete = {
                viewModel.deletePlaylist(playlist)
                Toast.makeText(context, "Deleted \"${playlist.name}\"", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ── Track Options Bottom Sheet ──────────────────────────────────────
    trackForOptions?.let { track ->
        val isDownloaded = downloadedTracks.any { it.id == track.id }
        val downloadProgress = downloadProgressMap[track.id]
        val isLiked by viewModel.isTrackLiked(track.id).collectAsState(initial = false)
        val isActiveTrack = currentTrack?.id == track.id
        val trackInVideoMode = isActiveTrack && (isVideoMode || viewModel.isTrackVideo(track))

        TrackOptionsBottomSheet(
            track = track,
            onDismiss = { trackForOptions = null },
            onPlayNow = { viewModel.playTrack(track) },
            onPlayNext = { viewModel.playNext(track) },
            onAddToQueue = { viewModel.addToQueue(track) },
            onGoToAlbum = {
                trackForOptions = null
                if (track.album.isNotBlank() && !track.albumId.isNullOrEmpty()) {
                    navController.navigate("album/${track.albumId}")
                } else if (track.album.isNotBlank()) {
                    viewModel.performSearch(track.album)
                    navController.navigate("search")
                }
            },
            onGoToArtist = {
                trackForOptions = null
                if (!track.artistId.isNullOrEmpty()) {
                    navController.navigate("artist/${track.artistId}")
                } else if (track.artist.isNotBlank()) {
                    viewModel.performSearch(track.artist)
                    navController.navigate("search")
                }
            },
            isDownloaded = isDownloaded,
            downloadProgress = downloadProgress,
            onDownload = { viewModel.downloadTrack(track) },
            onDeleteDownload = { viewModel.deleteDownload(track) },
            isLiked = isLiked,
            onLikeToggle = { viewModel.toggleTrackLike(track) },
            onAddToPlaylist = {
                trackForOptions = null
                trackForAddToPlaylist = track
            },
            onSetSleepTimer = { viewModel.setSleepTimer(it) },
            onSetSleepTimerAtEndOfTrack = { viewModel.setSleepTimerAtEndOfTrack() },
            onCancelSleepTimer = { viewModel.cancelSleepTimer() },
            sleepTimerRemaining = sleepTimerRemaining,
            isVideoMode = trackInVideoMode,
            onToggleVideoMode = {
                if (isActiveTrack) {
                    viewModel.setVideoMode(!trackInVideoMode)
                } else {
                    viewModel.playTrack(track, isVideo = true)
                    viewModel.setVideoMode(true)
                    onWatchVideo()
                }
            },
            onSwitchPlayerStyle = { showPlayerStyleSheet = true },
            onEqualizerClick = onEqualizerClick,
            isCanvasActive = isCanvasEnabledByUser,
            onSetCanvasUrl = { url ->
                if (url == "DISABLED") viewModel.disableCanvas()
                else if (url == "FETCH_CANVAS") viewModel.enableCanvasForTrack(track)
                else viewModel.setCanvasUrl(track, url)
            },
            onSetAsRingtone = { ringtoneClipperTrack = track }
        )
    }

    ringtoneClipperTrack?.let { track ->
        com.example.ecodot.ui.components.RingtoneClipperBottomSheet(
            track = track,
            viewModel = viewModel,
            onDismiss = { ringtoneClipperTrack = null }
        )
    }

    if (showPlayerStyleSheet) {
        com.example.ecodot.ui.components.player.PlayerStyleBottomSheet(
            currentStyle = playerStyle,
            accentColor = EcoDotRed,
            onStyleSelected = { viewModel.setPlayerStyle(it) },
            onDismiss = { showPlayerStyleSheet = false }
        )
    }

    // ── Add To Playlist Bottom Sheet ────────────────────────────────────
    trackForAddToPlaylist?.let { track ->
        AddToPlaylistBottomSheet(
            track = track,
            viewModel = viewModel,
            onDismiss = { trackForAddToPlaylist = null }
        )
    }

    // ── Listen Together Bottom Sheet ────────────────────────────────────
    if (showListenTogetherSheet) {
        ModalBottomSheet(
            onDismissRequest = { showListenTogetherSheet = false },
            containerColor = Color(0xFF1F2026),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE7D7FD).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Groups,
                        contentDescription = null,
                        tint = Color(0xFFE7D7FD),
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Listen Together",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Stream and discover songs together with friends in real-time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF282932))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ROOM CODE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "ECO-8821",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Room Code", "ECO-8821")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Room code copied!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE7D7FD)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Copy", color = Color(0xFF1E1035), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        Toast.makeText(context, "Session started! Share the room code to invite.", Toast.LENGTH_SHORT).show()
                        showListenTogetherSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE7D7FD)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Start Host Session", color = Color(0xFF1E1035), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // ── Main Screen Surface ─────────────────────────────────────────────
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
            // ── Top App Bar (Title "Library" on Left, Frosted Listen Together on Right) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                // Frosted circular Listen Together button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable { showListenTogetherSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Groups,
                        contentDescription = "Listen Together",
                        tint = Color(0xFFE7D7FD),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ── Filter Chips Bar ────────────────────────────────────────
            // Selected chip: soft lilac pill `#E7D7FD` with black bold text
            // Inactive chip: frosted dark pill `#191A22` with subtle outline border
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filterTabs.forEach { tab ->
                    val isSelected = selectedFilter == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(
                                if (isSelected) Color(0xFFE7D7FD)
                                else Color(0xFF191A22).copy(alpha = 0.90f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.09f),
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .clickable { selectedFilter = tab }
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color(0xFF1E1035) else Color.White.copy(alpha = 0.75f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }

            // ── Main Content Area ───────────────────────────────────────
            AnimatedContent(
                targetState = selectedFilter,
                transitionSpec = {
                    fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(120))
                },
                label = "library_filter_transition",
                modifier = Modifier
                    .fillMaxSize()
                    .background(EcoDotBlack)
            ) { filter ->
                when (filter) {
                    // ── 1. Your library (The default SimpMusic Home tab) ──────
                    "Your library" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
                        ) {
                            // Hero Tiling Box (Favorite wide card + 2x2 grid cards)
                            item {
                                LibraryTilingBox(
                                    favoriteCount = likedCount,
                                    favoriteThumbnails = likedTracks.mapNotNull { it.albumArtUri }.take(3),
                                    onFavoriteClick = {
                                        likedPlaylist?.let { openPlaylist = it } ?: scope.launch {
                                            val p = viewModel.getPlaylistByName("Liked Songs")
                                            if (p != null) openPlaylist = p
                                        }
                                    },
                                    followedCount = followedArtists.size,
                                    followedThumbnails = followedArtists.mapNotNull { it.imageUrl }.take(2),
                                    onFollowedClick = {
                                        if (followedArtists.isNotEmpty()) {
                                            navController.navigate("artist/${followedArtists.first().artistId}")
                                        } else {
                                            Toast.makeText(context, "No artists followed yet", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    mostPlayedCount = mostPlayed.size,
                                    mostPlayedThumbnails = mostPlayed.mapNotNull { it.albumArtUri }.take(2),
                                    onMostPlayedClick = {
                                        if (mostPlayed.isNotEmpty()) {
                                            viewModel.playTracks(mostPlayed, startIndex = 0)
                                        } else {
                                            Toast.makeText(context, "No most played songs yet", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    downloadedCount = downloadedTracks.size,
                                    downloadedThumbnails = downloadedTracks.mapNotNull { it.albumArtUri }.take(2),
                                    onDownloadedClick = {
                                        downloadsPlaylist?.let { openPlaylist = it } ?: scope.launch {
                                            val p = viewModel.getPlaylistByName("Downloads")
                                            if (p != null) openPlaylist = p
                                        }
                                    },
                                    cachedCount = if (cachedTracks.isNotEmpty()) cachedTracks.size else cachedTrackIds.size,
                                    cachedThumbnails = cachedTracks.mapNotNull { it.albumArtUri }.take(2),
                                    onCachedClick = { showCachedSongsScreen = true }
                                )
                            }

                            // ── Playlists Carousel Section ──────────────────────────────
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Playlists",
                                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(percent = 50))
                                                .background(Color.White.copy(alpha = 0.08f))
                                                .padding(horizontal = 10.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "${userPlaylists.size}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE7D7FD)
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "+ New",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE7D7FD),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { showCreateSheet = true }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                        Text(
                                            text = "See all",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White.copy(alpha = 0.55f),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedFilter = "Your Local Playlists" }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            item {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // "+ Create New" Tile Card
                                    item {
                                        CreatePlaylistOverviewCard(
                                            onClick = { showCreateSheet = true }
                                        )
                                    }

                                    // User Playlists
                                    items(userPlaylists, key = { "overview_p_${it.id}" }) { playlist ->
                                        val tracksFlow = remember(playlist.id) {
                                            viewModel.getTracksInPlaylistFlow(playlist.id)
                                        }
                                        val tracks by tracksFlow.collectAsState(initial = emptyList())
                                        val isPinned = pinnedPlaylistIds.contains(playlist.id.toString())

                                        PlaylistOverviewCard(
                                            playlist = playlist,
                                            tracks = tracks,
                                            isPinned = isPinned,
                                            onClick = { openPlaylist = playlist },
                                            onLongClick = { playlistForOptions = Pair(playlist, tracks) }
                                        )
                                    }
                                }
                            }

                            // Section Title: "Recently Added" with track count chip
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Recently Added",
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (recentlyAddedSongs.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(percent = 50))
                                                .background(Color.White.copy(alpha = 0.08f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${recentlyAddedSongs.size} tracks",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }

                            // List of Recently Added Songs
                            items(recentlyAddedSongs, key = { "recent_${it.id}" }) { track ->
                                val isThisPlaying = isPlaying && currentTrack?.id == track.id
                                RecentTrackRow(
                                    track = track,
                                    isPlaying = isThisPlaying,
                                    onClick = { viewModel.playTrack(track) },
                                    onOptionsClick = { trackForOptions = track },
                                    onAddToQueue = { viewModel.addToQueue(track) }
                                )
                            }
                        }
                    }

                    // ── 2. Your Local Playlists (SimpMusic Grid with + Create) ──
                    "Your Local Playlists" -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 120.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // First tile: + Create Card (45° angled gradient + large add icon)
                            item {
                                Box(
                                    modifier = Modifier
                                        .clickable { showCreateSheet = true }
                                        .padding(2.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(Color(0xFF222432), Color(0xFF14151D)),
                                                        start = Offset(0f, 0f),
                                                        end = Offset(300f, 300f)
                                                    )
                                                )
                                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = "Create",
                                                tint = Color.White,
                                                modifier = Modifier.size(54.dp)
                                            )
                                        }
                                        Text(
                                            text = "Create",
                                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp)
                                        )
                                    }
                                }
                            }

                            // Followed by user local playlists
                            items(userPlaylists, key = { "local_p_${it.id}" }) { playlist ->
                                val tracksFlow = remember(playlist.id) {
                                    viewModel.getTracksInPlaylistFlow(playlist.id)
                                }
                                val tracks by tracksFlow.collectAsState(initial = emptyList())
                                val isPinned = pinnedPlaylistIds.contains(playlist.id.toString())

                                PlaylistGridCell(
                                    playlist = playlist,
                                    tracks = tracks,
                                    isPinned = isPinned,
                                    onClick = { openPlaylist = playlist },
                                    onLongClick = { playlistForOptions = Pair(playlist, tracks) }
                                )
                            }
                        }
                    }

                    // ── 4. Favorite Playlists ────────────────────────────────
                    "Favorite Playlists" -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 120.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .clickable { showCreateSheet = true }
                                        .padding(2.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(Color(0xFF222432), Color(0xFF14151D)),
                                                        start = Offset(0f, 0f),
                                                        end = Offset(300f, 300f)
                                                    )
                                                )
                                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = "Create",
                                                tint = Color.White,
                                                modifier = Modifier.size(54.dp)
                                            )
                                        }
                                        Text(
                                            text = "Create",
                                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                        )
                                    }
                                }
                            }

                            items(favoritePlaylists, key = { "fav_p_${it.id}" }) { playlist ->
                                val tracksFlow = remember(playlist.id) {
                                    viewModel.getTracksInPlaylistFlow(playlist.id)
                                }
                                val tracks by tracksFlow.collectAsState(initial = emptyList())

                                PlaylistGridCell(
                                    playlist = playlist,
                                    tracks = tracks,
                                    isPinned = true,
                                    onClick = { openPlaylist = playlist },
                                    onLongClick = { playlistForOptions = Pair(playlist, tracks) }
                                )
                            }
                        }
                    }

                    // ── 5. Downloaded Playlists ──────────────────────────────
                    "Downloaded Playlists" -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 120.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(downloadedPlaylists, key = { "dl_p_${it.id}" }) { playlist ->
                                val tracksFlow = remember(playlist.id) {
                                    viewModel.getTracksInPlaylistFlow(playlist.id)
                                }
                                val tracks by tracksFlow.collectAsState(initial = emptyList())

                                PlaylistGridCell(
                                    playlist = playlist,
                                    tracks = tracks,
                                    isPinned = false,
                                    onClick = { openPlaylist = playlist },
                                    onLongClick = { playlistForOptions = Pair(playlist, tracks) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single song row in the "Recently Added" section, exactly matching SimpMusic.
 */
@Composable
private fun RecentTrackRow(
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
            // 52dp square album cover with 12dp rounded corners and subtle border
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B1C24))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
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
                            Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
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
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) Color(0xFFE7D7FD) else Color.White,
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

            // 3-dots option menu button
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Playlist cell in 3-column GridLibraryPlaylist layout.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistGridCell(
    playlist: Playlist,
    tracks: List<Track>,
    isPinned: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            ) {
                PlaylistMosaicThumbnail(
                    playlist = playlist,
                    tracks = tracks,
                    modifier = Modifier.fillMaxSize()
                )

                if (isPinned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.PushPin,
                            contentDescription = "Pinned",
                            tint = Color(0xFFE7D7FD),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                text = "${tracks.size} songs",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Aesthetic Playlist Card in the Library overview matching Favorite, Cached, and Downloaded cards.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistOverviewCard(
    playlist: Playlist,
    tracks: List<Track>,
    isPinned: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(180.dp)
            .height(120.dp),
        shape = AestheticCardShape,
        border = AestheticCardBorder,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1C23))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            val validThumbs = remember(tracks) {
                tracks.mapNotNull { it.albumArtUri?.takeIf { u -> u.isNotBlank() } }.distinct().take(2)
            }

            SleeveFan(
                thumbnails = validThumbs,
                slots = 2,
                sleeve = 42.dp,
                spread = 20.dp,
                tilt = 9f,
                round = false,
                ring = Color(0xFF1B1C23),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .size(width = 66.dp, height = 50.dp)
            )

            if (isPinned) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.PushPin,
                        contentDescription = "Pinned",
                        tint = Color(0xFFE7D7FD),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${tracks.size} songs",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * "+ Create New" card matching the exact same aesthetic card styling.
 */
@Composable
private fun CreatePlaylistOverviewCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(155.dp)
            .height(120.dp),
        shape = AestheticCardShape,
        border = AestheticCardBorder,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1C23))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 14.dp)
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Create",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "New Playlist",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = "Create mix",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1
                )
            }
        }
    }
}
