package com.example.ecodot.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.ecodot.LocalHazeState
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
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

/**
 * Floating 3-Squircle Realistic iOS Liquid Glass Navigation Bar (Home, Search, Playlist).
 * Features:
 *  1. Liquid Morphing Indicator: Active squircle indicator stretches organically like a fluid drop
 *     between tabs before snapping cleanly with spring physics to the target.
 *  2. Gyroscope-Reactive Specular Rim: Subtly shifts angle and highlight gradients as the phone tilts.
 *  3. Crystal-Clear Liquid Glass Optics: Pure optical refraction through AGSL lens shader + vibrancy + inner reflection.
 */
@Composable
fun SimpBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = LocalBackdrop.current
) {
    val hazeState = LocalHazeState.current
    val hapticFeedback = LocalHapticFeedback.current
    val tilt = LocalTiltState.current
    var lastNavTime by remember { mutableLongStateOf(0L) }

    val safeNavigate: (String) -> Unit = { route ->
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onNavigate(route)
    }

    // Determine target index: 0 = Home, 1 = Search, 2 = Library
    val targetIndex = when (currentRoute) {
        "home" -> 0
        "search" -> 1
        "library" -> 2
        else -> 0
    }

    var oldIndex by remember { mutableIntStateOf(targetIndex) }
    val isMovingRight = targetIndex > oldIndex

    LaunchedEffect(targetIndex) {
        oldIndex = targetIndex
    }

    // Tab slots layout parameters
    // Each button is 58dp wide, spaced by 16dp -> pitch = 74dp
    // Positions: 0dp, 74dp, 148dp. Total row width = 206dp.
    val targetLeft = targetIndex * 74f
    val targetRight = targetIndex * 74f + 58f

    // Asymmetric fluid spring physics:
    // Leading edge races forward with high stiffness; trailing edge lags behind with damping
    val leftSpec = if (isMovingRight) {
        spring<Float>(dampingRatio = 0.72f, stiffness = 280f)
    } else {
        spring<Float>(dampingRatio = 0.65f, stiffness = 480f)
    }

    val rightSpec = if (isMovingRight) {
        spring<Float>(dampingRatio = 0.65f, stiffness = 480f)
    } else {
        spring<Float>(dampingRatio = 0.72f, stiffness = 280f)
    }

    val animatedLeft by animateFloatAsState(
        targetValue = targetLeft,
        animationSpec = leftSpec,
        label = "drop_left"
    )
    val animatedRight by animateFloatAsState(
        targetValue = targetRight,
        animationSpec = rightSpec,
        label = "drop_right"
    )

    // Fluid volume preservation (squash & stretch)
    val dropWidth = (animatedRight - animatedLeft).coerceAtLeast(58f)
    val stretch = ((dropWidth - 58f) / 60f).coerceIn(0f, 1f)
    val dropHeight = 58f - (stretch * 6f) // Compresses from 58dp down to 52dp at peak flight
    val cornerRadius = (20f - stretch * 2f).dp
    val dropShape = RoundedCornerShape(cornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(width = 206.dp, height = 58.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // ── 1. Base Glass Squircles (3 unselected resting slots) ──────────
            for (i in 0..2) {
                val slotX = (i * 74).dp
                val baseShape = RoundedCornerShape(20.dp)
                val baseGlassModifier = if (backdrop != null) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { baseShape },
                        highlight = {
                            Highlight(
                                style = HighlightStyle.Default(
                                    angle = 45f + (tilt.tiltX * 30f) + (tilt.tiltY * 15f)
                                ),
                                alpha = 0.40f
                            )
                        },
                        shadow = {
                            Shadow(radius = 4.dp, color = Color.Black.copy(alpha = 0.12f))
                        },
                        innerShadow = {
                            InnerShadow(radius = 5.dp, alpha = 0.16f, color = Color.White)
                        },
                        effects = {
                            blur(2.dp.toPx())
                        },
                        onDrawSurface = {
                            drawRect(Color.Black.copy(alpha = 0.04f))
                            drawRect(Color.White.copy(alpha = 0.02f))
                        }
                    )
                } else {
                    Modifier
                        .hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = Color.Transparent,
                                tint = HazeDefaults.tint(Color.White.copy(alpha = 0.02f)),
                                blurRadius = 4.dp,
                                noiseFactor = 0f
                            )
                        )
                        .background(Color.Black.copy(alpha = 0.05f))
                }

                Box(
                    modifier = Modifier
                        .offset(x = slotX, y = 0.dp)
                        .size(58.dp)
                        .then(baseGlassModifier)
                        .border(
                            width = 0.75.dp,
                            brush = createSpecularRimBrush(
                                tiltX = tilt.tiltX,
                                tiltY = tilt.tiltY,
                                baseAlpha = 0.35f,
                                secondaryAlpha = 0.15f
                            ),
                            shape = baseShape
                        )
                )
            }

            // ── 2. Active Liquid Morphing Droplet ─────────────────────────────
            val dropOffsetY = ((58f - dropHeight) / 2f).dp
            val liquidGlassModifier = if (backdrop != null) {
                Modifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { dropShape },
                    highlight = {
                        Highlight(
                            style = HighlightStyle.Default(
                                angle = 45f + (tilt.tiltX * 35f) + (tilt.tiltY * 20f)
                            ),
                            alpha = 0.92f
                        )
                    },
                    shadow = {
                        Shadow(radius = 10.dp, color = Color.Black.copy(alpha = 0.30f))
                    },
                    innerShadow = {
                        InnerShadow(radius = 7.dp, alpha = 0.42f, color = Color.White)
                    },
                    effects = {
                        blur(2.dp.toPx())
                        colorControls(
                            brightness = 0.05f,
                            contrast = 1.08f,
                            saturation = 1.25f
                        )
                    },
                    onDrawSurface = {
                        drawRect(Color.Black.copy(alpha = 0.07f))
                        drawRect(Color.White.copy(alpha = 0.09f))
                    }
                )
            } else {
                Modifier
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = Color.Transparent,
                            tint = HazeDefaults.tint(Color(0xFFE7D7FD).copy(alpha = 0.08f)),
                            blurRadius = 4.dp,
                            noiseFactor = 0f
                        )
                    )
                    .background(Color.Black.copy(alpha = 0.10f))
            }

            Box(
                modifier = Modifier
                    .offset(x = animatedLeft.dp, y = dropOffsetY)
                    .size(width = dropWidth.dp, height = dropHeight.dp)
                    .then(liquidGlassModifier)
                    .border(
                        width = 0.95.dp,
                        brush = createSpecularRimBrush(
                            tiltX = tilt.tiltX,
                            tiltY = tilt.tiltY,
                            baseAlpha = 0.75f,
                            secondaryAlpha = 0.40f
                        ),
                        shape = dropShape
                    )
                    .drawWithContent {
                        drawContent()
                        // Fluid droplet glow accent
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFE7D7FD).copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.width * 0.75f
                            ),
                            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                            blendMode = BlendMode.Plus
                        )
                    }
            )

            // ── 3. Interactive Icon Buttons (Home, Search, Library) ───────────
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Button
                LiquidGlassIconButton(
                    icon = SimpIcons.Home,
                    contentDescription = "Home",
                    isSelected = targetIndex == 0,
                    onClick = { safeNavigate("home") }
                )

                // Search Button
                LiquidGlassIconButton(
                    icon = SimpIcons.Search,
                    contentDescription = "Search",
                    isSelected = targetIndex == 1,
                    onClick = { safeNavigate("search") }
                )

                // Library / Playlist Button
                LiquidGlassIconButton(
                    icon = SimpIcons.QueueMusic,
                    contentDescription = "Playlist",
                    isSelected = targetIndex == 2,
                    onClick = { safeNavigate("library") }
                )
            }
        }
    }
}

/**
 * Interactive squircle icon touch target with tactile press spring physics.
 */
@Composable
private fun LiquidGlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var touchPos by remember { mutableStateOf(Offset.Zero) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 800f),
        label = "icon_scale"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.60f),
        animationSpec = tween(150),
        label = "icon_color"
    )

    Box(
        modifier = modifier
            .size(58.dp)
            .scale(scale)
            .drawWithContent {
                drawContent()
                if (isPressed) {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            radius = size.minDimension * 0.85f
                        ),
                        cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                        blendMode = BlendMode.Plus
                    )
                }
            }
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
            modifier = Modifier.size(26.dp)
        )
    }
}
