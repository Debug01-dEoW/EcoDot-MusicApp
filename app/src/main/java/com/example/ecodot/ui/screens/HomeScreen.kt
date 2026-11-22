package com.example.ecodot.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.local.entities.UserProfile
import com.example.ecodot.data.remote.HomeMediaItem
import com.example.ecodot.data.remote.HomeSection
import com.example.ecodot.data.remote.MediaType
import com.example.ecodot.ui.components.*
import com.example.ecodot.ui.theme.*
import com.example.ecodot.LocalHazeState
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import androidx.compose.ui.graphics.Shape
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

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    onEqualizerClick: () -> Unit = {},
    onWatchVideo: () -> Unit = {},
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

    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()

    var selectedMood by remember { mutableStateOf<String?>(null) }
    var isMoodExpanded by remember { mutableStateOf(false) }

    var selectedCountry by remember { mutableStateOf("Canada") }
    var isCountryDropdownOpen by remember { mutableStateOf(false) }

    var showCreatePlaylistSheet by remember { mutableStateOf(false) }
    var longPressedTrack by remember { mutableStateOf<Track?>(null) }
    var longPressedAlbum by remember { mutableStateOf<Album?>(null) }
    var longPressedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()
    var showPlaylistPicker by remember { mutableStateOf<Track?>(null) }

    val listState = rememberLazyListState()
    val backdrop = LocalBackdrop.current

    // Scroll to top when Home bottom tab is clicked while on Home
    LaunchedEffect(Unit) {
        viewModel.homeResetEvent.collect {
            listState.animateScrollToItem(0)
        }
    }

    // Dismiss active sheets on back press
    BackHandler(enabled = longPressedTrack != null || longPressedAlbum != null || longPressedPlaylist != null || showPlaylistPicker != null || showCreatePlaylistSheet) {
        when {
            showPlaylistPicker != null -> showPlaylistPicker = null
            showCreatePlaylistSheet -> showCreatePlaylistSheet = false
            longPressedTrack != null -> longPressedTrack = null
            longPressedAlbum != null -> longPressedAlbum = null
            longPressedPlaylist != null -> longPressedPlaylist = null
        }
    }

    val supportedCountries = remember {
        listOf("Canada", "India", "Global", "United States", "United Kingdom", "Australia", "Germany", "France", "Japan", "Brazil")
    }

    val moodOptions = remember {
        listOf("Relax", "Workout", "Focus", "Energize", "Chill", "Feel good", "Romance", "Sad", "Sleep", "Party", "Commute")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070709))
    ) {
        // ── 1. Dynamic Background & Ambience Lights ─────────────────────────
        HomeAmbientBackground(
            activeMood = selectedMood,
            dominantColor = dominantColor
        )

        // ── 2. Scrollable Home Feed ─────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Top clearance for status bar and liquid glass floating header
            item(key = "header_spacer") {
                Spacer(
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(62.dp)
                )
            }

            // ────────────────────────────────────────────────────────────────
            // MODE A: A MOOD IS SELECTED (e.g. Relax, Workout, Focus, etc.)
            // ────────────────────────────────────────────────────────────────
            if (selectedMood != null) {
                val mood = selectedMood!!
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
                    else -> "FOR YOU • $mood".uppercase()
                }

                // Curated Shelf 1 for selected mood
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

                    SimpSectionHeader(
                        eyebrow = moodEyebrow,
                        title = shelfTitle
                    )

                    val shelfItems = getCuratedMoodItems(mood, 1)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(shelfItems, key = { it.id }) { item ->
                            SimpMusicCard(
                                item = item,
                                onClick = { handleMediaItemClick(item, viewModel, navController) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Curated Shelf 2 for selected mood
                item(key = "mood_shelf_2_$mood") {
                    val shelfTitle2 = when (mood.lowercase()) {
                        "relax" -> "Coffee shop blends"
                        "workout" -> "High BPM Cardio"
                        "focus" -> "Ambient Soundscapes"
                        "energize" -> "Morning Boost"
                        "chill" -> "90s Acoustic Vibes"
                        "feel good" -> "Sunny Day Jams"
                        "romance" -> "Candlelight Dinner"
                        "party" -> "Dance Floor Hits"
                        else -> "$mood Highlights"
                    }

                    SimpSectionHeader(
                        title = shelfTitle2
                    )

                    val shelfItems2 = getCuratedMoodItems(mood, 2)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(shelfItems2, key = { it.id }) { item ->
                            SimpMusicCard(
                                item = item,
                                onClick = { handleMediaItemClick(item, viewModel, navController) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Quick Picks for this mood
                if (moodTracks.isNotEmpty()) {
                    item(key = "mood_quick_picks_$mood") {
                        SimpSectionHeader(
                            eyebrow = "LET'S START WITH A RADIO",
                            title = "Quick picks",
                            actionText = "Play all",
                            onActionClick = {
                                viewModel.playQueue(moodTracks, startIndex = 0)
                            }
                        )

                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(4),
                            modifier = Modifier.height(256.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(moodTracks.take(16), key = { "mood_qp_${it.id}" }) { track ->
                                QuickPicksItem(
                                    track = track,
                                    isPlaying = isPlaying && currentTrack?.id == track.id,
                                    onClick = { viewModel.playTrack(track) },
                                    onLongClick = { longPressedTrack = track }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Any additional remote sections fetched from YouTube for this mood
                homeSections.forEachIndexed { index, section ->
                    if (section.items.isNotEmpty()) {
                        item(key = "remote_mood_sec_${section.title}_$index") {
                            SimpSectionHeader(
                                eyebrow = section.strapline,
                                title = section.title
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(section.items, key = { it.id + it.hashCode() }) { item ->
                                    SimpMusicCard(
                                        item = item,
                                        onClick = { handleMediaItemClick(item, viewModel, navController) },
                                        onLongClick = { handleMediaItemLongClick(item, viewModel, { longPressedTrack = it }, { longPressedAlbum = it }, { longPressedPlaylist = it }) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }

            // ────────────────────────────────────────────────────────────────
            // MODE B: "ALL" IS SELECTED (Default Full SimpMusic Experience)
            // ────────────────────────────────────────────────────────────────
            else {
                // 1. Featured Top Shelf ("Romance Right Now" or "Hindi Hits")
                item(key = "featured_top_shelf") {
                    val topShelf = homeSections.firstOrNull { it.items.isNotEmpty() }
                    val eyebrow = topShelf?.strapline ?: "A BACKGROUND SCORE TO YOUR LOVE STORY"
                    val title = topShelf?.title ?: "Romance Right Now"
                    val items = topShelf?.items ?: getDefaultRomanceItems()

                    SimpSectionHeader(
                        eyebrow = eyebrow,
                        title = title
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(items, key = { "top_${it.id}" }) { item ->
                            SimpMusicCard(
                                item = item,
                                onClick = { handleMediaItemClick(item, viewModel, navController) },
                                onLongClick = { handleMediaItemLongClick(item, viewModel, { longPressedTrack = it }, { longPressedAlbum = it }, { longPressedPlaylist = it }) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2. Modern 4-Row Horizontal Quick Picks (With "Play all")
                if (suggestedTracks.isNotEmpty()) {
                    item(key = "quick_picks_section") {
                        SimpSectionHeader(
                            eyebrow = "LET'S START WITH A RADIO",
                            title = "Quick picks",
                            actionText = "Play all",
                            onActionClick = {
                                viewModel.playQueue(suggestedTracks, startIndex = 0)
                            }
                        )

                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(4),
                            modifier = Modifier.height(256.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(suggestedTracks.take(16), key = { "qp_${it.id}" }) { track ->
                                QuickPicksItem(
                                    track = track,
                                    isPlaying = isPlaying && currentTrack?.id == track.id,
                                    onClick = { viewModel.playTrack(track) },
                                    onLongClick = { longPressedTrack = track }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // 3. Music Video Section
                item(key = "music_video_section") {
                    SimpSectionHeader(
                        title = "Music video",
                        actionText = "More",
                        onActionClick = {
                            viewModel.performSearch("Trending Music Videos")
                            navController.navigate("search")
                        }
                    )

                    val videoItems = getSampleMusicVideos()
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(videoItems, key = { it.id }) { video ->
                            MusicVideoCard(
                                video = video,
                                onClick = {
                                    val track = Track(
                                        id = video.id,
                                        title = video.title,
                                        artist = video.subtitle ?: "Artist",
                                        album = "Music Video",
                                        albumArtUri = video.thumbnailUrl,
                                        duration = 0,
                                        path = "https://www.youtube.com/watch?v=${video.id}",
                                        isYouTube = true
                                    )
                                    viewModel.playTrack(track, isVideo = true)
                                    viewModel.setVideoMode(true)
                                    onWatchVideo()
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Additional Real YouTube Music Sections (Hindi Hits, Global Top Hits, etc.)
                if (homeSections.size > 1) {
                    homeSections.drop(1).forEachIndexed { secIdx, section ->
                        if (section.items.isNotEmpty()) {
                            item(key = "home_real_sec_${section.title}_$secIdx") {
                                SimpSectionHeader(
                                    eyebrow = section.strapline,
                                    title = section.title
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(section.items, key = { "sec_item_${it.id}_${it.title}" }) { item ->
                                        SimpMusicCard(
                                            item = item,
                                            onClick = { handleMediaItemClick(item, viewModel, navController) },
                                            onLongClick = { handleMediaItemLongClick(item, viewModel, { longPressedTrack = it }, { longPressedAlbum = it }, { longPressedPlaylist = it }) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }

                // 4. Moods & Moments 2-Column Pill Grid
                item(key = "moods_moments_grid") {
                    SimpSectionHeader(
                        eyebrow = "LET'S FIND A PLAYLIST FOR YOU",
                        title = "Moods & moments"
                    )

                    val moodCards = listOf(
                        Pair("Chill", Color(0xFF00B4D8)),
                        Pair("Feel good", Color(0xFF2EC4B6)),
                        Pair("Commute", Color(0xFFFFB703)),
                        Pair("Focus", Color(0xFFE63946)),
                        Pair("Energize", Color(0xFFFB8500)),
                        Pair("Running", Color(0xFFE76F51))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in moodCards.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MoodPillCard(
                                    title = moodCards[i].first,
                                    accentColor = moodCards[i].second,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val picked = if (moodCards[i].first == "Running") "Workout" else moodCards[i].first
                                        selectedMood = picked
                                        isMoodExpanded = false
                                        viewModel.setHomeMood(picked)
                                    }
                                )
                                if (i + 1 < moodCards.size) {
                                    MoodPillCard(
                                        title = moodCards[i + 1].first,
                                        accentColor = moodCards[i + 1].second,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            selectedMood = moodCards[i + 1].first
                                            isMoodExpanded = false
                                            viewModel.setHomeMood(moodCards[i + 1].first)
                                        }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 5. Genres 2-Column Pill Grid
                item(key = "genres_grid") {
                    SimpSectionHeader(
                        title = "Genres"
                    )

                    val genreCards = listOf(
                        Pair("Pop", Color(0xFFFF4081)),
                        Pair("Hip-Hop & Rap", Color(0xFFFF6D00)),
                        Pair("Rock & Metal", Color(0xFF7C4DFF)),
                        Pair("Bollywood & Desi", Color(0xFFFFD600)),
                        Pair("R&B & Soul", Color(0xFF536DFE)),
                        Pair("EDM & Dance", Color(0xFF00E5FF)),
                        Pair("Lo-Fi Chill", Color(0xFFB388FF)),
                        Pair("K-Pop", Color(0xFFFF1744)),
                        Pair("Indie & Folk", Color(0xFF00E676)),
                        Pair("Workout Pump", Color(0xFFFF3D00)),
                        Pair("Romance", Color(0xFFFF80AB)),
                        Pair("Focus & Study", Color(0xFF1DE9B6))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in genreCards.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MoodPillCard(
                                    title = genreCards[i].first,
                                    accentColor = genreCards[i].second,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.performSearch(genreCards[i].first)
                                        navController.navigate("search")
                                    }
                                )
                                if (i + 1 < genreCards.size) {
                                    MoodPillCard(
                                        title = genreCards[i + 1].first,
                                        accentColor = genreCards[i + 1].second,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.performSearch(genreCards[i + 1].first)
                                            navController.navigate("search")
                                        }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 6. Chart Section with Country Dropdown Selector
                item(key = "chart_section") {
                    SimpSectionHeader(
                        eyebrow = "WHAT IS MUSIC CHOICE TODAY",
                        title = "Chart"
                    )

                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1E1E26))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .clickable { isCountryDropdownOpen = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = selectedCountry,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "Select country",
                                tint = Color.White.copy(alpha = 0.7f),
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
                                    text = { Text(country, color = Color.White) },
                                    onClick = {
                                        selectedCountry = country
                                        isCountryDropdownOpen = false
                                        viewModel.performSearch("Top 50 $country")
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 7. Video Charts
                item(key = "video_charts") {
                    SimpSectionHeader(
                        title = "Video charts"
                    )

                    val videoChartItems = listOf(
                        HomeMediaItem(
                            id = "kPa7bsKwL-8",
                            title = "Top 100 Live Performances",
                            subtitle = "Global • YouTube Charts",
                            thumbnailUrl = "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg",
                            type = MediaType.TRACK
                        ),
                        HomeMediaItem(
                            id = "34Na4j8AVgA",
                            title = "Trending 20 $selectedCountry",
                            subtitle = "Daily • Top Viral Videos",
                            thumbnailUrl = "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg",
                            type = MediaType.TRACK
                        )
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(videoChartItems, key = { it.id }) { item ->
                            SimpMusicCard(
                                item = item,
                                onClick = { handleMediaItemClick(item, viewModel, navController) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 8. Top Artists Numbered List
                item(key = "top_artists_section") {
                    SimpSectionHeader(
                        title = "Top artists"
                    )

                    val topArtistsList = listOf(
                        Triple("Arijit Singh", "44.2M subscribers", "https://i.ytimg.com/vi/JFcgOboQZ08/hqdefault.jpg"),
                        Triple("The Weeknd", "36.5M subscribers", "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg"),
                        Triple("Taylor Swift", "59.1M subscribers", "https://i.ytimg.com/vi/e-ORhEE9VVg/hqdefault.jpg"),
                        Triple("Diljit Dosanjh", "16.8M subscribers", "https://i.ytimg.com/vi/mH_LFkW_oao/hqdefault.jpg")
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        topArtistsList.forEachIndexed { idx, (name, subs, thumb) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.performSearch(name)
                                        navController.navigate("search")
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.width(28.dp)
                                )
                                AsyncImage(
                                    model = thumb,
                                    contentDescription = name,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subs,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.55f)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 9. Weekly Top Podcast Shows
                item(key = "weekly_top_podcasts") {
                    SimpSectionHeader(
                        title = "Weekly top podcast shows"
                    )

                    val topPodcastsList = listOf(
                        Triple("The Joe Rogan Experience", "Joe Rogan", "https://i.ytimg.com/vi/WPni755-Krg/hqdefault.jpg"),
                        Triple("The Diary Of A CEO", "Steven Bartlett", "https://i.ytimg.com/vi/DWcJFNfaw9c/hqdefault.jpg"),
                        Triple("Huberman Lab", "Dr. Andrew Huberman", "https://i.ytimg.com/vi/n61ULEU7SU0/hqdefault.jpg")
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        topPodcastsList.forEachIndexed { idx, (title, host, thumb) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.performSearch(title)
                                        navController.navigate("search")
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.width(28.dp)
                                )
                                AsyncImage(
                                    model = thumb,
                                    contentDescription = title,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = host,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.55f)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 10. Local Library Highlights (Recently Played, On Repeat, Albums)
                if (recentlyPlayed.isNotEmpty()) {
                    item(key = "recently_played_shelf") {
                        SimpSectionHeader(title = "Recently Played")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(recentlyPlayed, key = { "rec_${it.id}" }) { track ->
                                HomeTrackCard(
                                    track = track,
                                    onClick = { viewModel.playTrack(track) },
                                    onLongClick = { longPressedTrack = track }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (onRepeatTracks.isNotEmpty()) {
                    item(key = "on_repeat_shelf") {
                        SimpSectionHeader(title = "Your On Repeat Mix")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(onRepeatTracks, key = { "repeat_${it.id}" }) { track ->
                                HomeTrackCard(
                                    track = track,
                                    onClick = { viewModel.playTrack(track) },
                                    onLongClick = { longPressedTrack = track }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (albums.isNotEmpty()) {
                    item(key = "albums_shelf") {
                        SimpSectionHeader(title = "Albums")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(albums, key = { "album_${it.id}_${it.title}" }) { album ->
                                HomeAlbumCard(
                                    album = album,
                                    onClick = { album.id?.let { id -> navController.navigate("album/$id") } },
                                    onLongClick = { longPressedAlbum = album }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // 11. Footer Credit matching SimpMusic
                item(key = "home_footer") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "© 2026 EcoDot v0.5.0",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = Color.White.copy(alpha = 0.35f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Powered by SimpMusic engine",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp
                            ),
                            color = Color.White.copy(alpha = 0.25f)
                        )
                    }
                }
            }
        }

        // ── 3. Top Bar Floating Liquid Glass Header ─────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Circular Liquid Glass Button
            LiquidGlassProfileButton(
                userProfile = userProfile,
                onClick = { navController.navigate("profile") }
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Capsule Strip: [All] & [Mood] (Expandable / Compound)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All Capsule
                LiquidGlassAllChip(
                    isSelected = selectedMood == null,
                    onClick = {
                        selectedMood = null
                        isMoodExpanded = false
                        viewModel.fetchHomeContent(null)
                        viewModel.fetchMoodRecommendations("Chill")
                    }
                )

                // Mood Expandable Compound Capsule
                LiquidGlassMoodChip(
                    selectedMood = selectedMood,
                    isExpanded = isMoodExpanded,
                    options = moodOptions,
                    onExpandChange = { isMoodExpanded = it },
                    onSelectMood = { mood ->
                        selectedMood = mood
                        isMoodExpanded = false
                        viewModel.setHomeMood(mood)
                    }
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action Liquid Glass Pill: [+] and [⚙]
            LiquidGlassActionPill(
                onCreateClick = { showCreatePlaylistSheet = true },
                onSettingsClick = { onEqualizerClick() }
            )
        }

        // ── 4. Bottom Sheets & Dialogs ───────────────────────────────────────
        if (showCreatePlaylistSheet) {
            CreatePlaylistBottomSheet(
                onDismiss = { showCreatePlaylistSheet = false },
                onCreate = { name, _, _ ->
                    viewModel.createPlaylist(name)
                    showCreatePlaylistSheet = false
                }
            )
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

// ── Crystal Liquid Glass Modifier for Header Capsules ───────────────────────
@Composable
fun Modifier.headerGlass(
    shape: Shape,
    specularAlpha: Float = 0.45f,
    bgAlpha: Float = 0.22f
): Modifier {
    val hazeState = LocalHazeState.current
    return this
        .clip(shape)
        .then(
            if (hazeState != null) {
                Modifier.hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Color.Transparent,
                        tint = HazeDefaults.tint(Color.White.copy(alpha = 0.04f)),
                        blurRadius = 6.dp,
                        noiseFactor = 0f
                    )
                )
            } else Modifier
        )
        .background(Color.Black.copy(alpha = bgAlpha))
        .border(
            0.85.dp,
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = specularAlpha),
                    Color.White.copy(alpha = specularAlpha * 0.35f),
                    Color.White.copy(alpha = 0.05f)
                )
            ),
            shape
        )
}

// ── Liquid Glass Profile Button (Circle 40dp) ────────────────────────────────
@Composable
fun LiquidGlassProfileButton(
    userProfile: UserProfile?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .size(40.dp)
            .headerGlass(shape = shape, specularAlpha = 0.50f, bgAlpha = 0.22f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!userProfile?.avatarUri.isNullOrEmpty()) {
            AsyncImage(
                model = userProfile?.avatarUri,
                contentDescription = "Profile",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Profile",
                tint = Color.White.copy(alpha = 0.90f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Liquid Glass [All] Capsule ───────────────────────────────────────────────
@Composable
fun LiquidGlassAllChip(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    if (isSelected) {
        // SimpMusic Active Lavender Solid Pill
        Box(
            modifier = modifier
                .height(36.dp)
                .clip(shape)
                .background(Color(0xFFE7D7FD))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "All",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = Color(0xFF1E1035)
            )
        }
    } else {
        // Crystal-Clear Liquid Glass Pill
        Box(
            modifier = modifier
                .height(36.dp)
                .headerGlass(shape = shape, specularAlpha = 0.40f, bgAlpha = 0.18f)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "All",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = Color.White.copy(alpha = 0.70f)
            )
        }
    }
}

// ── Liquid Glass [Mood] Expandable Compound Capsule ─────────────────────────
@Composable
fun LiquidGlassMoodChip(
    selectedMood: String?,
    isExpanded: Boolean,
    options: List<String>,
    onExpandChange: (Boolean) -> Unit,
    onSelectMood: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    // CASE 1: Expanded state (Horizontal row showing active Mood + options)
    if (isExpanded) {
        Row(
            modifier = modifier
                .height(36.dp)
                .clip(shape)
                .background(Color(0xFF2C2040))
                .border(1.dp, Color(0xFF4A3A68), shape)
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Mood" Head Pill
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE7D7FD))
                    .clickable { onExpandChange(false) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mood",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = Color(0xFF1E1035)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Mood options list
            options.forEach { mood ->
                val isCurrent = selectedMood == mood
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCurrent) Color(0xFF4E376E) else Color.Transparent)
                        .clickable { onSelectMood(mood) }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mood,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = if (isCurrent) Color(0xFFE7D7FD) else Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
    // CASE 2: Folded state with a mood selected -> [ Mood | Relax ]
    else if (selectedMood != null) {
        Row(
            modifier = modifier
                .height(36.dp)
                .clip(shape)
                .background(Color(0xFF281E3B))
                .border(1.dp, Color(0xFF483563), shape)
                .clickable { onExpandChange(true) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Mood" segment
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFE7D7FD))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mood",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = Color(0xFF1E1035)
                )
            }

            // Selected Mood segment (e.g. "Relax")
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedMood,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = Color(0xFFE7D7FD)
                )
            }
        }
    }
    // CASE 3: Unselected collapsed -> Single Liquid Glass "Mood" Capsule
    else {
        Box(
            modifier = modifier
                .height(36.dp)
                .headerGlass(shape = shape, specularAlpha = 0.40f, bgAlpha = 0.18f)
                .clickable { onExpandChange(true) }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mood",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = Color.White.copy(alpha = 0.70f)
            )
        }
    }
}

// ── Liquid Glass Action Pill [ + | ⚙ ] ──────────────────────────────────────
@Composable
fun LiquidGlassActionPill(
    onCreateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .height(40.dp)
            .headerGlass(shape = shape, specularAlpha = 0.48f, bgAlpha = 0.18f)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCreateClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Create",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

// ── Dynamic Background & Ambience Lights ────────────────────────────────────
@Composable
fun HomeAmbientBackground(
    activeMood: String?,
    dominantColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_bokeh")

    // Subtle breathing pulse for bokeh lights
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bokeh_pulse"
    )

    // Dynamic color palette per mood
    val (targetColor1, targetColor2, targetColor3) = when (activeMood?.lowercase()) {
        "relax", "chill" -> Triple(
            Color(0xFF38234D), // Dusk violet
            Color(0xFF1E284A), // Twilight navy
            Color(0xFF121B2F)  // Deep midnight
        )
        "workout", "energize" -> Triple(
            Color(0xFFE63946), // Electric crimson
            Color(0xFFFB8500), // Vibrant orange
            Color(0xFF6B1116)  // Deep garnet
        )
        "focus" -> Triple(
            Color(0xFF0077B6), // Midnight teal
            Color(0xFF00B4D8), // Cyan neon
            Color(0xFF03264A)  // Deep indigo
        )
        "romance" -> Triple(
            Color(0xFFE05780), // Rose
            Color(0xFF9D0208), // Romantic ruby
            Color(0xFF3A0B1A)  // Dark crimson
        )
        "party" -> Triple(
            Color(0xFF7209B7), // Neon purple
            Color(0xFFF72585), // Magenta
            Color(0xFF180A33)  // Club dark
        )
        "sleep", "sad" -> Triple(
            Color(0xFF1E293B), // Midnight slate
            Color(0xFF0F172A), // Dark starry navy
            Color(0xFF090D16)  // Pitch
        )
        // Default "All": Warm Golden Bokeh Lights matching Screenshot 2
        else -> Triple(
            Color(0xFFE5A93B), // Warm Golden Amber
            Color(0xFFC88132), // Warm Bronze
            Color(0xFF8B5E3C)  // Warm Ochre
        )
    }

    val color1 by animateColorAsState(targetColor1, tween(700), label = "ambient_c1")
    val color2 by animateColorAsState(targetColor2, tween(700), label = "ambient_c2")
    val color3 by animateColorAsState(targetColor3, tween(700), label = "ambient_c3")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Primary Glow Orb (Top Center-Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1.copy(alpha = 0.50f), Color.Transparent),
                    center = Offset(w * 0.28f, h * 0.22f),
                    radius = (w * 0.45f) * pulse
                ),
                center = Offset(w * 0.28f, h * 0.22f),
                radius = (w * 0.45f) * pulse
            )

            // 2. Secondary Glow Orb (Top Center-Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color2.copy(alpha = 0.42f), Color.Transparent),
                    center = Offset(w * 0.76f, h * 0.18f),
                    radius = (w * 0.42f) * (2f - pulse)
                ),
                center = Offset(w * 0.76f, h * 0.18f),
                radius = (w * 0.42f) * (2f - pulse)
            )

            // 3. Lower Tertiary Ambient Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color3.copy(alpha = 0.32f), Color.Transparent),
                    center = Offset(w * 0.52f, h * 0.42f),
                    radius = (w * 0.38f)
                ),
                center = Offset(w * 0.52f, h * 0.42f),
                radius = (w * 0.38f)
            )

            // 4. Tropical Palm Leaves Silhouette for "Relax" / "Chill"
            if (activeMood?.equals("relax", true) == true || activeMood?.equals("chill", true) == true) {
                // Top-right palm silhouette
                val palmPath = Path().apply {
                    moveTo(w * 0.75f, 0f)
                    quadraticTo(w * 0.88f, h * 0.12f, w, h * 0.08f)
                    quadraticTo(w * 0.90f, h * 0.22f, w, h * 0.25f)
                    quadraticTo(w * 0.82f, h * 0.18f, w * 0.85f, h * 0.32f)
                    quadraticTo(w * 0.75f, h * 0.16f, w * 0.70f, h * 0.24f)
                    close()
                }
                drawPath(palmPath, Color.Black.copy(alpha = 0.55f))

                // Top-left palm silhouette
                val palmLeft = Path().apply {
                    moveTo(w * 0.25f, 0f)
                    quadraticTo(w * 0.12f, h * 0.10f, 0f, h * 0.06f)
                    quadraticTo(w * 0.08f, h * 0.18f, 0f, h * 0.22f)
                    close()
                }
                drawPath(palmLeft, Color.Black.copy(alpha = 0.45f))
            }
        }

        // Seamless Scrim dissolving into dark page background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .align(Alignment.BottomCenter)
                .background(artworkScrimBrush(Color(0xFF070709)))
        )
    }
}

// ── Section Header with Eyebrow and Action Button ────────────────────────────
@Composable
fun SimpSectionHeader(
    title: String,
    eyebrow: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            if (!eyebrow.isNullOrBlank()) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = Color.White.copy(alpha = 0.55f)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                ),
                color = Color.White
            )
        }
        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White.copy(alpha = 0.70f),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onActionClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

// ── 3D Depth Parallax Modifier for Carousel Cards ───────────────────────────
@Composable
fun Modifier.carouselCard3D(
    onPositionDelta: (Float) -> Unit
): Modifier {
    var itemCenterX by remember { mutableFloatStateOf(0f) }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val localDensity = androidx.compose.ui.platform.LocalDensity.current
    val screenWidthPx = with(localDensity) { configuration.screenWidthDp.dp.toPx() }
    val screenCenter = screenWidthPx / 2f

    return this
        .onGloballyPositioned { coordinates ->
            if (coordinates.isAttached) {
                val windowPos = coordinates.positionInWindow()
                val centerX = windowPos.x + coordinates.size.width / 2f
                itemCenterX = centerX
                if (screenCenter > 0f) {
                    val delta = ((centerX - screenCenter) / screenCenter).coerceIn(-1.2f, 1.2f)
                    onPositionDelta(delta)
                }
            }
        }
        .graphicsLayer {
            if (screenCenter > 0f && itemCenterX > 0f) {
                val delta = ((itemCenterX - screenCenter) / screenCenter).coerceIn(-1.2f, 1.2f)
                cameraDistance = 14f * this.density
                // Microscopic 3D inward tilt: cards approaching screen edges angle slightly towards viewer
                rotationY = delta * -5.5f
            }
        }
}

// ── SimpMusic Playlist / Album Card with Play Overlay Icon ───────────────────
@Composable
fun SimpMusicCard(
    item: HomeMediaItem,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "card_scale")
    val shape = RoundedCornerShape(14.dp)
    var itemDelta by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .width(148.dp)
            .carouselCard3D { itemDelta = it }
            .scale(scale)
            .animatedCombinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(148.dp)
                .clip(shape)
                .background(Color(0xFF1C1E26))
        ) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(item.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Internal artwork stereoscopic parallax with 8% bleed margin
                        scaleX = 1.08f
                        scaleY = 1.08f
                        translationX = itemDelta * 7.dp.toPx()
                    },
                contentScale = ContentScale.Crop
            )

            // Circular Play Overlay Icon in Top-Left (SimpMusic signature)
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(0.7.dp, Color.White.copy(alpha = 0.30f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
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
        if (!item.subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Music Video 16:9 Card ───────────────────────────────────────────────────
@Composable
fun MusicVideoCard(
    video: HomeMediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var itemDelta by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .width(220.dp)
            .carouselCard3D { itemDelta = it }
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1E26))
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.08f
                        scaleY = 1.08f
                        translationX = itemDelta * 8.dp.toPx()
                    },
                contentScale = ContentScale.Crop
            )
            // Duration badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "3:42",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = video.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = video.subtitle ?: "Music Video",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color.White.copy(alpha = 0.55f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Moods & Genres Pill Card with Accent Color Strip ─────────────────────────
@Composable
fun MoodPillCard(
    title: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF16161D))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical colored accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Quick Picks Item ────────────────────────────────────────────────────────
@Composable
fun QuickPicksItem(
    track: Track,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val itemWidth = (configuration.screenWidthDp.dp - 44.dp).coerceAtLeast(280.dp)

    Row(
        modifier = modifier
            .width(itemWidth)
            .height(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .animatedCombinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(track.albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = if (isPlaying) EcoDotRed else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                    ExplicitBadge(modifier = Modifier.padding(end = 4.dp))
                }
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (isPlaying) {
            Spacer(modifier = Modifier.width(8.dp))
            MiniEqualizerBars(modifier = Modifier.padding(end = 4.dp))
        }
    }
}

// ── Mini Equalizer Bars ─────────────────────────────────────────────────────
@Composable
fun MiniEqualizerBars(modifier: Modifier = Modifier, color: Color = EcoDotRed) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(360, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(510, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        modifier = modifier.size(width = 16.dp, height = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight(h1).clip(RoundedCornerShape(1.dp)).background(color))
        Box(modifier = Modifier.weight(1f).fillMaxHeight(h2).clip(RoundedCornerShape(1.dp)).background(color))
        Box(modifier = Modifier.weight(1f).fillMaxHeight(h3).clip(RoundedCornerShape(1.dp)).background(color))
    }
}

// ── Track & Album Cards for Local Shelves ────────────────────────────────────
@Composable
fun HomeTrackCard(
    track: Track,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var itemDelta by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .width(145.dp)
            .carouselCard3D { itemDelta = it }
            .animatedCombinedClickable(onLongClick, onClick)
    ) {
        Box(
            modifier = Modifier
                .size(145.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1E26))
        ) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(track.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.08f
                        scaleY = 1.08f
                        translationX = itemDelta * 7.dp.toPx()
                    },
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = track.title,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                ExplicitBadge(modifier = Modifier.padding(end = 4.dp))
            }
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
    var itemDelta by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .width(145.dp)
            .carouselCard3D { itemDelta = it }
            .animatedCombinedClickable(onLongClick, onClick)
    ) {
        Box(
            modifier = Modifier
                .size(145.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1E26))
        ) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(album.artUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.08f
                        scaleY = 1.08f
                        translationX = itemDelta * 7.dp.toPx()
                    },
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
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
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            color = Color.White.copy(alpha = 0.55f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Helper click handlers ───────────────────────────────────────────────────
private fun handleMediaItemClick(item: HomeMediaItem, viewModel: MusicViewModel, navController: NavController) {
    when (item.type) {
        MediaType.TRACK -> {
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
        MediaType.ALBUM,
        MediaType.PLAYLIST -> {
            navController.navigate("album/${item.id}")
        }
        MediaType.ARTIST -> {
            navController.navigate("artist/${item.id}")
        }
        MediaType.UNKNOWN -> {
            viewModel.performSearch(item.title)
            navController.navigate("search")
        }
    }
}

private fun handleMediaItemLongClick(
    item: HomeMediaItem,
    viewModel: MusicViewModel,
    onTrackLongPress: (Track) -> Unit,
    onAlbumLongPress: (Album) -> Unit,
    onPlaylistLongPress: (Playlist) -> Unit
) {
    when (item.type) {
        MediaType.TRACK -> {
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
            onTrackLongPress(track)
        }
        MediaType.ALBUM -> {
            val album = Album(
                id = item.id,
                title = item.title,
                artist = item.subtitle ?: "Various Artists",
                artUri = item.thumbnailUrl,
                tracks = emptyList()
            )
            onAlbumLongPress(album)
        }
        MediaType.PLAYLIST -> {
            val playlist = Playlist(
                id = 0L,
                name = item.title,
                description = item.subtitle ?: "",
                coverArtUri = item.thumbnailUrl
            )
            onPlaylistLongPress(playlist)
        }
        else -> {}
    }
}

// ── Real Verified YouTube Music Songs and Playlists ──────────────────────────
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
                HomeMediaItem(
                    id = "5qap5aO4i9A",
                    title = "Lofi Hip Hop Radio",
                    subtitle = "Lofi Girl • Beats to Relax/Study",
                    thumbnailUrl = "https://i.ytimg.com/vi/5qap5aO4i9A/hqdefault.jpg",
                    type = MediaType.TRACK
                ),
                HomeMediaItem(
                    id = "DWcJFNfaw9c",
                    title = "Weightless (Original)",
                    subtitle = "Marconi Union • Ambient Relax",
                    thumbnailUrl = "https://i.ytimg.com/vi/DWcJFNfaw9c/hqdefault.jpg",
                    type = MediaType.TRACK
                ),
                HomeMediaItem(
                    id = "jfKfPfyJRdk",
                    title = "Relaxing Piano Music",
                    subtitle = "Peaceful Dream • Soft Melodies",
                    thumbnailUrl = "https://i.ytimg.com/vi/jfKfPfyJRdk/hqdefault.jpg",
                    type = MediaType.TRACK
                )
            )
        } else {
            listOf(
                HomeMediaItem(
                    id = "ilNt2bikxDI",
                    title = "Raataan Lambiyan",
                    subtitle = "Jubin Nautiyal, Asees Kaur",
                    thumbnailUrl = "https://i.ytimg.com/vi/ilNt2bikxDI/hqdefault.jpg",
                    type = MediaType.TRACK
                ),
                HomeMediaItem(
                    id = "VAdGW7QDJUI",
                    title = "Chaleya (Acoustic)",
                    subtitle = "Arijit Singh, Shilpa Rao",
                    thumbnailUrl = "https://i.ytimg.com/vi/VAdGW7QDJUI/hqdefault.jpg",
                    type = MediaType.TRACK
                ),
                HomeMediaItem(
                    id = "WPni755-Krg",
                    title = "Sunset Lover",
                    subtitle = "Petit Biscuit",
                    thumbnailUrl = "https://i.ytimg.com/vi/WPni755-Krg/hqdefault.jpg",
                    type = MediaType.TRACK
                )
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
        "focus" -> if (shelfIndex == 1) {
            listOf(
                HomeMediaItem("n61ULEU7SU0", "Resonance", "HOME • Synthwave Beats", "https://i.ytimg.com/vi/n61ULEU7SU0/hqdefault.jpg", MediaType.TRACK),
                HomeMediaItem("2OEL4P1Rz04", "Clair de Lune", "Claude Debussy • Classical", "https://i.ytimg.com/vi/2OEL4P1Rz04/hqdefault.jpg", MediaType.TRACK),
                HomeMediaItem("5qap5aO4i9A", "Deep Focus Beats", "Lofi Girl", "https://i.ytimg.com/vi/5qap5aO4i9A/hqdefault.jpg", MediaType.TRACK)
            )
        } else {
            listOf(
                HomeMediaItem("DWcJFNfaw9c", "Binaural Study Alpha", "Marconi Union", "https://i.ytimg.com/vi/DWcJFNfaw9c/hqdefault.jpg", MediaType.TRACK),
                HomeMediaItem("jfKfPfyJRdk", "Nocturne Op. 9 No. 2", "Chopin", "https://i.ytimg.com/vi/jfKfPfyJRdk/hqdefault.jpg", MediaType.TRACK)
            )
        }
        else -> listOf(
            HomeMediaItem("kPa7bsKwL-8", "Die With A Smile", "Lady Gaga & Bruno Mars", "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg", MediaType.TRACK),
            HomeMediaItem("KRaWnd3LJfs", "Payphone", "Maroon 5 ft. Wiz Khalifa", "https://i.ytimg.com/vi/KRaWnd3LJfs/hqdefault.jpg", MediaType.TRACK, isExplicit = true),
            HomeMediaItem("yzTuBuRdAyA", "The Hills", "The Weeknd", "https://i.ytimg.com/vi/yzTuBuRdAyA/hqdefault.jpg", MediaType.TRACK, isExplicit = true)
        )
    }
}

private fun getSampleMusicVideos(): List<HomeMediaItem> = listOf(
    HomeMediaItem(
        id = "kPa7bsKwL-8",
        title = "Die With A Smile (Official Video)",
        subtitle = "Lady Gaga & Bruno Mars • 420M views",
        thumbnailUrl = "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg",
        type = MediaType.TRACK
    ),
    HomeMediaItem(
        id = "34Na4j8AVgA",
        title = "Starboy (Official Music Video)",
        subtitle = "The Weeknd ft. Daft Punk • 2.5B views",
        thumbnailUrl = "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg",
        type = MediaType.TRACK
    ),
    HomeMediaItem(
        id = "4NRXx6U8ABQ",
        title = "Blinding Lights (Official Video)",
        subtitle = "The Weeknd • 1.1B views",
        thumbnailUrl = "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
        type = MediaType.TRACK
    ),
    HomeMediaItem(
        id = "VAdGW7QDJUI",
        title = "Chaleya (Official Music Video)",
        subtitle = "Arijit Singh, Shilpa Rao • 480M views",
        thumbnailUrl = "https://i.ytimg.com/vi/VAdGW7QDJUI/hqdefault.jpg",
        type = MediaType.TRACK
    )
)
