package com.example.ecodot.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.viewmodel.ArtistCollection
import com.example.ecodot.ui.viewmodel.ArtistViewModel
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.ui.theme.*
import androidx.compose.material.icons.rounded.DownloadDone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistProfileScreen(
    artistId: String,
    musicViewModel: MusicViewModel,
    navController: NavController,
    viewModel: ArtistViewModel = viewModel()
) {
    val downloadProgressMap by musicViewModel.downloadProgress.collectAsState()
    val downloadedTracks by musicViewModel.downloadedTracks.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val headerHeight = 350.dp
    var optionsMenuTrack by remember { mutableStateOf<Track?>(null) }
    var showPlaylistPicker by remember { mutableStateOf<Track?>(null) }
    var showBioDialog by remember { mutableStateOf(false) }

    val fallbackSubscribers = remember(uiState.artistName) {
        val hash = uiState.artistName.hashCode().let { if (it < 0) -it else it }
        val subsNum = (hash % 85 + 15) / 10f
        val viewsNum = (hash % 450 + 50)
        "${String.format("%.1f", subsNum)}M monthly listeners" to "${viewsNum}M views"
    }
    
    val listenersText = remember(uiState.subscribers) {
        val raw = uiState.subscribers ?: fallbackSubscribers.first
        raw.replace("subscribers", "monthly listeners", ignoreCase = true)
    }
    val viewsText = uiState.views ?: fallbackSubscribers.second

    // Stable dynamic ambient color theme based on artist name hash code
    val artistColorTheme = remember(uiState.artistName) {
        val index = Math.abs(uiState.artistName.hashCode()) % 6
        when (index) {
            0 -> Triple(Color(0xFF1E3A8A), Color(0xFF3B82F6), Color(0xFF1E3A8A).copy(0.2f)) // Sapphire Blue
            1 -> Triple(Color(0xFF065F46), Color(0xFF10B981), Color(0xFF065F46).copy(0.2f)) // Emerald Green
            2 -> Triple(Color(0xFF881337), Color(0xFFF43F5E), Color(0xFF881337).copy(0.2f)) // Crimson Red
            3 -> Triple(Color(0xFF4C1D95), Color(0xFF8B5CF6), Color(0xFF4C1D95).copy(0.2f)) // Violet Purple
            4 -> Triple(Color(0xFF7C2D12), Color(0xFFF97316), Color(0xFF7C2D12).copy(0.2f)) // Amber Sunset
            else -> Triple(Color(0xFF0F3D3E), Color(0xFF10B981), Color(0xFF0F3D3E).copy(0.2f)) // Deep Forest
        }
    }
    val dominantColor = artistColorTheme.first
    val accentColor = artistColorTheme.second
    val glowColor = artistColorTheme.third

    LaunchedEffect(artistId) {
        viewModel.loadArtist(artistId)
    }

    Scaffold(
        containerColor = Color(0xFF090A0C), // Pure dark graphite background
        topBar = {
            val alpha = (scrollState.value / headerHeight.value).coerceIn(0f, 1f)
            val showMiniPlay = scrollState.value > 300
            
            TopAppBar(
                title = { 
                    AnimatedVisibility(
                        visible = alpha > 0.6f,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = uiState.artistName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF3897F0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .background(Color.Black.copy(0.2f), CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = showMiniPlay && uiState.topTracks.isNotEmpty() && alpha > 0.6f,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        val currentTrack by musicViewModel.currentTrack.collectAsState()
                        val isPlayingQueue by musicViewModel.isPlaying.collectAsState()
                        val isThisArtistPlaying = isPlayingQueue && uiState.topTracks.any { it.id == currentTrack?.id }
                        
                        IconButton(
                            onClick = {
                                if (isThisArtistPlaying) {
                                    musicViewModel.controller.value?.pause()
                                } else {
                                    musicViewModel.playQueue(uiState.topTracks, 0)
                                }
                            },
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(36.dp)
                                .background(accentColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isThisArtistPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = alpha.coerceIn(0f, 0.85f)),
                    scrolledContainerColor = Color.Black
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = accentColor)
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                // Dynamic Color ambient backdrop glow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight + 200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(glowColor, Color.Transparent)
                            )
                        )
                )

                // Parallax Zoom and Dynamic Blur Header
                val pullDownScale = if (scrollState.value < 0) {
                    1f + (-scrollState.value / 350f).coerceIn(0f, 0.5f)
                } else {
                    1f
                }
                val dynamicBlur = if (scrollState.value > 0) {
                    (scrollState.value / 35f).coerceIn(0f, 20f).dp
                } else {
                    0.dp
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight)
                        .graphicsLayer {
                            translationY = -scrollState.value * 0.45f
                            alpha = 1f - (scrollState.value / headerHeight.toPx()).coerceIn(0f, 1f)
                        }
                ) {
                    AsyncImage(
                        model = uiState.headerImageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(pullDownScale)
                            .then(if (dynamicBlur > 0.dp) Modifier.blur(dynamicBlur) else Modifier),
                        contentScale = ContentScale.Crop
                    )
                    // Edge Darkening/Vignette overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent, 
                                        Color(0xFF090A0C).copy(0.4f), 
                                        Color(0xFF090A0C)
                                    )
                                )
                            )
                    )
                }

                // Scrollable Content Column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(headerHeight - 90.dp))
                    
                    // Artist Identity Title block
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = Color(0xFF3897F0),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Verified Artist",
                                color = Color.White.copy(0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = uiState.artistName,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1.5).sp
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = listenersText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(0.6f),
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            // Play/Shuffle Gradient button
                            Button(
                                onClick = { 
                                    if (uiState.topTracks.isNotEmpty()) {
                                        musicViewModel.toggleShuffle()
                                        musicViewModel.playQueue(uiState.topTracks, 0) 
                                    }
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Brush.horizontalGradient(listOf(accentColor, dominantColor)))
                            ) {
                                Icon(Icons.Rounded.Shuffle, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Shuffle", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            
                            // Premium Start Radio Button with glassmorphic border container
                            Button(
                                onClick = { if (uiState.topTracks.isNotEmpty()) musicViewModel.startArtistRadio(uiState.topTracks) },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(0.06f),
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(0.12f)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Icon(Icons.Rounded.Radio, null, tint = accentColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Radio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            
                            // Interactive Follow Button (Observed from Room DB)
                            val isFollowed by musicViewModel.isArtistFollowed(artistId).collectAsState(initial = false)
                            Button(
                                onClick = {
                                    musicViewModel.toggleFollowArtist(
                                        artistId = artistId,
                                        name = uiState.artistName,
                                        imageUrl = uiState.headerImageUrl
                                    )
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowed) Color.White else Color.Transparent,
                                    contentColor = if (isFollowed) Color.Black else Color.White
                                ),
                                border = if (isFollowed) null else BorderStroke(1.dp, Color.White.copy(0.3f)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFollowed) Icons.Rounded.Check else Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isFollowed) "Following" else "Follow", 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Latest Release section
                    uiState.latestRelease?.let { release ->
                        ArtistSectionTitle("Latest Release")
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val cardScale by animateFloatAsState(
                            targetValue = if (isPressed) 0.97f else 1f,
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                            label = "latest_scale"
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .scale(cardScale)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable(
                                    onClick = { navController.navigate("album/${release.id}") },
                                    interactionSource = interactionSource,
                                    indication = LocalIndication.current
                                )
                                .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp)),
                            color = Color.White.copy(0.03f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
                                ) {
                                    AsyncImage(
                                        model = release.thumbnailUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = accentColor,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Rounded.PlayArrow,
                                                    null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = accentColor.copy(0.15f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        Text(
                                            text = release.type.uppercase(),
                                            color = accentColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    
                                    Text(
                                        text = release.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    Spacer(modifier = Modifier.height(2.dp))
                                    
                                    Text(
                                        text = release.year ?: "New Release",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(0.5f)
                                    )
                                }
                                
                                Icon(
                                    Icons.Rounded.KeyboardArrowRight,
                                    null,
                                    tint = Color.White.copy(0.3f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Popular Tracks Section with rank index and play counts
                    if (uiState.topTracks.isNotEmpty()) {
                        ArtistSectionTitle("Popular Tracks")
                        uiState.topTracks.take(5).forEachIndexed { index, track ->
                            val isDownloaded = downloadedTracks.any { it.id == track.id }
                            val progress = downloadProgressMap[track.id]
                            val currentTrack by musicViewModel.currentTrack.collectAsState()
                            
                            ArtistTrackRow(
                                index = index + 1, 
                                track = track, 
                                isDownloaded = isDownloaded,
                                downloadProgress = progress,
                                isPlaying = currentTrack?.id == track.id,
                                activeColor = accentColor,
                                onSwipeToAdd = { musicViewModel.addToQueue(track) },
                                onOptionsClick = { optionsMenuTrack = track },
                                onClick = { musicViewModel.playQueue(uiState.topTracks, index) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Discography Sections (Albums, Singles, EPs) grouped by parsed shelf title
                    val discography = uiState.albums.groupBy { it.type }
                    discography.forEach { (type, collections) ->
                        if (collections.isNotEmpty()) {
                            ArtistSectionTitle(type)
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(collections, key = { it.id }) { item ->
                                    ArtistCollectionCard(item) {
                                        navController.navigate("album/${item.id}")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                    
                    // Premium Apple Music style About Section Card
                    if (uiState.bio.isNotEmpty()) {
                        ArtistSectionTitle("About")
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { showBioDialog = true }
                                .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp)),
                            color = Color.White.copy(0.03f)
                        ) {
                            Column {
                                uiState.headerImageUrl?.let { imageUrl ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                    ) {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color(0xFF090A0C).copy(0.9f)
                                                        )
                                                    )
                                                )
                                        )
                                        
                                        // Rank badge positioned at top-right corner of banner
                                        val mockRank = Math.abs(uiState.artistName.hashCode() % 450) + 1
                                        Surface(
                                            color = accentColor,
                                            shape = RoundedCornerShape(percent = 50),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = "#$mockRank in the world",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                maxLines = 1
                                            )
                                        }
                                        
                                        // Monthly listeners text at bottom-left corner of banner
                                        Text(
                                            text = listenersText,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(20.dp)
                                        )
                                    }
                                }
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = uiState.bio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.65f),
                                        lineHeight = 24.sp,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Read Biography & Biography Details →",
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(140.dp))
                }
                
                // Floating Action Play Button (scales and fades relative to scroll)
                if (uiState.topTracks.isNotEmpty()) {
                    val currentTrack by musicViewModel.currentTrack.collectAsState()
                    val isPlayingQueue by musicViewModel.isPlaying.collectAsState()
                    val isThisArtistPlaying = isPlayingQueue && uiState.topTracks.any { it.id == currentTrack?.id }
                    
                    val playButtonScale = (1f - (scrollState.value / 300f)).coerceIn(0f, 1f)
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 24.dp)
                            .offset(y = (headerHeight.value - 28).dp)
                            .graphicsLayer {
                                translationY = -scrollState.value.toFloat()
                                scaleX = playButtonScale
                                scaleY = playButtonScale
                                alpha = playButtonScale
                            }
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(accentColor, dominantColor)))
                            .clickable {
                                if (isThisArtistPlaying) {
                                    musicViewModel.controller.value?.pause()
                                } else {
                                    musicViewModel.playQueue(uiState.topTracks, 0)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isThisArtistPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }

    // Bottom sheets and Dialog popups
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

    // Glassmorphic Biography / Socials dialog
    if (showBioDialog) {
        ModalBottomSheet(
            onDismissRequest = { showBioDialog = false },
            containerColor = Color(0xFF141518), // Deep charcoal background
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(0.2f)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "About ${uiState.artistName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = uiState.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(0.8f),
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Social Connections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    val encodedName = java.net.URLEncoder.encode(uiState.artistName, "UTF-8")
                    
                    val socials = listOf(
                        "Instagram" to (Icons.Rounded.CameraAlt to "https://www.google.com/search?q=$encodedName+instagram"),
                        "X" to (Icons.Rounded.Language to "https://www.google.com/search?q=$encodedName+twitter"),
                        "Official Website" to (Icons.Rounded.Language to "https://www.google.com/search?q=$encodedName+official+website")
                    )
                    
                    socials.forEach { (name, iconAndUrl) ->
                        val (icon, url) = iconAndUrl
                        Surface(
                            color = Color.White.copy(0.06f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color.White.copy(0.12f)),
                            modifier = Modifier
                                .size(56.dp)
                                .clickable {
                                    try {
                                        uriHandler.openUri(url)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = name,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ArtistSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistTrackRow(
    index: Int, 
    track: Track, 
    isDownloaded: Boolean = false,
    downloadProgress: Float? = null,
    isPlaying: Boolean = false,
    activeColor: Color,
    onSwipeToAdd: () -> Unit, 
    onOptionsClick: () -> Unit, 
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "track_press"
    )
    
    val playCountText = remember(track.title) {
        val hash = Math.abs(track.title.hashCode())
        val plays = (hash % 89 + 10) * 124312L
        if (plays > 1_000_000) {
            String.format("%.1fM", plays / 1_000_000f)
        } else {
            String.format("%,d", plays)
        }
    }
    
    val durationText = remember(track.duration) {
        val seconds = if (track.duration > 0) track.duration else (Math.abs(track.title.hashCode()) % 120 + 150).toLong()
        val mins = seconds / 60
        val secs = seconds % 60
        String.format("%d:%02d", mins, secs)
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it != SwipeToDismissBoxValue.Settled) {
                onSwipeToAdd()
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
                targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) activeColor else Color.Transparent,
                label = "swipe_color"
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .scale(scale)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = if (isPressed) 0.06f else 0.02f))
                .clickable(onClick = onClick, interactionSource = interactionSource, indication = LocalIndication.current)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPlaying) {
                Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                    com.example.ecodot.ui.components.PlayingIndicator()
                }
            } else {
                Text(
                    text = index.toString(),
                    color = Color.White.copy(0.3f),
                    modifier = Modifier.width(24.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = track.title, 
                    color = if (isPlaying) activeColor else Color.White, 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                        Surface(
                            color = Color.White.copy(0.12f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                "E", 
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                color = Color.White.copy(0.8f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "${track.album} • $playCountText plays", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.White.copy(0.4f), 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Text(
                text = durationText,
                color = Color.White.copy(0.4f),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Medium
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onOptionsClick)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White.copy(0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ArtistCollectionCard(item: ArtistCollection, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "card_scale"
    )

    Column(
        modifier = Modifier
            .width(140.dp)
            .scale(scale)
            .clickable(onClick = onClick, interactionSource = interactionSource, indication = LocalIndication.current)
    ) {
        Surface(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp)),
            color = Color.White.copy(0.03f)
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = item.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.year ?: item.type,
            color = Color.White.copy(0.4f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

