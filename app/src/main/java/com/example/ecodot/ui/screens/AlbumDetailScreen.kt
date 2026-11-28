package com.example.ecodot.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.components.AddToPlaylistDialog
import com.example.ecodot.ui.components.LiquidGlassCard
import com.example.ecodot.ui.components.LocalLiquidGlassEnabled
import com.example.ecodot.ui.components.PlayingIndicator
import com.example.ecodot.ui.components.SwipeToQueueBox
import com.example.ecodot.ui.components.TrackOptionsBottomSheet
import com.example.ecodot.ui.components.liquidGlass
import com.example.ecodot.ui.theme.Add
import com.example.ecodot.ui.theme.AddToQueue
import com.example.ecodot.ui.theme.Album
import com.example.ecodot.ui.theme.ArrowBack
import com.example.ecodot.ui.theme.Bookmark
import com.example.ecodot.ui.theme.BookmarkBorder
import com.example.ecodot.ui.theme.DeleteOutline
import com.example.ecodot.ui.theme.DownloadDone
import com.example.ecodot.ui.theme.DownloadForOffline
import com.example.ecodot.ui.theme.EcoDotBlack
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.MoreVert
import com.example.ecodot.ui.theme.PlayArrow
import com.example.ecodot.ui.theme.PlaylistAdd
import com.example.ecodot.ui.theme.QueuePlayNext
import com.example.ecodot.ui.theme.Share
import com.example.ecodot.ui.theme.Shuffle
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.viewmodel.AlbumViewModel
import com.example.ecodot.ui.viewmodel.MusicViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

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
    val savedAlbums by musicViewModel.allAlbums.collectAsState()
    val currentTrack by musicViewModel.currentTrack.collectAsState()
    val allPlaylists by musicViewModel.allPlaylists.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val accentColor by musicViewModel.accentColor.collectAsState()
    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val context = LocalContext.current

    val listState = rememberLazyListState()
    val albumHazeState = remember { HazeState() }

    var optionsMenuTrack by remember { mutableStateOf<Track?>(null) }
    var showPlaylistPickerTrack by remember { mutableStateOf<Track?>(null) }
    var showAlbumMoreSheet by remember { mutableStateOf(false) }
    var showAddAllPlaylistDialog by remember { mutableStateOf(false) }
    var newAllPlaylistName by remember { mutableStateOf("") }

    LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }

    val downloadedIds = remember(downloadedTracks) { downloadedTracks.map { it.id }.toSet() }
    val allTracksDownloaded = remember(uiState.tracks, downloadedIds) {
        uiState.tracks.isNotEmpty() && uiState.tracks.all { it.id in downloadedIds || musicViewModel.isDownloaded(it.id) }
    }
    val activeDownloadProgress = remember(uiState.tracks, downloadProgressMap) {
        val active = uiState.tracks.mapNotNull { downloadProgressMap[it.id] }
        if (active.isEmpty()) null else active.average().toFloat()
    }
    val downloadedCount = remember(uiState.tracks, downloadedIds) {
        uiState.tracks.count { it.id in downloadedIds || musicViewModel.isDownloaded(it.id) }
    }
    val totalDurationFormatted = remember(uiState.tracks) {
        formatTotalAlbumDuration(uiState.tracks)
    }
    val isAlbumSaved = remember(uiState.title, savedAlbums) {
        savedAlbums.any { it.title.equals(uiState.title, ignoreCase = true) }
    }

    val shareAlbum: () -> Unit = {
        val shareUrl = if (albumId.startsWith("MPREb") || albumId.startsWith("OLAK5") || albumId.startsWith("VL") || albumId.startsWith("PL")) {
            "https://music.youtube.com/browse/$albumId"
        } else {
            uiState.title
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${uiState.title} by ${uiState.artistName}")
            putExtra(Intent.EXTRA_TEXT, "Listen to ${uiState.title} by ${uiState.artistName} on EcoDot\n$shareUrl")
        }
        runCatching { context.startActivity(Intent.createChooser(intent, "Share Album")) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoDotBlack)
    ) {
        // Blurred Ambient Backdrop Glow
        if (!uiState.thumbnailUrl.isNullOrEmpty()) {
            AsyncImage(
                model = uiState.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(470.dp)
                    .blur(86.dp)
                    .alpha(0.45f),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(490.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.14f),
                                EcoDotBlack.copy(alpha = 0.52f),
                                EcoDotBlack
                            )
                        )
                    )
            )
        }

        if (uiState.isLoading) {
            AlbumLoadingSkeleton()
        } else if (uiState.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.error ?: "Failed to load album",
                    color = EcoDotRed,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.loadAlbum(albumId) },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(albumHazeState),
                contentPadding = PaddingValues(top = 84.dp, bottom = 148.dp)
            ) {
                // ── Hero Album Art + Glass Metadata Pill ─────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(226.dp)
                                .shadow(
                                    elevation = 28.dp,
                                    shape = RoundedCornerShape(22.dp),
                                    ambientColor = accentColor.copy(alpha = 0.45f),
                                    spotColor = Color.Black
                                )
                                .clip(RoundedCornerShape(22.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(22.dp))
                        ) {
                            AsyncImage(
                                model = uiState.thumbnailUrl,
                                contentDescription = uiState.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        Text(
                            text = uiState.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Artist chip & metadata frosted pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.14f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        uiState.artistId?.let { navController.navigate("artist/$it") }
                                    }
                            ) {
                                Text(
                                    text = uiState.artistName.ifBlank { "Artist" },
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.09f))
                            ) {
                                val metaText = buildString {
                                    uiState.year?.takeIf { it.isNotBlank() }?.let { append("$it · ") }
                                    append("${uiState.tracks.size} songs")
                                    if (totalDurationFormatted.isNotEmpty()) {
                                        append(" · $totalDurationFormatted")
                                    }
                                }
                                Text(
                                    text = metaText,
                                    color = Color.White.copy(alpha = 0.68f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // ── Liquid Glass Primary Controls Bar ─────────────────────
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            tintColor = Color(0xFF141724),
                            specularAlpha = 0.34f,
                            hazeState = if (liquidGlassEnabled) albumHazeState else null
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Play & Shuffle Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (uiState.tracks.isNotEmpty()) {
                                                musicViewModel.playQueue(uiState.tracks, startIndex = 0)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                    ) {
                                        Icon(SimpIcons.PlayArrow, null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Play", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Surface(
                                        onClick = {
                                            if (uiState.tracks.isNotEmpty()) {
                                                musicViewModel.playQueue(uiState.tracks.shuffled(), startIndex = 0)
                                            }
                                        },
                                        color = accentColor.copy(alpha = 0.22f),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                SimpIcons.Shuffle,
                                                null,
                                                tint = accentColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Shuffle",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Utility Pills Row: Save, Download, Add Queue, Options
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Save / Bookmark Album
                                    AlbumActionIconPill(
                                        icon = if (isAlbumSaved) SimpIcons.Bookmark else SimpIcons.BookmarkBorder,
                                        label = if (isAlbumSaved) "Saved" else "Save",
                                        tint = if (isAlbumSaved) accentColor else Color.White,
                                        isActive = isAlbumSaved,
                                        onClick = {
                                            if (isAlbumSaved) {
                                                musicViewModel.unsaveAlbum(uiState.tracks)
                                                Toast.makeText(context, "Removed from Library", Toast.LENGTH_SHORT).show()
                                            } else {
                                                musicViewModel.saveAlbum(uiState.tracks)
                                                Toast.makeText(context, "Album saved to Library", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )

                                    // Download Album / Progress
                                    AlbumDownloadIconPill(
                                        isAllDownloaded = allTracksDownloaded,
                                        downloadedCount = downloadedCount,
                                        totalCount = uiState.tracks.size,
                                        activeProgress = activeDownloadProgress,
                                        accentColor = accentColor,
                                        onClick = {
                                            if (allTracksDownloaded) {
                                                musicViewModel.deleteTracksDownloads(uiState.tracks)
                                            } else {
                                                musicViewModel.downloadTracks(uiState.tracks)
                                            }
                                        }
                                    )

                                    // Queue Album
                                    AlbumActionIconPill(
                                        icon = SimpIcons.AddToQueue,
                                        label = "Queue",
                                        tint = Color.White,
                                        isActive = false,
                                        onClick = {
                                            if (uiState.tracks.isNotEmpty()) {
                                                musicViewModel.addTracksToQueue(uiState.tracks)
                                            }
                                        }
                                    )

                                    // Add to Playlist
                                    AlbumActionIconPill(
                                        icon = SimpIcons.PlaylistAdd,
                                        label = "Playlist",
                                        tint = Color.White,
                                        isActive = false,
                                        onClick = { showAddAllPlaylistDialog = true }
                                    )

                                    // More Options
                                    AlbumActionIconPill(
                                        icon = SimpIcons.MoreVert,
                                        label = "More",
                                        tint = Color.White,
                                        isActive = false,
                                        onClick = { showAlbumMoreSheet = true }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // ── Tracks Header ────────────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRACKLIST",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        if (downloadedCount > 0) {
                            Text(
                                text = "$downloadedCount/${uiState.tracks.size} offline",
                                color = accentColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // ── Track Rows ───────────────────────────────────────────────────
                itemsIndexed(uiState.tracks, key = { _, track -> track.id }) { index, track ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.97f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                        label = "album_track_scale"
                    )
                    val isDownloaded = downloadedIds.contains(track.id) || musicViewModel.isDownloaded(track.id)
                    val progress = downloadProgressMap[track.id]
                    val isPlayingTrack = currentTrack?.id == track.id

                    SwipeToQueueBox(onAddToQueue = { musicViewModel.addToQueue(track) }) {
                        Surface(
                            color = if (isPlayingTrack) accentColor.copy(alpha = 0.14f)
                            else Color.White.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                width = 0.7.dp,
                                color = if (isPlayingTrack) accentColor.copy(alpha = 0.4f)
                                else Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .scale(scale)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = LocalIndication.current,
                                    onClick = { musicViewModel.playQueue(uiState.tracks, startIndex = index) }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.width(30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPlayingTrack) {
                                        PlayingIndicator(color = accentColor)
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.45f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        color = if (isPlayingTrack) accentColor else Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (track.isExplicit) {
                                            Surface(
                                                color = Color.White.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    text = "E",
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        if (isDownloaded) {
                                            Icon(
                                                imageVector = SimpIcons.DownloadDone,
                                                contentDescription = "Downloaded",
                                                tint = accentColor,
                                                modifier = Modifier
                                                    .size(13.dp)
                                                    .padding(end = 4.dp)
                                            )
                                        } else if (progress != null) {
                                            CircularProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .padding(end = 4.dp),
                                                strokeWidth = 1.5.dp,
                                                color = accentColor
                                            )
                                        }
                                        val durationLabel = if (track.duration > 0L) {
                                            " · ${musicViewModel.formatDuration(track.duration)}"
                                        } else ""
                                        Text(
                                            text = "${track.artist}$durationLabel",
                                            color = Color.White.copy(alpha = 0.52f),
                                            fontSize = 12.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { optionsMenuTrack = track },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = SimpIcons.MoreVert,
                                        contentDescription = "Track options",
                                        tint = Color.White.copy(alpha = 0.55f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Floating Liquid Glass Top Bar ─────────────────────────────────────
        val showStickyTitle by remember {
            derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 220 }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .align(Alignment.TopCenter)
                .liquidGlass(
                    shape = RoundedCornerShape(22.dp),
                    tintColor = if (showStickyTitle) Color(0xFF101320) else Color(0xFF141622),
                    specularAlpha = if (showStickyTitle) 0.34f else 0.2f,
                    elevation = if (showStickyTitle) 10.dp else 4.dp,
                    hazeState = if (liquidGlassEnabled) albumHazeState else null
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(38.dp)) {
                    Icon(SimpIcons.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                AnimatedVisibility(
                    visible = showStickyTitle,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = uiState.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
                if (!showStickyTitle) {
                    Spacer(modifier = Modifier.weight(1f))
                }

                IconButton(onClick = shareAlbum, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = SimpIcons.Share,
                        contentDescription = "Share Album",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(19.dp)
                    )
                }
                IconButton(onClick = { showAlbumMoreSheet = true }, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = SimpIcons.MoreVert,
                        contentDescription = "Album Options",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── Album More Options Modal Bottom Sheet ─────────────────────────────
        if (showAlbumMoreSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAlbumMoreSheet = false },
                containerColor = Color(0xFF131622),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = uiState.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${uiState.artistName} · ${uiState.tracks.size} tracks",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(8.dp))

                    AlbumSheetActionRow(
                        icon = SimpIcons.QueuePlayNext,
                        label = "Play Album Next",
                        onClick = {
                            showAlbumMoreSheet = false
                            if (uiState.tracks.isNotEmpty()) {
                                musicViewModel.playTracksNext(uiState.tracks)
                                Toast.makeText(context, "Playing ${uiState.tracks.size} tracks next", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    AlbumSheetActionRow(
                        icon = SimpIcons.AddToQueue,
                        label = "Add Album to Queue",
                        onClick = {
                            showAlbumMoreSheet = false
                            if (uiState.tracks.isNotEmpty()) {
                                musicViewModel.addTracksToQueue(uiState.tracks)
                            }
                        }
                    )
                    AlbumSheetActionRow(
                        icon = if (allTracksDownloaded) SimpIcons.DeleteOutline else SimpIcons.DownloadForOffline,
                        label = if (allTracksDownloaded) "Remove Downloaded Tracks" else "Download Entire Album",
                        tint = if (allTracksDownloaded) Color(0xFFFF6B6B) else accentColor,
                        onClick = {
                            showAlbumMoreSheet = false
                            if (allTracksDownloaded) {
                                musicViewModel.deleteTracksDownloads(uiState.tracks)
                            } else {
                                musicViewModel.downloadTracks(uiState.tracks)
                            }
                        }
                    )
                    AlbumSheetActionRow(
                        icon = SimpIcons.PlaylistAdd,
                        label = "Add All Tracks to Playlist",
                        onClick = {
                            showAlbumMoreSheet = false
                            showAddAllPlaylistDialog = true
                        }
                    )
                    AlbumSheetActionRow(
                        icon = if (isAlbumSaved) SimpIcons.Bookmark else SimpIcons.BookmarkBorder,
                        label = if (isAlbumSaved) "Remove from Saved Library" else "Save Album to Library",
                        onClick = {
                            showAlbumMoreSheet = false
                            if (isAlbumSaved) {
                                musicViewModel.unsaveAlbum(uiState.tracks)
                                Toast.makeText(context, "Removed from Library", Toast.LENGTH_SHORT).show()
                            } else {
                                musicViewModel.saveAlbum(uiState.tracks)
                                Toast.makeText(context, "Album saved to Library", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    AlbumSheetActionRow(
                        icon = SimpIcons.Share,
                        label = "Share Album",
                        onClick = {
                            showAlbumMoreSheet = false
                            shareAlbum()
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        // ── Add All Album Tracks to Playlist Dialog ───────────────────────────
        if (showAddAllPlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showAddAllPlaylistDialog = false },
                containerColor = Color(0xFF161926),
                titleContentColor = Color.White,
                title = { Text("Add Album to Playlist", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allPlaylists.forEach { playlist ->
                            Surface(
                                onClick = {
                                    showAddAllPlaylistDialog = false
                                    musicViewModel.addTracksToPlaylist(playlist.id, uiState.tracks, playlist.name)
                                },
                                color = Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = playlist.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = newAllPlaylistName,
                            onValueChange = { newAllPlaylistName = it },
                            singleLine = true,
                            placeholder = { Text("Or new playlist name...", color = Color.White.copy(alpha = 0.4f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    if (newAllPlaylistName.trim().isNotEmpty()) {
                        TextButton(
                            onClick = {
                                val title = newAllPlaylistName.trim()
                                musicViewModel.createPlaylist(title)
                                newAllPlaylistName = ""
                                showAddAllPlaylistDialog = false
                                Toast.makeText(context, "Created playlist \"$title\" — tap Playlist again to add tracks", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Create", color = accentColor, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddAllPlaylistDialog = false }) {
                        Text("Close", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            )
        }

        // ── Track Options Bottom Sheet ────────────────────────────────────────
        optionsMenuTrack?.let { track ->
            val isLiked by musicViewModel.isTrackLiked(track.id).collectAsState(initial = false)
            val isInQueue = musicViewModel.playbackQueue.collectAsState().value.any { it.id == track.id }
            val sleepTimerRemaining by musicViewModel.sleepTimerRemaining.collectAsState()
            val equalizerEnabled by musicViewModel.eqEnabled.collectAsState()

            TrackOptionsBottomSheet(
                track = track,
                onDismiss = { optionsMenuTrack = null },
                onPlayNow = { musicViewModel.playTrack(track) },
                onPlayNext = { musicViewModel.playNext(track) },
                onAddToQueue = { musicViewModel.addToQueue(track) },
                onRemoveFromQueue = if (isInQueue) { { musicViewModel.removeTrackFromQueue(track) } } else null,
                onGoToAlbum = { },
                onGoToArtist = {
                    uiState.artistId?.let { id ->
                        optionsMenuTrack = null
                        navController.navigate("artist/$id")
                    }
                },
                isDownloaded = downloadedIds.contains(track.id) || musicViewModel.isDownloaded(track.id),
                downloadProgress = downloadProgressMap[track.id],
                onDownload = { musicViewModel.downloadTrack(track) },
                onDeleteDownload = { musicViewModel.deleteDownload(track) },
                isLiked = isLiked,
                onLikeToggle = { musicViewModel.toggleTrackLike(track) },
                onAddToPlaylist = { showPlaylistPickerTrack = track },
                onSetSleepTimer = { musicViewModel.setSleepTimer(it) },
                onSetSleepTimerAtEndOfTrack = { musicViewModel.setSleepTimerAtEndOfTrack() },
                onCancelSleepTimer = { musicViewModel.cancelSleepTimer() },
                sleepTimerRemaining = sleepTimerRemaining,
                onEqualizerClick = if (equalizerEnabled) { {} } else null,
                onSetCanvasUrl = { url ->
                    if (url == "FETCH_CANVAS") musicViewModel.enableCanvasForTrack(track)
                    else musicViewModel.setCanvasUrl(track, url)
                }
            )
        }

        showPlaylistPickerTrack?.let { track ->
            AddToPlaylistDialog(
                track = track,
                playlists = allPlaylists,
                onDismiss = { showPlaylistPickerTrack = null },
                onPlaylistSelected = { playlist ->
                    musicViewModel.addTrackToPlaylist(playlist.id, track)
                    showPlaylistPickerTrack = null
                },
                onCreatePlaylist = { name ->
                    musicViewModel.createPlaylist(name)
                }
            )
        }
    }
}

@Composable
private fun AlbumActionIconPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isActive) tint.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.08f))
                .border(0.6.dp, if (isActive) tint.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) tint else Color.White.copy(alpha = 0.72f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AlbumDownloadIconPill(
    isAllDownloaded: Boolean,
    downloadedCount: Int,
    totalCount: Int,
    activeProgress: Float?,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isAllDownloaded || activeProgress != null) accentColor.copy(alpha = 0.16f)
                    else Color.White.copy(alpha = 0.08f)
                )
                .border(
                    0.6.dp,
                    if (isAllDownloaded || activeProgress != null) accentColor.copy(alpha = 0.5f)
                    else Color.White.copy(alpha = 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                activeProgress != null -> CircularProgressIndicator(
                    progress = { activeProgress },
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = accentColor
                )
                isAllDownloaded -> Icon(
                    imageVector = SimpIcons.DownloadDone,
                    contentDescription = "Downloaded",
                    tint = accentColor,
                    modifier = Modifier.size(19.dp)
                )
                else -> Icon(
                    imageVector = SimpIcons.DownloadForOffline,
                    contentDescription = "Download Album",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when {
                activeProgress != null -> "${(activeProgress * 100).toInt()}%"
                isAllDownloaded -> "Offline"
                downloadedCount > 0 -> "$downloadedCount/$totalCount"
                else -> "Download"
            },
            color = if (isAllDownloaded || activeProgress != null) accentColor else Color.White.copy(alpha = 0.72f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AlbumSheetActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTotalAlbumDuration(tracks: List<Track>): String {
    val totalSec = tracks.sumOf { d ->
        when {
            d.duration > 10_000L -> d.duration / 1000L // guard ms vs sec
            d.duration > 0L -> d.duration
            else -> 0L
        }
    }
    if (totalSec <= 0L) return ""
    val hours = totalSec / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return when {
        hours > 0 -> "${hours}h ${mins}m"
        mins > 0 -> "${mins}m ${secs}s"
        else -> "${secs}s"
    }
}

@Composable
private fun AlbumLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 96.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.08f))
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .width(190.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f))
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .width(130.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.06f))
        )
        Spacer(modifier = Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(126.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.06f))
        )
        Spacer(modifier = Modifier.height(24.dp))
        repeat(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(15.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                    )
                }
            }
        }
    }
}
