package com.example.ecodot.ui.components
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.Check
import com.example.ecodot.ui.theme.Close
import com.example.ecodot.ui.theme.PlaylistAdd

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.ui.theme.EcoDotCard
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.util.CustomCover
import com.example.ecodot.util.PlaylistCustomizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, customCoverJson: String?) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    var playlistDesc by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val gradientPresets = remember {
        listOf(
            Pair("#FF1DB954", "#FF0D2C1D"), // Spotify Green
            Pair("#FF4FACFE", "#FF00F2FE"), // Cyber Blue
            Pair("#FFF857A6", "#FFFF5858"), // Sunset Pink
            Pair("#FF7F00FF", "#FFE100FF"), // Neon Violet
            Pair("#FFFF416C", "#FFFF4B2B"), // Crimson Ember
            Pair("#FF3A7BD5", "#FF1D2731")  // Midnight Slate
        )
    }
    var selectedPresetIndex by remember { mutableStateOf(0) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(150)
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141418),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.25f))
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            SimpIcons.PlaylistAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "New Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        SimpIcons.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Playlist Name Input
            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                label = { Text("Playlist name") },
                placeholder = { Text("My awesome playlist", color = Color.White.copy(alpha = 0.35f)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = EcoDotRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = Color(0xFF1B1B20),
                    unfocusedContainerColor = Color(0xFF1B1B20),
                    focusedLabelColor = EcoDotRed,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                )
            )

            // Playlist Description Input
            OutlinedTextField(
                value = playlistDesc,
                onValueChange = { playlistDesc = it },
                label = { Text("Description (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = EcoDotRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = Color(0xFF1B1B20),
                    unfocusedContainerColor = Color(0xFF1B1B20),
                    focusedLabelColor = EcoDotRed,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                )
            )

            // Theme Color Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Cover Theme",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gradientPresets.forEachIndexed { index, (start, end) ->
                        val isSelected = selectedPresetIndex == index
                        val startColor = PlaylistCustomizer.parseColorSafe(start, Color.Gray)
                        val endColor = PlaylistCustomizer.parseColorSafe(end, Color.DarkGray)

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(startColor, endColor)))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f),
                                    shape = CircleShape
                                )
                                .clickable { selectedPresetIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    SimpIcons.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Privacy Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B1B20))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Private Playlist",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Only visible to you on this device",
                        color = Color.White.copy(alpha = 0.45f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EcoDotRed,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons: Cancel and Create
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        val name = playlistName.trim()
                        if (name.isNotEmpty()) {
                            val preset = gradientPresets[selectedPresetIndex]
                            val customCover = CustomCover(
                                startColor = preset.first,
                                endColor = preset.second,
                                isPrivate = isPrivate,
                                tags = emptyList()
                            )
                            val customJson = PlaylistCustomizer.serializeCustomCover(customCover)
                            onCreate(name, playlistDesc.trim(), customJson)
                            onDismiss()
                        }
                    },
                    enabled = playlistName.trim().isNotEmpty(),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoDotRed,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.White.copy(alpha = 0.1f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
