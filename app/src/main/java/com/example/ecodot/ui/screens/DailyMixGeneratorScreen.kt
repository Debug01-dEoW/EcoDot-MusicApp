package com.example.ecodot.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ecodot.ui.theme.EcoDotCard
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.ui.theme.animatedClickable
import com.example.ecodot.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMixGeneratorScreen(
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var dailyMixName by remember { mutableStateOf("My Daily Chill Mix") }
    var selectedDailyMood by remember { mutableStateOf("Chill") }
    var selectedDailySize by remember { mutableStateOf(20) }
    var selectedDailyType by remember { mutableStateOf("Mixed") }
    var dailyMixTagsText by remember { mutableStateOf("") }
    var selectedDailyStyle by remember { mutableStateOf("Pop") }
    var selectedDailyStrategy by remember { mutableStateOf("Most Listened") }

    val moods = listOf(
        Triple("Chill", "🍃", Color(0xFF2ECC71)),
        Triple("Focus", "🎯", Color(0xFF3498DB)),
        Triple("Workout", "⚡", Color(0xFFE67E22)),
        Triple("Party", "🎉", Color(0xFF9B59B6)),
        Triple("Sleep", "😴", Color(0xFF5E35B1)),
        Triple("Study", "📚", Color(0xFFFF8F00)),
        Triple("Upbeat", "☀️", Color(0xFFFFB300)),
        Triple("Romance", "💖", Color(0xFFE91E63)),
        Triple("Gaming", "🎮", Color(0xFF00E5FF)),
        Triple("Acoustic", "🎸", Color(0xFF8D6E63))
    )
    val sizes = listOf(10, 20, 30, 50)
    val styles = listOf("Pop", "Hip Hop", "Rap", "Rock", "Lofi", "Electronic", "Acoustic", "Instrumental")
    val strategies = listOf(
        Pair("Most Listened", "📈"),
        Pair("Followed Artists", "👤"),
        Pair("Recently Played", "🕒"),
        Pair("On Repeat", "🔁"),
        Pair("Taste Recommendations", "✨")
    )
    val types = listOf("Local Only", "YouTube", "Mixed")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF050505),
                                Color(0xFF050505).copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .animatedClickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Mix Generator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            
            // Header Image/Icon Graphic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF232526), Color(0xFF414345))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Discover New Music Daily",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Playlist Name Field
            OutlinedTextField(
                value = dailyMixName,
                onValueChange = { dailyMixName = it },
                label = { Text("Playlist Name", color = Color.White.copy(alpha = 0.6f)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.03f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                    focusedBorderColor = EcoDotRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Mood Selector
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select Mood",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 4.dp)
                ) {
                    moods.forEach { (mood, icon, color) ->
                        val isSelected = selectedDailyMood == mood
                        val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")
                        
                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) color else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    selectedDailyMood = mood
                                    dailyMixName = "Daily $mood Mix"
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(icon, fontSize = 16.sp)
                                Text(
                                    text = mood,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Style Selector
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Mix Style",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    styles.forEach { style ->
                        val isSelected = selectedDailyStyle == style
                        val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")
                        
                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) Color(0xFF6200EA) else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedDailyStyle = style }
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = style,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Curation Strategy Selector
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Curation Strategy",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    strategies.forEach { (strategy, icon) ->
                        val isSelected = selectedDailyStrategy == strategy
                        val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")
                        
                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) Color(0xFF007A87) else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedDailyStrategy = strategy }
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(icon, fontSize = 16.sp)
                                Text(
                                    text = strategy,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Mix Size Selector (stacked vertically for more space)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Mix Size",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    sizes.forEach { size ->
                        val isSelected = selectedDailySize == size
                        val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")
                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) EcoDotRed else Color.White.copy(alpha = 0.05f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedDailySize = size }
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "$size tracks",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Source Selector (stacked vertically to prevent text clipping)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Source",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    types.forEach { type ->
                        val isSelected = selectedDailyType == type
                        val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")
                        val icon = when(type) { "YouTube" -> "🌐"; "Local Only" -> "📱"; else -> "🔀" }
                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) EcoDotRed else Color.White.copy(alpha = 0.05f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedDailyType = type }
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "$icon $type",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Custom Tags
            OutlinedTextField(
                value = dailyMixTagsText,
                onValueChange = { dailyMixTagsText = it },
                label = { Text("Custom Tags (optional)", color = Color.White.copy(alpha = 0.6f)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("chill, coding, late night", color = Color.White.copy(alpha = 0.3f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.03f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                    focusedBorderColor = EcoDotRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Generate Button
            Button(
                onClick = {
                    if (dailyMixName.isNotBlank()) {
                        viewModel.generateDailyMix(
                            name = dailyMixName.trim(),
                            mood = selectedDailyMood,
                            size = selectedDailySize,
                            type = selectedDailyType,
                            style = selectedDailyStyle,
                            strategy = selectedDailyStrategy,
                            tags = dailyMixTagsText
                        )
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(EcoDotRed, Color(0xFFFF5E3A))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White)
                    Text(
                        text = "Generate Mix",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
