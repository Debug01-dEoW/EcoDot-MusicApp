@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.example.ecodot.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.remote.HomeMediaItem
import com.example.ecodot.ui.components.AddToPlaylistDialog
import com.example.ecodot.ui.components.SimpQuickPicksRowItem
import com.example.ecodot.ui.components.SimpSectionHeader
import com.example.ecodot.ui.components.TrackOptionsBottomSheet
import com.example.ecodot.ui.theme.animatedCombinedClickable
import com.example.ecodot.ui.viewmodel.Album
import com.example.ecodot.ui.viewmodel.Artist
import com.example.ecodot.ui.viewmodel.MusicViewModel

private enum class BrowseFilterTab(val label: String) {
    ALL("All"),
    SONGS("Songs"),
    ALBUMS("Albums"),
    PLAYLISTS("Playlists"),
    VIDEOS("Videos")
}

/**
 * Full-screen SimpMusic-style "More" / Category Browse page showing Related Songs, Albums,
 * Playlists, Music Videos, and Artists for any Home shelf or Mood/Genre card.
 */
@Composable
fun BrowseScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    onWatchVideo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.browsePageState.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val downloadProgressMap by viewModel.downloadProgress.collectAsState()

    var selectedTab by remember(state.title) { mutableStateOf(BrowseFilterTab.ALL) }
    var longPressedTrack by remember { mutableStateOf<Track?>(null) }
    var showPlaylistPicker by remember { mutableStateOf<Track?>(null) }

    val availableTabs = remember(state) {
        buildList {
            add(BrowseFilterTab.ALL)
            if (state.songs.isNotEmpty()) add(BrowseFilterTab.SONGS)
            if (state.albums.isNotEmpty()) add(BrowseFilterTab.ALBUMS)
            if (state.playlists.isNotEmpty()) add(BrowseFilterTab.PLAYLISTS)
            if (state.videos.isNotEmpty()) add(BrowseFilterTab.VIDEOS)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070709))
    ) {
        // Subtle SimpMusic violet/slate top glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF332452).copy(alpha = 0.55f),
                            Color(0xFF141522).copy(alpha = 0.30f),
                            Color(0xFF070709)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(0.8.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (!state.strapline.isNullOrBlank()) {
                        Text(
                            text = state.strapline!!.uppercase(),
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = state.title.ifBlank { "Explore" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (state.songs.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE7D7FD))
                            .clickable { viewModel.playQueue(state.songs, startIndex = 0) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF1E1035),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Play all",
                                color = Color(0xFF1E1035),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.refreshBrowsePage() },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableTabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFFE7D7FD) else Color.White.copy(alpha = 0.08f)
                            )
                            .border(
                                0.8.dp,
                                if (isSelected) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.18f),
                                CircleShape
                            )
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = tab.label,
                            color = if (isSelected) Color(0xFF1E1035) else Color.White.copy(alpha = 0.88f),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            val listState = rememberLazyListState()
            val hasAnyContent = state.songs.isNotEmpty() ||
                state.albums.isNotEmpty() ||
                state.playlists.isNotEmpty() ||
                state.videos.isNotEmpty()

            LaunchedEffect(state.title, state.browseId, state.moodFilterName, selectedTab) {
                listState.scrollToItem(0)
            }

            if (state.isLoading && hasAnyContent) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = Color(0xFFE7D7FD),
                    trackColor = Color.White.copy(alpha = 0.08f)
                )
            }

            if (state.isLoading && !hasAnyContent) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFE7D7FD))
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 132.dp)
                ) {
                    item(key = "browse_top_anchor") {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // 1. Related Songs
                    val showSongs = selectedTab == BrowseFilterTab.ALL || selectedTab == BrowseFilterTab.SONGS
                    if (showSongs && state.songs.isNotEmpty()) {
                        item(key = "browse_songs_header") {
                            SimpSectionHeader(
                                eyebrow = "TRACKS & SINGLES",
                                title = "Related songs",
                                actionText = "Shuffle",
                                onActionClick = { viewModel.playQueue(state.songs.shuffled(), startIndex = 0) }
                            )
                        }
                        val songLimit = if (selectedTab == BrowseFilterTab.ALL) 10 else state.songs.size
                        itemsIndexed(
                            items = state.songs.take(songLimit),
                            key = { idx, track -> "bsong_${idx}_${track.id}" }
                        ) { _, track ->
                            Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                                SimpQuickPicksRowItem(
                                    track = track,
                                    isPlaying = isPlaying && currentTrack?.id == track.id,
                                    itemWidth = 360.dp,
                                    onClick = { viewModel.playTrack(track) },
                                    onLongClick = { longPressedTrack = track }
                                )
                            }
                        }
                        if (selectedTab == BrowseFilterTab.ALL && state.songs.size > 10) {
                            item(key = "browse_see_all_songs") {
                                Text(
                                    text = "Show all ${state.songs.size} songs",
                                    color = Color(0xFFE7D7FD),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .clip(CircleShape)
                                        .clickable { selectedTab = BrowseFilterTab.SONGS }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 2. Related Albums
                    val showAlbums = selectedTab == BrowseFilterTab.ALL || selectedTab == BrowseFilterTab.ALBUMS
                    if (showAlbums && state.albums.isNotEmpty()) {
                        item(key = "browse_albums_section") {
                            SimpSectionHeader(
                                eyebrow = "RECORDS & EPS",
                                title = "Related albums"
                            )
                            SimpLazyRow {
                                itemsIndexed(
                                    items = state.albums,
                                    key = { idx, album -> "balbum_${idx}_${album.id ?: album.title}" }
                                ) { _, album ->
                                    BrowseAlbumTile(
                                        album = album,
                                        onClick = { album.id?.let { id -> navController.navigate("album/$id") } }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // 3. Related Playlists
                    val showPlaylists = selectedTab == BrowseFilterTab.ALL || selectedTab == BrowseFilterTab.PLAYLISTS
                    if (showPlaylists && state.playlists.isNotEmpty()) {
                        item(key = "browse_playlists_section") {
                            SimpSectionHeader(
                                eyebrow = "CURATED COLLECTIONS",
                                title = "Related playlists"
                            )
                            SimpLazyRow {
                                itemsIndexed(
                                    items = state.playlists,
                                    key = { idx, pl -> "bpl_${idx}_${pl.id}" }
                                ) { _, pl ->
                                    SimpMusicCard(
                                        item = pl,
                                        onClick = { navController.navigate("album/${pl.id}") }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // 4. Related Videos
                    val showVideos = selectedTab == BrowseFilterTab.ALL || selectedTab == BrowseFilterTab.VIDEOS
                    if (showVideos && state.videos.isNotEmpty()) {
                        item(key = "browse_videos_section") {
                            SimpSectionHeader(
                                eyebrow = "MUSIC VIDEOS",
                                title = "Related videos"
                            )
                            SimpLazyRow {
                                itemsIndexed(
                                    items = state.videos,
                                    key = { idx, vid -> "bvid_${idx}_${vid.id}" }
                                ) { _, vid ->
                                    MusicVideoCard(
                                        video = vid,
                                        onClick = {
                                            val videoTrack = Track(
                                                id = vid.id,
                                                title = vid.title,
                                                artist = vid.subtitle.ifBlank { "YouTube Video" },
                                                album = "YouTube Video",
                                                albumArtUri = vid.thumbnailUrl,
                                                duration = 0,
                                                path = "ytvideo://${vid.id}",
                                                isYouTube = true
                                            )
                                            viewModel.playTrack(videoTrack, isVideo = true)
                                            viewModel.setVideoMode(true)
                                            onWatchVideo()
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // 5. Related Artists
                    if (selectedTab == BrowseFilterTab.ALL && state.artists.isNotEmpty()) {
                        item(key = "browse_artists_section") {
                            SimpSectionHeader(
                                eyebrow = "FEATURED ARTISTS",
                                title = "Artists"
                            )
                            SimpLazyRow {
                                itemsIndexed(
                                    items = state.artists,
                                    key = { idx, a -> "bart_${idx}_${a.id.ifBlank { a.name }}" }
                                ) { _, artist ->
                                    BrowseArtistCircle(
                                        artist = artist,
                                        onClick = {
                                            if (artist.id.startsWith("UC")) {
                                                navController.navigate("artist/${artist.id}")
                                            } else {
                                                viewModel.performSearch(artist.name, type = "Artists")
                                                navController.navigate("search")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (!state.isLoading &&
                        state.songs.isEmpty() &&
                        state.albums.isEmpty() &&
                        state.playlists.isEmpty() &&
                        state.videos.isEmpty()
                    ) {
                        item(key = "browse_empty") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 64.dp, horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No related results loaded yet. Tap refresh to try again.",
                                    color = Color.White.copy(alpha = 0.65f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        longPressedTrack?.let { track ->
            val isDownloaded = downloadedTracks.any { it.id == track.id }
            val downloadProgress = downloadProgressMap[track.id]
            val isLiked by viewModel.isTrackLiked(track.id).collectAsState(initial = false)
            val isInQueue = viewModel.playbackQueue.collectAsState().value.any { it.id == track.id }

            TrackOptionsBottomSheet(
                track = track,
                onDismiss = { longPressedTrack = null },
                onPlayNext = { viewModel.playNext(track) },
                onAddToQueue = { viewModel.addToQueue(track) },
                onRemoveFromQueue = if (isInQueue) { { viewModel.removeTrackFromQueue(track) } } else null,
                onGoToAlbum = {
                    longPressedTrack = null
                    if (!track.albumId.isNullOrEmpty()) navController.navigate("album/${track.albumId}")
                },
                onGoToArtist = {
                    longPressedTrack = null
                    if (!track.artistId.isNullOrEmpty()) navController.navigate("artist/${track.artistId}")
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

@Composable
private fun BrowseAlbumTile(
    album: Album,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(148.dp)
            .animatedCombinedClickable(onLongClick = null, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(148.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF161722))
                .border(0.6.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
        ) {
            AsyncImage(
                model = album.artUri,
                contentDescription = album.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
        if (album.artist.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = album.artist,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFFA8A8A8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BrowseArtistCircle(
    artist: Artist,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = artist.imageUrl,
            contentDescription = artist.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(Color(0xFF161722))
                .border(0.7.dp, Color.White.copy(alpha = 0.16f), CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist.name,
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
