package com.example.ecodot.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.local.prefs.AudioQuality
import com.example.ecodot.data.local.prefs.PlayerStyle
import com.example.ecodot.data.local.prefs.UserPreferencesManager
import com.example.ecodot.data.remote.YouTubeParser
import com.example.ecodot.ui.components.player.PlayerStyleBottomSheet
import com.example.ecodot.ui.components.player.PlayerStyleMath
import com.example.ecodot.ui.theme.poppinsFamily
import com.example.ecodot.ui.viewmodel.MusicViewModel

private val CURATED_PROFILE_GENRES = listOf(
    "Pop", "Lo-Fi", "Hip-Hop", "Indie", "Electronic", "Rock",
    "R&B", "Jazz", "Bollywood", "Phonk", "Classical", "Ambient"
)

private val ProfileBg = Color(0xFF090A0D)
private val ProfileSurface = Color(0xFF13141A)
private val ProfileTonalButton = Color(0xFF1C1E26)
private val ProfileHairline = Color.White.copy(alpha = 0.06f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    onEqualizerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val mostPlayed by viewModel.mostPlayed.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val playbackHistory by viewModel.playbackHistory.collectAsState()
    val totalMinutes by viewModel.totalMinutesListened.collectAsState()
    val topGenre by viewModel.topGenre.collectAsState()
    val likedTrackIds by viewModel.likedTrackIds.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val followedArtists by viewModel.followedArtists.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val cachedTracks by viewModel.cachedTracks.collectAsState()
    val playerStyle by viewModel.playerStyle.collectAsState()
    val audioQuality by viewModel.audioQuality.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()

    var showEditProfile by remember { mutableStateOf(false) }
    var showResetProfileDialog by remember { mutableStateOf(false) }
    var showPlayerStyleSheet by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.updateProfile(
                name = userProfile?.name ?: "EcoDot Listener",
                genres = userProfile?.favoriteGenres ?: "",
                avatarUrl = uri.toString(),
                isPro = userProfile?.isPro ?: true
            )
        }
    }

    val offlineUniqueCount = remember(downloadedTracks, cachedTracks) {
        (downloadedTracks.map { it.id } + cachedTracks.map { it.id }).distinct().size
    }
    val favoriteGenreList = remember(userProfile?.favoriteGenres) {
        PlayerStyleMath.parseFavoriteGenres(userProfile?.favoriteGenres.orEmpty())
    }
    val resolvedAccent = if (accentColor == Color.Black) Color(0xFFEADBC8) else accentColor
    val topTrack = mostPlayed.firstOrNull()
    val displayName = userProfile?.name?.ifBlank { "EcoDot Listener" } ?: "EcoDot Listener"

    // Quiet, natural header scrim like Spotify/YouTube Music — never a harsh neon glow
    val headerWashBrush = remember(dominantColor) {
        val mutedTop = lerp(Color(0xFF171922), dominantColor, 0.14f)
        Brush.verticalGradient(
            colors = listOf(mutedTop, ProfileBg)
        )
    }

    val listState = rememberLazyListState()
    val showCollapsedTitle by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 140
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ProfileBg)
    ) {
        // Subtle charcoal-to-black editorial wash anchored at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .drawBehind { drawRect(headerWashBrush) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── 1. Clean Top Navigation Bar ───────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Text(
                        text = displayName,
                        fontFamily = poppinsFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer {
                            alpha = if (showCollapsedTitle) 1f else 0f
                        }
                    )
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {
                // ── 2. Open-Canvas Profile Header (Spotify / Apple Music style) ──
                item(key = "profile_header", contentType = "header") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 8.dp, bottom = 18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clickable { imagePicker.launch("image/*") }
                            ) {
                                val avatarUri = userProfile?.avatarUri
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(ProfileTonalButton),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!avatarUri.isNullOrBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(avatarUri)
                                                .size(256, 256)
                                                .crossfade(160)
                                                .build(),
                                            contentDescription = "Profile Picture",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        val initial = displayName.firstOrNull()?.uppercaseChar()
                                        if (initial != null && initial.isLetter()) {
                                            Text(
                                                text = initial.toString(),
                                                fontFamily = poppinsFamily,
                                                fontSize = 30.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.86f)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.Person,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.72f),
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF232530))
                                        .border(2.dp, ProfileBg, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CameraAlt,
                                        contentDescription = "Change avatar",
                                        tint = Color.White.copy(alpha = 0.86f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(18.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = displayName,
                                        fontFamily = poppinsFamily,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.3).sp,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (userProfile?.isPro == true) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Rounded.Verified,
                                            contentDescription = "Hi-Fi Pro",
                                            tint = Color.White.copy(alpha = 0.78f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${likedTrackIds.size} liked · ${allPlaylists.size} playlists · ${followedArtists.size} artists",
                                    fontFamily = poppinsFamily,
                                    fontSize = 12.5.sp,
                                    color = Color.White.copy(alpha = 0.62f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "${audioQualityShortLabel(audioQuality)} · ${profileStyleDisplayName(playerStyle)}",
                                    fontFamily = poppinsFamily,
                                    fontSize = 11.5.sp,
                                    color = Color.White.copy(alpha = 0.44f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Clean borderless pill actions row (like Spotify's Edit / Style pills)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ProfileTonalButton,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clickable { showEditProfile = true }
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Edit profile",
                                        fontFamily = poppinsFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = ProfileTonalButton,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clickable { showPlayerStyleSheet = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Style,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.80f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = profileStyleShortName(playerStyle),
                                        fontFamily = poppinsFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = ProfileTonalButton,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable(onClick = onEqualizerClick)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Equalizer,
                                        contentDescription = "Equalizer",
                                        tint = Color.White.copy(alpha = 0.82f),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (favoriteGenreList.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(favoriteGenreList, key = { it }) { genre ->
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF15171F)
                                    ) {
                                        Text(
                                            text = genre,
                                            fontFamily = poppinsFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.White.copy(alpha = 0.74f),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showEditProfile = true }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.56f),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add favorite genres",
                                    fontFamily = poppinsFamily,
                                    fontSize = 12.5.sp,
                                    color = Color.White.copy(alpha = 0.56f)
                                )
                            }
                        }
                    }
                }

                // ── 3. Clean Library Metrics Bar (Single flat surface, hairline dividers) ──
                item(key = "library_metrics", contentType = "metrics_bar") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ProfileSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LibrarySummaryStat(
                                count = likedTrackIds.size,
                                label = "Liked",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("library") }
                            )
                            VerticalDivider(
                                color = ProfileHairline,
                                thickness = 0.5.dp,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(vertical = 4.dp)
                            )
                            LibrarySummaryStat(
                                count = allPlaylists.size,
                                label = "Playlists",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("library") }
                            )
                            VerticalDivider(
                                color = ProfileHairline,
                                thickness = 0.5.dp,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(vertical = 4.dp)
                            )
                            LibrarySummaryStat(
                                count = followedArtists.size,
                                label = "Artists",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("library") }
                            )
                            VerticalDivider(
                                color = ProfileHairline,
                                thickness = 0.5.dp,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(vertical = 4.dp)
                            )
                            LibrarySummaryStat(
                                count = offlineUniqueCount,
                                label = "Offline",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("library") }
                            )
                        }
                    }
                }

                // ── 4. Listening Activity (Matte 2x2 Recap Grid) ─────────────────
                item(key = "listening_activity", contentType = "stats_grid") {
                    Spacer(modifier = Modifier.height(26.dp))
                    ProfileSectionHeader(
                        title = "Listening activity",
                        trailingText = if (playbackHistory.isNotEmpty()) "${playbackHistory.size} plays" else null
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProfileMetricTile(
                                label = "Time listened",
                                value = PlayerStyleMath.formatListeningTime(totalMinutes),
                                detail = if (totalMinutes > 0) "${playbackHistory.size} sessions" else "Start playing music",
                                modifier = Modifier.weight(1f)
                            )
                            ProfileMetricTile(
                                label = "Top vibe",
                                value = topGenre.ifBlank { "Discovery" },
                                detail = if (favoriteGenreList.isNotEmpty()) "Primary taste" else "Based on plays",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProfileMetricTile(
                                label = "Most replayed",
                                value = topTrack?.title ?: "None yet",
                                detail = if (topTrack != null) "${topTrack.playCount} plays · ${topTrack.artist}" else "Waiting for plays",
                                modifier = Modifier.weight(1f)
                            )
                            ProfileMetricTile(
                                label = "Unique tracks",
                                value = "${mostPlayed.size}",
                                detail = if (mostPlayed.isNotEmpty()) "Distinct songs heard" else "Explore catalog",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ── 5. Top Tracks (Clean Borderless Track List) ──────────────────
                if (mostPlayed.isNotEmpty()) {
                    item(key = "top_tracks_section", contentType = "track_list") {
                        Spacer(modifier = Modifier.height(26.dp))
                        ProfileSectionHeader(
                            title = "Top tracks",
                            actionLabel = "Play all",
                            onActionClick = { viewModel.playTracks(mostPlayed, startIndex = 0) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            mostPlayed.take(5).forEachIndexed { idx, track ->
                                EditorialTopTrackRow(
                                    rank = idx + 1,
                                    track = track,
                                    onClick = { viewModel.playTracks(mostPlayed, startIndex = idx) }
                                )
                            }
                        }
                    }
                }

                // ── 6. Recently Played Shelf ─────────────────────────────────────
                if (recentlyPlayed.isNotEmpty()) {
                    item(key = "recently_played_section", contentType = "recent_shelf") {
                        Spacer(modifier = Modifier.height(26.dp))
                        ProfileSectionHeader(title = "Recently played")
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(
                                items = recentlyPlayed.take(12),
                                key = { idx, track -> "prof_rec_${idx}_${track.id}" }
                            ) { idx, track ->
                                RecentTrackCard(
                                    track = track,
                                    onClick = { viewModel.playTracks(recentlyPlayed, startIndex = idx) }
                                )
                            }
                        }
                    }
                }

                // ── 7. Preferences & Playback Group (Borderless Matte Surface) ──
                item(key = "preferences_group", contentType = "settings_group") {
                    Spacer(modifier = Modifier.height(28.dp))
                    ProfileSectionHeader(title = "Playback & preferences")
                    Spacer(modifier = Modifier.height(10.dp))
                    MatteListGroup {
                        MattePreferenceRow(
                            icon = Icons.Rounded.Style,
                            title = "Now Playing style",
                            value = profileStyleShortName(playerStyle),
                            onClick = { showPlayerStyleSheet = true }
                        )
                        MatteGroupDivider()
                        MattePreferenceRow(
                            icon = Icons.Rounded.GraphicEq,
                            title = "Audio quality",
                            value = audioQualityShortLabel(audioQuality),
                            onClick = { navController.navigate("settings") }
                        )
                        MatteGroupDivider()
                        MattePreferenceRow(
                            icon = Icons.Rounded.Equalizer,
                            title = "Equalizer & effects",
                            value = "EQ & spatializer",
                            onClick = onEqualizerClick
                        )
                        MatteGroupDivider()
                        MattePreferenceRow(
                            icon = Icons.Rounded.Settings,
                            title = "All settings",
                            onClick = { navController.navigate("settings") }
                        )
                        MatteGroupDivider()
                        MattePreferenceRow(
                            icon = Icons.Rounded.NotificationsNone,
                            title = "Notifications",
                            onClick = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                runCatching { context.startActivity(intent) }
                            }
                        )
                    }
                }

                // ── 8. About & Account Actions (Quiet List Rows) ─────────────────
                item(key = "about_and_reset", contentType = "about_group") {
                    Spacer(modifier = Modifier.height(22.dp))
                    ProfileSectionHeader(title = "About")
                    Spacer(modifier = Modifier.height(10.dp))
                    MatteListGroup {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "EcoDot Music",
                                fontFamily = poppinsFamily,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Made by Debug01-dEoW aka Deep",
                                fontFamily = poppinsFamily,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.52f)
                            )
                        }
                        MatteGroupDivider()
                        MattePreferenceRow(
                            icon = Icons.Rounded.Person,
                            title = "GitHub profile",
                            value = "@Debug01-dEoW",
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(UserPreferencesManager.DEFAULT_GITHUB_PROFILE_URL)
                                        )
                                    )
                                }
                            }
                        )
                        MatteGroupDivider()
                        MattePreferenceRow(
                            icon = Icons.Rounded.Code,
                            title = "Source repository",
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(UserPreferencesManager.DEFAULT_GITHUB_REPO_URL)
                                        )
                                    )
                                }
                            }
                        )
                        MatteGroupDivider()
                        MattePreferenceRow(
                            icon = Icons.Rounded.NewReleases,
                            title = "Releases & updates",
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(UserPreferencesManager.DEFAULT_GITHUB_RELEASES_URL)
                                        )
                                    )
                                }
                            }
                        )
                        MatteGroupDivider()
                        MattePreferenceRow(
                            icon = Icons.Rounded.SupportAgent,
                            title = "WhatsApp support",
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/918799114016"))
                                    )
                                }
                            }
                        )
                        MatteGroupDivider()
                        MattePreferenceRow(
                            icon = Icons.Rounded.Email,
                            title = "Email developer",
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:deep082008.patel@gmail.com")
                                        }
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showResetProfileDialog = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Reset profile & preferences",
                            fontFamily = poppinsFamily,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE5534B)
                        )
                    }
                }
            }
        }

        if (showEditProfile) {
            EditProfileSheet(
                currentName = userProfile?.name ?: "EcoDot Listener",
                currentGenres = userProfile?.favoriteGenres ?: "",
                currentAvatarUri = userProfile?.avatarUri,
                currentIsPro = userProfile?.isPro ?: true,
                accentColor = resolvedAccent,
                onPickAvatar = { imagePicker.launch("image/*") },
                onSave = { name, genres, isPro ->
                    viewModel.updateProfile(
                        name = name,
                        genres = genres,
                        avatarUrl = userProfile?.avatarUri,
                        isPro = isPro
                    )
                    showEditProfile = false
                },
                onDismiss = { showEditProfile = false }
            )
        }

        if (showPlayerStyleSheet) {
            PlayerStyleBottomSheet(
                currentStyle = playerStyle,
                accentColor = resolvedAccent,
                onStyleSelected = { viewModel.setPlayerStyle(it) },
                onDismiss = { showPlayerStyleSheet = false }
            )
        }

        if (showResetProfileDialog) {
            AlertDialog(
                onDismissRequest = { showResetProfileDialog = false },
                title = {
                    Text(
                        text = "Reset profile & preferences?",
                        fontFamily = poppinsFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "This restores your default profile name, clears favorite genres & avatar, and resets audio/network preferences.",
                        fontFamily = poppinsFamily,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateProfile("EcoDot Listener", "", null, false)
                            viewModel.setOfflineMode(false)
                            viewModel.setDataSaver(false)
                            viewModel.setKillServiceOnExit(false)
                            viewModel.setAudioQuality(AudioQuality.HIGH)
                            showResetProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5534B))
                    ) {
                        Text("Reset", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetProfileDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                },
                containerColor = Color(0xFF1A1B22),
                titleContentColor = Color.White
            )
        }
    }
}

private fun audioQualityShortLabel(quality: AudioQuality): String = when (quality) {
    AudioQuality.LOW -> "Low · 48 kbps"
    AudioQuality.NORMAL -> "Normal · 128 kbps"
    AudioQuality.HIGH -> "High · 256 kbps"
    AudioQuality.HIGH_AAC -> "High AAC · 256 kbps"
}

private fun profileStyleDisplayName(style: PlayerStyle): String = when (style) {
    PlayerStyle.ECODOT -> "EcoDot Classic"
    PlayerStyle.APPLE_MUSIC -> "Apple Music"
    PlayerStyle.SPOTIFY -> "Spotify"
    PlayerStyle.VINYL -> "Vinyl Turntable"
}

private fun profileStyleShortName(style: PlayerStyle): String = when (style) {
    PlayerStyle.ECODOT -> "Classic"
    PlayerStyle.APPLE_MUSIC -> "Apple Music"
    PlayerStyle.SPOTIFY -> "Spotify"
    PlayerStyle.VINYL -> "Vinyl"
}

@Composable
private fun ProfileSectionHeader(
    title: String,
    trailingText: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontFamily = poppinsFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        when {
            actionLabel != null && onActionClick != null -> {
                Text(
                    text = actionLabel,
                    fontFamily = poppinsFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.64f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onActionClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            trailingText != null -> {
                Text(
                    text = trailingText,
                    fontFamily = poppinsFamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
private fun LibrarySummaryStat(
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            fontFamily = poppinsFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = poppinsFamily,
            fontSize = 11.5.sp,
            color = Color.White.copy(alpha = 0.54f),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileMetricTile(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ProfileSurface,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = label,
                fontFamily = poppinsFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.50f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontFamily = poppinsFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                fontFamily = poppinsFamily,
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.46f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EditorialTopTrackRow(
    rank: Int,
    track: Track,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val artUrl = remember(track.id, track.albumArtUri) {
        YouTubeParser.getHighResUrl(track.albumArtUri, isWide = false) ?: track.albumArtUri
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            fontFamily = poppinsFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.46f),
            modifier = Modifier.width(24.dp)
        )
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(artUrl)
                .size(160, 160)
                .crossfade(160)
                .build(),
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(ProfileTonalButton)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontFamily = poppinsFamily,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "${track.artist} · ${track.playCount} plays",
                fontFamily = poppinsFamily,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.54f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = "Play",
            tint = Color.White.copy(alpha = 0.34f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun RecentTrackCard(
    track: Track,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val artUrl = remember(track.id, track.albumArtUri) {
        YouTubeParser.getHighResUrl(track.albumArtUri, isWide = false) ?: track.albumArtUri
    }
    Column(
        modifier = Modifier
            .width(132.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(artUrl)
                .size(360, 360)
                .crossfade(160)
                .build(),
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(132.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ProfileSurface)
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = track.title,
            fontFamily = poppinsFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            fontFamily = poppinsFamily,
            fontSize = 11.5.sp,
            color = Color.White.copy(alpha = 0.54f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MatteListGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ProfileSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun MatteGroupDivider() {
    HorizontalDivider(
        color = ProfileHairline,
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 50.dp)
    )
}

@Composable
private fun MattePreferenceRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.72f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontFamily = poppinsFamily,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (!value.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                fontFamily = poppinsFamily,
                fontSize = 12.5.sp,
                color = Color.White.copy(alpha = 0.48f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.30f),
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit Profile Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    currentName: String,
    currentGenres: String,
    currentAvatarUri: String?,
    currentIsPro: Boolean,
    accentColor: Color,
    onPickAvatar: () -> Unit,
    onSave: (name: String, genres: String, isPro: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    val selectedGenres = remember {
        mutableStateListOf<String>().apply {
            addAll(PlayerStyleMath.parseFavoriteGenres(currentGenres))
        }
    }
    var customGenreInput by remember { mutableStateOf("") }
    var isPro by remember { mutableStateOf(currentIsPro) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF14151B),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Edit profile",
                fontFamily = poppinsFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(ProfileTonalButton)
                        .clickable(onClick = onPickAvatar),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentAvatarUri.isNullOrBlank()) {
                        AsyncImage(
                            model = currentAvatarUri,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.78f))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Profile photo",
                        fontFamily = poppinsFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Choose from gallery",
                        fontFamily = poppinsFamily,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.58f),
                        modifier = Modifier.clickable(onClick = onPickAvatar)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display name", fontFamily = poppinsFamily) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White.copy(alpha = 0.55f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White.copy(alpha = 0.78f),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.55f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Favorite genres",
                fontFamily = poppinsFamily,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            val allDisplayGenres = remember(selectedGenres.toList()) {
                (CURATED_PROFILE_GENRES + selectedGenres).distinctBy { it.lowercase() }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                allDisplayGenres.chunked(3).forEach { rowGenres ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowGenres.forEach { genre ->
                            val isSelected = selectedGenres.any { it.equals(genre, ignoreCase = true) }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedGenres.removeAll { it.equals(genre, ignoreCase = true) }
                                    } else {
                                        selectedGenres.add(genre)
                                    }
                                },
                                label = {
                                    Text(
                                        text = genre,
                                        fontFamily = poppinsFamily,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = Color.Black,
                                    containerColor = ProfileTonalButton,
                                    labelColor = Color.White.copy(alpha = 0.76f)
                                ),
                                border = null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowGenres.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customGenreInput,
                    onValueChange = { customGenreInput = it },
                    placeholder = { Text("Add custom genre...", fontSize = 12.5.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(alpha = 0.55f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        val trimmed = customGenreInput.trim()
                        if (trimmed.isNotEmpty() && selectedGenres.none { it.equals(trimmed, ignoreCase = true) }) {
                            selectedGenres.add(trimmed)
                            customGenreInput = ""
                        }
                    }
                ) {
                    Text("Add", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Verified listener badge",
                        fontFamily = poppinsFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Show verified checkmark beside your name",
                        fontFamily = poppinsFamily,
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.52f)
                    )
                }
                Switch(
                    checked = isPro,
                    onCheckedChange = { isPro = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    onSave(
                        name.trim().ifBlank { "EcoDot Listener" },
                        selectedGenres.joinToString(", "),
                        isPro
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(
                    text = "Save",
                    fontFamily = poppinsFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}
