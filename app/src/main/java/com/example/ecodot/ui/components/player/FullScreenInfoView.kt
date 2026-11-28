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
    val streamTelemetry by viewModel.activeStreamTelemetry.collectAsState()
    val activeStreamLabel by viewModel.activeStreamLabel.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val playbackSource by viewModel.playbackSource.collectAsState()
    val isVideoMode by viewModel.isVideoMode.collectAsState()

    LaunchedEffect(currentTrack?.id) {
        val track = currentTrack ?: return@LaunchedEffect
        if (track.isYouTube) {
            viewModel.ensureVideoInfoLoaded(track.id)
        }
    }

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
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Song Info",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    if (currentTrack?.isYouTube == true) {
                        IconButton(
                            onClick = { viewModel.ensureVideoInfoLoaded(currentTrack.id, forceRefresh = true) },
                            enabled = !isVideoInfoLoading
                        ) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "Refresh Song Info",
                                tint = Color.White.copy(alpha = if (isVideoInfoLoading) 0.4f else 0.85f)
                            )
                        }
                    }
                }
            }
        ) { padding ->
            currentTrack?.let { track ->
                val info = videoInfo?.takeIf { it.videoId == track.id }
                val resolverMeta = remember(track.id, streamTelemetry) {
                    com.example.ecodot.data.remote.YouTubeStreamResolver.resolvedAudioTelemetry[track.id]
                }

                // Merge live ExoPlayer telemetry with stream metadata so Song Info reflects active playback.
                val liveBitrateBps = streamTelemetry.bitrateBps.takeIf { it > 0 }
                    ?: resolverMeta?.bitrateBps?.takeIf { it > 0 }
                    ?: 0
                val displayBitrate = PlayerStyleMath.formatBitrateKbpsLabel(liveBitrateBps)
                    ?: info?.bitrate?.takeIf { it.isNotBlank() && it != "0" && it != "—" }
                    ?: "—"

                val liveCodec = streamTelemetry.codec?.takeIf { it.isNotBlank() }
                    ?: resolverMeta?.codec?.takeIf { it.isNotBlank() }
                    ?: info?.codec?.takeIf { it.isNotBlank() && it != "—" }
                    ?: if (!track.isYouTube) track.path.substringAfterLast('.', "").lowercase().ifBlank { "local" } else "—"

                val liveMimeType = (streamTelemetry.mimeType ?: resolverMeta?.mimeType ?: info?.mimeType)
                    ?.substringBefore(";")
                    ?.trim()
                    ?.takeIf { it.isNotBlank() && it != "—" }
                    ?: if (!track.isYouTube) "audio/${track.path.substringAfterLast('.', "file").lowercase()}" else "—"

                val liveSampleRateHz = streamTelemetry.sampleRateHz.takeIf { it > 0 }
                    ?: resolverMeta?.sampleRateHz?.takeIf { it > 0 }
                    ?: 0
                val displaySampleRate = if (liveSampleRateHz > 0) {
                    val khz = liveSampleRateHz / 1000.0
                    if (liveSampleRateHz % 1000 == 0) "${liveSampleRateHz / 1000} kHz"
                    else String.format(java.util.Locale.US, "%.1f kHz", khz)
                } else {
                    info?.sampleRate?.takeIf { it.isNotBlank() } ?: "—"
                }

                val displayItag = streamTelemetry.itag?.takeIf { it.isNotBlank() && it != "0" }
                    ?: resolverMeta?.itag?.takeIf { it > 0 }?.toString()
                    ?: info?.itag?.takeIf { it.isNotBlank() && it != "0" && it != "—" }
                    ?: if (!track.isYouTube) "Local" else "—"

                val isTrackLiked by viewModel.isTrackLiked(track.id).collectAsState(initial = false)
                val viewsDisplay = when {
                    !track.isYouTube -> "Local File"
                    info != null && info.viewCountRaw > 0L -> formatNumberShort(info.viewCountRaw.toString())
                    info != null && info.viewCount.isNotBlank() && info.viewCount != "0" && info.viewCount != "—" -> info.viewCount
                    isVideoInfoLoading -> "…"
                    else -> "—"
                }

                val likesDisplay = when {
                    !track.isYouTube -> if (isTrackLiked) "Liked" else "Library"
                    info != null && info.likeCountRaw > 0L -> formatNumberShort(info.likeCountRaw.toString())
                    info != null && info.likeCount.isNotBlank() && info.likeCount != "0" && info.likeCount != "—" -> info.likeCount
                    isVideoInfoLoading -> "…"
                    else -> "—"
                }

                val durationDisplay = formatTrackDuration(track.duration)
                    ?: info?.durationSeconds?.takeIf { it.isNotBlank() && it != "—" }
                    ?: "—"

                val sourceDisplay = when {
                    !track.isYouTube -> "Offline Storage"
                    isVideoMode -> "YouTube Video"
                    else -> "YouTube Music"
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(14.dp))
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
                            color = Color.White.copy(0.65f),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (!activeStreamLabel.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.14f),
                            modifier = Modifier.border(0.5.dp, accentColor.copy(alpha = 0.35f), CircleShape)
                        ) {
                            Text(
                                text = activeStreamLabel ?: "",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 1. REAL SYNCED STATS ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(icon = Icons.Rounded.PlayArrow, value = viewsDisplay, label = if (track.isYouTube) "Views" else "Source")
                        StatItem(icon = Icons.Rounded.Favorite, value = likesDisplay, label = if (track.isYouTube) "Likes" else "Status")
                        StatItem(icon = Icons.Rounded.Schedule, value = durationDisplay, label = "Duration")
                    }

                    if (isVideoInfoLoading && info == null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth(0.45f)
                                .height(2.dp)
                                .clip(CircleShape),
                            color = accentColor,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // 2. LIVE AUDIO TECH SPECS GRID
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TechBadge(label = "CODEC", value = liveCodec, color = accentColor, modifier = Modifier.weight(1f))
                        TechBadge(label = "BITRATE", value = displayBitrate, color = Color(0xFF00E676), modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TechBadge(label = "MIME TYPE", value = liveMimeType, color = Color(0xFF29B6F6), modifier = Modifier.weight(1f))
                        TechBadge(label = "SAMPLE RATE", value = displaySampleRate, color = Color(0xFFCE93D8), modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TechBadge(label = "ITAG", value = displayItag, color = Color(0xFFFFB74D), modifier = Modifier.weight(1f))
                        TechBadge(label = "SOURCE", value = sourceDisplay, color = Color(0xFF80CBC4), modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // 3. SYNCED TRACK DETAILS CARD
                    val detailRows = buildList {
                        val channel = info?.channelName?.takeIf { it.isNotBlank() } ?: track.artist
                        if (channel.isNotBlank()) add("Channel" to channel)
                        val albumName = track.album.takeIf { it.isNotBlank() && !it.equals("Unknown Album", true) }
                        if (albumName != null) add("Album" to albumName)
                        if (playbackSource.isNotBlank()) add("Queue Origin" to playbackSource)
                        add("Media ID" to track.id)
                    }
                    if (detailRows.isNotEmpty()) {
                        Text(
                            text = "TRACK DETAILS",
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
                            detailRows.forEach { (key, value) ->
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
                                        modifier = Modifier.weight(1.8f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    // 4. PARSED CREDITS & REAL DESCRIPTION
                    val descText = info?.description?.takeIf { it.isNotBlank() } ?: ""
                    val parsed = remember(descText) {
                        if (descText.isNotEmpty()) parseYouTubeDescription(descText) else emptyList<Pair<String, String>>() to ""
                    }
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
                        Spacer(modifier = Modifier.height(28.dp))
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
                                color = Color.White.copy(0.65f),
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    } else if (!isVideoInfoLoading && track.isYouTube) {
                        Text(
                            text = "No description available from stream metadata.",
                            color = Color.White.copy(0.4f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }
}

private fun formatTrackDuration(durationMs: Long): String? {
    if (durationMs <= 0L) return null
    val totalSec = durationMs / 1000L
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return String.format(java.util.Locale.US, "%d:%02d", mins, secs)
}
