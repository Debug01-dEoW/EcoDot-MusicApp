package com.example.ecodot.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.indication
import androidx.compose.foundation.clickable
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple

// Vibrant Material 3 Color Palette (EcoDot)
// Inspired by energy and nature (Green/Teal/Vibrant Blue)

val PrimaryLight = Color(0xFF006D40)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF95F7B9)
val OnPrimaryContainerLight = Color(0xFF002110)

val SecondaryLight = Color(0xFF4E6354)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFD1E8D5)
val OnSecondaryContainerLight = Color(0xFF0B1F14)

val TertiaryLight = Color(0xFF3B6470)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFBEEAF7)
val OnTertiaryContainerLight = Color(0xFF001F26)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFFBFDF8)
val OnBackgroundLight = Color(0xFF191C19)
val SurfaceLight = Color(0xFFFBFDF8)
val OnSurfaceLight = Color(0xFF191C19)

// Dark Theme Colors
val PrimaryDark = Color(0xFFEADBC8) // Muted champagne/ivory instead of pale green
val OnPrimaryDark = Color(0xFF121214)
val PrimaryContainerDark = Color(0xFF332D28)
val OnPrimaryContainerDark = Color(0xFFEADBC8)

val SecondaryDark = Color(0xFF9E9E9E)
val OnSecondaryDark = Color(0xFF121214)
val SecondaryContainerDark = Color(0xFF2C2C2E)
val OnSecondaryContainerDark = Color(0xFFE5E5EA)

val TertiaryDark = Color(0xFFA2CEDB)
val OnTertiaryDark = Color(0xFF01353F)
val TertiaryContainerDark = Color(0xFF214C57)
val OnTertiaryContainerDark = Color(0xFFBEEAF7)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF08080A) // Deep cool slate-black space
val OnBackgroundDark = Color(0xFFECEBE6) // Muted elegant off-white
val SurfaceDark = Color(0xFF0C0C0E) // Very dark surface card base
val OnSurfaceDark = Color(0xFFECEBE6)

// Glassmorphism Tokens
val GlassWhite = Color(0x1AFFFFFF)
val GlassBlack = Color(0x33000000)
val GlassBorder = Color(0x1AFFFFFF)
val GlassHighlight = Color(0x4DFFFFFF)

// New Aesthetic Palette
val EcoDotRed = Color(0xFFEADBC8) // Warm Champagne/Ivory accent instead of bright red
val EcoDotBlack = Color(0xFF08080A)
val EcoDotCard = Color(0xFF141417) // Sleek neutral card background
