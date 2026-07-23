package com.example.ecodot.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.example.ecodot.ui.theme.animatedCombinedClickable
import com.kashif_e.backdrop.Backdrop
import com.kashif_e.backdrop.backdrops.LayerBackdrop
import com.kashif_e.backdrop.backdrops.layerBackdrop
import com.kashif_e.backdrop.backdrops.rememberBackdrop
import com.kashif_e.backdrop.backdrops.rememberCombinedBackdrop
import com.kashif_e.backdrop.backdrops.rememberLayerBackdrop
import com.kashif_e.backdrop.drawBackdrop
import com.kashif_e.backdrop.effects.blur
import com.kashif_e.backdrop.effects.lens
import com.kashif_e.backdrop.effects.vibrancy
import com.kashif_e.backdrop.effects.colorControls
import com.kashif_e.backdrop.highlight.Highlight
import com.kashif_e.backdrop.shadow.InnerShadow
import com.kashif_e.backdrop.shadow.Shadow
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

/**
 * CompositionLocal to propagate the backdrop state down the UI tree for true refraction blur.
 */
val LocalBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/**
 * Drag Gesture Inspector to track touch events and coordinate squashing/stretching physics.
 */
suspend fun PointerInputScope.inspectDragGestures(
    onDragStart: (down: PointerInputChange) -> Unit = {},
    onDragEnd: (change: PointerInputChange) -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
) {
    awaitEachGesture {
        val initialDown = awaitFirstDown(false, PointerEventPass.Initial)
        val down = awaitFirstDown(false)
        val drag = initialDown

        onDragStart(down)
        onDrag(drag, Offset.Zero)
        val upEvent = drag(
            pointerId = drag.id,
            onDrag = { onDrag(it, it.positionChange()) }
        )
        if (upEvent == null) {
            onDragCancel()
        } else {
            onDragEnd(upEvent)
        }
    }
}

private suspend inline fun AwaitPointerEventScope.drag(
    pointerId: PointerId,
    onDrag: (PointerInputChange) -> Unit
): PointerInputChange? {
    val isPointerUp = currentEvent.changes.fastFirstOrNull { it.id == pointerId }?.pressed != true
    if (isPointerUp) return null
    var pointer = pointerId
    while (true) {
        val change = awaitDragOrUp(pointer) ?: return null
        if (change.isConsumed) return null
        if (change.changedToUpIgnoreConsumed()) return change
        onDrag(change)
        pointer = change.id
    }
}

private suspend inline fun AwaitPointerEventScope.awaitDragOrUp(
    pointerId: PointerId
): PointerInputChange? {
    var pointer = pointerId
    while (true) {
        val event = awaitPointerEvent()
        val dragEvent = event.changes.fastFirstOrNull { it.id == pointer } ?: return null
        if (dragEvent.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.fastFirstOrNull { it.pressed }
            if (otherDown == null) {
                return dragEvent
            } else {
                pointer = otherDown.id
            }
        } else {
            val hasDragged = dragEvent.previousPosition != dragEvent.position
            if (hasDragged) return dragEvent
        }
    }
}

/**
 * Damped Drag Animation helper for squashing, stretching, and physical spring animations.
 */
class DampedDragAnimation(
    private val animationScope: CoroutineScope,
    val initialValue: Float,
    val valueRange: ClosedRange<Float>,
    val visibilityThreshold: Float,
    val initialScale: Float,
    val pressedScale: Float,
    val onDragStarted: DampedDragAnimation.(position: Offset) -> Unit,
    val onDragStopped: DampedDragAnimation.() -> Unit,
    val onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit,
) {
    private val valueAnimationSpec = spring<Float>(1f, 1000f, visibilityThreshold)
    private val velocityAnimationSpec = spring<Float>(0.5f, 300f, visibilityThreshold * 10f)
    private val pressProgressAnimationSpec = spring<Float>(1f, 1000f, 0.001f)
    private val scaleXAnimationSpec = spring<Float>(0.6f, 250f, 0.001f)
    private val scaleYAnimationSpec = spring<Float>(0.7f, 250f, 0.001f)

    private val valueAnimation = Animatable(initialValue, visibilityThreshold)
    private val velocityAnimation = Animatable(0f, 5f)
    private val pressProgressAnimation = Animatable(0f, 0.001f)
    private val scaleXAnimation = Animatable(initialScale, 0.001f)
    private val scaleYAnimation = Animatable(initialScale, 0.001f)

    private val mutatorMutex = androidx.compose.foundation.MutatorMutex()
    private val velocityTracker = VelocityTracker()

    val value: Float get() = valueAnimation.value
    val progress: Float get() = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    val targetValue: Float get() = valueAnimation.targetValue
    val pressProgress: Float get() = pressProgressAnimation.value
    val scaleX: Float get() = scaleXAnimation.value
    val scaleY: Float get() = scaleYAnimation.value
    val velocity: Float get() = velocityAnimation.value

    val modifier: Modifier = Modifier.pointerInput(Unit) {
        inspectDragGestures(
            onDragStart = { down ->
                onDragStarted(down.position)
                press()
            },
            onDragEnd = {
                onDragStopped()
                release()
            },
            onDragCancel = {
                onDragStopped()
                release()
            }
        ) { change, dragAmount ->
            onDrag(this.size, dragAmount)
        }
    }

    fun press() {
        velocityTracker.resetTracking()
        animationScope.launch {
            launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(pressedScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(pressedScale, scaleYAnimationSpec) }
        }
    }

    fun release() {
        animationScope.launch {
            delay(16L)
            if (value != targetValue) {
                val threshold = (valueRange.endInclusive - valueRange.start) * 0.025f
                snapshotFlow { valueAnimation.value }
                    .filter { abs(it - valueAnimation.targetValue) < threshold }
                    .first()
            }
            launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(initialScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(initialScale, scaleYAnimationSpec) }
        }
    }

    fun updateValue(value: Float) {
        val target = value.coerceIn(valueRange)
        animationScope.launch {
            launch { valueAnimation.animateTo(target, valueAnimationSpec) { updateVelocity() } }
        }
    }

    fun animateToValue(value: Float) {
        animationScope.launch {
            mutatorMutex.mutate {
                press()
                val target = value.coerceIn(valueRange)
                launch { valueAnimation.animateTo(target, valueAnimationSpec) }
                if (velocity != 0f) {
                    launch { velocityAnimation.animateTo(0f, velocityAnimationSpec) }
                }
                release()
            }
        }
    }

    private fun updateVelocity() {
        velocityTracker.addPosition(
            System.currentTimeMillis(),
            Offset(value, 0f)
        )
        val targetVelocity = velocityTracker.calculateVelocity().x / (valueRange.endInclusive - valueRange.start)
        animationScope.launch { velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec) }
    }
}

/**
 * Interactive Highlight class helper for surface press gestures.
 */
class InteractiveHighlight(
    val animationScope: CoroutineScope,
    val position: (size: Size, offset: Offset) -> Offset = { _, offset -> offset }
) {
    private val pressProgressAnimationSpec = spring<Float>(0.5f, 300f, 0.001f)
    private val positionAnimationSpec = spring<Offset>(0.5f, 300f, Offset.VisibilityThreshold)

    private val pressProgressAnimation = Animatable(0f, 0.001f)
    private val positionAnimation = Animatable(Offset.Zero, Offset.VectorConverter, Offset.VisibilityThreshold)

    private var startPosition = Offset.Zero
    val pressProgress: Float get() = pressProgressAnimation.value
    val offset: Offset get() = positionAnimation.value - startPosition

    val modifier: Modifier = Modifier.drawWithContent {
        val progress = pressProgressAnimation.value
        if (progress > 0f) {
            drawRect(
                Color.White.copy(0.25f * progress),
                blendMode = BlendMode.Plus
            )
        }
        drawContent()
    }

    val gestureModifier: Modifier = Modifier.pointerInput(animationScope) {
        inspectDragGestures(
            onDragStart = { down ->
                startPosition = down.position
                animationScope.launch {
                    launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
                    launch { positionAnimation.snapTo(startPosition) }
                }
            },
            onDragEnd = {
                animationScope.launch {
                    launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
                    launch { positionAnimation.animateTo(startPosition, positionAnimationSpec) }
                }
            },
            onDragCancel = {
                animationScope.launch {
                    launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
                    launch { positionAnimation.animateTo(startPosition, positionAnimationSpec) }
                }
            }
        ) { change, _ ->
            animationScope.launch { positionAnimation.snapTo(change.position) }
        }
    }
}

/**
 * Reusable Liquid Glass modifier with true backdrop-sampling refraction when Backdrop is provided.
 * Falls back gracefully to Haze / solid backgrounds if no backdrop state exists.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0x22FFFFFF),
    specularAlpha: Float = 0.38f,
    borderWidth: Dp = 0.8.dp,
    elevation: Dp = 8.dp,
    hazeState: HazeState? = null,
    tintColor: Color = Color(0xFF141620),
    blurRadius: Dp = 20.dp,
    parentBackdrop: Backdrop? = null
): Modifier {
    val base = this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.4f),
            spotColor = Color.Black.copy(alpha = 0.6f)
        )
        .clip(shape)

    val withBlur = if (parentBackdrop != null) {
        base.drawBackdrop(
            backdrop = parentBackdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(blurRadius.toPx())
                lens(10f.dp.toPx(), 20f.dp.toPx(), depthEffect = true, chromaticAberration = true)
            },
            highlight = { Highlight.Plain },
            onDrawSurface = {
                drawRect(tintColor.copy(alpha = 0.4f))
            }
        )
    } else if (hazeState != null) {
        val glassStyle = HazeStyle(
            backgroundColor = tintColor.copy(alpha = 0.48f),
            tint = HazeDefaults.tint(tintColor.copy(alpha = 0.28f)),
            blurRadius = blurRadius,
            noiseFactor = 0.06f
        )
        base.hazeEffect(state = hazeState, style = glassStyle)
    } else {
        base.background(
            color = tintColor.copy(alpha = 0.75f),
            shape = shape
        )
    }

    return withBlur.border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = specularAlpha),
                Color.White.copy(alpha = specularAlpha * 0.5f),
                Color.White.copy(alpha = 0.04f),
                Color.Black.copy(alpha = 0.25f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = shape
    )
}

/**
 * A reusable Liquid Glass container card.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color(0x22FFFFFF),
    tintColor: Color = Color(0xFF141620),
    specularAlpha: Float = 0.35f,
    elevation: Dp = 6.dp,
    hazeState: HazeState? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val parentBackdrop = LocalBackdrop.current
    val cardModifier = modifier.liquidGlass(
        shape = shape,
        backgroundColor = backgroundColor,
        tintColor = tintColor,
        specularAlpha = specularAlpha,
        elevation = elevation,
        hazeState = hazeState,
        parentBackdrop = parentBackdrop
    )

    val interactiveModifier = if (onClick != null || onLongClick != null) {
        cardModifier.animatedCombinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick
        )
    } else {
        cardModifier
    }

    Box(
        modifier = interactiveModifier,
        content = content
    )
}

/**
 * High-End Motion Liquid Glass Card Container.
 * Features dynamic continuous fluid motion gradients flowing beneath/inside the glass,
 * AGSL refraction shader rendering, and specular highlight edges.
 */
@Composable
fun MotionLiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color = Color(0xFF141628),
    fluidColor: Color = Color(0xFF7C3AED),
    specularAlpha: Float = 0.42f,
    elevation: Dp = 10.dp,
    hazeState: HazeState? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_motion_glass")

    val motionX by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "motion_x"
    )
    val motionY by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "motion_y"
    )

    val parentBackdrop = LocalBackdrop.current
    val baseModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.45f),
            spotColor = Color.Black.copy(alpha = 0.65f)
        )
        .clip(shape)

    val withBlur = if (parentBackdrop != null) {
        baseModifier.drawBackdrop(
            backdrop = parentBackdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(24.dp.toPx())
                lens(14f.dp.toPx(), 28f.dp.toPx(), depthEffect = true, chromaticAberration = true)
            },
            highlight = { Highlight.Plain },
            onDrawSurface = {
                drawRect(tintColor.copy(alpha = 0.4f))
            }
        )
    } else if (hazeState != null) {
        val glassStyle = HazeStyle(
            backgroundColor = tintColor.copy(alpha = 0.50f),
            tint = HazeDefaults.tint(tintColor.copy(alpha = 0.30f)),
            blurRadius = 24.dp,
            noiseFactor = 0.05f
        )
        baseModifier.hazeEffect(state = hazeState, style = glassStyle)
    } else {
        baseModifier.background(tintColor.copy(alpha = 0.76f), shape)
    }

    val interactiveModifier = if (onClick != null || onLongClick != null) {
        withBlur.animatedCombinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick
        )
    } else {
        withBlur
    }

    Box(
        modifier = interactiveModifier
            .drawWithCache {
                val w = size.width
                val h = size.height
                val centerOffset = Offset(w * (0.3f + motionX), h * (0.25f + motionY))

                onDrawBehind {
                    // Fluid moving liquid ambient light blob inside glass
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                fluidColor.copy(alpha = 0.28f),
                                fluidColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = centerOffset,
                            radius = w * 0.75f
                        ),
                        radius = w * 0.75f,
                        center = centerOffset
                    )

                    // Top-left specular rim light highlight stroke
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = specularAlpha),
                                Color.White.copy(alpha = specularAlpha * 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        )
                    )
                }
            }
            .border(
                width = 0.8.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = specularAlpha),
                        Color.White.copy(alpha = specularAlpha * 0.4f),
                        Color.White.copy(alpha = 0.03f),
                        Color.Black.copy(alpha = 0.22f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            ),
        content = content
    )
}

/**
 * Authentic iOS-style Cupertino Switch upgraded with true squashing, stretching, and backdrop-sampling lens refraction!
 */
@Composable
fun IOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val parentBackdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val isLightTheme = false // Force dark theme styled iOS switches
    val accentColor = Color(0xFF30D158) // iOS Green
    val trackColor = Color(0xFF787880).copy(0.36f)

    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val dragWidth = with(density) { 20.dp.toPx() }
    val animationScope = rememberCoroutineScope()
    var didDrag by remember { mutableStateOf(false) }
    var fraction by remember { mutableStateOf(if (checked) 1f else 0f) }

    val dampedDragAnimation = remember(animationScope) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1.4f,
            onDragStarted = {},
            onDragStopped = {
                if (didDrag) {
                    fraction = if (targetValue >= 0.5f) 1f else 0f
                    onCheckedChange(fraction == 1f)
                    didDrag = false
                } else {
                    fraction = if (checked) 0f else 1f
                    onCheckedChange(fraction == 1f)
                }
            },
            onDrag = { _, dragAmount ->
                if (!didDrag) {
                    didDrag = dragAmount.x != 0f
                }
                val delta = dragAmount.x / dragWidth
                fraction = if (isLtr) (fraction + delta).coerceIn(0f, 1f)
                else (fraction - delta).coerceIn(0f, 1f)
            }
        )
    }

    LaunchedEffect(dampedDragAnimation) {
        snapshotFlow { fraction }
            .collectLatest { frac -> dampedDragAnimation.updateValue(frac) }
    }

    LaunchedEffect(checked) {
        snapshotFlow { checked }
            .collectLatest { isChecked ->
                val target = if (isChecked) 1f else 0f
                if (target != fraction) {
                    fraction = target
                    dampedDragAnimation.animateToValue(target)
                }
            }
    }

    val trackBackdrop = rememberLayerBackdrop()

    Box(
        modifier = modifier.size(54.dp, 30.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .layerBackdrop(trackBackdrop)
                .clip(RoundedCornerShape(100.dp))
                .drawBehind {
                    val frac = dampedDragAnimation.value
                    drawRect(androidx.compose.ui.graphics.lerp(trackColor, accentColor, frac))
                }
                .fillMaxSize()
        )

        Box(
            modifier = Modifier
                .graphicsLayer {
                    val frac = dampedDragAnimation.value
                    val padding = 3.dp.toPx()
                    translationX = if (isLtr) {
                        androidx.compose.ui.util.lerp(padding, padding + dragWidth, frac)
                    } else {
                        androidx.compose.ui.util.lerp(-padding, -(padding + dragWidth), frac)
                    }
                }
                .semantics { role = Role.Switch }
                .then(dampedDragAnimation.modifier)
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        parentBackdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = dampedDragAnimation.pressProgress
                            val scaleX = androidx.compose.ui.util.lerp(2f / 3f, 0.75f, progress)
                            val scaleY = androidx.compose.ui.util.lerp(0f, 0.75f, progress)
                            scale(scaleX, scaleY) {
                                drawBackdrop()
                            }
                        }
                    ),
                    shape = { RoundedCornerShape(100.dp) },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        blur(6.dp.toPx() * (1f - progress))
                        lens(
                            4.dp.toPx() * progress,
                            8.dp.toPx() * progress,
                            chromaticAberration = true
                        )
                    },
                    highlight = {
                        val progress = dampedDragAnimation.pressProgress
                        Highlight.Ambient.copy(
                            width = Highlight.Ambient.width / 1.5f,
                            blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                            alpha = progress
                        )
                    },
                    shadow = {
                        Shadow(radius = 3.dp, color = Color.Black.copy(alpha = 0.06f))
                    },
                    innerShadow = {
                        val progress = dampedDragAnimation.pressProgress
                        InnerShadow(radius = 3.dp * progress, alpha = progress)
                    },
                    layerBlock = {
                        scaleX = dampedDragAnimation.scaleX
                        scaleY = dampedDragAnimation.scaleY
                        val velocity = dampedDragAnimation.velocity / 60f
                        scaleX /= 1f - (velocity * 0.75f).coerceIn(-0.15f, 0.15f)
                        scaleY *= 1f - (velocity * 0.25f).coerceIn(-0.15f, 0.15f)
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        drawRect(Color.White.copy(alpha = 1f - progress))
                    }
                )
                .size(24.dp)
        )
    }
}

/**
 * LiquidSlider with real-time squashing, stretching, and physical dragging feedback.
 */
@Composable
fun LiquidSlider(
    value: () -> Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    visibilityThreshold: Float = 0.01f,
    modifier: Modifier = Modifier
) {
    val parentBackdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val trackColor = Color(0xFF787880).copy(0.36f)
    val accentColor = Color(0xFF0091FF) // iOS Blue

    val trackBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        val trackWidth = constraints.maxWidth
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var didDrag by remember { mutableStateOf(false) }

        val dampedDragAnimation = remember(animationScope) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = value(),
                valueRange = valueRange,
                visibilityThreshold = visibilityThreshold,
                initialScale = 1f,
                pressedScale = 1.4f,
                onDragStarted = {},
                onDragStopped = {
                    if (didDrag) {
                        onValueChange(targetValue)
                    }
                },
                onDrag = { _, dragAmount ->
                    if (!didDrag) {
                        didDrag = dragAmount.x != 0f
                    }
                    val delta = (valueRange.endInclusive - valueRange.start) * (dragAmount.x / trackWidth)
                    onValueChange(
                        if (isLtr) (targetValue + delta).coerceIn(valueRange)
                        else (targetValue - delta).coerceIn(valueRange)
                    )
                }
            )
        }

        LaunchedEffect(dampedDragAnimation) {
            snapshotFlow { value() }
                .collectLatest { valVal ->
                    if (dampedDragAnimation.targetValue != valVal) {
                        dampedDragAnimation.updateValue(valVal)
                    }
                }
        }

        Box(modifier = Modifier.layerBackdrop(trackBackdrop)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(trackColor)
                    .pointerInput(animationScope) {
                        detectTapGestures { position ->
                            val delta = (valueRange.endInclusive - valueRange.start) * (position.x / trackWidth)
                            val target = (if (isLtr) valueRange.start + delta else valueRange.endInclusive - delta).coerceIn(valueRange)
                            dampedDragAnimation.animateToValue(target)
                            onValueChange(target)
                        }
                    }
                    .height(6.dp)
                    .fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(accentColor)
                    .height(6.dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val width = (constraints.maxWidth * dampedDragAnimation.progress).fastRoundToInt()
                        layout(width, placeable.height) {
                            placeable.place(0, 0)
                        }
                    }
            )
        }

        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = (-size.width / 2f + trackWidth * dampedDragAnimation.progress)
                        .fastCoerceIn(-size.width / 4f, trackWidth - size.width * 3f / 4f) * if (isLtr) 1f else -1f
                }
                .then(dampedDragAnimation.modifier)
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        parentBackdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = dampedDragAnimation.pressProgress
                            val scaleX = androidx.compose.ui.util.lerp(2f / 3f, 1f, progress)
                            val scaleY = androidx.compose.ui.util.lerp(0f, 1f, progress)
                            scale(scaleX, scaleY) {
                                drawBackdrop()
                            }
                        }
                    ),
                    shape = { RoundedCornerShape(100.dp) },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        blur(6.dp.toPx() * (1f - progress))
                        lens(
                            8.dp.toPx() * progress,
                            12.dp.toPx() * progress,
                            chromaticAberration = true
                        )
                    },
                    highlight = {
                        val progress = dampedDragAnimation.pressProgress
                        Highlight.Ambient.copy(
                            width = Highlight.Ambient.width / 1.5f,
                            blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                            alpha = progress
                        )
                    },
                    shadow = {
                        Shadow(radius = 3.dp, color = Color.Black.copy(alpha = 0.05f))
                    },
                    innerShadow = {
                        val progress = dampedDragAnimation.pressProgress
                        InnerShadow(radius = 3.dp * progress, alpha = progress)
                    },
                    layerBlock = {
                        scaleX = dampedDragAnimation.scaleX
                        scaleY = dampedDragAnimation.scaleY
                        val velocity = dampedDragAnimation.velocity / 12f
                        scaleX /= 1f - (velocity * 0.75f).coerceIn(-0.15f, 0.15f)
                        scaleY *= 1f - (velocity * 0.25f).coerceIn(-0.15f, 0.15f)
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        drawRect(Color.White.copy(alpha = 1f - progress))
                    }
                )
                .size(24.dp)
        )
    }
}

/**
 * iOS Grouped Settings Section Container with Liquid Glass styling.
 */
@Composable
fun IOSSettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    footer: String? = null,
    hazeState: HazeState? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.45f),
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(18.dp),
                    tintColor = Color(0xFF141626),
                    specularAlpha = 0.35f,
                    elevation = 6.dp,
                    hazeState = hazeState,
                    parentBackdrop = LocalBackdrop.current
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }

        if (footer != null) {
            Text(
                text = footer,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.4f),
                lineHeight = 16.sp,
                modifier = Modifier.padding(start = 12.dp, top = 6.dp, end = 12.dp)
            )
        }
    }
}

/**
 * iOS Settings Row with colorful icon tile, title, subtitle, value label & iOS switch.
 */
@Composable
fun IOSSettingsRow(
    icon: ImageVector,
    iconBackgroundColor: Color,
    title: String,
    subtitle: String? = null,
    valueLabel: String? = null,
    isToggle: Boolean = false,
    isChecked: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animatedCombinedClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = if (subtitle != null) 12.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // iOS colorful square icon tile
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.45f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (valueLabel != null && !isToggle) {
            Text(
                text = valueLabel,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier.width(6.dp))
        }

        if (isToggle) {
            IOSSwitch(
                checked = isChecked,
                onCheckedChange = { onClick() }
            )
        } else {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Animated liquid mesh background with subtle shifting ambient color blobs.
 */
@Composable
fun LiquidMeshBackground(
    modifier: Modifier = Modifier,
    dominantColor: Color = Color(0xFF6B21A8),
    accentColor: Color = Color(0xFF0EA5E9),
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_mesh")

    val offsetX1 by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_x"
    )
    val offsetY1 by infiniteTransition.animateFloat(
        initialValue = -0.08f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_y"
    )
    val offsetX2 by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = -0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_x"
    )
    val offsetY2 by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_y"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060810))
            .drawWithCache {
                val w = size.width
                val h = size.height
                onDrawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                dominantColor.copy(alpha = 0.26f),
                                dominantColor.copy(alpha = 0.09f),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.25f + offsetX1), h * (0.20f + offsetY1)),
                            radius = w * 0.85f
                        ),
                        radius = w * 0.85f,
                        center = Offset(w * (0.25f + offsetX1), h * (0.20f + offsetY1))
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.20f),
                                accentColor.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.75f + offsetX2), h * (0.70f + offsetY2)),
                            radius = w * 0.70f
                        ),
                        radius = w * 0.70f,
                        center = Offset(w * (0.75f + offsetX2), h * (0.70f + offsetY2))
                    )
                }
            }
    ) {
        content()
    }
}
