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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val tiltState = remember { mutableStateOf(TiltState()) }

    DisposableEffect(context, lifecycleOwner) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = if (rotationSensor == null) {
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } else null

        val activeSensor = rotationSensor ?: accelSensor

        var smoothX = 0f
        var smoothY = 0f
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                var targetX = 0f
                var targetY = 0f

                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val pitch = orientation[1] // -pi to pi
                    val roll = orientation[2]  // -pi/2 to pi/2

                    // Holding position roughly ~45 deg pitch (0.78 rad)
                    targetX = (roll / (PI.toFloat() / 3f)).coerceIn(-1f, 1f)
                    targetY = ((pitch - 0.78f) / (PI.toFloat() / 3f)).coerceIn(-1f, 1f)
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    targetX = (-event.values[0] / 6.0f).coerceIn(-1f, 1f)
                    targetY = ((event.values[1] - 5.5f) / 6.0f).coerceIn(-1f, 1f)
                }

                // Silky smooth low-pass filter (0.15f gives responsive 60-120fps motion without jitter)
                smoothX += 0.15f * (targetX - smoothX)
                smoothY += 0.15f * (targetY - smoothY)

                tiltState.value = TiltState(tiltX = smoothX, tiltY = smoothY)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        var isRegistered = false
        fun register() {
            if (!isRegistered && activeSensor != null && sensorManager != null) {
                sensorManager.registerListener(listener, activeSensor, SensorManager.SENSOR_DELAY_GAME)
                isRegistered = true
            }
        }

        fun unregister() {
            if (isRegistered && sensorManager != null) {
                sensorManager.unregisterListener(listener)
                isRegistered = false
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                register()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                unregister()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            register()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            unregister()
        }
    }

    return tiltState
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
