package com.example.ecodot.ui.screens

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.components.*
import com.example.ecodot.ui.components.player.PlayerStyleBottomSheet
import com.example.ecodot.ui.theme.*
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.util.CustomCover
import com.example.ecodot.util.PlaylistCustomizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onGoToAlbum: ((Track) -> Unit)? = null,
    onGoToArtist: ((Track) -> Unit)? = null,
    onEqualizerClick: (() -> Unit)? = null,
    onWatchVideo: ((Track) -> Unit)? = null
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
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isVideoMode by viewModel.isVideoMode.collectAsState()
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsState()
    val playerStyle by viewModel.playerStyle.collectAsState()
    val isCanvasEnabledByUser by viewModel.isCanvasEnabledByUser.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()

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
    var optionsMenuTrack by remember { mutableStateOf<Track?>(null) }
    var ringtoneClipperTrack by remember { mutableStateOf<Track?>(null) }
    var showPlayerStyleSheet by remember { mutableStateOf(false) }
    var showPlaylistOptionsSheet by remember { mutableStateOf(false) }
    var showDeletePlaylistConfirm by remember { mutableStateOf(false) }

    var localTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(tracks) {
        if (draggingIndex == null) {
            localTracks = tracks
        }
    }

    // Dismiss active overlays/dialogs or reorder mode on system back press before closing the playlist
    BackHandler(
        enabled = isEditMode || showEditDialog || showPlaylistPicker != null ||
            optionsMenuTrack != null || ringtoneClipperTrack != null ||
            showPlayerStyleSheet || showPlaylistOptionsSheet || showDeletePlaylistConfirm
    ) {
        when {
            showDeletePlaylistConfirm -> showDeletePlaylistConfirm = false
            ringtoneClipperTrack != null -> ringtoneClipperTrack = null
            showPlayerStyleSheet -> showPlayerStyleSheet = false
            optionsMenuTrack != null -> optionsMenuTrack = null
            showPlaylistPicker != null -> showPlaylistPicker = null
            showPlaylistOptionsSheet -> showPlaylistOptionsSheet = false
            showEditDialog -> showEditDialog = false
            isEditMode -> isEditMode = false
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

    val density = LocalDensity.current
    val itemHeightPx = remember(density) { with(density) { 68.dp.toPx() } }
    val listState = rememberLazyListState()

    val isLikedSongs = activePlaylist.name.equals("Liked Songs", ignoreCase = true)
    val isDownloadsPlaylist = activePlaylist.name.equals("Downloads", ignoreCase = true)
    val isSystemPlaylist = isLikedSongs || isDownloadsPlaylist
    val customCover = remember(activePlaylist.coverArtUri) {
        PlaylistCustomizer.parseCustomCover(activePlaylist.coverArtUri)
    }
    // Use Spotify's signature purple heart cover for Liked Songs unless user picked a local/custom cover
    val useLikedSongsSignatureArt = isLikedSongs && customCover == null &&
        (activePlaylist.coverArtUri.isNullOrEmpty() || activePlaylist.coverArtUri.startsWith("http"))

    val paletteImageSource = remember(activePlaylist.coverArtUri, localTracks, useLikedSongsSignatureArt) {
        when {
            customCover != null -> null
            useLikedSongsSignatureArt -> null
            !activePlaylist.coverArtUri.isNullOrEmpty() -> activePlaylist.coverArtUri
            else -> localTracks.firstOrNull { !it.albumArtUri.isNullOrBlank() }?.albumArtUri
        }
    }

    val defaultAccentColor = remember(activePlaylist.name, customCover) {
        when {
            customCover != null -> PlaylistCustomizer.parseColorSafe(customCover.startColor, Color(0xFF282E3D))
            isLikedSongs -> Color(0xFF4B2CE0)
            isDownloadsPlaylist -> Color(0xFF145C46)
            else -> Color(0xFF272D3C)
        }
    }

    var extractedHeaderColor by remember(activePlaylist.id) { mutableStateOf(defaultAccentColor) }

    LaunchedEffect(paletteImageSource, defaultAccentColor) {
        if (paletteImageSource == null) {
            extractedHeaderColor = defaultAccentColor
            return@LaunchedEffect
        }
        val colorFromArt = withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(context)
                    .data(paletteImageSource)
                    .size(128, 128)
                    .allowHardware(false)
                    .build()
                val result = context.imageLoader.execute(request) as? SuccessResult
                val bitmap = (result?.drawable as? BitmapDrawable)?.bitmap ?: return@runCatching null
                val palette = Palette.from(bitmap).generate()
                val rgb = palette.darkVibrantSwatch?.rgb
                    ?: palette.vibrantSwatch?.rgb
                    ?: palette.dominantSwatch?.rgb
                    ?: palette.mutedSwatch?.rgb
                rgb?.let { Color(it) }
            }.getOrNull()
        }
        extractedHeaderColor = colorFromArt ?: defaultAccentColor
    }

    val headerAccentColor by animateColorAsState(
        targetValue = extractedHeaderColor,
        animationSpec = tween(durationMillis = 450),
        label = "playlist_header_accent"
    )

    val topBarProgress by remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 1f
                else -> (listState.firstVisibleItemScrollOffset / 520f).coerceIn(0f, 1f)
            }
        }
    }

    val spotifyGreen = Color(0xFF1ED760)
    val baseBackground = Color(0xFF0A0B0F)
    val downloadableTracks = remember(localTracks) { localTracks.filter { it.isYouTube } }
    val downloadedIdSet = remember(downloadedTracks) { downloadedTracks.map { it.id }.toSet() }
    val allDownloaded = downloadableTracks.isNotEmpty() && downloadableTracks.all { it.id in downloadedIdSet }
    val anyDownloading = localTracks.any { it.id in downloadProgressMap }
    val totalDurationMs = remember(localTracks) { localTracks.sumOf { it.duration.coerceAtLeast(0L) } }
    val isThisPlaylistActive = currentTrack != null && localTracks.any { it.id == currentTrack?.id }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBackground)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // Spotify-style gradient hero header + centered artwork + left metadata
            item(key = "spotify_playlist_header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to headerAccentColor.copy(alpha = 0.78f),
                                    0.52f to headerAccentColor.copy(alpha = 0.34f),
                                    0.86f to headerAccentColor.copy(alpha = 0.10f),
                                    1.0f to baseBackground
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(top = 56.dp, bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Centered square playlist artwork
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val coverShape = RoundedCornerShape(8.dp)
                            val coverModifier = Modifier
                                .size(212.dp)
                                .shadow(elevation = 24.dp, shape = coverShape, clip = false)
                                .clip(coverShape)

                            when {
                                useLikedSongsSignatureArt -> {
                                    Box(
                                        modifier = coverModifier.background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF450AF5),
                                                    Color(0xFF7B6DF6),
                                                    Color(0xFFC4EFD9)
                                                )
                                            )
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Favorite,
                                            contentDescription = "Liked Songs",
                                            tint = Color.White,
                                            modifier = Modifier.size(78.dp)
                                        )
                                    }
                                }
                                isDownloadsPlaylist && activePlaylist.coverArtUri.isNullOrEmpty() && localTracks.isEmpty() -> {
                                    Box(
                                        modifier = coverModifier.background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF0F7A5A),
                                                    Color(0xFF0B3A2C)
                                                )
                                            )
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DownloadDone,
                                            contentDescription = "Downloads",
                                            tint = Color.White,
                                            modifier = Modifier.size(70.dp)
                                        )
                                    }
                                }
                                else -> {
                                    PlaylistMosaicThumbnail(
                                        playlist = activePlaylist,
                                        tracks = localTracks,
                                        modifier = coverModifier,
                                        shape = coverShape,
                                        iconSize = 68.dp
                                    )
                                }
                            }
                        }

                        // Title, description, creator & duration metadata
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = activePlaylist.name,
                                    color = Color.White,
                                    fontSize = 25.sp,
                                    lineHeight = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.4).sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (customCover?.isPrivate == true) {
                                    Icon(
                                        Icons.Rounded.Lock,
                                        contentDescription = "Private",
                                        tint = Color.White.copy(alpha = 0.65f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (activePlaylist.description.isNotBlank()) {
                                Text(
                                    text = activePlaylist.description,
                                    color = Color.White.copy(alpha = 0.72f),
                                    fontSize = 13.5.sp,
                                    lineHeight = 18.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (customCover != null && customCover.tags.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    items(customCover.tags) { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.White.copy(alpha = 0.14f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.88f),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            // Spotify creator line + count + total runtime
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(spotifyGreen, Color(0xFF0E7A38))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                val countLabel = "${localTracks.size} ${if (localTracks.size == 1) "song" else "songs"}"
                                val durationSuffix = formatPlaylistDuration(totalDurationMs)?.let { " · $it" } ?: ""
                                Text(
                                    text = "EcoDot",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = " · $countLabel$durationSuffix",
                                    color = Color.White.copy(alpha = 0.68f),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Spotify Action Controls Row: Left icons + Right Shuffle & Green Play FAB
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (downloadableTracks.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        downloadableTracks
                                            .filter { it.id !in downloadedIdSet && it.id !in downloadProgressMap }
                                            .forEach { viewModel.downloadTrack(it) }
                                    }
                                ) {
                                    when {
                                        anyDownloading -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(22.dp),
                                                color = spotifyGreen,
                                                strokeWidth = 2.dp,
                                                trackColor = Color.White.copy(alpha = 0.16f)
                                            )
                                        }
                                        allDownloaded -> {
                                            Icon(
                                                imageVector = Icons.Rounded.DownloadForOffline,
                                                contentDescription = "All songs downloaded",
                                                tint = spotifyGreen,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                imageVector = Icons.Rounded.DownloadForOffline,
                                                contentDescription = "Download playlist",
                                                tint = Color.White.copy(alpha = 0.72f),
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (localTracks.size > 1) {
                                IconButton(onClick = { isEditMode = !isEditMode }) {
                                    Icon(
                                        imageVector = if (isEditMode) Icons.Rounded.Check else Icons.Rounded.SwapVert,
                                        contentDescription = if (isEditMode) "Done reordering" else "Reorder tracks",
                                        tint = if (isEditMode) spotifyGreen else Color.White.copy(alpha = 0.72f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            IconButton(onClick = { showPlaylistOptionsSheet = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Playlist options",
                                    tint = Color.White.copy(alpha = 0.72f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            if (localTracks.isNotEmpty()) {
                                IconButton(onClick = { viewModel.shufflePlaylist(activePlaylist) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shuffle,
                                        contentDescription = "Shuffle",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Surface(
                                    onClick = {
                                        if (isThisPlaylistActive && isPlaying) {
                                            viewModel.togglePlayback()
                                        } else {
                                            viewModel.playPlaylist(activePlaylist)
                                        }
                                    },
                                    shape = CircleShape,
                                    color = spotifyGreen,
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            imageVector = if (isThisPlaylistActive && isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                            contentDescription = if (isThisPlaylistActive && isPlaying) "Pause" else "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isEditMode && localTracks.size > 1) {
                item(key = "reorder_hint") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Drag handles to reorder tracks",
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Done",
                            color = spotifyGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { isEditMode = false }
                        )
                    }
                }
            }

            // Track list
            if (localTracks.isEmpty()) {
                item(key = "empty_playlist") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.16f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "This playlist is empty",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Add songs from Search, Home, or any player menu.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(items = localTracks, key = { _, t -> t.id }) { index, track ->
                    val isDragging = index == draggingIndex
                    PlaylistTrackItem(
                        track = track,
                        isPlaying = currentTrack?.id == track.id,
                        isDownloaded = track.id in downloadedIdSet,
                        downloadProgress = downloadProgressMap[track.id],
                        isEditMode = isEditMode,
                        onPlay = { viewModel.playTracks(localTracks, index) },
                        onAddToQueue = { viewModel.addToQueue(track) },
                        onMoreClick = { optionsMenuTrack = track },
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
                                    scaleX = 1.03f
                                    scaleY = 1.03f
                                }
                            }
                            .zIndex(if (isDragging) 10f else 1f)
                            .then(if (isDragging) Modifier else Modifier.animateItem())
                    )
                }
            }
        }

        // Floating sticky Spotify header bar with scroll-linked background & title fade
        val barTitleAlpha = ((topBarProgress - 0.55f) / 0.45f).coerceIn(0f, 1f)
        val barBgColor = Color(
            red = (headerAccentColor.red * 0.35f + baseBackground.red * 0.65f),
            green = (headerAccentColor.green * 0.35f + baseBackground.green * 0.65f),
            blue = (headerAccentColor.blue * 0.35f + baseBackground.blue * 0.65f),
            alpha = (topBarProgress * 0.96f).coerceIn(0f, 0.96f)
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(barBgColor)
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (isEditMode) isEditMode = false else onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = activePlaylist.name,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
                    .graphicsLayer { alpha = barTitleAlpha }
            )
            if (isEditMode) {
                TextButton(onClick = { isEditMode = false }) {
                    Text("Done", color = spotifyGreen, fontWeight = FontWeight.Bold)
                }
            } else {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit playlist",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }

    // Full scrollable TrackOptionsBottomSheet for every track 3-dots tap
    optionsMenuTrack?.let { track ->
        val trackIndex = remember(localTracks, track.id) { localTracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0) }
        val isDownloaded = track.id in downloadedIdSet
        val downloadProgress = downloadProgressMap[track.id]
        val isLiked by viewModel.isTrackLiked(track.id).collectAsState(initial = false)
        val queueState by viewModel.playbackQueue.collectAsState()
        val isInQueue = remember(queueState, track.id) { queueState.any { it.id == track.id } }
        val isActiveTrack = currentTrack?.id == track.id
        val trackInVideoMode = isActiveTrack && (isVideoMode || viewModel.isTrackVideo(track))

        TrackOptionsBottomSheet(
            track = track,
            onDismiss = { optionsMenuTrack = null },
            onPlayNow = { viewModel.playTracks(localTracks, trackIndex) },
            onPlayNext = { viewModel.playNext(track) },
            onAddToQueue = { viewModel.addToQueue(track) },
            onRemoveFromQueue = if (isInQueue) { { viewModel.removeTrackFromQueue(track) } } else null,
            onGoToAlbum = {
                optionsMenuTrack = null
                onGoToAlbum?.invoke(track) ?: run {
                    if (track.album.isNotBlank()) {
                        viewModel.performSearch(track.album)
                    }
                }
            },
            onGoToArtist = {
                optionsMenuTrack = null
                onGoToArtist?.invoke(track) ?: run {
                    if (track.artist.isNotBlank()) {
                        viewModel.performSearch(track.artist)
                    }
                }
            },
            isDownloaded = isDownloaded,
            downloadProgress = downloadProgress,
            onDownload = { viewModel.downloadTrack(track) },
            onDeleteDownload = { viewModel.deleteDownload(track) },
            isLiked = isLiked,
            onLikeToggle = { viewModel.toggleTrackLike(track) },
            onAddToPlaylist = {
                optionsMenuTrack = null
                showPlaylistPicker = track
            },
            onRemoveFromPlaylist = if (!isDownloadsPlaylist) {
                {
                    if (isLikedSongs) {
                        viewModel.toggleTrackLike(track)
                    } else {
                        viewModel.removeTrackFromPlaylist(activePlaylist.id, track.id)
                    }
                }
            } else null,
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
                    onWatchVideo?.invoke(track)
                }
            },
            onSwitchPlayerStyle = { showPlayerStyleSheet = true },
            onEqualizerClick = onEqualizerClick,
            isCanvasActive = isCanvasEnabledByUser,
            onSetCanvasUrl = { url ->
                if (url == "DISABLED") viewModel.disableCanvas()
                else viewModel.enableCanvasForTrack(track)
            },
            onSetAsRingtone = { ringtoneClipperTrack = track }
        )
    }

    // Playlist Options Bottom Sheet
    if (showPlaylistOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlaylistOptionsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF14151B),
            contentColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.24f)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = activePlaylist.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 6.dp))

                PlaylistSheetActionItem(
                    icon = Icons.Rounded.QueuePlayNext,
                    label = "Play Next"
                ) {
                    viewModel.playPlaylistNext(activePlaylist)
                    showPlaylistOptionsSheet = false
                }
                PlaylistSheetActionItem(
                    icon = Icons.AutoMirrored.Rounded.QueueMusic,
                    label = "Add Playlist to Queue"
                ) {
                    viewModel.addPlaylistToQueue(activePlaylist)
                    showPlaylistOptionsSheet = false
                }
                if (localTracks.size > 1) {
                    PlaylistSheetActionItem(
                        icon = Icons.Rounded.SwapVert,
                        label = if (isEditMode) "Finish Reordering" else "Reorder Tracks"
                    ) {
                        isEditMode = !isEditMode
                        showPlaylistOptionsSheet = false
                    }
                }
                PlaylistSheetActionItem(
                    icon = Icons.Rounded.Edit,
                    label = "Edit Playlist Details"
                ) {
                    showPlaylistOptionsSheet = false
                    showEditDialog = true
                }
                if (!isSystemPlaylist) {
                    PlaylistSheetActionItem(
                        icon = Icons.Rounded.DeleteOutline,
                        label = "Delete Playlist",
                        tint = Color(0xFFFF6B6B)
                    ) {
                        showPlaylistOptionsSheet = false
                        showDeletePlaylistConfirm = true
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (showDeletePlaylistConfirm) {
        AlertDialog(
            onDismissRequest = { showDeletePlaylistConfirm = false },
            containerColor = Color(0xFF1E1F26),
            title = { Text("Delete playlist?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${activePlaylist.name}\"?",
                    color = Color.White.copy(alpha = 0.72f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeletePlaylistConfirm = false
                        viewModel.deletePlaylist(activePlaylist)
                        onBack()
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePlaylistConfirm = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }

    showPlaylistPicker?.let { track ->
        AddToPlaylistDialog(
            track = track,
            playlists = allPlaylists,
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

    ringtoneClipperTrack?.let { track ->
        RingtoneClipperBottomSheet(
            track = track,
            viewModel = viewModel,
            onDismiss = { ringtoneClipperTrack = null }
        )
    }

    if (showPlayerStyleSheet) {
        PlayerStyleBottomSheet(
            currentStyle = playerStyle,
            accentColor = headerAccentColor,
            onStyleSelected = { viewModel.setPlayerStyle(it) },
            onDismiss = { showPlayerStyleSheet = false }
        )
    }
}

@Composable
private fun PlaylistSheetActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(text = label, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

private fun formatPlaylistDuration(totalMs: Long): String? {
    if (totalMs <= 0L) return null
    val totalMinutes = (totalMs / 60_000L).toInt().coerceAtLeast(1)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes} min"
}

@Composable
private fun PlaylistTrackItem(
    track: Track,
    isPlaying: Boolean = false,
    isDownloaded: Boolean = false,
    downloadProgress: Float? = null,
    isEditMode: Boolean = false,
    onPlay: () -> Unit,
    onAddToQueue: () -> Unit,
    onMoreClick: () -> Unit,
    onDragStart: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val spotifyGreen = Color(0xFF1ED760)

    SwipeToQueueBox(
        onAddToQueue = onAddToQueue,
        enabled = !isEditMode
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .animatedClickable(onClick = onPlay)
                .padding(start = 18.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
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
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1C1D24)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPlaying) {
                        PlayingIndicator(color = spotifyGreen)
                    }
                    Text(
                        text = track.title,
                        color = if (isPlaying) spotifyGreen else Color.White,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (downloadProgress != null) {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(13.dp),
                            color = spotifyGreen,
                            strokeWidth = 1.8.dp,
                            trackColor = Color.White.copy(alpha = 0.14f)
                        )
                    } else if (isDownloaded) {
                        Icon(
                            imageVector = Icons.Rounded.DownloadForOffline,
                            contentDescription = "Downloaded",
                            tint = spotifyGreen,
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .size(14.dp)
                        )
                    }
                    if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                        ExplicitBadge(modifier = Modifier.padding(end = 5.dp))
                    }
                    Text(
                        text = track.artist,
                        color = Color.White.copy(alpha = 0.62f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isEditMode) {
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = "Drag to reorder",
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(26.dp)
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
                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Track options",
                        tint = Color.White.copy(alpha = 0.65f)
                    )
                }
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
