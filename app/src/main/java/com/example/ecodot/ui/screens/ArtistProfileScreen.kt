package com.example.ecodot.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ecodot.ui.theme.ArrowBack
import com.example.ecodot.ui.theme.CameraAlt
import com.example.ecodot.ui.theme.Check
import com.example.ecodot.ui.theme.ChevronRight
import com.example.ecodot.ui.theme.DownloadDone
import com.example.ecodot.ui.theme.EcoDotBlack
import com.example.ecodot.ui.theme.Language
import com.example.ecodot.ui.theme.MoreVert
import com.example.ecodot.ui.theme.Pause
import com.example.ecodot.ui.theme.PlayArrow
import com.example.ecodot.ui.theme.Radio
import com.example.ecodot.ui.theme.Share
import com.example.ecodot.ui.theme.Shuffle
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Verified
import com.example.ecodot.ui.viewmodel.ArtistCollection
import com.example.ecodot.ui.viewmodel.ArtistViewModel
import com.example.ecodot.ui.viewmodel.MusicViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.net.URLEncoder
import kotlin.math.abs

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
    val allPlaylists by musicViewModel.allPlaylists.collectAsState()
    val currentTrack by musicViewModel.currentTrack.collectAsState()
    val isPlayingQueue by musicViewModel.isPlaying.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val context = LocalContext.current

    val scrollState = rememberLazyListState()
    val artistHazeState = remember { HazeState() }

    var showAllPopular by remember { mutableStateOf(false) }
    var optionsMenuTrack by remember { mutableStateOf<Track?>(null) }
    var showPlaylistPicker by remember { mutableStateOf<Track?>(null) }
    var showBioDialog by remember { mutableStateOf(false) }

    val listenersText = remember(uiState.subscribers) {
        uiState.subscribers?.takeIf { it.isNotBlank() }?.let { raw ->
            if (raw.contains("subscriber", true) || raw.contains("listener", true)) raw else "$raw Subscribers"
        }
    }
    val viewsText = remember(uiState.views) {
        uiState.views?.takeIf { it.isNotBlank() }
    }

    val accentColor = remember(uiState.artistName) {
        val index = abs(uiState.artistName.hashCode()) % 6
        when (index) {
            0 -> Color(0xFF9E8CFF)
            1 -> Color(0xFF42E2B8)
            2 -> Color(0xFFFF6B6B)
            3 -> Color(0xFF4DABF7)
            4 -> Color(0xFFFFCA3A)
            else -> Color(0xFFF06595)
        }
    }

    LaunchedEffect(artistId) {
        viewModel.loadArtist(artistId)
    }

    val downloadedIds = remember(downloadedTracks) { downloadedTracks.map { it.id }.toSet() }

    val showStickyTopBar by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 440 }
    }

    val shareArtist: () -> Unit = {
        val shareLink = if (artistId.startsWith("UC") || artistId.startsWith("FE")) {
            "https://music.youtube.com/channel/$artistId"
        } else {
            uiState.artistName
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, uiState.artistName)
            putExtra(Intent.EXTRA_TEXT, "Listen to ${uiState.artistName} on EcoDot\n$shareLink")
        }
        runCatching { context.startActivity(Intent.createChooser(intent, "Share Artist")) }
    }

    val isThisArtistPlaying = isPlayingQueue && uiState.topTracks.any { it.id == currentTrack?.id }

    Scaffold(
        containerColor = EcoDotBlack,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (showStickyTopBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(
                                shape = RoundedCornerShape(22.dp),
                                tintColor = Color(0xFF101320),
                                specularAlpha = 0.36f,
                                elevation = 12.dp,
                                hazeState = if (liquidGlassEnabled) artistHazeState else null
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(SimpIcons.ArrowBack, "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.artistName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = shareArtist, modifier = Modifier.size(36.dp)) {
                                Icon(SimpIcons.Share, "Share", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            if (uiState.topTracks.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        if (isThisArtistPlaying) musicViewModel.controller.value?.pause()
                                        else musicViewModel.playQueue(uiState.topTracks, 0)
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accentColor)
                                ) {
                                    Icon(
                                        imageVector = if (isThisArtistPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { navController.popBackStack() },
                            color = Color.Black.copy(alpha = 0.42f),
                            shape = CircleShape,
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(SimpIcons.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }

                        Surface(
                            onClick = shareArtist,
                            color = Color.Black.copy(alpha = 0.42f),
                            shape = CircleShape,
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(SimpIcons.Share, "Share", tint = Color.White, modifier = Modifier.size(19.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accentColor)
            }
        } else {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(artistHazeState),
                contentPadding = PaddingValues(bottom = 148.dp)
            ) {
                // ── 1. Hero Parallax Artist Header ────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(390.dp)
                    ) {
                        if (uiState.headerImageUrl != null) {
                            AsyncImage(
                                model = uiState.headerImageUrl,
                                contentDescription = uiState.artistName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(accentColor.copy(alpha = 0.42f), Color(0xFF141724))
                                        )
                                    )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color.Black.copy(alpha = 0.45f),
                                            0.45f to Color.Transparent,
                                            0.8f to EcoDotBlack.copy(alpha = 0.78f),
                                            1.0f to EcoDotBlack
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 18.dp)
                        ) {
                            Surface(
                                color = accentColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.6.dp, accentColor.copy(alpha = 0.45f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        SimpIcons.Verified,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = "OFFICIAL ARTIST",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = uiState.artistName,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-1).sp,
                                    fontSize = 36.sp,
                                    lineHeight = 40.sp
                                ),
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            val metaParts = buildList {
                                listenersText?.let { add(it) }
                                viewsText?.let { add(it) }
                                if (uiState.topTracks.isNotEmpty()) add("${uiState.topTracks.size} top songs")
                            }
                            if (metaParts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = metaParts.joinToString(" · "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.74f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // ── 2. Liquid Glass Action Row ────────────────────────────────
                item {
                    val isFollowed by musicViewModel.isArtistFollowed(artistId).collectAsState(initial = false)
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(22.dp),
                        tintColor = Color(0xFF131622),
                        specularAlpha = 0.32f,
                        hazeState = if (liquidGlassEnabled) artistHazeState else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Shuffle button
                            Surface(
                                onClick = {
                                    if (uiState.topTracks.isNotEmpty()) {
                                        musicViewModel.playQueue(uiState.topTracks.shuffled(), 0)
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                color = accentColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(SimpIcons.Shuffle, null, tint = Color.Black, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("Shuffle", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            // Radio button
                            Surface(
                                onClick = {
                                    if (uiState.topTracks.isNotEmpty()) {
                                        musicViewModel.startArtistRadio(uiState.topTracks)
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(0.7.dp, Color.White.copy(alpha = 0.16f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(SimpIcons.Radio, null, tint = accentColor, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("Radio", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }

                            // Follow / Following button
                            Surface(
                                onClick = {
                                    musicViewModel.toggleFollowArtist(
                                        artistId = artistId,
                                        name = uiState.artistName,
                                        imageUrl = uiState.headerImageUrl
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                color = if (isFollowed) Color.White else Color.White.copy(alpha = 0.06f),
                                border = if (isFollowed) null else BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 15.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isFollowed) SimpIcons.Check else SimpIcons.Add,
                                        contentDescription = null,
                                        tint = if (isFollowed) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isFollowed) "Following" else "Follow",
                                        color = if (isFollowed) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Quick Add Queue button
                            if (uiState.topTracks.isNotEmpty()) {
                                Surface(
                                    onClick = { musicViewModel.addTracksToQueue(uiState.topTracks) },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.08f),
                                    border = BorderStroke(0.7.dp, Color.White.copy(alpha = 0.14f)),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = SimpIcons.AddToQueue,
                                            contentDescription = "Queue Top Songs",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 3. Latest Release Liquid Glass Card ───────────────────────
                uiState.latestRelease?.let { release ->
                    item {
                        ArtistSectionTitle("Latest Release")
                        LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            tintColor = Color(0xFF151826),
                            specularAlpha = 0.3f,
                            hazeState = if (liquidGlassEnabled) artistHazeState else null,
                            onClick = { navController.navigate("album/${release.id}") }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = release.thumbnailUrl,
                                    contentDescription = release.title,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(0.6.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = accentColor.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "NEW RELEASE",
                                            color = accentColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = release.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = listOfNotNull(release.type, release.year).joinToString(" · "),
                                        color = Color.White.copy(alpha = 0.58f),
                                        fontSize = 12.5.sp
                                    )
                                }
                                Icon(
                                    imageVector = SimpIcons.ChevronRight,
                                    contentDescription = "Open Release",
                                    tint = Color.White.copy(alpha = 0.55f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // ── 4. Popular Tracks ─────────────────────────────────────────
                if (uiState.topTracks.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 16.dp, top = 18.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Popular Tracks",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (uiState.topTracks.size > 5) {
                                    Surface(
                                        onClick = { showAllPopular = !showAllPopular },
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.14f))
                                    ) {
                                        Text(
                                            text = if (showAllPopular) "Show Top 5" else "See all (${uiState.topTracks.size})",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        val visibleTracks = if (showAllPopular) uiState.topTracks else uiState.topTracks.take(5)
                        visibleTracks.forEachIndexed { index, track ->
                            val isDownloaded = downloadedIds.contains(track.id) || musicViewModel.isDownloaded(track.id)
                            val progress = downloadProgressMap[track.id]
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
                }

                // ── 5. Discography Carousels ──────────────────────────────────
                val discography = uiState.albums.groupBy { it.type }
                discography.forEach { (type, itemsList) ->
                    if (itemsList.isNotEmpty()) {
                        item {
                            ArtistSectionTitle(type)
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(itemsList, key = { it.id }) { item ->
                                    ArtistCollectionCard(item) {
                                        navController.navigate("album/${item.id}")
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 6. About Artist (Liquid Glass + Honest Real Metadata) ─────
                if (uiState.bio.isNotEmpty() || listenersText != null || viewsText != null) {
                    item {
                        ArtistSectionTitle("About")
                        LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(22.dp),
                            tintColor = Color(0xFF131622),
                            specularAlpha = 0.32f,
                            hazeState = if (liquidGlassEnabled) artistHazeState else null,
                            onClick = if (uiState.bio.isNotEmpty()) { { showBioDialog = true } } else null
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    uiState.headerImageUrl?.let { imageUrl ->
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = uiState.artistName,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, accentColor.copy(alpha = 0.45f), CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(Modifier.width(14.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = uiState.artistName,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Icon(
                                                SimpIcons.Verified,
                                                contentDescription = "Verified",
                                                tint = accentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        val statsSummary = listOfNotNull(listenersText, viewsText).joinToString(" · ")
                                        if (statsSummary.isNotEmpty()) {
                                            Spacer(Modifier.height(3.dp))
                                            Text(
                                                text = statsSummary,
                                                color = accentColor,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }

                                if (uiState.bio.isNotEmpty()) {
                                    Spacer(Modifier.height(14.dp))
                                    Text(
                                        text = uiState.bio,
                                        color = Color.White.copy(alpha = 0.78f),
                                        fontSize = 13.5.sp,
                                        lineHeight = 20.sp,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Read full biography →",
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Track Options Bottom Sheet
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
                onGoToAlbum = { track.albumId?.let { id -> navController.navigate("album/$id") } },
                onGoToArtist = {},
                isDownloaded = downloadedIds.contains(track.id) || musicViewModel.isDownloaded(track.id),
                downloadProgress = downloadProgressMap[track.id],
                onDownload = { musicViewModel.downloadTrack(track) },
                onDeleteDownload = { musicViewModel.deleteDownload(track) },
                isLiked = isLiked,
                onLikeToggle = { musicViewModel.toggleTrackLike(track) },
                onAddToPlaylist = { showPlaylistPicker = track },
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

        showPlaylistPicker?.let { track ->
            AddToPlaylistDialog(
                track = track,
                playlists = allPlaylists,
                onDismiss = { showPlaylistPicker = null },
                onPlaylistSelected = { playlist ->
                    musicViewModel.addTrackToPlaylist(playlist.id, track)
                    showPlaylistPicker = null
                },
                onCreatePlaylist = { name -> musicViewModel.createPlaylist(name) }
            )
        }

        // Full Biography Modal Sheet
        if (showBioDialog) {
            ModalBottomSheet(
                onDismissRequest = { showBioDialog = false },
                containerColor = Color(0xFF131622),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                ) {
                    Text(
                        text = "About ${uiState.artistName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = uiState.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        lineHeight = 23.sp
                    )
                    Spacer(Modifier.height(26.dp))
                    Text(
                        text = "Explore Online",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        val uriHandler = LocalUriHandler.current
                        val encodedName = URLEncoder.encode(uiState.artistName, "UTF-8")
                        val socials = listOf(
                            SimpIcons.CameraAlt to "https://www.google.com/search?q=$encodedName+instagram",
                            SimpIcons.Language to "https://www.google.com/search?q=$encodedName+official"
                        )
                        socials.forEach { (icon, url) ->
                            Surface(
                                onClick = { runCatching { uriHandler.openUri(url) } },
                                color = Color.White.copy(alpha = 0.08f),
                                shape = CircleShape,
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.14f)),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }
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
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
    )
}

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
        label = "artist_track_press"
    )

    val durationText = remember(track.duration) {
        val rawSec = when {
            track.duration > 10_000L -> track.duration / 1000L
            track.duration > 0L -> track.duration
            else -> 0L
        }
        if (rawSec <= 0L) "" else String.format("%d:%02d", rawSec / 60, rawSec % 60)
    }

    SwipeToQueueBox(onAddToQueue = onSwipeToAdd) {
        Surface(
            color = if (isPlaying) activeColor.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.04f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 0.65.dp,
                color = if (isPlaying) activeColor.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .scale(scale)
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    onClick = onClick,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(26.dp), contentAlignment = Alignment.Center) {
                    if (isPlaying) {
                        PlayingIndicator(color = activeColor)
                    } else {
                        Text(
                            text = index.toString(),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(0.6.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = if (isPlaying) activeColor else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
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
                                tint = activeColor,
                                modifier = Modifier
                                    .size(13.dp)
                                    .padding(end = 4.dp)
                            )
                        } else if (downloadProgress != null) {
                            CircularProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(end = 4.dp),
                                strokeWidth = 1.5.dp,
                                color = activeColor
                            )
                        }
                        val sub = track.album.takeIf { it.isNotBlank() && it != "Unknown" } ?: track.artist
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.52f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (durationText.isNotEmpty()) {
                    Text(
                        text = durationText,
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(
                    onClick = onOptionsClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = SimpIcons.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ArtistCollectionCard(item: ArtistCollection, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "card_scale"
    )

    Column(
        modifier = Modifier
            .width(146.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(0.7.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick, interactionSource = interactionSource, indication = LocalIndication.current)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(0.6.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = listOfNotNull(item.year, item.type).joinToString(" · "),
            color = Color.White.copy(alpha = 0.52f),
            fontSize = 11.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
