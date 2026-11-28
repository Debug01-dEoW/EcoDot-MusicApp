package com.example.ecodot.ui.components.player
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Album
import com.example.ecodot.ui.theme.CheckCircle
import com.example.ecodot.ui.theme.GraphicEq
import com.example.ecodot.ui.theme.MusicNote
import com.example.ecodot.ui.theme.Radio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.data.local.prefs.PlayerStyle
import com.example.ecodot.ui.theme.poppinsFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerStyleBottomSheet(
    currentStyle: PlayerStyle,
    accentColor: Color,
    onStyleSelected: (PlayerStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val resolvedAccent = if (accentColor == Color.Black) Color(0xFF8C6EFF) else accentColor
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF14151A),
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Now Playing Style",
                fontFamily = poppinsFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Switch layouts seamlessly — all styles support HD Song & Responsive Video modes",
                fontFamily = poppinsFamily,
                fontSize = 12.5.sp,
                color = Color.White.copy(alpha = 0.58f),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            StyleOptionCard(
                title = "Apple Music",
                description = "Artwork mesh backdrop, frosted blur, expanding pill scrubber, live lyric strip & dock tabs",
                badge = "Mesh + Dock",
                icon = SimpIcons.MusicNote,
                previewBrush = Brush.linearGradient(listOf(Color(0xFFFA233B), Color(0xFF4C1D3D))),
                isSelected = currentStyle == PlayerStyle.APPLE_MUSIC,
                accentColor = Color(0xFFFF375F),
                onClick = {
                    onStyleSelected(PlayerStyle.APPLE_MUSIC)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            StyleOptionCard(
                title = "Vinyl Turntable",
                description = "Spinning 33⅓ RPM disc, tracking aluminum tonearm, sleeve notes & studio monitor video",
                badge = "33⅓ RPM",
                icon = SimpIcons.Radio,
                previewBrush = Brush.linearGradient(listOf(Color(0xFFE5C07B), Color(0xFF3E2D1E))),
                isSelected = currentStyle == PlayerStyle.VINYL,
                accentColor = Color(0xFFE5C07B),
                onClick = {
                    onStyleSelected(PlayerStyle.VINYL)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            StyleOptionCard(
                title = "Spotify",
                description = "Obsidian gradient canvas, swipeable art, emerald Save toggle, crisp cards & output pill",
                badge = "Canvas + Cards",
                icon = SimpIcons.Album,
                previewBrush = Brush.linearGradient(listOf(Color(0xFF1ED760), Color(0xFF143622))),
                isSelected = currentStyle == PlayerStyle.SPOTIFY,
                accentColor = Color(0xFF1ED760),
                onClick = {
                    onStyleSelected(PlayerStyle.SPOTIFY)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            StyleOptionCard(
                title = "EcoDot Classic",
                description = "Vibrant Liquid Glass aesthetics, inline synced lyrics card & ambient video aurora",
                badge = "Liquid Glass",
                icon = SimpIcons.GraphicEq,
                previewBrush = Brush.linearGradient(listOf(resolvedAccent, Color(0xFF191B26))),
                isSelected = currentStyle == PlayerStyle.ECODOT,
                accentColor = resolvedAccent,
                onClick = {
                    onStyleSelected(PlayerStyle.ECODOT)
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
    badge: String,
    icon: ImageVector,
    previewBrush: Brush,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) accentColor.copy(alpha = 0.13f) else Color.White.copy(alpha = 0.045f)
    val borderColor = if (isSelected) accentColor.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.08f)

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
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(previewBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontFamily = poppinsFamily,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.10f)
                    ) {
                        Text(
                            text = badge,
                            fontFamily = poppinsFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accentColor else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = description,
                    fontFamily = poppinsFamily,
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.56f),
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isSelected) {
                Icon(
                    imageVector = SimpIcons.CheckCircle,
                    contentDescription = "Selected",
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
