package com.example.ecodot.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.composed
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

@Composable
fun EcoDotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled — we control our own OLED palette
    dynamicColor: Boolean = false,
    dominantColor: Color? = null,
    accentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dominantColor != null && accentColor != null -> {
            // Dynamic Theming: subtly shift the global app background with the dominant color (Haze effect)
            val bg = Color(
                red = dominantColor.red * 0.02f + 0.03f * 0.98f,
                green = dominantColor.green * 0.02f + 0.03f * 0.98f,
                blue = dominantColor.blue * 0.02f + 0.03f * 0.98f
            )
            darkColorScheme(
                primary = accentColor,
                onPrimary = if (accentColor.luminance() > 0.5f) Color.Black else Color.White,
                primaryContainer = accentColor.copy(alpha = 0.15f),
                onPrimaryContainer = accentColor,
                background = bg,
                surface = bg,
                onBackground = Color.White,
                onSurface = Color.White,
                surfaceVariant = Color(0xFF16161A),
                onSurfaceVariant = Color.White.copy(alpha = 0.7f)
            )
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color(0xFF050505).toArgb()
            // Always force dark icons off — our bg is always OLED black
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

fun Modifier.animatedClickable(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = androidx.compose.ui.platform.LocalView.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "click_scale"
    )
    
    this
        .minimumInteractiveComponentSize()
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .indication(
            interactionSource = interactionSource,
            indication = ripple(),
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                onClick()
            }
        )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun Modifier.animatedCombinedClickable(
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = androidx.compose.ui.platform.LocalView.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "click_scale"
    )
    
    this
        .minimumInteractiveComponentSize()
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .indication(
            interactionSource = interactionSource,
            indication = ripple(),
        )
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
                onClick()
            },
            onLongClick = onLongClick?.let {
                {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    it()
                }
            }
        )
}
