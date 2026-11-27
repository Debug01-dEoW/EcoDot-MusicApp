package com.example.ecodot.ui.components.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
                Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Drag handle — receives touch on the handle only
        Icon(
            imageVector = Icons.Rounded.DragHandle,
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
