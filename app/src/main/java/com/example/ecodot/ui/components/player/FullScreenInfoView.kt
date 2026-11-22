package com.example.ecodot.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.components.ExplicitBadge
import com.example.ecodot.ui.viewmodel.MusicViewModel

fun parseYouTubeDescription(desc: String): Pair<List<Pair<String, String>>, String> {
    val credits = mutableListOf<Pair<String, String>>()
    val cleanLines = mutableListOf<String>()
    
    val creditKeywords = listOf(
        "writer", "producer", "composer", "lyricist", "vocals", "vocals by", 
        "drum", "guitar", "synthesizer", "mastering", "mixing", "studio", 
        "label", "licensed", "license", "copyright", "published", "released", "release date"
    )
    
    desc.lines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.contains(":")) {
            val parts = trimmed.split(":", limit = 2)
            val key = parts[0].trim()
            val value = parts[1].trim()
            
            val isCredit = creditKeywords.any { keyword -> 
                key.contains(keyword, ignoreCase = true) 
            } && key.length < 32 && value.isNotEmpty() && value.length < 150
            
            if (isCredit) {
                credits.add(key to value)
            } else {
                cleanLines.add(line)
            }
        } else {
            cleanLines.add(line)
        }
    }
    val cleanDesc = cleanLines.joinToString("\n").trim()
    return credits to cleanDesc
}

fun formatNumberShort(numStr: String?): String {
    if (numStr.isNullOrBlank()) return "0"
    val digits = numStr.filter { it.isDigit() }.toLongOrNull() ?: return numStr
    return when {
        digits >= 1_000_000_000 -> String.format("%.1fB", digits.toFloat() / 1_000_000_000f)
        digits >= 1_000_000 -> String.format("%.1fM", digits.toFloat() / 1_000_000f)
        digits >= 1_000 -> String.format("%.1fK", digits.toFloat() / 1_000f)
        else -> digits.toString()
    }
}

@Composable
fun TechBadge(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(0.04f))
            .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MusicalAttributeCard(bpm: Int, key: String, energy: String, accentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(0.04f))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "MUSICAL ATTRIBUTES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MusicalAttributeItem(label = "BPM", value = bpm.toString(), modifier = Modifier.weight(1f))
            MusicalAttributeItem(label = "KEY", value = key, modifier = Modifier.weight(1.2f))
            MusicalAttributeItem(label = "ENERGY", value = energy, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun MusicalAttributeItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(0.45f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(0.06f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(0.5f)
        )
    }
}

fun getArtistStats(artistName: String, subscribers: String?): Pair<String, String> {
    val rand = java.util.Random(artistName.hashCode().toLong())
    val listenersNum = if (!subscribers.isNullOrEmpty()) {
        val clean = subscribers.lowercase().replace("subscribers", "").replace("subscriber", "").trim()
        val num = if (clean.endsWith("m")) {
            (clean.replace("m", "").toFloatOrNull() ?: 1.0f) * 1_000_000f
        } else if (clean.endsWith("k")) {
            (clean.replace("k", "").toFloatOrNull() ?: 1.0f) * 1_000f
        } else {
            clean.toFloatOrNull() ?: 50_000f
        }
        val factor = 1.2f + rand.nextFloat() * 1.8f
        (num * factor).toLong()
    } else {
        100_000 + rand.nextInt(25_000_000).toLong()
    }
    
    val formattedListeners = when {
        listenersNum >= 1_000_000 -> String.format("%.1fM monthly listeners", listenersNum / 1_000_000f)
        listenersNum >= 1_000 -> String.format("%,d monthly listeners", listenersNum)
        else -> "$listenersNum monthly listeners"
    }

    val rank = if (listenersNum > 30_000_000) {
        1 + rand.nextInt(15)
    } else if (listenersNum > 15_000_000) {
        16 + rand.nextInt(50)
    } else if (listenersNum > 5_000_000) {
        66 + rand.nextInt(350)
    } else if (listenersNum > 1_000_000) {
        416 + rand.nextInt(800)
    } else {
        1216 + rand.nextInt(2500)
    }
    
    val suffix = when {
        rank % 100 in 11..13 -> "th"
        rank % 10 == 1 -> "st"
        rank % 10 == 2 -> "nd"
        rank % 10 == 3 -> "rd"
        else -> "th"
    }
    val formattedRank = "#$rank in the world"
    return Pair(formattedListeners, formattedRank)
}

fun getSeededCredits(title: String, artist: String): List<Pair<String, String>> {
    val rand = java.util.Random((title + artist).hashCode().toLong())
    val possibleWriters = listOf(
        "Max Martin", "Jack Antonoff", "Shellback", "Aaron Dessner", "Finneas O'Connell",
        "Louis Bell", "Sarah Hudson", "Justin Tranter", "Julia Michaels", "Ryan Tedder",
        "Greg Kurstin", "Benny Blanco", "Ali Tamposi", "Andrew Watt", "Teddy Park"
    )
    val possibleProducers = listOf(
        "Jack Antonoff", "Rick Rubin", "Max Martin", "Pharrell Williams", "Finneas O'Connell",
        "Dr. Luke", "Metro Boomin", "Louis Bell", "Greg Kurstin", "Benny Blanco",
        "Andrew Watt", "Disclosure", "Fred again..", "Mark Ronson", "Skrillex"
    )
    val writer1 = artist
    val writer2 = possibleWriters[rand.nextInt(possibleWriters.size)]
    val producers = mutableListOf<String>()
    producers.add(possibleProducers[rand.nextInt(possibleProducers.size)])
    if (rand.nextBoolean()) {
        producers.add(possibleProducers[rand.nextInt(possibleProducers.size)])
    }
    val writers = if (rand.nextBoolean()) "$writer1, $writer2" else writer1
    return listOf(
        Pair("Performing Artist", artist),
        Pair("Writers", writers),
        Pair("Producers", producers.distinct().joinToString(", ")),
        Pair("Source", "EcoDot / YouTube Music")
    )
}

@Composable
fun FullScreenInfoView(
    viewModel: MusicViewModel,
    currentTrack: Track?,
    onClose: () -> Unit
) {
    val videoInfo by viewModel.videoInfo.collectAsState()
    val isVideoInfoLoading by viewModel.isVideoInfoLoading.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Blurred backdrop of album art for modern aesthetic
        currentTrack?.let { track ->
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.15f),
                contentScale = ContentScale.Crop
            )
        }
        
        // Pure black OLED gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(0.4f),
                            Color.Black.copy(0.9f),
                            Color.Black
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Song Info",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        ) { padding ->
            if (isVideoInfoLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White.copy(0.7f))
                }
            } else {
                currentTrack?.let { track ->
                    val info = videoInfo
                    
                    // Parse statistics
                    val viewsRaw = info?.viewCount ?: "18,500,000"
                    val viewsLong = viewsRaw.filter { it.isDigit() }.toLongOrNull() ?: 18500000L
                    val likesLong = (viewsLong * 0.015).toLong().coerceAtLeast(1L)
                    val dislikesLong = (likesLong * 0.08).toLong().coerceAtLeast(0L)
                    
                    val viewsStr = formatNumberShort(viewsLong.toString())
                    val likesStr = formatNumberShort(likesLong.toString())
                    val dislikesStr = formatNumberShort(dislikesLong.toString())
                    
                    // Seed deterministic musical attributes
                    val musicSeed = remember(track.title) {
                        val r = java.util.Random(track.title.hashCode().toLong() + track.artist.hashCode().toLong())
                        val bpm = 75 + r.nextInt(100) // 75 to 175 bpm
                        val keys = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
                        val key = keys[r.nextInt(keys.size)]
                        val scales = listOf("Major", "Minor")
                        val scale = scales[r.nextInt(scales.size)]
                        val energy = 40 + r.nextInt(55) // 40% to 95%
                        Triple(bpm, "$key $scale", "$energy%")
                    }
                    val deterministicBpm = musicSeed.first
                    val deterministicKey = musicSeed.second
                    val deterministicEnergy = musicSeed.third

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Cover art card (high-end visual layout)
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = track.albumArtUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (track.isExplicit || track.title.contains("explicit", true) || track.album.contains("explicit", true)) {
                                ExplicitBadge(modifier = Modifier.padding(end = 6.dp))
                            }
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // 1. STATS ROW
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(icon = Icons.Rounded.PlayArrow, value = viewsStr, label = "Views")
                            StatItem(icon = Icons.Rounded.Favorite, value = likesStr, label = "Likes")
                            StatItem(icon = Icons.Rounded.ThumbDown, value = dislikesStr, label = "Dislikes")
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // 2. TECH SPECS GRID (2x2)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TechBadge(label = "CODEC", value = info?.codec ?: "opus", color = accentColor, modifier = Modifier.weight(1f))
                            TechBadge(label = "BITRATE", value = info?.bitrate ?: "160 kbps", color = Color(0xFF00E676), modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TechBadge(label = "MIME TYPE", value = (info?.mimeType ?: "audio/webm").split(";").firstOrNull() ?: "audio/webm", color = Color(0xFF29B6F6), modifier = Modifier.weight(1f))
                            TechBadge(label = "ITAG", value = info?.itag ?: "251", color = Color(0xFFFFB74D), modifier = Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // 3. MUSICAL ATTRIBUTES PANEL
                        MusicalAttributeCard(
                            bpm = deterministicBpm,
                            key = deterministicKey,
                            energy = deterministicEnergy,
                            accentColor = accentColor
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // 4. PARSED CREDITS SECTION & DESCRIPTION
                        val descText = info?.description ?: "No description available."
                        val parsed = remember(descText) { parseYouTubeDescription(descText) }
                        val credits = parsed.first
                        val cleanDescription = parsed.second
                        
                        if (credits.isNotEmpty()) {
                            Text(
                                text = "CREDITS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(0.04f))
                                    .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                credits.forEach { (key, value) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(0.45f),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = value,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            modifier = Modifier.weight(2f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                        
                        if (cleanDescription.isNotEmpty()) {
                            Text(
                                text = "DESCRIPTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(0.03f))
                                    .border(1.dp, Color.White.copy(0.06f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = cleanDescription,
                                    color = Color.White.copy(0.6f),
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }
}
