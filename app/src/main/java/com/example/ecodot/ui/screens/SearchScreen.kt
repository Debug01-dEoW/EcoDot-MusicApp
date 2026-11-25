@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.example.ecodot.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecodot.data.local.entities.RecentSearchItem
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.remote.HomeMediaItem
import com.example.ecodot.data.remote.HomeMoodsData
import com.example.ecodot.data.remote.MoodCategoryItem
import com.example.ecodot.ui.components.AddToPlaylistDialog
import com.example.ecodot.ui.components.AlbumOptionsBottomSheet
import com.example.ecodot.ui.components.PlayingIndicator
import com.example.ecodot.ui.components.SimpChip
import com.example.ecodot.ui.components.SimpMoodCategoryCard
import com.example.ecodot.ui.components.SimpMusicChartButton
import com.example.ecodot.ui.components.SimpSearchArtistRow
import com.example.ecodot.ui.components.SimpSearchCollectionRow
import com.example.ecodot.ui.components.SimpSearchShimmer
import com.example.ecodot.ui.components.SimpSearchSongRow
import com.example.ecodot.ui.components.SimpSelectedSongsBottomSheet
import com.example.ecodot.ui.components.SimpSongSelectionTopAppBar
import com.example.ecodot.ui.components.SongRecognitionBottomSheet
import com.example.ecodot.ui.components.SwipeToQueueBox
import com.example.ecodot.ui.components.TrackOptionsBottomSheet
import com.example.ecodot.ui.components.extractYouTubeLinkOrNull
import com.example.ecodot.ui.components.homeChipStripEdgeFades
import com.example.ecodot.ui.components.homeLiquidGlass
import com.example.ecodot.ui.theme.ArrowOutward
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.viewmodel.Album
import com.example.ecodot.ui.viewmodel.Artist
import com.example.ecodot.ui.viewmodel.InstantSearchItem
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay

/**
 * SimpMusic 4-state Search architecture:
 * - EMPTY: Centered header + Charts button + 2-column Moods & Genres grid with tilted covers.
 * - SEARCH_HISTORY: Sticky "Clear search history" + past searches & insert arrows (when bar focused).
 * - SEARCH_SUGGESTIONS: Instant Artist/Song/Album/Playlist matches + highlighted autocomplete rows.
 * - SEARCH_RESULTS: Pull-to-refresh results with pinned 8-filter SimpMusic chips & multi-select mode.
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
    val context = LocalContext.current
    val searchResults by viewModel.searchResults.collectAsState()
    val searchVideos by viewModel.searchVideos.collectAsState()
    val searchAlbums by viewModel.searchAlbums.collectAsState()
    val searchArtists by viewModel.searchArtists.collectAsState()
    val searchPlaylists by viewModel.searchPlaylists.collectAsState()
    val searchFeaturedPlaylists by viewModel.searchFeaturedPlaylists.collectAsState()
    val searchPodcasts by viewModel.searchPodcasts.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val instantSearchItems by viewModel.instantSearchItems.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val likedTrackIds by viewModel.likedTrackIds.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    val homeMoodsData by viewModel.homeMoodsData.collectAsState()
    val moodArtworkMap by viewModel.moodCategoryArtwork.collectAsState()

    var searchUIType by rememberSaveable { mutableStateOf(SearchUIType.EMPTY) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var isSearchSubmitted by rememberSaveable { mutableStateOf(false) }
    var isFocused by rememberSaveable { mutableStateOf(false) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }

    var optionsMenuTrack by remember { mutableStateOf<Track?>(null) }
    var longPressedAlbum by remember { mutableStateOf<Album?>(null) }
    var showPlaylistPicker by remember { mutableStateOf<Track?>(null) }
    var showBatchPlaylistPicker by remember { mutableStateOf(false) }
    var showRecognitionSheet by remember { mutableStateOf(false) }

    // Multi-select song state (SimpMusic feature)
    var isSelectionMode by rememberSaveable { mutableStateOf(false) }
    val selectedTrackMap = remember { mutableStateMapOf<String, Track>() }
    var showSelectionSheet by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val filters = remember {
        listOf(
            "All",
            "Songs",
            "Videos",
            "Albums",
            "Artists",
            "Playlists",
            "Featured playlists",
            "Podcasts"
        )
    }

    LaunchedEffect(Unit) {
        viewModel.ensureMoodsLoaded()
    }

    val exitSelectionMode: () -> Unit = {
        isSelectionMode = false
        selectedTrackMap.clear()
        showSelectionSheet = false
    }

    val toggleTrackSelection: (Track) -> Unit = { track ->
        if (selectedTrackMap.containsKey(track.id)) {
            selectedTrackMap.remove(track.id)
            if (selectedTrackMap.isEmpty()) isSelectionMode = false
        } else {
            isSelectionMode = true
            selectedTrackMap[track.id] = track
        }
    }

    val isSearchPanelVisible =
        searchUIType == SearchUIType.SEARCH_HISTORY || searchUIType == SearchUIType.SEARCH_SUGGESTIONS

    val dismissSearchPanel: () -> Unit = {
        isExpanded = false
        isFocused = false
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    BackHandler(
        enabled = showBatchPlaylistPicker ||
            showSelectionSheet ||
            isSelectionMode ||
            showRecognitionSheet ||
            optionsMenuTrack != null ||
            longPressedAlbum != null ||
            showPlaylistPicker != null ||
            isSearchPanelVisible ||
            searchUIType == SearchUIType.SEARCH_RESULTS
    ) {
        when {
            showBatchPlaylistPicker -> showBatchPlaylistPicker = false
            showSelectionSheet -> showSelectionSheet = false
            isSelectionMode -> exitSelectionMode()
            showRecognitionSheet -> showRecognitionSheet = false
            showPlaylistPicker != null -> showPlaylistPicker = null
            optionsMenuTrack != null -> optionsMenuTrack = null
            longPressedAlbum != null -> longPressedAlbum = null
            isSearchPanelVisible -> dismissSearchPanel()
            searchUIType == SearchUIType.SEARCH_RESULTS -> {
                searchText = ""
                isSearchSubmitted = false
                isExpanded = false
                selectedFilter = "All"
                viewModel.clearSearch()
                focusManager.clearFocus()
            }
        }
    }

    // Re-focus Search input when tapping Search icon while already on Search screen
    LaunchedEffect(Unit) {
        viewModel.searchFocusEvent.collect {
            if (!isSelectionMode) {
                isExpanded = true
                runCatching {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                    isFocused = true
                }
            }
        }
    }

    fun executeSearch(
        query: String,
        filter: String = selectedFilter,
        forceRefresh: Boolean = false
    ) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        searchText = trimmed
        isSearchSubmitted = true
        isExpanded = false
        isFocused = false
        exitSelectionMode()
        focusManager.clearFocus()
        keyboardController?.hide()

        val ytLink = extractYouTubeLinkOrNull(trimmed)
        if (ytLink != null) {
            when (ytLink.first) {
                "video" -> {
                    val directTrack = Track(
                        id = ytLink.second,
                        title = "YouTube Video",
                        artist = "YouTube Music",
                        album = "YouTube Video",
                        duration = 0L,
                        path = "ytvideo://${ytLink.second}",
                        isYouTube = true
                    )
                    viewModel.playTrack(directTrack, isVideo = true)
                    onWatchVideo()
                    return
                }
                "playlist" -> {
                    navController.navigate("album/${ytLink.second}")
                    return
                }
                "artist" -> {
                    navController.navigate("artist/${ytLink.second}")
                    return
                }
            }
        }

        viewModel.performSearch(
            query = trimmed,
            type = if (filter == "All") null else filter,
            saveToHistory = !forceRefresh,
            forceRefresh = forceRefresh
        )
    }

    LaunchedEffect(isExpanded, searchText, isFocused, isSearchSubmitted) {
        searchUIType = when {
            searchText.isNotEmpty() && isExpanded -> SearchUIType.SEARCH_SUGGESTIONS
            isFocused && isExpanded -> SearchUIType.SEARCH_HISTORY
            searchText.isEmpty() -> SearchUIType.EMPTY
            isSearchSubmitted -> SearchUIType.SEARCH_RESULTS
            else -> SearchUIType.EMPTY
        }
    }

    LaunchedEffect(searchText, isExpanded) {
        if (searchText.isBlank()) {
            viewModel.updateSuggestions("")
        } else if (isExpanded) {
            delay(120)
            viewModel.updateSuggestions(searchText)
        }
    }

    // Hoisted scroll states & sibling backdrop/haze sources
    val moodGridState = rememberLazyGridState()
    val historyState = rememberLazyListState()
    val suggestionsState = rememberLazyListState()
    val resultsState = rememberLazyListState()
    val chipRowScrollState = rememberScrollState()

    val searchChipBackdrop = rememberLayerBackdrop()
    val searchHazeState = remember { HazeState() }
    val density = LocalDensity.current
    var searchBarHeightPx by remember { mutableIntStateOf(0) }
    val searchBarHeight = with(density) { searchBarHeightPx.toDp() }.coerceAtLeast(68.dp)

    val isContentAtTop by remember {
        derivedStateOf {
            when (searchUIType) {
                SearchUIType.EMPTY ->
                    moodGridState.firstVisibleItemIndex == 0 && moodGridState.firstVisibleItemScrollOffset == 0
                SearchUIType.SEARCH_HISTORY ->
                    historyState.firstVisibleItemIndex == 0 && historyState.firstVisibleItemScrollOffset == 0
                SearchUIType.SEARCH_SUGGESTIONS ->
                    suggestionsState.firstVisibleItemIndex == 0 && suggestionsState.firstVisibleItemScrollOffset == 0
                SearchUIType.SEARCH_RESULTS ->
                    resultsState.firstVisibleItemIndex == 0 && resultsState.firstVisibleItemScrollOffset == 0
            }
        }
    }

    val downloadedIds = remember(downloadedTracks) { downloadedTracks.map { it.id }.toSet() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070709))
    ) {
        // ── 1. Scrollable Body (Sibling source for Haze & Kyant Backdrop) ──────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(searchChipBackdrop)
                .hazeSource(searchHazeState)
        ) {
            // Subtle SimpMusic violet/crimson ambient glow at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF391D48).copy(alpha = 0.42f),
                                Color(0xFF191529).copy(alpha = 0.24f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Crossfade(
                targetState = searchUIType,
                animationSpec = tween(220),
                label = "search_state_crossfade"
            ) { state ->
                when (state) {
                    SearchUIType.EMPTY -> {
                        SimpEmptyMoodsGridBody(
                            searchBarHeight = searchBarHeight,
                            gridState = moodGridState,
                            moodsData = homeMoodsData,
                            artworkMap = moodArtworkMap,
                            onLoadArtwork = { viewModel.loadMoodCategoryArtwork(it) },
                            onChartClick = {
                                viewModel.openChartBrowsePage()
                                navController.navigate("browse")
                            },
                            onCategoryClick = { item ->
                                viewModel.openBrowsePage(
                                    title = item.title,
                                    strapline = "MOOD OR GENRE",
                                    browseId = item.browseId,
                                    params = item.params,
                                    fallbackQuery = "${item.title} hits"
                                )
                                navController.navigate("browse")
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
                                    "Album", "Playlist" -> {
                                        viewModel.selectSearchResult(item.query, item.type, item.itemId, item.imageUrl, item.subtitle)
                                        if (!item.itemId.isNullOrEmpty()) {
                                            navController.navigate("album/${item.itemId}")
                                        } else {
                                            executeSearch(item.query)
                                        }
                                    }
                                    "Song", "Video" -> {
                                        val isVideoItem = item.type == "Video"
                                        val vidId = item.itemId ?: ""
                                        val track = Track(
                                            id = vidId,
                                            title = item.query,
                                            artist = item.subtitle ?: "",
                                            album = if (isVideoItem) "YouTube Video" else "",
                                            duration = 0L,
                                            path = if (isVideoItem && vidId.isNotBlank()) "ytvideo://$vidId" else "https://www.youtube.com/watch?v=$vidId",
                                            albumArtUri = item.imageUrl,
                                            isYouTube = vidId.isNotBlank()
                                        )
                                        viewModel.selectSearchResult(item.query, item.type, item.itemId, item.imageUrl, item.subtitle)
                                        viewModel.playTrack(track, isVideo = isVideoItem)
                                        if (isVideoItem) onWatchVideo()
                                    }
                                    else -> executeSearch(item.query)
                                }
                            },
                            onInsertClick = { text ->
                                searchText = text
                                isSearchSubmitted = false
                                isExpanded = true
                                runCatching { focusRequester.requestFocus() }
                            },
                            onRemoveItem = { viewModel.removeSearchHistory(it) },
                            onClearAll = { viewModel.clearAllSearchHistory() }
                        )
                    }

                    SearchUIType.SEARCH_SUGGESTIONS -> {
                        SimpSuggestionsBody(
                            searchBarHeight = searchBarHeight,
                            suggestionsState = suggestionsState,
                            query = searchText,
                            instantItems = instantSearchItems,
                            suggestions = searchSuggestions,
                            currentTrackId = currentTrack?.id,
                            isPlaying = isPlaying,
                            onTrackClick = { track ->
                                viewModel.selectSearchResult(track.title, "Song", track.id, track.albumArtUri, track.artist)
                                viewModel.playTrack(track)
                                dismissSearchPanel()
                            },
                            onArtistClick = { id, name, thumb ->
                                viewModel.selectSearchResult(name, "Artist", id, thumb, "Artist")
                                navController.navigate("artist/$id")
                            },
                            onAlbumClick = { id, title, artist, art ->
                                viewModel.selectSearchResult(title, "Album", id, art, artist)
                                if (!id.isNullOrEmpty()) navController.navigate("album/$id")
                                else executeSearch(title)
                            },
                            onPlaylistClick = { pl ->
                                viewModel.selectSearchResult(pl.title, "Playlist", pl.id, pl.thumbnailUrl, pl.subtitle)
                                navController.navigate("album/${pl.id}")
                            },
                            onSuggestionClick = { suggestion ->
                                executeSearch(suggestion)
                            },
                            onInsertClick = { suggestion ->
                                searchText = suggestion
                                isSearchSubmitted = false
                                isExpanded = true
                                runCatching { focusRequester.requestFocus() }
                            },
                            onAddToQueue = { viewModel.addToQueue(it) }
                        )
                    }

                    SearchUIType.SEARCH_RESULTS -> {
                        val pullToRefreshState = rememberPullToRefreshState()
                        PullToRefreshBox(
                            modifier = Modifier.fillMaxSize(),
                            state = pullToRefreshState,
                            isRefreshing = isSearching,
                            onRefresh = {
                                executeSearch(searchText, selectedFilter, forceRefresh = true)
                            },
                            indicator = {
                                PullToRefreshDefaults.Indicator(
                                    state = pullToRefreshState,
                                    isRefreshing = isSearching,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = searchBarHeight + 4.dp),
                                    containerColor = Color(0xFF1F1B2B),
                                    color = Color(0xFFF0E3FF)
                                )
                            }
                        ) {
                            SimpSearchResultsBody(
                                searchBarHeight = searchBarHeight,
                                resultsState = resultsState,
                                tracks = searchResults,
                                videos = searchVideos,
                                albums = searchAlbums,
                                artists = searchArtists,
                                playlists = searchPlaylists,
                                featuredPlaylists = searchFeaturedPlaylists,
                                podcasts = searchPodcasts,
                                selectedFilter = selectedFilter,
                                isLoading = isSearching &&
                                    searchResults.isEmpty() &&
                                    searchVideos.isEmpty() &&
                                    searchAlbums.isEmpty() &&
                                    searchArtists.isEmpty() &&
                                    searchPlaylists.isEmpty() &&
                                    searchFeaturedPlaylists.isEmpty() &&
                                    searchPodcasts.isEmpty(),
                                errorMessage = searchError,
                                viewModel = viewModel,
                                navController = navController,
                                currentTrack = currentTrack,
                                isPlaying = isPlaying,
                                downloadedIds = downloadedIds,
                                likedTrackIds = likedTrackIds,
                                selectionMode = isSelectionMode,
                                selectedTrackIds = selectedTrackMap.keys,
                                onToggleTrackSelection = toggleTrackSelection,
                                onFilterChange = { filter ->
                                    selectedFilter = filter
                                    val needsFetch = when (filter) {
                                        "Songs" -> searchResults.isEmpty()
                                        "Videos" -> searchVideos.isEmpty()
                                        "Albums" -> searchAlbums.isEmpty()
                                        "Artists" -> searchArtists.isEmpty()
                                        "Playlists" -> searchPlaylists.isEmpty()
                                        "Featured playlists" -> searchFeaturedPlaylists.isEmpty()
                                        "Podcasts" -> searchPodcasts.isEmpty()
                                        else -> false
                                    }
                                    if (needsFetch) {
                                        executeSearch(searchText, filter)
                                    }
                                },
                                onRetry = {
                                    executeSearch(searchText, selectedFilter, forceRefresh = true)
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
            }
        }

        // ── 2. Floating Sibling SimpMusic Header (Liquid Glass Bar + Filter Chips) ─
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
            AnimatedVisibility(
                visible = !isContentAtTop,
                modifier = Modifier.matchParentSize(),
                enter = fadeIn(tween(240)),
                exit = fadeOut(tween(180)),
                label = "search_header_haze"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeEffect(
                            state = searchHazeState,
                            style = HazeStyle(
                                backgroundColor = Color(0xFF0A0B10).copy(alpha = 0.74f),
                                tint = HazeDefaults.tint(Color(0xFF0A0B10).copy(alpha = 0.45f)),
                                blurRadius = 22.dp,
                                noiseFactor = 0.02f
                            )
                        )
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF070709).copy(alpha = 0.58f),
                                    Color(0xFF070709).copy(alpha = 0.14f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp, bottom = 6.dp)
            ) {
                // Multi-select TopAppBar (replaces / stacks over bar during song selection)
                AnimatedVisibility(visible = isSelectionMode) {
                    SimpSongSelectionTopAppBar(
                        selectedCount = selectedTrackMap.size,
                        onExit = exitSelectionMode,
                        onSelectAll = {
                            val visibleTracks = when (selectedFilter) {
                                "Videos" -> searchVideos
                                "All", "Songs" -> searchResults + searchVideos
                                else -> searchResults
                            }.distinctBy { it.id }
                            if (visibleTracks.isNotEmpty() && visibleTracks.all { selectedTrackMap.containsKey(it.id) }) {
                                selectedTrackMap.clear()
                            } else {
                                visibleTracks.take(50).forEach { selectedTrackMap[it.id] = it }
                            }
                        },
                        onOpenActions = { showSelectionSheet = true }
                    )
                }

                // Liquid Glass Floating Search Input Field
                if (!isSelectionMode) {
                    val barShape = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(52.dp)
                            .homeLiquidGlass(
                                backdrop = searchChipBackdrop,
                                shape = barShape,
                                surfaceTint = if (isFocused) Color(0xFF1F1D2C) else Color(0xFF151622),
                                surfaceAlpha = if (isFocused) 0.72f else 0.56f,
                                specularAlpha = if (isFocused) 0.92f else 0.76f,
                                frostSheenAlpha = 0.18f,
                                blurDp = 12.dp,
                                isPressed = false
                            )
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                            tint = if (isFocused) Color(0xFFF0E3FF) else Color.White.copy(alpha = 0.75f),
                                            modifier = Modifier.size(21.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchText.isEmpty()) {
                                    AnimatedContent(
                                        targetState = currentPlaceholderIndex,
                                        transitionSpec = {
                                            (fadeIn(tween(380)) + slideInVertically { it }).togetherWith(
                                                fadeOut(tween(380)) + slideOutVertically { -it }
                                            )
                                        },
                                        label = "placeholder_anim"
                                    ) { index ->
                                        Text(
                                            text = placeholderTexts[index],
                                            color = Color.White.copy(alpha = 0.50f),
                                            fontSize = 15.sp,
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
                                        isExpanded = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .onFocusChanged { state ->
                                            isFocused = state.isFocused
                                            if (state.isFocused) {
                                                isExpanded = true
                                            }
                                        },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(Color(0xFFF0E3FF)),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = { executeSearch(searchText) }
                                    )
                                )
                            }

                            if (searchText.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        searchText = ""
                                        isSearchSubmitted = false
                                        isExpanded = true
                                        viewModel.clearSearch()
                                        viewModel.updateSuggestions("")
                                        runCatching { focusRequester.requestFocus() }
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(Color.White.copy(alpha = 0.14f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Clear search",
                                            tint = Color.White.copy(alpha = 0.88f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            } else {
                                IconButton(
                                    onClick = { showRecognitionSheet = true },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = "Recognize song",
                                        tint = Color(0xFFF0E3FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // SimpMusic 8 Filter Chips Row inside floating header
                AnimatedVisibility(
                    visible = searchUIType == SearchUIType.SEARCH_RESULTS,
                    enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                    exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .homeChipStripEdgeFades(
                                width = 20.dp,
                                start = { if (chipRowScrollState.canScrollBackward) 1f else 0f },
                                end = { if (chipRowScrollState.canScrollForward) 1f else 0f }
                            )
                            .horizontalScroll(chipRowScrollState)
                            .padding(horizontal = 16.dp)
                    ) {
                        filters.forEach { filter ->
                            SimpChip(
                                isSelected = selectedFilter == filter,
                                isAnimated = isSearching,
                                text = filter,
                                backdrop = searchChipBackdrop,
                                onClick = {
                                    selectedFilter = filter
                                    val needsFetch = when (filter) {
                                        "Songs" -> searchResults.isEmpty()
                                        "Videos" -> searchVideos.isEmpty()
                                        "Albums" -> searchAlbums.isEmpty()
                                        "Artists" -> searchArtists.isEmpty()
                                        "Playlists" -> searchPlaylists.isEmpty()
                                        "Featured playlists" -> searchFeaturedPlaylists.isEmpty()
                                        "Podcasts" -> searchPodcasts.isEmpty()
                                        else -> searchResults.isEmpty() && searchAlbums.isEmpty()
                                    }
                                    if (needsFetch) {
                                        executeSearch(searchText, filter)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── 3. Sheets & Overlays ──────────────────────────────────────────────────
    if (showSelectionSheet) {
        val pickedTracks = selectedTrackMap.values.toList()
        SimpSelectedSongsBottomSheet(
            selectedCount = pickedTracks.size,
            onDismiss = { showSelectionSheet = false },
            onPlayNext = {
                pickedTracks.asReversed().forEach { viewModel.playNext(it) }
                Toast.makeText(context, "Playing ${pickedTracks.size} songs next", Toast.LENGTH_SHORT).show()
                exitSelectionMode()
            },
            onAddToQueue = {
                pickedTracks.forEach { viewModel.addToQueue(it) }
                Toast.makeText(context, "Added ${pickedTracks.size} songs to queue", Toast.LENGTH_SHORT).show()
                exitSelectionMode()
            },
            onAddToPlaylist = { showBatchPlaylistPicker = true },
            onDownload = {
                pickedTracks.forEach { viewModel.downloadTrack(it) }
                Toast.makeText(context, "Downloading ${pickedTracks.size} songs", Toast.LENGTH_SHORT).show()
                exitSelectionMode()
            },
            onAddToFavorites = {
                pickedTracks.forEach { track ->
                    if (track.id !in likedTrackIds) {
                        viewModel.toggleTrackLike(track)
                    }
                }
                Toast.makeText(context, "Added ${pickedTracks.size} songs to Favorites", Toast.LENGTH_SHORT).show()
                exitSelectionMode()
            }
        )
    }

    if (showBatchPlaylistPicker && selectedTrackMap.isNotEmpty()) {
        val playlists by viewModel.allPlaylists.collectAsState()
        val pickedTracks = selectedTrackMap.values.toList()
        AddToPlaylistDialog(
            track = pickedTracks.first(),
            playlists = playlists,
            onDismiss = { showBatchPlaylistPicker = false },
            onPlaylistSelected = { playlist ->
                pickedTracks.forEach { viewModel.addTrackToPlaylist(playlist.id, it) }
                showBatchPlaylistPicker = false
                exitSelectionMode()
                Toast.makeText(context, "Added to ${playlist.name}", Toast.LENGTH_SHORT).show()
            },
            onCreatePlaylist = { name -> viewModel.createPlaylist(name) }
        )
    }

    optionsMenuTrack?.let { track ->
        val isDownloaded = track.id in downloadedIds
        val downloadProgress = downloadProgressMap[track.id]
        val isLiked = track.id in likedTrackIds

        TrackOptionsBottomSheet(
            track = track,
            onDismiss = { optionsMenuTrack = null },
            onPlayNext = { viewModel.playNext(track) },
            onAddToQueue = { viewModel.addToQueue(track) },
            onGoToAlbum = {
                optionsMenuTrack = null
                if (!track.albumId.isNullOrEmpty()) {
                    navController.navigate("album/${track.albumId}")
                } else if (track.album.isNotBlank()) {
                    executeSearch(track.album, "Albums")
                }
            },
            onGoToArtist = {
                optionsMenuTrack = null
                if (!track.artistId.isNullOrEmpty()) {
                    navController.navigate("artist/${track.artistId}")
                } else if (track.artist.isNotBlank()) {
                    executeSearch(track.artist, "Artists")
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
                if (isSaved) viewModel.unsaveAlbum(album.tracks) else viewModel.saveAlbum(album.tracks)
            },
            onGoToArtist = {
                longPressedAlbum = null
                val artistTrack = album.tracks.firstOrNull()
                if (artistTrack != null && !artistTrack.artistId.isNullOrEmpty()) {
                    navController.navigate("artist/${artistTrack.artistId}")
                } else {
                    executeSearch(album.artist, "Artists")
                }
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
            onCreatePlaylist = { name -> viewModel.createPlaylist(name) }
        )
    }

    if (showRecognitionSheet) {
        SongRecognitionBottomSheet(
            onFind = { recognizedSong ->
                searchText = recognizedSong
                executeSearch(recognizedSong, "All")
            },
            onDismiss = { showRecognitionSheet = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. SimpMusic EMPTY STATE: "Everything you need" + Chart Pill + Moods & Genres
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SimpEmptyMoodsGridBody(
    searchBarHeight: Dp,
    gridState: LazyGridState,
    moodsData: HomeMoodsData,
    artworkMap: Map<String, String>,
    onLoadArtwork: (MoodCategoryItem) -> Unit,
    onChartClick: () -> Unit,
    onCategoryClick: (MoodCategoryItem) -> Unit
) {
    val fallbackMoods = remember {
        listOf(
            MoodCategoryItem("Chill", "ggM8SgQIBxADSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB", stripeColor = 0xFF71B280),
            MoodCategoryItem("Feel good", "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBADSgQIBBABSgQIDhABSgQIAxABSgQIBhAB", stripeColor = 0xFFE94057),
            MoodCategoryItem("Focus", "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAD", stripeColor = 0xFF00B4DB),
            MoodCategoryItem("Workout", "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBADSgQIDhABSgQIAxABSgQIBhAB", stripeColor = 0xFFFF4B2B),
            MoodCategoryItem("Party", "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhADSgQIAxABSgQIBhAB", stripeColor = 0xFF8E2DE2),
            MoodCategoryItem("Romance", "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRADSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB", stripeColor = 0xFFE100FF),
            MoodCategoryItem("Energize", "ggM8SgQIBxABSgQIBRABSgQICRADSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB", stripeColor = 0xFFF7971E),
            MoodCategoryItem("Sleep", "ggM8SgQIBxABSgQIBRADSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB", stripeColor = 0xFF396AFC)
        )
    }

    val moodsSection = moodsData.moodsAndMoments.ifEmpty { fallbackMoods }
    val genresSection = moodsData.genres

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = searchBarHeight + 8.dp, bottom = 152.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "empty_search_hero") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Everything you need",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Search for songs, artists, albums, playlists, and more",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = Color.White.copy(alpha = 0.62f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                SimpMusicChartButton(
                    modifier = Modifier.padding(top = 14.dp),
                    onClick = onChartClick
                )
            }
        }

        items(
            items = moodsSection,
            key = { item -> "moods/${item.title}/${item.params}" }
        ) { item ->
            LaunchedEffect(item.params) {
                onLoadArtwork(item)
            }
            SimpMoodCategoryCard(
                title = item.title,
                artworkUrl = artworkMap[item.params],
                onClick = { onCategoryClick(item) }
            )
        }

        if (genresSection.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "genres_section_header") {
                Text(
                    text = "Genres",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            items(
                items = genresSection,
                key = { item -> "genres/${item.title}/${item.params}" }
            ) { item ->
                LaunchedEffect(item.params) {
                    onLoadArtwork(item)
                }
                SimpMoodCategoryCard(
                    title = item.title,
                    artworkUrl = artworkMap[item.params],
                    onClick = { onCategoryClick(item) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. SimpMusic SEARCH HISTORY
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SimpSearchHistoryBody(
    searchBarHeight: Dp,
    historyState: LazyListState,
    history: List<RecentSearchItem>,
    onHistoryClick: (RecentSearchItem) -> Unit,
    onInsertClick: (String) -> Unit,
    onRemoveItem: (RecentSearchItem) -> Unit,
    onClearAll: () -> Unit
) {
    LazyColumn(
        state = historyState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = searchBarHeight + 4.dp, bottom = 148.dp)
    ) {
        if (history.isNotEmpty()) {
            stickyHeader(key = "clear_history_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF070709).copy(alpha = 0.90f))
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent searches",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(
                        onClick = onClearAll,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Clear search history",
                            color = Color(0xFFF0E3FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
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
        } else {
            item(key = "history_empty_placeholder") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.30f),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No search history yet",
                            color = Color.White.copy(alpha = 0.50f),
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
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(if (isArtist) CircleShape else RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            if (isQuery || item.imageUrl.isNullOrEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.55f),
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.query,
                color = Color.White.copy(alpha = 0.94f),
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
                "Playlist" -> if (!item.subtitle.isNullOrEmpty()) "Playlist · ${item.subtitle}" else "Playlist"
                else -> null
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.48f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(onClick = onInsertClick, modifier = Modifier.size(34.dp)) {
            Icon(
                imageVector = SimpIcons.ArrowOutward,
                contentDescription = "Insert suggestion",
                tint = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
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
// 3. SimpMusic LIVE SUGGESTIONS + INSTANT ITEMS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SimpSuggestionsBody(
    searchBarHeight: Dp,
    suggestionsState: LazyListState,
    query: String,
    instantItems: List<InstantSearchItem>,
    suggestions: List<String>,
    currentTrackId: String?,
    isPlaying: Boolean,
    onTrackClick: (Track) -> Unit,
    onArtistClick: (id: String, name: String, thumb: String?) -> Unit,
    onAlbumClick: (id: String?, title: String, artist: String, art: String?) -> Unit,
    onPlaylistClick: (InstantSearchItem.PlaylistMatch) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onInsertClick: (String) -> Unit,
    onAddToQueue: (Track) -> Unit
) {
    LazyColumn(
        state = suggestionsState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = searchBarHeight + 6.dp, bottom = 148.dp)
    ) {
        val ytLink = extractYouTubeLinkOrNull(query)
        if (ytLink != null) {
            item(key = "yt_direct_link") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1F1B2B))
                        .border(1.dp, Color(0xFFF0E3FF).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
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
                                .background(Color(0xFFF0E3FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF1A102B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Open YouTube Link Directly",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                            Text(
                                text = "Detected ${ytLink.first.replaceFirstChar { it.uppercase() }} · Tap to open",
                                color = Color(0xFFF0E3FF),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        items(instantItems) { item ->
            when (item) {
                is InstantSearchItem.ArtistMatch -> {
                    InstantMediaRow(
                        title = item.name,
                        subtitle = "Artist",
                        imageUrl = item.thumbnailUrl,
                        isCircular = true,
                        onClick = { onArtistClick(item.id, item.name, item.thumbnailUrl) }
                    )
                }
                is InstantSearchItem.SongMatch -> {
                    val isTrackPlaying = isPlaying && currentTrackId == item.track.id
                    SwipeToQueueBox(onAddToQueue = { onAddToQueue(item.track) }) {
                        InstantMediaRow(
                            title = item.track.title,
                            subtitle = item.track.artist.ifBlank { "Song" },
                            imageUrl = item.track.albumArtUri,
                            isCircular = false,
                            isPlaying = isTrackPlaying,
                            trailingIcon = Icons.Rounded.Add,
                            onTrailingClick = { onAddToQueue(item.track) },
                            onClick = { onTrackClick(item.track) }
                        )
                    }
                }
                is InstantSearchItem.AlbumMatch -> {
                    InstantMediaRow(
                        title = item.title,
                        subtitle = if (item.artist.isNotBlank()) "Album · ${item.artist}" else "Album",
                        imageUrl = item.artUri,
                        isCircular = false,
                        onClick = { onAlbumClick(item.id, item.title, item.artist, item.artUri) }
                    )
                }
                is InstantSearchItem.PlaylistMatch -> {
                    InstantMediaRow(
                        title = item.title,
                        subtitle = item.subtitle.ifBlank { "Playlist" },
                        imageUrl = item.thumbnailUrl,
                        isCircular = false,
                        onClick = { onPlaylistClick(item) }
                    )
                }
            }
        }

        if (instantItems.isNotEmpty() && suggestions.isNotEmpty()) {
            item(key = "suggestions_divider_space") {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        items(suggestions, key = { "qsug_$it" }) { suggestion ->
            InstantQueryRow(
                query = query,
                suggestion = suggestion,
                onClick = { onSuggestionClick(suggestion) },
                onInsertClick = { onInsertClick(suggestion) }
            )
        }
    }
}

@Composable
private fun InstantMediaRow(
    title: String,
    subtitle: String,
    imageUrl: String?,
    isCircular: Boolean,
    isPlaying: Boolean = false,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(if (isCircular) CircleShape else RoundedCornerShape(8.dp))
                .background(Color(0xFF1E202E)),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = if (isCircular) Icons.Rounded.Person else Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    PlayingIndicator(color = Color(0xFFF0E3FF))
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isPlaying) Color(0xFFF0E3FF) else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (trailingIcon != null && onTrailingClick != null) {
            IconButton(onClick = onTrailingClick, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = "Add to queue",
                    tint = Color.White.copy(alpha = 0.72f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun InstantQueryRow(
    query: String,
    suggestion: String,
    onClick: () -> Unit,
    onInsertClick: () -> Unit
) {
    val trimmedQuery = query.trim()
    val matchIdx = if (trimmedQuery.isNotEmpty()) suggestion.indexOf(trimmedQuery, ignoreCase = true) else -1
    val annotated = buildAnnotatedString {
        if (matchIdx >= 0) {
            append(suggestion.substring(0, matchIdx))
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                append(suggestion.substring(matchIdx, matchIdx + trimmedQuery.length))
            }
            append(suggestion.substring(matchIdx + trimmedQuery.length))
        } else {
            append(suggestion)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = annotated,
            color = Color.White.copy(alpha = 0.76f),
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onInsertClick, modifier = Modifier.size(34.dp)) {
            Icon(
                imageVector = SimpIcons.ArrowOutward,
                contentDescription = "Insert query",
                tint = Color.White.copy(alpha = 0.68f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. SimpMusic SEARCH RESULTS (All 8 Categories + Offline/Error/Shimmer)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SimpSearchResultsBody(
    searchBarHeight: Dp,
    resultsState: LazyListState,
    tracks: List<Track>,
    videos: List<Track>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<HomeMediaItem>,
    featuredPlaylists: List<HomeMediaItem>,
    podcasts: List<HomeMediaItem>,
    selectedFilter: String,
    isLoading: Boolean,
    errorMessage: String?,
    viewModel: MusicViewModel,
    navController: NavController,
    currentTrack: Track?,
    isPlaying: Boolean,
    downloadedIds: Set<String>,
    likedTrackIds: Set<String>,
    selectionMode: Boolean,
    selectedTrackIds: Set<String>,
    onToggleTrackSelection: (Track) -> Unit,
    onFilterChange: (String) -> Unit,
    onRetry: () -> Unit,
    onOptionsClick: (Track) -> Unit,
    onAlbumLongClick: (Album) -> Unit,
    onVideoClick: (Track) -> Unit
) {
    val networkState by viewModel.networkState.collectAsState()

    val anyData = tracks.isNotEmpty() || videos.isNotEmpty() || albums.isNotEmpty() ||
        artists.isNotEmpty() || playlists.isNotEmpty() ||
        featuredPlaylists.isNotEmpty() || podcasts.isNotEmpty()

    if (!networkState.isConnected && !anyData) {
        SearchCenteredMessage(
            searchBarHeight = searchBarHeight,
            icon = Icons.Rounded.WifiOff,
            title = "No Internet Connection",
            subtitle = "Check your connection to search online music. Downloaded music remains available in Library."
        )
        return
    }

    if (isLoading) {
        SimpSearchShimmer(searchBarHeight = searchBarHeight)
        return
    }

    if (errorMessage != null && !anyData) {
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
                    tint = EcoDotRed.copy(alpha = 0.72f)
                )
                Spacer(Modifier.height(14.dp))
                Text("An error occurred", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(errorMessage, color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0E3FF)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Retry", color = Color(0xFF1A102B), fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val isEmptyForFilter = when (selectedFilter) {
        "Songs" -> tracks.isEmpty()
        "Videos" -> videos.isEmpty()
        "Albums" -> albums.isEmpty()
        "Artists" -> artists.isEmpty()
        "Playlists" -> playlists.isEmpty()
        "Featured playlists" -> featuredPlaylists.isEmpty()
        "Podcasts" -> podcasts.isEmpty()
        else -> !anyData
    }

    if (isEmptyForFilter) {
        SearchCenteredMessage(
            searchBarHeight = searchBarHeight,
            icon = Icons.Rounded.SearchOff,
            title = "No results found",
            subtitle = "Try another keyword or switch filter tab"
        )
        return
    }

    LazyColumn(
        state = resultsState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = searchBarHeight + 6.dp, bottom = 152.dp)
    ) {
        // 1. Top Artists (matching SimpMusic SearchAll placement of up to 3 top artists)
        if ((selectedFilter == "All" || selectedFilter == "Artists") && artists.isNotEmpty()) {
            val topArtists = if (selectedFilter == "All") artists.take(3) else artists
            if (selectedFilter == "All") {
                item(key = "hdr_artists_top") {
                    SearchCategoryHeader("Artists", if (artists.size > 3) ({ onFilterChange("Artists") }) else null)
                }
            }
            items(
                items = topArtists,
                key = { a -> "art_${a.id.ifEmpty { a.name }}" }
            ) { artist ->
                SimpSearchArtistRow(
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

        // 2. Songs (with Swipe-to-Queue, Long-Press Multi-Select, Marquee, Explicit & Download badges)
        if ((selectedFilter == "All" || selectedFilter == "Songs") && tracks.isNotEmpty()) {
            val displaySongs = if (selectedFilter == "All") tracks.take(10) else tracks
            if (selectedFilter == "All") {
                item(key = "hdr_songs") {
                    SearchCategoryHeader("Songs", if (tracks.size > 10) ({ onFilterChange("Songs") }) else null)
                }
            }
            items(
                items = displaySongs,
                key = { t -> "song_${t.id}" }
            ) { track ->
                SimpSearchSongRow(
                    track = track,
                    isVideo = false,
                    isPlaying = isPlaying && currentTrack?.id == track.id,
                    isDownloaded = track.id in downloadedIds,
                    isLiked = track.id in likedTrackIds,
                    selectionMode = selectionMode,
                    isSelected = track.id in selectedTrackIds,
                    onSelectToggle = { onToggleTrackSelection(track) },
                    onLongClick = { onToggleTrackSelection(track) },
                    onLikeToggle = { viewModel.toggleTrackLike(track) },
                    onArtistClick = track.artistId?.let { aid ->
                        {
                            viewModel.selectSearchResult(track.artist, "Artist", aid, null, null)
                            navController.navigate("artist/$aid")
                        }
                    },
                    onOptionsClick = { onOptionsClick(track) },
                    onAddToQueue = { viewModel.addToQueue(track) },
                    onClick = {
                        viewModel.selectSearchResult(track.title, "Song", track.id, track.albumArtUri, track.artist)
                        viewModel.playTrack(track)
                    }
                )
            }
        }

        // 3. Videos
        if ((selectedFilter == "All" || selectedFilter == "Videos") && videos.isNotEmpty()) {
            val displayVideos = if (selectedFilter == "All") videos.take(6) else videos
            if (selectedFilter == "All") {
                item(key = "hdr_videos") {
                    SearchCategoryHeader("Videos", if (videos.size > 6) ({ onFilterChange("Videos") }) else null)
                }
            }
            items(
                items = displayVideos,
                key = { v -> "vid_${v.id}" }
            ) { videoTrack ->
                SimpSearchSongRow(
                    track = videoTrack,
                    isVideo = true,
                    isPlaying = isPlaying && currentTrack?.id == videoTrack.id,
                    isDownloaded = videoTrack.id in downloadedIds,
                    isLiked = videoTrack.id in likedTrackIds,
                    selectionMode = selectionMode,
                    isSelected = videoTrack.id in selectedTrackIds,
                    onSelectToggle = { onToggleTrackSelection(videoTrack) },
                    onLongClick = { onToggleTrackSelection(videoTrack) },
                    onLikeToggle = { viewModel.toggleTrackLike(videoTrack) },
                    onArtistClick = videoTrack.artistId?.let { aid ->
                        {
                            viewModel.selectSearchResult(videoTrack.artist, "Artist", aid, null, null)
                            navController.navigate("artist/$aid")
                        }
                    },
                    onOptionsClick = { onOptionsClick(videoTrack) },
                    onAddToQueue = { viewModel.addToQueue(videoTrack) },
                    onClick = { onVideoClick(videoTrack) }
                )
            }
        }

        // 4. Albums
        if ((selectedFilter == "All" || selectedFilter == "Albums") && albums.isNotEmpty()) {
            val displayAlbums = if (selectedFilter == "All") albums.take(6) else albums
            if (selectedFilter == "All") {
                item(key = "hdr_albums") {
                    SearchCategoryHeader("Albums", if (albums.size > 6) ({ onFilterChange("Albums") }) else null)
                }
            }
            items(
                items = displayAlbums,
                key = { a -> "alb_${a.id ?: a.title}" }
            ) { album ->
                SimpSearchCollectionRow(
                    title = album.title,
                    typeLabel = "Album",
                    subtitle = album.artist,
                    thumbnailUrl = album.artUri,
                    onLongClick = { onAlbumLongClick(album) },
                    onClick = {
                        viewModel.selectSearchResult(album.title, "Album", album.id, album.artUri, album.artist)
                        album.id?.let { navController.navigate("album/$it") }
                    }
                )
            }
        }

        // 5. Community Playlists
        if ((selectedFilter == "All" || selectedFilter == "Playlists") && playlists.isNotEmpty()) {
            val displayPlaylists = if (selectedFilter == "All") playlists.take(6) else playlists
            if (selectedFilter == "All") {
                item(key = "hdr_playlists") {
                    SearchCategoryHeader("Playlists", if (playlists.size > 6) ({ onFilterChange("Playlists") }) else null)
                }
            }
            items(
                items = displayPlaylists,
                key = { pl -> "pl_${pl.id}" }
            ) { pl ->
                SimpSearchCollectionRow(
                    title = pl.title,
                    typeLabel = "Playlist",
                    subtitle = pl.subtitle,
                    thumbnailUrl = pl.thumbnailUrl,
                    onClick = {
                        viewModel.selectSearchResult(pl.title, "Playlist", pl.id, pl.thumbnailUrl, pl.subtitle)
                        navController.navigate("album/${pl.id}")
                    }
                )
            }
        }

        // 6. Featured Playlists
        if ((selectedFilter == "All" || selectedFilter == "Featured playlists") && featuredPlaylists.isNotEmpty()) {
            val displayFeatured = if (selectedFilter == "All") featuredPlaylists.take(6) else featuredPlaylists
            if (selectedFilter == "All") {
                item(key = "hdr_featured_playlists") {
                    SearchCategoryHeader(
                        "Featured playlists",
                        if (featuredPlaylists.size > 6) ({ onFilterChange("Featured playlists") }) else null
                    )
                }
            }
            items(
                items = displayFeatured,
                key = { fpl -> "fpl_${fpl.id}" }
            ) { fpl ->
                SimpSearchCollectionRow(
                    title = fpl.title,
                    typeLabel = "Featured playlist",
                    subtitle = fpl.subtitle,
                    thumbnailUrl = fpl.thumbnailUrl,
                    onClick = {
                        viewModel.selectSearchResult(fpl.title, "Playlist", fpl.id, fpl.thumbnailUrl, fpl.subtitle)
                        navController.navigate("album/${fpl.id}")
                    }
                )
            }
        }

        // 7. Podcasts
        if ((selectedFilter == "All" || selectedFilter == "Podcasts") && podcasts.isNotEmpty()) {
            val displayPodcasts = if (selectedFilter == "All") podcasts.take(6) else podcasts
            if (selectedFilter == "All") {
                item(key = "hdr_podcasts") {
                    SearchCategoryHeader("Podcasts", if (podcasts.size > 6) ({ onFilterChange("Podcasts") }) else null)
                }
            }
            items(
                items = displayPodcasts,
                key = { pod -> "pod_${pod.id}" }
            ) { pod ->
                SimpSearchCollectionRow(
                    title = pod.title,
                    typeLabel = "Podcast",
                    subtitle = pod.subtitle,
                    thumbnailUrl = pod.thumbnailUrl,
                    onClick = {
                        viewModel.selectSearchResult(pod.title, "Playlist", pod.id, pod.thumbnailUrl, pod.subtitle)
                        navController.navigate("album/${pod.id}")
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchCategoryHeader(
    title: String,
    onSeeAll: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            color = Color.White.copy(alpha = 0.56f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp
        )
        if (onSeeAll != null) {
            Text(
                text = "See all >",
                color = Color(0xFFF0E3FF),
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

@Composable
private fun SearchCenteredMessage(
    searchBarHeight: Dp,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
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
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFF0E3FF).copy(alpha = 0.78f),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.52f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
