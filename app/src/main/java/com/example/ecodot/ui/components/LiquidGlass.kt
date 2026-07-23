package com.example.ecodot.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ecodot.ui.theme.animatedCombinedClickable
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import org.intellij.lang.annotations.Language

/**
 * AGSL Liquid Glass Shader code (Android 13 / API 33+)
 * Simulates real 3D liquid glass refraction, chromatic dispersion (RGB offset),
 * and dynamic specular light highlights (iOS 26 / SimpMusic Backdrop style).
 */
@Language("AGSL")
private const val LIQUID_GLASS_AGSL = """
    uniform shader composable;
    uniform float2 size;
    uniform float refIndex;
    uniform float dispersion;
    uniform float specularStrength;

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / size;
        float2 center = float2(0.5, 0.5);
        float2 distVec = uv - center;
        float dist = length(distVec);
        
        // Liquid lens curvature distortion
        float distortion = pow(dist, 1.8) * refIndex * 0.08;
        float2 offset = distVec * distortion;

        // Chromatic aberration (RGB channel splitting near glass edges)
        float2 redOffset = offset * (1.0 + dispersion);
        float2 blueOffset = offset * (1.0 - dispersion);

        half4 colorR = composable.eval((uv + redOffset) * size);
        half4 colorG = composable.eval((uv + offset) * size);
        half4 colorB = composable.eval((uv + blueOffset) * size);

        half4 finalColor = half4(colorR.r, colorG.g, colorB.b, colorG.a);

        // Specular highlight / Fresnel rim illumination
        float rim = smoothstep(0.35, 0.5, dist);
        float highlight = pow(rim, 2.5) * specularStrength;
        finalColor.rgb += half3(highlight, highlight, highlight * 1.1);

        return finalColor;
    }
"""

/**
 * Comprehensive Liquid Glass modifier.
 * Uses AGSL Shaders on Android 13+ (API 33+) for genuine physical liquid glass refraction & dispersion,
 * combined with Haze backdrop blur and multi-layer specular rim borders across all API levels.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0x22FFFFFF),
    specularAlpha: Float = 0.38f,
    borderWidth: Dp = 1.2.dp,
    elevation: Dp = 10.dp,
    hazeState: HazeState? = null,
    tintColor: Color = Color(0xFF10121E),
    blurRadius: Dp = 24.dp,
    refractionIndex: Float = 0.45f,
    chromaticDispersion: Float = 0.12f
): Modifier {
    val base = this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.45f),
            spotColor = Color.Black.copy(alpha = 0.65f)
        )
        .clip(shape)

    val glassStyle = HazeStyle(
        backgroundColor = tintColor.copy(alpha = 0.45f),
        tint = HazeDefaults.tint(tintColor.copy(alpha = 0.28f)),
        blurRadius = blurRadius,
        noiseFactor = 0.05f
    )

    // Backdrop Haze Blur layer
    val blurredBase = if (hazeState != null) {
        base.hazeEffect(state = hazeState, style = glassStyle)
    } else {
        base.background(
            color = tintColor.copy(alpha = 0.82f),
            shape = shape
        )
    }

    // Apply AGSL Shader on API 33+ for liquid lens refraction & dispersion
    val shaderApplied = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        blurredBase.graphicsLayer {
            val runtimeShader = RuntimeShader(LIQUID_GLASS_AGSL).apply {
                setFloatUniform("size", size.width, size.height)
                setFloatUniform("refIndex", refractionIndex)
                setFloatUniform("dispersion", chromaticDispersion)
                setFloatUniform("specularStrength", specularAlpha)
            }
            renderEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "composable").asComposeRenderEffect()
        }
    } else {
        blurredBase
    }

    // Dual-pass specular highlight border (creates 3D glass edge refraction)
    return shaderApplied
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = specularAlpha * 0.4f),
                    tintColor.copy(alpha = 0.15f),
                    Color.Black.copy(alpha = 0.25f)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            shape = shape
        )
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = specularAlpha),
                    Color.White.copy(alpha = specularAlpha * 0.55f),
                    Color.White.copy(alpha = 0.04f),
                    Color.Black.copy(alpha = 0.35f)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            shape = shape
        )
}

/**
 * Reusable Liquid Glass container card with optional click interactions.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color(0x22FFFFFF),
    tintColor: Color = Color(0xFF141620),
    specularAlpha: Float = 0.35f,
    elevation: Dp = 8.dp,
    hazeState: HazeState? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cardModifier = modifier
        .liquidGlass(
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
 * Animated liquid mesh background with dynamic color blob physics.
 * Simulates ambient light shifting behind Liquid Glass interfaces.
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
        initialValue = -0.18f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_x"
    )
    val offsetY1 by infiniteTransition.animateFloat(
        initialValue = -0.10f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(17000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_y"
    )
    val offsetX2 by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = -0.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_x"
    )
    val offsetY2 by infiniteTransition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
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
                                dominantColor.copy(alpha = 0.32f),
                                dominantColor.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.25f + offsetX1), h * (0.20f + offsetY1)),
                            radius = w * 0.90f
                        ),
                        radius = w * 0.90f,
                        center = Offset(w * (0.25f + offsetX1), h * (0.20f + offsetY1))
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.24f),
                                accentColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.75f + offsetX2), h * (0.70f + offsetY2)),
                            radius = w * 0.75f
                        ),
                        radius = w * 0.75f,
                        center = Offset(w * (0.75f + offsetX2), h * (0.70f + offsetY2))
                    )
                }
            }
    ) {
        content()
    }
}
