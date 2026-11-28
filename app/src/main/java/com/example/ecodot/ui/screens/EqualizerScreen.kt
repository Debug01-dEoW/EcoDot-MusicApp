package com.example.ecodot.ui.screens

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.domain.model.DEFAULT_EQ_PRESETS
import com.example.ecodot.domain.model.EqPreset
import com.example.ecodot.domain.model.formatEqFrequency
import com.example.ecodot.ui.components.IOSSwitch
import com.example.ecodot.ui.components.LiquidGlassCard
import com.example.ecodot.ui.components.LocalLiquidGlassEnabled
import com.example.ecodot.ui.components.liquidGlass
import com.example.ecodot.ui.theme.Add
import com.example.ecodot.ui.theme.ArrowBack
import com.example.ecodot.ui.theme.ChevronRight
import com.example.ecodot.ui.theme.Close
import com.example.ecodot.ui.theme.GraphicEq
import com.example.ecodot.ui.theme.RestartAlt
import com.example.ecodot.ui.theme.SettingsEthernet
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.SurroundSound
import com.example.ecodot.ui.theme.VolumeUp
import com.example.ecodot.ui.viewmodel.MusicViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit
) {
    val eqEnabled by viewModel.eqEnabled.collectAsState()
    val eqBands by viewModel.eqBands.collectAsState()
    val deviceEqSpec by viewModel.deviceEqSpec.collectAsState()
    val customPresets by viewModel.customEqPresets.collectAsState()
    val virtualizerStrength by viewModel.virtualizerStrength.collectAsState()
    val bassBoostStrength by viewModel.bassBoostStrength.collectAsState()
    val normalizeVolume by viewModel.normalizeVolume.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val selectedPreset by viewModel.selectedEqPreset.collectAsState()
    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val context = LocalContext.current

    val eqHazeState = remember { HazeState() }
    val scrollState = rememberScrollState()
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var presetToDelete by remember { mutableStateOf<EqPreset?>(null) }

    val bandCount = deviceEqSpec.bandCount.coerceAtLeast(5)
    val bandFreqLabels = remember(deviceEqSpec) {
        (0 until bandCount).map { idx ->
            val hz = deviceEqSpec.centerFreqsHz.getOrElse(idx) {
                listOf(60, 230, 910, 3600, 14000).getOrElse(idx) { 1000 }
            }
            formatEqFrequency(hz)
        }
    }
    val minMb = deviceEqSpec.minLevelMb
    val maxMb = deviceEqSpec.maxLevelMb
    val activeAccent = if (eqEnabled) accentColor else Color.White.copy(alpha = 0.35f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080D))
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (eqEnabled) 0.22f else 0.08f),
                            Color(0xFF0D111D).copy(alpha = 0.55f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.14f),
                        radius = size.width * 1.15f
                    )
                )
            }
    ) {
        // Scrollable body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(eqHazeState)
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 74.dp, bottom = 28.dp)
        ) {
            // ── 1. Live Frequency Response Curve Card ────────────────────────
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                tintColor = Color(0xFF131622),
                specularAlpha = 0.36f,
                hazeState = if (liquidGlassEnabled) eqHazeState else null
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FREQUENCY CURVE",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedPreset,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = if (eqEnabled) accentColor.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                1.dp,
                                if (eqEnabled) accentColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (eqEnabled) accentColor else Color.White.copy(alpha = 0.35f))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (eqEnabled) "$bandCount-BAND ACTIVE" else "BYPASS",
                                    color = if (eqEnabled) accentColor else Color.White.copy(alpha = 0.55f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val bandValues = (0 until bandCount).map { eqBands[it] ?: 0 }
                    EqFrequencyCurveCanvas(
                        bandLevels = bandValues,
                        minLevelMb = minMb,
                        maxLevelMb = maxMb,
                        enabled = eqEnabled,
                        accentColor = accentColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(116.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${minMb / 100} dB",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "0 dB Reference",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "+${maxMb / 100} dB",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── 2. Presets Bar (Save Custom + Saved + Built-in) ──────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SOUND PROFILES",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "${customPresets.size} custom · ${DEFAULT_EQ_PRESETS.size} built-in",
                    color = Color.White.copy(alpha = 0.38f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save Preset action pill
                item(key = "save_preset_btn") {
                    Surface(
                        onClick = {
                            presetNameInput = ""
                            showSavePresetDialog = true
                        },
                        shape = RoundedCornerShape(50),
                        color = accentColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = SimpIcons.Add,
                                contentDescription = "Save Preset",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save Preset",
                                color = accentColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Custom chip when manually altered
                if (selectedPreset == "Custom") {
                    item(key = "preset_custom_active") {
                        EqPresetChip(
                            title = "Custom",
                            isSelected = true,
                            isCustom = false,
                            accentColor = accentColor,
                            onClick = {},
                            onDeleteClick = null
                        )
                    }
                }

                // User saved custom presets
                items(customPresets, key = { "custom_${it.name}" }) { preset ->
                    EqPresetChip(
                        title = preset.name,
                        isSelected = selectedPreset == preset.name,
                        isCustom = true,
                        accentColor = accentColor,
                        onClick = {
                            if (!eqEnabled) viewModel.toggleEq()
                            viewModel.applyEqPreset(preset)
                        },
                        onDeleteClick = { presetToDelete = preset }
                    )
                }

                // Built-in presets
                items(DEFAULT_EQ_PRESETS, key = { "builtin_${it.name}" }) { preset ->
                    EqPresetChip(
                        title = preset.name,
                        isSelected = selectedPreset == preset.name,
                        isCustom = false,
                        accentColor = accentColor,
                        onClick = {
                            if (!eqEnabled) viewModel.toggleEq()
                            viewModel.applyEqPreset(preset)
                        },
                        onDeleteClick = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── 3. Multi-Band Graphic Equalizer Sliders ──────────────────────
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                tintColor = Color(0xFF131622),
                specularAlpha = 0.34f,
                hazeState = if (liquidGlassEnabled) eqHazeState else null
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BANDS TUNER",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Surface(
                            onClick = {
                                viewModel.resetEqToFlat()
                                Toast.makeText(context, "Bands & FX reset to flat", Toast.LENGTH_SHORT).show()
                            },
                            color = Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.14f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = SimpIcons.RestartAlt,
                                    contentDescription = "Reset Flat",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Flatten",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    val useHorizontalBands = bandCount > 6
                    val bandsContent: @Composable () -> Unit = {
                        for (i in 0 until bandCount) {
                            val level = eqBands[i] ?: 0
                            val dbValue = level / 100f
                            val dbLabel = if (dbValue > 0.05f) {
                                String.format(java.util.Locale.US, "+%.1f", dbValue)
                            } else {
                                String.format(java.util.Locale.US, "%.1f", dbValue)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = if (useHorizontalBands) {
                                    Modifier.width(56.dp)
                                } else {
                                    Modifier.fillMaxWidth()
                                }
                            ) {
                                Surface(
                                    color = if (eqEnabled && level != 0) accentColor.copy(alpha = 0.14f)
                                    else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${dbLabel}dB",
                                        color = if (eqEnabled && level != 0) accentColor
                                        else Color.White.copy(alpha = if (eqEnabled) 0.7f else 0.3f),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                CanvasVerticalSlider(
                                    value = level.toFloat(),
                                    onValueChange = { viewModel.setEqBand(i, it.roundToInt()) },
                                    enabled = eqEnabled,
                                    accentColor = accentColor,
                                    valueRange = minMb.toFloat()..maxMb.toFloat(),
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(185.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = bandFreqLabels[i],
                                    color = if (eqEnabled) Color.White else Color.White.copy(alpha = 0.35f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (useHorizontalBands) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bandsContent()
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until bandCount) {
                                val level = eqBands[i] ?: 0
                                val dbValue = level / 100f
                                val dbLabel = if (dbValue > 0.05f) {
                                    String.format(java.util.Locale.US, "+%.1f", dbValue)
                                } else {
                                    String.format(java.util.Locale.US, "%.1f", dbValue)
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = if (eqEnabled && level != 0) accentColor.copy(alpha = 0.14f)
                                        else Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${dbLabel}dB",
                                            color = if (eqEnabled && level != 0) accentColor
                                            else Color.White.copy(alpha = if (eqEnabled) 0.7f else 0.3f),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    CanvasVerticalSlider(
                                        value = level.toFloat(),
                                        onValueChange = { viewModel.setEqBand(i, it.roundToInt()) },
                                        enabled = eqEnabled,
                                        accentColor = accentColor,
                                        valueRange = minMb.toFloat()..maxMb.toFloat(),
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(185.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = bandFreqLabels[i],
                                        color = if (eqEnabled) Color.White else Color.White.copy(alpha = 0.35f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── 4. Hardware FX: Bass Boost + Spatial Surround ────────────────
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                tintColor = Color(0xFF131622),
                specularAlpha = 0.32f,
                hazeState = if (liquidGlassEnabled) eqHazeState else null
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "HARDWARE AUDIO EFFECTS",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bass Boost slider
                    EffectStrengthSliderRow(
                        icon = SimpIcons.GraphicEq,
                        title = "Bass Boost",
                        subtitle = "Hardware low-end sub punch",
                        strength = bassBoostStrength,
                        enabled = eqEnabled,
                        accentColor = accentColor,
                        onStrengthChange = { viewModel.setBassBoostStrength(it) }
                    )

                    Spacer(modifier = Modifier.height(22.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Spatial Surround slider
                    EffectStrengthSliderRow(
                        icon = SimpIcons.SurroundSound,
                        title = "Spatial Surround",
                        subtitle = "Stereo soundstage virtualizer",
                        strength = virtualizerStrength,
                        enabled = eqEnabled,
                        accentColor = accentColor,
                        onStrengthChange = { viewModel.setVirtualizerStrength(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 5. Loudness Normalization & System EQ Panel ──────────────────
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(22.dp),
                tintColor = Color(0xFF131622),
                specularAlpha = 0.28f,
                hazeState = if (liquidGlassEnabled) eqHazeState else null
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setNormalizeVolume(!normalizeVolume) }
                            .padding(horizontal = 18.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(activeAccent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = SimpIcons.VolumeUp,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Normalize Volume",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Keep track loudness balanced",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        IOSSwitch(
                            checked = normalizeVolume,
                            onCheckedChange = { viewModel.setNormalizeVolume(it) }
                        )
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.06f),
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Device equalizer not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 18.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.07f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = SimpIcons.SettingsEthernet,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System Equalizer Panel",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Launch built-in OEM DSP effects",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = SimpIcons.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ── Floating Liquid Glass Header Bar ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .align(Alignment.TopCenter)
                .liquidGlass(
                    shape = RoundedCornerShape(22.dp),
                    tintColor = Color(0xFF10131F),
                    specularAlpha = 0.35f,
                    elevation = 10.dp,
                    hazeState = if (liquidGlassEnabled) eqHazeState else null
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = SimpIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Equalizer & Effects",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = {
                        viewModel.resetEqToFlat()
                        Toast.makeText(context, "Equalizer reset to flat", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = SimpIcons.RestartAlt,
                        contentDescription = "Reset Equalizer",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IOSSwitch(
                    checked = eqEnabled,
                    onCheckedChange = { viewModel.toggleEq() }
                )
            }
        }

        // ── Save Custom Preset Dialog ────────────────────────────────────────
        if (showSavePresetDialog) {
            AlertDialog(
                onDismissRequest = { showSavePresetDialog = false },
                containerColor = Color(0xFF161926),
                titleContentColor = Color.White,
                textContentColor = Color.White.copy(alpha = 0.8f),
                title = { Text("Save Custom Preset", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Save your current ${bandCount}-band curve so you can switch back anytime.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = presetNameInput,
                            onValueChange = { if (it.length <= 28) presetNameInput = it },
                            singleLine = true,
                            placeholder = { Text("e.g. Late Night Bass", color = Color.White.copy(alpha = 0.35f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                cursorColor = accentColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = presetNameInput.trim().isNotEmpty(),
                        onClick = {
                            val name = presetNameInput.trim()
                            viewModel.saveCustomEqPreset(name)
                            if (!eqEnabled) viewModel.toggleEq()
                            showSavePresetDialog = false
                            Toast.makeText(context, "Saved preset \"$name\"", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Save", color = accentColor, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSavePresetDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.65f))
                    }
                }
            )
        }

        // ── Delete Custom Preset Confirmation ────────────────────────────────
        presetToDelete?.let { target ->
            AlertDialog(
                onDismissRequest = { presetToDelete = null },
                containerColor = Color(0xFF161926),
                titleContentColor = Color.White,
                textContentColor = Color.White.copy(alpha = 0.8f),
                title = { Text("Delete Preset?", fontWeight = FontWeight.Bold) },
                text = { Text("Remove \"${target.name}\" from your custom presets?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteCustomEqPreset(target.name)
                            presetToDelete = null
                            Toast.makeText(context, "Deleted \"${target.name}\"", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Delete", color = Color(0xFFFF5A5F), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { presetToDelete = null }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.65f))
                    }
                }
            )
        }
    }
}

@Composable
private fun EqPresetChip(
    title: String,
    isSelected: Boolean,
    isCustom: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)?
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) accentColor else Color.White.copy(alpha = 0.06f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                start = 15.dp,
                end = if (isCustom && onDeleteClick != null) 8.dp else 15.dp,
                top = 9.dp,
                bottom = 9.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.88f),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (isCustom && onDeleteClick != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.Black.copy(alpha = 0.18f)
                            else Color.White.copy(alpha = 0.12f)
                        )
                        .clickable(onClick = onDeleteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SimpIcons.Close,
                        contentDescription = "Delete Preset",
                        tint = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EqFrequencyCurveCanvas(
    bandLevels: List<Int>,
    minLevelMb: Int,
    maxLevelMb: Int,
    enabled: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val span = (maxLevelMb - minLevelMb).coerceAtLeast(1).toFloat()
    val animatedNorms = bandLevels.mapIndexed { idx, level ->
        val norm = ((level - minLevelMb) / span).coerceIn(0f, 1f)
        animateFloatAsState(
            targetValue = norm,
            animationSpec = tween(durationMillis = 180),
            label = "curve_band_$idx"
        ).value
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padH = 14.dp.toPx()
        val padV = 12.dp.toPx()
        val usableW = (w - padH * 2).coerceAtLeast(1f)
        val usableH = (h - padV * 2).coerceAtLeast(1f)

        // Grid lines (+max, 0 dB, -min)
        val gridRatios = listOf(0f, 0.5f, 1f)
        gridRatios.forEachIndexed { idx, r ->
            val y = padV + usableH * r
            drawLine(
                color = Color.White.copy(alpha = if (idx == 1) 0.14f else 0.06f),
                start = Offset(padH, y),
                end = Offset(w - padH, y),
                strokeWidth = if (idx == 1) 1.2.dp.toPx() else 0.8.dp.toPx()
            )
        }

        if (animatedNorms.isEmpty()) return@Canvas

        val points = animatedNorms.mapIndexed { i, norm ->
            val x = if (animatedNorms.size == 1) w / 2f else padH + usableW * (i.toFloat() / (animatedNorms.size - 1))
            val y = padV + usableH * (1f - norm)
            Offset(x, y)
        }

        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val midX = (p0.x + p1.x) / 2f
                cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            }
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(points.last().x, h - padV)
            lineTo(points.first().x, h - padV)
            close()
        }

        val lineColor = if (enabled) accentColor else Color.White.copy(alpha = 0.28f)
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = if (enabled) 0.34f else 0.10f),
                    lineColor.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                startY = padV,
                endY = h - padV
            )
        )

        if (enabled) {
            drawPath(
                path = strokePath,
                color = accentColor.copy(alpha = 0.25f),
                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        points.forEach { pt ->
            if (enabled) {
                drawCircle(
                    color = accentColor.copy(alpha = 0.25f),
                    radius = 7.dp.toPx(),
                    center = pt
                )
            }
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = lineColor,
                radius = 2.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
private fun EffectStrengthSliderRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    strength: Int,
    enabled: Boolean,
    accentColor: Color,
    onStrengthChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (enabled) accentColor.copy(alpha = 0.14f)
                            else Color.White.copy(alpha = 0.05f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) accentColor else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = if (enabled) Color.White else Color.White.copy(alpha = 0.45f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = if (enabled) 0.5f else 0.28f),
                        fontSize = 11.5.sp
                    )
                }
            }
            Surface(
                color = if (enabled && strength > 0) accentColor.copy(alpha = 0.16f)
                else Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "${(strength / 10f).roundToInt()}%",
                    color = if (enabled && strength > 0) accentColor
                    else Color.White.copy(alpha = if (enabled) 0.7f else 0.3f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        CanvasHorizontalSlider(
            value = strength.toFloat(),
            onValueChange = { onStrengthChange(it.roundToInt()) },
            enabled = enabled,
            accentColor = accentColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Custom Smooth Gradient Sliders (Canvas)
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
    val span = (valueRange.endInclusive - valueRange.start).coerceAtLeast(1f)
    val normalizedValue = ((value - valueRange.start) / span).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = normalizedValue, animationSpec = tween(150), label = "progress")

    Canvas(
        modifier = modifier
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectVerticalDragGestures { change, _ ->
                    val y = change.position.y.coerceIn(0f, size.height.toFloat())
                    val percent = 1f - (y / size.height.toFloat())
                    val newValue = valueRange.start + (percent * span)
                    onValueChange(newValue)
                }
            }
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val y = offset.y.coerceIn(0f, size.height.toFloat())
                    val percent = 1f - (y / size.height.toFloat())
                    val newValue = valueRange.start + (percent * span)
                    onValueChange(newValue)
                }
            }
    ) {
        val trackWidth = 6.dp.toPx()
        val cornerRadius = CornerRadius(trackWidth / 2, trackWidth / 2)
        val trackX = center.x - trackWidth / 2

        drawRoundRect(
            color = Color.White.copy(alpha = 0.08f),
            topLeft = Offset(trackX, 0f),
            size = Size(trackWidth, size.height),
            cornerRadius = cornerRadius
        )

        val zeroY = size.height / 2f
        val valueY = size.height * (1f - animatedProgress)

        // Subtle center tick
        drawLine(
            color = Color.White.copy(alpha = 0.25f),
            start = Offset(trackX - 5.dp.toPx(), zeroY),
            end = Offset(trackX + trackWidth + 5.dp.toPx(), zeroY),
            strokeWidth = 1.dp.toPx()
        )

        val topY = minOf(zeroY, valueY)
        val activeHeight = kotlin.math.abs(zeroY - valueY)

        if (enabled) {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(accentColor, accentColor.copy(alpha = 0.65f)),
                    startY = topY,
                    endY = topY + activeHeight.coerceAtLeast(1f)
                ),
                topLeft = Offset(trackX, topY),
                size = Size(trackWidth, activeHeight),
                cornerRadius = cornerRadius
            )
        } else {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.2f),
                topLeft = Offset(trackX, topY),
                size = Size(trackWidth, activeHeight),
                cornerRadius = cornerRadius
            )
        }

        val thumbRadius = 10.dp.toPx()
        val thumbY = valueY.coerceIn(thumbRadius, size.height - thumbRadius)

        if (enabled) {
            drawCircle(
                color = accentColor.copy(alpha = 0.28f),
                radius = thumbRadius * 1.75f,
                center = Offset(center.x, thumbY)
            )
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(center.x, thumbY)
            )
            drawCircle(
                color = accentColor,
                radius = thumbRadius * 0.42f,
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

@Composable
fun CanvasHorizontalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1000f,
    enabled: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val span = (valueRange.endInclusive - valueRange.start).coerceAtLeast(1f)
    val normalizedValue = ((value - valueRange.start) / span).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = normalizedValue, animationSpec = tween(150), label = "progress")

    Canvas(
        modifier = modifier
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures { change, _ ->
                    val x = change.position.x.coerceIn(0f, size.width.toFloat())
                    val percent = x / size.width.toFloat()
                    val newValue = valueRange.start + (percent * span)
                    onValueChange(newValue)
                }
            }
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val x = offset.x.coerceIn(0f, size.width.toFloat())
                    val percent = x / size.width.toFloat()
                    val newValue = valueRange.start + (percent * span)
                    onValueChange(newValue)
                }
            }
    ) {
        val trackHeight = 6.dp.toPx()
        val cornerRadius = CornerRadius(trackHeight / 2, trackHeight / 2)
        val trackY = center.y - trackHeight / 2

        drawRoundRect(
            color = Color.White.copy(alpha = 0.08f),
            topLeft = Offset(0f, trackY),
            size = Size(size.width, trackHeight),
            cornerRadius = cornerRadius
        )

        val activeWidth = size.width * animatedProgress

        if (enabled) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.55f), accentColor),
                    startX = 0f,
                    endX = activeWidth.coerceAtLeast(1f)
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

        val thumbRadius = 11.dp.toPx()
        val thumbX = activeWidth.coerceIn(thumbRadius, size.width - thumbRadius)

        if (enabled) {
            drawCircle(
                color = accentColor.copy(alpha = 0.26f),
                radius = thumbRadius * 1.75f,
                center = Offset(thumbX, center.y)
            )
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(thumbX, center.y)
            )
            drawCircle(
                color = accentColor,
                radius = thumbRadius * 0.42f,
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
