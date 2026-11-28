package com.example.ecodot.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.ecodot.data.local.dao.*
import com.example.ecodot.data.local.entities.*
import com.example.ecodot.data.remote.LyricsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLDecoder
import okio.buffer
import okio.sink
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.example.ecodot.ui.viewmodel.Album
import com.example.ecodot.ui.viewmodel.Artist
import com.example.ecodot.ui.viewmodel.InstantSearchItem
import com.example.ecodot.data.remote.*
import com.example.ecodot.data.local.prefs.AudioQuality
import com.example.ecodot.data.local.prefs.VideoQuality


class MusicRepository(
    private val context: Context,
    private val trackDao: TrackDao,
    private val historyDao: PlaybackHistoryDao,
    private val profileDao: UserProfileDao,
    private val playlistDao: PlaylistDao,
    private val recentSearchDao: RecentSearchDao,
    private val followedArtistDao: FollowedArtistDao,
    private val streamResolver: com.example.ecodot.data.remote.YouTubeStreamResolver = com.example.ecodot.data.remote.YouTubeStreamResolver(sharedHttpClient)
) : com.example.ecodot.domain.repository.IMusicRepository {
    
    private val lyricsRepository = LyricsRepository()
    
    override val allTracks: Flow<List<Track>> = trackDao.getAllTracks()
    override val recentlyPlayed: Flow<List<Track>> = trackDao.getRecentlyPlayed()
    override val mostPlayed: Flow<List<Track>> = trackDao.getMostPlayed()
    override val userProfile: Flow<UserProfile?> = profileDao.getUserProfile()
    override val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    override val playbackHistory: Flow<List<PlaybackHistoryWithTrack>> = historyDao.getHistoryWithTracks()
    override val recentSearches: Flow<List<RecentSearchItem>> = recentSearchDao.getRecentSearches()
    override val followedArtists: Flow<List<FollowedArtist>> = followedArtistDao.getFollowedArtists()

    fun getOnRepeatTracks(sinceTimestamp: Long): Flow<List<Track>> {
        return historyDao.getTopTracksSince(sinceTimestamp)
    }


    override suspend fun followArtist(artistId: String, name: String, imageUrl: String?) {
        followedArtistDao.followArtist(FollowedArtist(artistId, name, imageUrl))
    }

    override suspend fun unfollowArtist(artistId: String) {
        followedArtistDao.unfollowArtist(artistId)
    }

    fun isArtistFollowed(artistId: String): Flow<Boolean> {
        return followedArtistDao.isArtistFollowed(artistId)
    }

    suspend fun addRecentSearch(
        query: String,
        type: String,
        itemId: String? = null,
        imageUrl: String? = null,
        subtitle: String? = null
    ) = withContext(Dispatchers.IO) {
        recentSearchDao.deleteByQueryAndType(query, type)
        recentSearchDao.insertRecentSearch(
            RecentSearchItem(
                query = query,
                type = type,
                itemId = itemId,
                imageUrl = imageUrl,
                subtitle = subtitle,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeRecentSearch(item: RecentSearchItem) = withContext(Dispatchers.IO) {
        recentSearchDao.deleteRecentSearch(item)
    }

    suspend fun clearRecentSearches() = withContext(Dispatchers.IO) {
        recentSearchDao.clearAllRecentSearches()
    }

    suspend fun saveTracks(tracks: List<Track>) = withContext(Dispatchers.IO) {
        trackDao.insertTracks(tracks)
    }

    suspend fun saveTrack(track: Track) = withContext(Dispatchers.IO) {
        trackDao.insertTracks(listOf(track))
    }

    suspend fun deleteTrack(track: Track) = withContext(Dispatchers.IO) {
        trackDao.deleteTrack(track)
    }

    // --- Playlist management ---
    suspend fun createPlaylist(name: String, description: String = ""): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(Playlist(name = name, description = description))
    }

    suspend fun renamePlaylist(playlist: Playlist, newName: String) = withContext(Dispatchers.IO) {
        playlistDao.updatePlaylist(playlist.copy(name = newName))
    }

    suspend fun updatePlaylistDetails(playlist: Playlist, name: String, description: String, coverArtUri: String?) = withContext(Dispatchers.IO) {
        playlistDao.updatePlaylist(playlist.copy(name = name, description = description, coverArtUri = coverArtUri))
    }

    suspend fun reorderTracks(playlistId: Long, trackIdsInOrder: List<String>) = withContext(Dispatchers.IO) {
        playlistDao.reorderTracks(playlistId, trackIdsInOrder)
    }

    fun getPlaylistByIdFlow(playlistId: Long): Flow<Playlist?> = playlistDao.getPlaylistByIdFlow(playlistId)

    suspend fun updatePlaylistFolder(playlist: Playlist, folderName: String?) = withContext(Dispatchers.IO) {
        playlistDao.updatePlaylist(playlist.copy(folder = folderName))
    }

    suspend fun deletePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, track: Track) = withContext(Dispatchers.IO) {
        // Ensure the track exists in the tracks table to prevent Foreign Key constraint failures
        trackDao.insertTracks(listOf(track))
        playlistDao.addTrackToPlaylist(PlaylistTrack(playlistId = playlistId, trackId = track.id))
        
        // Auto-update playlist cover art if it's currently empty
        val playlist = playlistDao.getPlaylistById(playlistId)
        if (playlist != null && playlist.coverArtUri.isNullOrEmpty() && !track.albumArtUri.isNullOrEmpty()) {
            playlistDao.updatePlaylist(playlist.copy(coverArtUri = track.albumArtUri))
        }
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeTrackFromPlaylist(PlaylistTrack(playlistId = playlistId, trackId = trackId))
    }

    fun getTracksInPlaylist(playlistId: Long): Flow<List<Track>> = playlistDao.getTracksInPlaylist(playlistId)
    fun getAllPlaylistTrackIds(): Flow<List<String>> = playlistDao.getAllPlaylistTrackIds()
    fun getPlaylistsContainingTrack(trackId: String): Flow<List<Playlist>> = playlistDao.getPlaylistsContainingTrack(trackId)
    fun getPlaylistTrackCount(playlistId: Long): Flow<Int> = playlistDao.getPlaylistTrackCount(playlistId)
    fun isTrackLiked(trackId: String): Flow<Boolean> = playlistDao.isTrackLiked(trackId)
    fun getLikedTrackIds(): Flow<Set<String>> = playlistDao.getLikedTrackIds().map { it.toSet() }

    suspend fun getPlaylistByName(name: String): Playlist? = withContext(Dispatchers.IO) {
        playlistDao.getPlaylistByName(name)
    }

    suspend fun isTrackLikedSuspended(trackId: String): Boolean = withContext(Dispatchers.IO) {
        playlistDao.isTrackLikedSuspended(trackId)
    }

    suspend fun searchTracksLocally(query: String): List<Track> = withContext(Dispatchers.IO) {
        trackDao.searchTracksLocally(query)
    }

    suspend fun initDefaultPlaylists() = withContext(Dispatchers.IO) {
        if (playlistDao.getPlaylistByName("Liked Songs") == null) {
            playlistDao.insertPlaylist(Playlist(name = "Liked Songs", description = "Your favourite tracks"))
        }
        if (playlistDao.getPlaylistByName("Downloads") == null) {
            playlistDao.insertPlaylist(Playlist(name = "Downloads", description = "Downloaded tracks"))
        }
    }

    suspend fun toggleTrackLike(track: Track) = withContext(Dispatchers.IO) {
        // Ensure "Liked Songs" playlist is created. Under normal flow initDefaultPlaylists handles this,
        // but let's fall back just in case.
        val likedId = playlistDao.getPlaylistByName("Liked Songs")?.id
            ?: playlistDao.insertPlaylist(Playlist(name = "Liked Songs", description = "Your favourite tracks"))
        
        val alreadyLiked = playlistDao.isTrackLikedSuspended(track.id)
        if (alreadyLiked) {
            playlistDao.removeTrackFromPlaylist(PlaylistTrack(playlistId = likedId, trackId = track.id))
        } else {
            // Save metadata first to satisfy foreign key constraints
            trackDao.insertTracks(listOf(track))
            playlistDao.addTrackToPlaylist(PlaylistTrack(playlistId = likedId, trackId = track.id))
            
            // Auto-update playlist cover art if it's currently empty
            val playlist = playlistDao.getPlaylistById(likedId)
            if (playlist != null && playlist.coverArtUri.isNullOrEmpty() && !track.albumArtUri.isNullOrEmpty()) {
                playlistDao.updatePlaylist(playlist.copy(coverArtUri = track.albumArtUri))
            }
        }
    }

    
    // ---- NETWORK LOGIC IMPORTED FROM YouTubeApiClient ----
        companion object {
        private const val TAG = "MusicRepository"
        // Cache visitorData for session tracking globally across all repository instances
        var cachedVisitorData: String? = null
        
        val sharedHttpClient: okhttp3.OkHttpClient by lazy {
            okhttp3.OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build()
        }

        val apiService: YouTubeApiService by lazy {
            Retrofit.Builder()
                .baseUrl("https://music.youtube.com/youtubei/v1/")
                .client(sharedHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(YouTubeApiService::class.java)
        }
    }

    enum class Client(
        val clientName: String,
        val clientVersion: String,
        val userAgent: String,
        val clientLabel: String? = null
    ) {
        WEB_REMIX(
            "WEB_REMIX",
            "1.20240722.01.00",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36",
            "67"
        ),
        ANDROID_MUSIC(
            "ANDROID_MUSIC",
            "7.03.52",
            "com.google.android.apps.youtube.music/70352 (Linux; U; Android 14; en_US; V2334; Build/UP1A.231005.007;)",
            "26"
        ),
        ANDROID(
            "ANDROID",
            "19.29.37",
            "com.google.android.youtube/19.29.37 (Linux; U; Android 14; en_US; V2334; Build/UP1A.231005.007;)",
            "3"
        ),
        TVHTML5(
            "TVHTML5",
            "7.20241022.01.01",
            "Mozilla/5.0 (WebOS; Linux/SmartTV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36 SmartTV",
            "85"
        ),
        WEB(
            "WEB",
            "2.20241022.01.00",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"
        ),
        IOS(
            "IOS",
            "19.38.3",
            "com.google.ios.youtube/19.38.3 (iPhone16,2; U; CPU iOS 17_5_1 like Mac OS X;)",
            "2"
        ),
        MWEB(
            "MWEB",
            "2.20241022.01.00",
            "Mozilla/5.0 (Linux; Android 14; V2334) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.36",
            "2"
        ),
        ANDROID_VR(
            "ANDROID_VR",
            "1.50.41",
            "com.google.android.apps.youtube.vr/1.50.41 (Linux; U; Android 14; en_US; V2334; Build/UP1A.231005.007;)",
            "32"
        ),
        ANDROID_TESTSUITE(
            "ANDROID_TESTSUITE",
            "1.9.3",
            "com.google.android.youtube.testsuite/1.9.3 (Linux; U; Android 14; en_US; V2334; Build/UP1A.231005.007;)",
            "30"
        ),
        ANDROID_EMBEDDED_PLAYER(
            "ANDROID_EMBEDDED_PLAYER",
            "19.29.37",
            "Mozilla/5.0 (Linux; Android 14; V2334; Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/127.0.6533.103 Mobile Safari/537.36",
            "55"
        )
    }





    private fun getContext(client: Client): JSONObject {
        return JSONObject().apply {
            put("client", JSONObject().apply {
                put("clientName", client.clientName)
                put("clientVersion", client.clientVersion)
                put("hl", "en")
                put("gl", "US")
                put("utcOffsetMinutes", 0)
                
                if (cachedVisitorData != null) {
                    put("visitorData", cachedVisitorData)
                }

                when (client) {
                    Client.ANDROID_MUSIC, Client.ANDROID, Client.ANDROID_VR, Client.ANDROID_TESTSUITE -> {
                        put("androidSdkVersion", 34)
                        put("osName", "Android")
                        put("osVersion", "14")
                        put("platform", "MOBILE")
                    }
                    Client.IOS -> {
                        put("osName", "iOS")
                        put("osVersion", "17.5.1")
                        put("platform", "MOBILE")
                        put("deviceMake", "Apple")
                        put("deviceModel", "iPhone16,2")
                    }
                    else -> {}
                }
            })
            put("user", JSONObject().apply {
                put("lockedSafetyMode", false)
            })
        }
    }

    private fun getHeaders(client: Client): Map<String, String> {
        val headers = mutableMapOf(
            "Content-Type" to "application/json",
            "User-Agent" to client.userAgent,
            "Origin" to "https://music.youtube.com",
            "Referer" to "https://music.youtube.com/"
        )
        
        // Android / Non-Web clients often require strict YouTube headers
        // WEB_REMIX without these headers acts like a normal browser request (matching ytmusicapi), avoiding bot detection
        if (client != Client.WEB_REMIX) {
            headers["X-Goog-Api-Format-Return-Instruction"] = "true"
            headers["X-YouTube-Client-Name"] = client.clientLabel ?: "1"
            headers["X-YouTube-Client-Version"] = client.clientVersion
        }
        
        cachedVisitorData?.let {
            headers["X-Goog-Visitor-Id"] = it
        }
        return headers
    }


    data class SearchSuggestionsData(
        val suggestions: List<String>,
        val instantItems: List<InstantSearchItem>
    )

    suspend fun getSearchSuggestions(query: String): List<String> {
        return getSearchSuggestionsAndInstantMatches(query).suggestions
    }

    suspend fun getSearchSuggestionsAndInstantMatches(query: String): SearchSuggestionsData = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val suggestions = mutableListOf<String>()
        val instantItems = mutableListOf<InstantSearchItem>()

        try {
            val jsonBody = org.json.JSONObject().apply {
                put("context", org.json.JSONObject().apply {
                    put("client", org.json.JSONObject().apply {
                        put("clientName", "WEB_REMIX")
                        put("clientVersion", "1.20240101.01.00")
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
                put("input", query)
            }
            val requestBody = okhttp3.RequestBody.create(null, jsonBody.toString())
            val request = okhttp3.Request.Builder()
                .url("https://music.youtube.com/youtubei/v1/music/get_search_suggestions")
                .post(requestBody)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://music.youtube.com/")
                .build()

            val responseStr = sharedHttpClient.newCall(request).execute().body?.string().orEmpty()
            val json = org.json.JSONObject(responseStr)
            val contents = json.optJSONArray("contents")

            if (contents != null) {
                for (c in 0 until contents.length()) {
                    val secRenderer = contents.optJSONObject(c)?.optJSONObject("searchSuggestionsSectionRenderer")
                    val items = secRenderer?.optJSONArray("contents") ?: continue

                    for (i in 0 until items.length()) {
                        val item = items.optJSONObject(i) ?: continue

                        // 1. Textual Query Autocomplete Suggestion
                        val sug = item.optJSONObject("searchSuggestionRenderer")
                        if (sug != null) {
                            val runs = sug.optJSONObject("suggestion")?.optJSONArray("runs")
                            if (runs != null) {
                                val sb = StringBuilder()
                                for (r in 0 until runs.length()) {
                                    sb.append(runs.getJSONObject(r).optString("text", ""))
                                }
                                val text = sb.toString().trim()
                                if (text.isNotEmpty() && !suggestions.contains(text)) {
                                    suggestions.add(text)
                                }
                            }
                        }

                        // 2. Instant Media Matches (Top Artist or Top Songs or Album)
                        val resp = item.optJSONObject("musicResponsiveListItemRenderer")
                        if (resp != null) {
                            val flexCols = resp.optJSONArray("flexColumns")
                            val title = flexCols?.optJSONObject(0)
                                ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                                ?.optJSONObject("text")
                                ?.optJSONArray("runs")
                                ?.optJSONObject(0)
                                ?.optString("text")
                                .orEmpty()

                            val subRuns = flexCols?.optJSONObject(1)
                                ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                                ?.optJSONObject("text")
                                ?.optJSONArray("runs")

                            val subSb = StringBuilder()
                            if (subRuns != null) {
                                for (sr in 0 until subRuns.length()) {
                                    subSb.append(subRuns.getJSONObject(sr).optString("text", ""))
                                }
                            }
                            val fullSub = subSb.toString().trim()

                            // Get thumbnail URL
                            val thumbArray = resp.optJSONObject("thumbnail")
                                ?.optJSONObject("musicThumbnailRenderer")
                                ?.optJSONObject("thumbnail")
                                ?.optJSONArray("thumbnails")
                            var thumbUrl: String? = null
                            if (thumbArray != null && thumbArray.length() > 0) {
                                thumbUrl = YouTubeParser.getHighResUrl(
                                    thumbArray.getJSONObject(thumbArray.length() - 1).optString("url")
                                )
                            }

                            val nav = resp.optJSONObject("navigationEndpoint")
                            val browseId = nav?.optJSONObject("browseEndpoint")?.optString("browseId")
                            val videoId = nav?.optJSONObject("watchEndpoint")?.optString("videoId")

                            val isArtist = (browseId != null && browseId.startsWith("UC")) ||
                                    fullSub.contains("audience", ignoreCase = true) ||
                                    fullSub.contains("artist", ignoreCase = true)

                            if (isArtist && browseId != null && title.isNotEmpty()) {
                                if (instantItems.none { it is InstantSearchItem.ArtistMatch }) {
                                    instantItems.add(
                                        InstantSearchItem.ArtistMatch(
                                            id = browseId,
                                            name = title,
                                            thumbnailUrl = thumbUrl
                                        )
                                    )
                                }
                            } else if (!videoId.isNullOrEmpty() && title.isNotEmpty()) {
                                val parts = fullSub.split(" • ")
                                val artistName = if (parts.size >= 2) parts[1].trim() else fullSub
                                val track = Track(
                                    id = videoId,
                                    title = title,
                                    artist = artistName.ifEmpty { "YouTube Music" },
                                    album = "YouTube Music",
                                    duration = 0,
                                    path = "https://www.youtube.com/watch?v=$videoId",
                                    albumArtUri = thumbUrl,
                                    isYouTube = true
                                )
                                if (instantItems.none { it is InstantSearchItem.SongMatch && it.track.id == videoId }) {
                                    instantItems.add(InstantSearchItem.SongMatch(track))
                                }
                            } else if (browseId != null && browseId.startsWith("MPREb") && title.isNotEmpty()) {
                                val parts = fullSub.split(" • ")
                                val artistName = if (parts.size >= 2) parts[1].trim() else "Album"
                                instantItems.add(
                                    InstantSearchItem.AlbumMatch(
                                        id = browseId,
                                        title = title,
                                        artist = artistName,
                                        artUri = thumbUrl
                                    )
                                )
                            } else if (browseId != null && (
                                    browseId.startsWith("VL") ||
                                    browseId.startsWith("PL") ||
                                    browseId.startsWith("RD") ||
                                    browseId.startsWith("MPSP")
                                ) && title.isNotEmpty()
                            ) {
                                instantItems.add(
                                    InstantSearchItem.PlaylistMatch(
                                        id = browseId,
                                        title = title,
                                        subtitle = fullSub.ifBlank { "Playlist" },
                                        thumbnailUrl = thumbUrl
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "YTM search suggestions fallback: ${e.message}")
        }

        // Fallback for suggestions if empty
        if (suggestions.isEmpty()) {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://suggestqueries.google.com/complete/search?client=youtube&ds=yt&q=$encodedQuery&oe=utf-8"
            try {
                val response = sharedHttpClient.newCall(
                    okhttp3.Request.Builder().url(url).build()
                ).execute().body?.string() ?: ""

                val startIndex = response.indexOf("[[")
                val endIndex = response.lastIndexOf("]]")
                if (startIndex != -1 && endIndex != -1) {
                    val suggestionsJson = response.substring(startIndex, endIndex + 2)
                    val jsonArray = org.json.JSONArray(suggestionsJson)
                    for (i in 0 until jsonArray.length()) {
                        val sug = jsonArray.getJSONArray(i).getString(0)
                        if (!suggestions.contains(sug)) {
                            suggestions.add(sug)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get suggestions", e)
            }
        }

        SearchSuggestionsData(
            suggestions = suggestions,
            instantItems = instantItems
        )
    }

    suspend fun getStreamUrl(videoId: String, quality: AudioQuality, videoQuality: VideoQuality = VideoQuality.HIGH, isVideo: Boolean = false): String? {
        // High priority clients for guest extraction
        val clientsToTry = when {
            isVideo -> listOf(
                Client.IOS, // Pristine 1080p HLS manifest with unthrottled adaptive streams
                Client.ANDROID_VR,
                Client.TVHTML5,
                Client.WEB
            )
            quality == AudioQuality.HIGH_AAC -> listOf(
                Client.IOS, // Consistently provides unthrottled M4A/AAC (itag 140/141)
                Client.ANDROID_MUSIC, // Often returns 256kbps AAC
                Client.TVHTML5,
                Client.ANDROID_VR,
                Client.WEB
            )
            else -> listOf(
                Client.TVHTML5, // Often returns direct URLs without signature cipher (fastest & most stable)
                Client.ANDROID_MUSIC, // Often returns 256kbps AAC or high quality Opus
                Client.IOS, // Often returns 256kbps AAC
                Client.ANDROID_VR,
                Client.WEB
            )
        }

        for (client in clientsToTry) {
            val url = fetchStreamUrl(videoId, client, quality, videoQuality, isVideo)
            if (url != null) return url
            Log.d(TAG, "${client.clientName} failed, trying next...")
        }

        return null
    }

    private fun generateCPN(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
        return (1..16).map { chars.random() }.joinToString("")
    }



    private suspend fun fetchStreamUrl(videoId: String, client: Client, quality: AudioQuality, videoQuality: VideoQuality, isVideo: Boolean): String? {
        Log.d(TAG, "Fetching stream for videoId: $videoId using client: ${client.clientName}")
        
        val json = JSONObject().apply {
            put("context", getContext(client))
            put("videoId", videoId)
            put("cpn", generateCPN())
            put("playbackContext", JSONObject().apply {
                put("contentPlaybackContext", JSONObject().apply {
                    put("signatureTimestamp", 20252) // Updated signature timestamp
                    put("visid_sent", 1)
                })
            })
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())


        return try {
            val response = apiService.getPlayer(requestBody, getHeaders(client))
            val formats = response.streamingData?.adaptiveFormats
            val status = response.playabilityStatus?.status
            val reason = response.playabilityStatus?.reason
            
            // Update visitorData from player response too
            response.responseContext?.visitorData?.let {
                cachedVisitorData = it
            }
            
            Log.d(TAG, "[${client.clientName}] Status: $status, Reason: $reason, Formats: ${formats?.size ?: 0}")


            if (formats.isNullOrEmpty() && response.streamingData?.formats.isNullOrEmpty()) return null

            if (isVideo) {
                // Try HLS/DASH manifests first for adaptive streaming support (all qualities)
                val hlsUrl = response.streamingData?.hlsManifestUrl
                if (!hlsUrl.isNullOrEmpty()) {
                    Log.d(TAG, "[${client.clientName}] Selected HLS Manifest for Video")
                    return hlsUrl
                }
                val dashUrl = response.streamingData?.dashManifestUrl
                if (!dashUrl.isNullOrEmpty()) {
                    Log.d(TAG, "[${client.clientName}] Selected DASH Manifest for Video")
                    return dashUrl
                }

                // Try to find a muxed video format (video + audio)
                val muxedFormats = response.streamingData?.formats
                if (!muxedFormats.isNullOrEmpty()) {
                    val videoFormats = muxedFormats.filter { it.mimeType?.contains("video") == true }
                    
                    val videoFormat = when (videoQuality) {
                        VideoQuality.HIGH -> videoFormats.maxByOrNull { it.height ?: 0 }
                        VideoQuality.NORMAL -> videoFormats.minByOrNull { kotlin.math.abs((it.height ?: 0) - 720) } ?: videoFormats.maxByOrNull { it.height ?: 0 }
                        VideoQuality.LOW -> videoFormats.minByOrNull { kotlin.math.abs((it.height ?: 0) - 360) } ?: videoFormats.minByOrNull { it.height ?: 0 }
                    }

                    if (videoFormat != null) {
                        Log.d(TAG, "[${client.clientName}] Selected video format: ${videoFormat.mimeType} ${videoFormat.height}p for $videoQuality")
                        val videoUrl = when {
                            !videoFormat.url.isNullOrEmpty() -> videoFormat.url
                            !videoFormat.signatureCipher.isNullOrEmpty() -> extractUrlFromCipher(videoFormat.signatureCipher)
                            else -> null
                        }
                        if (videoUrl != null) return videoUrl
                    }
                }
                Log.w(TAG, "[${client.clientName}] No muxed video format found, falling back to audio")
            }

            // Fallback to audio formats
            val audioFormats = formats?.filter { it.mimeType?.contains("audio") == true } ?: emptyList()

            val bestFormat = when (quality) {
                AudioQuality.HIGH -> {
                    audioFormats.filter { !isAacFormat(it) }
                        .maxByOrNull { it.averageBitrate ?: it.bitrate ?: 0L }
                        ?: audioFormats.maxByOrNull { it.averageBitrate ?: it.bitrate ?: 0L }
                }
                AudioQuality.HIGH_AAC -> {
                    audioFormats.filter(::isAacFormat)
                        .maxByOrNull { it.averageBitrate ?: it.bitrate ?: 0L }
                        ?: audioFormats.maxByOrNull { it.averageBitrate ?: it.bitrate ?: 0L }
                }
                AudioQuality.NORMAL -> {
                    audioFormats.minByOrNull { kotlin.math.abs((it.averageBitrate ?: it.bitrate ?: 0L) - 128000L) }
                }
                AudioQuality.LOW -> {
                    audioFormats.minByOrNull { kotlin.math.abs((it.averageBitrate ?: it.bitrate ?: 0L) - 48000L) }
                }
            }

            if (bestFormat == null) return null

            val finalUrl = when {
                !bestFormat.url.isNullOrEmpty() -> {
                    Log.d(TAG, "[${client.clientName}] Found direct URL")
                    bestFormat.url
                }
                !bestFormat.signatureCipher.isNullOrEmpty() -> {
                    Log.d(TAG, "[${client.clientName}] Found signatureCipher, parsing...")
                    extractUrlFromCipher(bestFormat.signatureCipher)
                }
                else -> null
            }

            val resolvedBps = com.example.ecodot.ui.components.player.PlayerStyleMath.normalizeBitrateBps(
                ((bestFormat.averageBitrate ?: bestFormat.bitrate) ?: 0L).toInt()
            )
            if (resolvedBps > 0 && finalUrl != null) {
                val codecName = bestFormat.mimeType
                    ?.substringAfter("codecs=\"", "")
                    ?.substringBefore("\"")
                    ?.takeIf { it.isNotEmpty() }
                    ?: bestFormat.mimeType
                com.example.ecodot.data.remote.YouTubeStreamResolver.resolvedAudioTelemetry[videoId] =
                    com.example.ecodot.data.remote.ResolvedAudioTelemetry(
                        bitrateBps = resolvedBps,
                        sampleRateHz = 48_000,
                        codec = codecName,
                        mimeType = bestFormat.mimeType?.substringBefore(";")?.trim(),
                        itag = bestFormat.itag.toString().toIntOrNull()?.takeIf { it > 0 }
                    )
            }
            Log.d(TAG, "[${client.clientName}] Selected bitrate: ${bestFormat.bitrate}")
            finalUrl
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "[${client.clientName}] getPlayer failed with body: $errorBody", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "[${client.clientName}] getPlayer failed", e)
            null
        }
    }



    private fun isAacFormat(format: AdaptiveFormat): Boolean {
        val mime = format.mimeType?.lowercase().orEmpty()
        return mime.contains("mp4a") ||
            mime.contains("audio/mp4") ||
            format.itag in setOf(139, 140, 141)
    }

    private fun extractUrlFromCipher(cipher: String): String? {
        return try {
            val parts = cipher.split("&")
            var url: String? = null
            var s: String? = null
            
            parts.forEach {
                val keyValue = it.split("=")
                if (keyValue.size == 2) {
                    val key = keyValue[0]
                    val value = URLDecoder.decode(keyValue[1], "UTF-8")
                    when (key) {
                        "url" -> url = value
                        "s" -> s = value
                    }
                }
            }
            
            if (url != null && s != null) {
                // We do NOT implement the YouTube signature deciphering algorithm.
                // Returning a URL with just &sig=$s will result in an HTTP 403 Forbidden.
                // We MUST return null here to force the repository to fallback to another client
                // (like TVHTML5) which might provide a direct URL without signature cipher.
                Log.w(TAG, "Signature deciphering required. Returning null to force client fallback.")
                null
            } else if (url != null) {
                url
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing cipher", e)
            null
        }
    }

    override suspend fun getArtistDetails(artistId: String): YouTubeBrowseResponse? {
        val client = Client.WEB_REMIX
        val json = JSONObject().apply {
            put("context", getContext(client))
            put("browseId", artistId)
        }
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        return try {
            apiService.browse(requestBody, getHeaders(client))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get artist details", e)
            null
        }
    }

    /**
     * Two-step album fetch (the approach used by SimpMusic/ytmusicapi):
     * Step 1: Browse `MPREb_*` → get album header info + audioPlaylistId (OLAK5uy_...)
     * Step 2: Browse `VL` + audioPlaylistId → fetch tracks from the playlist container
     * Returns a pair of (headerJson, tracksJson) raw strings for the ViewModel to parse.
     */
    override suspend fun getAlbumJsonPair(albumId: String): Pair<String, String>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val apiKey = YouTubeApiService.API_KEY
        val httpClient = sharedHttpClient
        val clientsToTry = listOf(
            Client.WEB_REMIX,
            Client.IOS,
            Client.ANDROID_MUSIC,
            Client.WEB
        )

        var lastExceptionMessage = "Unknown error"
        val normalizedBrowseId = when {
            albumId.startsWith("PL") || albumId.startsWith("RD") || albumId.startsWith("OLAK5") -> "VL$albumId"
            else -> albumId
        }
        for (client in clientsToTry) {
            try {
                val step1Body = JSONObject().apply {
                    put("context", getContext(client))
                    put("browseId", normalizedBrowseId)
                }.toString().toRequestBody("application/json".toMediaType())

                val step1Raw = httpClient.newCall(
                    okhttp3.Request.Builder()
                        .url("https://music.youtube.com/youtubei/v1/browse?key=$apiKey&prettyPrint=false")
                        .post(step1Body)
                        .apply { getHeaders(client).forEach { (k, v) -> header(k, v) } }
                        .build()
                ).execute().body?.string()

                if (step1Raw == null) continue

                Log.d(TAG, "Album Step1 with ${client.clientName}: ${step1Raw.length} bytes, albumId=$albumId")

                val albumObj = JSONObject(step1Raw)
                if (!albumObj.has("header") && !albumObj.has("contents") && !albumObj.has("microformat")) {
                    Log.w(TAG, "Album Step1 invalid response with ${client.clientName}, keys: ${albumObj.keys().asSequence().toList()}")
                    continue // Try next client
                }
                
                if (client == Client.ANDROID_MUSIC) {
                    Log.d(TAG, "Album Step1 succeeded with ANDROID_MUSIC, returning immediately since it contains tracks.")
                    return@withContext Pair(step1Raw, step1Raw)
                }

                // If step1 succeeds, try to extract the playlist ID
                val audioPlaylistId = extractAudioPlaylistId(albumObj)
                if (audioPlaylistId == null) {
                    // If we can't get playlist ID but have tracks directly, just use step1
                    return@withContext Pair(step1Raw, step1Raw)
                }

                val vlBrowseId = if (audioPlaylistId.startsWith("VL")) audioPlaylistId else "VL$audioPlaylistId"
                val step2Body = JSONObject().apply {
                    put("context", getContext(client))
                    put("browseId", vlBrowseId)
                }.toString().toRequestBody("application/json".toMediaType())

                val step2Raw = httpClient.newCall(
                    okhttp3.Request.Builder()
                        .url("https://music.youtube.com/youtubei/v1/browse?key=$apiKey&prettyPrint=false")
                        .post(step2Body)
                        .apply { getHeaders(client).forEach { (k, v) -> header(k, v) } }
                        .build()
                ).execute().body?.string()

                if (step2Raw == null) continue

                Log.d(TAG, "Album Step2 with ${client.clientName}: ${step2Raw.length} bytes")
                return@withContext Pair(step1Raw, step2Raw)

            } catch (e: Exception) {
                lastExceptionMessage = e.message ?: e.toString()
                Log.e(TAG, "getAlbumJsonPair failed for client ${client.clientName}: $lastExceptionMessage")
            }
        }
        
        Log.e(TAG, "All clients failed to fetch album details for $albumId. Last error: $lastExceptionMessage")
        throw Exception("API Error: $lastExceptionMessage")
    }

    /**
     * Extracts the audioPlaylistId (OLAK5uy_...) from an album browse response.
     * Tries multiple known JSON paths.
     */
    private fun extractAudioPlaylistId(albumJson: JSONObject): String? {
        // Path 1: microformat → microformatDataRenderer → urlCanonical
        val microformatData = albumJson.optJSONObject("microformat")
            ?.optJSONObject("microformatDataRenderer")
        if (microformatData != null) {
            val urlCanonical = microformatData.optString("urlCanonical")
            if (urlCanonical.contains("list=OLAK5") || urlCanonical.contains("list=PL")) {
                val plId = urlCanonical.substringAfter("list=").substringBefore("&")
                if (plId.isNotEmpty()) return plId
            }
        }

        // Path 1.5: microformat → musicMicroformatRenderer → musicVideoId (older APIs)
        val microformat = albumJson.optJSONObject("microformat")
            ?.optJSONObject("musicMicroformatRenderer")
        if (microformat != null) {
            val plId = microformat.optString("musicVideoId").takeIf { it.isNotEmpty() }
                ?: microformat.optJSONObject("urlCanonical")?.optString("url")
            if (plId != null && (plId.startsWith("OLAK5") || plId.startsWith("PL"))) {
                return plId
            }
        }

        // Path 2: header.musicDetailHeaderRenderer — look for menu items that have playlistId
        val header = albumJson.optJSONObject("header")
        val detailHeader = header?.optJSONObject("musicDetailHeaderRenderer")
        if (detailHeader != null) {
            // Try menu → menuRenderer → items → menuNavigationItemRenderer → navigationEndpoint → browseEndpoint → browseId
            val menuItems = detailHeader.optJSONObject("menu")?.optJSONObject("menuRenderer")?.optJSONArray("items")
            if (menuItems != null) {
                for (i in 0 until menuItems.length()) {
                    val item = menuItems.optJSONObject(i)
                    val browseId = item?.optJSONObject("menuNavigationItemRenderer")
                        ?.optJSONObject("navigationEndpoint")
                        ?.optJSONObject("browseEndpoint")
                        ?.optString("browseId")
                    if (browseId?.startsWith("OLAK5") == true) return browseId
                }
            }
        }

        // Path 3: contents → singleColumnBrowseResultsRenderer tabs → tabRenderer → content → sectionListRenderer
        // → musicShelfRenderer → playlistId
        val sections = findSectionContentsFromJson(albumJson)
        for (section in sections) {
            val shelfPl = section.optJSONObject("musicShelfRenderer")?.optString("playlistId")
            if (!shelfPl.isNullOrEmpty() && (shelfPl.startsWith("OLAK5") || shelfPl.startsWith("OLA"))) {
                return shelfPl
            }
            val playlistShelfPl = section.optJSONObject("musicPlaylistShelfRenderer")?.optString("playlistId")
            if (!playlistShelfPl.isNullOrEmpty()) return playlistShelfPl
        }

        // Path 4: Search anywhere in response for OLAK5uy_ pattern
        val raw = albumJson.toString()
        val match = Regex("\"(OLAK5uy_[A-Za-z0-9_-]+)\"").find(raw)
        if (match != null) {
            return match.groupValues[1]
        }

        return null
    }

    private fun findSectionContentsFromJson(json: JSONObject): List<JSONObject> {
        val result = mutableListOf<JSONObject>()
        val contents = json.optJSONObject("contents") ?: return result

        fun addFromArray(arr: org.json.JSONArray?) {
            if (arr == null) return
            for (i in 0 until arr.length()) arr.optJSONObject(i)?.let { result.add(it) }
        }

        contents.optJSONObject("singleColumnBrowseResultsRenderer")
            ?.optJSONArray("tabs")?.optJSONObject(0)
            ?.optJSONObject("tabRenderer")?.optJSONObject("content")
            ?.optJSONObject("sectionListRenderer")?.optJSONArray("contents")
            ?.let { addFromArray(it) }

        if (result.isEmpty()) {
            contents.optJSONObject("twoColumnBrowseResultsRenderer")
                ?.optJSONArray("tabs")?.optJSONObject(0)
                ?.optJSONObject("tabRenderer")?.optJSONObject("content")
                ?.optJSONObject("sectionListRenderer")?.optJSONArray("contents")
                ?.let { addFromArray(it) }
        }

        if (result.isEmpty()) {
            contents.optJSONObject("sectionListRenderer")?.optJSONArray("contents")
                ?.let { addFromArray(it) }
        }
        return result
    }

    // Keep getRawAlbumJson for backward compat (now just wraps step1)
    suspend fun getRawAlbumJson(albumId: String): String? {
        return getAlbumJsonPair(albumId)?.first
    }


    /**
     * Fetches rich video metadata: view count, likes, description, codec info.
     * Uses the player endpoint (already called for stream URL) which returns videoDetails,
     * and parses the best audio format for codec/bitrate/itag.
     */
    suspend fun fetchVideoInfo(
        videoId: String,
        preferredQuality: AudioQuality = AudioQuality.HIGH
    ): VideoInfo? = withContext(Dispatchers.IO) {
        val telemetry = com.example.ecodot.data.remote.YouTubeStreamResolver.resolvedAudioTelemetry[videoId]
        val newPipeMeta = runCatching {
            streamResolver.resolveTrackMetadata(videoId, preferredQuality)
        }.getOrNull()

        val innerTubeMeta = if (newPipeMeta == null || newPipeMeta.viewCountRaw <= 0L) {
            runCatching { fetchVideoInfoInnerTubeFallback(videoId, preferredQuality) }.getOrNull()
        } else {
            null
        }

        val base = newPipeMeta ?: innerTubeMeta
        if (base != null) {
            val activeBitrate = telemetry?.bitrateBps?.takeIf { it > 0 }?.let {
                com.example.ecodot.ui.components.player.PlayerStyleMath.formatBitrateKbpsLabel(it)
            }
            val baseBitrateNormalized = base.bitrate.replace(",", "").substringBefore(" ").toIntOrNull()
                ?.takeIf { it > 0 }
                ?.let { raw ->
                    com.example.ecodot.ui.components.player.PlayerStyleMath.formatBitrateKbpsLabel(
                        com.example.ecodot.ui.components.player.PlayerStyleMath.normalizeBitrateBps(raw)
                    )
                }
                ?: base.bitrate.takeIf { it.isNotBlank() && it != "0" && it != "—" }

            val resolvedSampleRate = telemetry?.sampleRateHz?.takeIf { it > 0 }?.let { hz ->
                val khz = hz / 1000.0
                if (hz % 1000 == 0) "${hz / 1000} kHz" else "%.1f kHz".format(java.util.Locale.US, khz)
            } ?: base.sampleRate.takeIf { it.isNotBlank() } ?: "48 kHz"

            val parsedInnerViews = innerTubeMeta?.viewCount?.filter(Char::isDigit)?.toLongOrNull() ?: -1L
            val parsedBaseViews = base.viewCount.filter(Char::isDigit).toLongOrNull() ?: -1L
            val mergedViewsRaw = maxOf(
                base.viewCountRaw,
                innerTubeMeta?.viewCountRaw ?: -1L,
                if (parsedInnerViews > 0L) parsedInnerViews else -1L,
                if (parsedBaseViews > 0L) parsedBaseViews else -1L
            )
            val mergedViews = if (base.viewCount != "0" && base.viewCount != "—" && base.viewCount.isNotBlank()) {
                base.viewCount
            } else {
                innerTubeMeta?.viewCount?.takeIf { it.isNotBlank() && it != "0" } ?: base.viewCount
            }
            val mergedLikesRaw = maxOf(base.likeCountRaw, innerTubeMeta?.likeCountRaw ?: -1L)
            val mergedLikes = if (base.likeCountRaw > 0L) base.likeCount else (innerTubeMeta?.likeCount ?: base.likeCount)
            val mergedCodec = telemetry?.codec?.takeIf { it.isNotBlank() }
                ?: base.codec.takeIf { it.isNotBlank() && it != "—" }
                ?: innerTubeMeta?.codec
                ?: "—"
            val mergedMime = telemetry?.mimeType?.takeIf { it.isNotBlank() }
                ?: base.mimeType.takeIf { it.isNotBlank() && it != "—" }
                ?: innerTubeMeta?.mimeType
                ?: "—"
            val mergedItag = telemetry?.itag?.takeIf { it > 0 }?.toString()
                ?: base.itag.takeIf { it.isNotBlank() && it != "0" && it != "—" }
                ?: innerTubeMeta?.itag?.takeIf { it.isNotBlank() && it != "0" && it != "—" }
                ?: "—"
            val mergedDesc = base.description.takeIf { it.isNotBlank() }
                ?: innerTubeMeta?.description.orEmpty()
            val mergedChannel = base.channelName.takeIf { it.isNotBlank() }
                ?: innerTubeMeta?.channelName.orEmpty()
            val mergedDuration = base.durationSeconds.takeIf { it.isNotBlank() && it != "—" }
                ?: innerTubeMeta?.durationSeconds
                ?: "—"

            return@withContext base.copy(
                description = mergedDesc,
                viewCount = mergedViews,
                likeCount = mergedLikes,
                codec = mergedCodec,
                mimeType = mergedMime,
                bitrate = activeBitrate ?: baseBitrateNormalized ?: "—",
                itag = mergedItag,
                durationSeconds = mergedDuration,
                sampleRate = resolvedSampleRate,
                channelName = mergedChannel,
                likeCountRaw = mergedLikesRaw,
                viewCountRaw = mergedViewsRaw
            )
        }
        null
    }

    private suspend fun fetchVideoInfoInnerTubeFallback(
        videoId: String,
        preferredQuality: AudioQuality = AudioQuality.HIGH
    ): VideoInfo? {
        return try {
            val client = if (preferredQuality == AudioQuality.HIGH_AAC) Client.ANDROID_MUSIC else Client.WEB_REMIX
            val json = JSONObject().apply {
                put("context", getContext(client))
                put("videoId", videoId)
                put("cpn", generateCPN())
                put("playbackContext", JSONObject().apply {
                    put("contentPlaybackContext", JSONObject().apply {
                        put("signatureTimestamp", 20252)
                    })
                })
            }
            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val response = apiService.getPlayer(requestBody, getHeaders(client))

            val details = response.videoDetails
            val formats = response.streamingData?.adaptiveFormats
            val audioCandidates = formats?.filter { it.mimeType?.contains("audio") == true }.orEmpty()

            // Match audio format to active quality setting (High AAC vs Opus/Normal/Low)
            val bestAudio = when (preferredQuality) {
                AudioQuality.HIGH_AAC -> {
                    audioCandidates.filter(::isAacFormat).maxByOrNull { it.averageBitrate ?: it.bitrate ?: 0L }
                        ?: audioCandidates.maxByOrNull { it.averageBitrate ?: it.bitrate ?: 0L }
                }
                AudioQuality.NORMAL -> {
                    audioCandidates.minByOrNull { kotlin.math.abs((it.averageBitrate ?: it.bitrate ?: 0L) - 128000L) }
                }
                AudioQuality.LOW -> {
                    audioCandidates.minByOrNull { kotlin.math.abs((it.averageBitrate ?: it.bitrate ?: 0L) - 48000L) }
                }
                AudioQuality.HIGH -> {
                    audioCandidates.filter { !isAacFormat(it) }.maxByOrNull { it.averageBitrate ?: it.bitrate ?: 0L }
                        ?: audioCandidates.maxByOrNull { it.averageBitrate ?: it.bitrate ?: 0L }
                }
            }

            val rawMime   = bestAudio?.mimeType ?: "audio/webm"
            val codec     = Regex("codecs=\"([^\"]+)\"").find(rawMime)?.groupValues?.getOrNull(1) ?: "opus"
            val mimeClean = rawMime.substringBefore(";").trim()
            val bitrate   = bestAudio?.averageBitrate ?: bestAudio?.bitrate ?: 0L
            val itag      = bestAudio?.itag?.toString() ?: "—"

            // View count: in videoDetails as "viewCount" string
            // We parse it manually from the raw JSON because Moshi model doesn't include it yet
            val rawJson = JSONObject(json.toString())  // re-use same body for parsing trick
            // Re-fetch raw to get viewCount from response JSON
            val rawResponse = sharedHttpClient.newCall(
                okhttp3.Request.Builder()
                    .url("https://music.youtube.com/youtubei/v1/player?prettyPrint=false")
                    .post(requestBody)
                    .apply { getHeaders(client).forEach { (k, v) -> header(k, v) } }
                    .build()
            ).execute().body?.string() ?: "{}"

            val playerObj   = JSONObject(rawResponse)
            val detailsObj  = playerObj.optJSONObject("videoDetails")
            val viewCount   = detailsObj?.optString("viewCount", "0") ?: "0"
            val rawDesc     = detailsObj?.optString("shortDescription", "") ?: ""
            val authorName  = detailsObj?.optString("author", details?.author ?: "Unknown") ?: "Unknown"
            val title       = detailsObj?.optString("title", details?.title ?: "") ?: ""

            // Format view count with commas
            val formattedViews = try {
                "%,d".format(viewCount.toLong())
            } catch (e: Exception) { viewCount }

            val durationSec = detailsObj?.optString("lengthSeconds", "0")?.toLongOrNull() ?: 0L
            val durFormatted = "%d:%02d".format(durationSec / 60, durationSec % 60)

            VideoInfo(
                videoId       = videoId,
                title         = title.ifEmpty { details?.title ?: "" },
                artist        = authorName,
                album         = "",
                viewCount     = formattedViews,
                likeCount     = "—",            // YouTube hides likes in API responses
                description   = rawDesc,
                mimeType      = mimeClean,
                codec         = codec,
                bitrate       = com.example.ecodot.ui.components.player.PlayerStyleMath.formatBitrateKbpsLabel(bitrate.toInt()) ?: "—",
                itag          = itag,
                durationSeconds = durFormatted
            )
        } catch (e: Exception) {
            Log.e(TAG, "fetchVideoInfo failed for $videoId", e)
            null
        }
    }

    suspend fun getYouTubeLyrics(videoId: String): String? {
        val client = Client.WEB_REMIX
        val json = JSONObject().apply {
            put("context", getContext(client))
            put("videoId", videoId)
        }
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        
        return try {
            val responseString = apiService.getNext(requestBody, getHeaders(client)).string()
            val nextObj = JSONObject(responseString)
            
            // Extract lyricsBrowseId
            var lyricsBrowseId: String? = null
            val tabs = nextObj.optJSONObject("contents")
                ?.optJSONObject("singleColumnMusicWatchNextResultsRenderer")
                ?.optJSONObject("tabbedRenderer")
                ?.optJSONObject("watchNextTabbedResultsRenderer")
                ?.optJSONArray("tabs")
            
            if (tabs != null) {
                for (i in 0 until tabs.length()) {
                    val tabRenderer = tabs.optJSONObject(i)?.optJSONObject("tabRenderer")
                    if (tabRenderer?.optString("title")?.lowercase() == "lyrics") {
                        lyricsBrowseId = tabRenderer.optJSONObject("endpoint")
                            ?.optJSONObject("browseEndpoint")
                            ?.optString("browseId")
                        break
                    }
                }
            }
            
            if (lyricsBrowseId == null) return null
            
            val browseJson = JSONObject().apply {
                put("context", getContext(client))
                put("browseId", lyricsBrowseId)
            }
            val browseBody = browseJson.toString().toRequestBody("application/json".toMediaType())
            val browseResponse = apiService.browse(browseBody, getHeaders(client))
            
            // Extract lyrics text from browse response
            val lyricsText = browseResponse.contents?.sectionListRenderer?.contents?.getOrNull(0)
                ?.musicDescriptionShelfRenderer?.description?.runs?.getOrNull(0)?.text
                
            lyricsText
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get YT lyrics for $videoId", e)
            null
        }
    }
    
    // Add getHomeFeed (backwards-compatible wrapper using rich raw InnerTube parser)
    override suspend fun getHomeFeed(params: String?): List<HomeSection> =
        getHomeFeedPage(params = params).sections

    private fun countryNameToGlCode(country: String): String = when (country.trim()) {
        "Global" -> "ZZ"
        "Canada" -> "CA"
        "India" -> "IN"
        "United States", "US" -> "US"
        "United Kingdom", "UK" -> "GB"
        "Australia" -> "AU"
        "Germany" -> "DE"
        "France" -> "FR"
        "Japan" -> "JP"
        "Brazil" -> "BR"
        "South Korea" -> "KR"
        "Vietnam" -> "VN"
        "Indonesia" -> "ID"
        else -> "US"
    }

    private suspend fun executeBrowseRaw(
        browseId: String? = null,
        params: String? = null,
        continuation: String? = null,
        glOverride: String? = null,
        resetVisitorData: Boolean = false
    ): String? = withContext(Dispatchers.IO) {
        if (resetVisitorData) {
            cachedVisitorData = null
        }
        val client = Client.WEB_REMIX
        val contextObj = getContext(client)
        if (!glOverride.isNullOrBlank()) {
            contextObj.optJSONObject("client")?.put("gl", glOverride)
        }
        val json = JSONObject().apply {
            put("context", contextObj)
            if (!browseId.isNullOrBlank()) put("browseId", browseId)
            if (!params.isNullOrBlank()) put("params", params)
            if (!continuation.isNullOrBlank()) put("continuation", continuation)
        }
        val url = if (!continuation.isNullOrBlank()) {
            "https://music.youtube.com/youtubei/v1/browse?ctoken=$continuation&continuation=$continuation&type=next&key=${YouTubeApiService.API_KEY}&prettyPrint=false"
        } else {
            "https://music.youtube.com/youtubei/v1/browse?key=${YouTubeApiService.API_KEY}&prettyPrint=false"
        }
        try {
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .apply { getHeaders(client).forEach { (k, v) -> header(k, v) } }
                .build()
            sharedHttpClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w("MusicRepository", "Browse HTTP ${resp.code} for $browseId / cont=${continuation != null}")
                    return@withContext null
                }
                resp.body?.string()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "executeBrowseRaw failed for $browseId", e)
            null
        }
    }

    suspend fun getHomeFeedPage(
        params: String? = null,
        continuation: String? = null,
        country: String? = null,
        resetVisitorData: Boolean = false
    ): HomeFeedPage = withContext(Dispatchers.IO) {
        val gl = country?.let(::countryNameToGlCode)?.takeIf { it != "ZZ" }
        val raw = executeBrowseRaw(
            browseId = if (continuation == null) "FEmusic_home" else null,
            params = if (continuation == null) params else null,
            continuation = continuation,
            glOverride = gl,
            resetVisitorData = resetVisitorData
        )
        if (!raw.isNullOrBlank()) {
            val parsed = YouTubeParser.parseHomeFeedRaw(raw)
            parsed.visitorData?.let { cachedVisitorData = it }
            if (parsed.sections.isNotEmpty() || continuation != null) {
                return@withContext parsed
            }
        }
        if (continuation == null) {
            // Fallback to typed Retrofit call if raw call yielded empty
            try {
                val client = Client.WEB_REMIX
                val json = JSONObject().apply {
                    put("context", getContext(client))
                    put("browseId", "FEmusic_home")
                    if (!params.isNullOrBlank()) put("params", params)
                }
                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                val response = apiService.browse(requestBody, getHeaders(client))
                HomeFeedPage(sections = YouTubeParser.parseHomeFeed(response))
            } catch (e: Exception) {
                Log.e("MusicRepository", "Fallback getHomeFeed failed", e)
                HomeFeedPage(emptyList())
            }
        } else {
            HomeFeedPage(emptyList())
        }
    }

    suspend fun getNewReleasesSections(country: String? = null): List<HomeSection> = withContext(Dispatchers.IO) {
        val gl = country?.let(::countryNameToGlCode)?.takeIf { it != "ZZ" }
        val raw = executeBrowseRaw(browseId = "FEmusic_new_releases", glOverride = gl) ?: return@withContext emptyList()
        YouTubeParser.parseHomeFeedRaw(raw).sections
    }

    suspend fun getMoodsAndGenres(): HomeMoodsData = withContext(Dispatchers.IO) {
        val raw = executeBrowseRaw(browseId = "FEmusic_moods_and_genres") ?: return@withContext HomeMoodsData()
        YouTubeParser.parseMoodsAndGenresRaw(raw)
    }

    suspend fun getChartsForCountry(country: String): HomeChartData = withContext(Dispatchers.IO) {
        val glCode = countryNameToGlCode(country)
        val raw = executeBrowseRaw(
            browseId = "FEmusic_charts",
            params = if (glCode == "ZZ") "ggMGCgQIAhAB" else null,
            glOverride = if (glCode == "ZZ") "US" else glCode
        )
        if (!raw.isNullOrBlank()) {
            val parsed = YouTubeParser.parseChartsRaw(raw, country)
            if (parsed.videoCharts.isNotEmpty() || parsed.topArtists.isNotEmpty() || parsed.chartPlaylists.isNotEmpty()) {
                return@withContext parsed
            }
        }
        HomeChartData(countryName = country)
    }

    private val moodArtworkCache = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val moodArtworkSemaphore = Semaphore(2)

    suspend fun browseCategorySections(browseId: String, params: String? = null): List<HomeSection> = withContext(Dispatchers.IO) {
        val raw = executeBrowseRaw(browseId = browseId, params = params) ?: return@withContext emptyList()
        YouTubeParser.parseHomeFeedRaw(raw).sections
    }

    /**
     * Resolves and caches representative cover art for a YouTube Music Mood/Genre category tile.
     * Uses a concurrency limit (2) and direct window scan (no full JSONObject DOM tree) to avoid OOM
     * when dozens of category tiles compose in a grid.
     */
    suspend fun getMoodCategoryArtwork(browseId: String, params: String): String? = withContext(Dispatchers.IO) {
        moodArtworkCache[params]?.let { return@withContext it }
        moodArtworkSemaphore.withPermit {
            moodArtworkCache[params]?.let { return@withPermit it }
            val raw = executeBrowseRaw(browseId = browseId, params = params) ?: return@withPermit null
            val url = extractFirstMoodArtworkUrl(raw)
            if (!url.isNullOrBlank()) {
                moodArtworkCache[params] = url
            }
            url
        }
    }

    private suspend fun searchPlaylistsWithFilter(
        query: String,
        primaryParam: String?,
        fallbackSuffix: String?
    ): List<HomeMediaItem> = withContext(Dispatchers.IO) {
        val client = Client.WEB_REMIX
        val attempts = buildList {
            add(primaryParam to query)
            if (fallbackSuffix != null) add(null to "$query $fallbackSuffix")
        }
        for ((searchParams, q) in attempts) {
            try {
                val json = JSONObject().apply {
                    put("context", getContext(client))
                    put("query", q)
                    if (searchParams != null) put("params", searchParams)
                }
                val request = okhttp3.Request.Builder()
                    .url("https://music.youtube.com/youtubei/v1/search?key=${YouTubeApiService.API_KEY}&prettyPrint=false")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .apply { getHeaders(client).forEach { (k, v) -> header(k, v) } }
                    .build()
                val raw = sharedHttpClient.newCall(request).execute().use { resp ->
                    if (resp.isSuccessful) resp.body?.string() else null
                }
                if (!raw.isNullOrBlank()) {
                    val playlists = YouTubeParser.parsePlaylistsFromSearchJson(raw)
                    if (playlists.isNotEmpty()) return@withContext playlists
                }
            } catch (e: Exception) {
                Log.w("MusicRepository", "searchPlaylistsWithFilter failed for $q", e)
            }
        }
        emptyList()
    }

    suspend fun searchYouTubePlaylists(query: String): List<HomeMediaItem> =
        searchPlaylistsWithFilter(query, "EgWKAQIoAWoKEAMQBBAJEAoQCg==", "playlist")

    suspend fun searchYouTubeFeaturedPlaylists(query: String): List<HomeMediaItem> =
        searchPlaylistsWithFilter(query, "EgeKAQQoADgBahIQBRAOEAkQAxAEEAoQFRAQEBE=", "mix playlist")

    suspend fun searchYouTubePodcasts(query: String): List<HomeMediaItem> =
        searchPlaylistsWithFilter(query, "EgWKAQJQAWoSEAUQDhAJEAMQBBAKEBUQEBAR", "podcast")

    // Wrappers pointing to YouTubeParser
    override suspend fun searchYouTube(query: String): List<Track> = withContext(Dispatchers.IO) {
        val client = Client.WEB_REMIX
        val json = JSONObject().apply {
            put("context", getContext(client))
            put("query", query)
            put("params", "EgWKAQIIAWoKEAMQBBAJEAoQCg==")
        }
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        try {
            val response = apiService.search(requestBody, getHeaders(client))
            YouTubeParser.parseSearchResponse(response)
        } catch (e: Exception) {
            Log.e("MusicRepository", "Search failed", e)
            emptyList()
        }
    }

    suspend fun searchYouTubeInstantMatches(query: String): SearchResults = withContext(Dispatchers.IO) {
        searchYouTubeSingleQuery(query, null, forceDisableParams = true)
    }

    private suspend fun searchYouTubeSingleQuery(query: String, type: String? = null, forceDisableParams: Boolean = false): SearchResults {
        val clientsToTry = listOf(
            Client.WEB_REMIX,
            Client.ANDROID_MUSIC
        )

        val isLikelyLyrics = query.split("\\s+".toRegex()).size >= 3

        for (client in clientsToTry) {
            try {
                val searchParams = if (forceDisableParams || (isLikelyLyrics && type == "Songs")) {
                    null
                } else {
                    when (type) {
                        "Songs"   -> "EgWKAQIIAWoKEAMQBBAJEAoQCg=="
                        "Videos"  -> "EgWKAQIQAWoKEAMQBBAJEAoQCg=="
                        "Albums"  -> "EgWKAQIYAWoKEAMQBBAJEAoQCg=="
                        "Artists" -> "EgWKAQIgAWoKEAMQBBAJEAoQCg=="
                        else      -> null
                    }
                }

                val json = JSONObject().apply {
                    put("context", getContext(client))
                    put("query", query)
                    if (searchParams != null) {
                        put("params", searchParams)
                    }
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                val response = apiService.search(requestBody, getHeaders(client))
                
                response.responseContext?.visitorData?.let {
                    if (it.isNotEmpty()) cachedVisitorData = it
                }
                
                val results = YouTubeParser.parseCategorizedSearchResponse(response)

                if (results.songs.isNotEmpty() || results.videos.isNotEmpty() || results.albums.isNotEmpty() || results.artists.isNotEmpty()) {
                    return results
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "Search failed for client", e)
            }
        }
        
        try {
            val json = JSONObject().apply {
                put("context", getContext(Client.WEB_REMIX))
                put("query", query)
            }
            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val response = apiService.search(requestBody, getHeaders(Client.WEB_REMIX))
            return YouTubeParser.parseCategorizedSearchResponse(response)
        } catch (e: Exception) {
            Log.e("MusicRepository", "Fallback search failed", e)
        }

        return SearchResults()
    }

    override suspend fun searchYouTubeCategorized(query: String, type: String?): SearchResults = withContext(Dispatchers.IO) {
        if (type == null || type == "All") {
            // Vast Multi-Category Parallel Search (matching SimpMusic architecture)
            val topMatchDeferred = async { searchYouTubeSingleQuery(query, null, forceDisableParams = true) }
            val songsDeferred = async { searchYouTubeSingleQuery(query, "Songs") }
            val videosDeferred = async { searchYouTubeSingleQuery(query, "Videos") }
            val albumsDeferred = async { searchYouTubeSingleQuery(query, "Albums") }
            val artistsDeferred = async { searchYouTubeSingleQuery(query, "Artists") }
            val playlistsDeferred = async { searchYouTubePlaylists(query) }
            val featuredPlaylistsDeferred = async { searchYouTubeFeaturedPlaylists(query) }
            val podcastsDeferred = async { searchYouTubePodcasts(query) }

            val topMatch = topMatchDeferred.await()
            val songsRes = songsDeferred.await()
            val videosRes = videosDeferred.await()
            val albumsRes = albumsDeferred.await()
            val artistsRes = artistsDeferred.await()
            val playlistsRes = playlistsDeferred.await()
            val featuredRes = featuredPlaylistsDeferred.await()
            val podcastsRes = podcastsDeferred.await()

            val combinedSongs = (topMatch.songs + songsRes.songs).distinctBy { it.id }
            val normalizedVideosShelf = (videosRes.videos + videosRes.songs).map { t ->
                val cleanAlbum = if (
                    t.album.isBlank() ||
                    t.album.equals("Unknown", true) ||
                    t.album.equals("Video", true) ||
                    t.album.contains("view", true) ||
                    t.album.contains("like", true)
                ) "YouTube Video" else t.album
                t.copy(album = cleanAlbum, path = "ytvideo://${t.id}")
            }
            val combinedVideos = (normalizedVideosShelf + topMatch.videos + topMatch.songs.filter {
                it.path.startsWith("ytvideo://") ||
                it.title.contains("official music video", ignoreCase = true) ||
                it.title.contains("official video", ignoreCase = true) ||
                it.title.contains("(mv)", ignoreCase = true) ||
                it.title.contains("[mv]", ignoreCase = true)
            }.map { it.copy(album = "YouTube Video", path = "ytvideo://${it.id}") }).distinctBy { it.id }
            val combinedAlbums = (topMatch.albums + albumsRes.albums).distinctBy { it.id ?: it.title }
            val combinedArtists = (topMatch.artists + artistsRes.artists).distinctBy { it.id }

            val sortedSongs = combinedSongs.sortedWith(compareByDescending { song ->
                val titleLower = song.title.lowercase()
                val queryLower = query.lowercase()
                when {
                    titleLower == queryLower -> 5
                    titleLower.startsWith(queryLower) -> 4
                    titleLower.contains(queryLower) -> 3
                    song.artist.lowercase().contains(queryLower) -> 2
                    else -> 1
                }
            })

            SearchResults(
                songs = sortedSongs,
                albums = combinedAlbums,
                artists = combinedArtists,
                videos = combinedVideos,
                playlists = playlistsRes,
                featuredPlaylists = featuredRes,
                podcasts = podcastsRes
            )
        } else if (type == "Videos") {
            val res = searchYouTubeSingleQuery(query, "Videos")
            val normalizedVideos = (res.videos + res.songs).distinctBy { it.id }.map { t ->
                t.copy(album = "YouTube Video", path = "ytvideo://${t.id}")
            }
            res.copy(songs = normalizedVideos, videos = normalizedVideos)
        } else if (type == "Playlists") {
            SearchResults(playlists = searchYouTubePlaylists(query))
        } else if (type == "Featured playlists") {
            SearchResults(featuredPlaylists = searchYouTubeFeaturedPlaylists(query))
        } else if (type == "Podcasts") {
            SearchResults(podcasts = searchYouTubePodcasts(query))
        } else {
            searchYouTubeSingleQuery(query, type)
        }
    }

    suspend fun getYouTubeRelatedTracks(videoId: String): List<Track> = withContext(Dispatchers.IO) {
        val client = Client.WEB_REMIX
        val json = JSONObject().apply {
            put("context", getContext(client))
            put("videoId", videoId)
            put("playlistId", "RDAMVM$videoId")
        }
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        try {
            val responseString = apiService.getNext(requestBody, getHeaders(client)).string()
            val results = YouTubeParser.parseNextResponse(responseString)
            results.distinctBy { it.id }.filter { it.id != videoId }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Failed to get related tracks", e)
            throw e
        }
    }

    override suspend fun getYouTubeStreamUrl(videoId: String, quality: AudioQuality, videoQuality: VideoQuality, isVideo: Boolean): String? = withContext(Dispatchers.IO) {
        // 1. Resolve unthrottled, cipher-deciphered stream via NewPipeExtractor (avoids 403 throttling)
        try {
            val unthrottledUrl = if (isVideo) {
                streamResolver.resolveVideoStream(videoId, videoQuality)
            } else {
                streamResolver.resolveAudioStream(videoId, quality)
            }
            if (!unthrottledUrl.isNullOrEmpty()) {
                Log.d(TAG, "Successfully resolved unthrottled stream for $videoId via NewPipeExtractor ($quality)")
                return@withContext unthrottledUrl
            }
        } catch (e: Exception) {
            Log.w(TAG, "NewPipe stream extraction failed for $videoId, trying fallback clients: ${e.message}")
        }

        // 2. Fallback to InnerTube multi-client extraction (prioritizes iOS / Android Music M4A when HIGH_AAC)
        getStreamUrl(videoId, quality, videoQuality, isVideo)
    }

    suspend fun logPlayback(track: Track) = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        trackDao.insertTracks(listOf(track))
        trackDao.incrementPlayCount(track.id, timestamp)
        historyDao.insertHistory(PlaybackHistory(trackId = track.id, timestamp = timestamp))
    }

    suspend fun logPlayback(trackId: String) = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val track = trackDao.getTrackById(trackId)
        if (track != null) {
            trackDao.incrementPlayCount(trackId, timestamp)
            historyDao.insertHistory(PlaybackHistory(trackId = trackId, timestamp = timestamp))
        } else {
            Log.w("MusicRepository", "Skipping history log for unknown track ID: $trackId")
        }
    }

    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        profileDao.updateProfile(profile)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }

    suspend fun downloadTrackToLocal(context: android.content.Context, track: Track, quality: AudioQuality): Track? = withContext(Dispatchers.IO) {
        if (!track.isYouTube) return@withContext track // Already local
        try {
            val url = getYouTubeStreamUrl(track.id, quality) ?: return@withContext null
            val request = okhttp3.Request.Builder().url(url).build()
            val response = sharedHttpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val file = java.io.File(context.filesDir, "ecodot_download_${track.id}.m4a")
                val sink = file.sink().buffer()
                sink.writeAll(response.body!!.source())
                sink.close()
                // Return updated track with local path
                return@withContext track.copy(path = file.absolutePath, isYouTube = false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for ${track.id}", e)
        }
        return@withContext null
    }
}

private val moodThumbRegex = Regex(
    """"(https://(?:lh3\.googleusercontent\.com|i\.ytimg\.com|yt3\.googleusercontent\.com)/[^"\\]+)"""",
    RegexOption.IGNORE_CASE
)

internal fun extractFirstMoodArtworkUrl(raw: String): String? {
    val anchor = raw.indexOf("\"sectionListRenderer\"").let { if (it >= 0) it else 0 }
    val end = (anchor + 16000).coerceAtMost(raw.length)
    val url = moodThumbRegex.find(raw.substring(anchor, end))?.groupValues?.getOrNull(1) ?: return null
    return url.replace(Regex("""=w\d+-h\d+"""), "=w544-h544")
}
