package com.example.ecodot.ui.components.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.data.local.prefs.PlayerStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerStyleBottomSheet(
    currentStyle: PlayerStyle,
    accentColor: Color,
    onStyleSelected: (PlayerStyle) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141416),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Player Style",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Choose your preferred Now Playing layout and visual theme",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            StyleOptionCard(
                title = "EcoDot Classic",
                description = "42-bar waveform visualizer, liquid glassmorphism & canvas video",
                icon = Icons.Rounded.GraphicEq,
                isSelected = currentStyle == PlayerStyle.ECODOT,
                accentColor = accentColor,
                onClick = {
                    onStyleSelected(PlayerStyle.ECODOT)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            StyleOptionCard(
                title = "Apple Music",
                description = "Fluid mesh gradient background, prominent live karaoke lyrics",
                icon = Icons.Rounded.MusicNote,
                isSelected = currentStyle == PlayerStyle.APPLE_MUSIC,
                accentColor = accentColor,
                onClick = {
                    onStyleSelected(PlayerStyle.APPLE_MUSIC)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            StyleOptionCard(
                title = "Spotify",
                description = "High-contrast minimalist cards, slim seeker & instant equalizer",
                icon = Icons.Rounded.Album,
                isSelected = currentStyle == PlayerStyle.SPOTIFY,
                accentColor = accentColor,
                onClick = {
                    onStyleSelected(PlayerStyle.SPOTIFY)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            StyleOptionCard(
                title = "Vinyl Turntable",
                description = "Spinning 33 RPM vinyl record with realistic tonearm, grooves & vintage glow",
                icon = Icons.Rounded.Radio,
                isSelected = currentStyle == PlayerStyle.VINYL,
                accentColor = accentColor,
                onClick = {
                    onStyleSelected(PlayerStyle.VINYL)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun StyleOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) accentColor.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f)
    val borderColor = if (isSelected) accentColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) accentColor else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
