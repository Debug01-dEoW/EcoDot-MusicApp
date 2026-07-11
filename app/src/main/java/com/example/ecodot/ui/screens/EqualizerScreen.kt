package com.example.ecodot.ui.screens

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.SettingsEthernet
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit
) {
    val eqEnabled by viewModel.eqEnabled.collectAsState()
    val eqBands by viewModel.eqBands.collectAsState()
    val virtualizerStrength by viewModel.virtualizerStrength.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val context = LocalContext.current

    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val bandNames = listOf("BASS", "LOW", "MID", "UPPER", "TREBLE")

    Scaffold(
        containerColor = Color(0xFF050505),
        topBar = {
            TopAppBar(
                title = { Text("Audio Effects", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle top background glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(500f, 0f),
                            radius = 900f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ── Custom Equalizer Card ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Equalizer", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("Custom frequency tuning", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                            }
                            Switch(
                                checked = eqEnabled,
                                onCheckedChange = { viewModel.toggleEq() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentColor,
                                    uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(36.dp))

                        // Bands
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (i in 0 until 5) {
                                val level = eqBands[i] ?: 0
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // dB Label
                                    Text(
                                        text = if (level > 0) "+${level / 100}" else "${level / 100}",
                                        color = if (eqEnabled) accentColor else Color.White.copy(alpha = 0.3f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Custom Fader
                                    CanvasVerticalSlider(
                                        value = level.toFloat(),
                                        onValueChange = { viewModel.setEqBand(i, it.toInt()) },
                                        enabled = eqEnabled,
                                        accentColor = accentColor,
                                        modifier = Modifier
                                            .height(180.dp)
                                            .width(40.dp) // Touch target width
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Frequency Name
                                    Text(
                                        text = bandNames[i],
                                        color = if (eqEnabled) Color.White else Color.White.copy(alpha = 0.3f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    // Frequency Hz
                                    Text(
                                        text = bandLabels[i],
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Spatial Surround Card ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.SurroundSound,
                                    contentDescription = null,
                                    tint = if (eqEnabled) accentColor else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Spatial Surround",
                                    color = if (eqEnabled) Color.White else Color.White.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                            Text(
                                text = "${(virtualizerStrength / 10f).toInt()}%",
                                color = if (eqEnabled) accentColor else Color.White.copy(alpha = 0.3f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        CanvasHorizontalSlider(
                            value = virtualizerStrength.toFloat(),
                            onValueChange = { viewModel.setVirtualizerStrength(it.toInt()) },
                            enabled = eqEnabled,
                            accentColor = accentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── System Equalizer Button ────────────────────────────────────
                OutlinedButton(
                    onClick = {
                        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "No system equalizer found on this device", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.03f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.SettingsEthernet, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Launch Device Equalizer", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("DeepField, SoundAlive, Dirac, etc.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Custom Canvas Vertical Slider (Fader Style)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CanvasVerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = -1500f..1500f,
    enabled: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val normalizedValue = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = normalizedValue, animationSpec = tween(150), label = "progress")
    
    Canvas(
        modifier = modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectVerticalDragGestures { change, _ ->
                    val y = change.position.y.coerceIn(0f, size.height.toFloat())
                    val percent = 1f - (y / size.height.toFloat())
                    val newValue = valueRange.start + (percent * (valueRange.endInclusive - valueRange.start))
                    onValueChange(newValue)
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val y = offset.y.coerceIn(0f, size.height.toFloat())
                    val percent = 1f - (y / size.height.toFloat())
                    val newValue = valueRange.start + (percent * (valueRange.endInclusive - valueRange.start))
                    onValueChange(newValue)
                }
            }
    ) {
        val trackWidth = 6.dp.toPx()
        val cornerRadius = CornerRadius(trackWidth / 2, trackWidth / 2)
        val trackX = center.x - trackWidth / 2
        
        // Background inactive track
        drawRoundRect(
            color = Color.White.copy(alpha = 0.05f),
            topLeft = Offset(trackX, 0f),
            size = Size(trackWidth, size.height),
            cornerRadius = cornerRadius
        )
        
        // Zero dB Center line
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(center.x - 12.dp.toPx(), size.height / 2),
            end = Offset(center.x + 12.dp.toPx(), size.height / 2),
            strokeWidth = 1.dp.toPx()
        )
        
        // Active filled track
        val activeHeight = size.height * animatedProgress
        val startY = size.height - activeHeight
        
        if (enabled) {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(accentColor, accentColor.copy(alpha = 0.5f)),
                    startY = startY,
                    endY = size.height
                ),
                topLeft = Offset(trackX, startY),
                size = Size(trackWidth, activeHeight),
                cornerRadius = cornerRadius
            )
        } else {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.2f),
                topLeft = Offset(trackX, startY),
                size = Size(trackWidth, activeHeight),
                cornerRadius = cornerRadius
            )
        }
        
        // Thumb
        val thumbRadius = 12.dp.toPx()
        val thumbY = startY.coerceIn(thumbRadius, size.height - thumbRadius)
        
        if (enabled) {
            // Glow
            drawCircle(
                color = accentColor.copy(alpha = 0.25f),
                radius = thumbRadius * 1.8f,
                center = Offset(center.x, thumbY)
            )
            // Thumb center
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(center.x, thumbY)
            )
            drawCircle(
                color = accentColor,
                radius = thumbRadius * 0.4f,
                center = Offset(center.x, thumbY)
            )
        } else {
            drawCircle(
                color = Color.DarkGray,
                radius = thumbRadius,
                center = Offset(center.x, thumbY)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Custom Canvas Horizontal Slider
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CanvasHorizontalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1000f,
    enabled: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val normalizedValue = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = normalizedValue, animationSpec = tween(150), label = "progress")
    
    Canvas(
        modifier = modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures { change, _ ->
                    val x = change.position.x.coerceIn(0f, size.width.toFloat())
                    val percent = x / size.width.toFloat()
                    val newValue = valueRange.start + (percent * (valueRange.endInclusive - valueRange.start))
                    onValueChange(newValue)
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val x = offset.x.coerceIn(0f, size.width.toFloat())
                    val percent = x / size.width.toFloat()
                    val newValue = valueRange.start + (percent * (valueRange.endInclusive - valueRange.start))
                    onValueChange(newValue)
                }
            }
    ) {
        val trackHeight = 6.dp.toPx()
        val cornerRadius = CornerRadius(trackHeight / 2, trackHeight / 2)
        val trackY = center.y - trackHeight / 2
        
        // Inactive track
        drawRoundRect(
            color = Color.White.copy(alpha = 0.05f),
            topLeft = Offset(0f, trackY),
            size = Size(size.width, trackHeight),
            cornerRadius = cornerRadius
        )
        
        val activeWidth = size.width * animatedProgress
        
        if (enabled) {
            // Active track
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.5f), accentColor),
                    startX = 0f,
                    endX = activeWidth
                ),
                topLeft = Offset(0f, trackY),
                size = Size(activeWidth, trackHeight),
                cornerRadius = cornerRadius
            )
        } else {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.2f),
                topLeft = Offset(0f, trackY),
                size = Size(activeWidth, trackHeight),
                cornerRadius = cornerRadius
            )
        }
        
        // Thumb
        val thumbRadius = 12.dp.toPx()
        val thumbX = activeWidth.coerceIn(thumbRadius, size.width - thumbRadius)
        
        if (enabled) {
            // Glow
            drawCircle(
                color = accentColor.copy(alpha = 0.25f),
                radius = thumbRadius * 1.8f,
                center = Offset(thumbX, center.y)
            )
            // Inner
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(thumbX, center.y)
            )
            drawCircle(
                color = accentColor,
                radius = thumbRadius * 0.4f,
                center = Offset(thumbX, center.y)
            )
        } else {
            drawCircle(
                color = Color.DarkGray,
                radius = thumbRadius,
                center = Offset(thumbX, center.y)
            )
        }
    }
}
