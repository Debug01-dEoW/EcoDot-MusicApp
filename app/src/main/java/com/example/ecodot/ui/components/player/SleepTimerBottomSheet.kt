package com.example.ecodot.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodot.ui.theme.EcoDotRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    sleepTimerRemaining: Long?,
    onDismiss: () -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onSetSleepTimerAtEndOfTrack: () -> Unit,
    onCancelSleepTimer: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF161617), // Deep premium dark grey
        contentColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.15f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = EcoDotRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Sleep Timer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Select when to pause playback",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Active Timer Status Card
            if (sleepTimerRemaining != null) {
                val timerText = if (sleepTimerRemaining == 0L) {
                    "End of Track"
                } else {
                    val m = sleepTimerRemaining / 60
                    val s = sleepTimerRemaining % 60
                    String.format("%02d:%02d remaining", m, s)
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .background(EcoDotRed.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .border(1.dp, EcoDotRed.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active Timer",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = EcoDotRed
                            )
                            Text(
                                text = timerText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        
                        TextButton(
                            onClick = {
                                onCancelSleepTimer()
                                onDismiss()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = EcoDotRed)
                        ) {
                            Text("Turn Off", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Durations Grid (2-column layout)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val durationRows = listOf(
                    listOf(5, 15),
                    listOf(30, 45),
                    listOf(60, 90)
                )

                durationRows.forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { minutes ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(14.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        onSetSleepTimer(minutes)
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$minutes min",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // End of Track Option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                        .clickable {
                            onSetSleepTimerAtEndOfTrack()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "End of Track",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
