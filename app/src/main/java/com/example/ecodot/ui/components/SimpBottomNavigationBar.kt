package com.example.ecodot.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.LocalHazeState
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.theme.Home
import com.example.ecodot.ui.theme.QueueMusic
import com.example.ecodot.ui.theme.Search
import com.example.ecodot.ui.theme.SimpIcons
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

/**
 * Authentic SimpMusic Liquid Glass Navigation Bar & MiniPlayer Architecture.
 *
 * Layout Behavior (faithful to maxrave-dev/SimpMusic):
 *  1. When track is loaded & playing:
 *     - Collapsed State (Default): A single unified horizontal row.
 *       [ Left: 54dp Circular Glass Tab Button ] + [ Right: MiniPlayer (56dp, fills width) ]
 *       No awkward vertical stacking covering half the screen.
 *     - Expanded State (when tapped): MiniPlayer sits cleanly above the expanded bar,
 *       allowing seamless navigation between Home, Library, and Search.
 *  2. When no track is playing:
 *     - Expanded Bar: [ Left: Liquid Glass Capsule with Home & Library ] + [ Right: 56dp Search Glass FAB ]
 *  3. Visual Micro-Details:
 *     - Gyroscope-reactive specular rim lighting shifting with device tilt.
 *     - Organic liquid morphing indicator inside the capsule (squash & stretch spring physics).
 *     - Crystal-clear AGSL backdrop refraction lens with zero cloudy blur haze.
 */
@Composable
fun SimpBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = LocalBackdrop.current,
    hasTrack: Boolean = false,
    track: Track? = null,
    isPlaying: Boolean = false,
    progressProvider: (() -> Float)? = null,
    onTogglePlayback: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    onLike: (() -> Unit)? = null,
    isLiked: Boolean = false,
    onOpenNowPlaying: () -> Unit = {},
    onClosePlayer: (() -> Unit)? = null,
    dominantColor: Color = Color(0xFF1E1F2A)
) {
    val hapticFeedback = LocalHapticFeedback.current
    val tilt = LocalTiltState.current
    var lastNavTime by remember { mutableLongStateOf(0L) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val safeNavigate: (String) -> Unit = { route ->
        val now = System.currentTimeMillis()
        if (now - lastNavTime > 200L) {
            lastNavTime = now
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onNavigate(route)
            isExpanded = false
        }
    }

    // Determine target index: 0 = Home, 1 = Library, 2 = Search
    val activeIndex = when (currentRoute) {
        "home" -> 0
        "library" -> 1
        "search" -> 2
        else -> 0
    }

    val activeIcon = when (currentRoute) {
        "library" -> SimpIcons.QueueMusic
        "search" -> SimpIcons.Search
        else -> SimpIcons.Home
    }

    val isMediaActive = hasTrack && track != null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedContent(
            targetState = Pair(isMediaActive, isExpanded),
            transitionSpec = {
                (fadeIn(animationSpec = tween(240)) + scaleIn(initialScale = 0.96f, animationSpec = tween(240)))
                    .togetherWith(fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.96f, animationSpec = tween(180)))
            },
            label = "nav_layout_mode"
        ) { (mediaActive, expanded) ->
            if (mediaActive && !expanded) {
                // ── Mode 1: Collapsed Single-Row SimpMusic Layout (Image 4 exact) ────
                // [ 54dp Circular Glass Tab Button ]  +  [ MiniPlayer (weight 1f) ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left: Compact Circular Glass Navigation Button
                    LiquidGlassCircleButton(
                        icon = activeIcon,
                        contentDescription = "Navigate / Expand Tabs",
                        onClick = {
                            if (currentRoute != "home") {
                                safeNavigate("home")
                            } else {
                                isExpanded = true
                            }
                        },
                        backdrop = backdrop,
                        tiltX = tilt.tiltX,
                        tiltY = tilt.tiltY,
                        modifier = Modifier.size(54.dp)
                    )

                    // Right: MiniPlayer filling the rest of the bottom row
                    MiniPlayer(
                        track = track,
                        isPlaying = isPlaying,
                        progressProvider = progressProvider,
                        dominantColor = dominantColor,
                        onTogglePlayback = onTogglePlayback,
                        onSkipNext = onSkipNext,
                        onSkipPrevious = onSkipPrevious,
                        onLike = onLike,
                        isLiked = isLiked,
                        onClick = onOpenNowPlaying,
                        onClose = onClosePlayer,
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    )
                }
            } else if (mediaActive && expanded) {
                // ── Mode 2: Expanded with Playing Music (MiniPlayer above full bar) ──
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // MiniPlayer full width
                    MiniPlayer(
                        track = track,
                        isPlaying = isPlaying,
                        progressProvider = progressProvider,
                        dominantColor = dominantColor,
                        onTogglePlayback = onTogglePlayback,
                        onSkipNext = onSkipNext,
                        onSkipPrevious = onSkipPrevious,
                        onLike = onLike,
                        isLiked = isLiked,
                        onClick = onOpenNowPlaying,
                        onClose = onClosePlayer,
                        backdrop = backdrop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    )

                    // Full Navigation Bar: Capsule [Home | Library] + Circular Search FAB
                    ExpandedNavBarRow(
                        activeIndex = activeIndex,
                        onSelectTab = { index ->
                            when (index) {
                                0 -> safeNavigate("home")
                                1 -> safeNavigate("library")
                            }
                        },
                        onSearchClick = { safeNavigate("search") },
                        backdrop = backdrop,
                        tiltX = tilt.tiltX,
                        tiltY = tilt.tiltY
                    )
                }
            } else {
                // ── Mode 3: Idle / No Music Playing (Image 6 exact) ───────────────────
                // [ Capsule [Home | Library] ]  +  [ Search FAB ]
                ExpandedNavBarRow(
                    activeIndex = activeIndex,
                    onSelectTab = { index ->
                        when (index) {
                            0 -> safeNavigate("home")
                            1 -> safeNavigate("library")
                        }
                    },
                    onSearchClick = { safeNavigate("search") },
                    backdrop = backdrop,
                    tiltX = tilt.tiltX,
                    tiltY = tilt.tiltY
                )
            }
        }
    }
}

/**
 * Authentic SimpMusic Expanded Bottom Bar:
 * [ Left: Liquid Glass Capsule with Home & Library ] + [ Right: 56dp Circular Glass Search FAB ]
 */
@Composable
private fun ExpandedNavBarRow(
    activeIndex: Int,
    onSelectTab: (Int) -> Unit,
    onSearchClick: () -> Unit,
    backdrop: Backdrop?,
    tiltX: Float,
    tiltY: Float,
    modifier: Modifier = Modifier
) {
    val hazeState = LocalHazeState.current
    val capsuleShape = RoundedCornerShape(percent = 50)
    val tabWidth = 88.dp
    val capsuleHeight = 56.dp
    val blobHeight = 48.dp
    val tabCount = 2
    val barInset = 5.dp

    // Morphing spring indicator position: 0 = Home (x = 5dp), 1 = Library (x = 93dp)
    val targetIndex = activeIndex.coerceIn(0, 1)
    val targetOffset = if (targetIndex == 0) barInset.value else (barInset.value + tabWidth.value)

    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
        label = "tab_blob_offset"
    )

    // Glass Modifier for the main capsule
    val capsuleGlassModifier = if (backdrop != null) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { capsuleShape },
            highlight = {
                Highlight(
                    style = com.kyant.backdrop.highlight.HighlightStyle.Default(
                        angle = 45f + (tiltX * 35f) + (tiltY * 20f),
                        falloff = 1.0f
                    ),
                    alpha = 0.85f
                )
            },
            shadow = { Shadow(radius = 8.dp, color = Color.Black.copy(alpha = 0.24f)) },
            innerShadow = { InnerShadow(radius = 6.dp, alpha = 0.22f, color = Color.White) },
            effects = {
                vibrancy()
                colorControls(brightness = 0.04f, contrast = 1.05f, saturation = 1.35f)
                blur(1.5.dp.toPx())
                lens(
                    refractionHeight = 8.dp.toPx(),
                    refractionAmount = 16.dp.toPx(),
                    depthEffect = true,
                    chromaticAberration = true
                )
            },
            onDrawSurface = {
                drawRect(Color.Black.copy(alpha = 0.16f))
                drawRect(Color.White.copy(alpha = 0.04f))
            }
        )
    } else {
        Modifier
            .hazeEffect(
                state = hazeState ?: LocalHazeState.current,
                style = HazeStyle(
                    backgroundColor = Color.Transparent,
                    tint = HazeDefaults.tint(Color.White.copy(alpha = 0.04f)),
                    blurRadius = 4.dp
                )
            )
            .background(Color.Black.copy(alpha = 0.25f))
    }

    Row(
        modifier = modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── 1. Capsule Container: Home & Library ─────────────────────────────
        val capsuleWidth = (tabWidth * tabCount) + (barInset * 2)

        Box(
            modifier = Modifier
                .size(width = capsuleWidth, height = capsuleHeight)
                .then(capsuleGlassModifier)
                .border(
                    width = 0.85.dp,
                    brush = createSpecularRimBrush(
                        tiltX = tiltX,
                        tiltY = tiltY,
                        baseAlpha = 0.55f,
                        secondaryAlpha = 0.25f
                    ),
                    shape = capsuleShape
                )
                .clip(capsuleShape),
            contentAlignment = Alignment.CenterStart
        ) {
            // Frosted Sliding Blob Selection Indicator
            Box(
                modifier = Modifier
                    .offset(x = animatedOffset.dp)
                    .size(width = tabWidth, height = blobHeight)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color.White.copy(alpha = 0.18f))
                    .border(
                        width = 0.75.dp,
                        color = Color.White.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(percent = 50)
                    )
            )

            // Crisp Tab Icons + Labels
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = barInset),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Tab
                SimpTabItem(
                    icon = SimpIcons.Home,
                    label = "Home",
                    isSelected = targetIndex == 0,
                    width = tabWidth,
                    onClick = { onSelectTab(0) }
                )

                // Library Tab
                SimpTabItem(
                    icon = SimpIcons.QueueMusic,
                    label = "Library",
                    isSelected = targetIndex == 1,
                    width = tabWidth,
                    onClick = { onSelectTab(1) }
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ── 2. Search Floating Circular Glass FAB ────────────────────────────
        LiquidGlassCircleButton(
            icon = SimpIcons.Search,
            contentDescription = "Search",
            isSelected = activeIndex == 2,
            onClick = onSearchClick,
            backdrop = backdrop,
            tiltX = tiltX,
            tiltY = tiltY,
            modifier = Modifier.size(capsuleHeight)
        )
    }
}

/**
 * Individual Tab Item inside the Capsule (Icon + Label).
 */
@Composable
private fun SimpTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 600f),
        label = "tab_item_scale"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.62f),
        animationSpec = tween(200),
        label = "tab_item_color"
    )

    Column(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor
            )
        )
    }
}

/**
 * Authentic SimpMusic Circular Floating Liquid Glass Button.
 */
@Composable
fun LiquidGlassCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    backdrop: Backdrop? = LocalBackdrop.current,
    tiltX: Float = 0f,
    tiltY: Float = 0f
) {
    val hazeState = LocalHazeState.current
    val circleShape = CircleShape
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 600f),
        label = "circle_btn_scale"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.88f),
        animationSpec = tween(200),
        label = "circle_btn_color"
    )

    val glassModifier = if (backdrop != null) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { circleShape },
            highlight = {
                Highlight(
                    style = com.kyant.backdrop.highlight.HighlightStyle.Default(
                        angle = 45f + (tiltX * 35f) + (tiltY * 20f),
                        falloff = 1.0f
                    ),
                    alpha = 0.85f
                )
            },
            shadow = { Shadow(radius = 8.dp, color = Color.Black.copy(alpha = 0.22f)) },
            innerShadow = { InnerShadow(radius = 6.dp, alpha = 0.22f, color = Color.White) },
            effects = {
                vibrancy()
                colorControls(brightness = 0.04f, contrast = 1.05f, saturation = 1.35f)
                blur(1.5.dp.toPx())
                lens(
                    refractionHeight = 8.dp.toPx(),
                    refractionAmount = 16.dp.toPx(),
                    depthEffect = true,
                    chromaticAberration = true
                )
            },
            onDrawSurface = {
                drawRect(Color.Black.copy(alpha = 0.16f))
                drawRect(Color.White.copy(alpha = 0.04f))
            }
        )
    } else {
        Modifier
            .hazeEffect(
                state = hazeState ?: LocalHazeState.current,
                style = HazeStyle(
                    backgroundColor = Color.Transparent,
                    tint = HazeDefaults.tint(Color.White.copy(alpha = 0.04f)),
                    blurRadius = 4.dp
                )
            )
            .background(Color.Black.copy(alpha = 0.25f))
    }

    Box(
        modifier = modifier
            .scale(scale)
            .then(glassModifier)
            .border(
                width = 0.85.dp,
                brush = createSpecularRimBrush(
                    tiltX = tiltX,
                    tiltY = tiltY,
                    baseAlpha = 0.55f,
                    secondaryAlpha = 0.25f
                ),
                shape = circleShape
            )
            .clip(circleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
