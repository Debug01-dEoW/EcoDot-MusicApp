package com.example.ecodot.ui.screens

import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.scale
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.local.entities.FollowedArtist
import androidx.compose.material.icons.rounded.DownloadDone
import com.example.ecodot.ui.viewmodel.MusicViewModel
import androidx.activity.compose.BackHandler
import com.example.ecodot.util.PlaylistCustomizer
import com.example.ecodot.util.CustomCover
import com.example.ecodot.ui.theme.*
import com.example.ecodot.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val mostPlayed by viewModel.mostPlayed.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val albums by viewModel.allAlbums.collectAsState()
    val genres by viewModel.allGenres.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    val followedArtists by viewModel.followedArtists.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val likedPlaylist = allPlaylists.find { it.name == "Liked Songs" }
    val likedTracksFlow = remember(likedPlaylist?.id) {
        if (likedPlaylist != null) {
            viewModel.getTracksInPlaylistFlow(likedPlaylist.id)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }
    val likedTracks by likedTracksFlow.collectAsState(initial = emptyList())
    val likedCount = likedTracks.size

    // Active playlist detail state
    var openPlaylist by remember { mutableStateOf<Playlist?>(null) }

    // Playback history state
    var showHistory by remember { mutableStateOf(false) }
    var showPlaylistMenu by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var showPlusBottomSheet by remember { mutableStateOf(false) }
    var showEditPlaylistsDialog by remember { mutableStateOf(false) }
    var longPressedTrack by remember { mutableStateOf<Track?>(null) }
    var longPressedAlbum by remember { mutableStateOf<com.example.ecodot.ui.viewmodel.Album?>(null) }
    var longPressedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showPlaylistPicker by remember { mutableStateOf<Track?>(null) }
    val scope = rememberCoroutineScope()

    BackHandler(enabled = showHistory) {
        showHistory = false
    }

    if (showHistory) {
        HistoryScreen(
            viewModel = viewModel,
            onBack = { showHistory = false },
            modifier = modifier
        )
        return
    }

    // Create playlist dialog state
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistDesc by remember { mutableStateOf("") }
    var selectedStartColor by remember { mutableStateOf("#FF1DB954") }
    var selectedEndColor by remember { mutableStateOf("#FF0D2C1D") }
    var isPrivatePlaylist by remember { mutableStateOf(false) }
    var playlistTagsText by remember { mutableStateOf("") }

    // Show detail screen for selected playlist
    openPlaylist?.let { playlist ->
        PlaylistDetailScreen(
            playlist = playlist,
            viewModel = viewModel,
            onBack = { openPlaylist = null }
        )
        return
    }

    // Create playlist dialog
    if (showCreateDialog) {
        val colorOptions = listOf(
            Pair("#FF1DB954", "#FF0D2C1D"), // Spotify Green
            Pair("#FFF857A6", "#FFFF5858"), // Sunset Pink
            Pair("#FF4FACFE", "#FF00F2FE"), // Neon Blue
            Pair("#FFFF416C", "#FFFF4B2B"), // Red Orange
            Pair("#FF7F00FF", "#FFE100FF"), // Midnight Violet
            Pair("#FF3A7BD5", "#FF3A6073")  // Slate Blue
        )

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = EcoDotCard,
            title = {
                Text("New Playlist", color = Color.White, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Name", color = Color.White.copy(alpha = 0.6f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EcoDotRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPlaylistDesc,
                        onValueChange = { newPlaylistDesc = it },
                        label = { Text("Description (optional)", color = Color.White.copy(alpha = 0.6f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EcoDotRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Theme gradient colors selector
                    Text("Theme Color", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(colorOptions) { (startColor, endColor) ->
                            val startParsed = PlaylistCustomizer.parseColorSafe(startColor, Color.Gray)
                            val endParsed = PlaylistCustomizer.parseColorSafe(endColor, Color.Black)
                            val isSelected = selectedStartColor == startColor && selectedEndColor == endColor
                            
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(startParsed, endParsed)))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedStartColor = startColor
                                        selectedEndColor = endColor
                                    }
                            )
                        }
                    }

                    // Privacy toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Private Playlist", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = isPrivatePlaylist,
                            onCheckedChange = { isPrivatePlaylist = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EcoDotRed,
                                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    // Tags field
                    OutlinedTextField(
                        value = playlistTagsText,
                        onValueChange = { playlistTagsText = it },
                        label = { Text("Tags (comma separated)", color = Color.White.copy(alpha = 0.6f)) },
                        singleLine = true,
                        placeholder = { Text("chill, workout, focus", color = Color.White.copy(alpha = 0.3f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EcoDotRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            val tagsList = playlistTagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val customCoverObj = CustomCover(
                                startColor = selectedStartColor,
                                endColor = selectedEndColor,
                                isPrivate = isPrivatePlaylist,
                                tags = tagsList
                            )
                            val customJson = PlaylistCustomizer.serializeCustomCover(customCoverObj)
                            
                            viewModel.createPlaylistCustom(
                                name = newPlaylistName.trim(),
                                description = newPlaylistDesc.trim(),
                                customJson = customJson
                            )
                            newPlaylistName = ""
                            newPlaylistDesc = ""
                            playlistTagsText = ""
                            isPrivatePlaylist = false
                            showCreateDialog = false
                        }
                    }
                ) { Text("Create", color = EcoDotRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    LiquidMeshBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                // Top bar Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 40.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .animatedClickable { navController.navigate("profile") }
                    ) {
                        if (!userProfile?.avatarUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = userProfile?.avatarUri,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = "Profile",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxSize().padding(6.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Your Library",
                        style = MaterialTheme.typography.titleLarge.copy(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color.White, Color(0xFFAAAAAA))
                            )
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = { showPlusBottomSheet = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                
                // Filter Chips Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    val filters = listOf("All", "Playlists", "Downloads", "Albums", "Artists")
                    filters.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) EcoDotRed else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .clickable {
                                    selectedFilter = filter
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    thickness = 1.dp
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Recents Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.animatedClickable { /* Sort logic placeholder */ }
                ) {
                    Icon(
                        Icons.Rounded.Sort,
                        contentDescription = "Sort",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Recents",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Icon(
                    Icons.Rounded.GridView,
                    contentDescription = "Grid Layout",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Grid content depending on selectedFilter
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 3 columns matching the layout requested!
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedFilter) {
                    "All" -> {
                        // Liked Songs Pinned Card
                        item {
                            LibraryGridItem(
                                title = "Liked Songs",
                                subtitle = "Playlist • Me",
                                imageUrl = null,
                                customCoverJson = PlaylistCustomizer.serializeCustomCover(
                                    CustomCover(startColor = "#FFFF2658", endColor = "#FF86002A", isPrivate = false, tags = emptyList())
                                ),
                                isPinned = true,
                                onLongClick = {
                                    val liked = allPlaylists.find { it.name == "Liked Songs" }
                                    if (liked != null) {
                                        longPressedPlaylist = liked
                                    }
                                },
                                onClick = {
                                    val liked = allPlaylists.find { it.name == "Liked Songs" }
                                    if (liked != null) {
                                        openPlaylist = liked
                                    } else {
                                        scope.launch {
                                            val playlist = viewModel.getPlaylistByName("Liked Songs")
                                            if (playlist != null) openPlaylist = playlist
                                        }
                                    }
                                }
                            )
                        }
                        
                        // Downloads card
                        item {
                            LibraryGridItem(
                                title = "Downloads",
                                subtitle = "Playlist • Me",
                                imageUrl = null,
                                customCoverJson = PlaylistCustomizer.serializeCustomCover(
                                    CustomCover(startColor = "#FF1DB954", endColor = "#FF0D2C1D", isPrivate = false, tags = emptyList())
                                ),
                                isPinned = true,
                                onLongClick = {
                                    val downloads = allPlaylists.find { it.name == "Downloads" }
                                    if (downloads != null) {
                                        longPressedPlaylist = downloads
                                    }
                                },
                                onClick = {
                                    val downloads = allPlaylists.find { it.name == "Downloads" }
                                    if (downloads != null) {
                                        openPlaylist = downloads
                                    } else {
                                        scope.launch {
                                            val playlist = viewModel.getPlaylistByName("Downloads")
                                            if (playlist != null) openPlaylist = playlist
                                        }
                                    }
                                }
                            )
                        }
                        
                        // Custom User Playlists
                        val userPlaylists = allPlaylists.filter { it.name != "Liked Songs" && it.name != "Downloads" }
                        items(userPlaylists, key = { "all_playlist_${it.id}" }) { playlist ->
                            val trackCountFlow = remember(playlist.id) {
                                viewModel.getTracksInPlaylistFlow(playlist.id)
                            }
                            val tracks by trackCountFlow.collectAsState(initial = emptyList())
                            val cover = playlist.coverArtUri ?: tracks.firstOrNull()?.albumArtUri
                            val customCover = PlaylistCustomizer.parseCustomCover(cover)
                            
                            val isSystemGenerated = playlist.name.startsWith("Daily") || playlist.name.contains("Mix")
                            val creator = if (isSystemGenerated) "EcoDot" else "Me"
                            
                            LibraryGridItem(
                                title = playlist.name,
                                subtitle = "Playlist • $creator",
                                imageUrl = cover,
                                customCoverJson = if (customCover != null) cover else null,
                                isPinned = false,
                                onLongClick = { longPressedPlaylist = playlist },
                                onClick = { openPlaylist = playlist }
                            )
                        }

                        // Albums
                        items(albums, key = { "all_album_${it.title}" }) { album ->
                            LibraryGridItem(
                                title = album.title,
                                subtitle = "Album • ${album.artist}",
                                imageUrl = album.tracks.firstOrNull()?.albumArtUri,
                                customCoverJson = null,
                                onLongClick = { longPressedAlbum = album },
                                onClick = {
                                    if (album.id != null) {
                                        navController.navigate("album/${album.id}")
                                    }
                                }
                            )
                        }

                        // Artists
                        items(followedArtists, key = { "all_artist_${it.artistId}" }) { artist ->
                            LibraryGridItem(
                                title = artist.artistName,
                                subtitle = "Artist",
                                imageUrl = artist.imageUrl,
                                customCoverJson = null,
                                onClick = {
                                    navController.navigate("artist/${artist.artistId}")
                                }
                            )
                        }
                    }
                    "Playlists" -> {
                        // Liked Songs Pinned Card
                        item {
                            LibraryGridItem(
                                title = "Liked Songs",
                                subtitle = "Playlist • Me",
                                imageUrl = null,
                                customCoverJson = PlaylistCustomizer.serializeCustomCover(
                                    CustomCover(startColor = "#FFFF2658", endColor = "#FF86002A", isPrivate = false, tags = emptyList())
                                ),
                                isPinned = true,
                                onLongClick = {
                                    val liked = allPlaylists.find { it.name == "Liked Songs" }
                                    if (liked != null) {
                                        longPressedPlaylist = liked
                                    }
                                },
                                onClick = {
                                    val liked = allPlaylists.find { it.name == "Liked Songs" }
                                    if (liked != null) {
                                        openPlaylist = liked
                                    } else {
                                        scope.launch {
                                            val playlist = viewModel.getPlaylistByName("Liked Songs")
                                            if (playlist != null) openPlaylist = playlist
                                        }
                                    }
                                }
                            )
                        }
                        
                        // Custom User Playlists
                        val userPlaylists = allPlaylists.filter { it.name != "Liked Songs" && it.name != "Downloads" }
                        items(userPlaylists, key = { "playlist_${it.id}" }) { playlist ->
                            val trackCountFlow = remember(playlist.id) {
                                viewModel.getTracksInPlaylistFlow(playlist.id)
                            }
                            val tracks by trackCountFlow.collectAsState(initial = emptyList())
                            val cover = playlist.coverArtUri ?: tracks.firstOrNull()?.albumArtUri
                            val customCover = PlaylistCustomizer.parseCustomCover(cover)
                            
                            val isSystemGenerated = playlist.name.startsWith("Daily") || playlist.name.contains("Mix")
                            val creator = if (isSystemGenerated) "EcoDot" else "Me"
                            
                            LibraryGridItem(
                                title = playlist.name,
                                subtitle = "Playlist • $creator",
                                imageUrl = cover,
                                customCoverJson = if (customCover != null) cover else null,
                                isPinned = false,
                                onLongClick = { longPressedPlaylist = playlist },
                                onClick = { openPlaylist = playlist }
                            )
                        }
                    }
                    "Downloads" -> {
                        // Downloads card
                        item {
                            LibraryGridItem(
                                title = "Downloads",
                                subtitle = "Playlist • Me",
                                imageUrl = null,
                                customCoverJson = PlaylistCustomizer.serializeCustomCover(
                                    CustomCover(startColor = "#FF1DB954", endColor = "#FF0D2C1D", isPrivate = false, tags = emptyList())
                                ),
                                isPinned = true,
                                onLongClick = {
                                    val downloads = allPlaylists.find { it.name == "Downloads" }
                                    if (downloads != null) {
                                        longPressedPlaylist = downloads
                                    }
                                },
                                onClick = {
                                    val downloads = allPlaylists.find { it.name == "Downloads" }
                                    if (downloads != null) {
                                        openPlaylist = downloads
                                    } else {
                                        scope.launch {
                                            val playlist = viewModel.getPlaylistByName("Downloads")
                                            if (playlist != null) openPlaylist = playlist
                                        }
                                    }
                                }
                            )
                        }
                        // Downloaded tracks list
                        items(downloadedTracks, key = { "downloaded_${it.id}" }) { track ->
                            LibraryGridItem(
                                title = track.title,
                                subtitle = "Song • ${track.artist}",
                                imageUrl = track.albumArtUri,
                                customCoverJson = null,
                                onLongClick = { longPressedTrack = track },
                                onClick = { viewModel.playTrack(track) }
                            )
                        }
                    }
                    "Albums" -> {
                        items(albums, key = { "album_${it.title}" }) { album ->
                            LibraryGridItem(
                                title = album.title,
                                subtitle = "Album • ${album.artist}",
                                imageUrl = album.tracks.firstOrNull()?.albumArtUri,
                                customCoverJson = null,
                                onLongClick = { longPressedAlbum = album },
                                onClick = {
                                    if (album.id != null) {
                                        navController.navigate("album/${album.id}")
                                    }
                                }
                            )
                        }
                    }
                    "Artists" -> {
                        items(followedArtists, key = { "artist_${it.artistId}" }) { artist ->
                            LibraryGridItem(
                                title = artist.artistName,
                                subtitle = "Artist",
                                imageUrl = artist.imageUrl,
                                customCoverJson = null,
                                onClick = {
                                    navController.navigate("artist/${artist.artistId}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal bottom sheet on Add (+) icon click
    if (showPlusBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlusBottomSheet = false },
            containerColor = Color(0xFF161616),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add to Library",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animatedClickable {
                            showPlusBottomSheet = false
                            showCreateDialog = true
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Rounded.PlaylistAdd, contentDescription = null, tint = EcoDotRed, modifier = Modifier.size(24.dp))
                    Text("Create Playlist", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animatedClickable {
                            showPlusBottomSheet = false
                            navController.navigate("daily_mix")
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(24.dp))
                    Text("Your Mix Generator", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animatedClickable {
                            showPlusBottomSheet = false
                            showEditPlaylistsDialog = true
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color(0xFF3498DB), modifier = Modifier.size(24.dp))
                    Text("Edit Playlists", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    // Edit playlists dialog
    if (showEditPlaylistsDialog) {
        val userPlaylists = allPlaylists.filter { it.name != "Liked Songs" && it.name != "Downloads" }
        AlertDialog(
            onDismissRequest = { showEditPlaylistsDialog = false },
            containerColor = EcoDotCard,
            title = { Text("Edit Playlists", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                if (userPlaylists.isEmpty()) {
                    Text("No playlists to edit.", color = Color.White.copy(alpha = 0.5f))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(userPlaylists) { playlist ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    playlist.name,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            showEditPlaylistsDialog = false
                                            openPlaylist = playlist
                                        }
                                    ) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit details", tint = Color.White)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deletePlaylist(playlist)
                                        }
                                    ) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = EcoDotRed)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEditPlaylistsDialog = false }) {
                    Text("Close", color = EcoDotRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    longPressedTrack?.let { track ->
        val isDownloaded = downloadedTracks.any { it.id == track.id }
        val downloadProgress = downloadProgressMap[track.id]
        val isLiked by viewModel.isTrackLiked(track.id).collectAsState(initial = false)
        
        TrackOptionsBottomSheet(
            track = track,
            onDismiss = { longPressedTrack = null },
            onPlayNext = { viewModel.playNext(track) },
            onAddToQueue = { viewModel.addToQueue(track) },
            onGoToAlbum = { 
                longPressedTrack = null
                if (track.album.isNotBlank() && !track.albumId.isNullOrEmpty()) {
                    navController.navigate("album/${track.albumId}") 
                } else if (track.album.isNotBlank()) {
                    viewModel.performSearch(track.album)
                    navController.navigate("search")
                }
            },
            onGoToArtist = { 
                longPressedTrack = null
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
                longPressedTrack = null
                showPlaylistPicker = track
            },
            onSetCanvasUrl = { url -> 
                if (url == "FETCH_CANVAS") viewModel.enableCanvasForTrack(track)
                else viewModel.setCanvasUrl(track, url)
            }
        )
    }

    longPressedAlbum?.let { album ->
        val savedAlbums by viewModel.allAlbums.collectAsState()
        val isSaved = savedAlbums.any { it.title.equals(album.title, ignoreCase = true) }
        
        AlbumOptionsBottomSheet(
            albumTitle = album.title,
            albumArtist = album.artist,
            albumArtUri = album.artUri,
            isSaved = isSaved,
            onDismiss = { longPressedAlbum = null },
            onPlayAlbum = {
                if (album.tracks.isNotEmpty()) {
                    viewModel.playQueue(album.tracks, startIndex = 0)
                }
            },
            onSaveToggle = {
                if (isSaved) {
                    viewModel.unsaveAlbum(album.tracks)
                } else {
                    viewModel.saveAlbum(album.tracks)
                }
            },
            onGoToArtist = {
                longPressedAlbum = null
                val artistTrack = album.tracks.firstOrNull()
                if (artistTrack != null && !artistTrack.artistId.isNullOrEmpty()) {
                    navController.navigate("artist/${artistTrack.artistId}")
                } else {
                    viewModel.performSearch(album.artist)
                    navController.navigate("search")
                }
            }
        )
    }

    longPressedPlaylist?.let { playlist ->
        val tracksFlow = remember(playlist.id) { viewModel.getTracksInPlaylistFlow(playlist.id) }
        val tracks by tracksFlow.collectAsState(initial = emptyList())
        val isCustom = playlist.name != "Liked Songs" && playlist.name != "Downloads" && !playlist.name.startsWith("Daily")
        
        PlaylistOptionsBottomSheet(
            playlistName = playlist.name,
            playlistCoverUri = playlist.coverArtUri ?: tracks.firstOrNull()?.albumArtUri,
            isCustom = isCustom,
            onDismiss = { longPressedPlaylist = null },
            onPlayPlaylist = {
                if (tracks.isNotEmpty()) {
                    viewModel.playQueue(tracks, startIndex = 0)
                }
            },
            onAddToQueue = {
                tracks.forEach { viewModel.addToQueue(it) }
            },
            onEditPlaylist = {
                longPressedPlaylist = null
                showPlusBottomSheet = false
                showEditPlaylistsDialog = true
            },
            onDeletePlaylist = {
                longPressedPlaylist = null
                viewModel.deletePlaylist(playlist)
            }
        )
    }

    showPlaylistPicker?.let { track ->
        val playlists by viewModel.allPlaylists.collectAsState()
        AddToPlaylistDialog(
            track = track,
            playlists = playlists,
            onDismiss = { showPlaylistPicker = null },
            onPlaylistSelected = { playlist ->
                viewModel.addTrackToPlaylist(playlist.id, track)
                showPlaylistPicker = null
            },
            onCreatePlaylist = { name ->
                viewModel.createPlaylist(name)
            }
        )
    }
}
}

@Composable
fun LibraryGridItem(
    title: String,
    subtitle: String,
    imageUrl: String?,
    customCoverJson: String?,
    isPinned: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animatedCombinedClickable(onLongClick, onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val customCover = PlaylistCustomizer.parseCustomCover(customCoverJson ?: imageUrl)
        
        // Artwork
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
        ) {
            if (customCover != null) {
                val startParsed = PlaylistCustomizer.parseColorSafe(customCover.startColor, Color.Gray)
                val endParsed = PlaylistCustomizer.parseColorSafe(customCover.endColor, Color.Black)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(startParsed, endParsed))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(EcoDotCard),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        
        // Info
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isPinned) {
                    Icon(
                        Icons.Rounded.PushPin,
                        contentDescription = "Pinned",
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = subtitle,
                    style = androidx.compose.ui.text.TextStyle(
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ---- Frosted Glass Pinned Category Card ----
@Composable
private fun PinnedLibraryCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        gradientColors[0].copy(alpha = 0.07f),
                        gradientColors[1].copy(alpha = 0.02f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        gradientColors[0].copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .animatedClickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradientColors[0].copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ---- User playlist card ----
@Composable
private fun PlaylistCard(
    playlist: Playlist,
    viewModel: MusicViewModel,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    onMoveToFolder: (String?) -> Unit
) {
    val trackCountFlow = remember(playlist.id) {
        viewModel.getTracksInPlaylistFlow(playlist.id)
    }
    val trackCount by trackCountFlow.collectAsState(initial = emptyList())
    val tracks = trackCount
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(playlist.name) }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = EcoDotCard,
            title = { Text("Rename Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Name", color = Color.White.copy(alpha = 0.6f)) },
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
                    if (renameText.isNotBlank()) { onRename(renameText.trim()); showRenameDialog = false }
                }) { Text("Save", color = EcoDotRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    var showFolderDialog by remember { mutableStateOf(false) }
    var folderText by remember { mutableStateOf(playlist.folder ?: "") }

    if (showFolderDialog) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            containerColor = EcoDotCard,
            title = { Text("Move to Folder", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = folderText,
                    onValueChange = { folderText = it },
                    singleLine = true,
                    label = { Text("Folder Name (leave empty to remove)", color = Color.White.copy(alpha = 0.6f)) },
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
                    onMoveToFolder(if (folderText.isBlank()) null else folderText.trim())
                    showFolderDialog = false
                }) { Text("Save", color = EcoDotRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showFolderDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animatedClickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cover art or placeholder
        val coverUri = playlist.coverArtUri ?: tracks.firstOrNull()?.albumArtUri
        val customCover = PlaylistCustomizer.parseCustomCover(coverUri)
        if (customCover != null) {
            val startParsed = PlaylistCustomizer.parseColorSafe(customCover.startColor, Color.Gray)
            val endParsed = PlaylistCustomizer.parseColorSafe(customCover.endColor, Color.Black)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(startParsed, endParsed))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.QueueMusic,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(26.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(EcoDotCard),
                contentAlignment = Alignment.Center
            ) {
                if (coverUri != null) {
                    AsyncImage(model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(coverUri)
                            .crossfade(true)
                            .build(), contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Rounded.QueueMusic, contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(26.dp))
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(playlist.name, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold, color = Color.White,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false))
                if (customCover?.isPrivate == true) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Rounded.Lock, contentDescription = "Private", tint = Color.White.copy(alpha = 0.45f), modifier = Modifier.size(13.dp))
                }
            }
            Text("${tracks.size} songs",
                style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = Color.White.copy(0.4f))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false },
                modifier = Modifier.background(EcoDotCard)) {
                DropdownMenuItem(
                    text = { Text("Rename", color = Color.White) },
                    onClick = { showMenu = false; showRenameDialog = true },
                    leadingIcon = { Icon(Icons.Rounded.Edit, null, tint = Color.White) }
                )
                DropdownMenuItem(
                    text = { Text("Move to Folder", color = Color.White) },
                    onClick = { showMenu = false; showFolderDialog = true },
                    leadingIcon = { Icon(Icons.Rounded.Folder, null, tint = Color.White) }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    onClick = { showMenu = false; onDelete() },
                    leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) }
                )
            }
        }
    }
}

@Composable
fun TrackItem(
    track: Track,
    isPlaying: Boolean,
    downloadProgress: Float?,
    isDownloaded: Boolean,
    viewModel: MusicViewModel,
    navController: NavController,
    allPlaylists: List<Playlist> = emptyList(),
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showAddToPlaylist by remember { mutableStateOf(false) }
    val progress = downloadProgress

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
            Text(track.title, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold, maxLines = 1,
                overflow = TextOverflow.Ellipsis, color = if (isPlaying) EcoDotRed else Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                    ExplicitBadge(modifier = Modifier.padding(end = 6.dp))
                }
                Text(track.artist, style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f), maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
        }
        
        // isDownloaded and progress are now passed as parameters
        
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
            track.artistId?.let { id ->
                DropdownMenuItem(
                    text = { Text("View Artist", color = Color.White) },
                    onClick = { navController.navigate("artist/$id"); showMenu = false },
                    leadingIcon = { Icon(Icons.Rounded.Person, "Artist", tint = Color.White) }
                )
            }
        }
    }
}

@Composable
fun AlbumCard(album: com.example.ecodot.ui.viewmodel.Album, onClick: () -> Unit, onLongClick: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.width(140.dp).animatedCombinedClickable(onLongClick, onClick)
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(album.artUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.02f))
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(album.title, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold, color = Color.White,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(2.dp))
        Text(album.artist, style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.45f),
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun GenreChip(genre: com.example.ecodot.ui.viewmodel.Genre, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.03f))
                )
            )
            .border(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .animatedClickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = genre.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun LibraryRecentCard(track: Track, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(104.dp).animatedClickable(onClick = onClick)
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(track.albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(104.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.02f))
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(track.title, style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold, color = Color.White,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(2.dp))
        Text(track.artist, style = androidx.compose.ui.text.TextStyle(
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        ), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun LibraryArtistCard(
    artist: FollowedArtist,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .animatedClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.03f))
                .border(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        ) {
            AsyncImage(
                model = artist.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = artist.artistName,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

