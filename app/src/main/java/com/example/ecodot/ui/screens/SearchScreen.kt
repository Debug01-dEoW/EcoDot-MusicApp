package com.example.ecodot.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle   
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.ui.viewmodel.Album
import com.example.ecodot.ui.viewmodel.Artist
import com.example.ecodot.data.local.entities.RecentSearchItem
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.ui.theme.*
import com.example.ecodot.ui.components.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay

private enum class SearchState { IDLE, RESULTS }

@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val searchResults    by viewModel.searchResults.collectAsState()
    val searchAlbums     by viewModel.searchAlbums.collectAsState()
    val searchArtists    by viewModel.searchArtists.collectAsState()
    val isSearching      by viewModel.isSearching.collectAsState()
    val searchHistory    by viewModel.searchHistory.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val searchError      by viewModel.searchError.collectAsState()

    var query           by remember { mutableStateOf("") }
    var selectedFilter  by remember { mutableStateOf("All") }
    var searchState     by remember { mutableStateOf(SearchState.IDLE) }
    var isFocused       by remember { mutableStateOf(false) }

    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    var optionsMenuTrack by remember { mutableStateOf<com.example.ecodot.data.local.entities.Track?>(null) }
    var longPressedAlbum by remember { mutableStateOf<Album?>(null) }
    var longPressedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showPlaylistPicker by remember { mutableStateOf<com.example.ecodot.data.local.entities.Track?>(null) }

    val focusManager    = LocalFocusManager.current
    val focusRequester  = remember { FocusRequester() }

    val filters = listOf("All", "Artists", "Songs", "Videos", "Albums")

    LaunchedEffect(query) {
        if (query.isBlank()) {
            viewModel.clearSearch()
            viewModel.updateSuggestions("")
            searchState = SearchState.IDLE
            return@LaunchedEffect
        }
        delay(150)
        viewModel.updateSuggestions(query)
        delay(500)
        searchState = SearchState.RESULTS
        viewModel.performSearch(query, if (selectedFilter == "All") null else selectedFilter)
    }

    fun executeSearch(q: String) {
        if (q.isBlank()) return
        focusManager.clearFocus()
        searchState = SearchState.RESULTS
        viewModel.performSearch(q, if (selectedFilter == "All") null else selectedFilter)
    }

    LiquidMeshBackground(
        modifier = modifier.fillMaxSize()
    ) {

        // ── Scrollable Body Content ──────────────────────────────────────────
        val isSuggestionsVisible = isFocused && query.isNotEmpty() && searchSuggestions.isNotEmpty()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = if (searchState == SearchState.RESULTS && !isSuggestionsVisible) 180.dp else 140.dp)
        ) {
            Crossfade(
                targetState = when {
                    isSuggestionsVisible -> "SUGGESTIONS"
                    searchState == SearchState.RESULTS -> "RESULTS"
                    else -> "IDLE"
                },
                label = "search_body"
            ) { state ->
                when (state) {
                    "IDLE" -> {
                        IdleSearchBody(
                            history = searchHistory,
                            onHistoryClick = { item ->
                                when (item.type) {
                                    "Artist" -> {
                                        viewModel.selectSearchResult(item.query, item.type, item.itemId, item.imageUrl, item.subtitle)
                                        if (!item.itemId.isNullOrEmpty()) {
                                            navController.navigate("artist/${item.itemId}")
                                        }
                                    }
                                    "Album" -> {
                                        viewModel.selectSearchResult(item.query, item.type, item.itemId, item.imageUrl, item.subtitle)
                                        if (!item.itemId.isNullOrEmpty()) {
                                            navController.navigate("album/${item.itemId}")
                                        }
                                    }
                                    "Song", "Video" -> {
                                        val dummyTrack = com.example.ecodot.data.local.entities.Track(
                                            id = item.itemId ?: "",
                                            title = item.query,
                                            artist = item.subtitle ?: "",
                                            album = "",
                                            duration = 0L,
                                            path = "",
                                            albumArtUri = item.imageUrl
                                        )
                                        viewModel.selectSearchResult(item.query, item.type, item.itemId, item.imageUrl, item.subtitle)
                                        viewModel.playTrack(dummyTrack, isVideo = item.type == "Video")
                                    }
                                    else -> {
                                        query = item.query
                                        executeSearch(item.query)
                                    }
                                }
                            },
                            onHistoryRemove = { viewModel.removeSearchHistory(it) },
                            onClearAllHistory = { viewModel.clearAllSearchHistory() }
                        )
                    }
                    "SUGGESTIONS" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            items(searchSuggestions.take(8)) { suggestion ->
                                SuggestionRow(
                                    suggestion = suggestion,
                                    query = query,
                                    onClick = {
                                        query = suggestion
                                        executeSearch(suggestion)
                                    }
                                )
                            }
                        }
                    }
                    "RESULTS" -> {
                        SearchResultList(
                            tracks   = searchResults,
                            albums   = searchAlbums,
                            artists  = searchArtists,
                            selectedFilter = selectedFilter,
                            onFilterChange = { filter ->
                                selectedFilter = filter
                                executeSearch(query)
                            },
                            isLoading = isSearching && searchResults.isEmpty() && searchAlbums.isEmpty() && searchArtists.isEmpty(),
                            errorMessage = searchError,
                            viewModel = viewModel,
                            navController = navController,
                            onOptionsClick = { optionsMenuTrack = it },
                            onAlbumLongClick = { longPressedAlbum = it },
                            onVideoClick = { track ->
                                viewModel.selectSearchResult(track.title, "Video", track.id, track.albumArtUri, track.artist)
                                viewModel.playTrack(track, isVideo = true)
                            }
                        )
                    }
                }
            }
        }

        // ── Floating Frosted Top Header ─────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.85f)) // Frosted glass tint
                .drawBehind {
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1f
                    )
                }
                .statusBarsPadding()
                .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Search",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val searchBarBorderColor by animateColorAsState(
                targetValue = if (isFocused) EcoDotRed.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                animationSpec = tween(300),
                label = "search_border"
            )
            val searchBarScale by animateFloatAsState(
                targetValue = if (isFocused) 1.01f else 1.0f,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
                label = "search_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .scale(searchBarScale)
                    .liquidGlass(
                        shape = RoundedCornerShape(26.dp),
                        backgroundColor = Color(0x35202336),
                        specularAlpha = 0.38f,
                        elevation = 8.dp
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Search, null,
                        tint = if (isFocused) EcoDotRed else Color.White.copy(0.35f),
                        modifier = Modifier.size(22.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                "Songs, artists, albums…",
                                color = Color.White.copy(0.25f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 16.sp
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { newVal ->
                                query = newVal
                                if (newVal.isEmpty()) {
                                    viewModel.clearSearch()
                                    viewModel.updateSuggestions("")
                                    searchState = SearchState.IDLE
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { isFocused = it.isFocused },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White, fontSize = 16.sp
                            ),
                            cursorBrush = SolidColor(EcoDotRed),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { executeSearch(query) }
                            )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedVisibility(visible = isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White.copy(0.7f),
                                strokeWidth = 2.dp
                            )
                        }
                        AnimatedVisibility(visible = query.isNotEmpty() && !isSearching) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(0.1f))
                                    .clickable {
                                        query = ""
                                        viewModel.clearSearch()
                                        searchState = SearchState.IDLE
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Close, null,
                                    tint = Color.White.copy(0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = searchState == SearchState.RESULTS && !isSuggestionsVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 2.dp)
                ) {
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        
                        val chipScale by animateFloatAsState(
                            targetValue = if (isPressed) 0.92f else 1f,
                            label = "chip_scale"
                        )
                        val chipBgColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.05f),
                            animationSpec = tween(250),
                            label = "chip_bg"
                        )
                        val chipTextColor by animateColorAsState(
                            targetValue = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                            animationSpec = tween(250),
                            label = "chip_text"
                        )
                        val chipBorderColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.08f),
                            animationSpec = tween(250),
                            label = "chip_border"
                        )

                        Box(
                            modifier = Modifier
                                .scale(chipScale)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(chipBgColor)
                                .border(
                                    width = 0.5.dp,
                                    color = chipBorderColor,
                                    shape = RoundedCornerShape(percent = 50)
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    selectedFilter = filter
                                    executeSearch(query)
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                color = chipTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    optionsMenuTrack?.let { track ->
        val isDownloaded = downloadedTracks.any { it.id == track.id }
        val downloadProgress = downloadProgressMap[track.id]
        val isLiked by viewModel.isTrackLiked(track.id).collectAsState(initial = false)
        
        TrackOptionsBottomSheet(
            track = track,
            onDismiss = { optionsMenuTrack = null },
            onPlayNext = { viewModel.playNext(track) },
            onAddToQueue = { viewModel.addToQueue(track) },
            onGoToAlbum = { 
                optionsMenuTrack = null
                if (track.album.isNotBlank() && !track.albumId.isNullOrEmpty()) {
                    navController.navigate("album/${track.albumId}") 
                } else if (track.album.isNotBlank()) {
                    viewModel.performSearch(track.album)
                }
            },
            onGoToArtist = { 
                optionsMenuTrack = null
                if (!track.artistId.isNullOrEmpty()) {
                    navController.navigate("artist/${track.artistId}") 
                } else if (track.artist.isNotBlank()) {
                    viewModel.performSearch(track.artist)
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

// ─────────────────────────────────────────────────────────────────────────────
// Idle / History Body
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IdleSearchBody(
    history: List<RecentSearchItem>,
    onHistoryClick: (RecentSearchItem) -> Unit,
    onHistoryRemove: (RecentSearchItem) -> Unit,
    onClearAllHistory: () -> Unit
) {
    val genres = listOf(
        "Pop" to Color(0xFFE13300),
        "Hip-Hop" to Color(0xFFBA5D07),
        "Rock" to Color(0xFFE91429),
        "Indie" to Color(0xFF608108),
        "R&B" to Color(0xFFB02897),
        "K-Pop" to Color(0xFF148A08),
        "Chill" to Color(0xFF477D95),
        "Workout" to Color(0xFF777777),
        "Party" to Color(0xFFAF2896),
        "Focus" to Color(0xFF503750)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 120.dp)
    ) {
        if (history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Searches",
                        color = Color.White.copy(0.9f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Clear all",
                        color = EcoDotRed.copy(0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onClearAllHistory() }
                    )
                }
            }
            items(history, key = { it.id }) { item ->
                HistoryRow(
                    item     = item,
                    onClick  = { onHistoryClick(item) },
                    onRemove = { onHistoryRemove(item) }
                )
            }
        }
        
        item {
            Text(
                "Browse all",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }
        
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                for (i in genres.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GenreCard(
                            title = genres[i].first,
                            color = genres[i].second,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val dummyItem = RecentSearchItem(query = genres[i].first, type = "Query")
                                onHistoryClick(dummyItem)
                            }
                        )
                        if (i + 1 < genres.size) {
                            GenreCard(
                                title = genres[i + 1].first,
                                color = genres[i + 1].second,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val dummyItem = RecentSearchItem(query = genres[i + 1].first, type = "Query")
                                    onHistoryClick(dummyItem)
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenreCard(title: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.93f else 1f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f), label = "genre_scale")

    val gradient = remember(color) {
        Brush.linearGradient(
            colors = listOf(
                color,
                color.copy(alpha = 0.8f),
                Color.Black.copy(alpha = 0.4f)
            ),
            start = Offset(0f, 0f),
            end = Offset(450f, 450f)
        )
    }

    val artworkUrl = remember(title) {
        when (title) {
            "Pop" -> "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=150&auto=format&fit=crop&q=60"
            "Hip-Hop" -> "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=150&auto=format&fit=crop&q=60"
            "Rock" -> "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=150&auto=format&fit=crop&q=60"
            "Indie" -> "https://images.unsplash.com/photo-1487180142328-0c4e37023af5?w=150&auto=format&fit=crop&q=60"
            "R&B" -> "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=150&auto=format&fit=crop&q=60"
            "K-Pop" -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150&auto=format&fit=crop&q=60"
            "Chill" -> "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=150&auto=format&fit=crop&q=60"
            "Workout" -> "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=150&auto=format&fit=crop&q=60"
            "Party" -> "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=150&auto=format&fit=crop&q=60"
            "Focus" -> "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=150&auto=format&fit=crop&q=60"
            else -> null
        }
    }

    Box(
        modifier = modifier
            .height(95.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick, interactionSource = interactionSource, indication = null),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(14.dp)
            )
            if (artworkUrl != null) {
                AsyncImage(
                    model = artworkUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 18.dp, y = 10.dp)
                        .graphicsLayer { rotationZ = 25f }
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Results List
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchResultList(
    tracks: List<com.example.ecodot.data.local.entities.Track>,
    albums: List<Album>,
    artists: List<Artist>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    viewModel: MusicViewModel,
    navController: NavController,
    onOptionsClick: (com.example.ecodot.data.local.entities.Track) -> Unit,
    onAlbumLongClick: (Album) -> Unit,
    onVideoClick: (com.example.ecodot.data.local.entities.Track) -> Unit
) {
    val networkState by viewModel.networkState.collectAsState()

    if (!networkState.isConnected && tracks.isEmpty() && albums.isEmpty() && artists.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(90.dp).background(Color.White.copy(0.04f), CircleShape))
                    Icon(
                        Icons.Rounded.WifiOff, null,
                        tint = EcoDotRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "No Internet Connection",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Check your connection and try again. You can still play your downloaded music in the Library.",
                    color = Color.White.copy(0.4f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
        return
    }

    if (isLoading) {
        SearchShimmerList()
        return
    }

    if (errorMessage != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White.copy(0.2f)
                )
                Spacer(Modifier.height(16.dp))
                Text("Search Failed", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(errorMessage, color = Color.White.copy(0.5f), fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val firstQuery = viewModel.searchHistory.value.firstOrNull()?.query ?: ""
                        viewModel.performSearch(firstQuery, if (selectedFilter == "All") null else selectedFilter)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoDotRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Retry Search", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }

    val hasSongs = tracks.isNotEmpty()
    val hasAlbums = albums.isNotEmpty()
    val hasArtists = artists.isNotEmpty()

    val isEmptyForFilter = when (selectedFilter) {
        "Songs" -> !hasSongs
        "Videos" -> !hasSongs // Videos are also in the 'tracks' list
        "Albums" -> !hasAlbums
        "Artists" -> !hasArtists
        else -> !hasSongs && !hasAlbums && !hasArtists
    }

    if (isEmptyForFilter && !hasSongs && !hasAlbums && !hasArtists) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(90.dp).background(Color.White.copy(0.04f), CircleShape))
                    Icon(
                        Icons.Rounded.SearchOff, null,
                        tint = Color.White.copy(0.2f),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("No results found", color = Color.White.copy(0.5f), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Try a different search term", color = Color.White.copy(0.25f), fontSize = 13.sp)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        if (!networkState.isConnected) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(0.03f))
                        .border(1.5.dp, Color.White.copy(0.08f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CloudOff,
                            contentDescription = null,
                            tint = Color.White.copy(0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Offline Mode. Showing local downloads only.",
                            color = Color.White.copy(0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (isEmptyForFilter) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No matching $selectedFilter found",
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Try searching in other categories:",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterSuggestionChip(label = "All Results", onClick = { onFilterChange("All") })
                        if (selectedFilter != "Songs" && hasSongs) {
                            FilterSuggestionChip(label = "Songs (${tracks.size})", onClick = { onFilterChange("Songs") })
                        }
                        if (selectedFilter != "Videos" && hasSongs) {
                            FilterSuggestionChip(label = "Videos (${tracks.size})", onClick = { onFilterChange("Videos") })
                        }
                        if (selectedFilter != "Albums" && hasAlbums) {
                            FilterSuggestionChip(label = "Albums (${albums.size})", onClick = { onFilterChange("Albums") })
                        }
                        if (selectedFilter != "Artists" && hasArtists) {
                            FilterSuggestionChip(label = "Artists (${artists.size})", onClick = { onFilterChange("Artists") })
                        }
                    }
                }
            }
        }
        // ── Top Match - Featured Artist Card ────────────────────────────
        if ((selectedFilter == "All" || selectedFilter == "Artists") && artists.isNotEmpty()) {
            val topArtist = artists.first()
            val cardKey = if (topArtist.id.isNotEmpty()) "featured_artist_${topArtist.id}" else "featured_artist_${topArtist.name}"
            item(key = cardKey) {
                FeaturedArtistCard(
                    artist = topArtist,
                    onClick = {
                        viewModel.selectSearchResult(topArtist.name, "Artist", topArtist.id, topArtist.imageUrl, null)
                        if (topArtist.id.isNotEmpty()) {
                            navController.navigate("artist/${topArtist.id}")
                        }
                    }
                )
            }
        }

        // ── Songs ───────────────────────────────────────────────────────
        if (tracks.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Songs" || selectedFilter == "Videos")) {
            item { SectionLabel(if (selectedFilter == "Videos") "Videos" else "Songs") }
            items(tracks, key = { track -> track.id }) { track ->
                ResultRow(
                    title    = track.title,
                    subtitle = track.artist,
                    imageUrl = track.albumArtUri,
                    type     = if (selectedFilter == "Videos") "Video" else "Song",
                    isExplicit = track.isExplicit,
                    onArtistClick = track.artistId?.let { aid ->
                        {
                            viewModel.selectSearchResult(track.artist, "Artist", aid, null, null)
                            navController.navigate("artist/$aid")
                        }
                    },
                    onOptionsClick = { onOptionsClick(track) },
                    onLongClick = { onOptionsClick(track) },
                    onClick  = { 
                        viewModel.selectSearchResult(track.title, if (selectedFilter == "Videos") "Video" else "Song", track.id, track.albumArtUri, track.artist)
                        if (selectedFilter == "Videos") {
                            onVideoClick(track)
                        } else {
                            viewModel.playTrack(track)
                        }
                    }
                )
            }
        }

        // ── Albums ──────────────────────────────────────────────────────
        if (albums.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Albums")) {
            item { SectionLabel("Albums") }
            items(albums, key = { album -> album.id ?: album.title }) { album ->
                ResultRow(
                    title    = album.title,
                    subtitle = album.artist,
                    imageUrl = album.artUri,
                    type     = "Album",
                    onLongClick = { onAlbumLongClick(album) },
                    onClick  = {
                        viewModel.selectSearchResult(album.title, "Album", album.id, album.artUri, album.artist)
                        album.id?.let { navController.navigate("album/$it") }
                    }
                )
            }
        }

        // ── Other Artists ───────────────────────────────────────────────
        if (artists.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Artists")) {
            val otherArtists = artists.drop(1)
            if (otherArtists.isNotEmpty()) {
                item { SectionLabel(if (selectedFilter == "All") "More Artists" else "Artists") }
                items(otherArtists, key = { artist -> artist.id }) { artist ->
                    ResultRow(
                        title    = artist.name,
                        subtitle = "Artist",
                        imageUrl = artist.imageUrl,
                        type     = "Artist",
                        onClick  = {
                            viewModel.selectSearchResult(artist.name, "Artist", artist.id, artist.imageUrl, null)
                            navController.navigate("artist/${artist.id}")
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Featured Artist Card (Top Match)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FeaturedArtistCard(
    artist: Artist,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "artist_card_scale"
    )
    val rotationX by animateFloatAsState(
        targetValue = if (isPressed) -3f else 0f,
        animationSpec = tween(150),
        label = "artist_card_rotX"
    )
    val rotationY by animateFloatAsState(
        targetValue = if (isPressed) 3f else 0f,
        animationSpec = tween(150),
        label = "artist_card_rotY"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .scale(scale)
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                cameraDistance = 8 * density
            }
    ) {
        // Glow shadow behind card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .offset(y = 8.dp)
                .background(
                    Brush.radialGradient(
                        listOf(EcoDotRed.copy(0.2f), Color.Transparent),
                        center = Offset(200f, 60f),
                        radius = 300f
                    ),
                    RoundedCornerShape(24.dp)
                )
                )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onClick, interactionSource = interactionSource, indication = LocalIndication.current),
            color = Color(0xFF141414),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    listOf(EcoDotRed.copy(0.45f), Color.White.copy(0.04f), Color.Transparent)
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Artist avatar with glow ring
                Box(modifier = Modifier.size(76.dp)) {
                    // Glow ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(listOf(EcoDotRed.copy(0.4f), Color.Transparent)),
                                CircleShape
                            )
                    )
                    AsyncImage(
                        model = artist.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(EcoDotRed, CircleShape)
                        )
                        Text(
                            text = "TOP MATCH",
                            color = EcoDotRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "•",
                            color = Color.White.copy(0.2f),
                            fontSize = 9.sp
                        )
                        Text(
                            text = "ARTIST",
                            color = Color.White.copy(0.4f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = artist.name,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "View profile & all releases →",
                        color = Color.White.copy(0.35f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable UI Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        color = Color.White.copy(0.35f),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(start = 22.dp, top = 24.dp, bottom = 6.dp)
    )
}

@Composable
fun ResultRow(
    title: String,
    subtitle: String,
    imageUrl: String?,
    type: String,
    isExplicit: Boolean = false,
    onArtistClick: (() -> Unit)? = null,
    onOptionsClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f), label = "result_row_scale")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F0F11))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(16.dp)
            )
            .animatedCombinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with a subtle inner border
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(if (type == "Artist") CircleShape else RoundedCornerShape(12.dp))
                .background(Color(0xFF222222))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = if (type == "Artist") CircleShape else RoundedCornerShape(12.dp)
                )
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Subtle Play Overlay for Songs
            if (type == "Song") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.3).sp
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Type pill
                val (pillBg, pillText) = when (type) {
                    "Artist" -> Color(0xFF2A1F4A) to Color(0xFFB39DDB)
                    "Album"  -> Color(0xFF3D0F0F) to EcoDotRed
                    else     -> Color(0xFF1A1A1A) to Color.White.copy(0.5f)
                }
                Surface(
                    color = pillBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = type,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = pillText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isExplicit) {
                    Spacer(modifier = Modifier.width(6.dp))
                    ExplicitBadge()
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (onArtistClick != null) {
                    Text(
                        text = subtitle,
                        color = EcoDotRed.copy(0.8f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onArtistClick() }
                    )
                } else {
                    Text(
                        text = subtitle,
                        color = Color.White.copy(0.4f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (onOptionsClick != null) {
            IconButton(onClick = onOptionsClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Rounded.MoreVert, null,
                    tint = Color.White.copy(0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(item: RecentSearchItem, onClick: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isArtist = item.type == "Artist"
        val isQuery = item.type == "Query"
        
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(if (isArtist) CircleShape else RoundedCornerShape(8.dp))
                .background(Color.White.copy(0.04f))
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = if (isArtist) CircleShape else RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isQuery || item.imageUrl.isNullOrEmpty()) {
                Icon(
                    imageVector = if (isQuery) Icons.Rounded.History else Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(0.4f),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.query,
                color = Color.White.copy(0.9f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when (item.type) {
                    "Query" -> "Search query"
                    "Song" -> if (!item.subtitle.isNullOrEmpty()) "Song • ${item.subtitle}" else "Song"
                    "Video" -> if (!item.subtitle.isNullOrEmpty()) "Video • ${item.subtitle}" else "Video"
                    else -> item.type
                },
                color = Color.White.copy(0.4f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Rounded.Close, null,
                tint = Color.White.copy(0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SearchShimmerList() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 22.dp, end = 22.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = shimmerAlpha))
        )

        repeat(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F0F11))
                    .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = shimmerAlpha))
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = shimmerAlpha))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = shimmerAlpha))
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Suggestion Row
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Suggestion Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SuggestionRow(
    suggestion: String,
    query: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "suggestion_scale"
    )

    // Highlight logic
    val annotatedString = buildAnnotatedString {
        val startIndex = suggestion.indexOf(query, ignoreCase = true)
        if (startIndex >= 0) {
            withStyle(style = SpanStyle(color = Color.White.copy(0.5f), fontWeight = FontWeight.Normal)) {
                append(suggestion.substring(0, startIndex))
            }
            withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                append(suggestion.substring(startIndex, startIndex + query.length))
            }
            withStyle(style = SpanStyle(color = Color.White.copy(0.5f), fontWeight = FontWeight.Normal)) {
                append(suggestion.substring(startIndex + query.length))
            }
        } else {
            withStyle(style = SpanStyle(color = Color.White.copy(0.5f), fontWeight = FontWeight.Normal)) {
                append(suggestion)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    Brush.linearGradient(listOf(Color.White.copy(0.08f), Color.Transparent)),
                    CircleShape
                )
                .border(1.dp, Color.White.copy(0.04f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = Color.White.copy(0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = annotatedString,
            color = Color.White.copy(0.75f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Rounded.NorthWest,
            contentDescription = null,
            tint = Color.White.copy(0.15f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun FilterSuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(0.08f))
            .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
