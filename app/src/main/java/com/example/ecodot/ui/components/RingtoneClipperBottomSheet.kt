package com.example.ecodot.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtoneClipperBottomSheet(
    track: Track,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Layout States
    var startOffsetMs by remember { mutableStateOf(0L) }
    var isSettingRingtone by remember { mutableStateOf(false) }
    var isPreviewPlaying by remember { mutableStateOf(false) }

    val trackDurationMs = track.duration.coerceAtLeast(30_000L)
    val maxStartOffsetMs = (trackDurationMs - 30_000L).coerceAtLeast(0L)

    // Preview Player lifecycle
    var previewPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    DisposableEffect(Unit) {
        val player = ExoPlayer.Builder(context).build().apply {
            val localFile = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "${track.id}.m4a")
            if (localFile.exists()) {
                setMediaItem(MediaItem.fromUri(Uri.fromFile(localFile)))
                prepare()
            }
        }
        previewPlayer = player
        onDispose {
            player.release()
        }
    }

    // Monitor preview duration limits
    LaunchedEffect(isPreviewPlaying, startOffsetMs) {
        if (isPreviewPlaying) {
            val player = previewPlayer ?: return@LaunchedEffect
            player.seekTo(startOffsetMs)
            player.play()

            while (isPreviewPlaying) {
                delay(100)
                val currentPos = player.currentPosition
                if (currentPos >= startOffsetMs + 30_000L || currentPos >= trackDurationMs) {
                    player.pause()
                    isPreviewPlaying = false
                }
            }
        } else {
            previewPlayer?.pause()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF161617),
        contentColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.15f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCut,
                        contentDescription = null,
                        tint = EcoDotRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Set as Ringtone",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Clip a 30-second window of the song",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Track Details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(16.dp),
                border = borderStroke(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Time Selector / Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Start: ${formatMs(startOffsetMs)}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "End: ${formatMs((startOffsetMs + 30_000L).coerceAtMost(trackDurationMs))}",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                
                Slider(
                    value = startOffsetMs.toFloat(),
                    onValueChange = {
                        startOffsetMs = it.toLong()
                        isPreviewPlaying = false
                    },
                    valueRange = 0f..maxStartOffsetMs.toFloat(),
                    colors = SliderDefaults.colors(
                        activeTrackColor = EcoDotRed,
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f),
                        thumbColor = Color.White
                    ),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // Play/Pause Clip Preview Button
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(if (isPreviewPlaying) EcoDotRed else Color.White.copy(alpha = 0.05f))
                    .border(1.dp, if (isPreviewPlaying) Color.Transparent else Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { isPreviewPlaying = !isPreviewPlaying },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPreviewPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Preview Clip",
                    tint = if (isPreviewPlaying) Color.White else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = {
                    isSettingRingtone = true
                    isPreviewPlaying = false
                    viewModel.setTrackAsRingtone(track, startOffsetMs) { success, message ->
                        isSettingRingtone = false
                        if (success) {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else if (message == "PERMISSION_REQUIRED") {
                            // Redirect to system write settings permission
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                Toast.makeText(context, "Please allow WRITE_SETTINGS permission first.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isSettingRingtone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoDotRed,
                    disabledContainerColor = EcoDotRed.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                if (isSettingRingtone) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Set System Ringtone",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun borderStroke() = androidx.compose.foundation.BorderStroke(
    width = 1.dp,
    color = Color.White.copy(alpha = 0.08f)
)

private fun formatMs(ms: Long): String {
    val sec = (ms / 1000) % 60
    val min = (ms / 1000) / 60
    return String.format("%02d:%02d", min, sec)
}
