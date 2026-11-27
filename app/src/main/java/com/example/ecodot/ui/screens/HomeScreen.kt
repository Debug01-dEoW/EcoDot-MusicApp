@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.example.ecodot.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.remote.HomeMediaItem
import com.example.ecodot.data.remote.HomeSection
import com.example.ecodot.data.remote.MediaType
import com.example.ecodot.data.remote.MoodCategoryItem
import com.example.ecodot.data.remote.RealHomeData
import com.example.ecodot.ui.components.AddToPlaylistDialog
import com.example.ecodot.ui.components.AlbumOptionsBottomSheet
import com.example.ecodot.ui.components.CreatePlaylistBottomSheet
import com.example.ecodot.ui.components.ExplicitBadge
import com.example.ecodot.ui.components.HomeAmbientBackground
import com.example.ecodot.ui.components.HomeArtistCard
import com.example.ecodot.ui.components.HomeBrowseSheet
import com.example.ecodot.ui.components.HomeCreateActionBottomSheet
import com.example.ecodot.ui.components.HomeDiscoverHeroCarousel
import com.example.ecodot.ui.components.HomeFeedShimmer
import com.example.ecodot.ui.components.HomeLiveBadge
import com.example.ecodot.ui.components.LiquidGlassActionPill
import com.example.ecodot.ui.components.LiquidGlassAllChip
import com.example.ecodot.ui.components.LiquidGlassMoodChip
import com.example.ecodot.ui.components.LiquidGlassProfileButton
import com.example.ecodot.ui.components.PlaylistOptionsBottomSheet
import com.example.ecodot.ui.components.RankedChartRow
import com.example.ecodot.ui.components.RankedVideoChartCard
import com.example.ecodot.ui.components.SimpQuickPicksRowItem
import com.example.ecodot.ui.components.SimpSectionHeader
import com.example.ecodot.ui.components.TrackOptionsBottomSheet
import com.example.ecodot.ui.components.homeChipStripEdgeFades
import com.example.ecodot.ui.components.homeLiquidGlass
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.animatedCombinedClickable
import com.example.ecodot.ui.viewmodel.Album
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls

@Composable
fun SimpLazyRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(14.dp),
    content: LazyListScope.() -> Unit
) {
    val state = rememberLazyListState()
    val snapFling = rememberSnapFlingBehavior(
        SnapLayoutInfoProvider(lazyListState = state)
    )
    LazyRow(
        state = state,
        flingBehavior = snapFling,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    onSettingsClick: () -> Unit = { navController.navigate("settings") },
    onEqualizerClick: () -> Unit = {},
    onWatchVideo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val homeSections by viewModel.homeSections.collectAsState()
    val suggestedTracks by viewModel.suggestedTracks.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val historyRecommendations by viewModel.historyRecommendations.collectAsState()
    val albums by viewModel.allAlbums.collectAsState()
    val onRepeatTracks by viewModel.onRepeatTracks.collectAsState()
    val moodTracks by viewModel.moodRecommendedTracks.collectAsState()

    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val realMusicVideos by viewModel.realMusicVideos.collectAsState()
    val selectedCountry by viewModel.selectedHomeCountry.collectAsState()
    val countryVideoCharts by viewModel.countryVideoCharts.collectAsState()
    val topChartArtists by viewModel.topChartArtists.collectAsState()
    val topArtistChannelIds by viewModel.topArtistChannelIds.collectAsState()
    val chartPodcasts by viewModel.chartPodcasts.collectAsState()
    val isChartLoading by viewModel.isChartLoading.collectAsState()

    val isHomeLoading by viewModel.isHomeLoading.collectAsState()
    val isHomeRefreshing by viewModel.isHomeRefreshing.collectAsState()
    val isHomePaginating by viewModel.isHomePaginating.collectAsState()
    val homeContinuation by viewModel.homeContinuation.collectAsState()
    val homeBackgroundUrl by viewModel.homeBackgroundUrl.collectAsState()
    val homeHeroColor by viewModel.homeHeroColor.collectAsState()
    val homeMoodsData by viewModel.homeMoodsData.collectAsState()
    val browseSheetState by viewModel.browseSheetState.collectAsState()

    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()

    var selectedMood by remember { mutableStateOf<String?>(null) }
    var isMoodExpanded by remember { mutableStateOf(false) }
    var isCountryDropdownOpen by remember { mutableStateOf(false) }

    var showCreateActionSheet by remember { mutableStateOf(false) }
    var showCreatePlaylistSheet by remember { mutableStateOf(false) }
    var longPressedTrack by remember { mutableStateOf<Track?>(null) }
    var longPressedAlbum by remember { mutableStateOf<Album?>(null) }
    var longPressedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showPlaylistPicker by remember { mutableStateOf<Track?>(null) }

    val listState = rememberLazyListState()
    val chipScrollState = rememberScrollState()
    val pullToRefreshState = rememberPullToRefreshState()

    // Dedicated sibling backdrop so top Liquid Glass controls refract scrolling content in a single pass
    val homeFeedBackdrop = rememberLayerBackdrop()
    var topAppBarHeightPx by remember { mutableIntStateOf(0) }

    val atTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 12
        }
    }

    val shouldPaginate by remember(homeContinuation, isHomePaginating, selectedMood) {
        derivedStateOf {
            !homeContinuation.isNullOrBlank() &&
                !isHomePaginating &&
                selectedMood == null &&
                listState.layoutInfo.totalItemsCount > 0 &&
                (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1) >=
                (listState.layoutInfo.totalItemsCount - 2)
        }
    }

    LaunchedEffect(shouldPaginate) {
        if (shouldPaginate) {
            viewModel.loadMoreHomeContent()
        }
    }

    // Re-tapping Home bottom tab scrolls to top, or refreshes YouTube feed when already at top (SimpMusic behavior)
    LaunchedEffect(Unit) {
        viewModel.homeResetEvent.collect {
            if (listState.firstVisibleItemIndex > 1 || listState.firstVisibleItemScrollOffset > 80) {
                listState.animateScrollToItem(0)
            } else {
                viewModel.refreshHomeContent(forceFreshSeed = true, isPullToRefresh = true)
            }
        }
    }

    BackHandler(
        enabled = isMoodExpanded ||
            isCountryDropdownOpen ||
            browseSheetState.isVisible ||
            showCreateActionSheet ||
            longPressedTrack != null ||
            longPressedAlbum != null ||
            longPressedPlaylist != null ||
            showPlaylistPicker != null ||
            showCreatePlaylistSheet
    ) {
        when {
            showPlaylistPicker != null -> showPlaylistPicker = null
            showCreatePlaylistSheet -> showCreatePlaylistSheet = false
            showCreateActionSheet -> showCreateActionSheet = false
            browseSheetState.isVisible -> viewModel.dismissBrowseSheet()
            longPressedTrack != null -> longPressedTrack = null
            longPressedAlbum != null -> longPressedAlbum = null
            longPressedPlaylist != null -> longPressedPlaylist = null
            isCountryDropdownOpen -> isCountryDropdownOpen = false
            isMoodExpanded -> isMoodExpanded = false
        }
    }

    val appVersionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        }.getOrDefault("1.0")
    }

    val supportedCountries = remember {
        listOf(
            "Global",
            "Canada",
            "India",
            "United States",
            "United Kingdom",
            "Australia",
            "Germany",
            "France",
            "Japan",
            "Brazil",
            "South Korea"
        )
    }

    val moodOptions = remember {
        listOf("Relax", "Sleep", "Energize", "Sad", "Romance", "Feel good", "Workout", "Party", "Commute", "Focus")
    }

    val gridRowWidth = (configuration.screenWidthDp.dp - 44.dp).coerceAtLeast(280.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070709))
    ) {
        // ── 1. Backdrop Source Layer: Ambient Glow + PullToRefresh + LazyColumn ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(homeFeedBackdrop)
        ) {
            HomeAmbientBackground(
                activeMood = selectedMood,
                heroColor = homeHeroColor,
                headerImageUrl = homeBackgroundUrl
            )

            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = isHomeRefreshing,
                onRefresh = {
                    viewModel.refreshHomeContent(forceFreshSeed = true, isPullToRefresh = true)
                },
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isHomeRefreshing,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = with(density) { topAppBarHeightPx.toDp() } + 4.dp),
                        containerColor = Color(0xFF212333),
                        color = Color(0xFFE7D7FD)
                    )
                }
            ) {
                Crossfade(
                    targetState = isHomeLoading && homeSections.isEmpty(),
                    animationSpec = tween(260),
                    label = "home_loading_crossfade"
                ) { showSkeleton ->
                    if (showSkeleton) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(
                                modifier = Modifier.height(
                                    with(density) { topAppBarHeightPx.toDp() }.coerceAtLeast(88.dp)
                                )
                            )
                            HomeFeedShimmer()
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 128.dp)
                        ) {
                            item(key = "header_spacer", contentType = "spacer") {
                                Spacer(
                                    modifier = Modifier.height(
                                        with(density) { topAppBarHeightPx.toDp() }.coerceAtLeast(96.dp) + 4.dp
                                    )
                                )
                            }

                            if (selectedMood != null) {
                                val mood = selectedMood!!
                                renderMoodHomeFeed(
                                    mood = mood,
                                    moodTracks = moodTracks,
                                    homeSections = homeSections,
                                    currentTrack = currentTrack,
                                    isPlaying = isPlaying,
                                    gridRowWidth = gridRowWidth,
                                    viewModel = viewModel,
                                    navController = navController,
                                    onWatchVideo = onWatchVideo,
                                    onLongPressTrack = { longPressedTrack = it },
                                    onLongPressAlbum = { longPressedAlbum = it },
                                    onLongPressPlaylist = { longPressedPlaylist = it }
                                )
                            } else {
                                // ── ALL MODE: Full SimpMusic Homepage Experience ──
                                val navigateToBrowse: (
                                    String,
                                    String?,
                                    String?,
                                    String?,
                                    List<HomeMediaItem>,
                                    String?,
                                    String?
                                ) -> Unit = { title, strap, bId, bParams, seedItems, query, moodFilter ->
                                    viewModel.openBrowsePage(
                                        title = title,
                                        strapline = strap,
                                        browseId = bId,
                                        params = bParams,
                                        initialItems = seedItems,
                                        fallbackQuery = query ?: title,
                                        moodFilterName = moodFilter
                                    )
                                    navController.navigate("browse")
                                }

                                val topShelf = homeSections.firstOrNull {
                                    it.items.isNotEmpty() && !it.title.contains("Quick pick", ignoreCase = true)
                                }
                                val discoverShelf = homeSections.firstOrNull { sec ->
                                    sec != topShelf &&
                                        !sec.title.contains("Quick pick", ignoreCase = true) &&
                                        sec.items.size >= 4 &&
                                        sec.moreBrowseId == null &&
                                        sec.items.all { it.type == MediaType.TRACK && it.aspectRatio <= 1.1f }
                                }
                                val remainingShelves = homeSections.filter { sec ->
                                    sec != topShelf &&
                                        sec != discoverShelf &&
                                        !sec.title.contains("Quick pick", ignoreCase = true) &&
                                        sec.items.isNotEmpty()
                                }

                                // 1. Featured Top Shelf (with header avatar, strapline & More / Play all)
                                item(key = "featured_top_shelf", contentType = "shelf_row") {
                                    val eyebrow = topShelf?.strapline ?: "A BACKGROUND SCORE TO YOUR LOVE STORY"
                                    val title = topShelf?.title ?: "Romance Right Now"
                                    val shelfItems = topShelf?.items ?: getDefaultRomanceItems()
                                    val showMoreButton = topShelf?.moreBrowseId != null || shelfItems.any { it.type != MediaType.TRACK }

                                    SimpSectionHeader(
                                        eyebrow = eyebrow,
                                        title = title,
                                        avatarUrl = topShelf?.thumbnailUrl,
                                        actionText = when {
                                            showMoreButton -> "More"
                                            topShelf?.playAllPlaylistId != null || shelfItems.any { it.type == MediaType.TRACK } -> "Play all"
                                            else -> null
                                        },
                                        onHeaderClick = {
                                            navigateToBrowse(
                                                title,
                                                eyebrow,
                                                topShelf?.moreBrowseId,
                                                topShelf?.moreParams,
                                                shelfItems,
                                                title,
                                                null
                                            )
                                        },
                                        onActionClick = {
                                            if (showMoreButton) {
                                                navigateToBrowse(
                                                    title,
                                                    eyebrow,
                                                    topShelf?.moreBrowseId,
                                                    topShelf?.moreParams,
                                                    shelfItems,
                                                    title,
                                                    null
                                                )
                                            } else {
                                                playMediaItemsAsQueue(shelfItems, viewModel)
                                            }
                                        }
                                    )

                                    RenderHomeShelfRow(
                                        items = shelfItems,
                                        keyPrefix = "top",
                                        viewModel = viewModel,
                                        navController = navController,
                                        onWatchVideo = onWatchVideo,
                                        onLongPressTrack = { longPressedTrack = it },
                                        onLongPressAlbum = { longPressedAlbum = it },
                                        onLongPressPlaylist = { longPressedPlaylist = it }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // 2. Quick Picks (4-Row Column-Snapping Grid with Play All & Track Options)
                                if (suggestedTracks.isNotEmpty()) {
                                    item(key = "quick_picks_section", contentType = "quick_picks_grid") {
                                        val qpGridState = rememberLazyGridState()
                                        val qpSnapFling = rememberSnapFlingBehavior(
                                            SnapLayoutInfoProvider(
                                                lazyGridState = qpGridState,
                                                snapPosition = SnapPosition.Start
                                            )
                                        )
                                        SimpSectionHeader(
                                            eyebrow = "LET'S START WITH A RADIO",
                                            title = "Quick picks",
                                            actionText = "Play all",
                                            onActionClick = { viewModel.playQueue(suggestedTracks, startIndex = 0) }
                                        )

                                        LazyHorizontalGrid(
                                            rows = GridCells.Fixed(4),
                                            state = qpGridState,
                                            flingBehavior = qpSnapFling,
                                            modifier = Modifier.height(252.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            items(suggestedTracks.take(20), key = { "qp_${it.id}" }) { track ->
                                                SimpQuickPicksRowItem(
                                                    track = track,
                                                    isPlaying = isPlaying && currentTrack?.id == track.id,
                                                    itemWidth = gridRowWidth,
                                                    onClick = { viewModel.playTrack(track) },
                                                    onLongClick = { longPressedTrack = track }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                    }
                                }

                                // 3. Centered Hero Discover Carousel (SimpMusic HomeDiscoverSection)
                                item(key = "discover_hero_carousel", contentType = "hero_carousel") {
                                    val discoverTitle = discoverShelf?.title ?: "Your daily discover"
                                    val discoverEyebrow = discoverShelf?.strapline ?: "HANDPICKED RADIO FOR YOU"
                                    val discoverItems = remember(discoverShelf, historyRecommendations, suggestedTracks) {
                                        when {
                                            discoverShelf != null -> discoverShelf.items.take(10)
                                            historyRecommendations.isNotEmpty() -> historyRecommendations.take(8).map { it.toHomeMediaItem() }
                                            else -> suggestedTracks.drop(4).take(8).map { it.toHomeMediaItem() }
                                        }
                                    }
                                    if (discoverItems.isNotEmpty()) {
                                        SimpSectionHeader(
                                            eyebrow = discoverEyebrow,
                                            title = discoverTitle,
                                            actionText = "Play all",
                                            onActionClick = { playMediaItemsAsQueue(discoverItems, viewModel) }
                                        )
                                        HomeDiscoverHeroCarousel(
                                            items = discoverItems,
                                            onItemClick = { item -> handleMediaItemClick(item, viewModel, navController, onWatchVideo) },
                                            onItemLongClick = { item ->
                                                handleMediaItemLongClick(
                                                    item = item,
                                                    onTrackLongPress = { longPressedTrack = it },
                                                    onAlbumLongPress = { longPressedAlbum = it },
                                                    onPlaylistLongPress = { longPressedPlaylist = it }
                                                )
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                    }
                                }

                                // 4. Trending Music Videos (16:9 Widescreen Carousel with LIVE & MV Badges)
                                item(key = "music_video_section", contentType = "video_row") {
                                    SimpSectionHeader(
                                        eyebrow = "WATCH IN FULLSCREEN",
                                        title = "Music video",
                                        actionText = "More",
                                        onActionClick = {
                                            navigateToBrowse(
                                                "Trending Music Videos",
                                                "ON SCREEN NOW",
                                                null,
                                                null,
                                                realMusicVideos,
                                                "$selectedCountry Trending Music",
                                                null
                                            )
                                        }
                                    )

                                    SimpLazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        items(realMusicVideos, key = { "mv_${it.id}_${it.title}" }) { video ->
                                            MusicVideoCard(
                                                video = video,
                                                onClick = { playAsVideo(video, viewModel, onWatchVideo) },
                                                onLongClick = {
                                                    longPressedTrack = video.toTrack(isVideo = true)
                                                }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // 5. Dynamic Remote YouTube Music Shelves (Listen Again, New Releases, Mixes, Artists)
                                remainingShelves.forEachIndexed { secIdx, section ->
                                    item(key = "home_real_sec_${section.title}_$secIdx", contentType = "shelf_row") {
                                        val moreBrowseId = section.moreBrowseId
                                        val showMore = moreBrowseId != null || section.items.any { it.type != MediaType.TRACK }
                                        val actionLabel = when {
                                            showMore -> "More"
                                            section.items.all { it.type == MediaType.TRACK } -> "Play all"
                                            else -> null
                                        }
                                        SimpSectionHeader(
                                            eyebrow = section.strapline,
                                            title = section.title,
                                            avatarUrl = section.thumbnailUrl,
                                            actionText = actionLabel,
                                            onHeaderClick = section.channelId?.let { chId ->
                                                { navController.navigate("artist/$chId") }
                                            } ?: {
                                                navigateToBrowse(
                                                    section.title,
                                                    section.strapline,
                                                    moreBrowseId,
                                                    section.moreParams,
                                                    section.items,
                                                    section.title,
                                                    null
                                                )
                                            },
                                            onActionClick = actionLabel?.let {
                                                {
                                                    if (showMore) {
                                                        navigateToBrowse(
                                                            section.title,
                                                            section.strapline,
                                                            moreBrowseId,
                                                            section.moreParams,
                                                            section.items,
                                                            section.title,
                                                            null
                                                        )
                                                    } else {
                                                        playMediaItemsAsQueue(section.items, viewModel)
                                                    }
                                                }
                                            }
                                        )

                                        RenderHomeShelfRow(
                                            items = section.items,
                                            keyPrefix = "sec_$secIdx",
                                            viewModel = viewModel,
                                            navController = navController,
                                            onWatchVideo = onWatchVideo,
                                            onLongPressTrack = { longPressedTrack = it },
                                            onLongPressAlbum = { longPressedAlbum = it },
                                            onLongPressPlaylist = { longPressedPlaylist = it }
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                    }
                                }

                                // 6. Moods & Moments (3-Row Horizontal Grid matching SimpMusic)
                                item(key = "moods_moments_grid", contentType = "mood_grid") {
                                    val moodGridState = rememberLazyGridState()
                                    val moodSnap = rememberSnapFlingBehavior(SnapLayoutInfoProvider(moodGridState))
                                    val moodCards = remember(homeMoodsData) {
                                        if (homeMoodsData.moodsAndMoments.isNotEmpty()) {
                                            homeMoodsData.moodsAndMoments
                                        } else {
                                            getFallbackMoodCategoryItems()
                                        }
                                    }

                                    SimpSectionHeader(
                                        eyebrow = "LET'S PICK A PLAYLIST FOR YOU",
                                        title = "Moods & moments"
                                    )

                                    LazyHorizontalGrid(
                                        rows = GridCells.Fixed(3),
                                        state = moodGridState,
                                        flingBehavior = moodSnap,
                                        modifier = Modifier.height(184.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(moodCards, key = { "mm_${it.title}_${it.params}" }) { cat ->
                                            val matchingHomeMood = moodOptions.firstOrNull {
                                                it.equals(cat.title, ignoreCase = true) ||
                                                    (cat.title.equals("Running", true) && it == "Workout")
                                            }
                                            MoodPillCard(
                                                title = cat.title,
                                                accentColor = Color(cat.stripeColor),
                                                modifier = Modifier.width(156.dp),
                                                onClick = {
                                                    navigateToBrowse(
                                                        cat.title,
                                                        "MOODS & MOMENTS",
                                                        cat.params.takeIf { it.isNotBlank() }?.let { cat.browseId },
                                                        cat.params.takeIf { it.isNotBlank() },
                                                        emptyList(),
                                                        "${cat.title} music",
                                                        matchingHomeMood
                                                    )
                                                }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // 7. Genres (3-Row Horizontal Grid matching SimpMusic)
                                item(key = "genres_grid", contentType = "mood_grid") {
                                    val genreGridState = rememberLazyGridState()
                                    val genreSnap = rememberSnapFlingBehavior(SnapLayoutInfoProvider(genreGridState))
                                    val genreCards = remember(homeMoodsData) {
                                        if (homeMoodsData.genres.isNotEmpty()) {
                                            homeMoodsData.genres
                                        } else {
                                            RealHomeData.getRealGenres().map { (label, color) ->
                                                MoodCategoryItem(
                                                    title = label,
                                                    params = "",
                                                    stripeColor = (color.alpha * 255).toLong().shl(24) or
                                                        (color.red * 255).toLong().shl(16) or
                                                        (color.green * 255).toLong().shl(8) or
                                                        (color.blue * 255).toLong()
                                                )
                                            }
                                        }
                                    }

                                    SimpSectionHeader(title = "Genres")

                                    LazyHorizontalGrid(
                                        rows = GridCells.Fixed(3),
                                        state = genreGridState,
                                        flingBehavior = genreSnap,
                                        modifier = Modifier.height(184.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(genreCards, key = { "genre_${it.title}_${it.params}" }) { cat ->
                                            MoodPillCard(
                                                title = cat.title,
                                                accentColor = Color(cat.stripeColor),
                                                modifier = Modifier.width(164.dp),
                                                onClick = {
                                                    navigateToBrowse(
                                                        cat.title,
                                                        "GENRE",
                                                        cat.params.takeIf { it.isNotBlank() }?.let { cat.browseId },
                                                        cat.params.takeIf { it.isNotBlank() },
                                                        emptyList(),
                                                        "${cat.title} hits music",
                                                        null
                                                    )
                                                }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(18.dp))
                                }

                                // 8. Chart Header & Liquid Glass Country Dropdown
                                item(key = "chart_section", contentType = "chart_header") {
                                    SimpSectionHeader(
                                        eyebrow = "WHAT IS BEST CHOICE TODAY",
                                        title = "Chart"
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box {
                                            Row(
                                                modifier = Modifier
                                                    .height(38.dp)
                                                    .homeLiquidGlass(
                                                        backdrop = null,
                                                        shape = CircleShape,
                                                        surfaceTint = Color(0xFF212435),
                                                        surfaceAlpha = 0.72f,
                                                        specularAlpha = 0.85f,
                                                        frostSheenAlpha = 0.22f
                                                    )
                                                    .clickable { isCountryDropdownOpen = true }
                                                    .padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = selectedCountry,
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = Color.White
                                                )
                                                Icon(
                                                    imageVector = Icons.Rounded.ArrowDropDown,
                                                    contentDescription = "Select country",
                                                    tint = Color.White.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = isCountryDropdownOpen,
                                                onDismissRequest = { isCountryDropdownOpen = false },
                                                modifier = Modifier.background(Color(0xFF1C1D24))
                                            ) {
                                                supportedCountries.forEach { country ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = country,
                                                                color = if (country == selectedCountry) Color(0xFFE7D7FD) else Color.White,
                                                                fontWeight = if (country == selectedCountry) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        },
                                                        onClick = {
                                                            isCountryDropdownOpen = false
                                                            viewModel.loadHomeContentForCountry(country)
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        if (isChartLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFFE7D7FD)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // 9. Ranked Video Charts (1, 2, 3...)
                                item(key = "video_charts", contentType = "video_row") {
                                    SimpSectionHeader(title = "Video charts")
                                    SimpLazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        itemsIndexed(
                                            items = countryVideoCharts,
                                            key = { idx, it -> "vchart_${idx}_${it.id}" }
                                        ) { idx, item ->
                                            RankedVideoChartCard(
                                                video = item,
                                                rank = idx + 1,
                                                onClick = { playAsVideo(item, viewModel, onWatchVideo) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // 10. Top Artists (3-Row Horizontal Ranked Grid matching ItemArtistChart)
                                item(key = "top_artists_section", contentType = "ranked_grid") {
                                    val artistGridState = rememberLazyGridState()
                                    val artistSnap = rememberSnapFlingBehavior(
                                        SnapLayoutInfoProvider(artistGridState, SnapPosition.Start)
                                    )
                                    SimpSectionHeader(title = "Top artists")

                                    LazyHorizontalGrid(
                                        rows = GridCells.Fixed(3),
                                        state = artistGridState,
                                        flingBehavior = artistSnap,
                                        modifier = Modifier.height(214.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        itemsIndexed(
                                            items = topChartArtists,
                                            key = { idx, artist -> "artist_chart_${idx}_${artist.first}" }
                                        ) { idx, (name, subs, thumb) ->
                                            RankedChartRow(
                                                rank = "${idx + 1}",
                                                title = name,
                                                subtitle = subs,
                                                thumbnailUrl = thumb,
                                                isCircularThumbnail = true,
                                                itemWidth = gridRowWidth,
                                                onClick = {
                                                    val channelId = topArtistChannelIds[name]
                                                    if (!channelId.isNullOrBlank()) {
                                                        navController.navigate("artist/$channelId")
                                                    } else {
                                                        viewModel.performSearch(name, type = "Artists", saveToHistory = true)
                                                        navController.navigate("search")
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                }

                                // 11. Weekly Top Podcast Shows (3-Row Horizontal Ranked Grid)
                                if (chartPodcasts.isNotEmpty()) {
                                    item(key = "weekly_top_podcasts", contentType = "ranked_grid") {
                                        val podGridState = rememberLazyGridState()
                                        val podSnap = rememberSnapFlingBehavior(
                                            SnapLayoutInfoProvider(podGridState, SnapPosition.Start)
                                        )
                                        SimpSectionHeader(title = "Weekly top podcast shows")

                                        LazyHorizontalGrid(
                                            rows = GridCells.Fixed(3),
                                            state = podGridState,
                                            flingBehavior = podSnap,
                                            modifier = Modifier.height(214.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            itemsIndexed(
                                                items = chartPodcasts,
                                                key = { idx, pod -> "pod_${idx}_${pod.title}" }
                                            ) { idx, pod ->
                                                RankedChartRow(
                                                    rank = pod.rank.ifBlank { "${idx + 1}" },
                                                    title = pod.title,
                                                    subtitle = pod.host,
                                                    thumbnailUrl = pod.thumbnailUrl,
                                                    isCircularThumbnail = false,
                                                    itemWidth = gridRowWidth,
                                                    onClick = {
                                                        navigateToBrowse(
                                                            pod.title,
                                                            "PODCAST SHOW",
                                                            pod.browseId.takeIf { it.isNotBlank() },
                                                            null,
                                                            emptyList(),
                                                            pod.title,
                                                            null
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }

                                // 12. Local Library Personal Shelves (Recently Played, On Repeat, Saved Albums)
                                if (recentlyPlayed.isNotEmpty()) {
                                    item(key = "recently_played_shelf", contentType = "local_shelf") {
                                        SimpSectionHeader(
                                            eyebrow = "LISTEN AGAIN",
                                            title = "Recently Played",
                                            actionText = "Play all",
                                            onActionClick = { viewModel.playQueue(recentlyPlayed, startIndex = 0) }
                                        )
                                        SimpLazyRow {
                                            items(recentlyPlayed.take(15), key = { "rec_${it.id}" }, contentType = { "track_card" }) { track ->
                                                HomeTrackCard(
                                                    track = track,
                                                    isPlaying = isPlaying && currentTrack?.id == track.id,
                                                    onClick = { viewModel.playTrack(track) },
                                                    onLongClick = { longPressedTrack = track }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }

                                if (onRepeatTracks.isNotEmpty()) {
                                    item(key = "on_repeat_shelf", contentType = "local_shelf") {
                                        SimpSectionHeader(
                                            eyebrow = "HEAVY ROTATION",
                                            title = "Your On Repeat Mix",
                                            actionText = "Play all",
                                            onActionClick = { viewModel.playQueue(onRepeatTracks, startIndex = 0) }
                                        )
                                        SimpLazyRow {
                                            items(onRepeatTracks.take(15), key = { "repeat_${it.id}" }, contentType = { "track_card" }) { track ->
                                                HomeTrackCard(
                                                    track = track,
                                                    isPlaying = isPlaying && currentTrack?.id == track.id,
                                                    onClick = { viewModel.playTrack(track) },
                                                    onLongClick = { longPressedTrack = track }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }

                                if (albums.isNotEmpty()) {
                                    item(key = "albums_shelf", contentType = "local_shelf") {
                                        SimpSectionHeader(title = "Albums in Library")
                                        SimpLazyRow {
                                            items(albums.take(15), key = { "album_${it.id}_${it.title}" }, contentType = { "album_card" }) { album ->
                                                HomeAlbumCard(
                                                    album = album,
                                                    onClick = { album.id?.let { id -> navController.navigate("album/$id") } },
                                                    onLongClick = { longPressedAlbum = album }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }

                                // 13. Pagination Spinner (End of Home Continuation)
                                if (isHomePaginating) {
                                    item(key = "home_paginating_spinner") {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(26.dp),
                                                strokeWidth = 2.5.dp,
                                                color = Color(0xFFE7D7FD)
                                            )
                                        }
                                    }
                                }

                                // 14. End-of-page SimpMusic-style Credit Footer
                                item(key = "home_footer") {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "© 2026 EcoDot v$appVersionName",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = Color.White.copy(alpha = 0.42f)
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "SimpMusic UI • Liquid Glass • YouTube Music",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                            color = Color.White.copy(alpha = 0.28f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── 2. Floating Liquid Glass Top Header (Stable chips + crossfading frosted backdrop behind) ──
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    topAppBarHeightPx = coords.size.height
                }
        ) {
            AnimatedVisibility(
                visible = !atTop,
                modifier = Modifier.matchParentSize(),
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(180)),
                label = "home_header_sticky_blur"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBackdrop(
                            backdrop = homeFeedBackdrop,
                            shape = { RectangleShape },
                            effects = {
                                colorControls(
                                    brightness = -0.16f,
                                    contrast = 1.04f,
                                    saturation = 1.15f
                                )
                                blur(14.dp.toPx())
                            },
                            onDrawSurface = {
                                drawRect(Color(0xFF0A0B10).copy(alpha = 0.68f))
                            }
                        )
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF070709).copy(alpha = 0.55f),
                                    Color(0xFF070709).copy(alpha = 0.12f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiquidGlassProfileButton(
                        userProfile = userProfile,
                        backdrop = homeFeedBackdrop,
                        onOpenProfile = { navController.navigate("profile") },
                        onRefreshFeed = {
                            viewModel.refreshHomeContent(forceFreshSeed = true, isPullToRefresh = true)
                        },
                        onCheckUpdates = { viewModel.checkForUpdates(force = true) },
                        onOpenSettings = onSettingsClick
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .homeChipStripEdgeFades(
                                width = 26.dp,
                                start = { if (chipScrollState.canScrollBackward) 1f else 0f },
                                end = { if (chipScrollState.canScrollForward) 1f else 0f }
                            )
                            .horizontalScroll(chipScrollState),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LiquidGlassAllChip(
                            isSelected = selectedMood == null,
                            isLoading = (isHomeLoading || isHomeRefreshing) && selectedMood == null,
                            backdrop = homeFeedBackdrop,
                            onClick = {
                                selectedMood = null
                                isMoodExpanded = false
                                viewModel.setHomeMood(null)
                            }
                        )

                        LiquidGlassMoodChip(
                            selectedMood = selectedMood,
                            isExpanded = isMoodExpanded,
                            isLoading = (isHomeLoading || isHomeRefreshing) && selectedMood != null,
                            options = moodOptions,
                            backdrop = homeFeedBackdrop,
                            onExpandChange = { isMoodExpanded = it },
                            onSelectMood = { mood ->
                                selectedMood = mood
                                isMoodExpanded = false
                                viewModel.setHomeMood(mood)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    LiquidGlassActionPill(
                        backdrop = homeFeedBackdrop,
                        onCreateClick = { showCreateActionSheet = true },
                        onSettingsClick = onSettingsClick
                    )
                }
            }
        }

        // ── 3. Interactive Sheets (Create, Mood/Genre/More Browser, Track/Album/Playlist Options) ──
        if (showCreateActionSheet) {
            HomeCreateActionBottomSheet(
                onDismiss = { showCreateActionSheet = false },
                onCreatePlaylist = { showCreatePlaylistSheet = true },
                onShuffleQuickPicks = {
                    if (suggestedTracks.isNotEmpty()) {
                        viewModel.playQueue(suggestedTracks.shuffled(), startIndex = 0)
                    }
                },
                onRefreshFeed = {
                    viewModel.refreshHomeContent(forceFreshSeed = true, isPullToRefresh = true)
                }
            )
        }

        if (showCreatePlaylistSheet) {
            CreatePlaylistBottomSheet(
                onDismiss = { showCreatePlaylistSheet = false },
                onCreate = { name, _, _ ->
                    viewModel.createPlaylist(name)
                    showCreatePlaylistSheet = false
                }
            )
        }

        HomeBrowseSheet(
            state = browseSheetState,
            onDismiss = { viewModel.dismissBrowseSheet() },
            onApplyMoodFilter = { mood ->
                selectedMood = mood
                isMoodExpanded = false
                viewModel.setHomeMood(mood)
            },
            onItemClick = { item ->
                viewModel.dismissBrowseSheet()
                handleMediaItemClick(item, viewModel, navController, onWatchVideo)
            },
            onItemLongClick = { item ->
                handleMediaItemLongClick(
                    item = item,
                    onTrackLongPress = { longPressedTrack = it },
                    onAlbumLongPress = { longPressedAlbum = it },
                    onPlaylistLongPress = { longPressedPlaylist = it }
                )
            }
        )

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
                    } else {
                        album.id?.let { navController.navigate("album/$it") }
                    }
                },
                onSaveToggle = {
                    if (isSaved) viewModel.unsaveAlbum(album.tracks)
                    else viewModel.saveAlbum(album.tracks)
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
                    if (tracks.isNotEmpty()) viewModel.playQueue(tracks, startIndex = 0)
                },
                onAddToQueue = { tracks.forEach { viewModel.addToQueue(it) } },
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
                onCreatePlaylist = { name -> viewModel.createPlaylist(name) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.renderMoodHomeFeed(
    mood: String,
    moodTracks: List<Track>,
    homeSections: List<HomeSection>,
    currentTrack: Track?,
    isPlaying: Boolean,
    gridRowWidth: Dp,
    viewModel: MusicViewModel,
    navController: NavController,
    onWatchVideo: () -> Unit,
    onLongPressTrack: (Track) -> Unit,
    onLongPressAlbum: (Album) -> Unit,
    onLongPressPlaylist: (Playlist) -> Unit
) {
    val moodEyebrow = when (mood.lowercase()) {
        "relax" -> "FOR RELAXING"
        "workout" -> "FOR WORKOUT"
        "focus" -> "FOR FOCUS & STUDY"
        "energize" -> "FOR HIGH ENERGY"
        "chill" -> "FOR CHILLING OUT"
        "feel good" -> "FEEL GOOD VIBES"
        "romance" -> "ROMANTIC MOMENTS"
        "sleep" -> "FOR DEEP SLEEP"
        "party" -> "PARTY TIME"
        "commute" -> "ON THE COMMUTE"
        "sad" -> "EMOTIONAL & REFLECTIVE"
        else -> "FOR YOU • ${mood.uppercase()}"
    }

    item(key = "mood_shelf_1_$mood") {
        val shelfTitle = when (mood.lowercase()) {
            "relax" -> "Deep focus"
            "workout" -> "Gym Motivation"
            "focus" -> "Productivity Beats"
            "energize" -> "Electric Pulse"
            "chill" -> "Lo-Fi Lounge"
            "feel good" -> "Pure Happiness"
            "romance" -> "Love Ballads"
            "party" -> "Club Anthems"
            else -> "$mood Essentials"
        }
        val shelfItems = getCuratedMoodItems(mood, 1)
        SimpSectionHeader(
            eyebrow = moodEyebrow,
            title = shelfTitle,
            actionText = "Play all",
            onActionClick = { playMediaItemsAsQueue(shelfItems, viewModel) }
        )
        RenderHomeShelfRow(
            items = shelfItems,
            keyPrefix = "m1_$mood",
            viewModel = viewModel,
            navController = navController,
            onWatchVideo = onWatchVideo,
            onLongPressTrack = onLongPressTrack,
            onLongPressAlbum = onLongPressAlbum,
            onLongPressPlaylist = onLongPressPlaylist
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (moodTracks.isNotEmpty()) {
        item(key = "mood_quick_picks_$mood") {
            val moodGridState = rememberLazyGridState()
            val moodSnap = rememberSnapFlingBehavior(
                SnapLayoutInfoProvider(moodGridState, SnapPosition.Start)
            )
            SimpSectionHeader(
                eyebrow = "LET'S START WITH A RADIO",
                title = "Quick picks",
                actionText = "Play all",
                onActionClick = { viewModel.playQueue(moodTracks, startIndex = 0) }
            )
            LazyHorizontalGrid(
                rows = GridCells.Fixed(4),
                state = moodGridState,
                flingBehavior = moodSnap,
                modifier = Modifier.height(252.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(moodTracks.take(16), key = { "mood_qp_${it.id}" }) { track ->
                    SimpQuickPicksRowItem(
                        track = track,
                        isPlaying = isPlaying && currentTrack?.id == track.id,
                        itemWidth = gridRowWidth,
                        onClick = { viewModel.playTrack(track) },
                        onLongClick = { onLongPressTrack(track) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }

    homeSections.forEachIndexed { index, section ->
        if (section.items.isNotEmpty()) {
            item(key = "remote_mood_sec_${section.title}_$index") {
                val showMore = section.moreBrowseId != null || section.items.any { it.type != MediaType.TRACK }
                SimpSectionHeader(
                    eyebrow = section.strapline,
                    title = section.title,
                    avatarUrl = section.thumbnailUrl,
                    actionText = if (showMore) "More" else null,
                    onActionClick = if (showMore) {
                        {
                            viewModel.openBrowsePage(
                                title = section.title,
                                strapline = section.strapline,
                                browseId = section.moreBrowseId,
                                params = section.moreParams,
                                initialItems = section.items,
                                fallbackQuery = "${section.title} $mood",
                                moodFilterName = mood
                            )
                            navController.navigate("browse")
                        }
                    } else null
                )
                RenderHomeShelfRow(
                    items = section.items,
                    keyPrefix = "rmood_$index",
                    viewModel = viewModel,
                    navController = navController,
                    onWatchVideo = onWatchVideo,
                    onLongPressTrack = onLongPressTrack,
                    onLongPressAlbum = onLongPressAlbum,
                    onLongPressPlaylist = onLongPressPlaylist
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun RenderHomeShelfRow(
    items: List<HomeMediaItem>,
    keyPrefix: String,
    viewModel: MusicViewModel,
    navController: NavController,
    onWatchVideo: () -> Unit,
    onLongPressTrack: (Track) -> Unit,
    onLongPressAlbum: (Album) -> Unit,
    onLongPressPlaylist: (Playlist) -> Unit
) {
    SimpLazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        itemsIndexed(items, key = { idx, it -> "${keyPrefix}_${idx}_${it.id}" }) { _, item ->
            when {
                item.type == MediaType.ARTIST -> {
                    HomeArtistCard(
                        item = item,
                        onClick = { handleMediaItemClick(item, viewModel, navController, onWatchVideo) }
                    )
                }
                item.type == MediaType.TRACK && item.aspectRatio > 1.2f -> {
                    MusicVideoCard(
                        video = item,
                        onClick = { playAsVideo(item, viewModel, onWatchVideo) },
                        onLongClick = { onLongPressTrack(item.toTrack(isVideo = true)) }
                    )
                }
                else -> {
                    SimpMusicCard(
                        item = item,
                        onClick = { handleMediaItemClick(item, viewModel, navController, onWatchVideo) },
                        onLongClick = {
                            handleMediaItemLongClick(
                                item = item,
                                onTrackLongPress = onLongPressTrack,
                                onAlbumLongPress = onLongPressAlbum,
                                onPlaylistLongPress = onLongPressPlaylist
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SimpMusicCard(
    item: HomeMediaItem,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .width(148.dp)
            .animatedCombinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(148.dp)
                .clip(shape)
                .background(Color(0xFF161722))
                .border(0.6.dp, Color.White.copy(alpha = 0.10f), shape)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.thumbnailUrl)
                    .size(450, 450)
                    .crossfade(160)
                    .build(),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.62f))
                    .border(0.75.dp, Color.White.copy(alpha = 0.34f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (item.subtitle.isNotBlank() || item.isExplicit) {
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.isExplicit) {
                    ExplicitBadge(modifier = Modifier.padding(end = 5.dp))
                }
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFFA8A8A8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MusicVideoCard(
    video: HomeMediaItem,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(254.dp)
            .clip(RoundedCornerShape(14.dp))
            .animatedCombinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(142.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF161722))
                .border(0.6.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(video.thumbnailUrl)
                    .size(640, 360)
                    .crossfade(160)
                    .build(),
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                if (video.isLive) {
                    HomeLiveBadge()
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EcoDotRed.copy(alpha = 0.92f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MV",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.28f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = video.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                lineHeight = 19.sp
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = video.subtitle.ifBlank { "YouTube Music" },
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color(0xFFA8A8A8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = if (isPlaying) {
                Modifier.basicMarquee(
                    initialDelayMillis = 2000,
                    repeatDelayMillis = 2000,
                    velocity = 25.dp
                )
            } else {
                Modifier
            }
        )
    }
}

@Composable
fun MoodPillCard(
    title: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF171822))
            .border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(7.dp)
                .fillMaxHeight()
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HomeTrackCard(
    track: Track,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isPlaying: Boolean = false
) {
    Column(
        modifier = Modifier
            .width(145.dp)
            .animatedCombinedClickable(onLongClick, onClick)
    ) {
        Box(
            modifier = Modifier
                .size(145.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF161722))
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(track.albumArtUri)
                    .size(450, 450)
                    .crossfade(160)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp
            ),
            color = if (isPlaying) EcoDotRed else Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                ExplicitBadge(modifier = Modifier.padding(end = 4.dp))
            }
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFFA8A8A8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (isPlaying) {
                    Modifier.basicMarquee(
                        initialDelayMillis = 2000,
                        repeatDelayMillis = 2000,
                        velocity = 25.dp
                    )
                } else {
                    Modifier
                }
            )
        }
    }
}

@Composable
fun HomeAlbumCard(
    album: Album,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(145.dp)
            .animatedCombinedClickable(onLongClick, onClick)
    ) {
        Box(
            modifier = Modifier
                .size(145.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF161722))
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.artUri)
                    .size(450, 450)
                    .crossfade(160)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color.White.copy(alpha = 0.58f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun Track.toHomeMediaItem(): HomeMediaItem =
    HomeMediaItem(
        id = id,
        title = title,
        subtitle = listOfNotNull(
            artist.takeIf { it.isNotBlank() },
            album.takeIf { it.isNotBlank() && !it.equals("Unknown", true) && !it.equals("YouTube Video", true) }
        ).joinToString(" • "),
        thumbnailUrl = albumArtUri ?: "https://i.ytimg.com/vi/$id/hqdefault.jpg",
        type = MediaType.TRACK,
        isExplicit = isExplicit,
        aspectRatio = if (path.startsWith("ytvideo://")) 16f / 9f else 1f,
        artistId = artistId
    )

private fun HomeMediaItem.toTrack(isVideo: Boolean = aspectRatio > 1.2f): Track =
    Track(
        id = id,
        title = title,
        artist = subtitle.split("•").firstOrNull()?.trim().takeUnless { it.isNullOrBlank() } ?: "YouTube Music",
        album = if (isVideo) "YouTube Video" else "",
        albumArtUri = thumbnailUrl.ifBlank { "https://i.ytimg.com/vi/$id/hqdefault.jpg" },
        duration = 0,
        path = if (isVideo) "ytvideo://$id" else "https://www.youtube.com/watch?v=$id",
        isYouTube = true,
        artistId = artistId,
        isExplicit = isExplicit
    )

private fun playAsVideo(item: HomeMediaItem, viewModel: MusicViewModel, onWatchVideo: () -> Unit) {
    viewModel.playTrack(item.toTrack(isVideo = true), isVideo = true)
    viewModel.setVideoMode(true)
    onWatchVideo()
}

private fun playMediaItemsAsQueue(items: List<HomeMediaItem>, viewModel: MusicViewModel) {
    val tracks = items.filter { it.type == MediaType.TRACK }.map { it.toTrack(isVideo = false) }
    if (tracks.isNotEmpty()) {
        viewModel.playQueue(tracks, startIndex = 0)
    }
}

private fun handleMediaItemClick(
    item: HomeMediaItem,
    viewModel: MusicViewModel,
    navController: NavController,
    onWatchVideo: () -> Unit
) {
    when (item.type) {
        MediaType.TRACK -> {
            if (item.aspectRatio > 1.2f) {
                playAsVideo(item, viewModel, onWatchVideo)
            } else {
                viewModel.playTrack(item.toTrack(isVideo = false))
            }
        }
        MediaType.ALBUM,
        MediaType.PLAYLIST -> navController.navigate("album/${item.id}")
        MediaType.ARTIST -> navController.navigate("artist/${item.id}")
        MediaType.UNKNOWN -> {
            viewModel.performSearch(item.title)
            navController.navigate("search")
        }
    }
}

private fun handleMediaItemLongClick(
    item: HomeMediaItem,
    onTrackLongPress: (Track) -> Unit,
    onAlbumLongPress: (Album) -> Unit,
    onPlaylistLongPress: (Playlist) -> Unit
) {
    when (item.type) {
        MediaType.TRACK -> onTrackLongPress(item.toTrack())
        MediaType.ALBUM -> onAlbumLongPress(
            Album(
                id = item.id,
                title = item.title,
                artist = item.subtitle.ifBlank { "Various Artists" },
                artUri = item.thumbnailUrl,
                tracks = emptyList()
            )
        )
        MediaType.PLAYLIST -> onPlaylistLongPress(
            Playlist(
                id = 0L,
                name = item.title,
                description = item.subtitle,
                coverArtUri = item.thumbnailUrl
            )
        )
        else -> {}
    }
}

private fun getFallbackMoodCategoryItems(): List<MoodCategoryItem> = listOf(
    MoodCategoryItem("Chill", "", stripeColor = 0xFF00B4D8L),
    MoodCategoryItem("Feel good", "", stripeColor = 0xFF2EC4B6L),
    MoodCategoryItem("Commute", "", stripeColor = 0xFFFFB703L),
    MoodCategoryItem("Focus", "", stripeColor = 0xFFE63946L),
    MoodCategoryItem("Energize", "", stripeColor = 0xFFFB8500L),
    MoodCategoryItem("Running", "", stripeColor = 0xFFE76F51L),
    MoodCategoryItem("Party", "", stripeColor = 0xFF7209B7L),
    MoodCategoryItem("Romance", "", stripeColor = 0xFFE05780L),
    MoodCategoryItem("Sleep", "", stripeColor = 0xFF3F51B5L)
)

private fun getDefaultRomanceItems(): List<HomeMediaItem> = listOf(
    HomeMediaItem(
        id = "VAdGW7QDJUI",
        title = "Chaleya",
        subtitle = "Arijit Singh, Shilpa Rao • Jawan",
        thumbnailUrl = "https://i.ytimg.com/vi/VAdGW7QDJUI/hqdefault.jpg",
        type = MediaType.TRACK
    ),
    HomeMediaItem(
        id = "IJq0cvO40Ass",
        title = "Tum Hi Ho",
        subtitle = "Arijit Singh • Aashiqui 2",
        thumbnailUrl = "https://i.ytimg.com/vi/IJq0cvO40Ass/hqdefault.jpg",
        type = MediaType.TRACK
    ),
    HomeMediaItem(
        id = "JFcgOboQZ08",
        title = "Kesariya",
        subtitle = "Arijit Singh, Pritam • Brahmāstra",
        thumbnailUrl = "https://i.ytimg.com/vi/JFcgOboQZ08/hqdefault.jpg",
        type = MediaType.TRACK
    ),
    HomeMediaItem(
        id = "ilNt2bikxDI",
        title = "Raataan Lambiyan",
        subtitle = "Jubin Nautiyal, Asees Kaur • Shershaah",
        thumbnailUrl = "https://i.ytimg.com/vi/ilNt2bikxDI/hqdefault.jpg",
        type = MediaType.TRACK
    ),
    HomeMediaItem(
        id = "mH_LFkW_oao",
        title = "Lover",
        subtitle = "Diljit Dosanjh • MoonChild Era",
        thumbnailUrl = "https://i.ytimg.com/vi/mH_LFkW_oao/hqdefault.jpg",
        type = MediaType.TRACK
    )
)

private fun getCuratedMoodItems(mood: String, shelfIndex: Int): List<HomeMediaItem> {
    return when (mood.lowercase()) {
        "relax" -> if (shelfIndex == 1) {
            listOf(
                HomeMediaItem("5qap5aO4i9A", "Lofi Hip Hop Radio", "Lofi Girl • Beats to Relax/Study", "https://i.ytimg.com/vi/5qap5aO4i9A/hqdefault.jpg", MediaType.TRACK),
                HomeMediaItem("DWcJFNfaw9c", "Weightless (Original)", "Marconi Union • Ambient Relax", "https://i.ytimg.com/vi/DWcJFNfaw9c/hqdefault.jpg", MediaType.TRACK),
                HomeMediaItem("jfKfPfyJRdk", "Relaxing Piano Music", "Peaceful Dream • Soft Melodies", "https://i.ytimg.com/vi/jfKfPfyJRdk/hqdefault.jpg", MediaType.TRACK)
            )
        } else {
            listOf(
                HomeMediaItem("ilNt2bikxDI", "Raataan Lambiyan", "Jubin Nautiyal, Asees Kaur", "https://i.ytimg.com/vi/ilNt2bikxDI/hqdefault.jpg", MediaType.TRACK),
                HomeMediaItem("VAdGW7QDJUI", "Chaleya (Acoustic)", "Arijit Singh, Shilpa Rao", "https://i.ytimg.com/vi/VAdGW7QDJUI/hqdefault.jpg", MediaType.TRACK),
                HomeMediaItem("WPni755-Krg", "Sunset Lover", "Petit Biscuit", "https://i.ytimg.com/vi/WPni755-Krg/hqdefault.jpg", MediaType.TRACK)
            )
        }
        "workout" -> if (shelfIndex == 1) {
            listOf(
                HomeMediaItem("YVkUvmDQ3HY", "Without Me", "Eminem", "https://i.ytimg.com/vi/YVkUvmDQ3HY/hqdefault.jpg", MediaType.TRACK, isExplicit = true),
                HomeMediaItem("34Na4j8AVgA", "Starboy", "The Weeknd ft. Daft Punk", "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg", MediaType.TRACK, isExplicit = true),
                HomeMediaItem("4NRXx6U8ABQ", "Blinding Lights", "The Weeknd", "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg", MediaType.TRACK)
            )
        } else {
            listOf(
                HomeMediaItem("e-ORhEE9VVg", "Blank Space", "Taylor Swift", "https://i.ytimg.com/vi/e-ORhEE9VVg/hqdefault.jpg", MediaType.TRACK),
                HomeMediaItem("hT_nvWreIhg", "Counting Stars", "OneRepublic", "https://i.ytimg.com/vi/hT_nvWreIhg/hqdefault.jpg", MediaType.TRACK),
                HomeMediaItem("ApXoWvfEYVU", "Sunflower", "Post Malone & Swae Lee", "https://i.ytimg.com/vi/ApXoWvfEYVU/hqdefault.jpg", MediaType.TRACK)
            )
        }
        "focus" -> listOf(
            HomeMediaItem("n61ULEU7SU0", "Resonance", "HOME • Synthwave Beats", "https://i.ytimg.com/vi/n61ULEU7SU0/hqdefault.jpg", MediaType.TRACK),
            HomeMediaItem("2OEL4P1Rz04", "Clair de Lune", "Claude Debussy • Classical", "https://i.ytimg.com/vi/2OEL4P1Rz04/hqdefault.jpg", MediaType.TRACK),
            HomeMediaItem("5qap5aO4i9A", "Deep Focus Beats", "Lofi Girl", "https://i.ytimg.com/vi/5qap5aO4i9A/hqdefault.jpg", MediaType.TRACK)
        )
        else -> listOf(
            HomeMediaItem("kPa7bsKwL-8", "Die With A Smile", "Lady Gaga & Bruno Mars", "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg", MediaType.TRACK),
            HomeMediaItem("KRaWnd3LJfs", "Payphone", "Maroon 5 ft. Wiz Khalifa", "https://i.ytimg.com/vi/KRaWnd3LJfs/hqdefault.jpg", MediaType.TRACK, isExplicit = true),
            HomeMediaItem("yzTuBuRdAyA", "The Hills", "The Weeknd", "https://i.ytimg.com/vi/yzTuBuRdAyA/hqdefault.jpg", MediaType.TRACK, isExplicit = true)
        )
    }
}
