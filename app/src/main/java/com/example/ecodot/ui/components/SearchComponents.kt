@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.example.ecodot.ui.components
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.AutoAwesome
import com.example.ecodot.ui.theme.Check
import com.example.ecodot.ui.theme.ChevronRight
import com.example.ecodot.ui.theme.Close
import com.example.ecodot.ui.theme.Download
import com.example.ecodot.ui.theme.DownloadForOffline
import com.example.ecodot.ui.theme.Favorite
import com.example.ecodot.ui.theme.FavoriteBorder
import com.example.ecodot.ui.theme.MoreVert
import com.example.ecodot.ui.theme.MusicNote
import com.example.ecodot.ui.theme.PlayArrow
import com.example.ecodot.ui.theme.PlaylistAdd
import com.example.ecodot.ui.theme.PlaylistPlay
import com.example.ecodot.ui.theme.QueueMusic
import com.example.ecodot.ui.theme.SelectAll

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.viewmodel.Artist
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

/**
 * Generates deterministic 2-stop HSV gradient colors from a category or playlist title,
 * matching SimpMusic's `generateGradientFromTitle`.
 */
fun generateSearchGradientColors(title: String): List<Color> {
    val hash = title.hashCode()
    val hue1 = ((hash and 0xFF) / 255f) * 360f
    val hue2 = (((hash shr 8) and 0xFF) / 255f) * 360f
    return listOf(
        hsvToComposeColor(hue1, 0.72f, 0.76f),
        hsvToComposeColor(hue2, 0.72f, 0.56f)
    )
}

private fun hsvToComposeColor(hue: Float, saturation: Float, value: Float): Color {
    val h = hue / 60f
    val c = value * saturation
    val x = c * (1f - abs((h % 2f) - 1f))
    val m = value - c
    val (r, g, b) = when (h.toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(red = r + m, green = g + m, blue = b + m, alpha = 1f)
}

/**
 * Extracts YouTube / YouTube Music destinations (`video`, `playlist`, `artist`) from pasted links.
 */
fun extractYouTubeLinkOrNull(input: String): Pair<String, String>? {
    val trimmed = input.trim()
    if (!trimmed.contains("youtube.com", ignoreCase = true) &&
        !trimmed.contains("youtu.be", ignoreCase = true)
    ) {
        return null
    }
    Regex("(?:youtu\\.be/|/watch\\?v=|[?&]v=|/v/|/embed/)([a-zA-Z0-9_-]{11})")
        .find(trimmed)
        ?.let { return Pair("video", it.groupValues[1]) }
    Regex("[?&]list=([a-zA-Z0-9_-]+)")
        .find(trimmed)
        ?.let { return Pair("playlist", it.groupValues[1]) }
    Regex("/channel/(UC[a-zA-Z0-9_-]+)")
        .find(trimmed)
        ?.let { return Pair("artist", it.groupValues[1]) }
    return null
}

/**
 * SimpMusic-style Explore Charts pill button shown under the Empty Search header.
 */
@Composable
fun SimpMusicChartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1A0F0F),
        border = BorderStroke(1.dp, Color(0xFF3D2828))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = SimpIcons.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFFFB74D),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Explore Music Charts",
                fontSize = 13.sp,
                color = Color(0xFFD0C8C8),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 2:1 Moods & Genres browse tile matching SimpMusic's `MoodCategoryCard`:
 * deterministic 45-degree gradient, top-end badge dot, and 25-degree tilted cover art.
 */
@Composable
fun SimpMoodCategoryCard(
    title: String,
    artworkUrl: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradientColors = remember(title) { generateSearchGradientColors(title) }
    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .drawBehind {
                val radius = size.height * 0.085f
                val centerX = if (layoutDirection == LayoutDirection.Rtl) radius * 2f else size.width - radius * 2f
                val centerY = radius * 2f
                drawCircle(
                    center = Offset(centerX, centerY),
                    color = Color.White.copy(alpha = 0.85f),
                    radius = radius
                )
                drawCircle(
                    center = Offset(centerX, centerY),
                    color = Color.Black.copy(alpha = 0.22f),
                    radius = radius * 0.45f
                )
            }
            .clickable(onClick = onClick)
    ) {
        if (!artworkUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artworkUrl)
                    .size(240, 240)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 12.dp)
                    .size(64.dp)
                    .rotate(25f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.22f))
            )
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 72.dp)
        )
    }
}

/**
 * Full-width SimpMusic search track row supporting swipe-to-queue, multi-select mode,
 * downloaded badge, explicit/LIVE badges, marquee text, playing indicator, like, and options.
 */
@Composable
fun SimpSearchSongRow(
    track: Track,
    isVideo: Boolean = false,
    isPlaying: Boolean,
    isDownloaded: Boolean,
    isLiked: Boolean,
    selectionMode: Boolean,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onLongClick: () -> Unit,
    onLikeToggle: () -> Unit,
    onArtistClick: (() -> Unit)?,
    onOptionsClick: () -> Unit,
    onAddToQueue: () -> Unit,
    onClick: () -> Unit
) {
    val isLive = track.duration == 0L && (
        track.title.contains("live", ignoreCase = true) &&
            track.title.contains("stream", ignoreCase = true)
    )
    val durationText = remember(track.duration) {
        if (track.duration > 0L) {
            val totalSec = track.duration / 1000L
            String.format(Locale.US, " · %d:%02d", totalSec / 60L, totalSec % 60L)
        } else ""
    }

    SwipeToQueueBox(
        enabled = !isPlaying && !selectionMode,
        onAddToQueue = onAddToQueue
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) Color(0xFFF0E3FF).copy(alpha = 0.14f) else Color.Transparent
                )
                .combinedClickable(
                    onClick = {
                        if (selectionMode) onSelectToggle() else onClick()
                    },
                    onLongClick = onLongClick
                )
                .padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = selectionMode,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFFF0E3FF) else Color.Transparent)
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) Color(0xFFF0E3FF) else Color.White.copy(alpha = 0.6f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = SimpIcons.Check,
                                contentDescription = null,
                                tint = Color(0xFF1A102B),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }

            Box(
                modifier = Modifier
                    .size(width = if (isVideo) 62.dp else 48.dp, height = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF161722))
                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!track.albumArtUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(track.albumArtUri)
                            .size(240, 200)
                            .crossfade(400)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = SimpIcons.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.56f)),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayingIndicator(color = Color(0xFFF0E3FF))
                    }
                } else if (isVideo) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color.Black.copy(alpha = 0.62f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = SimpIcons.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (isPlaying) Color(0xFFF0E3FF) else Color.White,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isPlaying) Modifier.basicMarquee(iterations = Int.MAX_VALUE) else Modifier)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDownloaded) {
                        Icon(
                            imageVector = SimpIcons.DownloadForOffline,
                            contentDescription = "Downloaded",
                            tint = Color(0xFF81C784),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(14.dp)
                        )
                    }
                    if (track.isExplicit) {
                        ExplicitBadge(modifier = Modifier.padding(end = 5.dp))
                    }
                    if (isLive) {
                        HomeLiveBadge(modifier = Modifier.padding(end = 5.dp))
                    }
                    Text(
                        text = buildString {
                            append(track.artist.ifBlank { "YouTube Music" })
                            if (durationText.isNotEmpty()) append(durationText)
                        },
                        color = Color(0xC4FFFFFF),
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .then(
                                if (onArtistClick != null && !selectionMode) {
                                    Modifier.clickable(onClick = onArtistClick)
                                } else Modifier
                            )
                            .then(if (isPlaying) Modifier.basicMarquee(iterations = Int.MAX_VALUE) else Modifier)
                    )
                }
            }

            if (!selectionMode) {
                IconButton(
                    onClick = onLikeToggle,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) SimpIcons.Favorite else SimpIcons.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) EcoDotRed else Color.White.copy(alpha = 0.40f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onOptionsClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = SimpIcons.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full-width SimpMusic container row shared across Albums, Playlists, Featured Playlists, and Podcasts.
 */
@Composable
fun SimpSearchCollectionRow(
    title: String,
    typeLabel: String,
    subtitle: String,
    thumbnailUrl: String?,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .size(200, 200)
                .crossfade(true)
                .build(),
            contentDescription = title,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1B26))
                .border(0.5.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (subtitle.isNotBlank()) "$typeLabel • $subtitle" else typeLabel,
                color = Color(0xC4FFFFFF),
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = SimpIcons.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Full-width SimpMusic Artist row.
 */
@Composable
fun SimpSearchArtistRow(
    artist: Artist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(artist.imageUrl)
                .size(200, 200)
                .crossfade(true)
                .build(),
            contentDescription = artist.name,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E202C))
                .border(0.6.dp, Color.White.copy(alpha = 0.15f), CircleShape),
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
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Artist",
                color = Color(0xC4FFFFFF),
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = SimpIcons.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * SimpMusic multi-select TopAppBar replacing the search chrome while songs are selected.
 */
@Composable
fun SimpSongSelectionTopAppBar(
    selectedCount: Int,
    onExit: () -> Unit,
    onSelectAll: () -> Unit,
    onOpenActions: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1D1829).copy(alpha = 0.94f))
            .border(0.8.dp, Color(0xFFF0E3FF).copy(alpha = 0.32f), RoundedCornerShape(14.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onExit) {
            Icon(
                imageVector = SimpIcons.Close,
                contentDescription = "Exit selection",
                tint = Color.White
            )
        }
        Text(
            text = "$selectedCount selected",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp)
        )
        IconButton(onClick = onSelectAll) {
            Icon(
                imageVector = SimpIcons.SelectAll,
                contentDescription = "Select all",
                tint = Color(0xFFF0E3FF)
            )
        }
        if (selectedCount > 0) {
            IconButton(onClick = onOpenActions) {
                Icon(
                    imageVector = SimpIcons.MoreVert,
                    contentDescription = "Batch actions",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Batch action bottom sheet matching SimpMusic `SelectedSongsBottomSheet`.
 */
@Composable
fun SimpSelectedSongsBottomSheet(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDownload: () -> Unit,
    onAddToFavorites: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun act(block: () -> Unit) {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            block()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF14151E),
        contentColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 14.dp, bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
            Text(
                text = "$selectedCount ${if (selectedCount == 1) "song" else "songs"} selected",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            SelectionSheetAction(SimpIcons.PlaylistPlay, "Play next") { act(onPlayNext) }
            SelectionSheetAction(SimpIcons.QueueMusic, "Add to queue") { act(onAddToQueue) }
            SelectionSheetAction(SimpIcons.PlaylistAdd, "Add to playlist") { act(onAddToPlaylist) }
            SelectionSheetAction(SimpIcons.Download, "Download all") { act(onDownload) }
            SelectionSheetAction(SimpIcons.FavoriteBorder, "Add to favorites") { act(onAddToFavorites) }
        }
    }
}

@Composable
private fun SelectionSheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFF0E3FF),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 10-row skeleton shimmer matching SimpMusic's `ShimmerSearchItem`.
 */
@Composable
fun SimpSearchShimmer(searchBarHeight: Dp) {
    LazyColumn(
        userScrollEnabled = false,
        contentPadding = PaddingValues(top = searchBarHeight + 8.dp, bottom = 120.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(10) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .simpShimmer()
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(15.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .simpShimmer()
                    )
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(13.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .simpShimmer()
                    )
                    Box(
                        modifier = Modifier
                            .width(84.dp)
                            .height(11.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .simpShimmer()
                    )
                }
            }
        }
    }
}
