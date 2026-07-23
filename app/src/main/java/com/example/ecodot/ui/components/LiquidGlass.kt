package com.example.ecodot.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.ui.theme.animatedCombinedClickable
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

/**
 * AGSL Shader code for true iOS-style Motion Liquid Glass (Refraction + Chromatic Dispersion + Edge Lighting)
 */
private const val AGSL_LIQUID_GLASS_SHADER = """
    uniform shader composable;
    uniform vec2 size;
    uniform float specularAlpha;
    uniform float distortionFactor;
    uniform float time;

    half4 main(vec2 fragCoord) {
        vec2 uv = fragCoord / size;
        vec2 center = vec2(0.5, 0.5);
        vec2 dist = uv - center;
        float r = length(dist);
        
        // Liquid glass lens refraction displacement with slight dynamic wave motion
        float wave = sin(uv.x * 10.0 + time) * cos(uv.y * 10.0 + time) * 0.02;
        vec2 refractOffset = dist * (r * r + wave) * distortionFactor;
        vec2 sampleUv = clamp(uv + refractOffset, vec2(0.001), vec2(0.999));
        
        // Chromatic aberration (RGB splitting near edges)
        float rCol = composable.eval((sampleUv + refractOffset * 0.05) * size).r;
        float gCol = composable.eval(sampleUv * size).g;
        float bCol = composable.eval((sampleUv - refractOffset * 0.05) * size).b;
        float aCol = composable.eval(sampleUv * size).a;
        
        // Specular rim light highlighting top-left glass edge
        float edgeLeft = smoothstep(0.0, 0.05, uv.x) * smoothstep(0.0, 0.05, uv.y);
        float edgeHighlight = (1.0 - edgeLeft) * specularAlpha;
        
        vec3 finalColor = vec3(rCol, gCol, bCol) + vec3(edgeHighlight);
        return half4(finalColor, aCol);
    }
"""

/**
 * Liquid Glass modifier with AGSL Shader support on Android 13+ (API 33+),
 * falling back gracefully to Haze / Specular Glass on API < 33.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0x22FFFFFF),
    specularAlpha: Float = 0.38f,
    borderWidth: Dp = 0.8.dp,
    elevation: Dp = 8.dp,
    hazeState: HazeState? = null,
    tintColor: Color = Color(0xFF141620),
    blurRadius: Dp = 20.dp
): Modifier {
    val shadowAndClip = this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.4f),
            spotColor = Color.Black.copy(alpha = 0.6f)
        )
        .clip(shape)

    val glassStyle = HazeStyle(
        backgroundColor = tintColor.copy(alpha = 0.48f),
        tint = HazeDefaults.tint(tintColor.copy(alpha = 0.28f)),
        blurRadius = blurRadius,
        noiseFactor = 0.06f
    )

    // Base background blur or fallback tint
    val withBackground = if (hazeState != null) {
        shadowAndClip.hazeEffect(state = hazeState, style = glassStyle)
    } else {
        shadowAndClip.background(
            color = tintColor.copy(alpha = 0.75f),
            shape = shape
        )
    }

    // Apply AGSL Shader on API 33+ for real refraction & chromatic glass effects
    val withShader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        withBackground.graphicsLayer {
            try {
                val shader = RuntimeShader(AGSL_LIQUID_GLASS_SHADER)
                shader.setFloatUniform("size", size.width, size.height)
                shader.setFloatUniform("specularAlpha", specularAlpha)
                shader.setFloatUniform("distortionFactor", 0.12f)
                shader.setFloatUniform("time", 1.0f)
                renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "composable").asComposeRenderEffect()
            } catch (_: Throwable) {
                // Graceful fallback if device GPU doesn't support AGSL RuntimeShader
            }
        }
    } else {
        withBackground
    }

    // Specular light edge border
    return withShader.border(
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
    val cardModifier = modifier.liquidGlass(
        shape = shape,
        backgroundColor = backgroundColor,
        tintColor = tintColor,
        specularAlpha = specularAlpha,
        elevation = elevation,
        hazeState = hazeState
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

    val baseModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.45f),
            spotColor = Color.Black.copy(alpha = 0.65f)
        )
        .clip(shape)

    val glassStyle = HazeStyle(
        backgroundColor = tintColor.copy(alpha = 0.50f),
        tint = HazeDefaults.tint(tintColor.copy(alpha = 0.30f)),
        blurRadius = 24.dp,
        noiseFactor = 0.05f
    )

    val withHazeOrTint = if (hazeState != null) {
        baseModifier.hazeEffect(state = hazeState, style = glassStyle)
    } else {
        baseModifier.background(tintColor.copy(alpha = 0.76f), shape)
    }

    val interactiveModifier = if (onClick != null || onLongClick != null) {
        withHazeOrTint.animatedCombinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick
        )
    } else {
        withHazeOrTint
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
 * Authentic iOS-style Toggle Switch (Cupertino Switch).
 * Smooth spring animation, vibrant emerald green track, and sleek white thumb.
 */
@Composable
fun IOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeTrackColor: Color = Color(0xFF34C759) // Official iOS Green
) {
    val interactionSource = remember { MutableInteractionSource() }

    val trackColor by animateColorAsState(
        targetValue = if (checked) activeTrackColor else Color.White.copy(alpha = 0.15f),
        animationSpec = tween(durationMillis = 220),
        label = "ios_switch_track"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ios_switch_thumb"
    )

    Box(
        modifier = modifier
            .size(width = 50.dp, height = 30.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(trackColor)
            .border(
                width = 0.5.dp,
                color = if (checked) Color.Transparent else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(percent = 50)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(26.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .background(Color.White)
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
                    hazeState = hazeState
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
