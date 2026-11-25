package com.example.ecodot.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.PI
import kotlin.math.abs

@Immutable
data class TiltState(
    val tiltX: Float = 0f,
    val tiltY: Float = 0f
)

val LocalTiltState = compositionLocalOf { TiltState() }

@Composable
fun rememberDeviceTilt(): State<TiltState> {
    // Disabled sensor polling: eliminates 60 FPS continuous CPU/GPU drain and recomposition loop across the UI
    return remember { mutableStateOf(TiltState(tiltX = 0f, tiltY = 0f)) }
}

/**
 * Creates a dynamic specular rim brush that reacts physically to phone tilt angles.
 * As the phone tilts in the user's hand, the specular highlight gleam travels along the rim to catch the light.
 */
fun createSpecularRimBrush(
    tiltX: Float,
    tiltY: Float,
    baseAlpha: Float = 0.55f,
    secondaryAlpha: Float = 0.25f
): Brush {
    val offsetX = tiltX * 180f
    val offsetY = tiltY * 180f

    val start = Offset(0f - offsetX, 0f - offsetY)
    val end = Offset(Float.POSITIVE_INFINITY - offsetX, Float.POSITIVE_INFINITY - offsetY)

    val dynamicHighlightAlpha = (baseAlpha + abs(tiltX) * 0.15f + abs(tiltY) * 0.10f).coerceIn(0.35f, 0.95f)
    val dynamicSecondaryAlpha = (secondaryAlpha + abs(tiltY) * 0.10f).coerceIn(0.12f, 0.55f)

    return Brush.linearGradient(
        0.0f to Color.White.copy(alpha = dynamicHighlightAlpha),
        0.30f to Color.White.copy(alpha = 0.10f),
        0.70f to Color.White.copy(alpha = (0.04f + abs(tiltX) * 0.08f).coerceIn(0.02f, 0.25f)),
        1.0f to Color.White.copy(alpha = dynamicSecondaryAlpha),
        start = start,
        end = end
    )
}
