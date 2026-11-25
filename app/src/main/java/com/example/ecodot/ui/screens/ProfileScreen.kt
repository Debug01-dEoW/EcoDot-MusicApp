package com.example.ecodot.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Style
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ecodot.ui.components.IOSSettingsGroup
import com.example.ecodot.ui.components.IOSSettingsRow
import com.example.ecodot.ui.components.player.PlayerStyleBottomSheet
import com.example.ecodot.ui.components.player.PlayerStyleMath
import com.example.ecodot.ui.theme.EcoDotCard
import com.example.ecodot.ui.theme.poppinsFamily
import com.example.ecodot.ui.viewmodel.MusicViewModel

private val CURATED_PROFILE_GENRES = listOf(
    "Pop", "Lo-Fi", "Hip-Hop", "Indie", "Electronic", "Rock",
    "R&B", "Jazz", "Bollywood", "Phonk", "Classical", "Ambient"
)

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
                name = userProfile?.name ?: "EcoDot User",
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
    val resolvedAccent = if (accentColor == Color.Black) Color(0xFF8C6EFF) else accentColor
    val topTrack = mostPlayed.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06070A))
    ) {
        // Subtle ambient aura anchored at top behind profile card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.42f),
                            resolvedAccent.copy(alpha = 0.16f),
                            Color(0xFF06070A)
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // ── 1. Top Header Bar ─────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Profile",
                            fontFamily = poppinsFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { showEditProfile = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // ── 2. Hero Profile Identity Card ─────────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(resolvedAccent, Color(0xFFFF6B8B))
                                        )
                                    )
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(EcoDotCard)
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                val avatarUri = userProfile?.avatarUri
                                if (!avatarUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(avatarUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(resolvedAccent)
                                    .border(2.dp, Color(0xFF0D0F14), CircleShape)
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CameraAlt,
                                    contentDescription = "Change avatar",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = userProfile?.name?.ifBlank { "EcoDot Listener" } ?: "EcoDot Listener",
                                fontFamily = poppinsFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (userProfile?.isPro == true) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = resolvedAccent.copy(alpha = 0.20f),
                                    border = BorderStroke(1.dp, resolvedAccent.copy(alpha = 0.55f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Verified,
                                            contentDescription = null,
                                            tint = resolvedAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "HI-FI PRO",
                                            fontFamily = poppinsFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${audioQualityShortLabel(audioQuality)} • ${profileStyleDisplayName(playerStyle)}",
                            fontFamily = poppinsFamily,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.62f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (favoriteGenreList.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(favoriteGenreList) { genre ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color.White.copy(alpha = 0.08f),
                                        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.14f))
                                    ) {
                                        Text(
                                            text = genre,
                                            fontFamily = poppinsFamily,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White.copy(alpha = 0.90f),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.06f),
                                border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.12f)),
                                modifier = Modifier.clickable { showEditProfile = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = resolvedAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Add your favorite music genres",
                                        fontFamily = poppinsFamily,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.78f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.10f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showEditProfile = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Edit Profile",
                                        fontFamily = poppinsFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = resolvedAccent.copy(alpha = 0.20f),
                                border = BorderStroke(1.dp, resolvedAccent.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showPlayerStyleSheet = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Style,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Player Style",
                                        fontFamily = poppinsFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. Library Footprint Strip ────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LibraryMetricTile(
                        icon = Icons.Rounded.Favorite,
                        count = likedTrackIds.size,
                        label = "Liked",
                        tint = Color(0xFFFF4D6D),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("library") }
                    )
                    LibraryMetricTile(
                        icon = Icons.Rounded.LibraryMusic,
                        count = allPlaylists.size,
                        label = "Playlists",
                        tint = Color(0xFF4CC9F0),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("library") }
                    )
                    LibraryMetricTile(
                        icon = Icons.Rounded.People,
                        count = followedArtists.size,
                        label = "Artists",
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("library") }
                    )
                    LibraryMetricTile(
                        icon = Icons.Rounded.DownloadDone,
                        count = offlineUniqueCount,
                        label = "Offline",
                        tint = Color(0xFF1ED760),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("library") }
                    )
                }
            }

            // ── 4. Listening Insights (Always visible 2x2 Grid) ───────────────
            item {
                Spacer(modifier = Modifier.height(22.dp))
                SectionTitleRow(title = "Listening Insights", subtitle = "Your playback telemetry")
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
                        StatCard(
                            label = "Time Listened",
                            value = PlayerStyleMath.formatListeningTime(totalMinutes),
                            subtitle = if (totalMinutes > 0) "${playbackHistory.size} sessions logged" else "Start listening today",
                            gradient = Brush.linearGradient(
                                listOf(Color(0xFF3E1F72), Color(0xFF171326))
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Top Taste",
                            value = topGenre.ifBlank { "Discovery" },
                            subtitle = if (favoriteGenreList.isNotEmpty()) "Primary vibe" else "Based on plays",
                            gradient = Brush.linearGradient(
                                listOf(Color(0xFF0E4B5A), Color(0xFF0E1B24))
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = "Top Track",
                            value = topTrack?.title ?: "None yet",
                            subtitle = if (topTrack != null) "${topTrack.playCount} plays · ${topTrack.artist}" else "Play tracks to unlock",
                            gradient = Brush.linearGradient(
                                listOf(Color(0xFF5D1F39), Color(0xFF1D1017))
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Unique Tracks",
                            value = "${mostPlayed.size}",
                            subtitle = if (mostPlayed.isNotEmpty()) "All-time distinct songs" else "Discover new music",
                            gradient = Brush.linearGradient(
                                listOf(Color(0xFF154F30), Color(0xFF0D1D14))
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── 5. Top Played Tracks List ─────────────────────────────────────
            if (mostPlayed.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionTitleRow(
                        title = "Your Top Tracks",
                        subtitle = "Most replayed songs"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mostPlayed.take(5).forEachIndexed { idx, track ->
                            TopTrackRow(
                                rank = idx + 1,
                                track = track,
                                accentColor = resolvedAccent,
                                onClick = { viewModel.playTracks(mostPlayed, startIndex = idx) }
                            )
                        }
                    }
                }
            }

            // ── 6. Recently Played Strip ──────────────────────────────────────
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionTitleRow(title = "Recently Played", subtitle = "Jump back in")
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(recentlyPlayed.take(12)) { idx, track ->
                            RecentTrackCard(
                                track = track,
                                onClick = { viewModel.playTracks(recentlyPlayed, startIndex = idx) }
                            )
                        }
                    }
                }
            }

            // ── 7. Preferences & Account Shortcuts ────────────────────────────
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSection("Player & Audio") {
                    SettingsRow(
                        icon = Icons.Rounded.Style,
                        title = "Now Playing Style",
                        subtitle = profileStyleDisplayName(playerStyle),
                        onClick = { showPlayerStyleSheet = true }
                    )
                    SettingsRow(
                        icon = Icons.Rounded.Equalizer,
                        title = "Equalizer & Effects",
                        subtitle = "5-band EQ, bass boost & spatializer",
                        onClick = onEqualizerClick
                    )
                    SettingsRow(
                        icon = Icons.Rounded.Settings,
                        title = "App Settings",
                        subtitle = "Audio quality, crossfade, theme & storage",
                        onClick = { navController.navigate("settings") }
                    )
                    SettingsRow(
                        icon = Icons.Rounded.Notifications,
                        title = "Notifications",
                        subtitle = "Manage notification settings",
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // ── 8. Developer Info (@Debug01-dEoW) ─────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = 0.045f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Developer Info",
                            fontFamily = poppinsFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Made by Debug01-dEoW aka Deep · Lead Engineer & Designer",
                            fontFamily = poppinsFamily,
                            fontSize = 12.5.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DevLinkChip(
                                label = "GitHub Profile",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(UserPreferencesManager.DEFAULT_GITHUB_PROFILE_URL)
                                        )
                                    )
                                }
                            )
                            DevLinkChip(
                                label = "Source Repo",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(UserPreferencesManager.DEFAULT_GITHUB_REPO_URL)
                                        )
                                    )
                                }
                            )
                            DevLinkChip(
                                label = "Releases",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(UserPreferencesManager.DEFAULT_GITHUB_RELEASES_URL)
                                        )
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DevLinkChip(
                                label = "WhatsApp Support",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/918799114016"))
                                    )
                                }
                            )
                            DevLinkChip(
                                label = "Email Developer",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:deep082008.patel@gmail.com")
                                    }
                                    runCatching { context.startActivity(intent) }
                                }
                            )
                        }
                    }
                }
            }

            // ── 9. Reset Profile & Preferences ────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFF453A).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFFF453A).copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { showResetProfileDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            tint = Color(0xFFFF6961),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset Profile & Preferences",
                            fontFamily = poppinsFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF6961)
                        )
                    }
                }
            }
        }

        if (showEditProfile) {
            EditProfileSheet(
                currentName = userProfile?.name ?: "EcoDot User",
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
                        text = "Reset Profile & Preferences?",
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
                            viewModel.updateProfile("EcoDot User", "", null, false)
                            viewModel.setOfflineMode(false)
                            viewModel.setDataSaver(false)
                            viewModel.setKillServiceOnExit(false)
                            viewModel.setAudioQuality(AudioQuality.HIGH)
                            showResetProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))
                    ) {
                        Text("Reset", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetProfileDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                },
                containerColor = Color(0xFF1C1C1E),
                titleContentColor = Color.White
            )
        }
    }
}

private fun audioQualityShortLabel(quality: AudioQuality): String = when (quality) {
    AudioQuality.LOW -> "Low · 48 kbps"
    AudioQuality.NORMAL -> "Normal · 128 kbps"
    AudioQuality.HIGH -> "High · 256 kbps Opus"
    AudioQuality.HIGH_AAC -> "High AAC · 256 kbps"
}

private fun profileStyleDisplayName(style: PlayerStyle): String = when (style) {
    PlayerStyle.ECODOT -> "EcoDot Classic"
    PlayerStyle.APPLE_MUSIC -> "Apple Music"
    PlayerStyle.SPOTIFY -> "Spotify"
    PlayerStyle.VINYL -> "Vinyl Turntable"
}

@Composable
private fun SectionTitleRow(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            fontFamily = poppinsFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = subtitle,
            fontFamily = poppinsFamily,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun LibraryMetricTile(
    icon: ImageVector,
    count: Int,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.055f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f)),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$count",
                fontFamily = poppinsFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontFamily = poppinsFamily,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.58f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TopTrackRow(
    rank: Int,
    track: Track,
    accentColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val artUrl = remember(track.id, track.albumArtUri) {
        YouTubeParser.getHighResUrl(track.albumArtUri, isWide = false) ?: track.albumArtUri
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.045f),
        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.07f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                fontFamily = poppinsFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (rank == 1) accentColor else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.width(24.dp)
            )
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontFamily = poppinsFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    fontFamily = poppinsFamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.58f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = accentColor.copy(alpha = 0.16f)
            ) {
                Text(
                    text = "${track.playCount}x",
                    fontFamily = poppinsFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }
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
            .width(124.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(124.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(EcoDotCard)
                .border(0.75.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = track.title,
            fontFamily = poppinsFamily,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            fontFamily = poppinsFamily,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.58f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DevLinkChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.12f)),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontFamily = poppinsFamily,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.88f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
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
        containerColor = Color(0xFF14151A),
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
                text = "Edit Profile",
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
                        .background(EcoDotCard)
                        .border(1.5.dp, accentColor, CircleShape)
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
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Profile Photo",
                        fontFamily = poppinsFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Tap photo to pick from gallery",
                        fontFamily = poppinsFamily,
                        fontSize = 12.sp,
                        color = accentColor,
                        modifier = Modifier.clickable(onClick = onPickAvatar)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name", fontFamily = poppinsFamily) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Favorite Genres",
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
                                    selectedContainerColor = accentColor.copy(alpha = 0.25f),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    labelColor = Color.White.copy(alpha = 0.72f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.White.copy(alpha = 0.14f),
                                    selectedBorderColor = accentColor
                                ),
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
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
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
                    Text("Add", color = accentColor, fontWeight = FontWeight.Bold)
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
                        text = "Hi-Fi Pro Badge",
                        fontFamily = poppinsFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Display verified listener badge on profile",
                        fontFamily = poppinsFamily,
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.55f)
                    )
                }
                Switch(
                    checked = isPro,
                    onCheckedChange = { isPro = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = accentColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    onSave(
                        name.trim().ifBlank { "EcoDot User" },
                        selectedGenres.joinToString(", "),
                        isPro
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Save Profile",
                    fontFamily = poppinsFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    IOSSettingsGroup(title = title, content = content)
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    val tint = when {
        title.contains("Settings", ignoreCase = true) -> Color(0xFF8E8E93)
        title.contains("Equalizer", ignoreCase = true) -> Color(0xFF5E5CE6)
        title.contains("Style", ignoreCase = true) -> Color(0xFFFF375F)
        else -> Color(0xFF30D158)
    }
    IOSSettingsRow(
        icon = icon,
        iconBackgroundColor = tint,
        title = title,
        subtitle = subtitle,
        onClick = onClick
    )
}

@Composable
fun StatCard(
    label: String,
    value: String,
    subtitle: String,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = label.uppercase(),
                    fontFamily = poppinsFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    fontFamily = poppinsFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = poppinsFamily,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
