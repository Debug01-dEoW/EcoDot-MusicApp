package com.example.ecodot.ui.components
import com.example.ecodot.ui.theme.SimpIcons
import com.example.ecodot.ui.theme.AutoAwesome
import com.example.ecodot.ui.theme.Build
import com.example.ecodot.ui.theme.Campaign
import com.example.ecodot.ui.theme.Download
import com.example.ecodot.ui.theme.Favorite
import com.example.ecodot.ui.theme.History
import com.example.ecodot.ui.theme.NewReleases
import com.example.ecodot.ui.theme.SystemUpdate
import com.example.ecodot.ui.theme.TrendingUp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ecodot.data.local.prefs.UserPreferencesManager
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.util.AppUpdateInfo
import com.example.ecodot.util.UpdateStatus
import java.io.File

private val UpdateBg = Color(0xFF13141A)
private val UpdateCardBg = Color(0xFF1B1C24)
private val UpdateGreen = Color(0xFF1ED760)

/**
 * Clean, modern In-App Update & Release News screen showing version upgrade info,
 * release overview news, categorized changes, download size/progress, and install actions.
 */
@Composable
fun UpdateDialog(
    status: UpdateStatus,
    currentVersion: String,
    onDownloadClick: (AppUpdateInfo) -> Unit,
    onInstallClick: (File) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val info = when (status) {
        is UpdateStatus.Available -> status.info
        is UpdateStatus.ReadyToInstall -> status.info
        else -> null
    }

    val downloadingState = status as? UpdateStatus.Downloading
    val readyState = status as? UpdateStatus.ReadyToInstall
    val errorState = status as? UpdateStatus.Error

    val isMandatory = info?.isMandatory == true
    val targetVersion = info?.versionName ?: readyState?.info?.versionName ?: currentVersion
    val displayTargetVersion = if (targetVersion.startsWith("v", ignoreCase = true)) targetVersion else "v$targetVersion"
    val displayCurrentVersion = if (currentVersion.startsWith("v", ignoreCase = true)) currentVersion else "v$currentVersion"
    val releaseDate = info?.releaseDate ?: info?.publishedAt ?: readyState?.info?.releaseDate
    val apkSizeBytes = info?.apkSize ?: readyState?.info?.apkSize ?: 0L
    val description = info?.description ?: readyState?.info?.description
    val uriHandler = LocalUriHandler.current

    Dialog(
        onDismissRequest = {
            if (!isMandatory && downloadingState == null) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isMandatory && downloadingState == null,
            dismissOnClickOutside = !isMandatory && downloadingState == null,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .fillMaxHeight(0.88f)
                    .border(0.6.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                color = UpdateBg,
                shadowElevation = 20.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Pinned Top Hero Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        UpdateGreen.copy(alpha = 0.16f),
                                        UpdateBg
                                    )
                                )
                            )
                            .padding(horizontal = 22.dp, vertical = 20.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(UpdateGreen.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = SimpIcons.SystemUpdate,
                                        contentDescription = null,
                                        tint = UpdateGreen,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when {
                                            readyState != null -> "Update Ready to Install"
                                            downloadingState != null -> "Downloading Update..."
                                            else -> "New EcoDot Update"
                                        },
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (!releaseDate.isNullOrBlank()) {
                                        Text(
                                            text = "Released $releaseDate",
                                            color = Color.White.copy(alpha = 0.62f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            // Version upgrade + size + developer credit chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(UpdateGreen.copy(alpha = 0.16f))
                                        .border(0.5.dp, UpdateGreen.copy(alpha = 0.45f), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$displayCurrentVersion -> $displayTargetVersion",
                                        color = UpdateGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (apkSizeBytes > 0L) {
                                    val mbSize = apkSizeBytes / (1024f * 1024f)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = String.format("%.1f MB", mbSize),
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "By Debug01-dEoW aka Deep",
                                color = Color(0xFF90CAF9),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        runCatching {
                                            uriHandler.openUri(UserPreferencesManager.DEFAULT_GITHUB_PROFILE_URL)
                                        }
                                    }
                                    .padding(vertical = 2.dp)
                            )
                        }
                    }

                    // Scrollable News & Changelog Area — starts cleanly at the top on every open
                    val targetVersionKey = "${info?.versionName ?: readyState?.info?.versionName}_${info?.source}"
                    val scrollState = key(targetVersionKey) { rememberScrollState(initial = 0) }
                    LaunchedEffect(targetVersionKey) {
                        scrollState.scrollTo(0)
                        withFrameNanos { }
                        scrollState.scrollTo(0)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 18.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Release Overview / Spotlight News
                        if (!description.isNullOrBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(UpdateCardBg)
                                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = SimpIcons.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Release News & Overview",
                                        color = Color.White,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = description,
                                    color = Color.White.copy(alpha = 0.84f),
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            }
                        }

                        // Structured Sections: News & Highlights, New, Improved, Fixed, What's Changed, New Contributors
                        val sections = info?.sections ?: readyState?.info?.sections ?: emptyList()
                        if (sections.isNotEmpty()) {
                            sections.forEach { section ->
                                UpdateSectionCard(
                                    title = section.title,
                                    items = section.items
                                )
                            }
                        } else {
                            val rawChangelog = info?.changelog ?: readyState?.info?.changelog
                            if (!rawChangelog.isNullOrBlank()) {
                                val fallbackItems = rawChangelog.lines()
                                    .map { it.trim().trimStart('•', '*', '-', '+', ' ').trim() }
                                    .filter { it.isNotEmpty() }
                                if (fallbackItems.isNotEmpty()) {
                                    UpdateSectionCard(
                                        title = "What's Changed",
                                        items = fallbackItems
                                    )
                                }
                            }
                        }

                        // Helpful Links Card
                        val changelogUrl = info?.changelogUrl ?: readyState?.info?.changelogUrl
                        val guideUrl = info?.guideUrl ?: readyState?.info?.guideUrl
                        if (!changelogUrl.isNullOrBlank() || !guideUrl.isNullOrBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(UpdateCardBg)
                                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (!changelogUrl.isNullOrBlank()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Full Changelogs & Releases",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        ClickableUrlText(url = changelogUrl)
                                    }
                                }
                                if (!guideUrl.isNullOrBlank()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "README & User Guide",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        ClickableUrlText(url = guideUrl)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Downloading progress or Error banner above buttons
                    if (downloadingState != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { downloadingState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = UpdateGreen,
                                trackColor = Color.White.copy(alpha = 0.14f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val mbDownloaded = downloadingState.downloadedBytes / (1024f * 1024f)
                                val mbTotal = downloadingState.totalBytes / (1024f * 1024f)
                                val sizeText = if (downloadingState.totalBytes > 0) {
                                    String.format("%.1f MB / %.1f MB", mbDownloaded, mbTotal)
                                } else "Downloading package..."

                                Text(
                                    text = sizeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.72f)
                                    )
                                )
                                val percent = (downloadingState.progress * 100).toInt()
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = UpdateGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    if (errorState != null) {
                        Text(
                            text = errorState.message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)

                    // Pinned Bottom Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isMandatory && downloadingState == null) {
                            TextButton(
                                onClick = onDismiss,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Later",
                                    color = Color.White.copy(alpha = 0.78f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        when {
                            readyState != null -> {
                                Button(
                                    onClick = { onInstallClick(readyState.apkFile) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = UpdateGreen,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "Install Update",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            downloadingState != null -> {
                                val percent = (downloadingState.progress * 100).toInt()
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = Color.White.copy(alpha = 0.10f),
                                        disabledContentColor = Color.White.copy(alpha = 0.72f)
                                    ),
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "Downloading $percent%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            info != null -> {
                                Button(
                                    onClick = { onDownloadClick(info) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = UpdateGreen,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = SimpIcons.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Download Update",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateSectionCard(
    title: String,
    items: List<String>
) {
    val (icon, tint) = sectionStyle(title)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(UpdateCardBg)
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    color = tint,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                FormattedChangelogItem(
                    text = item,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun sectionStyle(title: String): Pair<ImageVector, Color> {
    val normalized = title.lowercase()
    return when {
        normalized.contains("news") || normalized.contains("highlight") ->
            SimpIcons.Campaign to Color(0xFFFFD54F)
        normalized == "new" || normalized.contains("feature") ->
            SimpIcons.NewReleases to UpdateGreen
        normalized.contains("improv") ->
            SimpIcons.TrendingUp to Color(0xFF64B5F6)
        normalized.contains("fix") || normalized.contains("bug") ->
            SimpIcons.Build to Color(0xFFFF8A65)
        normalized.contains("contributor") ->
            SimpIcons.Favorite to EcoDotRed
        else ->
            SimpIcons.History to Color(0xFFB39DDB)
    }
}

/**
 * Clickable URL component with underline styling and safe external browser launching.
 */
@Composable
fun ClickableUrlText(
    url: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    Text(
        text = url,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = Color(0xFF90CAF9),
            textDecoration = TextDecoration.Underline,
            fontSize = 12.5.sp,
            lineHeight = 18.sp
        ),
        modifier = modifier
            .fillMaxWidth()
            .focusProperties { canFocus = false }
            .clickable {
                try {
                    uriHandler.openUri(url)
                } catch (_: Exception) {
                }
            }
    )
}

private val markdownTokenRegex = Regex(
    """\[([^\]]+)\]\((https?://[^\s)]+)\)|(https?://[^\s)\]]+)|\*\*([^*]+)\*\*|`([^`]+)`"""
)

/**
 * Formatted item text with inline clickable links (Markdown `[label](url)` or raw URLs),
 * **bold** emphasis spans, and inline `code` spans.
 */
@Composable
fun FormattedChangelogItem(
    text: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val matches = markdownTokenRegex.findAll(text).toList()

    if (matches.isEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.86f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            ),
            modifier = modifier
        )
        return
    }

    var hasUrl = false
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            if (start > currentIndex) {
                withStyle(
                    SpanStyle(
                        color = Color.White.copy(alpha = 0.86f),
                        fontSize = 13.sp
                    )
                ) {
                    append(text.substring(currentIndex, start))
                }
            }

            val mdLabel = match.groupValues[1]
            val mdUrl = match.groupValues[2]
            val bareUrl = match.groupValues[3]
            val boldText = match.groupValues[4]
            val codeText = match.groupValues[5]

            when {
                mdUrl.isNotEmpty() -> {
                    hasUrl = true
                    pushStringAnnotation(tag = "URL", annotation = mdUrl)
                    withStyle(
                        SpanStyle(
                            color = Color(0xFF90CAF9),
                            textDecoration = TextDecoration.Underline,
                            fontSize = 13.sp
                        )
                    ) {
                        append(mdLabel)
                    }
                    pop()
                }
                bareUrl.isNotEmpty() -> {
                    hasUrl = true
                    val cleanUrl = bareUrl.trimEnd('.', ',', ';')
                    val trailingPunct = bareUrl.substring(cleanUrl.length)
                    pushStringAnnotation(tag = "URL", annotation = cleanUrl)
                    withStyle(
                        SpanStyle(
                            color = Color(0xFF90CAF9),
                            textDecoration = TextDecoration.Underline,
                            fontSize = 13.sp
                        )
                    ) {
                        append(cleanUrl)
                    }
                    pop()
                    if (trailingPunct.isNotEmpty()) {
                        withStyle(
                            SpanStyle(
                                color = Color.White.copy(alpha = 0.86f),
                                fontSize = 13.sp
                            )
                        ) {
                            append(trailingPunct)
                        }
                    }
                }
                boldText.isNotEmpty() -> {
                    withStyle(
                        SpanStyle(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    ) {
                        append(boldText)
                    }
                }
                codeText.isNotEmpty() -> {
                    withStyle(
                        SpanStyle(
                            color = Color(0xFFE3F2FD),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp
                        )
                    ) {
                        append(codeText)
                    }
                }
            }
            currentIndex = end
        }
        if (currentIndex < text.length) {
            withStyle(
                SpanStyle(
                    color = Color.White.copy(alpha = 0.86f),
                    fontSize = 13.sp
                )
            ) {
                append(text.substring(currentIndex))
            }
        }
    }

    if (!hasUrl) {
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 19.sp
            ),
            modifier = modifier
        )
    } else {
        @Suppress("DEPRECATION")
        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 19.sp
            ),
            modifier = modifier.focusProperties { canFocus = false },
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        try {
                            uriHandler.openUri(annotation.item)
                        } catch (_: Exception) {
                        }
                    }
            }
        )
    }
}
