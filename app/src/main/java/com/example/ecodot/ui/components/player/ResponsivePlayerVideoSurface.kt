package com.example.ecodot.ui.components.player
import com.example.ecodot.ui.theme.AspectRatio
import com.example.ecodot.ui.theme.Headphones
import com.example.ecodot.ui.theme.MusicNote
import com.example.ecodot.ui.theme.PlayCircle

import android.view.HapticFeedbackConstants
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.R
import com.example.ecodot.data.remote.YouTubeParser
import com.example.ecodot.ui.theme.Forward5
import com.example.ecodot.ui.theme.Fullscreen
import com.example.ecodot.ui.theme.Replay5
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Subtitles
import com.example.ecodot.ui.theme.SubtitlesOff
import com.example.ecodot.ui.theme.poppinsFamily
import com.example.ecodot.ui.viewmodel.LyricLine

/**
 * Responsive video playback surface shared across Apple Music, Vinyl, Spotify, and EcoDot styles.
 * Handles controller attachment/cleanup safely, live synced subtitles overlay, FIT/ZOOM toggle,
 * tap-to-toggle 5s seek / fullscreen / captions overlay controls, and orientation changes cleanly.
 */
@OptIn(UnstableApi::class)
@Composable
fun ResponsivePlayerVideoSurface(
    controller: MediaController?,
    trackId: String,
    videoResizeMode: Int,
    showControls: Boolean,
    showCaptions: Boolean,
    syncedLyrics: List<LyricLine>,
    positionMs: Long,
    onToggleControls: () -> Unit,
    onToggleCaptions: () -> Unit,
    onToggleResizeMode: () -> Unit,
    onEnterFullscreen: () -> Unit,
    onSeekBackward5s: () -> Unit,
    onSeekForward5s: () -> Unit,
    modifier: Modifier = Modifier,
    fallbackArtUrl: String? = null,
    positionProvider: (() -> Long)? = null
) {
    val view = LocalView.current
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onToggleControls
            )
    ) {
        if (!fallbackArtUrl.isNullOrBlank()) {
            val highArt = remember(trackId, fallbackArtUrl) {
                YouTubeParser.getHighResUrl(fallbackArtUrl, isWide = true) ?: fallbackArtUrl
            }
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(highArt)
                    .size(480, 480)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.45f)
            )
        }
        if (controller != null) {
            val playerViewRef = remember { arrayOfNulls<PlayerView>(1) }
            AndroidView(
                factory = { ctx ->
                    (android.view.LayoutInflater.from(ctx).inflate(
                        R.layout.responsive_player_video_view,
                        null
                    ) as PlayerView).apply {
                        useController = false
                        resizeMode = videoResizeMode
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        setKeepContentOnPlayerReset(true)
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        player = controller
                        playerViewRef[0] = this
                    }
                },
                update = { playerView ->
                    playerView.resizeMode = videoResizeMode
                    if (playerView.player != controller) {
                        playerView.player = controller
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            DisposableEffect(controller) {
                onDispose {
                    playerViewRef[0]?.player = null
                }
            }
        }

        // Synced subtitle overlay isolated in child scope so AndroidView PlayerView never recomposes on ticks
        ResponsiveVideoSubtitleOverlay(
            syncedLyrics = syncedLyrics,
            showCaptions = showCaptions,
            showControls = showControls,
            positionProvider = positionProvider ?: { positionMs },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 16.dp,
                    end = 44.dp,
                    bottom = if (showControls) 16.dp else 10.dp
                )
        )

        // Over-video tap controls matching SimpMusic's Apple Music / M3E overlay
        Crossfade(
            targetState = showControls,
            label = "responsiveVideoControls",
            modifier = Modifier.fillMaxSize()
        ) { shown ->
            if (shown) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.34f))
                ) {
                    // Top-right: Aspect ratio FIT/ZOOM toggle & Fullscreen
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onToggleResizeMode()
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = SimpIcons.AspectRatio,
                                contentDescription = if (videoResizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) "Fit video" else "Fill video",
                                tint = if (videoResizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) Color(0xFFFFD54F) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onEnterFullscreen()
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = SimpIcons.Fullscreen,
                                contentDescription = "Fullscreen video",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Center: Replay 5s / Forward 5s
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onSeekBackward5s()
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.30f))
                        ) {
                            Icon(
                                imageVector = SimpIcons.Replay5,
                                contentDescription = "Rewind 5 seconds",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(32.dp)
                                    .alpha(0.92f)
                            )
                        }
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onSeekForward5s()
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.30f))
                        ) {
                            Icon(
                                imageVector = SimpIcons.Forward5,
                                contentDescription = "Forward 5 seconds",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(32.dp)
                                    .alpha(0.92f)
                            )
                        }
                    }

                    // Bottom-right: Captions / Subtitles toggle (whenever lyrics exist)
                    if (syncedLyrics.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onToggleCaptions()
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (showCaptions) SimpIcons.SubtitlesOff else SimpIcons.Subtitles,
                                contentDescription = if (showCaptions) "Hide subtitles" else "Show subtitles",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResponsiveVideoSubtitleOverlay(
    syncedLyrics: List<LyricLine>,
    showCaptions: Boolean,
    showControls: Boolean,
    positionProvider: () -> Long,
    modifier: Modifier = Modifier
) {
    val activeLyricLine by remember(syncedLyrics, positionProvider) {
        derivedStateOf {
            val pos = positionProvider()
            if (syncedLyrics.isEmpty()) null
            else syncedLyrics.lastOrNull { it.timeMs <= pos }?.text
        }
    }
    AnimatedVisibility(
        visible = showCaptions && !activeLyricLine.isNullOrBlank(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.62f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = activeLyricLine.orEmpty(),
                fontFamily = poppinsFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

/**
 * Compact frosted pill switching smoothly between Song Audio mode and Music Video mode
 * inside any Now Playing style for YouTube tracks.
 */
@Composable
fun AudioVideoModeTogglePill(
    isVideoMode: Boolean,
    accentColor: Color,
    onSetVideoMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.34f),
        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.16f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeSegment(
                label = "Song",
                selected = !isVideoMode,
                accentColor = accentColor,
                icon = SimpIcons.MusicNote,
                onClick = {
                    if (isVideoMode) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onSetVideoMode(false)
                    }
                }
            )
            Spacer(modifier = Modifier.width(2.dp))
            ModeSegment(
                label = "Video",
                selected = isVideoMode,
                accentColor = accentColor,
                icon = SimpIcons.PlayCircle,
                onClick = {
                    if (!isVideoMode) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onSetVideoMode(true)
                    }
                }
            )
        }
    }
}

@Composable
private fun ModeSegment(
    label: String,
    selected: Boolean,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val bg = if (selected) accentColor.copy(alpha = 0.88f) else Color.Transparent
    val fg = if (selected) Color.Black else Color.White.copy(alpha = 0.82f)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontFamily = poppinsFamily,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = fg,
            maxLines = 1
        )
    }
}

/**
 * Reusable Audio Output quick sheet triggered by Apple Music & Spotify device controls.
 */
@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun QuickAudioOutputSheet(
    currentDeviceName: String,
    accentColor: Color,
    onOpenEqualizer: () -> Unit,
    onOpenSystemBluetooth: () -> Unit,
    onDismiss: () -> Unit
) {
    val (volumeFraction, setVolumeFraction) = rememberSystemMediaVolumeController()
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                .padding(horizontal = 22.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Audio Output & Volume",
                fontFamily = poppinsFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Currently routing through $currentDeviceName",
                fontFamily = poppinsFamily,
                fontSize = 12.5.sp,
                color = Color.White.copy(alpha = 0.58f),
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = accentColor.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = SimpIcons.Headphones,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentDeviceName,
                            fontFamily = poppinsFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Active media route",
                            fontFamily = poppinsFamily,
                            fontSize = 11.5.sp,
                            color = accentColor
                        )
                    }
                    Text(
                        text = "${(volumeFraction * 100).toInt()}%",
                        fontFamily = poppinsFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            androidx.compose.material3.Slider(
                value = volumeFraction,
                onValueChange = setVolumeFraction,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color.White.copy(alpha = 0.18f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onDismiss()
                            onOpenEqualizer()
                        }
                ) {
                    Text(
                        text = "Open Equalizer",
                        fontFamily = poppinsFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onDismiss()
                            onOpenSystemBluetooth()
                        }
                ) {
                    Text(
                        text = "Pair / Switch Device",
                        fontFamily = poppinsFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}
