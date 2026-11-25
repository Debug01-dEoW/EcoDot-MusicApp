@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.example.ecodot.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.local.entities.UserProfile
import com.example.ecodot.data.remote.HomeChartPodcast
import com.example.ecodot.data.remote.HomeMediaItem
import com.example.ecodot.data.remote.HomeSection
import com.example.ecodot.data.remote.MediaType
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.PlayArrow
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.animatedCombinedClickable
import com.example.ecodot.ui.viewmodel.Album
import com.example.ecodot.ui.viewmodel.MusicViewModel
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.absoluteValue

/**
 * Rich frosted Liquid Glass surface modifier for floating Home top controls.
 *
 * Unlike plain transparent overlays, this combines Kyant Backdrop lens refraction, blur,
 * vibrancy, inner bevel reflection (`InnerShadow`), specular edge highlight (`Highlight`),
 * and a frosted smoked-glass tint + vertical sheen so controls remain visibly sculpted
 * over both dark obsidian and bright ambient artwork backgrounds.
 */
@Composable
fun Modifier.homeLiquidGlass(
    backdrop: Backdrop?,
    shape: Shape,
    surfaceTint: Color = Color(0xFF1F2130),
    surfaceAlpha: Float = 0.52f,
    specularAlpha: Float = 0.86f,
    frostSheenAlpha: Float = 0.22f,
    blurDp: Dp = 12.dp,
    pressedScaleTarget: Float = 0.96f,
    isPressed: Boolean = false
): Modifier {
    val glassEnabled = LocalLiquidGlassEnabled.current
    val tilt = LocalTiltState.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScaleTarget else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "home_glass_scale"
    )

    val rimBrush = remember(tilt.tiltX, tilt.tiltY, specularAlpha) {
        createSpecularRimBrush(
            tiltX = tilt.tiltX,
            tiltY = tilt.tiltY,
            baseAlpha = specularAlpha * 0.62f,
            secondaryAlpha = 0.16f
        )
    }

    val scaled = this.scale(scale)

    if (!glassEnabled || backdrop == null) {
        return scaled
            .clip(shape)
            .background(surfaceTint.copy(alpha = 0.90f), shape)
            .border(1.dp, rimBrush, shape)
    }

    return scaled
        .drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            highlight = {
                Highlight(
                    style = HighlightStyle.Default(
                        angle = 45f + (tilt.tiltX * 28f) + (tilt.tiltY * 14f)
                    ),
                    width = 1.15.dp,
                    alpha = specularAlpha
                )
            },
            shadow = {
                Shadow(radius = 8.dp, color = Color.Black.copy(alpha = 0.34f))
            },
            innerShadow = {
                InnerShadow(radius = 6.dp, alpha = 0.36f, color = Color.White)
            },
            effects = {
                vibrancy()
                colorControls(
                    brightness = 0.08f,
                    contrast = 1.12f,
                    saturation = 1.42f
                )
                blur(blurDp.toPx())
                if (size.minDimension > 2f) {
                    lens(
                        refractionHeight = size.minDimension / 4f,
                        refractionAmount = size.minDimension / 2f,
                        depthEffect = false
                    )
                }
            },
            onDrawSurface = {
                drawRect(surfaceTint.copy(alpha = surfaceAlpha))
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = frostSheenAlpha),
                            Color.White.copy(alpha = frostSheenAlpha * 0.30f),
                            Color.Black.copy(alpha = 0.16f)
                        )
                    )
                )
            }
        )
        .border(0.85.dp, rimBrush, shape)
}

/**
 * Animated sweep-gradient rim stroke around active chips while Home refreshes or loads.
 */
@Composable
fun Modifier.homeLoadingRing(
    enabled: Boolean,
    shape: Shape
): Modifier {
    if (!enabled) return this
    val transition = rememberInfiniteTransition(label = "chip_loading_ring")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_phase"
    )
    return this.drawWithContent {
        drawContent()
        val radius = size.height / 2f
        val cx = size.width / 2f + kotlin.math.cos(phase) * (size.width * 0.35f)
        val cy = size.height / 2f + kotlin.math.sin(phase) * (size.height * 0.35f)
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.92f),
                    Color(0xFFE7D7FD).copy(alpha = 0.55f),
                    Color.White.copy(alpha = 0.08f)
                ),
                center = Offset(cx, cy),
                radius = size.maxDimension * 0.55f
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            style = Stroke(width = 1.6.dp.toPx())
        )
    }
}

/**
 * Smooth DstIn edge fade for the scrollable Home chip strip (SimpMusic chipStripEdgeFades).
 */
fun Modifier.homeChipStripEdgeFades(
    width: Dp,
    start: () -> Float,
    end: () -> Float
): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val fadePx = width.toPx().coerceAtMost(size.width / 2f)
            val s = start()
            if (s > 0f) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colorStops = homeChipFadeStops(s, fadingOut = false),
                        startX = 0f,
                        endX = fadePx
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
            val e = end()
            if (e > 0f) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colorStops = homeChipFadeStops(e, fadingOut = true),
                        startX = size.width - fadePx,
                        endX = size.width
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
        }

private fun homeChipFadeStops(fade: Float, fadingOut: Boolean): Array<Pair<Float, Color>> =
    Array(25) { i ->
        val t = i / 24f
        val ramp = t * t * (3f - 2f * t)
        t to Color.Black.copy(alpha = 1f - fade * (if (fadingOut) ramp else 1f - ramp))
    }

/**
 * Circular Liquid Glass Account / Profile Button with SimpMusic greeting menu.
 */
@Composable
fun LiquidGlassProfileButton(
    userProfile: UserProfile?,
    backdrop: Backdrop?,
    onOpenProfile: () -> Unit,
    onRefreshFeed: () -> Unit,
    onCheckUpdates: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember(hour) {
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }
    val displayName = userProfile?.name?.takeIf { it.isNotBlank() } ?: "EcoDot Listener"

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .homeLiquidGlass(
                    backdrop = backdrop,
                    shape = CircleShape,
                    surfaceTint = Color(0xFF212435),
                    surfaceAlpha = 0.56f,
                    specularAlpha = 0.90f,
                    frostSheenAlpha = 0.26f,
                    isPressed = isPressed
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { menuExpanded = true }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!userProfile?.avatarUri.isNullOrEmpty()) {
                AsyncImage(
                    model = userProfile?.avatarUri,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(21.dp)
                )
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier
                .width(248.dp)
                .background(Color(0xFF1A1C26))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        menuExpanded = false
                        onOpenProfile()
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2D3E)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userProfile?.avatarUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = userProfile?.avatarUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color(0xFFE7D7FD),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = greeting,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            DropdownMenuItem(
                text = { Text("Profile & stats", color = Color.White, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.Person, null, tint = Color(0xFFE7D7FD), modifier = Modifier.size(19.dp)) },
                onClick = {
                    menuExpanded = false
                    onOpenProfile()
                }
            )
            DropdownMenuItem(
                text = { Text("Refresh Home feed", color = Color.White, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.Refresh, null, tint = Color(0xFF90CAF9), modifier = Modifier.size(19.dp)) },
                onClick = {
                    menuExpanded = false
                    onRefreshFeed()
                }
            )
            DropdownMenuItem(
                text = { Text("Check for updates", color = Color.White, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.SystemUpdate, null, tint = Color(0xFF80CBC4), modifier = Modifier.size(19.dp)) },
                onClick = {
                    menuExpanded = false
                    onCheckUpdates()
                }
            )
            DropdownMenuItem(
                text = { Text("Settings", color = Color.White, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.Settings, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(19.dp)) },
                onClick = {
                    menuExpanded = false
                    onOpenSettings()
                }
            )
        }
    }
}

/**
 * Liquid Glass [All] filter capsule.
 * Selected state uses luminous frosted lavender glass; unselected state renders deep frosted Liquid Glass.
 */
@Composable
fun LiquidGlassAllChip(
    isSelected: Boolean,
    isLoading: Boolean,
    backdrop: Backdrop?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = CircleShape
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pillTint by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFE5D4FC) else Color(0xFF212435),
        animationSpec = tween(220),
        label = "all_chip_tint"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF1D0F36) else Color.White.copy(alpha = 0.92f),
        animationSpec = tween(220),
        label = "all_chip_label"
    )

    Box(
        modifier = modifier
            .height(38.dp)
            .then(
                if (isSelected) {
                    Modifier
                        .clip(shape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFF0E3FF),
                                    Color(0xFFE0CAF9)
                                )
                            ),
                            shape
                        )
                        .border(0.9.dp, Color.White.copy(alpha = 0.72f), shape)
                } else {
                    Modifier.homeLiquidGlass(
                        backdrop = backdrop,
                        shape = shape,
                        surfaceTint = pillTint,
                        surfaceAlpha = 0.56f,
                        specularAlpha = 0.88f,
                        frostSheenAlpha = 0.24f,
                        isPressed = isPressed
                    )
                }
            )
            .homeLoadingRing(enabled = isLoading && isSelected, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "All",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = labelColor,
            maxLines = 1
        )
    }
}

/**
 * SimpMusic-style Expandable Compound Liquid Glass [Mood] capsule.
 * Transitions fluidly in one pill across Closed ("Mood"), Expanded ("Mood | Relax Sleep ..."),
 * and Folded ("Mood | Relax") with genuine Liquid Glass frost & refraction.
 */
@Composable
fun LiquidGlassMoodChip(
    selectedMood: String?,
    isExpanded: Boolean,
    isLoading: Boolean,
    options: List<String>,
    backdrop: Backdrop?,
    onExpandChange: (Boolean) -> Unit,
    onSelectMood: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = CircleShape
    val folded = !isExpanded && selectedMood != null
    val active = isExpanded || selectedMood != null

    var lastPicked by remember { mutableStateOf(selectedMood) }
    SideEffect { if (selectedMood != null) lastPicked = selectedMood }
    val pickedLabel = selectedMood ?: lastPicked.orEmpty()

    val outerTint by animateColorAsState(
        targetValue = when {
            folded -> Color(0xFF422C68)
            isExpanded -> Color(0xFF2B2242)
            else -> Color(0xFF212435)
        },
        animationSpec = tween(220),
        label = "mood_outer_tint"
    )
    val headTint by animateColorAsState(
        targetValue = if (active) Color(0xFFE7D7FD) else Color.Transparent,
        animationSpec = tween(220),
        label = "mood_head_tint"
    )
    val headTextColor by animateColorAsState(
        targetValue = if (active) Color(0xFF1E1035) else Color.White.copy(alpha = 0.92f),
        animationSpec = tween(220),
        label = "mood_head_label"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = modifier
            .height(38.dp)
            .homeLiquidGlass(
                backdrop = backdrop,
                shape = shape,
                surfaceTint = outerTint,
                surfaceAlpha = if (active) 0.68f else 0.56f,
                specularAlpha = if (folded) 0.78f else 0.88f,
                frostSheenAlpha = if (active) 0.24f else 0.24f,
                isPressed = isPressed && !active
            )
            .homeLoadingRing(enabled = isLoading && selectedMood != null, shape = shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(if (active) 2.dp else 0.dp)
                .clip(shape)
                .background(
                    if (active) {
                        Brush.verticalGradient(
                            listOf(
                                headTint.copy(alpha = 0.98f),
                                headTint.copy(alpha = 0.86f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    },
                    shape
                )
                .then(
                    if (active) {
                        Modifier.border(0.75.dp, Color.White.copy(alpha = 0.65f), shape)
                    } else Modifier
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onExpandChange(!isExpanded) }
                )
                .padding(horizontal = if (active) 14.dp else 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mood",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 13.5.sp
                ),
                color = headTextColor,
                maxLines = 1
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandHorizontally() + fadeIn(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 2.dp, end = 6.dp)
            ) {
                options.forEach { mood ->
                    val isCurrent = selectedMood == mood
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(shape)
                            .background(
                                if (isCurrent) Color(0xFFE7D7FD).copy(alpha = 0.22f) else Color.Transparent,
                                shape
                            )
                            .then(
                                if (isCurrent) Modifier.border(0.75.dp, Color(0xFFE7D7FD).copy(alpha = 0.6f), shape)
                                else Modifier
                            )
                            .clickable { onSelectMood(mood) }
                            .padding(horizontal = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mood,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isCurrent) Color(0xFFF3E9FF) else Color.White.copy(alpha = 0.88f),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = folded,
            enter = expandHorizontally() + fadeIn(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(shape)
                    .clickable { onExpandChange(true) }
                    .padding(start = 10.dp, end = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pickedLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = Color(0xFFF3E9FF),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Top-right Liquid Glass Action Pill containing [+] and [Settings].
 * Uses thick frosted glass optics with crisp icon contrast and press response.
 */
@Composable
fun LiquidGlassActionPill(
    backdrop: Backdrop?,
    onCreateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = CircleShape
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = modifier
            .height(42.dp)
            .homeLiquidGlass(
                backdrop = backdrop,
                shape = shape,
                surfaceTint = Color(0xFF212435),
                surfaceAlpha = 0.56f,
                specularAlpha = 0.90f,
                frostSheenAlpha = 0.25f,
                isPressed = isPressed
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCreateClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Create",
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
        }
        Box(
            modifier = Modifier
                .width(0.6.dp)
                .height(18.dp)
                .background(Color.White.copy(alpha = 0.16f))
        )
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

/**
 * Ambient glow header backdrop combining SimpMusic's 25-degree dominant artwork gradient,
 * optional YouTube Home artwork background, mood glow orbs, and 24-stop S-curve scrim.
 */
@Composable
fun HomeAmbientBackground(
    activeMood: String?,
    heroColor: Color,
    headerImageUrl: String?
) {
    val (moodColor1, moodColor2, moodColor3) = when (activeMood?.lowercase()) {
        "relax", "chill" -> Triple(Color(0xFF472C66), Color(0xFF213159), Color(0xFF141D33))
        "workout", "energize" -> Triple(Color(0xFFE63946), Color(0xFFFB8500), Color(0xFF6B1116))
        "focus" -> Triple(Color(0xFF0077B6), Color(0xFF00B4D8), Color(0xFF03264A))
        "romance" -> Triple(Color(0xFFE05780), Color(0xFF9D0208), Color(0xFF3A0B1A))
        "party" -> Triple(Color(0xFF7209B7), Color(0xFFF72585), Color(0xFF180A33))
        "sleep", "sad" -> Triple(Color(0xFF23324D), Color(0xFF10192D), Color(0xFF090D16))
        else -> {
            val darkHero = lerp(heroColor, Color(0xFF070709), 0.42f)
            val secondary = lerp(heroColor, Color(0xFFC88132), 0.35f)
            Triple(darkHero, secondary, Color(0xFF5A3D28))
        }
    }

    val color1 by animateColorAsState(moodColor1, tween(650), label = "ambient_c1")
    val color2 by animateColorAsState(moodColor2, tween(650), label = "ambient_c2")
    val color3 by animateColorAsState(moodColor3, tween(650), label = "ambient_c3")
    var bgImageFailed by remember(headerImageUrl) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(470.dp)
    ) {
        if (!headerImageUrl.isNullOrBlank() && !bgImageFailed && activeMood == null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(headerImageUrl)
                    .crossfade(550)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { bgImageFailed = true },
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .angledGradientBackground(
                        listOf(Color(0xFF070709).copy(alpha = 0.58f), Color(0xFF070709)),
                        25f
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .angledGradientBackground(
                        listOf(color1.copy(alpha = 0.72f), Color(0xFF070709)),
                        25f
                    )
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1.copy(alpha = 0.48f), Color.Transparent),
                    center = Offset(w * 0.24f, h * 0.20f),
                    radius = w * 0.48f
                ),
                center = Offset(w * 0.24f, h * 0.20f),
                radius = w * 0.48f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color2.copy(alpha = 0.40f), Color.Transparent),
                    center = Offset(w * 0.80f, h * 0.16f),
                    radius = w * 0.42f
                ),
                center = Offset(w * 0.80f, h * 0.16f),
                radius = w * 0.42f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color3.copy(alpha = 0.30f), Color.Transparent),
                    center = Offset(w * 0.50f, h * 0.40f),
                    radius = w * 0.40f
                ),
                center = Offset(w * 0.50f, h * 0.40f),
                radius = w * 0.40f
            )

            if (activeMood?.equals("relax", true) == true || activeMood?.equals("chill", true) == true) {
                val palmRight = Path().apply {
                    moveTo(w * 0.75f, 0f)
                    quadraticTo(w * 0.88f, h * 0.12f, w, h * 0.08f)
                    quadraticTo(w * 0.90f, h * 0.22f, w, h * 0.25f)
                    quadraticTo(w * 0.82f, h * 0.18f, w * 0.85f, h * 0.32f)
                    quadraticTo(w * 0.75f, h * 0.16f, w * 0.70f, h * 0.24f)
                    close()
                }
                drawPath(palmRight, Color.Black.copy(alpha = 0.45f))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.62f)
                .align(Alignment.BottomCenter)
                .background(artworkScrimBrush(Color(0xFF070709)))
        )
    }
}

/**
 * SimpMusic HomeSectionHeader: optional circular topic/artist avatar, eyebrow strapline,
 * bold title, and compact "More" / "Play all" action pill on the right.
 */
@Composable
fun SimpSectionHeader(
    title: String,
    eyebrow: String? = null,
    avatarUrl: String? = null,
    actionText: String? = null,
    onHeaderClick: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(8.dp))
                .then(if (onHeaderClick != null) Modifier.clickable { onHeaderClick() } else Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(0.75.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Column {
                if (!eyebrow.isNullOrBlank()) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = Color.White.copy(alpha = 0.58f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(0.75.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                    .clickable { onActionClick() }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White.copy(alpha = 0.90f),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Centered Hero Discover Carousel matching SimpMusic's HomeDiscoverSection ("Your daily discover").
 * Shows large rounded cards with neighboring previews, smooth dark gradient scrim,
 * song title + artist subtitle, and one-tap Play button on the hero card.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeDiscoverHeroCarousel(
    items: List<HomeMediaItem>,
    onItemClick: (HomeMediaItem) -> Unit,
    onItemLongClick: (HomeMediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val heroWidth = (maxWidth * 0.78f).coerceAtMost(336.dp)
        val sidePadding = ((maxWidth - heroWidth) / 2f).coerceAtLeast(16.dp)

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(heroWidth),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = sidePadding),
            modifier = Modifier
                .fillMaxWidth()
                .height(276.dp)
        ) { page ->
            val item = items[page]
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val scale = (1f - (pageOffset * 0.08f)).coerceIn(0.90f, 1f)
            val cardAlpha = (1f - (pageOffset * 0.25f)).coerceIn(0.68f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = cardAlpha
                    }
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF161722))
                    .border(0.8.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(26.dp))
                    .combinedClickable(
                        onClick = {
                            if (page == pagerState.currentPage) {
                                onItemClick(item)
                            } else {
                                scope.launch { pagerState.animateScrollToPage(page) }
                            }
                        },
                        onLongClick = { onItemLongClick(item) }
                    )
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.thumbnailUrl)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .crossfade(500)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            smoothScrimBrush(
                                from = Color.Transparent,
                                to = Color.Black.copy(alpha = 0.84f),
                                startFraction = 0.36f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 18.dp, end = 68.dp, bottom = 18.dp)
                ) {
                    if (item.isExplicit) {
                        ExplicitBadge(modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = Color.White.copy(alpha = 0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE7D7FD).copy(alpha = 0.92f))
                        .border(0.8.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                        .clickable { onItemClick(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SimpIcons.PlayArrow,
                        contentDescription = "Play",
                        tint = Color(0xFF1E1035),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Circular Artist card matching SimpMusic's HomeItemArtist.
 */
@Composable
fun HomeArtistCard(
    item: HomeMediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(136.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.thumbnailUrl)
                .crossfade(500)
                .build(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(Color(0xFF161722))
                .border(0.8.dp, Color.White.copy(alpha = 0.14f), CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        if (item.subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = Color(0xFFA8A8A8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.basicMarquee(
                    initialDelayMillis = 2000,
                    repeatDelayMillis = 2000,
                    velocity = 25.dp
                )
            )
        }
    }
}

/**
 * Ranked 16:9 Video Chart card matching SimpMusic's ItemVideoChart:
 * widescreen cover + prominent rank number + video title & artist/views.
 */
@Composable
fun RankedVideoChartCard(
    video: HomeMediaItem,
    rank: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(254.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(142.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF161722))
                .border(0.6.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(video.thumbnailUrl)
                    .crossfade(500)
                    .build(),
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Watch",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(34.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = video.subtitle.ifBlank { "YouTube Charts" },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFFA8A8A8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(
                        initialDelayMillis = 2000,
                        repeatDelayMillis = 2000,
                        velocity = 25.dp
                    )
                )
            }
        }
    }
}

/**
 * Ranked row inside 3-row horizontal grids (Top Artists and Weekly Top Podcasts).
 */
@Composable
fun RankedChartRow(
    rank: String,
    title: String,
    subtitle: String,
    thumbnailUrl: String,
    isCircularThumbnail: Boolean,
    itemWidth: Dp,
    onClick: () -> Unit
) {
    val thumbShape = if (isCircularThumbnail) CircleShape else RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .width(itemWidth)
            .height(66.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rank,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(30.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .crossfade(450)
                .build(),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(54.dp)
                .clip(thumbShape)
                .background(Color(0xFF161722))
                .border(0.6.dp, Color.White.copy(alpha = 0.14f), thumbShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFFA8A8A8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(
                        initialDelayMillis = 2000,
                        repeatDelayMillis = 2000,
                        velocity = 25.dp
                    )
                )
            }
        }
    }
}

/**
 * Enhanced Quick Picks Row with [E] badge, LIVE badge, marquee title/artist, playing equalizer bars,
 * and trailing options menu trigger matching SimpMusic.
 */
@Composable
fun SimpQuickPicksRowItem(
    track: Track,
    isPlaying: Boolean,
    itemWidth: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(itemWidth)
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .animatedCombinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(track.albumArtUri)
                .crossfade(500)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF161722)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = if (isPlaying) EcoDotRed else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(
                        initialDelayMillis = 2000,
                        repeatDelayMillis = 2000,
                        velocity = 25.dp
                    )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (track.isExplicit || track.title.contains("explicit", true)) {
                    ExplicitBadge(modifier = Modifier.padding(end = 5.dp))
                }
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFFA8A8A8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(
                        initialDelayMillis = 2000,
                        repeatDelayMillis = 2000,
                        velocity = 25.dp
                    )
                )
            }
        }
        if (isPlaying) {
            Spacer(modifier = Modifier.width(6.dp))
            HomeMiniEqualizerBars(modifier = Modifier.padding(end = 4.dp))
        }
        IconButton(
            onClick = onLongClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Track options",
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HomeMiniEqualizerBars(modifier: Modifier = Modifier, color: Color = EcoDotRed) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(360, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(510, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        modifier = modifier.size(width = 14.dp, height = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight(h1).clip(RoundedCornerShape(1.dp)).background(color))
        Box(modifier = Modifier.weight(1f).fillMaxHeight(h2).clip(RoundedCornerShape(1.dp)).background(color))
        Box(modifier = Modifier.weight(1f).fillMaxHeight(h3).clip(RoundedCornerShape(1.dp)).background(color))
    }
}

/**
 * Live Red Badge matching YouTube Music & SimpMusic LiveBadge.
 */
@Composable
fun HomeLiveBadge(modifier: Modifier = Modifier) {
    Text(
        text = "LIVE",
        color = Color.White,
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFFCC0000))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}

/**
 * Skeleton Loading Shimmer matching SimpMusic's HomeShimmer.
 */
@Composable
fun HomeFeedShimmer(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Box(
            modifier = Modifier
                .width(170.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .simpShimmer()
        )
        Spacer(modifier = Modifier.height(14.dp))
        LazyRow(
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(4) {
                Column(modifier = Modifier.width(148.dp)) {
                    Box(
                        modifier = Modifier
                            .size(148.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .simpShimmer()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .simpShimmer()
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .width(88.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .simpShimmer()
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(26.dp))
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .simpShimmer()
        )
        Spacer(modifier = Modifier.height(12.dp))
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .simpShimmer()
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .simpShimmer()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .simpShimmer()
                    )
                }
            }
        }
    }
}

/**
 * SimpMusic-style Create Action Sheet opened from top-right [+] button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCreateActionBottomSheet(
    onDismiss: () -> Unit,
    onCreatePlaylist: () -> Unit,
    onShuffleQuickPicks: () -> Unit,
    onRefreshFeed: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF171822),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Create",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            HomeCreateSheetRow(
                icon = Icons.Rounded.PlaylistAdd,
                title = "Local playlist",
                subtitle = "Create a custom playlist stored on your device",
                onClick = {
                    onDismiss()
                    onCreatePlaylist()
                }
            )
            HomeCreateSheetRow(
                icon = Icons.Rounded.Shuffle,
                title = "Play Instant Radio",
                subtitle = "Start a shuffled queue from your current Quick picks",
                onClick = {
                    onDismiss()
                    onShuffleQuickPicks()
                }
            )
            HomeCreateSheetRow(
                icon = Icons.Rounded.Refresh,
                title = "Refresh YouTube Music Feed",
                subtitle = "Pull fresh recommendations, videos & charts",
                onClick = {
                    onDismiss()
                    onRefreshFeed()
                }
            )
        }
    }
}

@Composable
private fun HomeCreateSheetRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .border(0.7.dp, Color.White.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFE7D7FD), modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.60f),
                fontSize = 12.5.sp
            )
        }
    }
}

/**
 * Interactive Bottom Sheet displaying live Mood / Genre / "More" shelf playlists & tracks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBrowseSheet(
    state: MusicViewModel.BrowseSheetUiState,
    onDismiss: () -> Unit,
    onApplyMoodFilter: ((String) -> Unit)? = null,
    onItemClick: (HomeMediaItem) -> Unit,
    onItemLongClick: (HomeMediaItem) -> Unit
) {
    if (!state.isVisible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF13141C),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.84f)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "YouTube Music",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 12.sp
                    )
                }
                if (!state.moodFilterName.isNullOrBlank() && onApplyMoodFilter != null) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE7D7FD))
                            .clickable {
                                val m = state.moodFilterName
                                onDismiss()
                                onApplyMoodFilter(m)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Filter Home",
                            color = Color(0xFF1E1035),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.8f))
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFE7D7FD))
                }
            } else if (state.sections.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items available right now.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    state.sections.forEachIndexed { idx, section ->
                        item(key = "browse_sec_${section.title}_$idx") {
                            SimpSectionHeader(
                                title = section.title,
                                eyebrow = section.strapline
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(section.items, key = { "${it.id}_${it.title}" }) { item ->
                                    Column(
                                        modifier = Modifier
                                            .width(if (item.aspectRatio > 1.2f) 220.dp else 140.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .combinedClickable(
                                                onClick = { onItemClick(item) },
                                                onLongClick = { onItemLongClick(item) }
                                            )
                                    ) {
                                        AsyncImage(
                                            model = item.thumbnailUrl,
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(if (item.aspectRatio > 1.2f) 124.dp else 140.dp)
                                                .clip(if (item.type == MediaType.ARTIST) CircleShape else RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1B1C27))
                                        )
                                        Spacer(modifier = Modifier.height(7.dp))
                                        Text(
                                            text = item.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (item.subtitle.isNotBlank()) {
                                            Text(
                                                text = item.subtitle,
                                                color = Color(0xFFA8A8A8),
                                                fontSize = 11.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
