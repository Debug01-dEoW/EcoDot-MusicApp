package com.example.ecodot.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SimpChip(
    isAnimated: Boolean = false,
    isSelected: Boolean = false,
    text: String,
    onClick: () -> Unit,
) {
    InfiniteBorderAnimationView(
        isAnimated = isAnimated && isSelected,
        brush = Brush.sweepGradient(listOf(Color(0xFF888888), Color.White, Color(0xFF888888))),
        backgroundColor = Color.Transparent,
        contentPadding = 0.dp,
        borderWidth = 1.dp,
        shape = CircleShape,
        oneCircleDurationMillis = 2500,
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            ElevatedFilterChip(
                shape = CircleShape,
                elevation = FilterChipDefaults.elevatedFilterChipElevation(elevation = 0.dp),
                colors = FilterChipDefaults.elevatedFilterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = Color.White,
                    labelColor = Color(0xFFAAAAAA),
                    selectedLabelColor = Color.Black,
                ),
                onClick = { onClick.invoke() },
                label = {
                    Text(text, maxLines = 1)
                },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = Color.Transparent,
                    borderColor = Color(0xFF333644),
                ),
                selected = isSelected,
            )
        }
    }
}
