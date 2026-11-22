package com.example.ecodot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.util.CustomCover
import com.example.ecodot.util.PlaylistCustomizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlaylistBottomSheet(
    playlist: Playlist,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, customCoverJson: String?) -> Unit
) {
    var name by remember { mutableStateOf(playlist.name) }
    var description by remember { mutableStateOf(playlist.description) }
    val customCover = remember(playlist.coverArtUri) {
        PlaylistCustomizer.parseCustomCover(playlist.coverArtUri)
    }

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

    var selectedPresetIndex by remember {
        val initialIdx = gradientPresets.indexOfFirst {
            it.first.equals(customCover?.startColor, ignoreCase = true)
        }
        mutableStateOf(if (initialIdx >= 0) initialIdx else 0)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
                            Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Edit Playlist",
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
                        Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Playlist Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist name") },
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

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
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
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
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
                        val trimmedName = name.trim()
                        if (trimmedName.isNotEmpty()) {
                            val preset = gradientPresets[selectedPresetIndex]
                            val newCover = CustomCover(
                                startColor = preset.first,
                                endColor = preset.second,
                                isPrivate = customCover?.isPrivate ?: false,
                                tags = customCover?.tags ?: emptyList()
                            )
                            val customJson = PlaylistCustomizer.serializeCustomCover(newCover)
                            onSave(trimmedName, description.trim(), customJson)
                            onDismiss()
                        }
                    },
                    enabled = name.trim().isNotEmpty(),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoDotRed,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.White.copy(alpha = 0.1f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
