package com.example.ecodot.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodot.data.local.entities.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.first

data class AlbumUiState(
    val title: String = "",
    val artistName: String = "",
    val artistId: String? = null,
    val year: String? = null,
    val thumbnailUrl: String? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AlbumViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AlbumViewModel"
    private val database = com.example.ecodot.data.local.database.EcoDotDatabase.getInstance(application)
    private val repository = com.example.ecodot.data.repository.MusicRepository(
        application, 
        database.trackDao(), 
        database.historyDao(),
        database.profileDao(),
        database.playlistDao(),
        database.recentSearchDao(),
        database.followedArtistDao()
    )
    
    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    fun loadAlbum(albumId: String) {
        if (albumId.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // YouTube album browse IDs usually start with MPREb_, OLAK5uy_, VL, or PL
            val isYouTubeAlbum = albumId.startsWith("MPREb") || albumId.startsWith("OLAK5") || albumId.startsWith("VL") || albumId.startsWith("PL")
            
            if (isYouTubeAlbum) {
                var lastError = "Failed to load album details"
                try {
                    val jsonPair = repository.getAlbumJsonPair(albumId)
                    if (jsonPair != null) {
                        val parsed = parseRawAlbumJsonPair(jsonPair.first, jsonPair.second)
                        if (parsed != null) {
                            _uiState.value = parsed.copy(isLoading = false)
                            return@launch
                        } else {
                            if (jsonPair.first.contains("musicElementHeaderRenderer")) {
                                lastError = "Rate limited by YouTube. Please try again later."
                            } else {
                                lastError = "Failed to parse album data (Id: $albumId)"
                            }
                        }
                    } else {
                        lastError = "Network error or rate limit for album: $albumId"
                    }
                } catch (e: Exception) {
                    lastError = e.message ?: "Unknown Exception"
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    error = lastError
                )
            } else {
                // Local album loading
                try {
                    val allTracks = repository.allTracks.first()
                    val albumTracks = allTracks.filter { it.albumId == albumId || it.album == albumId }
                    if (albumTracks.isNotEmpty()) {
                        val firstTrack = albumTracks.first()
                        _uiState.value = AlbumUiState(
                            title = firstTrack.album,
                            artistName = firstTrack.artist,
                            artistId = firstTrack.artistId,
                            thumbnailUrl = firstTrack.albumArtUri,
                            tracks = albumTracks,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Album not found: $albumId"
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load local album"
                    )
                }
            }
        }
    }

    /**
     * Parses album data from two-step raw JSON string pair.
     * Header information is extracted from step1Json (header browse response),
     * and track lists are extracted from step2Json (playlist browse response).
     */
    private fun parseRawAlbumJsonPair(headerJsonStr: String, tracksJsonStr: String): AlbumUiState? {
        return try {
            val headerJson = JSONObject(headerJsonStr)
            val tracksJson = JSONObject(tracksJsonStr)
            
            // -- ANDROID_MUSIC Fallback Parser --
            val elementHeader = headerJson.optJSONObject("header")?.optJSONObject("musicElementHeaderRenderer")
                ?: tracksJson.optJSONObject("header")?.optJSONObject("musicElementHeaderRenderer")
            
            if (elementHeader != null) {
                val aTitle = elementHeader.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: "Unknown Album"
                var aArtist = "Unknown Artist"
                var aThumb = com.example.ecodot.data.remote.YouTubeParser.getHighResUrl(Regex("https://[^\"]+w120-h120[^\"]+").find(headerJsonStr)?.value) ?: ""
                
                val parsedTracks = mutableListOf<Track>()
                
                // Tracks in Android Music are deeply nested
                fun extractTracks(jsonObj: JSONObject?) {
                    if (jsonObj == null) return
                    
                    val tabs = jsonObj.optJSONObject("contents")?.optJSONObject("singleColumnBrowseResultsRenderer")?.optJSONArray("tabs")
                    val tabContents = tabs?.optJSONObject(0)?.optJSONObject("tabRenderer")?.optJSONObject("content")?.optJSONObject("sectionListRenderer")?.optJSONArray("contents")
                    
                    if (tabContents != null) {
                        for (i in 0 until tabContents.length()) {
                            val c = tabContents.optJSONObject(i) ?: continue
                            val wrapper = c.optJSONObject("itemSectionRenderer")?.optJSONArray("contents")
                            if (wrapper != null && wrapper.length() > 0) {
                                for (j in 0 until wrapper.length()) {
                                    val itemData = wrapper.optJSONObject(j)?.optJSONObject("elementRenderer")
                                        ?.optJSONObject("newElement")?.optJSONObject("type")?.optJSONObject("componentType")
                                        ?.optJSONObject("model")?.optJSONObject("musicListItemWrapperModel")
                                        ?.optJSONObject("musicListItemData")
                                    if (itemData != null) {
                                        val titleObj = itemData.optJSONObject("title")
                                        val tTitle = if (titleObj != null) titleObj.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: "" else itemData.optString("title", "")
                                        
                                        val subtitleObj = itemData.optJSONObject("subtitle")
                                        val tSubtitle = if (subtitleObj != null) subtitleObj.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: "" else itemData.optString("subtitle", "")

                                        val tVid = itemData.optJSONObject("onTap")?.optJSONObject("innertubeCommand")?.optJSONObject("watchEndpoint")?.optString("videoId")
                                        
                                        if (tTitle.isNotEmpty() && !tVid.isNullOrEmpty()) {
                                            val parts = tSubtitle.split("   ")
                                            val trackArtist = if (parts.isNotEmpty()) parts[0] else "Unknown Artist"
                                            if (aArtist == "Unknown Artist") aArtist = trackArtist
                                            
                                            var durationMs = 0L
                                            if (parts.size > 1) {
                                                val timeParts = parts[1].split(":")
                                                if (timeParts.size == 2) {
                                                    durationMs = (timeParts[0].toLongOrNull() ?: 0L) * 60000L + (timeParts[1].toLongOrNull() ?: 0L) * 1000L
                                                } else if (timeParts.size == 3) {
                                                    durationMs = (timeParts[0].toLongOrNull() ?: 0L) * 3600000L + (timeParts[1].toLongOrNull() ?: 0L) * 60000L + (timeParts[2].toLongOrNull() ?: 0L) * 1000L
                                                }
                                            }
                                            
                                            parsedTracks.add(
                                                Track(
                                                    id = tVid,
                                                    title = tTitle,
                                                    artist = trackArtist,
                                                    album = aTitle,
                                                    albumArtUri = aThumb,
                                                    duration = durationMs,
                                                    path = "",
                                                    isYouTube = true
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                extractTracks(headerJson)
                extractTracks(tracksJson)
                
                if (parsedTracks.isNotEmpty()) {
                    return AlbumUiState(
                        title = aTitle,
                        artistName = aArtist,
                        artistId = null,
                        thumbnailUrl = aThumb,
                        year = null,
                        tracks = parsedTracks
                    )
                }
            }
            // -- End ANDROID_MUSIC Fallback Parser ----

            // ── 1. Header Parsing (from headerJson) ───────────────────────────
            var title = ""
            var artistName = "Various Artists"
            var artistId: String? = null
            var year: String? = null
            var thumbnailUrl: String? = null
            
            // Try microformatDataRenderer (new API structure)
            val microformatData = headerJson.optJSONObject("microformat")?.optJSONObject("microformatDataRenderer")
            if (microformatData != null) {
                val fullTitle = microformatData.optString("title")
                if (fullTitle.contains(" - Album by ")) {
                    title = fullTitle.substringBefore(" - Album by ")
                    artistName = fullTitle.substringAfter(" - Album by ")
                } else if (fullTitle.contains(" - EP by ")) {
                    title = fullTitle.substringBefore(" - EP by ")
                    artistName = fullTitle.substringAfter(" - EP by ")
                } else if (fullTitle.contains(" - Single by ")) {
                    title = fullTitle.substringBefore(" - Single by ")
                    artistName = fullTitle.substringAfter(" - Single by ")
                } else {
                    title = fullTitle
                }
                
                val thumbs = microformatData.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                if (thumbs != null && thumbs.length() > 0) {
                    thumbnailUrl = thumbs.optJSONObject(thumbs.length() - 1)?.optString("url")
                }
            }

            // Fallback to older header structures
            val header = headerJson.optJSONObject("header")
            
            // Try musicDetailHeaderRenderer (most common for albums)
            val detailHeader = header?.optJSONObject("musicDetailHeaderRenderer")
            if (detailHeader != null && title.isEmpty()) {
                title = detailHeader.optJSONObject("title")
                    ?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: ""
                
                // Subtitle usually: [ArtistName, " • ", "Album", " • ", "Year"]  
                // OR: [Type, " • ", ArtistName, " • ", "Year"]
                val subtitleRuns = detailHeader.optJSONObject("subtitle")?.optJSONArray("runs")
                if (subtitleRuns != null) {
                    for (i in 0 until subtitleRuns.length()) {
                        val run = subtitleRuns.optJSONObject(i)
                        val text = run?.optString("text") ?: continue
                        val runBrowseId = run.optJSONObject("navigationEndpoint")
                            ?.optJSONObject("browseEndpoint")?.optString("browseId")
                        
                        if (runBrowseId?.startsWith("UC") == true) {
                            artistName = text
                            artistId = runBrowseId
                        } else if (text.length == 4 && text.all { it.isDigit() }) {
                            year = text
                        }
                    }
                }
                
                // Thumbnail: try thumbnail first, then foregroundThumbnail
                thumbnailUrl = getBestThumbnail(detailHeader.optJSONObject("thumbnail"))
                    ?: getBestThumbnail(detailHeader.optJSONObject("foregroundThumbnail"))
                    
                Log.d(TAG, "Parsed detailHeader: title=$title artist=$artistName year=$year thumb=$thumbnailUrl")
            }
            
            // Try musicImmersiveHeaderRenderer (used for some albums/playlists)
            if (title.isEmpty()) {
                val immersiveHeader = header?.optJSONObject("musicImmersiveHeaderRenderer")
                if (immersiveHeader != null) {
                    title = immersiveHeader.optJSONObject("title")
                        ?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: ""
                    thumbnailUrl = getBestThumbnail(immersiveHeader.optJSONObject("thumbnail"))
                        ?: getBestThumbnail(immersiveHeader.optJSONObject("foregroundThumbnail"))
                }
            }
            
            // Try musicVisualHeaderRenderer
            if (title.isEmpty()) {
                val visualHeader = header?.optJSONObject("musicVisualHeaderRenderer")
                if (visualHeader != null) {
                    title = visualHeader.optJSONObject("title")
                        ?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: ""
                    thumbnailUrl = getBestThumbnail(visualHeader.optJSONObject("thumbnail"))
                        ?: getBestThumbnail(visualHeader.optJSONObject("foregroundThumbnail"))
                }
            }
            
            // ── 2. Track Parsing (from tracksJson) ────────────────────────────
            val tracks = mutableListOf<Track>()
            val contents = tracksJson.optJSONObject("contents")
            
            // Try all possible content paths
            val sections = findSectionContents(contents)
            
            for (section in sections) {
                extractTracksFromSection(section, tracks, artistName, artistId, title, thumbnailUrl)
            }
            
            Log.d(TAG, "Parsed ${tracks.size} tracks for album: $title")
            
            if (title.isEmpty() && tracks.isEmpty()) {
                Log.w(TAG, "Album parse resulted in empty data, returning null")
                return null
            }
            
            AlbumUiState(
                title = title,
                artistName = artistName,
                artistId = artistId,
                year = year,
                thumbnailUrl = thumbnailUrl,
                tracks = tracks,
                isLoading = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse raw album JSON pair", e)
            null
        }
    }
    
    /**
     * Finds all section content arrays from any possible content structure.
     */
    private fun findSectionContents(contents: JSONObject?): List<JSONObject> {
        if (contents == null) return emptyList()
        val sections = mutableListOf<JSONObject>()
        
        // Try singleColumnBrowseResultsRenderer
        val singleCol = contents.optJSONObject("singleColumnBrowseResultsRenderer")
        singleCol?.optJSONArray("tabs")?.optJSONObject(0)
            ?.optJSONObject("tabRenderer")?.optJSONObject("content")
            ?.optJSONObject("sectionListRenderer")?.optJSONArray("contents")
            ?.let { arr -> for (i in 0 until arr.length()) arr.optJSONObject(i)?.let { sections.add(it) } }
        
        // Try twoColumnBrowseResultsRenderer
        if (sections.isEmpty()) {
            val twoCol = contents.optJSONObject("twoColumnBrowseResultsRenderer")
            
            // 1. Try secondaryContents (often used for album tracklists in new APIs)
            val secondaryContents = twoCol?.optJSONObject("secondaryContents")
                ?.optJSONObject("sectionListRenderer")?.optJSONArray("contents")
            if (secondaryContents != null) {
                for (i in 0 until secondaryContents.length()) {
                    secondaryContents.optJSONObject(i)?.let { sections.add(it) }
                }
            }
            
            // 2. Try tabs
            if (sections.isEmpty()) {
                twoCol?.optJSONArray("tabs")?.optJSONObject(0)
                    ?.optJSONObject("tabRenderer")?.optJSONObject("content")
                    ?.optJSONObject("sectionListRenderer")?.optJSONArray("contents")
                    ?.let { arr -> for (i in 0 until arr.length()) arr.optJSONObject(i)?.let { sections.add(it) } }
            }
        }
        
        // Try direct sectionListRenderer
        if (sections.isEmpty()) {
            contents.optJSONObject("sectionListRenderer")?.optJSONArray("contents")
                ?.let { arr -> for (i in 0 until arr.length()) arr.optJSONObject(i)?.let { sections.add(it) } }
        }
        
        return sections
    }
    
    /**
     * Extracts tracks from a single section JSON object.
     * Handles musicShelfRenderer, musicPlaylistShelfRenderer, and musicCarouselShelfRenderer.
     */
    private fun extractTracksFromSection(
        section: JSONObject, 
        tracks: MutableList<Track>,
        albumArtist: String,
        albumArtistId: String?,
        albumTitle: String,
        albumArtUrl: String?
    ) {
        // musicShelfRenderer
        val shelfContents = section.optJSONObject("musicShelfRenderer")?.optJSONArray("contents")
            ?: section.optJSONObject("musicPlaylistShelfRenderer")?.optJSONArray("contents")
        
        shelfContents?.let { arr ->
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val renderer = item.optJSONObject("musicResponsiveListItemRenderer") ?: continue
                parseTrackFromRawRenderer(renderer, albumArtist, albumArtistId, albumTitle, albumArtUrl)?.let {
                    if (it.id.isNotEmpty()) tracks.add(it)
                }
            }
        }
    }
    
    /**
     * Parses a single track from a raw musicResponsiveListItemRenderer JSON.
     * Tries all possible paths to get videoId.
     */
    private fun parseTrackFromRawRenderer(
        renderer: JSONObject,
        albumArtist: String,
        albumArtistId: String?,
        albumTitle: String,
        albumArtUrl: String?
    ): Track? {
        // Try all ways to get videoId
        val videoId = renderer.optJSONObject("playlistItemData")?.optString("videoId")?.takeIf { it.isNotEmpty() }
            ?: renderer.optJSONObject("overlay")
                ?.optJSONObject("musicItemThumbnailOverlayRenderer")
                ?.optJSONObject("content")
                ?.optJSONObject("musicPlayButtonRenderer")
                ?.optJSONObject("playNavigationEndpoint")
                ?.optJSONObject("watchEndpoint")
                ?.optString("videoId")?.takeIf { it.isNotEmpty() }
            ?: renderer.optJSONObject("navigationEndpoint")
                ?.optJSONObject("watchEndpoint")
                ?.optString("videoId")?.takeIf { it.isNotEmpty() }
            ?: run {
                // Try from flexColumns title runs navigationEndpoint
                val flexCols = renderer.optJSONArray("flexColumns")
                if (flexCols != null) {
                    for (i in 0 until flexCols.length()) {
                        val runs = flexCols.optJSONObject(i)
                            ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                            ?.optJSONObject("text")?.optJSONArray("runs")
                        if (runs != null) {
                            for (j in 0 until runs.length()) {
                                val vid = runs.optJSONObject(j)
                                    ?.optJSONObject("navigationEndpoint")
                                    ?.optJSONObject("watchEndpoint")
                                    ?.optString("videoId")
                                if (!vid.isNullOrEmpty()) return@run vid
                            }
                        }
                    }
                }
                null
            }
        
        if (videoId.isNullOrEmpty()) return null
        
        val flexColumns = renderer.optJSONArray("flexColumns")
        val titleRuns = flexColumns?.optJSONObject(0)
            ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
            ?.optJSONObject("text")?.optJSONArray("runs")
        val subtitleRuns = flexColumns?.optJSONObject(1)
            ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
            ?.optJSONObject("text")?.optJSONArray("runs")
        
        val title = titleRuns?.optJSONObject(0)?.optString("text") ?: "Unknown"
        val trackArtist = if (subtitleRuns != null && subtitleRuns.length() > 0) {
            val parsed = com.example.ecodot.data.remote.YouTubeParser.parseMetadataFromJsonRuns(subtitleRuns).first
            if (parsed != "Unknown") parsed else albumArtist
        } else {
            albumArtist
        }

        val isExplicit = title.contains("explicit", true) || albumTitle.contains("explicit", true) || run {
            val badges = renderer.optJSONArray("badges")
            var explicit = false
            if (badges != null) {
                for (j in 0 until badges.length()) {
                    val badgeObj = badges.optJSONObject(j)
                    if (badgeObj != null) {
                        if (badgeObj.has("musicInlineExplicitBadgeRenderer")) {
                            explicit = true
                            break
                        }
                        val inlineBadge = badgeObj.optJSONObject("musicInlineBadgeRenderer")
                        if (inlineBadge != null) {
                            val iconType = inlineBadge.optJSONObject("icon")?.optString("iconType")
                            if (iconType == "MUSIC_EXPLICIT_BADGE") {
                                explicit = true
                                break
                            }
                        }
                    }
                }
            }
            explicit
        }
        
        // Get track thumbnail (may not always be present in album track lists)
        val thumbUrl = getBestThumbnailFromArray(
            renderer.optJSONObject("thumbnail")
                ?.optJSONObject("musicThumbnailRenderer")
                ?.optJSONObject("thumbnail")
                ?.optJSONArray("thumbnails")
        ) ?: albumArtUrl
        
        return Track(
            id = videoId,
            title = title,
            artist = trackArtist,
            album = albumTitle,
            duration = 0,
            path = "https://www.youtube.com/watch?v=$videoId",
            albumArtUri = thumbUrl,
            isYouTube = true,
            artistId = albumArtistId,
            isExplicit = isExplicit
        )
    }

    /**
     * Fallback: parse from Moshi-mapped YouTubeBrowseResponse model.
     */
    private fun parseMoshiAlbumResponse(response: com.example.ecodot.data.remote.YouTubeBrowseResponse) {
        val header = response.header
        var title = ""
        var artistName = "Various Artists"
        var artistId: String? = null
        var year: String? = null
        var thumbnailUrl: String? = null

        header?.musicDetailHeaderRenderer?.let { h ->
            title = h.title?.runs?.getOrNull(0)?.text ?: ""
            thumbnailUrl = com.example.ecodot.data.remote.YouTubeParser.getHighResUrl(h.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url)
            h.subtitle?.runs?.let { runs ->
                runs.forEach { run ->
                    if (run.navigationEndpoint?.browseEndpoint?.browseId?.startsWith("UC") == true) {
                        artistName = run.text
                        artistId = run.navigationEndpoint.browseEndpoint.browseId
                    }
                    if (run.text.length == 4 && run.text.all { it.isDigit() }) {
                        year = run.text
                    }
                }
            }
        } ?: header?.musicVisualHeaderRenderer?.let { h ->
            title = h.title?.runs?.getOrNull(0)?.text ?: ""
            thumbnailUrl = com.example.ecodot.data.remote.YouTubeParser.getHighResUrl(h.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url)
        } ?: header?.musicImmersiveHeaderRenderer?.let { h ->
            title = h.title?.runs?.getOrNull(0)?.text ?: ""
            thumbnailUrl = com.example.ecodot.data.remote.YouTubeParser.getHighResUrl(h.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url)
        }

        val sections = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.getOrNull(0)
            ?.tabRenderer?.content?.sectionListRenderer?.contents 
            ?: response.contents?.twoColumnBrowseResultsRenderer?.tabs?.getOrNull(0)
            ?.tabRenderer?.content?.sectionListRenderer?.contents
            ?: response.contents?.sectionListRenderer?.contents
            ?: emptyList()

        val tracks = mutableListOf<Track>()
        sections.forEach { section ->
            val contents = section.musicShelfRenderer?.contents
                ?: section.musicPlaylistShelfRenderer?.contents

            contents?.forEach { item ->
                item.musicResponsiveListItemRenderer?.let { renderer ->
                    val videoId = renderer.playlistItemData?.videoId 
                        ?: renderer.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId
                        ?: renderer.navigationEndpoint?.watchEndpoint?.videoId
                        ?: ""
                    if (videoId.isNotEmpty()) {
                        val titleFlex = renderer.flexColumns?.getOrNull(0)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                        val trackTitle = titleFlex?.getOrNull(0)?.text ?: "Unknown"
                        val subtitleFlex = renderer.flexColumns?.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                        val trackArtist = if (subtitleFlex != null) {
                            val parsed = com.example.ecodot.data.remote.YouTubeParser.parseMetadataFromRuns(subtitleFlex).first
                            if (parsed != "Unknown") parsed else artistName
                        } else {
                            artistName
                        }
                        val isExplicit = renderer.badges?.any { 
                            it.musicInlineExplicitBadgeRenderer != null || 
                            (it.musicInlineBadgeRenderer != null && it.musicInlineBadgeRenderer.icon?.iconType == "MUSIC_EXPLICIT_BADGE")
                        } == true || trackTitle.contains("explicit", true) || title.contains("explicit", true)
                        val rawThumb = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
                        val thumbUrl = com.example.ecodot.data.remote.YouTubeParser.getHighResUrl(rawThumb)
                        tracks.add(Track(
                            id = videoId, title = trackTitle, artist = trackArtist,
                            album = title, duration = 0,
                            path = "https://www.youtube.com/watch?v=$videoId",
                            albumArtUri = thumbUrl ?: thumbnailUrl, isYouTube = true, artistId = artistId,
                            isExplicit = isExplicit
                        ))
                    }
                }
            }
        }

        _uiState.value = AlbumUiState(
            title = title, artistName = artistName, artistId = artistId,
            year = year, thumbnailUrl = thumbnailUrl, tracks = tracks, isLoading = false
        )
    }

    // ── Thumbnail Helpers ──────────────────────────────────────────────────────
    
    private fun getBestThumbnail(thumbnailObj: JSONObject?): String? {
        if (thumbnailObj == null) return null
        // Try musicThumbnailRenderer wrapper
        val inner = thumbnailObj.optJSONObject("musicThumbnailRenderer")
            ?.optJSONObject("thumbnail")
            ?.optJSONArray("thumbnails")
            ?: thumbnailObj.optJSONArray("thumbnails")
        return getBestThumbnailFromArray(inner)
    }
    
    private fun getBestThumbnailFromArray(arr: JSONArray?): String? {
        if (arr == null || arr.length() == 0) return null
        // Return the last (usually highest resolution) thumbnail
        val rawUrl = arr.optJSONObject(arr.length() - 1)?.optString("url")?.takeIf { it.isNotEmpty() }
        return com.example.ecodot.data.remote.YouTubeParser.getHighResUrl(rawUrl)
    }
}

