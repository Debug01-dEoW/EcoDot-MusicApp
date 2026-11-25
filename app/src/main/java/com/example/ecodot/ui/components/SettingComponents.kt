package com.example.ecodot.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

val LocalLiquidGlassEnabled = staticCompositionLocalOf { true }

private val SettingGroupShape = RoundedCornerShape(20.dp)
private val SettingRowPaddingHorizontal = 16.dp
private val SettingDividerThickness = 0.5.dp
val AmbientGlowHeight = 360.dp

/**
 * SimpMusic-exact Apple-style grouped settings card:
 * an uppercase muted section title above a translucent rounded card whose top row hairline
 * is clipped cleanly so conditional rows never leave a stray border at the top.
 */
@Composable
fun SettingGroup(
    title: String,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surfaceContainer
    Column(
        modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Text(
            text = title.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = SettingRowPaddingHorizontal,
                end = SettingRowPaddingHorizontal,
                bottom = 8.dp
            )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SettingGroupShape)
                .background(cardColor.copy(alpha = 0.55f))
                .drawWithContent {
                    // Clip off the first row's top hairline cleanly.
                    clipRect(top = SettingDividerThickness.toPx()) {
                        this@drawWithContent.drawContent()
                    }
                },
            content = content
        )
        if (footer != null) {
            Box(
                Modifier.padding(
                    start = SettingRowPaddingHorizontal,
                    end = SettingRowPaddingHorizontal,
                    top = 8.dp
                )
            ) {
                footer()
            }
        }
    }
}

/**
 * SimpMusic-exact Settings row inside a [SettingGroup], supporting clickable pickers,
 * switches, disabled greyed state, inset top hairline divider, and optional embedded views.
 */
@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    smallSubtitle: Boolean = false,
    isEnable: Boolean = true,
    onClick: (() -> Unit)? = null,
    switch: Pair<Boolean, (Boolean) -> Unit>? = null,
    otherView: (@Composable () -> Unit)? = null
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val disabledColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)

    Box(
        Modifier
            .drawBehind {
                val y = SettingDividerThickness.toPx() / 2
                val inset = SettingRowPaddingHorizontal.toPx()
                val rtl = layoutDirection == LayoutDirection.Rtl
                drawLine(
                    color = dividerColor,
                    start = Offset(if (rtl) 0f else inset, y),
                    end = Offset(if (rtl) size.width - inset else size.width, y),
                    strokeWidth = SettingDividerThickness.toPx()
                )
            }
            .then(
                when {
                    onClick != null && isEnable -> Modifier.clickable { onClick() }
                    switch != null && isEnable -> Modifier.clickable { switch.second(!switch.first) }
                    else -> Modifier
                }
            )
            .alpha(if (isEnable) 1f else 0.55f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 13.dp,
                    horizontal = SettingRowPaddingHorizontal
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (isEnable) MaterialTheme.colorScheme.onSurface else disabledColor
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = if (smallSubtitle) {
                        MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 17.sp)
                    } else {
                        MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = 18.sp)
                    },
                    color = if (isEnable) MaterialTheme.colorScheme.onSurfaceVariant else disabledColor
                )

                otherView?.let {
                    Spacer(Modifier.height(14.dp))
                    it.invoke()
                }
            }
            if (switch != null) {
                Spacer(Modifier.width(12.dp))
                Switch(
                    modifier = Modifier.wrapContentWidth(),
                    checked = switch.first,
                    onCheckedChange = switch.second,
                    enabled = isEnable,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = primaryColor,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}

/**
 * SimpMusic's top ambient glow behind Settings: tints the upper 360dp with the app's
 * primary color and dissolves smoothly into the page background.
 */
@Composable
fun AmbientThemeGlow(
    tint: Color,
    pageBackground: Color = MaterialTheme.colorScheme.background,
    modifier: Modifier = Modifier
) {
    val animatedGlow by animateColorAsState(
        targetValue = Color(
            red = tint.red * 0.42f + pageBackground.red * 0.58f,
            green = tint.green * 0.42f + pageBackground.green * 0.58f,
            blue = tint.blue * 0.42f + pageBackground.blue * 0.58f
        ),
        animationSpec = tween(500),
        label = "ambientThemeGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AmbientGlowHeight)
            .background(
                Brush.linearGradient(
                    colors = listOf(animatedGlow, pageBackground),
                    start = Offset(0f, 0f),
                    end = Offset(450f, 1050f)
                )
            )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, pageBackground)
                    )
                )
        )
    }
}
