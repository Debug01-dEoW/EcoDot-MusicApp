package com.example.ecodot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.theme.EcoDotCard
import com.example.ecodot.util.PlaylistCustomizer
import kotlin.math.abs

@Composable
fun PlaylistMosaicThumbnail(
    playlist: Playlist,
    tracks: List<Track>,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    iconSize: Dp = 28.dp
) {
    val context = LocalContext.current
    val customCover = PlaylistCustomizer.parseCustomCover(playlist.coverArtUri)

    Box(
        modifier = modifier
            .clip(shape)
            .background(EcoDotCard),
        contentAlignment = Alignment.Center
    ) {
        if (customCover != null) {
            val startParsed = PlaylistCustomizer.parseColorSafe(customCover.startColor, Color(0xFF282A3A))
            val endParsed = PlaylistCustomizer.parseColorSafe(customCover.endColor, Color(0xFF161720))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(startParsed, endParsed))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.QueueMusic,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(iconSize)
                )
            }
        } else if (!playlist.coverArtUri.isNullOrEmpty() &&
            (playlist.coverArtUri.startsWith("http") || playlist.coverArtUri.startsWith("file") || playlist.coverArtUri.startsWith("content"))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(playlist.coverArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val validArts = tracks.mapNotNull { it.albumArtUri?.takeIf { uri -> uri.isNotBlank() } }.distinct()
            if (validArts.size >= 4) {
                // SimpMusic / Spotify 4-quadrant mosaic
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(validArts[0]).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(validArts[1]).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(validArts[2]).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(validArts[3]).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            } else if (validArts.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(validArts[0])
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Sleek dark matte container matching Library aesthetic
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF232532), Color(0xFF14151E))
                            )
                        )
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier
                            .size(iconSize)
                            .align(Alignment.TopEnd)
                    )
                    Text(
                        text = playlist.name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}

/**
 * Deterministic vibrant gradient color generator matching SimpMusic.
 */
fun generateGradientFromTitle(title: String): List<Color> {
    val hash = title.hashCode()
    val hue1 = ((hash and 0xFF) / 255f) * 360f
    val hue2 = (((hash shr 8) and 0xFF) / 255f) * 360f
    val color1 = hsvToColor(hue1, 0.72f, 0.85f)
    val color2 = hsvToColor(hue2, 0.72f, 0.55f)
    return listOf(color1, color2)
}

private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    val h = hue / 60f
    val c = value * saturation
    val x = c * (1 - abs((h % 2) - 1))
    val m = value - c

    val (r, g, b) = when (h.toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r + m).coerceIn(0f, 1f),
        green = (g + m).coerceIn(0f, 1f),
        blue = (b + m).coerceIn(0f, 1f),
        alpha = 1f
    )
}
