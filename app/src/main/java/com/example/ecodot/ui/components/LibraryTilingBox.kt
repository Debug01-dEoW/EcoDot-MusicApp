package com.example.ecodot.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

internal val AestheticCardShape = RoundedCornerShape(22.dp)
internal val AestheticCardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))

/**
 * Empty sleeve with dashed outline when collection has no covers yet,
 * exactly matching SimpMusic's visual empty state.
 */
@Composable
private fun EmptySleeve(
    shape: Shape,
    modifier: Modifier
) {
    val outline = Color.White.copy(alpha = 0.22f)
    val fill = Color.White.copy(alpha = 0.03f)
    Box(
        modifier = modifier.drawBehind {
            val sleeveOutline = shape.createOutline(size, layoutDirection, this)
            drawOutline(sleeveOutline, fill)
            drawOutline(
                outline = sleeveOutline,
                color = outline,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.5.dp.toPx()))
                )
            )
        }
    )
}

/**
 * Up to [slots] sleeves fanned out from the centre, each [tilt] degrees and [spread] further out,
 * with the newest artwork rendered last on top.
 */
@Composable
fun SleeveFan(
    thumbnails: List<String>?,
    slots: Int,
    sleeve: Dp,
    spread: Dp,
    tilt: Float,
    round: Boolean,
    ring: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val shape = if (round) CircleShape else RoundedCornerShape(12.dp)
        val covers = thumbnails?.filter { it.isNotBlank() }?.take(slots) ?: emptyList()
        val count = if (covers.isEmpty()) slots else covers.size

        for (i in 0 until count) {
            val step = i - (count - 1) / 2f
            val placed = Modifier
                .size(sleeve)
                .graphicsLayer {
                    translationX = (spread * step).toPx()
                    rotationZ = tilt * step
                }

            if (covers.isEmpty()) {
                EmptySleeve(shape = shape, modifier = placed)
            } else {
                // Oldest first, newest on top
                val thumbUrl = covers[count - 1 - i]
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = placed
                        .clip(shape)
                        .border(2.dp, ring, shape)
                )
            }
        }
    }
}

/**
 * Top wide card for Favorites (Liked Songs) fanning out 3 album covers.
 */
@Composable
fun FavoriteCard(
    count: Int,
    thumbnails: List<String>,
    container: Color = Color(0xFF1B1C23),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(136.dp),
        shape = AestheticCardShape,
        border = AestheticCardBorder,
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = "Favorite",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 21.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (count == 0) "Like songs to fill your collection" else "$count songs",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = if (count > 0) Color(0xFFE7D7FD) else Color.White.copy(alpha = 0.55f),
                    fontWeight = if (count > 0) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            SleeveFan(
                thumbnails = thumbnails,
                slots = 3,
                sleeve = 76.dp,
                spread = 38.dp,
                tilt = 11f,
                round = false,
                ring = container,
                modifier = Modifier.size(width = 152.dp, height = 112.dp)
            )
        }
    }
}

/**
 * 2-column collection card for Followed, Most Played, Downloaded, Cached.
 */
@Composable
fun CollectionCard(
    title: String,
    countText: String,
    thumbnails: List<String>,
    round: Boolean,
    container: Color = Color(0xFF1B1C23),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = AestheticCardShape,
        border = AestheticCardBorder,
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SleeveFan(
                thumbnails = thumbnails,
                slots = 2,
                sleeve = 42.dp,
                spread = 20.dp,
                tilt = 9f,
                round = round,
                ring = container,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .size(width = 66.dp, height = 50.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = countText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * The complete SimpMusic Library Tiling Box layout:
 * - Top: Wide Favorite Card
 * - Row 2: Followed (Artists) + Most Played (Songs)
 * - Row 3: Downloaded (Songs) + Cached (Songs)
 */
@Composable
fun LibraryTilingBox(
    favoriteCount: Int,
    favoriteThumbnails: List<String>,
    onFavoriteClick: () -> Unit,
    followedCount: Int,
    followedThumbnails: List<String>,
    onFollowedClick: () -> Unit,
    mostPlayedCount: Int,
    mostPlayedThumbnails: List<String>,
    onMostPlayedClick: () -> Unit,
    downloadedCount: Int,
    downloadedThumbnails: List<String>,
    onDownloadedClick: () -> Unit,
    cachedCount: Int,
    cachedThumbnails: List<String>,
    onCachedClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF1B1C23)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Favorite Wide Card
        FavoriteCard(
            count = favoriteCount,
            thumbnails = favoriteThumbnails,
            container = containerColor,
            onClick = onFavoriteClick
        )

        // Row 2: Followed + Most Played
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CollectionCard(
                title = "Followed",
                countText = "$followedCount artists",
                thumbnails = followedThumbnails,
                round = true,
                container = containerColor,
                modifier = Modifier.weight(1f),
                onClick = onFollowedClick
            )
            CollectionCard(
                title = "Most Played",
                countText = "$mostPlayedCount songs",
                thumbnails = mostPlayedThumbnails,
                round = false,
                container = containerColor,
                modifier = Modifier.weight(1f),
                onClick = onMostPlayedClick
            )
        }

        // Row 3: Downloaded + Cached
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CollectionCard(
                title = "Downloaded",
                countText = "$downloadedCount songs",
                thumbnails = downloadedThumbnails,
                round = false,
                container = containerColor,
                modifier = Modifier.weight(1f),
                onClick = onDownloadedClick
            )
            CollectionCard(
                title = "Cached",
                countText = "$cachedCount songs",
                thumbnails = cachedThumbnails,
                round = false,
                container = containerColor,
                modifier = Modifier.weight(1f),
                onClick = onCachedClick
            )
        }
    }
}
