package com.example.ecodot.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.components.*
import com.example.ecodot.ui.theme.*
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.util.PlaylistCustomizer
import com.example.ecodot.util.CustomCover
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playlistFlow = remember(playlist.id) { viewModel.getPlaylistByIdFlow(playlist.id) }
    val playlistState by playlistFlow.collectAsState(initial = playlist)
    val activePlaylist = playlistState ?: playlist

    val tracksFlow = remember(activePlaylist.id) { viewModel.getTracksInPlaylistFlow(activePlaylist.id) }
    val tracks by tracksFlow.collectAsState(initial = emptyList())
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(activePlaylist.name) }
    var editDesc by remember { mutableStateOf(activePlaylist.description) }
    var editCoverUrl by remember { mutableStateOf(activePlaylist.coverArtUri ?: "") }
    var editStartColor by remember { mutableStateOf("#FF1DB954") }
    var editEndColor by remember { mutableStateOf("#FF0D2C1D") }
    var editIsPrivate by remember { mutableStateOf(false) }
    var editTags by remember { mutableStateOf("") }
    var editCoverType by remember { mutableStateOf("Gradient") }

    var isEditMode by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf<Track?>(null) }

    var localTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(tracks) {
        if (draggingIndex == null) {
            localTracks = tracks
        }
    }

    // Launcher for selecting an image from the gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = copyUriToInternalStorage(context, uri)
            if (localPath != null) {
                editCoverUrl = localPath
            }
        }
    }

    // Keep state values in sync when dialog opens
    LaunchedEffect(showEditDialog) {
        if (showEditDialog) {
            editName = activePlaylist.name
            editDesc = activePlaylist.description
            val custom = PlaylistCustomizer.parseCustomCover(activePlaylist.coverArtUri)
            if (custom != null) {
                editCoverType = "Gradient"
                editStartColor = custom.startColor
                editEndColor = custom.endColor
                editIsPrivate = custom.isPrivate
                editTags = custom.tags.joinToString(", ")
                editCoverUrl = ""
            } else {
                editCoverType = if (activePlaylist.coverArtUri.isNullOrEmpty()) "Gradient" else "Image"
                editCoverUrl = activePlaylist.coverArtUri ?: ""
                editStartColor = "#FF1DB954"
                editEndColor = "#FF0D2C1D"
                editIsPrivate = false
                editTags = ""
            }
        }
    }

    // Edit Details dialog
    if (showEditDialog) {
        val colorOptions = listOf(
            Pair("#FF1DB954", "#FF0D2C1D"), // Spotify Green
            Pair("#FFF857A6", "#FFFF5858"), // Sunset Pink
            Pair("#FF4FACFE", "#FF00F2FE"), // Neon Blue
            Pair("#FFFF416C", "#FFFF4B2B"), // Red Orange
            Pair("#FF7F00FF", "#FFE100FF"), // Midnight Violet
            Pair("#FF3A7BD5", "#FF3A6073")  // Slate Blue
        )

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = EcoDotCard,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Edit Playlist Details",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        singleLine = true,
                        label = { Text("Playlist Name", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EcoDotRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        singleLine = false,
                        maxLines = 3,
                        label = { Text("Description", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EcoDotRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Cover Type selector (Gradient vs Image)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = editCoverType == "Gradient",
                            onClick = { editCoverType = "Gradient" },
                            label = { Text("Gradient Theme", color = if (editCoverType == "Gradient") Color.Black else Color.White) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.White)
                        )
                        FilterChip(
                            selected = editCoverType == "Image",
                            onClick = { editCoverType = "Image" },
                            label = { Text("Image Cover", color = if (editCoverType == "Image") Color.Black else Color.White) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.White)
                        )
                    }

                    if (editCoverType == "Gradient") {
                        // Predefined gradient theme row
                        Text("Theme Color", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(colorOptions) { (startColor, endColor) ->
                                val startParsed = PlaylistCustomizer.parseColorSafe(startColor, Color.Gray)
                                val endParsed = PlaylistCustomizer.parseColorSafe(endColor, Color.Black)
                                val isSelected = editStartColor == startColor && editEndColor == endColor
                                
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
                                            editStartColor = startColor
                                            editEndColor = endColor
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
                                checked = editIsPrivate,
                                onCheckedChange = { editIsPrivate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EcoDotRed,
                                    uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }

                        // Tags text input
                        OutlinedTextField(
                            value = editTags,
                            onValueChange = { editTags = it },
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
                    } else {
                        // Image Cover Inputs
                        OutlinedTextField(
                            value = editCoverUrl,
                            onValueChange = { editCoverUrl = it },
                            singleLine = true,
                            label = { Text("Cover Image URL / Path", color = Color.White.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = EcoDotRed,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            placeholder = { Text("https://example.com/image.jpg", color = Color.White.copy(alpha = 0.3f)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Choose from Gallery", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        val songArts = tracks.mapNotNull { it.albumArtUri }.distinct()
                        if (songArts.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Or tap a song cover to use it:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(songArts) { artUrl ->
                                        AsyncImage(
                                            model = artUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(EcoDotCard)
                                                .animatedClickable {
                                                    editCoverUrl = artUrl
                                                },
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotBlank()) {
                        val finalCover = if (editCoverType == "Gradient") {
                            val tagsList = editTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val customCoverObj = CustomCover(
                                startColor = editStartColor,
                                endColor = editEndColor,
                                isPrivate = editIsPrivate,
                                tags = tagsList
                            )
                            PlaylistCustomizer.serializeCustomCover(customCoverObj)
                        } else {
                            if (editCoverUrl.isBlank()) null else editCoverUrl.trim()
                        }

                        viewModel.updatePlaylistDetails(
                            playlist = activePlaylist,
                            name = editName.trim(),
                            description = editDesc.trim(),
                            coverArtUri = finalCover
                        )
                        showEditDialog = false
                    }
                }) { Text("Save", color = EcoDotRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val itemHeightPx = remember(density) { with(density) { 72.dp.toPx() } }

    Scaffold(
        modifier = modifier,
        containerColor = EcoDotBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    val artUris = tracks.mapNotNull { it.albumArtUri }.take(4)
                    val coverArt = activePlaylist.coverArtUri ?: artUris.firstOrNull()
                    val customCover = PlaylistCustomizer.parseCustomCover(coverArt)
                    
                    if (customCover != null) {
                        val startParsed = PlaylistCustomizer.parseColorSafe(customCover.startColor, Color.Gray)
                        val endParsed = PlaylistCustomizer.parseColorSafe(customCover.endColor, Color.Black)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(startParsed, endParsed))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.QueueMusic,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    } else if (coverArt.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(EcoDotCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.QueueMusic,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    } else {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(coverArt)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (customCover == null) {
                        // Gradient overlay for regular images
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(0.2f), EcoDotBlack)
                                )
                            )
                        )
                    }

                    // Back button + actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Row {
                            IconButton(onClick = { isEditMode = !isEditMode }) {
                                Icon(
                                    if (isEditMode) Icons.Rounded.Check else Icons.Rounded.SwapVert,
                                    contentDescription = "Reorder tracks",
                                    tint = if (isEditMode) EcoDotRed else Color.White
                                )
                            }
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit Playlist", tint = Color.White)
                            }
                        }
                    }

                    // Playlist title at bottom of header
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activePlaylist.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (customCover?.isPrivate == true) {
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    Icons.Rounded.Lock,
                                    contentDescription = "Private",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (activePlaylist.description.isNotEmpty()) {
                            Text(
                                text = activePlaylist.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        if (customCover != null && customCover.tags.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                customCover.tags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = "${tracks.size} songs",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Play all button
            item {
                AnimatedVisibility(visible = tracks.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.playPlaylist(activePlaylist) },
                            colors = ButtonDefaults.buttonColors(containerColor = EcoDotRed),
                            shape = RoundedCornerShape(percent = 50),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Play All", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.shufflePlaylist(activePlaylist) },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.3f)),
                            shape = RoundedCornerShape(percent = 50),
                            modifier = Modifier
                                .height(52.dp)
                                .widthIn(min = 52.dp)
                        ) {
                            Icon(Icons.Rounded.Shuffle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Track list
            if (tracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.MusicNote, null, tint = Color.White.copy(0.15f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No songs yet", color = Color.White.copy(0.4f), style = MaterialTheme.typography.bodyLarge)
                            Text("Add songs from your Library", color = Color.White.copy(0.3f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                itemsIndexed(items = localTracks, key = { _, t -> t.id }) { index, track ->
                    val isDragging = index == draggingIndex
                    PlaylistTrackItem(
                        track = track,
                        index = index + 1,
                        isPlaying = currentTrack?.id == track.id,
                        isDownloaded = downloadedTracks.any { it.id == track.id },
                        downloadProgress = downloadProgressMap[track.id],
                        viewModel = viewModel,
                        isEditMode = isEditMode,
                        isFirst = index == 0,
                        isLast = index == localTracks.size - 1,
                        onPlay = { viewModel.playTracks(localTracks, index) },
                        onRemove = { viewModel.removeTrackFromPlaylist(activePlaylist.id, track.id) },
                        onAddToPlaylist = { showPlaylistPicker = track },
                        onDragStart = {
                            draggingIndex = index
                            dragOffset = 0f
                        },
                        onDrag = { deltaY ->
                            dragOffset += deltaY
                            val fromIndex = draggingIndex
                            if (fromIndex != null) {
                                if (dragOffset > itemHeightPx / 2f && fromIndex < localTracks.size - 1) {
                                    val targetIndex = fromIndex + 1
                                    val list = localTracks.toMutableList()
                                    java.util.Collections.swap(list, fromIndex, targetIndex)
                                    localTracks = list
                                    draggingIndex = targetIndex
                                    dragOffset -= itemHeightPx
                                } else if (dragOffset < -itemHeightPx / 2f && fromIndex > 0) {
                                    val targetIndex = fromIndex - 1
                                    val list = localTracks.toMutableList()
                                    java.util.Collections.swap(list, fromIndex, targetIndex)
                                    localTracks = list
                                    draggingIndex = targetIndex
                                    dragOffset += itemHeightPx
                                }
                            }
                        },
                        onDragEnd = {
                            val finalIndex = draggingIndex
                            if (finalIndex != null) {
                                viewModel.savePlaylistTrackOrder(activePlaylist.id, localTracks.map { it.id })
                            }
                            draggingIndex = null
                            dragOffset = 0f
                        },
                        modifier = Modifier
                            .graphicsLayer {
                                if (isDragging) {
                                    translationY = dragOffset
                                    shadowElevation = 8.dp.toPx()
                                    scaleX = 1.04f
                                    scaleY = 1.04f
                                }
                            }
                            .zIndex(if (isDragging) 10f else 1f)
                            .then(if (isDragging) Modifier else Modifier.animateItem())
                    )
                }
            }
        }
    }

    showPlaylistPicker?.let { track ->
        val playlists by viewModel.allPlaylists.collectAsState()
        AddToPlaylistDialog(
            track = track,
            playlists = playlists,
            onDismiss = { showPlaylistPicker = null },
            onPlaylistSelected = { selectedPlaylist ->
                viewModel.addTrackToPlaylist(selectedPlaylist.id, track)
                showPlaylistPicker = null
            },
            onCreatePlaylist = { name ->
                viewModel.createPlaylist(name)
            }
        )
    }
}

@Composable
private fun PlaylistTrackItem(
    track: Track,
    index: Int,
    isPlaying: Boolean = false,
    isDownloaded: Boolean = false,
    downloadProgress: Float? = null,
    viewModel: MusicViewModel,
    isEditMode: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDragStart: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .animatedClickable(onClick = onPlay)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Index number or Playing Indicator
        if (isPlaying) {
            Box(modifier = Modifier.width(28.dp), contentAlignment = Alignment.CenterStart) {
                com.example.ecodot.ui.components.PlayingIndicator()
            }
        } else {
            Text(
                text = "$index",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.width(28.dp)
            )
        }

        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(track.albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(EcoDotCard),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isPlaying) EcoDotRed else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                    ExplicitBadge(modifier = Modifier.padding(end = 6.dp))
                }
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        if (isEditMode) {
            Icon(
                imageVector = Icons.Rounded.DragIndicator,
                contentDescription = "Drag to reorder",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(28.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onDragStart() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() }
                        )
                    }
            )
        } else {
            if (downloadProgress != null) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                    CircularProgressIndicator(progress = { downloadProgress }, modifier = Modifier.size(20.dp), color = EcoDotRed, strokeWidth = 2.dp, trackColor = Color.White.copy(0.1f))
                    Text("${(downloadProgress * 100).toInt()}%", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else if (isDownloaded) {
                Icon(Icons.Rounded.DownloadDone, contentDescription = "Downloaded", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
            }

            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = Color.White.copy(0.4f))
            }

            val isLikedFlow = remember(track.id) { viewModel.isTrackLiked(track.id) }
            val isLiked by isLikedFlow.collectAsState(initial = false)
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(EcoDotCard)
            ) {
                DropdownMenuItem(
                    text = { Text("Play", color = Color.White) },
                    onClick = { onPlay(); showMenu = false },
                    leadingIcon = { Icon(Icons.Rounded.PlayArrow, null, tint = Color.White) }
                )
                DropdownMenuItem(
                    text = { Text("Play Next", color = Color.White) },
                    onClick = { viewModel.playNext(track); showMenu = false },
                    leadingIcon = { Icon(Icons.Rounded.QueuePlayNext, null, tint = Color.White) }
                )
                DropdownMenuItem(
                    text = { Text("Add to Queue", color = Color.White) },
                    onClick = { viewModel.addToQueue(track); showMenu = false },
                    leadingIcon = { Icon(Icons.Rounded.QueueMusic, null, tint = Color.White) }
                )
                DropdownMenuItem(
                    text = { Text(if (isLiked) "Remove from Liked Songs" else "Like", color = Color.White) },
                    onClick = { viewModel.toggleTrackLike(track); showMenu = false },
                    leadingIcon = { Icon(if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = EcoDotRed) }
                )
                DropdownMenuItem(
                    text = { Text("Add to playlist", color = Color.White) },
                    onClick = { onAddToPlaylist(); showMenu = false },
                    leadingIcon = { Icon(Icons.Rounded.PlaylistAdd, null, tint = Color.White) }
                )
                DropdownMenuItem(
                    text = { Text("Remove from playlist", color = MaterialTheme.colorScheme.error) },
                    onClick = { onRemove(); showMenu = false },
                    leadingIcon = { Icon(Icons.Rounded.RemoveCircleOutline, null, tint = MaterialTheme.colorScheme.error) }
                )
            }
        }
    }
}

// Helper to copy selected Gallery photo URIs to app private storage
private fun copyUriToInternalStorage(context: android.content.Context, uri: android.net.Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "playlist_cover_${System.currentTimeMillis()}.jpg"
        val file = java.io.File(context.filesDir, fileName)
        val outputStream = java.io.FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
