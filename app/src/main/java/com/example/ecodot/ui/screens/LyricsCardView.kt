package com.example.ecodot.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.viewmodel.LyricLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import com.example.ecodot.ui.components.MotionLiquidGlassCard

/**
 * Renders a beautiful shareable lyrics card.
 * This composable is both used for preview and for bitmap generation.
 */
@Composable
fun LyricsCardContent(
    track: Track,
    selectedLines: List<LyricLine>,
    dominantColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    MotionLiquidGlassCard(
        modifier = modifier.aspectRatio(9f / 16f),
        shape = RoundedCornerShape(28.dp),
        fluidColor = accentColor,
        specularAlpha = 0.45f,
        elevation = 16.dp
    ) {
        // 1. Blurred album art background
        if (!track.albumArtUri.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(track.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.3f)
                    .blur(40.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                contentScale = ContentScale.Crop
            )
        }

        // 2. Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.4f),
                            0.3f to dominantColor.copy(alpha = 0.25f),
                            0.75f to Color.Black.copy(alpha = 0.85f),
                            1.0f to Color.Black
                        )
                    )
                )
        )

        // 3. Small album art in top-left corner
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: small album art + song info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!track.albumArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(track.albumArtUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }

            // Middle: lyrics (centered)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                selectedLines.forEach { line ->
                    Text(
                        text = "\u201C${line.text}\u201D",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            lineHeight = 32.sp,
                            letterSpacing = (-0.3).sp,
                            fontStyle = FontStyle.Normal
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Bottom: EcoDot logo + brand name watermark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular logo icon with accent color and small white offset dot inside
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.Center)
                            .offset(x = 1.dp, y = (-1).dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EcoDot",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
            }
        }

        // 4. Subtle border glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp)
                )
        )
    }
}

/**
 * Shares lyrics lines as an image card via the Android share sheet.
 * Uses native Canvas drawing for reliable bitmap generation.
 */
suspend fun shareLyricsCard(
    context: Context,
    track: Track,
    selectedLines: List<LyricLine>,
    dominantColor: Color,
    accentColor: Color
) {
    withContext(Dispatchers.IO) {
        try {
            val width = 720
            val height = 1280
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = AndroidCanvas(bitmap)

            // ── Background ──────────────────────────────────────────────────
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Dark base
            bgPaint.color = android.graphics.Color.parseColor("#0A0A0A")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Dominant color gradient overlay
            val gradShader = android.graphics.LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    blendWithBlack(dominantColor, 0.25f),
                    blendWithBlack(dominantColor, 0.15f),
                    android.graphics.Color.parseColor("#CC000000"),
                    android.graphics.Color.parseColor("#FF000000")
                ),
                floatArrayOf(0f, 0.3f, 0.7f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
            bgPaint.shader = gradShader
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
            bgPaint.shader = null

            // ── Album art (blurred/transparent in background) ───────────────
            try {
                val artBitmap = loadBitmapFromUri(context, track.albumArtUri)
                if (artBitmap != null) {
                    val scaled = Bitmap.createScaledBitmap(artBitmap, width, height, true)
                    val artPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 60 }
                    canvas.drawBitmap(scaled, 0f, 0f, artPaint)
                    // Re-draw gradient on top
                    bgPaint.shader = gradShader
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
                    bgPaint.shader = null
                    if (!artBitmap.isRecycled) artBitmap.recycle()
                    if (!scaled.isRecycled) scaled.recycle()
                }
            } catch (_: Exception) { /* ignore art load failures */ }

            val padding = 72f

            // ── Small album art square (top-left) ───────────────────────────
            try {
                val smallArt = loadBitmapFromUri(context, track.albumArtUri)
                if (smallArt != null) {
                    val artSize = 120f
                    val scaled = Bitmap.createScaledBitmap(smallArt, artSize.toInt(), artSize.toInt(), true)
                    val artPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    val artRect = RectF(padding, padding, padding + artSize, padding + artSize)
                    canvas.save()
                    val path = android.graphics.Path().apply {
                        addRoundRect(artRect, 24f, 24f, android.graphics.Path.Direction.CW)
                    }
                    canvas.clipPath(path)
                    canvas.drawBitmap(scaled, null, artRect, artPaint)
                    canvas.restore()
                    if (!smallArt.isRecycled) smallArt.recycle()
                    if (!scaled.isRecycled) scaled.recycle()
                }
            } catch (_: Exception) {}

            // ── Track title & artist ─────────────────────────────────────────
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 36f
                typeface = Typeface.DEFAULT_BOLD
            }
            val artistPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(153, 255, 255, 255)
                textSize = 28f
                typeface = Typeface.DEFAULT
            }
            canvas.drawText(
                track.title.take(36),
                padding + 140f, padding + 44f,
                titlePaint
            )
            canvas.drawText(
                track.artist.take(40),
                padding + 140f, padding + 82f,
                artistPaint
            )

            // ── Lyrics lines ─────────────────────────────────────────────────
            val lyricPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 62f
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            }
            var lyricY = height * 0.40f
            selectedLines.forEach { line ->
                val textWithQuotes = "\u201C${line.text}\u201D"
                val words = textWithQuotes.split(" ")
                val lineChunks = mutableListOf<String>()
                var currentChunk = ""
                for (word in words) {
                    val test = if (currentChunk.isEmpty()) word else "$currentChunk $word"
                    if (lyricPaint.measureText(test) < width - padding * 2) {
                        currentChunk = test
                    } else {
                        if (currentChunk.isNotEmpty()) lineChunks.add(currentChunk)
                        currentChunk = word
                    }
                }
                if (currentChunk.isNotEmpty()) lineChunks.add(currentChunk)

                lineChunks.forEach { chunk ->
                    canvas.drawText(chunk, padding, lyricY, lyricPaint)
                    lyricY += 80f
                }
                lyricY += 20f
            }

            // ── EcoDot watermark logo + name ──────────────────────────────────
            val logoRadius = 20f
            val logoCx = padding + logoRadius
            val logoCy = height - 76f
            
            // Draw background circle for logo using the accent color
            val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = blendWithBlack(accentColor, 1.0f)
            }
            canvas.drawCircle(logoCx, logoCy, logoRadius, logoPaint)
            
            // Draw inner white dot for logo
            val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
            }
            canvas.drawCircle(logoCx + 4f, logoCy - 4f, 7f, innerPaint)
            
            // Draw brand name text next to the logo
            val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("EcoDot", padding + 56f, logoCy + 11f, watermarkPaint)

            // ── Rounded corner clip ───────────────────────────────────────────
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val resultCanvas = AndroidCanvas(resultBitmap)
            val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val clipPath = android.graphics.Path().apply {
                addRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 60f, 60f, android.graphics.Path.Direction.CW)
            }
            resultCanvas.clipPath(clipPath)
            resultCanvas.drawBitmap(bitmap, 0f, 0f, clipPaint)
            bitmap.recycle()

            // ── Save & share ─────────────────────────────────────────────────
            val cacheDir = File(context.cacheDir, "lyrics_cards").also { it.mkdirs() }
            val file = File(cacheDir, "lyric_card_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out ->
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            resultBitmap.recycle()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            withContext(Dispatchers.Main) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(
                        Intent.EXTRA_TEXT,
                        selectedLines.joinToString("\n") { it.text } +
                                "\n\n— ${track.title} by ${track.artist}"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share lyrics").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        } catch (e: Exception) {
            android.util.Log.e("LyricsCard", "Share failed", e)
        }
    }
}

private fun blendWithBlack(color: Color, opacity: Float): Int {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()
    return android.graphics.Color.argb((opacity * 255).toInt(), r, g, b)
}

private suspend fun loadBitmapFromUri(context: Context, uri: String?): Bitmap? {
    if (uri.isNullOrEmpty()) return null
    return try {
        withContext(Dispatchers.IO) {
            val loader = coil.Coil.imageLoader(context)
            val request = coil.request.ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
        }
    } catch (_: Exception) { null }
}
