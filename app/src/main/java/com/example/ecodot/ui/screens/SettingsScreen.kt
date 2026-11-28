package com.example.ecodot.ui.screens
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.ArrowBackIos

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.data.local.prefs.AudioQuality
import com.example.ecodot.data.local.prefs.PlayerStyle
import com.example.ecodot.data.local.prefs.SponsorBlockCategory
import com.example.ecodot.data.local.prefs.ThemeColorSource
import com.example.ecodot.data.local.prefs.ThemeMode
import com.example.ecodot.data.local.prefs.VideoQuality
import com.example.ecodot.ui.components.AmbientThemeGlow
import com.example.ecodot.ui.components.LocalLiquidGlassEnabled
import com.example.ecodot.ui.components.SettingGroup
import com.example.ecodot.ui.components.SettingItem
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.example.ecodot.util.UpdateStatus
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class RadioOption<T>(val value: T, val label: String)

private sealed interface SettingsModal {
    data class SingleChoice(
        val title: String,
        val options: List<RadioOption<Any>>,
        val selectedValue: Any,
        val onConfirm: (Any) -> Unit
    ) : SettingsModal

    data class MultiChoice(
        val title: String,
        val options: List<RadioOption<String>>,
        val selectedValues: Set<String>,
        val onConfirm: (Set<String>) -> Unit
    ) : SettingsModal

    data class Confirm(
        val title: String,
        val message: String,
        val confirmLabel: String,
        val onConfirm: () -> Unit
    ) : SettingsModal
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    onEqualizerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val liquidGlassEnabled = LocalLiquidGlassEnabled.current

    // Settings state flows
    val themeMode by viewModel.themeMode.collectAsState()
    val themeColorSource by viewModel.themeColorSource.collectAsState()
    val customThemeColorHex by viewModel.customThemeColorHex.collectAsState()
    val playerStyle by viewModel.playerStyle.collectAsState()
    val isLiquidGlassOn by viewModel.liquidGlassEnabled.collectAsState()

    val normalizeVolume by viewModel.normalizeVolume.collectAsState()
    val skipSilent by viewModel.skipSilent.collectAsState()
    val eqEnabled by viewModel.eqEnabled.collectAsState()
    val selectedEqPreset by viewModel.selectedEqPreset.collectAsState()

    val audioQuality by viewModel.audioQuality.collectAsState()
    val videoQuality by viewModel.videoQuality.collectAsState()
    val offlineMode by viewModel.offlineMode.collectAsState()
    val crossfadeEnabled by viewModel.crossfadeEnabled.collectAsState()
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsState()
    val crossfadeDjMode by viewModel.crossfadeDjMode.collectAsState()
    val crossfadeSkipAlbum by viewModel.crossfadeSkipAlbum.collectAsState()
    val autoCanvasEnabled by viewModel.autoCanvasEnabled.collectAsState()
    val lockscreenLyricsEnabled by viewModel.lockscreenLyricsEnabled.collectAsState()
    val killServiceOnExit by viewModel.killServiceOnExit.collectAsState()

    val sponsorBlockEnabled by viewModel.sponsorBlockEnabled.collectAsState()
    val sponsorBlockCategories by viewModel.sponsorBlockCategories.collectAsState()

    val dataSaver by viewModel.dataSaver.collectAsState()
    val cacheLimitMb by viewModel.cacheLimitMb.collectAsState()
    val storageStats by viewModel.storageStats.collectAsState()

    val autoCheckUpdates by viewModel.autoCheckUpdates.collectAsState()
    val lastCheckTime by viewModel.lastUpdateCheckTime.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val currentVersion = viewModel.appUpdateManager.currentVersionName
    var lastManualCheckTime by remember { mutableLongStateOf(0L) }

    val displayVersion = if (currentVersion.contains('v', ignoreCase = true)) currentVersion else "v$currentVersion"

    LaunchedEffect(Unit) {
        viewModel.updateCacheSize()
    }

    LaunchedEffect(updateStatus) {
        if (updateStatus is UpdateStatus.UpToDate && lastManualCheckTime > 0L) {
            Toast.makeText(context, "EcoDot is up to date ($displayVersion)", Toast.LENGTH_SHORT).show()
            lastManualCheckTime = 0L
        }
    }

    var activeModal by remember { mutableStateOf<SettingsModal?>(null) }
    var showColorPickerDialog by rememberSaveable { mutableStateOf(false) }

    val settingListState = rememberLazyListState()
    val localHazeState = remember { HazeState() }

    val showPinnedBar by remember {
        derivedStateOf {
            settingListState.firstVisibleItemIndex > 0 ||
                settingListState.firstVisibleItemScrollOffset > 16
        }
    }

    val listSidePadding = 16.dp
    val pageBackground = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pageBackground)
    ) {
        // Scrolls smoothly with item 0 just like SimpMusic's Settings ambient glow
        AmbientThemeGlow(
            tint = MaterialTheme.colorScheme.primary,
            pageBackground = pageBackground,
            modifier = Modifier.graphicsLayer {
                translationY = if (settingListState.firstVisibleItemIndex == 0) {
                    -settingListState.firstVisibleItemScrollOffset.toFloat()
                } else {
                    -size.height
                }
            }
        )

        LazyColumn(
            state = settingListState,
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 40.dp
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = listSidePadding)
                .hazeSource(localHazeState)
        ) {
            // ── TopBar + User Interface (folded into item 0 for seamless glow tracking) ──
            item(key = "user_interface") {
                Column {
                    SettingTopBar(
                        onBack = onBack,
                        windowInsets = WindowInsets(0),
                        modifier = Modifier
                            .layout { measurable, constraints ->
                                val bleed = listSidePadding.roundToPx()
                                val width = constraints.maxWidth + 2 * bleed
                                val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                                layout(constraints.maxWidth, placeable.height) {
                                    placeable.place(-bleed, 0)
                                }
                            }
                            .graphicsLayer {
                                alpha = if (settingListState.firstVisibleItemIndex == 0) {
                                    (1f - settingListState.firstVisibleItemScrollOffset / 48f).coerceIn(0f, 1f)
                                } else {
                                    0f
                                }
                            }
                    )

                    SettingGroup(
                        title = "User Interface"
                    ) {
                        SettingItem(
                            title = "Theme",
                            subtitle = themeMode.label,
                            onClick = {
                                activeModal = SettingsModal.SingleChoice(
                                    title = "Theme",
                                    options = ThemeMode.entries.map { RadioOption(it, it.label) },
                                    selectedValue = themeMode,
                                    onConfirm = { chosen ->
                                        viewModel.setThemeMode(chosen as ThemeMode)
                                    }
                                )
                            }
                        )

                        SettingItem(
                            title = "Now Playing style",
                            subtitle = playerStyleLabel(playerStyle),
                            onClick = {
                                activeModal = SettingsModal.SingleChoice(
                                    title = "Now Playing style",
                                    options = PlayerStyle.entries.map { RadioOption(it, playerStyleLabel(it)) },
                                    selectedValue = playerStyle,
                                    onConfirm = { chosen ->
                                        viewModel.setPlayerStyle(chosen as PlayerStyle)
                                    }
                                )
                            }
                        )

                        val availableSources = buildList {
                            add(ThemeColorSource.ALBUM_ART)
                            add(ThemeColorSource.SIMP_DEFAULT)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                add(ThemeColorSource.WALLPAPER)
                            }
                            add(ThemeColorSource.CUSTOM)
                        }
                        SettingItem(
                            title = "Theme color",
                            subtitle = themeColorSource.label,
                            onClick = {
                                activeModal = SettingsModal.SingleChoice(
                                    title = "Theme color",
                                    options = availableSources.map { RadioOption(it, it.label) },
                                    selectedValue = themeColorSource,
                                    onConfirm = { chosen ->
                                        val source = chosen as ThemeColorSource
                                        viewModel.setThemeColorSource(source)
                                        if (source == ThemeColorSource.CUSTOM) {
                                            showColorPickerDialog = true
                                        }
                                    }
                                )
                            }
                        )

                        if (themeColorSource == ThemeColorSource.CUSTOM) {
                            SettingItem(
                                title = "Custom color",
                                subtitle = "#${customThemeColorHex.takeLast(6)}",
                                smallSubtitle = true,
                                onClick = { showColorPickerDialog = true }
                            )
                        }

                        SettingItem(
                            title = "Enable liquid glass effect",
                            subtitle = "Translucent refraction and blur on cards and navigation",
                            smallSubtitle = true,
                            switch = (isLiquidGlassOn to { viewModel.setLiquidGlassEnabled(it) })
                        )
                    }
                }
            }

            // ── Audio ─────────────────────────────────────────────────────────────
            item(key = "audio") {
                SettingGroup(title = "Audio") {
                    SettingItem(
                        title = "Normalize volume",
                        subtitle = "Balance media loudness for a more consistent listening level",
                        switch = (normalizeVolume to { viewModel.setNormalizeVolume(it) })
                    )
                    SettingItem(
                        title = "Skip silent",
                        subtitle = "Automatically skip silent parts of tracks",
                        switch = (skipSilent to { viewModel.setSkipSilent(it) })
                    )
                    SettingItem(
                        title = "Equalizer & effects",
                        subtitle = if (eqEnabled) "Active · Preset: $selectedEqPreset" else "Off · Tap to tune 5-band EQ & virtualizer",
                        onClick = onEqualizerClick
                    )
                    SettingItem(
                        title = "Open system equalizer",
                        subtitle = "Use your device's built-in audio equalizer panel",
                        onClick = {
                            try {
                                val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                                    putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No system equalizer found on this device", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // ── Playback ──────────────────────────────────────────────────────────
            item(key = "playback") {
                SettingGroup(title = "Playback") {
                    SettingItem(
                        title = "Audio quality",
                        subtitle = audioQualityLabel(audioQuality),
                        onClick = {
                            activeModal = SettingsModal.SingleChoice(
                                title = "Audio quality",
                                options = AudioQuality.entries.map { RadioOption(it, audioQualityLabel(it)) },
                                selectedValue = audioQuality,
                                onConfirm = { viewModel.setAudioQuality(it as AudioQuality) }
                            )
                        }
                    )
                    SettingItem(
                        title = "Video quality",
                        subtitle = videoQualityLabel(videoQuality),
                        onClick = {
                            activeModal = SettingsModal.SingleChoice(
                                title = "Video quality",
                                options = VideoQuality.entries.map { RadioOption(it, videoQualityLabel(it)) },
                                selectedValue = videoQuality,
                                onConfirm = { viewModel.setVideoQuality(it as VideoQuality) }
                            )
                        }
                    )
                    SettingItem(
                        title = "Offline mode",
                        subtitle = "Play downloaded songs only without network streaming",
                        switch = (offlineMode to { viewModel.setOfflineMode(it) })
                    )
                    SettingItem(
                        title = "Crossfade",
                        subtitle = "Smoothly transition between songs by fading out the current track while fading in the next one.",
                        smallSubtitle = true,
                        switch = (crossfadeEnabled to { viewModel.setCrossfadeEnabled(it) })
                    )
                    AnimatedVisibility(visible = crossfadeEnabled) {
                        Column {
                            SettingItem(
                                title = "Crossfade duration",
                                subtitle = if (crossfadeDuration == 0) "Auto (AutoMix)" else "${crossfadeDuration}s",
                                onClick = {
                                    val durations = listOf(0, 1, 2, 3, 5, 8, 10, 12, 15, 20, 30)
                                    activeModal = SettingsModal.SingleChoice(
                                        title = "Crossfade duration",
                                        options = durations.map { sec ->
                                            val label = if (sec == 0) "Auto (AutoMix)" else "${sec}s"
                                            RadioOption(sec, label)
                                        },
                                        selectedValue = crossfadeDuration,
                                        onConfirm = { viewModel.setCrossfadeDuration(it as Int) }
                                    )
                                }
                            )
                            SettingItem(
                                title = "DJ-Style Transition",
                                subtitle = "Apply low-pass and high-pass filter sweeps during crossfade for a smoother, DJ-like transition (similar to Apple Music Automix).",
                                smallSubtitle = true,
                                switch = (crossfadeDjMode to { viewModel.setCrossfadeDjMode(it) })
                            )
                            SettingItem(
                                title = "Keep albums continuous",
                                subtitle = "Skip the crossfade between tracks of the same album, so albums meant to run without gaps still do.",
                                smallSubtitle = true,
                                switch = (crossfadeSkipAlbum to { viewModel.setCrossfadeSkipAlbum(it) })
                            )
                        }
                    }
                    SettingItem(
                        title = "Auto looping canvas",
                        subtitle = "Automatically fetch short looping visuals when available",
                        switch = (autoCanvasEnabled to { viewModel.setAutoCanvasEnabled(it) })
                    )
                    SettingItem(
                        title = "Lock screen lyrics",
                        subtitle = "Show live synced lyrics overlay on the lock screen",
                        switch = (lockscreenLyricsEnabled to { viewModel.setLockscreenLyricsEnabled(it) })
                    )
                    SettingItem(
                        title = "Stop playback on exit",
                        subtitle = "Stop background service when EcoDot is swiped from recents",
                        switch = (killServiceOnExit to { viewModel.setKillServiceOnExit(it) })
                    )
                }
            }

            // ── SponsorBlock ──────────────────────────────────────────────────────
            item(key = "sponsor_block") {
                SettingGroup(
                    title = "SponsorBlock",
                    footer = {
                        val footerText = buildAnnotatedString {
                            append("Skip sponsors, intros, and non-music parts powered by ")
                            withStyle(
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("https://sponsor.ajay.app/")
                            }
                        }
                        Text(
                            text = footerText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.clickable {
                                runCatching { uriHandler.openUri("https://sponsor.ajay.app/") }
                            }
                        )
                    }
                ) {
                    SettingItem(
                        title = "Enable SponsorBlock",
                        subtitle = "Automatically skip sponsored or non-music segments",
                        switch = (sponsorBlockEnabled to { viewModel.setSponsorBlockEnabled(it) })
                    )
                    val selectedSummary = if (sponsorBlockCategories.isEmpty()) {
                        "No categories selected"
                    } else {
                        SponsorBlockCategory.entries
                            .filter { it.key in sponsorBlockCategories }
                            .joinToString(", ") { it.displayName }
                    }
                    SettingItem(
                        title = "Categories to skip",
                        subtitle = selectedSummary,
                        isEnable = sponsorBlockEnabled,
                        onClick = {
                            activeModal = SettingsModal.MultiChoice(
                                title = "Categories to skip",
                                options = SponsorBlockCategory.entries.map { RadioOption(it.key, it.displayName) },
                                selectedValues = sponsorBlockCategories,
                                onConfirm = { chosen ->
                                    viewModel.setSponsorBlockCategories(chosen)
                                }
                            )
                        }
                    )
                }
            }

            // ── Storage ───────────────────────────────────────────────────────────
            item(key = "storage") {
                val limitOptions = listOf(
                    0L to "Unlimited",
                    100L to "100 MB",
                    250L to "250 MB",
                    500L to "500 MB",
                    1024L to "1 GB",
                    2048L to "2 GB",
                    5120L to "5 GB"
                )
                val currentLimitLabel = limitOptions.firstOrNull { it.first == cacheLimitMb }?.second
                    ?: "$cacheLimitMb MB"

                SettingGroup(title = "Storage") {
                    SettingItem(
                        title = "Data saver",
                        subtitle = "Prefer lower bitrates when streaming on cellular connections",
                        switch = (dataSaver to { viewModel.setDataSaver(it) })
                    )
                    SettingItem(
                        title = "Player cache",
                        subtitle = formatBytesMb(storageStats.playerCacheBytes),
                        onClick = {
                            activeModal = SettingsModal.Confirm(
                                title = "Clear player cache?",
                                message = "Remove ${formatBytesMb(storageStats.playerCacheBytes)} of cached audio & video streams?",
                                confirmLabel = "Clear",
                                onConfirm = {
                                    viewModel.clearCache()
                                    Toast.makeText(context, "Player cache cleared", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                    SettingItem(
                        title = "Downloaded cache",
                        subtitle = formatBytesMb(storageStats.downloadedBytes),
                        onClick = {
                            activeModal = SettingsModal.Confirm(
                                title = "Clear downloaded songs?",
                                message = "Delete ${formatBytesMb(storageStats.downloadedBytes)} of offline downloaded tracks?",
                                confirmLabel = "Clear",
                                onConfirm = {
                                    viewModel.clearDownloadsCache()
                                    Toast.makeText(context, "Downloads cleared", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                    SettingItem(
                        title = "Thumbnail cache",
                        subtitle = formatBytesMb(storageStats.thumbnailCacheBytes),
                        onClick = {
                            activeModal = SettingsModal.Confirm(
                                title = "Clear thumbnail cache?",
                                message = "Remove ${formatBytesMb(storageStats.thumbnailCacheBytes)} of cached album artwork?",
                                confirmLabel = "Clear",
                                onConfirm = {
                                    viewModel.clearThumbnailCache()
                                    Toast.makeText(context, "Thumbnail cache cleared", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                    SettingItem(
                        title = "Limit player cache",
                        subtitle = "$currentLimitLabel (takes effect on next cache initialization)",
                        onClick = {
                            activeModal = SettingsModal.SingleChoice(
                                title = "Limit player cache",
                                options = limitOptions.map { RadioOption(it.first, it.second) },
                                selectedValue = cacheLimitMb,
                                onConfirm = { chosen ->
                                    viewModel.setCacheLimitMb(chosen as Long)
                                }
                            )
                        }
                    )

                    StorageBreakdownView(stats = storageStats)
                }
            }

            // ── About & Updates ───────────────────────────────────────────────────
            item(key = "about_us") {
                val isChecking = updateStatus is UpdateStatus.Checking
                val formattedLastCheck = remember(lastCheckTime) {
                    if (lastCheckTime <= 0L) {
                        "Tap to check latest release (GitHub & JSON)"
                    } else {
                        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        "Last checked at ${fmt.format(Date(lastCheckTime))}"
                    }
                }

                SettingGroup(title = "About & Updates") {
                    SettingItem(
                        title = "Version",
                        subtitle = "EcoDot $displayVersion · ${com.example.ecodot.data.local.prefs.UserPreferencesManager.APP_DESCRIPTION}",
                        smallSubtitle = true
                    )
                    SettingItem(
                        title = "Auto check for updates",
                        subtitle = "Check for a new release automatically when the app starts",
                        switch = (autoCheckUpdates to { viewModel.setAutoCheckUpdates(it) })
                    )
                    SettingItem(
                        title = "Check for updates",
                        subtitle = if (isChecking) "Checking..." else formattedLastCheck,
                        isEnable = !isChecking,
                        onClick = {
                            lastManualCheckTime = System.currentTimeMillis()
                            viewModel.checkForUpdates(force = true)
                        }
                    )
                    SettingItem(
                        title = "GitHub repository",
                        subtitle = "Debug01-dEoW/EcoDot-MusicApp · Source code & documentation",
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(com.example.ecodot.data.local.prefs.UserPreferencesManager.DEFAULT_GITHUB_REPO_URL)
                                    )
                                )
                            }
                        }
                    )
                    SettingItem(
                        title = "Releases & changelogs",
                        subtitle = "View published APKs & release notes on GitHub",
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(com.example.ecodot.data.local.prefs.UserPreferencesManager.DEFAULT_GITHUB_RELEASES_URL)
                                    )
                                )
                            }
                        }
                    )
                    SettingItem(
                        title = "Developer GitHub",
                        subtitle = "Made by Debug01-dEoW aka Deep · github.com/Debug01-dEoW",
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(com.example.ecodot.data.local.prefs.UserPreferencesManager.DEFAULT_GITHUB_PROFILE_URL)
                                    )
                                )
                            }
                        }
                    )
                    SettingItem(
                        title = "Privacy policy",
                        subtitle = "View source & privacy information on GitHub",
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("${com.example.ecodot.data.local.prefs.UserPreferencesManager.DEFAULT_GITHUB_REPO_URL}#readme")
                                    )
                                )
                            }
                        }
                    )
                    SettingItem(
                        title = "Email feedback",
                        subtitle = "deep082008.patel@gmail.com",
                        onClick = {
                            runCatching {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:deep082008.patel@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "EcoDot $displayVersion Feedback")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }

        // Pinned frosted top bar matching SimpMusic SettingScreen
        val settingBarTint = MaterialTheme.colorScheme.background
        AnimatedVisibility(
            visible = showPinnedBar,
            enter = fadeIn(animationSpec = tween(120)),
            exit = fadeOut(animationSpec = tween(120)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            val barModifier = if (liquidGlassEnabled) {
                Modifier
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                settingBarTint.copy(alpha = 0.90f),
                                settingBarTint.copy(alpha = 0.72f)
                            )
                        )
                    )
                    .hazeEffect(
                        state = localHazeState,
                        style = HazeStyle(
                            backgroundColor = settingBarTint.copy(alpha = 0.78f),
                            tint = HazeDefaults.tint(settingBarTint.copy(alpha = 0.62f)),
                            blurRadius = 16.dp,
                            noiseFactor = 0f
                        )
                    )
            } else {
                Modifier.background(settingBarTint.copy(alpha = 0.96f))
            }
            SettingTopBar(
                onBack = onBack,
                modifier = barModifier
            )
        }

        // Render modal pickers & confirmation dialogs
        RenderSettingsModal(
            modal = activeModal,
            onDismiss = { activeModal = null }
        )

        if (showColorPickerDialog) {
            CustomColorPickerDialog(
                currentHex = customThemeColorHex,
                onDismiss = { showColorPickerDialog = false },
                onConfirm = { newHex ->
                    viewModel.setCustomThemeColorHex(newHex)
                    viewModel.setThemeColorSource(ThemeColorSource.CUSTOM)
                    showColorPickerDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
) {
    TopAppBar(
        title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = SimpIcons.ArrowBackIos,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        windowInsets = windowInsets,
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun StorageBreakdownView(stats: com.example.ecodot.playback.StorageStats) {
    val total = stats.totalBytes.coerceAtLeast(1L).toFloat()
    val otherFraction = (stats.otherAppBytes / total).coerceIn(0f, 1f)
    val downloadsFraction = (stats.downloadedBytes / total).coerceIn(0f, 1f)
    val playerCacheFraction = (stats.playerCacheBytes / total).coerceIn(0f, 1f)
    val thumbFraction = (stats.thumbnailCacheBytes / total).coerceIn(0f, 1f)
    val dbFraction = (stats.databaseBytes / total).coerceIn(0f, 1f)
    val freeFraction = (stats.freeBytes / total).coerceIn(0f, 1f)

    val otherColor = MaterialTheme.colorScheme.primary
    val downloadsColor = Color(0xFF40FF17)
    val playerCacheColor = Color(0xFFFFFF00)
    val thumbColor = Color.Magenta
    val dbColor = Color.White
    val freeColor = Color.DarkGray

    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
        Spacer(Modifier.height(14.dp))

        var barWidthPx by remember { mutableIntStateOf(0) }
        val localDensity = LocalDensity.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(freeColor)
                .onGloballyPositioned { coords ->
                    barWidthPx = coords.size.width
                }
        ) {
            if (barWidthPx > 0) {
                val totalDp = with(localDensity) { barWidthPx.toDp() }
                val segments = listOf(
                    otherFraction to otherColor,
                    downloadsFraction to downloadsColor,
                    playerCacheFraction to playerCacheColor,
                    thumbFraction to thumbColor,
                    dbFraction to dbColor,
                    freeFraction to freeColor
                )
                segments.forEach { (fraction, color) ->
                    val minVisible = if (fraction > 0f && totalDp * fraction < 2.dp) 2.dp else totalDp * fraction
                    if (fraction > 0f) {
                        Box(
                            modifier = Modifier
                                .width(minVisible)
                                .fillMaxHeight()
                                .background(color)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        StorageLegendRow(otherColor, "Other apps", formatBytesGbOrMb(stats.otherAppBytes))
        StorageLegendRow(downloadsColor, "Downloaded cache", formatBytesMb(stats.downloadedBytes))
        StorageLegendRow(playerCacheColor, "Player cache", formatBytesMb(stats.playerCacheBytes))
        StorageLegendRow(thumbColor, "Thumbnail cache", formatBytesMb(stats.thumbnailCacheBytes))
        StorageLegendRow(dbColor, "Database", formatBytesMb(stats.databaseBytes))
        StorageLegendRow(Color.LightGray, "Free space", formatBytesGbOrMb(stats.freeBytes))
    }
}

@Composable
private fun StorageLegendRow(dotColor: Color, label: String, sizeLabel: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = sizeLabel,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RenderSettingsModal(
    modal: SettingsModal?,
    onDismiss: () -> Unit
) {
    if (modal == null) return

    when (modal) {
        is SettingsModal.SingleChoice -> {
            var selected by remember(modal) { mutableStateOf(modal.selectedValue) }
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                title = {
                    Text(
                        text = modal.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                        items(modal.options) { opt ->
                            val isSelected = selected == opt.value
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selected = opt.value }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selected = opt.value }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = opt.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .weight(1f)
                                        .basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            animationMode = MarqueeAnimationMode.Immediately
                                        )
                                        .focusable()
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        modal.onConfirm(selected)
                        onDismiss()
                    }) {
                        Text("Change", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }

        is SettingsModal.MultiChoice -> {
            var checkedSet by remember(modal) { mutableStateOf(modal.selectedValues) }
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                title = {
                    Text(
                        text = modal.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                        items(modal.options) { opt ->
                            val isChecked = opt.value in checkedSet
                            val toggle = {
                                checkedSet = if (isChecked) {
                                    checkedSet - opt.value
                                } else {
                                    checkedSet + opt.value
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { toggle() }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { toggle() }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = opt.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        modal.onConfirm(checkedSet)
                        onDismiss()
                    }) {
                        Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }

        is SettingsModal.Confirm -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                title = {
                    Text(
                        text = modal.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = modal.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        modal.onConfirm()
                        onDismiss()
                    }) {
                        Text(modal.confirmLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}

@Composable
private fun CustomColorPickerDialog(
    currentHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val presetColors = remember {
        listOf(
            "FF1ED760" to "Spotify",
            "FFFF5500" to "SoundCloud",
            "FFFA243C" to "Apple Music",
            "FFFF0000" to "YouTube Music",
            "FFA238FF" to "Deezer",
            "FF3668FF" to "Pandora",
            "FFFFA200" to "Audiomack",
            "FF1DA0C3" to "Bandcamp"
        )
    }

    var pendingHex by rememberSaveable { mutableStateOf(currentHex.takeLast(6)) }
    val parsedColor = parseHexColor(pendingHex)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = "Custom color",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                presetColors.chunked(4).forEach { rowColors ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        rowColors.forEach { (hex, name) ->
                            val color = parseHexColor(hex) ?: Color.Gray
                            val isSelected = pendingHex.equals(hex.takeLast(6), ignoreCase = true)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { pendingHex = hex.takeLast(6) }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = pendingHex,
                    onValueChange = { input ->
                        val cleaned = input.removePrefix("#").filter { it.isDigit() || it.lowercaseChar() in 'a'..'f' }.take(6).uppercase()
                        pendingHex = cleaned
                    },
                    label = { Text("HEX") },
                    prefix = { Text("#") },
                    singleLine = true,
                    isError = parsedColor == null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = parsedColor != null,
                onClick = { onConfirm(pendingHex) }
            ) {
                Text("Change", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

fun parseHexColor(hex: String): Color? {
    val clean = hex.removePrefix("#").trim()
    val full = when (clean.length) {
        6 -> "FF$clean"
        8 -> clean
        else -> return null
    }
    return try {
        Color(full.toLong(16))
    } catch (e: Exception) {
        null
    }
}

private fun playerStyleLabel(style: PlayerStyle): String = when (style) {
    PlayerStyle.ECODOT -> "EcoDot Classic · Liquid Glass & Video Aurora"
    PlayerStyle.APPLE_MUSIC -> "Apple Music · Mesh Backdrop, Lyric Strip & Dock"
    PlayerStyle.SPOTIFY -> "Spotify · Obsidian Canvas & Vibrant Cards"
    PlayerStyle.VINYL -> "Vinyl Turntable · 33⅓ RPM Platter & Tonearm"
}

private fun audioQualityLabel(q: AudioQuality): String = when (q) {
    AudioQuality.LOW -> "Low · ~48 kbps (saves data)"
    AudioQuality.NORMAL -> "Normal · ~128 kbps (balanced)"
    AudioQuality.HIGH -> "High · Best quality (up to 256 kbps Opus)"
    AudioQuality.HIGH_AAC -> "High AAC · Best AAC/M4A (up to 256 kbps)"
}

private fun videoQualityLabel(q: VideoQuality): String = when (q) {
    VideoQuality.LOW -> "Low · 360p (saves data)"
    VideoQuality.NORMAL -> "Normal · 720p (balanced)"
    VideoQuality.HIGH -> "High · 1080p+ (when available)"
}

private fun formatBytesMb(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val mb = bytes / (1024f * 1024f)
    return if (mb < 1f) String.format(Locale.US, "%.2f MB", mb) else String.format(Locale.US, "%.1f MB", mb)
}

private fun formatBytesGbOrMb(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val gb = bytes / (1024f * 1024f * 1024f)
    return if (gb >= 1f) String.format(Locale.US, "%.2f GB", gb) else formatBytesMb(bytes)
}
