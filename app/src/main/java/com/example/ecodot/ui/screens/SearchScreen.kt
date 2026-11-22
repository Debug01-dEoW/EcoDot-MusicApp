package com.example.ecodot.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.LocalHazeState
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.RecentSearchItem
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.components.*
import com.example.ecodot.ui.theme.*
import com.example.ecodot.ui.viewmodel.Album
import com.example.ecodot.ui.viewmodel.Artist
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay

/**
 * SimpMusic clean 4-state architecture for search:
 * - EMPTY: Clean discovery state with minimalist header and trending vibe chips.
 * - SEARCH_HISTORY: Recent searches list with "Clear all" button and quick trending vibes.
 * - SEARCH_SUGGESTIONS: User is typing; shows live YouTube suggestions with bold highlight & insert arrow.
 * - SEARCH_RESULTS: Search submitted; shows pinned filter chips, top match hero card, and full width result rows.
 */
enum class SearchUIType {
    EMPTY,
    SEARCH_HISTORY,
    SEARCH_SUGGESTIONS,
    SEARCH_RESULTS
}

@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    onWatchVideo: () -> Unit = {}
) {
    val searchResults     by viewModel.searchResults.collectAsState()
    val searchAlbums      by viewModel.searchAlbums.collectAsState()
    val searchArtists     by viewModel.searchArtists.collectAsState()
    val isSearching       by viewModel.isSearching.collectAsState()
    val searchHistory     by viewModel.searchHistory.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val searchError       by viewModel.searchError.collectAsState()
    val currentTrack      by viewModel.currentTrack.collectAsState()
    val isPlaying         by viewModel.isPlaying.collectAsState()

    var searchUIType       by rememberSaveable { mutableStateOf(SearchUIType.EMPTY) }
    var searchText         by rememberSaveable { mutableStateOf("") }
    var isSearchSubmitted  by rememberSaveable { mutableStateOf(false) }
    var isFocused          by rememberSaveable { mutableStateOf(false) }
    var selectedFilter     by rememberSaveable { mutableStateOf("All") }

    val downloadedTracks    by viewModel.downloadedTracks.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    var optionsMenuTrack    by remember { mutableStateOf<Track?>(null) }
    var longPressedAlbum    by remember { mutableStateOf<Album?>(null) }
    var longPressedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showPlaylistPicker  by remember { mutableStateOf<Track?>(null) }

    val focusManager       = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester     = remember { FocusRequester() }

    val filters = listOf("All", "Songs", "Videos", "Albums", "Artists")

    val isSearchPanelVisible = searchUIType == SearchUIType.SEARCH_HISTORY || searchUIType == SearchUIType.SEARCH_SUGGESTIONS
    val dismissSearchPanel: () -> Unit = {
        isFocused = false
        focusManager.clearFocus()
        keyboardController?.hide()
        if (searchText.isEmpty()) {
            searchUIType = SearchUIType.EMPTY
        } else if (isSearchSubmitted) {
            searchUIType = SearchUIType.SEARCH_RESULTS
        } else {
            searchUIType = SearchUIType.EMPTY
        }
    }

    // Hardware back button dismisses overlays, search panel, or search results cleanly
    BackHandler(
        enabled = optionsMenuTrack != null || longPressedAlbum != null || 
                  longPressedPlaylist != null || showPlaylistPicker != null || 
                  isSearchPanelVisible || searchUIType == SearchUIType.SEARCH_RESULTS
    ) {
        when {
            showPlaylistPicker != null -> showPlaylistPicker = null
            optionsMenuTrack != null -> optionsMenuTrack = null
            longPressedAlbum != null -> longPressedAlbum = null
            longPressedPlaylist != null -> longPressedPlaylist = null
            isSearchPanelVisible -> dismissSearchPanel()
            searchUIType == SearchUIType.SEARCH_RESULTS -> {
                searchText = ""
                isSearchSubmitted = false
                searchUIType = SearchUIType.EMPTY
                focusManager.clearFocus()
            }
        }
    }

    // Re-focus search when clicking Search button while already on Search screen (SimpMusic feature)
    LaunchedEffect(Unit) {
        viewModel.searchFocusEvent.collect {
            runCatching {
                focusRequester.requestFocus()
                keyboardController?.show()
                isFocused = true
            }
        }
    }

    fun executeSearch(query: String, filter: String = selectedFilter) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        searchText = trimmed
        isSearchSubmitted = true
        focusManager.clearFocus()
        keyboardController?.hide()
        searchUIType = SearchUIType.SEARCH_RESULTS

        // YouTube URL Parsing (SimpMusic feature): if a user pastes a direct link, resolve and play immediately
        val ytLink = extractYouTubeLinkOrNull(trimmed)
        if (ytLink != null) {
            when (ytLink.first) {
                "video" -> {
                    val dummyTrack = Track(
                        id = ytLink.second,
                        title = "YouTube Video",
                        artist = "YouTube Music",
                        album = "",
                        duration = 0L,
                        path = "https://www.youtube.com/watch?v=${ytLink.second}",
                        isYouTube = true
                    )
                    viewModel.playTrack(dummyTrack, isVideo = true)
                    return
                }
                "playlist" -> {
                    navController.navigate("album/${ytLink.second}")
                    return
                }
            }
        }

        viewModel.performSearch(trimmed, if (filter == "All") null else filter, saveToHistory = true)
    }

    // State machine updates
    LaunchedEffect(searchText, isFocused, isSearchSubmitted, searchHistory.size) {
        searchUIType = when {
            isSearchSubmitted && searchText.isNotEmpty() -> SearchUIType.SEARCH_RESULTS
            searchText.isNotEmpty() && isFocused -> SearchUIType.SEARCH_SUGGESTIONS
            searchHistory.isNotEmpty() -> SearchUIType.SEARCH_HISTORY
            else -> SearchUIType.EMPTY
        }
    }

    // Debounced query suggestions
    LaunchedEffect(searchText) {
        if (searchText.isBlank()) {
            viewModel.updateSuggestions("")
        } else if (isFocused && !isSearchSubmitted) {
            delay(120)
            viewModel.updateSuggestions(searchText)
        }
    }

    // Scroll states for content and floating header measurement
    val historyState = rememberLazyListState()
    val suggestionsState = rememberLazyListState()
    val emptyState = rememberLazyListState()
    val resultsState = rememberLazyListState()

    var searchBarHeightPx by remember { mutableIntStateOf(0) }
    val searchBarHeight = with(LocalDensity.current) { searchBarHeightPx.toDp() }

    val isContentAtTop by remember {
        derivedStateOf {
            when (searchUIType) {
                SearchUIType.EMPTY ->
                    emptyState.firstVisibleItemIndex == 0 && emptyState.firstVisibleItemScrollOffset == 0
                SearchUIType.SEARCH_HISTORY ->
                    historyState.firstVisibleItemIndex == 0 && historyState.firstVisibleItemScrollOffset == 0
                SearchUIType.SEARCH_SUGGESTIONS ->
                    suggestionsState.firstVisibleItemIndex == 0 && suggestionsState.firstVisibleItemScrollOffset == 0
                SearchUIType.SEARCH_RESULTS ->
                    resultsState.firstVisibleItemIndex == 0 && resultsState.firstVisibleItemScrollOffset == 0
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070709))
    ) {
        // ── Main Scrollable Body Content ──────────────────────────────────────
        Crossfade(
            targetState = searchUIType,
            animationSpec = tween(220),
            label = "search_state_crossfade"
        ) { state ->
            when (state) {
                SearchUIType.EMPTY -> {
                    SimpEmptySearchBody(
                        searchBarHeight = searchBarHeight,
                        listState = emptyState,
                        onVibeClick = { vibe ->
                            executeSearch(vibe)
                        }
                    )
                }

                SearchUIType.SEARCH_HISTORY -> {
                    SimpSearchHistoryBody(
                        searchBarHeight = searchBarHeight,
                        historyState = historyState,
                        history = searchHistory,
                        onHistoryClick = { item ->
                            when (item.type) {
                                "Artist" -> {
                                    viewModel.selectSearchResult(item.query, item.type, item.itemId, item.imageUrl, item.subtitle)
                                    if (!item.itemId.isNullOrEmpty()) {
                                        navController.navigate("artist/${item.itemId}")
                                    } else {
                                        executeSearch(item.query)
                                    }
                                }
                                "Album" -> {
                                    viewModel.selectSearchResult(item.query, item.type, item.itemId, item.imageUrl, item.subtitle)
                                    if (!item.itemId.isNullOrEmpty()) {
                                        navController.navigate("album/${item.itemId}")
                                    } else {
                                        executeSearch(item.query)
                                    }
                                }
                                "Song", "Video" -> {
                                    val dummyTrack = Track(
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
                                    executeSearch(item.query)
                                }
                            }
                        },
                        onInsertClick = { text ->
                            searchText = text
                            isSearchSubmitted = false
                            runCatching { focusRequester.requestFocus() }
                        },
                        onRemoveItem = { item ->
                            viewModel.removeSearchHistory(item)
                        },
                        onClearAll = {
                            viewModel.clearAllSearchHistory()
                        },
                        onVibeClick = { vibe ->
                            executeSearch(vibe)
                        }
                    )
                }

                SearchUIType.SEARCH_SUGGESTIONS -> {
                    SimpSuggestionsBody(
                        searchBarHeight = searchBarHeight,
                        suggestionsState = suggestionsState,
                        query = searchText,
                        suggestions = searchSuggestions,
                        onSuggestionClick = { suggestion ->
                            executeSearch(suggestion)
                        },
                        onInsertClick = { suggestion ->
                            searchText = suggestion
                            isSearchSubmitted = false
                            runCatching { focusRequester.requestFocus() }
                        }
                    )
                }

                SearchUIType.SEARCH_RESULTS -> {
                    SimpSearchResultsBody(
                        searchBarHeight = searchBarHeight,
                        resultsState = resultsState,
                        tracks = searchResults,
                        albums = searchAlbums,
                        artists = searchArtists,
                        selectedFilter = selectedFilter,
                        isLoading = isSearching && searchResults.isEmpty() && searchAlbums.isEmpty() && searchArtists.isEmpty(),
                        errorMessage = searchError,
                        viewModel = viewModel,
                        navController = navController,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onFilterChange = { filter ->
                            selectedFilter = filter
                            executeSearch(searchText, filter)
                        },
                        onRetry = {
                            executeSearch(searchText, selectedFilter)
                        },
                        onOptionsClick = { optionsMenuTrack = it },
                        onAlbumLongClick = { longPressedAlbum = it },
                        onVideoClick = { track ->
                            viewModel.selectSearchResult(track.title, "Video", track.id, track.albumArtUri, track.artist)
                            viewModel.playTrack(track, isVideo = true)
                            viewModel.setVideoMode(true)
                            onWatchVideo()
                        }
                    )
                }
            }
        }

        // ── Floating SimpMusic Header (Search Bar + Pinned Filter Chips) ────────
        val placeholderTexts = remember {
            listOf(
                "Search for songs...",
                "Search for artists...",
                "Search for albums...",
                "Search for playlists...",
                "Search for videos...",
                "Search for podcasts..."
            )
        }
        var currentPlaceholderIndex by remember { mutableIntStateOf(0) }
        LaunchedEffect(isFocused) {
            while (!isFocused) {
                delay(3000)
                currentPlaceholderIndex = (currentPlaceholderIndex + 1) % placeholderTexts.size
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onGloballyPositioned { searchBarHeightPx = it.size.height }
        ) {
            // Blurred backdrop that activates when content is scrolled under header
            AnimatedVisibility(
                visible = !isContentAtTop,
                modifier = Modifier.matchParentSize(),
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(200)),
                label = "search_header_blur"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF070709).copy(alpha = 0.94f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Authentic SimpMusic Liquid Glass Search Input Bar
                val tilt = LocalTiltState.current
                val searchBarShape = RoundedCornerShape(26.dp)

                val searchGlassModifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x30FFFFFF),
                                Color(0x10FFFFFF),
                                Color(0x06000000)
                            )
                        ),
                        shape = searchBarShape
                    )
                    .background(
                        color = if (isFocused) Color(0xFF1B1D2C).copy(alpha = 0.92f) else Color(0xFF11121C).copy(alpha = 0.88f),
                        shape = searchBarShape
                    )

                val searchRimBrush = remember(tilt.tiltX, tilt.tiltY, isFocused) {
                    if (isFocused) {
                        Brush.linearGradient(
                            colors = listOf(
                                EcoDotRed.copy(alpha = 0.85f),
                                EcoDotRed.copy(alpha = 0.40f),
                                Color.White.copy(alpha = 0.20f),
                                Color.White.copy(alpha = 0.05f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    } else {
                        createSpecularRimBrush(
                            tiltX = tilt.tiltX,
                            tiltY = tilt.tiltY,
                            baseAlpha = 0.45f,
                            secondaryAlpha = 0.18f
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = if (isFocused) 10.dp else 4.dp,
                            shape = searchBarShape,
                            clip = false,
                            ambientColor = Color.Black.copy(alpha = 0.45f),
                            spotColor = if (isFocused) EcoDotRed.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.5f)
                        )
                        .clip(searchBarShape)
                        .then(searchGlassModifier)
                        .border(1.dp, searchRimBrush, searchBarShape)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Leading Icon: Crossfade between Back arrow (when search panel active) and Search icon
                        Crossfade(
                            targetState = isSearchPanelVisible,
                            animationSpec = tween(200),
                            label = "search_leading_icon"
                        ) { showBack ->
                            if (showBack) {
                                IconButton(
                                    onClick = dismissSearchPanel,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Search",
                                        tint = if (isFocused) EcoDotRed else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Text Field & Animated Placeholder
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchText.isEmpty()) {
                                AnimatedContent(
                                    targetState = currentPlaceholderIndex,
                                    transitionSpec = {
                                        (fadeIn(tween(350)) + slideInVertically { it / 2 }).togetherWith(
                                            fadeOut(tween(350)) + slideOutVertically { -it / 2 }
                                        )
                                    },
                                    label = "placeholder_anim"
                                ) { index ->
                                    Text(
                                        text = placeholderTexts[index],
                                        color = Color.White.copy(alpha = 0.40f),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            BasicTextField(
                                value = searchText,
                                onValueChange = { newVal ->
                                    searchText = newVal
                                    isSearchSubmitted = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { isFocused = it.isFocused },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(EcoDotRed),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = { executeSearch(searchText) }
                                )
                            )
                        }

                        // Trailing Icon: Close ('X') to clear or GraphicEq / Recognition
                        if (searchText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchText = ""
                                    isSearchSubmitted = false
                                    viewModel.clearSearch()
                                    viewModel.updateSuggestions("")
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(Color.White.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = "Voice",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // SimpMusic Filter Chips Row (Rides inside the floating header)
                AnimatedVisibility(
                    visible = searchUIType == SearchUIType.SEARCH_RESULTS,
                    enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                    exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 10.dp, bottom = 2.dp)
                    ) {
                        filters.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            SimpChip(
                                isSelected = isSelected,
                                isAnimated = isSearching,
                                text = filter,
                                onClick = {
                                    selectedFilter = filter
                                    executeSearch(searchText, filter)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Bottom Sheets & Dialogs ──────────────────────────────────────────────
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
// 1. SimpMusic CLEAN EMPTY SEARCH BODY (Minimalist Icon/Header + Trending Vibes)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SimpEmptySearchBody(
    searchBarHeight: androidx.compose.ui.unit.Dp,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onVibeClick: (String) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = searchBarHeight + 16.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Minimalist Centered Icon & Title Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF13141D))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = EcoDotRed,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Search EcoDot",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Find your favorite songs, artists, albums, or playlists",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp
                    ),
                    color = Color.White.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Trending Vibes Clean Row
        item {
            TrendingVibesSection(
                onVibeClick = onVibeClick,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TrendingVibesSection(
    onVibeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val trendingVibes = remember {
        listOf(
            Triple("⚡ Trending Now", "Trending", Color(0xFFFF4B2B)),
            Triple("🌙 Lo-Fi Midnight", "Lo-Fi Beats", Color(0xFF8A2387)),
            Triple("🔥 Viral Hits", "Viral Hits", Color(0xFFFF416C)),
            Triple("🎧 Deep Focus", "Focus Music", Color(0xFF00B4DB)),
            Triple("🚗 Night Drive", "Night Drive Phonk Synthwave", Color(0xFF4776E6)),
            Triple("💪 Workout Energy", "Workout Motivation", Color(0xFFF12711)),
            Triple("☕ Acoustic Morning", "Acoustic Chill", Color(0xFFF7971E)),
            Triple("✨ Retro Synthwave", "Synthwave 80s", Color(0xFFE100FF))
        )
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = EcoDotRed,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = "Trending Vibes",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(trendingVibes) { (label, q, accentColor) ->
                SimpVibeChip(
                    label = label,
                    accentColor = accentColor,
                    onClick = { onVibeClick(q) }
                )
            }
        }
    }
}

@Composable
private fun SimpVibeChip(
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "vibe_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF13141D))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(accentColor.copy(alpha = 0.45f), Color.White.copy(alpha = 0.08f))
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. SimpMusic SEARCH HISTORY BODY ("Clear all" + ArrowOutward button + Trending Vibes)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SimpSearchHistoryBody(
    searchBarHeight: androidx.compose.ui.unit.Dp,
    historyState: androidx.compose.foundation.lazy.LazyListState,
    history: List<RecentSearchItem>,
    onHistoryClick: (RecentSearchItem) -> Unit,
    onInsertClick: (String) -> Unit,
    onRemoveItem: (RecentSearchItem) -> Unit,
    onClearAll: () -> Unit,
    onVibeClick: (String) -> Unit
) {
    LazyColumn(
        state = historyState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = searchBarHeight + 6.dp, bottom = 120.dp)
    ) {
        if (history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent searches",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = onClearAll,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Clear all",
                            color = EcoDotRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            items(history, key = { it.id }) { item ->
                SimpHistoryRow(
                    item = item,
                    onClick = { onHistoryClick(item) },
                    onInsertClick = { onInsertClick(item.query) },
                    onRemove = { onRemoveItem(item) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
                TrendingVibesSection(
                    onVibeClick = onVibeClick
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No search history yet",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpHistoryRow(
    item: RecentSearchItem,
    onClick: () -> Unit,
    onInsertClick: () -> Unit,
    onRemove: () -> Unit
) {
    val isArtist = item.type == "Artist"
    val isQuery = item.type == "Query"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon / Artwork thumbnail
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(if (isArtist) CircleShape else RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = if (isArtist) CircleShape else RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isQuery || item.imageUrl.isNullOrEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.45f),
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

        Spacer(Modifier.width(14.dp))

        // Title & Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.query,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val subtitle = when (item.type) {
                "Song" -> if (!item.subtitle.isNullOrEmpty()) "Song · ${item.subtitle}" else "Song"
                "Video" -> if (!item.subtitle.isNullOrEmpty()) "Video · ${item.subtitle}" else "Video"
                "Artist" -> "Artist"
                "Album" -> if (!item.subtitle.isNullOrEmpty()) "Album · ${item.subtitle}" else "Album"
                else -> null
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Insert Button (SimpMusic ArrowOutward) - puts query in text field without executing search
        IconButton(
            onClick = onInsertClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.NorthWest,
                contentDescription = "Insert query",
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(16.dp)
            )
        }

        // Remove item button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Remove",
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. SimpMusic LIVE SUGGESTIONS BODY
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SimpSuggestionsBody(
    searchBarHeight: androidx.compose.ui.unit.Dp,
    suggestionsState: androidx.compose.foundation.lazy.LazyListState,
    query: String,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    onInsertClick: (String) -> Unit
) {
    LazyColumn(
        state = suggestionsState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = searchBarHeight + 6.dp, bottom = 120.dp)
    ) {
        // Direct link card if query looks like a link
        val ytLink = extractYouTubeLinkOrNull(query)
        if (ytLink != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1F2D))
                        .border(1.dp, EcoDotRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable { onSuggestionClick(query) }
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EcoDotRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF121214),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Play YouTube Link Directly",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                            Text(
                                text = "Detected ${if (ytLink.first == "video") "Video" else "Playlist"} · Tap to play",
                                color = EcoDotRed,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        items(suggestions.take(12)) { suggestion ->
            SimpSuggestionRow(
                suggestion = suggestion,
                query = query,
                onClick = { onSuggestionClick(suggestion) },
                onInsertClick = { onInsertClick(suggestion) }
            )
        }
    }
}

@Composable
private fun SimpSuggestionRow(
    suggestion: String,
    query: String,
    onClick: () -> Unit,
    onInsertClick: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        val startIndex = suggestion.indexOf(query, ignoreCase = true)
        if (startIndex >= 0) {
            withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.45f), fontWeight = FontWeight.Normal)) {
                append(suggestion.substring(0, startIndex))
            }
            withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                append(suggestion.substring(startIndex, startIndex + query.length))
            }
            withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.45f), fontWeight = FontWeight.Normal)) {
                append(suggestion.substring(startIndex + query.length))
            }
        } else {
            withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Normal)) {
                append(suggestion)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.06f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = annotatedString,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(
            onClick = onInsertClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.NorthWest,
                contentDescription = "Fill query",
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. SimpMusic SEARCH RESULTS BODY (Spotlight Hero + Songs + Artists + Albums)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SimpSearchResultsBody(
    searchBarHeight: androidx.compose.ui.unit.Dp,
    resultsState: androidx.compose.foundation.lazy.LazyListState,
    tracks: List<Track>,
    albums: List<Album>,
    artists: List<Artist>,
    selectedFilter: String,
    isLoading: Boolean,
    errorMessage: String?,
    viewModel: MusicViewModel,
    navController: NavController,
    currentTrack: Track?,
    isPlaying: Boolean,
    onFilterChange: (String) -> Unit,
    onRetry: () -> Unit,
    onOptionsClick: (Track) -> Unit,
    onAlbumLongClick: (Album) -> Unit,
    onVideoClick: (Track) -> Unit
) {
    val networkState by viewModel.networkState.collectAsState()

    if (!networkState.isConnected && tracks.isEmpty() && albums.isEmpty() && artists.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = searchBarHeight),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(alpha = 0.04f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WifiOff,
                        contentDescription = null,
                        tint = EcoDotRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "No Internet Connection",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Check your connection to search online music. You can still play your downloaded music in Library.",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
        return
    }

    if (isLoading) {
        SimpSearchShimmer(searchBarHeight = searchBarHeight)
        return
    }

    if (errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = searchBarHeight),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = EcoDotRed.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(14.dp))
                Text("Search Failed", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(errorMessage, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoDotRed),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Retry Search", color = Color(0xFF121214), fontWeight = FontWeight.Bold)
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
        "Videos" -> !hasSongs
        "Albums" -> !hasAlbums
        "Artists" -> !hasArtists
        else -> !hasSongs && !hasAlbums && !hasArtists
    }

    if (isEmptyForFilter && !hasSongs && !hasAlbums && !hasArtists) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = searchBarHeight),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(alpha = 0.04f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SearchOff,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text("No results found", color = Color.White.copy(alpha = 0.85f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Try searching with a different term or spelling", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
            }
        }
        return
    }

    LazyColumn(
        state = resultsState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = searchBarHeight + 6.dp, bottom = 140.dp)
    ) {
        // ── Top Match Hero Spotlight ─────────────────────────────────────────
        if ((selectedFilter == "All" || selectedFilter == "Artists") && artists.isNotEmpty()) {
            val topArtist = artists.first()
            item(key = "spotlight_artist_${topArtist.id.ifEmpty { topArtist.name }}") {
                SimpTopMatchArtistCard(
                    artist = topArtist,
                    onClick = {
                        viewModel.selectSearchResult(topArtist.name, "Artist", topArtist.id, topArtist.imageUrl, null)
                        if (topArtist.id.isNotEmpty()) {
                            navController.navigate("artist/${topArtist.id}")
                        }
                    }
                )
            }
        } else if ((selectedFilter == "All" || selectedFilter == "Songs") && tracks.isNotEmpty()) {
            val topTrack = tracks.first()
            val isCurrentPlaying = isPlaying && currentTrack?.id == topTrack.id
            item(key = "spotlight_track_${topTrack.id}") {
                SimpTopMatchTrackCard(
                    track = topTrack,
                    isPlaying = isCurrentPlaying,
                    onPlayClick = {
                        viewModel.selectSearchResult(topTrack.title, "Song", topTrack.id, topTrack.albumArtUri, topTrack.artist)
                        viewModel.playTrack(topTrack)
                    },
                    onArtistClick = topTrack.artistId?.let { aid ->
                        {
                            viewModel.selectSearchResult(topTrack.artist, "Artist", aid, null, null)
                            navController.navigate("artist/$aid")
                        }
                    }
                )
            }
        }

        // ── Songs Section ────────────────────────────────────────────────────
        if (tracks.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Songs")) {
            val displaySongs = if (selectedFilter == "All") tracks.take(4) else tracks
            item {
                SimpSectionHeaderWithAction(
                    title = "Songs",
                    onSeeAll = if (selectedFilter == "All" && tracks.size > 4) { { onFilterChange("Songs") } } else null
                )
            }

            items(displaySongs, key = { track -> "song_${track.id}" }) { track ->
                val isCurrentPlaying = isPlaying && currentTrack?.id == track.id
                val isLiked by viewModel.isTrackLiked(track.id).collectAsState(initial = false)

                SimpSongResultRow(
                    track = track,
                    type = "Song",
                    isPlaying = isCurrentPlaying,
                    isLiked = isLiked,
                    onLikeToggle = { viewModel.toggleTrackLike(track) },
                    onArtistClick = track.artistId?.let { aid ->
                        {
                            viewModel.selectSearchResult(track.artist, "Artist", aid, null, null)
                            navController.navigate("artist/$aid")
                        }
                    },
                    onOptionsClick = { onOptionsClick(track) },
                    onClick = {
                        viewModel.selectSearchResult(track.title, "Song", track.id, track.albumArtUri, track.artist)
                        viewModel.playTrack(track)
                    }
                )
            }
        }

        // ── Music Videos Section (16:9 Widescreen Cards like SimpMusic) ───────
        if (tracks.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Videos")) {
            val displayVideos = if (selectedFilter == "All") tracks.take(4) else tracks
            item {
                SimpSectionHeaderWithAction(
                    title = "Music Videos",
                    onSeeAll = if (selectedFilter == "All" && tracks.size > 4) { { onFilterChange("Videos") } } else null
                )
            }

            items(displayVideos, key = { track -> "video_${track.id}" }) { track ->
                val isCurrentPlaying = isPlaying && currentTrack?.id == track.id
                SimpVideoResultCard(
                    track = track,
                    isPlaying = isCurrentPlaying,
                    onClick = {
                        viewModel.selectSearchResult(track.title, "Video", track.id, track.albumArtUri, track.artist)
                        viewModel.playTrack(track, isVideo = true)
                        onVideoClick(track)
                    }
                )
            }
        }

        // ── Albums Section ───────────────────────────────────────────────────
        if (albums.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Albums")) {
            val displayAlbums = if (selectedFilter == "All") albums.take(4) else albums
            item {
                SimpSectionHeaderWithAction(
                    title = "Albums",
                    onSeeAll = if (selectedFilter == "All" && albums.size > 4) { { onFilterChange("Albums") } } else null
                )
            }
            items(displayAlbums, key = { album -> "album_${album.id ?: album.title}" }) { album ->
                SimpAlbumResultRow(
                    album = album,
                    onLongClick = { onAlbumLongClick(album) },
                    onClick = {
                        viewModel.selectSearchResult(album.title, "Album", album.id, album.artUri, album.artist)
                        album.id?.let { navController.navigate("album/$it") }
                    }
                )
            }
        }

        // ── Artists Section ──────────────────────────────────────────────────
        if (artists.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Artists")) {
            val moreArtists = if (selectedFilter == "All") artists.drop(1).take(5) else artists
            if (moreArtists.isNotEmpty()) {
                item {
                    SimpSectionHeaderWithAction(
                        title = if (selectedFilter == "All") "More Artists" else "Artists",
                        onSeeAll = if (selectedFilter == "All" && artists.size > 6) { { onFilterChange("Artists") } } else null
                    )
                }
                items(moreArtists, key = { artist -> "artist_${artist.id.ifEmpty { artist.name }}" }) { artist ->
                    SimpArtistResultRow(
                        artist = artist,
                        onClick = {
                            viewModel.selectSearchResult(artist.name, "Artist", artist.id, artist.imageUrl, null)
                            if (artist.id.isNotEmpty()) {
                                navController.navigate("artist/${artist.id}")
                            }
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-Components & Full-Width Rows (SimpMusic Style)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SimpSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = Color.White.copy(alpha = 0.45f),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.8.sp,
        modifier = Modifier.padding(start = 18.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SimpSectionHeaderWithAction(
    title: String,
    onSeeAll: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            color = Color.White.copy(alpha = 0.50f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.6.sp
        )
        if (onSeeAll != null) {
            Text(
                text = "See all >",
                color = EcoDotRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * 16:9 Dedicated Music Video Card (SimpMusic Style).
 * Includes 16:9 widescreen thumbnail, duration pill, MV tag, channel name, and instant video player launch.
 */
@Composable
private fun SimpVideoResultCard(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    var artworkUrl by remember(track.id, track.albumArtUri) {
        mutableStateOf(track.albumArtUri)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x18FFFFFF))
            .border(
                1.dp,
                if (isPlaying) EcoDotRed.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 16:9 Thumbnail
            Box(
                modifier = Modifier
                    .width(132.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artworkUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = track.title,
                    placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.SmartDisplay),
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.SmartDisplay),
                    onError = {
                        val fallback = artworkUrl?.replace("maxresdefault", "hqdefault")
                        if (fallback != null && fallback != artworkUrl) {
                            artworkUrl = fallback
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Bottom-right duration pill badge
                val formattedDuration = remember(track.duration) {
                    val totalSec = track.duration / 1000L
                    val min = totalSec / 60
                    val sec = totalSec % 60
                    if (min > 0 || sec > 0) String.format("%02d:%02d", min, sec) else "Video"
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = formattedDuration,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Top-left MV badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(EcoDotRed.copy(alpha = 0.85f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "MV",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Video details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                Text(
                    text = track.title,
                    color = if (isPlaying) EcoDotRed else Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SmartDisplay,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = track.artist.ifEmpty { "YouTube Music" },
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Play Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) EcoDotRed else Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = if (isPlaying) Color.White else Color.White.copy(alpha = 0.90f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SimpTopMatchArtistCard(
    artist: Artist,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF13141E))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(EcoDotRed.copy(alpha = 0.4f), Color.White.copy(alpha = 0.05f))
                ),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(68.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artist.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(5.dp).background(EcoDotRed, CircleShape))
                    Text(
                        text = "TOP MATCH",
                        color = EcoDotRed,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Text(text = "•", color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp)
                    Text(
                        text = "ARTIST",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = artist.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "View artist profile →",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SimpTopMatchTrackCard(
    track: Track,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onArtistClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF13141E))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(EcoDotRed.copy(alpha = 0.4f), Color.White.copy(alpha = 0.05f))
                ),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onPlayClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1D1F2D))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(track.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayingIndicator(color = EcoDotRed)
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(5.dp).background(EcoDotRed, CircleShape))
                    Text(
                        text = "TOP MATCH",
                        color = EcoDotRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Text(text = "•", color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp)
                    Text(
                        text = "SONG",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = track.title,
                    color = if (isPlaying) EcoDotRed else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (track.isExplicit) {
                        ExplicitBadge(modifier = Modifier.padding(end = 5.dp))
                    }
                    if (onArtistClick != null) {
                        Text(
                            text = track.artist,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { onArtistClick() }
                        )
                    } else {
                        Text(
                            text = track.artist,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(EcoDotRed, CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color(0xFF121214),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun SimpSongResultRow(
    track: Track,
    type: String,
    isPlaying: Boolean,
    isLiked: Boolean,
    onLikeToggle: () -> Unit,
    onArtistClick: (() -> Unit)? = null,
    onOptionsClick: () -> Unit,
    onClick: () -> Unit
) {
    val isVideo = type == "Video"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Artwork
        Box(
            modifier = Modifier
                .size(if (isVideo) 60.dp else 48.dp, 48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF171822)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(track.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    PlayingIndicator(color = EcoDotRed)
                }
            } else if (isVideo) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title & Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isPlaying) EcoDotRed else Color.White,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 14.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (track.isExplicit) {
                    ExplicitBadge(modifier = Modifier.padding(end = 5.dp))
                }

                val durationText = if (track.duration > 0L) {
                    val sec = (track.duration / 1000) % 60
                    val min = (track.duration / 1000) / 60
                    String.format(" · %d:%02d", min, sec)
                } else ""

                if (onArtistClick != null) {
                    Text(
                        text = track.artist,
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { onArtistClick() }
                    )
                } else {
                    Text(
                        text = track.artist,
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (durationText.isNotEmpty()) {
                    Text(
                        text = durationText,
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Like button
        IconButton(
            onClick = onLikeToggle,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) EcoDotRed else Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )
        }

        // 3-dots Options
        IconButton(
            onClick = onOptionsClick,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SimpAlbumResultRow(
    album: Album,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = album.artUri,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1E202C)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Album · ${album.artist}",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SimpArtistResultRow(
    artist: Artist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = artist.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E202C))
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Artist",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Loading Shimmer Placeholder
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SimpSearchShimmer(searchBarHeight: androidx.compose.ui.unit.Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = searchBarHeight + 6.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = shimmerAlpha))
        )

        Box(
            modifier = Modifier
                .width(90.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = shimmerAlpha))
        )

        repeat(7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = shimmerAlpha))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = shimmerAlpha))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(11.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = shimmerAlpha))
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// YouTube Link Extractor Utility
// ─────────────────────────────────────────────────────────────────────────────

private fun extractYouTubeLinkOrNull(input: String): Pair<String, String>? {
    val trimmed = input.trim()
    if (!trimmed.contains("youtube.com") && !trimmed.contains("youtu.be")) return null

    val videoRegex = Regex("(?:v=|/v/|youtu\\.be/|/embed/|/watch\\?v=)([a-zA-Z0-9_-]{11})")
    videoRegex.find(trimmed)?.let {
        return Pair("video", it.groupValues[1])
    }
    val listRegex = Regex("[?&]list=([a-zA-Z0-9_-]+)")
    listRegex.find(trimmed)?.let {
        return Pair("playlist", it.groupValues[1])
    }
    return null
}
