package com.example.ecodot.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
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
import com.example.ecodot.ui.theme.EcoDotRed
import com.example.ecodot.util.AppUpdateInfo
import com.example.ecodot.util.UpdateStatus
import java.io.File

/**
 * SimpMusic-styled clean, organized In-App Update Dialog.
 *
 * Features:
 * - Pinned dialog title ("New update available")
 * - Subheader with version and formatted release timestamp
 * - Clickable changelog and user-guide hyperlinks
 * - Categorized sections: New, Improved, Fixed, What's Changed, New Contributors
 * - Bullet points with inline clickable pull-request links and @mentions
 * - Responsive smooth scrolling with pinned bottom actions (Cancel / Download / Install)
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
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF2C2D35),
                shadowElevation = 16.dp,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
                ) {
                    // Pinned Header
                    Text(
                        text = "New update available",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable Changelog Area — keyed and snapped post-layout so every open starts at top
                    val targetVersionKey = "${info?.versionName ?: readyState?.info?.versionName}_${info?.source}"
                    val scrollState = key(targetVersionKey) { rememberScrollState(initial = 0) }
                    LaunchedEffect(targetVersionKey) {
                        scrollState.scrollTo(0)
                        withFrameNanos { }
                        scrollState.scrollTo(0)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(scrollState)
                    ) {
                        // Version Line
                        val targetVersion = info?.versionName ?: readyState?.info?.versionName ?: "v$currentVersion"
                        Text(
                            text = "Version $targetVersion available",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        // Release Date Line
                        val releaseDate = info?.releaseDate ?: info?.publishedAt ?: readyState?.info?.releaseDate
                        if (!releaseDate.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Release: $releaseDate",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.82f)
                                )
                            )
                        }

                        // Full changelogs URL Link
                        val changelogUrl = info?.changelogUrl ?: readyState?.info?.changelogUrl
                        if (!changelogUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Full changelogs here:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            ClickableUrlText(url = changelogUrl)
                        }

                        // Read user-guide URL Link
                        val guideUrl = info?.guideUrl ?: readyState?.info?.guideUrl
                        if (!guideUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Read user-guide:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            ClickableUrlText(url = guideUrl)
                        }

                        // Structured Sections: New, Improved, Fixed, What's Changed, New Contributors
                        val sections = info?.sections ?: readyState?.info?.sections ?: emptyList()
                        if (sections.isNotEmpty()) {
                            for (section in sections) {
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                for (item in section.items) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "• ",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        )
                                        FormattedChangelogItem(
                                            text = item,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Unstructured Changelog Fallback
                            val rawChangelog = info?.changelog ?: readyState?.info?.changelog
                            if (!rawChangelog.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = "What's Changed",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                for (line in rawChangelog.lines()) {
                                    val trimmed = line.trim().trimStart('•', '*', '-', '+', ' ').trim()
                                    if (trimmed.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 6.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "• ",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White.copy(alpha = 0.85f)
                                                )
                                            )
                                            FormattedChangelogItem(
                                                text = trimmed,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Downloading Progress Indicator
                        if (downloadingState != null) {
                            Spacer(modifier = Modifier.height(20.dp))
                            LinearProgressIndicator(
                                progress = { downloadingState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = EcoDotRed,
                                trackColor = Color.White.copy(alpha = 0.15f)
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
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                                val percent = (downloadingState.progress * 100).toInt()
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Error Status
                        if (errorState != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = errorState.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFFF5252),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Pinned Bottom Actions
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isMandatory && downloadingState == null) {
                            TextButton(
                                onClick = onDismiss,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.5.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (readyState != null) {
                            TextButton(
                                onClick = { onInstallClick(readyState.apkFile) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Install",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                )
                            }
                        } else if (downloadingState != null) {
                            val percent = (downloadingState.progress * 100).toInt()
                            TextButton(
                                onClick = { /* no-op during active download */ },
                                enabled = false,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                )
                            }
                        } else if (info != null) {
                            TextButton(
                                onClick = { onDownloadClick(info) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Download",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
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
            fontSize = 13.sp,
            lineHeight = 18.sp
        ),
        modifier = modifier
            .fillMaxWidth()
            .focusProperties { canFocus = false }
            .clickable {
                try {
                    uriHandler.openUri(url)
                } catch (e: Exception) {
                    // Fallback or ignore
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
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 13.5.sp,
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
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 13.5.sp
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
                            fontSize = 13.5.sp
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
                            fontSize = 13.5.sp
                        )
                    ) {
                        append(cleanUrl)
                    }
                    pop()
                    if (trailingPunct.isNotEmpty()) {
                        withStyle(
                            SpanStyle(
                                color = Color.White.copy(alpha = 0.88f),
                                fontSize = 13.5.sp
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
                            fontSize = 13.5.sp
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
                            fontSize = 13.sp
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
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 13.5.sp
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
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
            }
        )
    }
}
