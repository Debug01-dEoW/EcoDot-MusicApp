package com.example.ecodot.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ecodot.data.local.prefs.AudioQuality
import com.example.ecodot.data.local.prefs.VideoQuality
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.ui.theme.*
import com.example.ecodot.ui.components.*

@android.annotation.SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    onEqualizerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile     by viewModel.userProfile.collectAsState()
    val audioQuality    by viewModel.audioQuality.collectAsState()
    val videoQuality    by viewModel.videoQuality.collectAsState()
    val offlineMode     by viewModel.offlineMode.collectAsState()
    val dataSaver       by viewModel.dataSaver.collectAsState()
    val killServiceOnExit by viewModel.killServiceOnExit.collectAsState()
    val crossfadeEnabled by viewModel.crossfadeEnabled.collectAsState()
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsState()
    val cacheSize       by viewModel.cacheSize.collectAsState()
    val mostPlayed      by viewModel.mostPlayed.collectAsState()
    val totalMinutes    by viewModel.totalMinutesListened.collectAsState()
    val topGenre        by viewModel.topGenre.collectAsState()

    val context = LocalContext.current

    // Dialog / sheet states
    var showEditProfile         by remember { mutableStateOf(false) }
    var showClearCacheDialog    by remember { mutableStateOf(false) }
    var showResetProfileDialog  by remember { mutableStateOf(false) }
    var showAudioQualityPicker  by remember { mutableStateOf(false) }
    var showVideoQualityPicker  by remember { mutableStateOf(false) }

    // Refresh cache size every time screen enters
    LaunchedEffect(Unit) {
        viewModel.updateCacheSize()
    }

    // Image picker for avatar
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.updateProfile(
                name      = userProfile?.name ?: "EcoDot User",
                genres    = userProfile?.favoriteGenres ?: "",
                avatarUrl = uri.toString(),
                isPro     = userProfile?.isPro ?: false
            )
        }
    }

    LiquidMeshBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { _ ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = 140.dp
                )
            ) {

                // ── Top bar ─────────────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White)
                        }
                        Text(
                            "Account & Settings",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(
                            onClick = { showEditProfile = true },
                            modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Rounded.Edit, "Edit profile", tint = Color.White)
                        }
                    }
                }

                // ── Avatar + Name ─────────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(112.dp)
                                .clip(CircleShape)
                                .background(EcoDotCard)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            if (userProfile?.avatarUri.isNullOrEmpty()) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp).align(Alignment.Center)
                                )
                            } else {
                                AsyncImage(
                                    model = userProfile?.avatarUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // Camera badge overlay
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(EcoDotRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            userProfile?.name ?: "EcoDot User",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tap the avatar to change photo",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 12.sp
                        )
                    }
                }

                // ── Listening Stats ──────────────────────────────────────
                if (totalMinutes > 0 || mostPlayed.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                            Text(
                                "Your Stats",
                                color = Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Minutes listened
                                StatCard(
                                    label = "Minutes Listened",
                                    value = if (totalMinutes >= 60)
                                        "${totalMinutes / 60}h ${totalMinutes % 60}m"
                                    else
                                        "${totalMinutes}m",
                                    icon = Icons.Rounded.Timer,
                                    gradient = Brush.linearGradient(
                                        listOf(Color(0xFFE13300).copy(0.5f), Color(0xFF800000).copy(0.2f))
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                // Top track
                                val topTrack = mostPlayed.firstOrNull()
                                StatCard(
                                    label = "Most Played",
                                    value = topTrack?.title?.take(14) ?: "—",
                                    icon = Icons.Rounded.Whatshot,
                                    gradient = Brush.linearGradient(
                                        listOf(Color(0xFFBA5D07).copy(0.5f), Color(0xFF5C2A00).copy(0.2f))
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Top genre / artist
                                StatCard(
                                    label = "Top Genre / Artist",
                                    value = topGenre.take(14),
                                    icon = Icons.Rounded.MusicNote,
                                    gradient = Brush.linearGradient(
                                        listOf(Color(0xFF477D95).copy(0.5f), Color(0xFF1A3A4A).copy(0.2f))
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                // Songs discovered
                                StatCard(
                                    label = "Songs Played",
                                    value = "${mostPlayed.size}",
                                    icon = Icons.Rounded.LibraryMusic,
                                    gradient = Brush.linearGradient(
                                        listOf(Color(0xFF608108).copy(0.5f), Color(0xFF2A3800).copy(0.2f))
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // ── General ─────────────────────────────────────────────
                item {
                    val hazeState = com.example.ecodot.LocalHazeState.current
                    IOSSettingsGroup(title = "General", hazeState = hazeState) {
                        IOSSettingsRow(
                            icon = Icons.Rounded.Person,
                            iconBackgroundColor = Color(0xFF007AFF), // iOS Blue
                            title = "Edit Profile",
                            subtitle = "Change name & avatar",
                            onClick = { showEditProfile = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        IOSSettingsRow(
                            icon = Icons.Rounded.Notifications,
                            iconBackgroundColor = Color(0xFFFF9500), // iOS Orange
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

                // ── Playback ─────────────────────────────────────────────
                item {
                    val hazeState = com.example.ecodot.LocalHazeState.current
                    IOSSettingsGroup(title = "Playback", hazeState = hazeState) {
                        IOSSettingsRow(
                            icon = Icons.Rounded.HighQuality,
                            iconBackgroundColor = Color(0xFF5856D6), // iOS Purple
                            title = "Audio Quality",
                            subtitle = when (audioQuality) {
                                AudioQuality.LOW    -> "Low · ~48 kbps, saves data"
                                AudioQuality.NORMAL -> "Normal · ~128 kbps, balanced"
                                AudioQuality.HIGH   -> "High · Best quality (up to 256 kbps Opus)"
                            },
                            valueLabel = audioQuality.name,
                            onClick = { showAudioQualityPicker = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        IOSSettingsRow(
                            icon = Icons.Rounded.Hd,
                            iconBackgroundColor = Color(0xFF32ADE6), // iOS Teal Blue
                            title = "Video Quality",
                            subtitle = when (videoQuality) {
                                VideoQuality.LOW    -> "Low · 360p, saves data"
                                VideoQuality.NORMAL -> "Normal · 720p, balanced"
                                VideoQuality.HIGH   -> "High · 1080p+ (if available)"
                            },
                            valueLabel = videoQuality.name,
                            onClick = { showVideoQualityPicker = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        IOSSettingsRow(
                            icon = Icons.Rounded.GraphicEq,
                            iconBackgroundColor = Color(0xFFFF2D55), // iOS Pink
                            title = "Equalizer",
                            subtitle = "Tune bass, treble & more",
                            onClick = onEqualizerClick
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        IOSSettingsRow(
                            icon = Icons.Rounded.SwapHoriz,
                            iconBackgroundColor = Color(0xFF007AFF),
                            title = "Auto Crossfade",
                            subtitle = if (crossfadeEnabled) "Overlap tracks for ${crossfadeDuration}s" else "Tracks play without overlap",
                            isToggle = true,
                            isChecked = crossfadeEnabled,
                            onClick = { viewModel.setCrossfadeEnabled(!crossfadeEnabled) }
                        )
                        if (crossfadeEnabled) {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Duration", color = Color.White, fontSize = 14.sp)
                                    Text("${crossfadeDuration}s", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                                }
                                androidx.compose.material3.Slider(
                                    value = crossfadeDuration.toFloat(),
                                    onValueChange = { viewModel.setCrossfadeDuration(it.toInt()) },
                                    valueRange = 1f..10f,
                                    steps = 8,
                                    colors = androidx.compose.material3.SliderDefaults.colors(
                                        thumbColor = Color(0xFF34C759),
                                        activeTrackColor = Color(0xFF34C759)
                                    )
                                )
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        IOSSettingsRow(
                            icon = Icons.Rounded.OfflineBolt,
                            iconBackgroundColor = Color(0xFF34C759), // iOS Green
                            title = "Offline Mode",
                            subtitle = if (offlineMode) "Only plays downloaded tracks" else "Streams when data is available",
                            isToggle = true,
                            isChecked = offlineMode,
                            onClick = { viewModel.setOfflineMode(!offlineMode) }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        val autoCanvas by viewModel.autoCanvasEnabled.collectAsState()
                        IOSSettingsRow(
                            icon = Icons.Rounded.Movie,
                            iconBackgroundColor = Color(0xFFAF52DE), // iOS Indigo
                            title = "Auto Looping Canvas",
                            subtitle = if (autoCanvas) "Automatically fetch Spotify Canvas for songs" else "Off",
                            isToggle = true,
                            isChecked = autoCanvas,
                            onClick = { viewModel.setAutoCanvasEnabled(!autoCanvas) }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        val lockscreenLyrics by viewModel.lockscreenLyricsEnabled.collectAsState()
                        IOSSettingsRow(
                            icon = Icons.Rounded.MenuBook,
                            iconBackgroundColor = Color(0xFFFF9500),
                            title = "Lock Screen Lyrics",
                            subtitle = if (lockscreenLyrics) "Show real-time lyrics overlay on lock screen" else "Off",
                            isToggle = true,
                            isChecked = lockscreenLyrics,
                            onClick = { viewModel.setLockscreenLyricsEnabled(!lockscreenLyrics) }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        IOSSettingsRow(
                            icon = Icons.Rounded.CloseFullscreen,
                            iconBackgroundColor = Color(0xFFFF3B30), // iOS Red
                            title = "Stop playback on exit",
                            subtitle = if (killServiceOnExit) "Music stops when app is swiped away" else "Keeps playing in background",
                            isToggle = true,
                            isChecked = killServiceOnExit,
                            onClick = { viewModel.setKillServiceOnExit(!killServiceOnExit) }
                        )
                    }
                }

                // ── Storage & Data ────────────────────────────────────────
                item {
                    val hazeState = com.example.ecodot.LocalHazeState.current
                    IOSSettingsGroup(title = "Storage & Data", hazeState = hazeState) {
                        val cacheMb = cacheSize / (1024 * 1024)
                        IOSSettingsRow(
                            icon = Icons.Rounded.Storage,
                            iconBackgroundColor = Color(0xFF8E8E93), // iOS Gray
                            title = "Stream Cache",
                            subtitle = "Audio cached for faster replay",
                            valueLabel = if (cacheMb > 0) "${cacheMb} MB" else "Empty",
                            onClick = { if (cacheMb > 0) showClearCacheDialog = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        IOSSettingsRow(
                            icon = Icons.Rounded.DataUsage,
                            iconBackgroundColor = Color(0xFF007AFF),
                            title = "Data Saver",
                            subtitle = if (dataSaver) "Streaming at lower quality" else "Full quality streaming",
                            isToggle = true,
                            isChecked = dataSaver,
                            onClick = { viewModel.setDataSaver(!dataSaver) }
                        )
                    }
                }

                // ── About ──────────────────────────────────────────────────
                item {
                    val hazeState = com.example.ecodot.LocalHazeState.current
                    IOSSettingsGroup(title = "About", hazeState = hazeState) {
                        IOSSettingsRow(
                            icon = Icons.Rounded.Info,
                            iconBackgroundColor = Color(0xFF007AFF),
                            title = "Version",
                            valueLabel = "1.0.0-beta",
                            onClick = {}
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        IOSSettingsRow(
                            icon = Icons.Rounded.Policy,
                            iconBackgroundColor = Color(0xFF34C759),
                            title = "Privacy Policy",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"))
                                context.startActivity(intent)
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        IOSSettingsRow(
                            icon = Icons.Rounded.Email,
                            iconBackgroundColor = Color(0xFFFF9500),
                            title = "Email Feedback",
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:deep082008.patel@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "EcoDot Beta Feedback")
                                }
                                context.startActivity(intent)
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        IOSSettingsRow(
                            icon = Icons.Rounded.Chat,
                            iconBackgroundColor = Color(0xFF25D366),
                            title = "Contact on WhatsApp",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/918799114016"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                // ── Developer Info ────────────────────────────────────────
                item {
                    var devInfoExpanded by remember { mutableStateOf(false) }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212))
                                )
                            )
                            .clickable { devInfoExpanded = !devInfoExpanded }
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val devAccentColor = Color(0xFF1DB954)
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(devAccentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Code, contentDescription = null, tint = devAccentColor, modifier = Modifier.size(26.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Developer Info",
                                        color = devAccentColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Patel Deepen",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Lead Engineer & Designer",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 13.sp
                                    )
                                }
                                Icon(
                                    imageVector = if (devInfoExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Expand",
                                    tint = Color.White.copy(alpha = 0.3f)
                                )
                            }
                            
                            AnimatedVisibility(visible = devInfoExpanded) {
                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Passionate Android developer and designer specializing in creating highly interactive and premium applications. EcoDot is a testament to blending stunning visuals with powerful audio processing.",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = { 
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/918799114016"))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Rounded.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("WhatsApp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                        Button(
                                            onClick = { 
                                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                    data = Uri.parse("mailto:deep082008.patel@gmail.com")
                                                }
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335)),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Email", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Danger Zone ───────────────────────────────────────────
                item {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFF3B30).copy(alpha = 0.08f))
                            .clickable { showResetProfileDialog = true }
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                Icons.Rounded.DeleteForever,
                                null,
                                tint = Color(0xFFFF453A),
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    "Reset Profile",
                                    color = Color(0xFFFF453A),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Clears your name, avatar & preferences",
                                    color = Color(0xFFFF453A).copy(alpha = 0.55f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }

        // ── Edit Profile Bottom Sheet ──────────────────────────────────────
        if (showEditProfile) {
            EditProfileSheet(
                currentName   = userProfile?.name ?: "EcoDot User",
                currentAvatar = userProfile?.avatarUri,
                onDismiss     = { showEditProfile = false },
                onSave        = { name, avatarUrl ->
                    viewModel.updateProfile(
                        name      = name,
                        genres    = userProfile?.favoriteGenres ?: "",
                        avatarUrl = avatarUrl,
                        isPro     = userProfile?.isPro ?: false
                    )
                    showEditProfile = false
                }
            )
        }

        // ── Audio Quality Picker ────────────────────────────────────────────
        if (showAudioQualityPicker) {
            AudioQualityPickerDialog(
                current   = audioQuality,
                onDismiss = { showAudioQualityPicker = false },
                onSelect  = { q ->
                    viewModel.setAudioQuality(q)
                    showAudioQualityPicker = false
                }
            )
        }

        // ── Video Quality Picker ────────────────────────────────────────────
        if (showVideoQualityPicker) {
            VideoQualityPickerDialog(
                current   = videoQuality,
                onDismiss = { showVideoQualityPicker = false },
                onSelect  = { q ->
                    viewModel.setVideoQuality(q)
                    showVideoQualityPicker = false
                }
            )
        }

        // ── Clear Cache Dialog ──────────────────────────────────────────────
        if (showClearCacheDialog) {
            AlertDialog(
                onDismissRequest = { showClearCacheDialog = false },
                icon    = { Icon(Icons.Rounded.DeleteSweep, null, tint = EcoDotRed) },
                title   = { Text("Clear Stream Cache", color = Color.White, fontWeight = FontWeight.Bold) },
                text    = {
                    Text(
                        "Remove ${cacheSize / (1024 * 1024)} MB of cached audio? Songs will re-buffer on next play.",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearCache()
                        showClearCacheDialog = false
                        android.widget.Toast.makeText(context, "Cache cleared", android.widget.Toast.LENGTH_SHORT).show()
                    }) { Text("Clear", color = EcoDotRed, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showClearCacheDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                },
                containerColor      = Color(0xFF1C1C1E),
                titleContentColor   = Color.White,
                textContentColor    = Color.White.copy(alpha = 0.7f)
            )
        }

        // ── Reset Profile Confirmation ──────────────────────────────────────
        if (showResetProfileDialog) {
            AlertDialog(
                onDismissRequest = { showResetProfileDialog = false },
                icon    = { Icon(Icons.Rounded.DeleteForever, null, tint = Color(0xFFFF453A)) },
                title   = { Text("Reset Profile?", color = Color.White, fontWeight = FontWeight.Bold) },
                text    = {
                    Text(
                        "This will reset your name, avatar, and listening preferences to defaults. Your library, playlists, and liked songs will NOT be affected.",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateProfile("EcoDot User", "", null, false)
                        viewModel.setOfflineMode(false)
                        viewModel.setDataSaver(false)
                        viewModel.setKillServiceOnExit(false)
                        viewModel.setAudioQuality(AudioQuality.HIGH)
                        showResetProfileDialog = false
                        android.widget.Toast.makeText(context, "Profile reset", android.widget.Toast.LENGTH_SHORT).show()
                    }) { Text("Reset", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showResetProfileDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                },
                containerColor      = Color(0xFF1C1C1E),
                titleContentColor   = Color.White,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit Profile Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    currentName: String,
    currentAvatar: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, avatarUrl: String?) -> Unit
) {
    var name      by remember { mutableStateOf(currentName) }
    var avatarUrl by remember { mutableStateOf(currentAvatar ?: "") }
    val nameError = name.isBlank()

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) avatarUrl = uri.toString()
    }

    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        containerColor    = Color(0xFF1C1C1E),
        dragHandle        = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Edit Profile",
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp,
                modifier   = Modifier.padding(bottom = 24.dp)
            )

            // Avatar picker
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(EcoDotCard)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { imagePicker.launch("image/*") },
                contentAlignment = Alignment.BottomEnd
            ) {
                if (avatarUrl.isBlank()) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile Placeholder",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp).align(Alignment.Center)
                    )
                } else {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier      = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale  = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(EcoDotRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CameraAlt, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Tap to change photo", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
            Spacer(Modifier.height(24.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name", color = Color.White.copy(alpha = 0.5f)) },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Name cannot be empty", color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = EcoDotRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor     = Color.White,
                    unfocusedTextColor   = Color.White,
                    cursorColor          = EcoDotRed
                ),
                leadingIcon = {
                    Icon(Icons.Rounded.Person, null, tint = Color.White.copy(alpha = 0.5f))
                }
            )

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
                ) { Text("Cancel") }

                Button(
                    onClick = { if (!nameError) onSave(name.trim(), avatarUrl.ifBlank { null }) },
                    modifier = Modifier.weight(1f),
                    enabled  = !nameError,
                    colors   = ButtonDefaults.buttonColors(containerColor = EcoDotRed)
                ) { Text("Save", color = Color.White, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Audio Quality Picker Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AudioQualityPickerDialog(
    current: AudioQuality,
    onDismiss: () -> Unit,
    onSelect: (AudioQuality) -> Unit
) {
    val options = listOf(
        Triple(AudioQuality.HIGH,   "High",   "Best quality · up to 256 kbps Opus · recommended"),
        Triple(AudioQuality.NORMAL, "Normal", "Balanced · ~128 kbps · good for most connections"),
        Triple(AudioQuality.LOW,    "Low",    "Data saver · ~48 kbps · for slow/limited data")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon    = { Icon(Icons.Rounded.HighQuality, null, tint = EcoDotRed) },
        title   = { Text("Audio Quality", color = Color.White, fontWeight = FontWeight.Bold) },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Higher quality uses more data and device storage cache.",
                    color    = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                options.forEach { (quality, label, desc) ->
                    val selected = quality == current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) EcoDotRed.copy(alpha = 0.12f)
                                else Color.White.copy(alpha = 0.04f)
                            )
                            .clickable { onSelect(quality) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick  = { onSelect(quality) },
                            colors   = RadioButtonDefaults.colors(
                                selectedColor   = EcoDotRed,
                                unselectedColor = Color.White.copy(alpha = 0.4f)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                label,
                                color      = if (selected) Color.White else Color.White.copy(alpha = 0.85f),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize   = 15.sp
                            )
                            Text(
                                desc,
                                color    = Color.White.copy(alpha = 0.45f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = EcoDotRed, fontWeight = FontWeight.Bold)
            }
        },
        containerColor    = Color(0xFF1C1C1E),
        titleContentColor = Color.White,
        textContentColor  = Color.White.copy(alpha = 0.7f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Video Quality Picker Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VideoQualityPickerDialog(
    current: VideoQuality,
    onDismiss: () -> Unit,
    onSelect: (VideoQuality) -> Unit
) {
    val options = listOf(
        Triple(VideoQuality.HIGH,   "High",   "Best quality · 1080p+ · high data usage"),
        Triple(VideoQuality.NORMAL, "Normal", "Balanced · 720p · good for most connections"),
        Triple(VideoQuality.LOW,    "Low",    "Data saver · 360p · for slow/limited data")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon    = { Icon(Icons.Rounded.Hd, null, tint = EcoDotRed) },
        title   = { Text("Video Quality", color = Color.White, fontWeight = FontWeight.Bold) },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Higher video quality requires more bandwidth and data.",
                    color    = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                options.forEach { (quality, label, desc) ->
                    val selected = quality == current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) EcoDotRed.copy(alpha = 0.12f)
                                else Color.White.copy(alpha = 0.04f)
                            )
                            .clickable { onSelect(quality) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick  = { onSelect(quality) },
                            colors   = RadioButtonDefaults.colors(
                                selectedColor   = EcoDotRed,
                                unselectedColor = Color.White.copy(alpha = 0.4f)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                label,
                                color      = if (selected) Color.White else Color.White.copy(alpha = 0.85f),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize   = 15.sp
                            )
                            Text(
                                desc,
                                color    = Color.White.copy(alpha = 0.45f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = EcoDotRed, fontWeight = FontWeight.Bold)
            }
        },
        containerColor    = Color(0xFF1C1C1E),
        titleContentColor = Color.White,
        textContentColor  = Color.White.copy(alpha = 0.7f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Settings Section Container
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp)) {
        Text(
            text       = title.uppercase(),
            style      = MaterialTheme.typography.labelSmall,
            color      = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier   = Modifier.padding(bottom = 10.dp, start = 4.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Settings Row — supports toggle, value label, subtitle
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    valueLabel: String? = null,
    isToggle: Boolean = false,
    isChecked: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animatedClickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = if (subtitle != null) 14.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.bodyLarge,
                color      = Color.White,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.45f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (valueLabel != null && !isToggle) {
            Text(
                text  = valueLabel,
                style = MaterialTheme.typography.bodySmall,
                color = EcoDotRed.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(6.dp))
        }
        if (isToggle) {
            Switch(
                checked           = isChecked,
                onCheckedChange   = { onClick() },
                colors            = SwitchDefaults.colors(
                    checkedThumbColor   = Color.White,
                    checkedTrackColor   = EcoDotRed,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.15f),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        } else {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stat Card used in the Stats section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(14.dp)
    ) {
        Column {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.45f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.weight(1f))
            Text(
                text       = value,
                color      = Color.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                text  = label,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
