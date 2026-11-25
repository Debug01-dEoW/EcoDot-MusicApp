package com.example.ecodot.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.ecodot.recognition.Recognition
import com.example.ecodot.recognition.SongRecognizer
import com.example.ecodot.ui.theme.EcoDotRed
import kotlinx.coroutines.launch

private val OrbSize = 220.dp
private val CoreSize = 100.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongRecognitionBottomSheet(
    onFind: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun hasMic() = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    var micGranted by remember { mutableStateOf(hasMic()) }
    var attempt by remember { mutableIntStateOf(0) }
    var recognition by remember { mutableStateOf<Recognition>(Recognition.Listening) }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted
    }

    LaunchedEffect(micGranted, attempt) {
        if (!micGranted) return@LaunchedEffect
        recognition = Recognition.Listening
        recognition = SongRecognizer.recognize(
            onIdentifying = { recognition = Recognition.Identifying }
        )
    }

    fun close(action: (() -> Unit)? = null) {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            action?.invoke()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF101118),
        contentColor = Color.White,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Header Row: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = EcoDotRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Song Recognition",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = { close() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Body Content based on recognition state
            when {
                !micGranted -> {
                    RecognitionOrb(
                        icon = Icons.Rounded.MicOff,
                        muted = true
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Microphone Permission Required",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "EcoDot needs microphone access to listen and identify songs playing nearby.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoDotRed),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grant Permission", fontWeight = FontWeight.Bold)
                    }
                }

                recognition is Recognition.Found -> {
                    val found = recognition as Recognition.Found
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E202E))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!found.coverArt.isNullOrEmpty()) {
                            AsyncImage(
                                model = found.coverArt,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = EcoDotRed,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = found.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = found.artist,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { attempt++ },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.2f)))
                            )
                        ) {
                            Icon(Icons.Rounded.Replay, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Listen Again", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                val query = "${found.title} ${found.artist}".trim()
                                close { onFind(query) }
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoDotRed)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Play & Search", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                recognition is Recognition.NoMatch || recognition is Recognition.Failed -> {
                    RecognitionOrb(
                        icon = Icons.Rounded.MusicOff,
                        muted = true
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = if (recognition is Recognition.NoMatch) "No Song Identified" else "Recognition Interrupted",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try holding your phone closer to the speakers and make sure there is minimal background noise.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { attempt++ },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoDotRed),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.75f).height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try Again", fontWeight = FontWeight.Bold)
                    }
                }

                else -> {
                    val isListening = recognition is Recognition.Listening
                    RecognitionOrb(
                        icon = if (isListening) Icons.Rounded.GraphicEq else Icons.Rounded.MusicNote,
                        listening = isListening
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (isListening) "Listening for music..." else "Identifying song...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isListening) "Make sure music is playing nearby" else "Matching audio with Shazam...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun RecognitionOrb(
    icon: ImageVector,
    muted: Boolean = false,
    listening: Boolean = false
) {
    Box(
        modifier = Modifier.size(if (muted) 140.dp else OrbSize),
        contentAlignment = Alignment.Center
    ) {
        if (listening) {
            val progress by rememberInfiniteTransition(label = "rings").animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
                label = "ring_anim"
            )

            Canvas(modifier = Modifier.size(OrbSize)) {
                val stroke = 2.5.dp.toPx()
                val from = CoreSize.toPx() / 2
                val to = size.minDimension / 2 - stroke / 2
                for (k in 0..1) {
                    val p = (progress + k / 2f) % 1f
                    drawCircle(
                        color = EcoDotRed,
                        radius = from + (to - from) * p,
                        alpha = 0.6f * (1f - p),
                        style = Stroke(stroke)
                    )
                }
            }
        }

        val orbBgBrush = remember(muted) {
            if (muted) Brush.linearGradient(listOf(Color(0xFF1E202E), Color(0xFF1E202E)))
            else Brush.radialGradient(listOf(EcoDotRed, Color(0xFFB50711)))
        }

        Box(
            modifier = Modifier
                .size(CoreSize)
                .clip(CircleShape)
                .background(orbBgBrush)
                .border(
                    width = 1.5.dp,
                    color = if (muted) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.35f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (muted) Color.White.copy(alpha = 0.5f) else Color.White,
                modifier = Modifier.size(46.dp)
            )
        }
    }
}
