package com.example.ecodot.data.backup

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.room.withTransaction
import com.example.ecodot.data.local.database.EcoDotDatabase
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.local.entities.UserProfile
import com.example.ecodot.data.local.prefs.AudioQuality
import com.example.ecodot.data.local.prefs.PlayerStyle
import com.example.ecodot.data.local.prefs.ThemeColorSource
import com.example.ecodot.data.local.prefs.ThemeMode
import com.example.ecodot.data.local.prefs.UserPreferencesManager
import com.example.ecodot.data.local.prefs.VideoQuality
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EcoDotBackupManager(
    private val context: Context,
    private val database: EcoDotDatabase,
    private val prefsManager: UserPreferencesManager
) {
    companion object {
        private const val TAG = "EcoDotBackupManager"
        const val AUTO_BACKUP_FILENAME = "EcoDot_AutoBackup_latest.ecodot"
        private const val DOWNLOADS_SUBFOLDER = "EcoDot"
        private const val MAX_AVATAR_BYTES = 750_000

        private val moshi: Moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        private val archiveAdapter = moshi.adapter(EcoDotBackupArchive::class.java).indent("  ")

        fun encodeArchiveToJson(archive: EcoDotBackupArchive): String {
            return archiveAdapter.toJson(archive)
        }

        fun decodeArchiveFromJson(json: String): EcoDotBackupArchive? {
            if (json.isBlank()) return null
            return runCatching { archiveAdapter.fromJson(json) }.getOrNull()
        }
    }

    suspend fun createBackupArchive(appVersionName: String = "1.28v"): EcoDotBackupArchive =
        withContext(Dispatchers.IO) {
            val tracks = database.trackDao().getAllTracksOnce().map { BackupTrackDto.fromEntity(it) }
            val playlists = database.playlistDao().getAllPlaylistsOnce().map { BackupPlaylistDto.fromEntity(it) }
            val playlistTracks = database.playlistDao().getAllPlaylistTracksOnce().map { BackupPlaylistTrackDto.fromEntity(it) }
            val followedArtists = database.followedArtistDao().getFollowedArtistsOnce().map { BackupFollowedArtistDto.fromEntity(it) }
            val history = database.historyDao().getAllHistoryOnce().map { BackupHistoryDto.fromEntity(it) }
            val recentSearches = database.recentSearchDao().getRecentSearchesOnce().map { BackupRecentSearchDto.fromEntity(it) }
            val entityProfile = database.profileDao().getUserProfileOnce()
            val profileDto = entityProfile?.let { prof ->
                BackupProfileDto(
                    name = prof.name,
                    favoriteGenres = prof.favoriteGenres,
                    favoriteArtists = prof.favoriteArtists,
                    avatarUri = prof.avatarUri,
                    avatarBase64 = readAvatarBase64(prof.avatarUri),
                    isPro = prof.isPro
                )
            }

            val prefsDto = BackupPreferencesDto(
                audioQuality = prefsManager.audioQuality.name,
                videoQuality = prefsManager.videoQuality.name,
                offlineMode = prefsManager.offlineMode,
                dataSaver = prefsManager.dataSaver,
                searchHistory = prefsManager.searchHistory,
                killServiceOnExit = prefsManager.killServiceOnExit,
                crossfadeEnabled = prefsManager.crossfadeEnabled,
                crossfadeDuration = prefsManager.crossfadeDuration,
                crossfadeDjMode = prefsManager.crossfadeDjMode,
                crossfadeSkipAlbum = prefsManager.crossfadeSkipAlbum,
                normalizeVolume = prefsManager.normalizeVolume,
                skipSilent = prefsManager.skipSilent,
                equalizerEnabled = prefsManager.equalizerEnabled,
                virtualizerStrength = prefsManager.virtualizerStrength,
                bassBoostStrength = prefsManager.bassBoostStrength,
                selectedEqPreset = prefsManager.selectedEqPreset,
                customEqBandsRaw = prefsManager.customEqBandsRaw,
                customEqPresetsRaw = prefsManager.customEqPresetsRaw,
                autoCanvasEnabled = prefsManager.autoCanvasEnabled,
                lockscreenLyricsEnabled = prefsManager.lockscreenLyricsEnabled,
                sponsorBlockEnabled = prefsManager.sponsorBlockEnabled,
                sponsorBlockCategories = prefsManager.sponsorBlockCategories.toList(),
                themeMode = prefsManager.themeMode.name,
                themeColorSource = prefsManager.themeColorSource.name,
                customThemeColorHex = prefsManager.customThemeColorHex,
                liquidGlassEnabled = prefsManager.liquidGlassEnabled,
                playerStyle = prefsManager.playerStyle.name,
                libraryIsGrid = prefsManager.libraryIsGrid,
                librarySort = prefsManager.librarySort,
                pinnedPlaylistIds = prefsManager.pinnedPlaylistIds.toList(),
                cacheLimitMb = prefsManager.cacheLimitMb,
                autoCheckUpdates = prefsManager.autoCheckUpdates,
                selectedHomeCountry = prefsManager.selectedHomeCountry,
                autoBackupFrequency = prefsManager.autoBackupFrequency
            )

            EcoDotBackupArchive(
                schemaVersion = 1,
                appVersion = appVersionName,
                createdAt = System.currentTimeMillis(),
                tracks = tracks,
                playlists = playlists,
                playlistTracks = playlistTracks,
                followedArtists = followedArtists,
                history = history,
                recentSearches = recentSearches,
                profile = profileDto,
                preferences = prefsDto
            )
        }

    suspend fun exportToUri(uri: Uri, appVersionName: String = "1.28v"): BackupOperationResult =
        withContext(Dispatchers.IO) {
            try {
                val archive = createBackupArchive(appVersionName)
                val json = encodeArchiveToJson(archive)
                val out = context.contentResolver.openOutputStream(uri, "wt")
                    ?: context.contentResolver.openOutputStream(uri, "w")
                    ?: return@withContext BackupOperationResult(false, "Could not open destination file for writing")
                out.use { it.write(json.toByteArray(Charsets.UTF_8)) }

                recordBackupSuccess(archive, "Saved to chosen file")
            } catch (e: Exception) {
                Log.e(TAG, "exportToUri failed", e)
                BackupOperationResult(false, e.message ?: "Backup export failed")
            }
        }

    suspend fun exportAutoBackup(appVersionName: String = "1.28v"): BackupOperationResult =
        withContext(Dispatchers.IO) {
            try {
                val archive = createBackupArchive(appVersionName)
                val json = encodeArchiveToJson(archive)
                val bytes = json.toByteArray(Charsets.UTF_8)

                // 1. User-selected SAF folder (survives uninstall & grants read/write back on folder re-pick)
                val treeUriStr = prefsManager.autoBackupTreeUri
                if (!treeUriStr.isNullOrBlank()) {
                    val treeUri = Uri.parse(treeUriStr)
                    if (hasPersistedWriteAccess(treeUri)) {
                        val written = writeToSafTree(treeUri, AUTO_BACKUP_FILENAME, bytes)
                        if (written) {
                            return@withContext recordBackupSuccess(archive, "Auto-saved to selected folder")
                        }
                    }
                }

                // 2. Shared Downloads/EcoDot via MediaStore (API 29+, survives app uninstall cleanly)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val written = writeToMediaStoreDownloads(AUTO_BACKUP_FILENAME, bytes)
                    if (written) {
                        return@withContext recordBackupSuccess(archive, "Saved to Downloads/$DOWNLOADS_SUBFOLDER")
                    }
                }

                // 3. Fallback to app-external backups folder (API 24-28 or restricted Storage)
                val dir = context.getExternalFilesDir("backups") ?: File(context.filesDir, "backups").also { it.mkdirs() }
                if (!dir.exists()) dir.mkdirs()
                File(dir, AUTO_BACKUP_FILENAME).writeBytes(bytes)
                recordBackupSuccess(archive, "Saved to local backups folder")
            } catch (e: Exception) {
                Log.e(TAG, "exportAutoBackup failed", e)
                BackupOperationResult(false, e.message ?: "Backup failed")
            }
        }

    suspend fun runAutoBackupIfDue(appVersionName: String = "1.28v"): BackupOperationResult? =
        withContext(Dispatchers.IO) {
            val schedule = BackupSchedule.fromKey(prefsManager.autoBackupFrequency)
            if (!BackupSchedule.isBackupDue(schedule, prefsManager.lastBackupTimestamp, System.currentTimeMillis())) {
                return@withContext null
            }

            // Do not overwrite an existing backup on Downloads/SAF with a brand-new empty library
            val hasMeaningfulData = database.playlistDao().getAllPlaylistTracksOnce().isNotEmpty() ||
                database.followedArtistDao().getFollowedArtistsOnce().isNotEmpty() ||
                database.trackDao().getAllTracksOnce().isNotEmpty()
            if (!hasMeaningfulData) {
                return@withContext null
            }

            exportAutoBackup(appVersionName)
        }

    suspend fun restoreFromUri(uri: Uri, mode: RestoreMode = RestoreMode.MERGE): BackupOperationResult =
        withContext(Dispatchers.IO) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader(Charsets.UTF_8).readText()
                } ?: return@withContext BackupOperationResult(false, "Unable to read selected backup file")

                val archive = decodeArchiveFromJson(json)
                    ?: return@withContext BackupOperationResult(false, "Invalid or corrupted EcoDot backup file")

                restoreArchive(archive, mode)
            } catch (e: Exception) {
                Log.e(TAG, "restoreFromUri failed", e)
                BackupOperationResult(false, e.message ?: "Restore failed")
            }
        }

    suspend fun restoreArchive(archive: EcoDotBackupArchive, mode: RestoreMode = RestoreMode.MERGE): BackupOperationResult =
        withContext(Dispatchers.IO) {
            try {
                val fileChecker: (String) -> Boolean = { p -> runCatching { File(p).exists() }.getOrDefault(false) }

                database.withTransaction {
                    val trackDao = database.trackDao()
                    val playlistDao = database.playlistDao()
                    val followedDao = database.followedArtistDao()
                    val historyDao = database.historyDao()
                    val searchDao = database.recentSearchDao()
                    val profileDao = database.profileDao()

                    if (mode == RestoreMode.REPLACE) {
                        historyDao.clearHistory()
                        playlistDao.clearAllPlaylistTracks()
                        playlistDao.clearAllPlaylists()
                        followedDao.clearFollowedArtists()
                        searchDao.clearAllRecentSearches()
                        trackDao.clearAllTracks()
                    }

                    // 1. Tracks
                    val existingTracks = if (mode == RestoreMode.MERGE) {
                        trackDao.getAllTracksOnce().associateBy { it.id }
                    } else {
                        emptyMap()
                    }
                    val tracksToUpsert: List<Track> = archive.tracks.map { dto ->
                        val converted = dto.toEntity(fileChecker)
                        val existing = existingTracks[converted.id]
                        if (existing != null) {
                            converted.copy(
                                path = if (!existing.isYouTube && fileChecker(existing.path)) existing.path else converted.path,
                                isYouTube = if (!existing.isYouTube && fileChecker(existing.path)) false else converted.isYouTube,
                                playCount = maxOf(existing.playCount, converted.playCount),
                                lastPlayedAt = maxNullable(existing.lastPlayedAt, converted.lastPlayedAt)
                            )
                        } else {
                            converted
                        }
                    }
                    if (tracksToUpsert.isNotEmpty()) {
                        trackDao.upsertTracks(tracksToUpsert)
                    }

                    // 2. Playlists + track mappings + pinned ID remapping
                    val oldToNewPlaylistId = mutableMapOf<Long, Long>()
                    if (mode == RestoreMode.REPLACE) {
                        val entities = archive.playlists.map { it.toEntity() }
                        if (entities.isNotEmpty()) {
                            playlistDao.insertPlaylists(entities)
                            entities.forEach { oldToNewPlaylistId[it.id] = it.id }
                        }
                    } else {
                        val existingByName = playlistDao.getAllPlaylistsOnce().associateBy { it.name.lowercase(Locale.ROOT) }
                        for (pDto in archive.playlists) {
                            val match = existingByName[pDto.name.lowercase(Locale.ROOT)]
                            if (match != null) {
                                oldToNewPlaylistId[pDto.id] = match.id
                            } else {
                                val newId = playlistDao.insertPlaylist(pDto.toEntity().copy(id = 0L))
                                oldToNewPlaylistId[pDto.id] = newId
                            }
                        }
                    }

                    val existingTrackSet = tracksToUpsert.map { it.id }.toMutableSet().apply {
                        addAll(existingTracks.keys)
                    }
                    val mappedPlaylistTracks = archive.playlistTracks.mapNotNull { pt ->
                        val targetPlaylistId = oldToNewPlaylistId[pt.playlistId] ?: return@mapNotNull null
                        if (pt.trackId !in existingTrackSet) return@mapNotNull null
                        pt.toEntity(mappedPlaylistId = targetPlaylistId)
                    }
                    if (mappedPlaylistTracks.isNotEmpty()) {
                        playlistDao.insertPlaylistTracks(mappedPlaylistTracks)
                    }

                    // 3. Followed Artists
                    if (archive.followedArtists.isNotEmpty()) {
                        followedDao.followArtists(archive.followedArtists.map { it.toEntity() })
                    }

                    // 4. Playback History
                    val validHistory = archive.history.filter { it.trackId in existingTrackSet }
                    if (mode == RestoreMode.REPLACE) {
                        if (validHistory.isNotEmpty()) {
                            historyDao.insertHistories(validHistory.map { it.toEntity() })
                        }
                    } else if (validHistory.isNotEmpty()) {
                        val existingKeys = historyDao.getAllHistoryOnce()
                            .map { it.trackId to it.timestamp }
                            .toHashSet()
                        val delta = validHistory
                            .filter { (it.trackId to it.timestamp) !in existingKeys }
                            .map { it.toEntity() }
                        if (delta.isNotEmpty()) {
                            historyDao.insertHistories(delta)
                        }
                    }

                    // 5. Recent Searches
                    if (archive.recentSearches.isNotEmpty()) {
                        val existingSearches = if (mode == RestoreMode.MERGE) {
                            searchDao.getRecentSearchesOnce().map { it.query.lowercase(Locale.ROOT) to it.type }.toHashSet()
                        } else {
                            emptySet()
                        }
                        val searchesToInsert = archive.recentSearches
                            .filter { (it.query.lowercase(Locale.ROOT) to it.type) !in existingSearches }
                            .map { it.toEntity() }
                        if (searchesToInsert.isNotEmpty()) {
                            searchDao.insertRecentSearches(searchesToInsert)
                        }
                    }

                    // 6. User Profile (and optional cropped avatar restore)
                    archive.profile?.let { profDto ->
                        val existingProf = profileDao.getUserProfileOnce()
                        val shouldRestoreProfile = mode == RestoreMode.REPLACE ||
                            existingProf == null ||
                            (existingProf.name == "EcoDot Listener" && existingProf.favoriteGenres.isBlank())
                        if (shouldRestoreProfile) {
                            val restoredAvatarPath = restoreAvatarFromBase64(profDto.avatarBase64)
                                ?: profDto.avatarUri?.takeIf { it.startsWith("http") || fileChecker(it) }
                            profileDao.updateProfile(
                                UserProfile(
                                    id = 1,
                                    name = profDto.name,
                                    favoriteGenres = profDto.favoriteGenres,
                                    favoriteArtists = profDto.favoriteArtists,
                                    avatarUri = restoredAvatarPath,
                                    isPro = profDto.isPro
                                )
                            )
                        }
                    }

                    // 7. Preferences & Equalizer State
                    restorePreferences(archive.preferences, oldToNewPlaylistId, mode)
                }

                BackupOperationResult(
                    success = true,
                    message = "Restored ${archive.playlists.size} playlists, ${archive.tracks.size} songs & ${archive.followedArtists.size} artists",
                    trackCount = archive.tracks.size,
                    playlistCount = archive.playlists.size,
                    artistCount = archive.followedArtists.size,
                    destinationLabel = "Restored"
                )
            } catch (e: Exception) {
                Log.e(TAG, "restoreArchive failed", e)
                BackupOperationResult(false, e.message ?: "Database restore failed")
            }
        }

    fun getSafFolderDisplayName(treeUriStr: String?): String? {
        if (treeUriStr.isNullOrBlank()) return null
        return runCatching {
            val uri = Uri.parse(treeUriStr)
            val docId = DocumentsContract.getTreeDocumentId(uri)
            val parts = docId.split(":")
            val rootLabel = when {
                parts.firstOrNull().equals("primary", ignoreCase = true) -> "Internal Storage"
                !parts.firstOrNull().isNullOrBlank() -> parts.first()
                else -> "Folder"
            }
            val sub = parts.getOrNull(1)?.takeIf { it.isNotBlank() }
            if (sub != null) "$rootLabel/$sub" else rootLabel
        }.getOrNull()
    }

    suspend fun findLatestBackupInSafTree(treeUri: Uri): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocId)
            var foundUri: Uri? = null
            resolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED
                ),
                null,
                null,
                null
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val modIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                var bestModified = -1L
                while (cursor.moveToNext()) {
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) else ""
                    val docId = if (idIdx >= 0) cursor.getString(idIdx) else continue
                    val mod = if (modIdx >= 0) cursor.getLong(modIdx) else 0L
                    if (name == AUTO_BACKUP_FILENAME) {
                        foundUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        break
                    } else if (name.endsWith(".ecodot", ignoreCase = true) && mod >= bestModified) {
                        bestModified = mod
                        foundUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    }
                }
            }
            foundUri
        }.getOrNull()
    }

    private fun restorePreferences(
        p: BackupPreferencesDto,
        oldToNewPlaylistId: Map<Long, Long>,
        mode: RestoreMode
    ) {
        runCatching { prefsManager.audioQuality = AudioQuality.valueOf(p.audioQuality) }
        runCatching { prefsManager.videoQuality = VideoQuality.valueOf(p.videoQuality) }
        prefsManager.offlineMode = p.offlineMode
        prefsManager.dataSaver = p.dataSaver
        prefsManager.searchHistory = if (mode == RestoreMode.MERGE) {
            (p.searchHistory + prefsManager.searchHistory).distinct().take(20)
        } else {
            p.searchHistory
        }
        prefsManager.killServiceOnExit = p.killServiceOnExit
        prefsManager.crossfadeEnabled = p.crossfadeEnabled
        prefsManager.crossfadeDuration = p.crossfadeDuration
        prefsManager.crossfadeDjMode = p.crossfadeDjMode
        prefsManager.crossfadeSkipAlbum = p.crossfadeSkipAlbum
        prefsManager.normalizeVolume = p.normalizeVolume
        prefsManager.skipSilent = p.skipSilent
        prefsManager.equalizerEnabled = p.equalizerEnabled
        prefsManager.virtualizerStrength = p.virtualizerStrength
        prefsManager.bassBoostStrength = p.bassBoostStrength
        prefsManager.selectedEqPreset = p.selectedEqPreset
        prefsManager.customEqBandsRaw = p.customEqBandsRaw
        prefsManager.customEqPresetsRaw = p.customEqPresetsRaw
        prefsManager.autoCanvasEnabled = p.autoCanvasEnabled
        prefsManager.lockscreenLyricsEnabled = p.lockscreenLyricsEnabled
        prefsManager.sponsorBlockEnabled = p.sponsorBlockEnabled
        prefsManager.sponsorBlockCategories = p.sponsorBlockCategories.toSet()
        runCatching { prefsManager.themeMode = ThemeMode.valueOf(p.themeMode) }
        runCatching { prefsManager.themeColorSource = ThemeColorSource.valueOf(p.themeColorSource) }
        prefsManager.customThemeColorHex = p.customThemeColorHex
        prefsManager.liquidGlassEnabled = p.liquidGlassEnabled
        runCatching { prefsManager.playerStyle = PlayerStyle.valueOf(p.playerStyle) }
        prefsManager.libraryIsGrid = p.libraryIsGrid
        prefsManager.librarySort = p.librarySort

        val remappedPins = p.pinnedPlaylistIds
            .mapNotNull { idStr ->
                val oldId = idStr.toLongOrNull() ?: return@mapNotNull null
                (oldToNewPlaylistId[oldId] ?: oldId).toString()
            }
            .toSet()
        prefsManager.pinnedPlaylistIds = if (mode == RestoreMode.MERGE) {
            prefsManager.pinnedPlaylistIds + remappedPins
        } else {
            remappedPins
        }
        prefsManager.cacheLimitMb = p.cacheLimitMb
        prefsManager.autoCheckUpdates = p.autoCheckUpdates
        prefsManager.selectedHomeCountry = p.selectedHomeCountry
        if (mode == RestoreMode.REPLACE || prefsManager.autoBackupFrequency == BackupSchedule.OFF.key) {
            prefsManager.autoBackupFrequency = p.autoBackupFrequency
        }
    }

    private fun recordBackupSuccess(
        archive: EcoDotBackupArchive,
        destinationLabel: String
    ): BackupOperationResult {
        val now = System.currentTimeMillis()
        val timeFmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(now))
        val summary = "$timeFmt · ${archive.playlists.size} playlists · ${archive.tracks.size} tracks · ${archive.followedArtists.size} artists ($destinationLabel)"
        prefsManager.lastBackupTimestamp = now
        prefsManager.lastBackupSummary = summary
        return BackupOperationResult(
            success = true,
            message = "Backup saved ($destinationLabel)",
            trackCount = archive.tracks.size,
            playlistCount = archive.playlists.size,
            artistCount = archive.followedArtists.size,
            destinationLabel = destinationLabel
        )
    }

    private fun hasPersistedWriteAccess(treeUri: Uri): Boolean {
        return context.contentResolver.persistedUriPermissions.any {
            it.uri == treeUri && it.isWritePermission
        }
    }

    private fun writeToSafTree(treeUri: Uri, fileName: String, bytes: ByteArray): Boolean {
        return runCatching {
            val resolver = context.contentResolver
            val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val parentDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocId)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocId)

            var existingFileUri: Uri? = null
            resolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ),
                null,
                null,
                null
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    if (nameIdx >= 0 && idIdx >= 0 && cursor.getString(nameIdx) == fileName) {
                        existingFileUri = DocumentsContract.buildDocumentUriUsingTree(
                            treeUri,
                            cursor.getString(idIdx)
                        )
                        break
                    }
                }
            }

            val targetUri = existingFileUri ?: DocumentsContract.createDocument(
                resolver,
                parentDocUri,
                "application/octet-stream",
                fileName
            ) ?: return@runCatching false

            val out = resolver.openOutputStream(targetUri, "wt")
                ?: resolver.openOutputStream(targetUri, "w")
                ?: return@runCatching false
            out.use { it.write(bytes) }
            true
        }.onFailure {
            Log.e(TAG, "writeToSafTree failed", it)
        }.getOrDefault(false)
    }

    private fun writeToMediaStoreDownloads(fileName: String, bytes: ByteArray): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return runCatching {
            val resolver = context.contentResolver
            val relPath = "${Environment.DIRECTORY_DOWNLOADS}/$DOWNLOADS_SUBFOLDER/"
            val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

            // Delete previous app-owned auto-backup file so MediaStore doesn't create (1), (2)
            resolver.delete(
                collection,
                "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?",
                arrayOf(fileName, relPath)
            )

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                put(MediaStore.MediaColumns.RELATIVE_PATH, relPath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val itemUri = resolver.insert(collection, values) ?: return@runCatching false
            resolver.openOutputStream(itemUri)?.use { it.write(bytes) }
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(itemUri, values, null, null)
            true
        }.onFailure {
            Log.w(TAG, "MediaStore downloads backup write fallback triggered: ${it.message}")
        }.getOrDefault(false)
    }

    private fun readAvatarBase64(avatarUri: String?): String? {
        if (avatarUri.isNullOrBlank()) return null
        return runCatching {
            val parsed = Uri.parse(avatarUri)
            val bytes = when (parsed.scheme) {
                "file", null -> {
                    val f = File(parsed.path ?: avatarUri)
                    if (f.exists() && f.length() in 1..MAX_AVATAR_BYTES) f.readBytes() else null
                }
                "content" -> {
                    context.contentResolver.openInputStream(parsed)?.use { stream ->
                        val data = stream.readBytes()
                        if (data.size in 1..MAX_AVATAR_BYTES) data else null
                    }
                }
                else -> null
            }
            bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        }.getOrNull()
    }

    private fun restoreAvatarFromBase64(encoded: String?): String? {
        if (encoded.isNullOrBlank()) return null
        return runCatching {
            val data = Base64.decode(encoded, Base64.NO_WRAP)
            if (data.isEmpty() || data.size > MAX_AVATAR_BYTES) return@runCatching null
            val outFile = File(context.filesDir, "user_avatar_restored_${System.currentTimeMillis()}.jpg")
            outFile.writeBytes(data)
            Uri.fromFile(outFile).toString()
        }.getOrNull()
    }

    private fun maxNullable(a: Long?, b: Long?): Long? = when {
        a == null -> b
        b == null -> a
        else -> maxOf(a, b)
    }
}
