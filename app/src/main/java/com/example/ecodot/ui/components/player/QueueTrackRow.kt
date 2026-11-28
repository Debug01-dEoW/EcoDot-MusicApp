package com.example.ecodot.ui.components.player
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Delete
import com.example.ecodot.ui.theme.DragHandle
import com.example.ecodot.ui.theme.MoreVert

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Track

@Stable
class QueueDragReorderState(
    private val itemCountProvider: () -> Int,
    private val onMoveRelative: (from: Int, to: Int) -> Unit
) {
    var draggedFromIndex by mutableIntStateOf(-1)
        private set
    var draggedToIndex by mutableIntStateOf(-1)
        private set
    var dragAccumPx by mutableFloatStateOf(0f)
        private set
    var itemHeightPx by mutableFloatStateOf(72f)
        private set

    fun isDragged(relativeIndex: Int): Boolean = draggedFromIndex == relativeIndex

    fun shiftAmountPx(relativeIndex: Int): Float {
        if (draggedFromIndex == -1 || draggedToIndex == -1 || relativeIndex == draggedFromIndex) return 0f
        val lo = minOf(draggedFromIndex, draggedToIndex)
        val hi = maxOf(draggedFromIndex, draggedToIndex)
        if (relativeIndex !in lo..hi) return 0f
        return if (draggedToIndex > draggedFromIndex) -itemHeightPx else itemHeightPx
    }

    fun updateItemHeight(heightPx: Float) {
        if (heightPx > 0f) itemHeightPx = heightPx
    }

    fun onDragStart(relativeIndex: Int) {
        draggedFromIndex = relativeIndex
        draggedToIndex = relativeIndex
        dragAccumPx = 0f
    }

    fun onDrag(dy: Float) {
        if (draggedFromIndex == -1) return
        val maxIdx = (itemCountProvider() - 1).coerceAtLeast(0)
        dragAccumPx += dy
        val rawSteps = dragAccumPx / itemHeightPx
        val steps = if (rawSteps > 0f) (rawSteps + 0.5f).toInt() else (rawSteps - 0.5f).toInt()
        draggedToIndex = (draggedFromIndex + steps).coerceIn(0, maxIdx)
    }

    fun onDragEnd() {
        val from = draggedFromIndex
        val to = draggedToIndex
        draggedFromIndex = -1
        draggedToIndex = -1
        dragAccumPx = 0f
        if (from != -1 && from != to) {
            onMoveRelative(from, to)
        }
    }
}

@Composable
fun rememberQueueDragReorderState(
    itemCount: Int,
    onMoveRelative: (from: Int, to: Int) -> Unit
): QueueDragReorderState {
    val countState = rememberUpdatedState(itemCount)
    val moveState = rememberUpdatedState(onMoveRelative)
    return remember {
        QueueDragReorderState(
            itemCountProvider = { countState.value },
            onMoveRelative = { from, to -> moveState.value(from, to) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSwipeRemoveBox(
    enabled: Boolean,
    isDragged: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val isDraggedState = rememberUpdatedState(isDragged)
    val enabledState = rememberUpdatedState(enabled)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (enabledState.value && !isDraggedState.value &&
                (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd)
            ) {
                onRemove()
                true
            } else {
                false
            }
        },
        positionalThreshold = { total -> total * 0.35f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val fraction = runCatching { dismissState.requireOffset() }
                .getOrDefault(0f)
                .let { (kotlin.math.abs(it) / 300f).coerceIn(0f, 1f) }
            val bgColor = Color(
                red = (0.17f * fraction).coerceIn(0f, 1f),
                green = 0f,
                blue = 0f,
                alpha = fraction
            )
            val iconAlign = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                Alignment.CenterStart
            } else {
                Alignment.CenterEnd
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = iconAlign
            ) {
                Icon(
                    SimpIcons.Delete,
                    contentDescription = "Remove from Queue",
                    tint = Color.White.copy(alpha = fraction.coerceIn(0f, 1f))
                )
            }
        },
        content = content
    )
}

@Composable
fun QueueTrackRow(
    track: Track,
    isDragged: Boolean,
    animatedShiftPx: Float,
    dragOffsetPx: Float,
    accentColor: Color,
    onTrackClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onItemHeightMeasured: (Float) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (dy: Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val elevation by animateFloatAsState(
        targetValue = if (isDragged) 16f else 0f,
        animationSpec = tween(120),
        label = "drag_elevation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isDragged) 1.025f else 1f,
        animationSpec = tween(120),
        label = "drag_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isDragged) Color(0xFF1E1E22) else Color(0xFF0A0A0A),
        animationSpec = tween(120),
        label = "drag_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size -> onItemHeightMeasured(size.height.toFloat()) }
            .graphicsLayer {
                // Shift: neighboring items slide smoothly to reveal the drop slot
                translationY = if (isDragged) dragOffsetPx else animatedShiftPx
                scaleX = scale
                scaleY = scale
                shadowElevation = elevation
            }
            .background(bgColor)
            .clickable(onClick = onTrackClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art
        AsyncImage(
            model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(track.albumArtUri)
                .size(200, 200)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(14.dp))

        // Track info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Options button
        IconButton(
            onClick = onOptionsClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                SimpIcons.MoreVert,
                contentDescription = "Options",
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Drag handle — receives touch on the handle only
        Icon(
            imageVector = SimpIcons.DragHandle,
            contentDescription = "Drag to reorder",
            tint = Color.White.copy(alpha = if (isDragged) 0.9f else 0.35f),
            modifier = Modifier
                .padding(start = 2.dp, end = 6.dp)
                .size(22.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.y)
                        }
                    )
                }
        )
    }
}
