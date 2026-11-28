package com.example.ecodot.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.example.ecodot.data.local.prefs.UserPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class AppUpdateManager(private val context: Context) {

    private val tag = "AppUpdateManager"
    private val prefs = UserPreferencesManager(context)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    private var downloadJob: Job? = null

    val currentVersionName: String
        get() {
            return try {
                val pInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
                pInfo.versionName ?: "1.0"
            } catch (e: Exception) {
                "1.0"
            }
        }

    val currentVersionCode: Long
        get() {
            return try {
                val pInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    pInfo.versionCode.toLong()
                }
            } catch (e: Exception) {
                1L
            }
        }

    fun dismissUpdate() {
        _status.value = UpdateStatus.Idle
    }

    /**
     * Check for updates using dual strategy:
     * 1. Direct JSON URL (e.g. raw.githubusercontent.com or custom CDN)
     * 2. GitHub Releases API (as fallback or alternative)
     */
    suspend fun checkForUpdates(
        force: Boolean = false,
        jsonUrl: String = prefs.updateJsonUrl,
        githubOwner: String = prefs.githubOwner,
        githubRepo: String = prefs.githubRepo
    ): AppUpdateInfo? = withContext(Dispatchers.IO) {
        if (!force && !prefs.autoCheckUpdates) {
            return@withContext null
        }

        _status.value = UpdateStatus.Checking

        // 1. Try Direct JSON Check first
        var updateInfo: AppUpdateInfo? = null
        if (jsonUrl.isNotBlank()) {
            try {
                updateInfo = checkDirectJson(jsonUrl)
                if (updateInfo != null) {
                    Log.d(tag, "Update found via Direct JSON: ${updateInfo.versionName} (${updateInfo.versionCode})")
                }
            } catch (e: Exception) {
                Log.w(tag, "Direct JSON check failed: ${e.message}")
            }
        }

        // 2. If no update found or JSON check failed, try GitHub Releases API
        if (updateInfo == null && githubOwner.isNotBlank() && githubRepo.isNotBlank()) {
            try {
                updateInfo = checkGitHubReleases(githubOwner, githubRepo)
                if (updateInfo != null) {
                    Log.d(tag, "Update found via GitHub Releases: ${updateInfo.versionName}")
                }
            } catch (e: Exception) {
                Log.w(tag, "GitHub Releases check failed: ${e.message}")
            }
        }

        // 3. Fallback to bundled assets/version.json if network failed or no remote update found
        if (updateInfo == null) {
            try {
                updateInfo = checkAssetsJson()
                if (updateInfo != null) {
                    Log.d(tag, "Update found via Bundled Assets: ${updateInfo.versionName}")
                }
            } catch (e: Exception) {
                Log.w(tag, "Assets JSON check failed: ${e.message}")
            }
        }

        prefs.lastUpdateCheckTime = System.currentTimeMillis()

        if (updateInfo != null) {
            _status.value = UpdateStatus.Available(updateInfo)
        } else {
            _status.value = UpdateStatus.UpToDate
        }

        return@withContext updateInfo
    }

    private fun checkDirectJson(url: String): AppUpdateInfo? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "EcoDot-Android")
            .header("Cache-Control", "no-cache")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(tag, "JSON endpoint returned HTTP ${response.code}")
                return null
            }
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)

            val remoteVersionCode = when {
                json.has("versionCode") -> json.optLong("versionCode", 0L)
                else -> 0L
            }
            val remoteVersionName = json.optString("versionName", "").trim()
            val downloadUrl = json.optString("downloadUrl", "").ifEmpty {
                json.optString("apkUrl", "").trim()
            }
            val changelog = json.optString("changelog", "").ifEmpty {
                json.optString("body", "Bug fixes and performance improvements.").trim()
            }
            val description = json.optString("description", "").trim().ifEmpty { null }
            val isMandatory = json.optBoolean("isMandatory", false)
            val apkSize = json.optLong("apkSize", 0L)

            // Optional rich metadata
            val rawReleaseDate = json.optString("releaseDate", "").ifEmpty {
                json.optString("publishedAt", "").ifEmpty {
                    json.optString("published_at", "").trim()
                }
            }
            val releaseDate = formatReleaseDate(rawReleaseDate)

            var changelogUrl = json.optString("changelogUrl", "").ifEmpty {
                json.optString("changelog_url", "").ifEmpty {
                    json.optString("blogUrl", "").trim()
                }
            }.ifEmpty { null }

            var guideUrl = json.optString("guideUrl", "").ifEmpty {
                json.optString("guide_url", "").ifEmpty {
                    json.optString("userGuideUrl", "").ifEmpty {
                        json.optString("docsUrl", "").trim()
                    }
                }
            }.ifEmpty { null }

            // Extract links from changelog text if not explicitly given, falling back to valid GitHub URLs
            if (changelogUrl == null) {
                changelogUrl = extractChangelogUrl(changelog) ?: UserPreferencesManager.DEFAULT_GITHUB_RELEASES_URL
            }
            if (guideUrl == null) {
                guideUrl = extractGuideUrl(changelog) ?: "${UserPreferencesManager.DEFAULT_GITHUB_REPO_URL}#readme"
            }

            // Parse structured sections
            val parsedSections = parseSectionsFromJson(json, changelog)

            if (downloadUrl.isBlank()) return null

            // Compare version
            val isNewer = isVersionNewer(remoteVersionCode, remoteVersionName)
            if (isNewer) {
                return AppUpdateInfo(
                    versionCode = remoteVersionCode,
                    versionName = remoteVersionName.ifEmpty { "v$remoteVersionCode" },
                    downloadUrl = downloadUrl,
                    changelog = changelog,
                    description = description,
                    sections = parsedSections,
                    releaseDate = releaseDate,
                    changelogUrl = changelogUrl,
                    guideUrl = guideUrl,
                    isMandatory = isMandatory,
                    source = UpdateSource.JSON,
                    apkSize = apkSize,
                    publishedAt = rawReleaseDate.ifEmpty { null }
                )
            }
        }
        return null
    }

    private fun checkGitHubReleases(owner: String, repo: String): AppUpdateInfo? {
        val apiUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
        val request = Request.Builder()
            .url(apiUrl)
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "EcoDot-Android")
            .header("Cache-Control", "no-cache")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(tag, "GitHub API returned HTTP ${response.code}")
                return null
            }
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)

            val tagName = json.optString("tag_name", "").trim()
            val changelog = json.optString("body", "What's new in this release.").trim()
            val publishedAt = if (json.has("published_at")) json.optString("published_at") else null

            // Find APK in assets
            val assets: JSONArray? = json.optJSONArray("assets")
            var apkDownloadUrl: String? = null
            var apkSize = 0L

            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "").lowercase()
                    if (name.endsWith(".apk")) {
                        apkDownloadUrl = asset.optString("browser_download_url")
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            if (apkDownloadUrl.isNullOrBlank()) return null

            val cleanTagName = tagName.removePrefix("v").trim()
            val isNewer = isVersionNewer(0L, cleanTagName)
            if (isNewer) {
                val releaseDate = formatReleaseDate(publishedAt)
                val htmlUrl = json.optString("html_url", "").trim()
                val changelogUrl = htmlUrl.ifEmpty {
                    extractChangelogUrl(changelog) ?: "https://github.com/$owner/$repo/releases"
                }
                val guideUrl = extractGuideUrl(changelog) ?: "https://github.com/$owner/$repo#readme"
                val parsedSections = parseMarkdownSections(changelog)

                return AppUpdateInfo(
                    versionCode = 0L,
                    versionName = tagName,
                    downloadUrl = apkDownloadUrl,
                    changelog = changelog,
                    sections = parsedSections,
                    releaseDate = releaseDate,
                    changelogUrl = changelogUrl,
                    guideUrl = guideUrl,
                    isMandatory = false,
                    source = UpdateSource.GITHUB,
                    apkSize = apkSize,
                    publishedAt = publishedAt
                )
            }
        }
        return null
    }

    private fun checkAssetsJson(): AppUpdateInfo? {
        return try {
            val jsonString = context.assets.open("version.json").bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)
            val remoteVersionCode = json.optLong("versionCode", 0L)
            val remoteVersionName = json.optString("versionName", "").trim()
            val downloadUrl = json.optString("downloadUrl", "").ifEmpty {
                json.optString("apkUrl", "").trim()
            }
            val changelog = json.optString("changelog", "").ifEmpty {
                json.optString("body", "Bug fixes and improvements.").trim()
            }
            val isMandatory = json.optBoolean("isMandatory", false)
            val apkSize = json.optLong("apkSize", 0L)

            val rawReleaseDate = json.optString("releaseDate", "").ifEmpty {
                json.optString("publishedAt", "").ifEmpty {
                    json.optString("published_at", "").trim()
                }
            }
            val releaseDate = formatReleaseDate(rawReleaseDate)

            var changelogUrl = json.optString("changelogUrl", "").ifEmpty {
                json.optString("changelog_url", "").ifEmpty {
                    json.optString("blogUrl", "").trim()
                }
            }.ifEmpty { null }

            var guideUrl = json.optString("guideUrl", "").ifEmpty {
                json.optString("guide_url", "").ifEmpty {
                    json.optString("userGuideUrl", "").ifEmpty {
                        json.optString("docsUrl", "").trim()
                    }
                }
            }.ifEmpty { null }

            if (changelogUrl == null) {
                changelogUrl = extractChangelogUrl(changelog) ?: UserPreferencesManager.DEFAULT_GITHUB_RELEASES_URL
            }
            if (guideUrl == null) {
                guideUrl = extractGuideUrl(changelog) ?: "${UserPreferencesManager.DEFAULT_GITHUB_REPO_URL}#readme"
            }

            val parsedSections = parseSectionsFromJson(json, changelog)

            val isNewer = isVersionNewer(remoteVersionCode, remoteVersionName)
            if (isNewer) {
                AppUpdateInfo(
                    versionCode = remoteVersionCode,
                    versionName = remoteVersionName.ifEmpty { "v$remoteVersionCode" },
                    downloadUrl = downloadUrl,
                    changelog = changelog,
                    sections = parsedSections,
                    releaseDate = releaseDate,
                    changelogUrl = changelogUrl,
                    guideUrl = guideUrl,
                    isMandatory = isMandatory,
                    source = UpdateSource.JSON,
                    apkSize = apkSize,
                    publishedAt = rawReleaseDate.ifEmpty { null }
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parses sections from either:
     * 1. A JSON object: { "New": ["..."], "Improved": ["..."], "Fixed": ["..."] }
     * 2. A JSON array: [ { "title": "New", "items": ["..."] } ]
     * 3. Fallback: Parse raw Markdown / bullet points changelog
     */
    private fun parseSectionsFromJson(json: JSONObject, changelogFallback: String): List<UpdateSection> {
        val result = mutableListOf<UpdateSection>()

        if (json.has("sections")) {
            val sectionsObj = json.optJSONObject("sections")
            if (sectionsObj != null) {
                val canonicalOrder = listOf(
                    "News & Highlights",
                    "New",
                    "Improved",
                    "Fixed",
                    "What's Changed",
                    "New Contributors"
                )
                // Process canonical in order
                for (title in canonicalOrder) {
                    if (sectionsObj.has(title)) {
                        val items = parseJsonArrayOrLines(sectionsObj.get(title))
                        if (items.isNotEmpty()) {
                            result.add(UpdateSection(title, items))
                        }
                    }
                }
                // Process any remaining keys
                val iter = sectionsObj.keys()
                while (iter.hasNext()) {
                    val key = iter.next()
                    if (!canonicalOrder.any { it.equals(key, ignoreCase = true) }) {
                        val items = parseJsonArrayOrLines(sectionsObj.get(key))
                        if (items.isNotEmpty()) {
                            result.add(UpdateSection(key, items))
                        }
                    }
                }
            } else {
                val sectionsArray = json.optJSONArray("sections")
                if (sectionsArray != null) {
                    for (i in 0 until sectionsArray.length()) {
                        val itemObj = sectionsArray.optJSONObject(i)
                        if (itemObj != null) {
                            val title = itemObj.optString("title", "").trim()
                            val items = parseJsonArrayOrLines(itemObj.opt("items"))
                            if (title.isNotEmpty() && items.isNotEmpty()) {
                                result.add(UpdateSection(title, items))
                            }
                        }
                    }
                }
            }
        }

        // If no structured sections found, parse from changelog markdown string
        if (result.isEmpty() && changelogFallback.isNotBlank()) {
            return parseMarkdownSections(changelogFallback)
        }

        return result
    }

    /**
     * Parses standard Markdown changelog / GitHub release body into structured sections.
     * Supports:
     * - #..###### Section headings of any length (canonical New/Improved/Fixed/What's Changed/Contributors or custom headings)
     * - Horizontal rules (---, ***, ___) skipped cleanly
     * - Top-level release version banner headings skipped when followed by structured subsections
     * - Bullet points starting with *, -, +, •, or numbered lists
     * - Merged consecutive/repeated section titles without duplication
     */
    fun parseMarkdownSections(rawMarkdown: String): List<UpdateSection> =
        Companion.parseMarkdownSections(rawMarkdown)

    private fun parseJsonArrayOrLines(value: Any?): List<String> {
        if (value == null) return emptyList()
        val list = mutableListOf<String>()
        if (value is JSONArray) {
            for (i in 0 until value.length()) {
                val str = value.optString(i, "").trim()
                val clean = cleanBulletPrefix(str)
                if (clean.isNotEmpty()) list.add(clean)
            }
        } else if (value is String) {
            for (raw in value.lines()) {
                val clean = cleanBulletPrefix(raw.trim())
                if (clean.isNotEmpty()) list.add(clean)
            }
        }
        return list
    }

    private fun cleanBulletPrefix(str: String): String {
        return when {
            str.startsWith("• ") -> str.removePrefix("• ").trim()
            str.startsWith("* ") -> str.removePrefix("* ").trim()
            str.startsWith("- ") -> str.removePrefix("- ").trim()
            str.startsWith("+ ") -> str.removePrefix("+ ").trim()
            str.matches(Regex("""^\d+\.\s+.*""")) -> str.replaceFirst(Regex("""^\d+\.\s+"""), "").trim()
            else -> str
        }
    }

    /**
     * Formats ISO 8601 timestamps (e.g. "2026-11-14T17:00:57Z")
     * into SimpMusic release format: "14 Nov 2026 17:00:57"
     */
    fun formatReleaseDate(rawDate: String?): String? {
        if (rawDate.isNullOrBlank()) return null
        return try {
            val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val cleanDate = rawDate.replace("Z", "").substringBefore("+")
            val date = isoFormat.parse(cleanDate)
            if (date != null) {
                val displayFormat = java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss", java.util.Locale.US)
                displayFormat.format(date)
            } else rawDate
        } catch (e: Exception) {
            rawDate
        }
    }

    private fun extractChangelogUrl(text: String): String? {
        val match = Regex("""(?:Full changelogs? here:?|\*\*Full Changelog\*\*:?)\s*(https?://[^\s\)]+)""", RegexOption.IGNORE_CASE).find(text)
        return match?.groupValues?.get(1)?.trim()
    }

    private fun extractGuideUrl(text: String): String? {
        val match = Regex("""(?:Read user-guide:?|Documentation:?|User guide:?)\s*(https?://[^\s\)]+)""", RegexOption.IGNORE_CASE).find(text)
        return match?.groupValues?.get(1)?.trim()
    }

    /**
     * Compares remote version with current local app version.
     * Checks versionCode first; if equal or 0, parses semantic version string.
     */
    private fun isVersionNewer(remoteVersionCode: Long, remoteVersionName: String): Boolean {
        val localCode = currentVersionCode
        if (remoteVersionCode > 0 && localCode > 0) {
            if (remoteVersionCode > localCode) return true
            if (remoteVersionCode < localCode) return false
        }

        // Semver fallback comparison: e.g. "1.1.0" vs "1.0"
        return compareSemVer(remoteVersionName, currentVersionName) > 0
    }

    private fun compareSemVer(v1: String, v2: String): Int {
        val s1 = v1.removePrefix("v").split(".", "-", "_")
        val s2 = v2.removePrefix("v").split(".", "-", "_")
        val length = maxOf(s1.size, s2.size)

        for (i in 0 until length) {
            val num1 = s1.getOrNull(i)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            val num2 = s2.getOrNull(i)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            if (num1 != num2) {
                return num1.compareTo(num2)
            }
        }
        return 0
    }

    /**
     * Download the APK with progress streaming.
     */
    suspend fun downloadUpdate(info: AppUpdateInfo): File? = withContext(Dispatchers.IO) {
        val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val apkFile = File(updateDir, "EcoDot-${info.versionName.replace('/', '_')}.apk")

        if (apkFile.exists() && apkFile.length() > 0 && (info.apkSize == 0L || apkFile.length() == info.apkSize)) {
            // Already fully downloaded
            _status.value = UpdateStatus.ReadyToInstall(apkFile, info)
            return@withContext apkFile
        }

        // Delete any partial download
        if (apkFile.exists()) {
            apkFile.delete()
        }

        _status.value = UpdateStatus.Downloading(0f, 0L, info.apkSize)

        val request = Request.Builder()
            .url(info.downloadUrl)
            .header("User-Agent", "EcoDot-Android")
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _status.value = UpdateStatus.Error("Failed to download: HTTP ${response.code}")
                    return@withContext null
                }

                val responseBody = response.body ?: run {
                    _status.value = UpdateStatus.Error("Empty download response")
                    return@withContext null
                }

                val totalLength = if (info.apkSize > 0) info.apkSize else responseBody.contentLength()
                var downloadedBytes = 0L

                responseBody.byteStream().use { inputStream ->
                    FileOutputStream(apkFile).use { outputStream ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var lastProgressEmit = 0L

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            val now = System.currentTimeMillis()
                            if (now - lastProgressEmit > 150 || downloadedBytes == totalLength) {
                                lastProgressEmit = now
                                val progress = if (totalLength > 0) {
                                    (downloadedBytes.toFloat() / totalLength.toFloat()).coerceIn(0f, 1f)
                                } else 0f
                                _status.value = UpdateStatus.Downloading(progress, downloadedBytes, totalLength)
                            }
                        }
                        outputStream.flush()
                    }
                }

                _status.value = UpdateStatus.ReadyToInstall(apkFile, info)
                return@withContext apkFile
            }
        } catch (e: Exception) {
            Log.e(tag, "Download failed", e)
            if (apkFile.exists()) {
                apkFile.delete()
            }
            _status.value = UpdateStatus.Error("Download failed: ${e.localizedMessage ?: "Network error"}")
            return@withContext null
        }
    }

    /**
     * Trigger Android Package Installer to install the APK file.
     * Handles unknown sources permission check on Android 8.0+.
     */
    fun installApk(apkFile: File): Boolean {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            _status.value = UpdateStatus.Error("APK file not found or corrupted.")
            return false
        }

        // On Android 8.0+ (Oreo), verify unknown sources permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                try {
                    val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(permissionIntent)
                } catch (e: Exception) {
                    Log.e(tag, "Could not open unknown sources settings", e)
                }
                return false
            }
        }

        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to launch package installer", e)
            _status.value = UpdateStatus.Error("Failed to start installer: ${e.localizedMessage}")
            false
        }
    }

    companion object {
        private val atxHeaderRegex = Regex("""^(#{1,6})\s+(.+)$""")
        private val canonicalHeaderRegex = Regex(
            """^(?:\*{2})?(New|Improved|Fixed|What's Changed|What Changed|New Contributors|Contributors|Improvements|Bug Fixes|Features)(?:\*{2}|:)?\s*$""",
            RegexOption.IGNORE_CASE
        )
        private val horizontalRuleRegex = Regex("""^([-*_])(?:\s*\1){2,}$""")
        private val releaseBannerVersionRegex = Regex("""(?i)\b(?:ecodot|release|version)\b.*\bv?\d+\.\d+|\bv\d+\.\d+\.\d+\b""")
        private val markdownLinkRegex = Regex("""\[([^\]]+)\]\([^)]+\)""")
        private val leadingSymbolPrefixRegex = Regex("""^[^\p{L}\p{N}]+""")

        fun parseMarkdownSections(rawMarkdown: String): List<UpdateSection> {
            if (rawMarkdown.isBlank()) return emptyList()

            val lines = rawMarkdown.lines()
            val sections = mutableListOf<UpdateSection>()
            var currentTitle: String? = null
            val currentItems = mutableListOf<String>()
            var lastLineWasBlank = false

            fun flushCurrent() {
                if (currentItems.isEmpty()) return
                val resolvedTitle = currentTitle?.takeIf { it.isNotBlank() } ?: "What's Changed"
                val existingIndex = sections.indexOfFirst { it.title.equals(resolvedTitle, ignoreCase = true) }
                if (existingIndex >= 0) {
                    val existing = sections[existingIndex]
                    sections[existingIndex] = existing.copy(items = existing.items + currentItems.toList())
                } else {
                    sections.add(UpdateSection(resolvedTitle, currentItems.toList()))
                }
                currentItems.clear()
            }

            for (rawLine in lines) {
                val trimmed = rawLine.trim()
                if (trimmed.isBlank()) {
                    lastLineWasBlank = true
                    continue
                }

                // Ignore Markdown horizontal rules (---, ***, ___) and HTML comments
                if (horizontalRuleRegex.matches(trimmed) || trimmed.startsWith("<!--")) {
                    lastLineWasBlank = true
                    continue
                }

                // Ignore header/footer links duplicated in UpdateDialog header
                if (trimmed.startsWith("**Full Changelog**", ignoreCase = true) ||
                    trimmed.startsWith("Full Changelog:", ignoreCase = true) ||
                    trimmed.startsWith("Full changelogs here", ignoreCase = true) ||
                    trimmed.startsWith("Read user-guide", ignoreCase = true)) {
                    lastLineWasBlank = true
                    continue
                }

                val atxMatch = atxHeaderRegex.find(trimmed)
                val canonicalMatch = canonicalHeaderRegex.find(trimmed)

                if (atxMatch != null || canonicalMatch != null) {
                    val headingLevel = atxMatch?.groupValues?.get(1)?.length ?: 2
                    val rawHeader = atxMatch?.groupValues?.get(2) ?: canonicalMatch!!.groupValues[1]
                    val cleanHeader = rawHeader
                        .replace(markdownLinkRegex, "$1")
                        .replace("**", "")
                        .replace("__", "")
                        .replace("`", "")
                        .trim()
                        .trimEnd(':')
                        .trim()

                    val strippedForCanonical = cleanHeader.replace(leadingSymbolPrefixRegex, "").trim()
                    val canonicalTitle = when {
                        strippedForCanonical.equals("New", ignoreCase = true) ||
                            strippedForCanonical.equals("Features", ignoreCase = true) -> "New"
                        strippedForCanonical.equals("Improved", ignoreCase = true) ||
                            strippedForCanonical.equals("Improvements", ignoreCase = true) -> "Improved"
                        strippedForCanonical.equals("Fixed", ignoreCase = true) ||
                            strippedForCanonical.equals("Bug Fixes", ignoreCase = true) -> "Fixed"
                        strippedForCanonical.equals("What's Changed", ignoreCase = true) ||
                            strippedForCanonical.equals("What Changed", ignoreCase = true) -> "What's Changed"
                        strippedForCanonical.equals("New Contributors", ignoreCase = true) ||
                            strippedForCanonical.equals("Contributors", ignoreCase = true) -> "New Contributors"
                        else -> null
                    }

                    val isTopBanner = canonicalTitle == null &&
                        headingLevel <= 2 &&
                        sections.isEmpty() &&
                        currentTitle == null &&
                        currentItems.isEmpty() &&
                        releaseBannerVersionRegex.containsMatchIn(cleanHeader)

                    if (isTopBanner || cleanHeader.isBlank()) {
                        lastLineWasBlank = true
                        continue
                    }

                    flushCurrent()
                    currentTitle = canonicalTitle ?: cleanHeader
                    lastLineWasBlank = false
                    continue
                }

                val contentLine = if (trimmed.startsWith("> ")) trimmed.removePrefix("> ").trim() else trimmed

                val bulletItem = when {
                    contentLine.startsWith("• ") -> contentLine.removePrefix("• ").trim()
                    contentLine.startsWith("* ") -> contentLine.removePrefix("* ").trim()
                    contentLine.startsWith("- ") -> contentLine.removePrefix("- ").trim()
                    contentLine.startsWith("+ ") -> contentLine.removePrefix("+ ").trim()
                    contentLine.matches(Regex("""^\d+\.\s+.*""")) -> contentLine.replaceFirst(Regex("""^\d+\.\s+"""), "").trim()
                    else -> null
                }

                if (bulletItem != null) {
                    if (bulletItem.isNotBlank()) currentItems.add(bulletItem)
                } else {
                    val isWrappedContinuation = currentItems.isNotEmpty() &&
                        !lastLineWasBlank &&
                        !contentLine.startsWith("**")
                    if (isWrappedContinuation) {
                        val lastIdx = currentItems.size - 1
                        currentItems[lastIdx] = currentItems[lastIdx] + " " + contentLine
                    } else {
                        currentItems.add(contentLine)
                    }
                }
                lastLineWasBlank = false
            }

            flushCurrent()
            return sections
        }
    }
}
