package com.example.ecodot.ui.screens

import com.example.ecodot.ui.theme.animatedClickable
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.ui.theme.*
import com.example.ecodot.ui.components.*
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.ui.viewmodel.Album
import com.example.ecodot.ui.viewmodel.Artist
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val homeSections by viewModel.homeSections.collectAsState()
    val suggestedTracks by viewModel.suggestedTracks.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val historyRecommendations by viewModel.historyRecommendations.collectAsState()
    val albums by viewModel.allAlbums.collectAsState()
    val artists by viewModel.allArtists.collectAsState()
    val onRepeatTracks by viewModel.onRepeatTracks.collectAsState()
    val moodGreeting by viewModel.moodGreeting.collectAsState()
    val moodTracks by viewModel.moodRecommendedTracks.collectAsState()

    var longPressedTrack by remember { mutableStateOf<Track?>(null) }
    var longPressedAlbum by remember { mutableStateOf<Album?>(null) }
    var longPressedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    var showPlaylistPicker by remember { mutableStateOf<Track?>(null) }



    LiquidMeshBackground(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = 120.dp
            )
        ) {
            item {
                HomeCustomHeader(
                    username = userProfile?.name ?: "EcoDot User",
                    userImageUrl = userProfile?.avatarUri ?: "",
                    onProfileClick = { navController.navigate("profile") },
                    onSearchClick = { navController.navigate("search") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (suggestedTracks.isNotEmpty()) {
                item {
                    // Spotify-like 2-column quick picks
                    val quickPicks = suggestedTracks.take(6)
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        for (i in quickPicks.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickPickCard(
                                    track = quickPicks[i],
                                    onClick = { viewModel.playTrack(quickPicks[i]) },
                                    onLongClick = { longPressedTrack = quickPicks[i] },
                                    modifier = Modifier.weight(1f)
                                )
                                if (i + 1 < quickPicks.size) {
                                    QuickPickCard(
                                        track = quickPicks[i + 1],
                                        onClick = { viewModel.playTrack(quickPicks[i + 1]) },
                                        onLongClick = { longPressedTrack = quickPicks[i + 1] },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 7. Recently Played Section
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    HomeSectionHeader(title = "Recently Played")
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recentlyPlayed, key = { "recent_${it.id}" }) { track ->
                            RecentTrackCard(
                                track = track,
                                onClick = { viewModel.playTrack(track) },
                                onLongClick = { longPressedTrack = track }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Your On Repeat Mix Section
            if (onRepeatTracks.isNotEmpty()) {
                item {
                    HomeSectionHeader(title = "Your On Repeat Mix")
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(onRepeatTracks, key = { "repeat_${it.id}" }) { track ->
                            RecentTrackCard(
                                track = track,
                                onClick = { viewModel.playTrack(track) },
                                onLongClick = { longPressedTrack = track }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Time-of-day Mood Recommendations Section
            if (moodTracks.isNotEmpty()) {
                item {
                    HomeSectionHeader(title = moodGreeting)
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(moodTracks, key = { "mood_${it.id}" }) { track ->
                            RecentTrackCard(
                                track = track,
                                onClick = { viewModel.playTrack(track) },
                                onLongClick = { longPressedTrack = track }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Albums Section
            if (albums.isNotEmpty()) {
                item {
                    HomeSectionHeader(title = "Albums")
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(albums, key = { "home_album_${it.id}_${it.title}" }) { album ->
                            AlbumCard(
                                album = album,
                                onClick = {
                                    album.id?.let { id -> navController.navigate("album/$id") }
                                },
                                onLongClick = {
                                    longPressedAlbum = album
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Artists Section
            if (artists.isNotEmpty()) {
                item {
                    HomeSectionHeader(title = "Artists")
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(artists, key = { "home_artist_${it.id}_${it.name}" }) { artist ->
                            HomeArtistCard(
                                artist = artist,
                                onClick = {
                                    navController.navigate("artist/${artist.id}")
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // 8. Taste recommendations section based on history
            if (historyRecommendations.isNotEmpty()) {
                item {
                    HomeSectionHeader(title = "Based on your recent listening")
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(historyRecommendations, key = { "rec_${it.id}" }) { track ->
                            SimpTrackCard(
                                track = track,
                                onClick = { viewModel.playTrack(track) },
                                onLongClick = { longPressedTrack = track }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            homeSections.forEach { section ->
                if (section.items.isEmpty()) return@forEach
                
                item {
                    HomeSectionHeader(title = section.title)
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(section.items, key = { it.id + it.hashCode() }) { item ->
                            SimpMusicCard(
                                item = item,
                                onClick = {
                                    when(item.type) {
                                        com.example.ecodot.data.remote.MediaType.TRACK -> {
                                            val track = Track(
                                                id = item.id,
                                                title = item.title,
                                                artist = item.subtitle ?: "Unknown",
                                                album = "",
                                                albumArtUri = item.thumbnailUrl,
                                                duration = 0,
                                                path = "https://www.youtube.com/watch?v=${item.id}",
                                                isYouTube = true
                                            )
                                            viewModel.playTrack(track)
                                        }
                                        com.example.ecodot.data.remote.MediaType.ALBUM,
                                        com.example.ecodot.data.remote.MediaType.PLAYLIST -> {
                                            navController.navigate("album/${item.id}")
                                        }
                                        com.example.ecodot.data.remote.MediaType.ARTIST -> {
                                            navController.navigate("artist/${item.id}")
                                        }
                                        com.example.ecodot.data.remote.MediaType.UNKNOWN -> {}
                                    }
                                },
                                onLongClick = {
                                    when(item.type) {
                                        com.example.ecodot.data.remote.MediaType.TRACK -> {
                                            val track = Track(
                                                id = item.id,
                                                title = item.title,
                                                artist = item.subtitle ?: "Unknown",
                                                album = "",
                                                albumArtUri = item.thumbnailUrl,
                                                duration = 0,
                                                path = "https://www.youtube.com/watch?v=${item.id}",
                                                isYouTube = true
                                            )
                                            longPressedTrack = track
                                        }
                                        com.example.ecodot.data.remote.MediaType.ALBUM -> {
                                            val album = Album(
                                                id = item.id,
                                                title = item.title,
                                                artist = item.subtitle ?: "Various Artists",
                                                artUri = item.thumbnailUrl,
                                                tracks = emptyList()
                                            )
                                            longPressedAlbum = album
                                        }
                                        com.example.ecodot.data.remote.MediaType.PLAYLIST -> {
                                            val playlist = Playlist(
                                                id = 0L, // dummy id
                                                name = item.title,
                                                description = item.subtitle ?: "",
                                                coverArtUri = item.thumbnailUrl
                                            )
                                            longPressedPlaylist = playlist
                                        }
                                        else -> {}
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
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
                    navController.navigate("library")
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
fun SimpMusicCard(
    item: com.example.ecodot.data.remote.HomeMediaItem,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val isCircle = item.type == com.example.ecodot.data.remote.MediaType.ARTIST
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

    Column(
        modifier = Modifier
            .width(150.dp)
            .scale(scale)
            .animatedCombinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = if (isCircle) CircleShape else RoundedCornerShape(16.dp),
                    spotColor = Color.Black.copy(alpha = 0.5f),
                    ambientColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(if (isCircle) CircleShape else RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E))
        ) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(item.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Premium inner border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = if (isCircle) CircleShape else RoundedCornerShape(16.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        item.subtitle?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HomeCustomHeader(
    username: String,
    userImageUrl: String,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    var imageLoadFailed by remember(userImageUrl) { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A))
                    .animatedClickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                if (userImageUrl.isEmpty() || imageLoadFailed) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile Placeholder",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(userImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        onError = { imageLoadFailed = true }
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = greeting,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onSearchClick)
            )
        }
    }
}

@Composable
fun ModernEcoDotLogo(
    modifier: Modifier = Modifier,
    accentColor: Color = PrimaryDark
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    
    val orbit1Angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_1"
    )
    val orbit2Angle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_2"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_text"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .size(36.dp)
                .padding(end = 8.dp)
        ) {
            val width = size.width
            val height = size.height
            val center = androidx.compose.ui.geometry.Offset(width / 2f, height / 2f)
            val coreRadius = 4.dp.toPx() * pulseScale

            drawCircle(
                color = accentColor.copy(alpha = 0.25f),
                radius = coreRadius + 10.dp.toPx(),
                center = center
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.5f),
                radius = coreRadius + 5.dp.toPx(),
                center = center
            )

            drawCircle(
                color = Color.White,
                radius = coreRadius,
                center = center
            )

            rotate(orbit1Angle, center) {
                drawArc(
                    color = accentColor,
                    startAngle = 0f,
                    sweepAngle = 120f,
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                    size = androidx.compose.ui.geometry.Size(width - 6.dp.toPx(), height - 6.dp.toPx()),
                    topLeft = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 3.dp.toPx())
                )
            }

            rotate(orbit2Angle, center) {
                drawArc(
                    color = Color.White.copy(alpha = 0.8f),
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
                    size = androidx.compose.ui.geometry.Size(width - 16.dp.toPx(), height - 16.dp.toPx()),
                    topLeft = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }

        val shimmerBrush = Brush.linearGradient(
            colors = listOf(
                accentColor,
                Color.White,
                accentColor,
                accentColor.copy(alpha = 0.6f)
            ),
            start = androidx.compose.ui.geometry.Offset(shimmerTranslate - 300f, 0f),
            end = androidx.compose.ui.geometry.Offset(shimmerTranslate, 0f),
            tileMode = TileMode.Repeated
        )

        Text(
            text = "Eco",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp
            ),
            color = Color.White
        )
        Text(
            text = "Dot",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp,
                brush = shimmerBrush
            )
        )
    }
}

@Composable
fun QuickPickCard(track: Track, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "scale")

    Row(
        modifier = modifier
            .scale(scale)
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color(0x3025283A),
                specularAlpha = 0.35f,
                elevation = 6.dp
            )
            .animatedCombinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(track.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
    }
}

@Composable
fun HomeSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun PremiumTrackCard(track: Track, onClick: () -> Unit, onLongClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .animatedCombinedClickable(onLongClick, onClick)
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(track.albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                ExplicitBadge(modifier = Modifier.padding(end = 4.dp))
            }
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun UpcomingTrackCard(track: Track, onClick: () -> Unit, onLongClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .animatedCombinedClickable(onLongClick, onClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp))) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(track.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "New Release",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(EcoDotRed, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                ExplicitBadge(modifier = Modifier.padding(end = 4.dp))
            }
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentlyPlayedCard(track: Track, onClick: () -> Unit, onLongClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .animatedCombinedClickable(onLongClick, onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(track.albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun RecentTrackCard(
    track: Track,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .animatedCombinedClickable(onLongClick, onClick)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(track.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Subtle play icon overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                ExplicitBadge(modifier = Modifier.padding(end = 4.dp))
            }
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SimpTrackCard(
    track: Track,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .animatedCombinedClickable(onLongClick, onClick)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(20.dp))
        ) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(track.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Premium Overlay play button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                ExplicitBadge(modifier = Modifier.padding(end = 4.dp))
            }
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HomeArtistCard(
    artist: Artist,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .animatedClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(artist.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = artist.name,
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(EcoDotCard),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

