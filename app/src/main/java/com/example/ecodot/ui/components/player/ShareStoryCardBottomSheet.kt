package com.example.ecodot.ui.components.player
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Close
import com.example.ecodot.ui.theme.FormatQuote
import com.example.ecodot.ui.theme.Share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.viewmodel.LyricLine
import java.io.File
import java.io.FileOutputStream

enum class StoryCardTheme {
    AMBIENT_MESH,
    OLED_DARK,
    FROSTED_GLASS,
    GOLDEN_SUNSET
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareStoryCardBottomSheet(
    track: Track,
    syncedLyrics: List<LyricLine> = emptyList(),
    dominantColor: Color = Color(0xFF2C1835),
    accentColor: Color = Color(0xFF9E4784),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf(StoryCardTheme.AMBIENT_MESH) }
    var selectedLyricIndex by remember { mutableIntStateOf(0) }
    var showLyrics by remember { mutableStateOf(syncedLyrics.isNotEmpty()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF101116),
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Share Song Story",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = SimpIcons.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Story Card Preview ───────────────────────────────────────────
            val backgroundBrush = remember(selectedTheme, dominantColor, accentColor) {
                when (selectedTheme) {
                    StoryCardTheme.AMBIENT_MESH -> Brush.radialGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.9f),
                            accentColor.copy(alpha = 0.7f),
                            Color(0xFF070709)
                        ),
                        center = Offset(300f, 300f),
                        radius = 800f
                    )
                    StoryCardTheme.OLED_DARK -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF14151E), Color(0xFF070709))
                    )
                    StoryCardTheme.FROSTED_GLASS -> Brush.linearGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.65f),
                            Color(0xFF1E202C).copy(alpha = 0.85f),
                            Color(0xFF090A0E)
                        )
                    )
                    StoryCardTheme.GOLDEN_SUNSET -> Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF512F),
                            Color(0xFFDD2476),
                            Color(0xFF150D2A)
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(370.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(backgroundBrush)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Brand
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(EcoDotRed, CircleShape)
                        )
                        Text(
                            text = "ECODOT MUSIC",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }

                    // Center Artwork or Lyrics
                    if (showLyrics && syncedLyrics.isNotEmpty()) {
                        val currentLyric = syncedLyrics.getOrNull(selectedLyricIndex)?.text ?: "Listening to ${track.title}"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = SimpIcons.FormatQuote,
                                contentDescription = null,
                                tint = EcoDotRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "“$currentLyric”",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .shadow(14.dp, RoundedCornerShape(14.dp), spotColor = Color.Black.copy(alpha = 0.6f))
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E202B))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(track.albumArtUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Track Details Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = track.artist,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Theme Selector ───────────────────────────────────────────────
            Text(
                text = "Card Theme",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StoryCardTheme.values().forEach { theme ->
                    val isSelected = selectedTheme == theme
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) EcoDotRed else Color(0xFF1E202B))
                            .clickable { selectedTheme = theme },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (theme) {
                                StoryCardTheme.AMBIENT_MESH -> "Ambient"
                                StoryCardTheme.OLED_DARK -> "Dark"
                                StoryCardTheme.FROSTED_GLASS -> "Glass"
                                StoryCardTheme.GOLDEN_SUNSET -> "Sunset"
                            },
                            color = if (isSelected) Color(0xFF121214) else Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (syncedLyrics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show Lyrics Snippet",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = showLyrics,
                        onCheckedChange = { showLyrics = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF121214),
                            checkedTrackColor = EcoDotRed
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Share Action Button ──────────────────────────────────────────
            Button(
                onClick = {
                    shareSongText(context, track)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoDotRed),
                shape = RoundedCornerShape(25.dp)
            ) {
                Icon(
                    imageVector = SimpIcons.Share,
                    contentDescription = null,
                    tint = Color(0xFF121214),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Story",
                    color = Color(0xFF121214),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

private fun shareSongText(context: Context, track: Track) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Listening to ${track.title} on EcoDot"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "🎶 I'm listening to \"${track.title}\" by ${track.artist} on EcoDot Music!\nhttps://music.youtube.com/watch?v=${track.id}"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share track", Toast.LENGTH_SHORT).show()
    }
}
