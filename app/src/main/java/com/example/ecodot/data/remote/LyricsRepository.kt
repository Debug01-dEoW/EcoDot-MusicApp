package com.example.ecodot.data.remote

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class LrcLibResponse(
    @Json(name = "id") val id: Long?,
    @Json(name = "trackName") val trackName: String?,
    @Json(name = "artistName") val artistName: String?,
    @Json(name = "albumName") val albumName: String?,
    @Json(name = "duration") val duration: Double?,
    @Json(name = "instrumental") val instrumental: Boolean?,
    @Json(name = "plainLyrics") val plainLyrics: String?,
    @Json(name = "syncedLyrics") val syncedLyrics: String?
)

@JsonClass(generateAdapter = true)
data class SimpMusicLyricsData(
    @Json(name = "lyrics") val lyrics: String?,
    @Json(name = "syncedLyrics") val syncedLyrics: String?,
    @Json(name = "translation") val translation: String?
)

@JsonClass(generateAdapter = true)
data class SimpMusicLyricsResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: SimpMusicLyricsData?
)

@JsonClass(generateAdapter = true)
data class BetterLyricsResponse(
    @Json(name = "ttml") val ttml: String?,
    @Json(name = "score") val score: Int?
)

interface LrcLibService {
    @GET("get")
    suspend fun getLyrics(
        @Query("artist_name") artist: String,
        @Query("track_name") title: String,
        @Query("duration") duration: Int
    ): Response<LrcLibResponse>

    @GET("search")
    suspend fun searchLyrics(
        @Query("track_name") title: String,
        @Query("artist_name") artist: String
    ): Response<List<LrcLibResponse>>

    @GET("search")
    suspend fun searchLyricsGeneric(
        @Query("q") query: String
    ): Response<List<LrcLibResponse>>
}

interface BetterLyricsService {
    @GET("getLyrics")
    suspend fun getLyrics(
        @Query("a") artist: String,
        @Query("s") title: String
    ): Response<BetterLyricsResponse>
}

interface SimpMusicLyricsService {
    @GET("api/v1/lyrics")
    suspend fun getLyrics(
        @Query("videoId") videoId: String
    ): Response<SimpMusicLyricsResponse>
}

class LyricsRepository {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "EcoDot/1.0.0 (https://github.com/example/ecodot; contact@ecodot.com)")
                .build()
            chain.proceed(request)
        }
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val lrcLibRetrofit = Retrofit.Builder()
        .baseUrl("https://lrclib.net/api/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    private val simpMusicRetrofit = Retrofit.Builder()
        .baseUrl("https://lyrics.simpmusic.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    private val betterLyricsRetrofit = Retrofit.Builder()
        .baseUrl("https://api.betterlyrics.cc/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    private val lrcLibService = lrcLibRetrofit.create(LrcLibService::class.java)
    private val simpMusicService = simpMusicRetrofit.create(SimpMusicLyricsService::class.java)
    private val betterLyricsService = betterLyricsRetrofit.create(BetterLyricsService::class.java)

    suspend fun fetchLyrics(videoId: String?, artist: String, title: String, duration: Long): LrcLibResponse? {
        var plainFallback: LrcLibResponse? = null

        // Strategy 1: SimpMusic Lyrics (Best match using videoId)
        if (!videoId.isNullOrEmpty()) {
            try {
                val response = simpMusicService.getLyrics(videoId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        val lyricsResponse = LrcLibResponse(
                            id = null,
                            trackName = title,
                            artistName = artist,
                            albumName = null,
                            duration = (duration / 1000).toDouble(),
                            instrumental = false,
                            plainLyrics = data.lyrics,
                            syncedLyrics = data.syncedLyrics
                        )
                        if (lyricsResponse.syncedLyrics != null) return lyricsResponse
                        if (lyricsResponse.plainLyrics != null) plainFallback = lyricsResponse
                    }
                }
            } catch (e: Exception) {
                Log.w("LyricsRepo", "SimpMusic API failed for $videoId: ${e.message}")
            }
        }

        // Prep cleaning and parsing
        val origArtist = cleanArtist(artist)
        val origTitle = cleanTitle(title)
        val durationSeconds = (duration / 1000).toInt()

        // Detect split title (e.g. "Taylor Swift - Blank Space")
        val dashRegex = Regex("\\s+[-—–]\\s+")
        var splitArtist: String? = null
        var splitTitle: String? = null
        
        if (title.contains(dashRegex)) {
            val parts = title.split(dashRegex)
            if (parts.size >= 2) {
                val left = parts[0].trim()
                val right = parts.subList(1, parts.size).joinToString(" - ").trim()
                
                val cleanedChannel = origArtist.lowercase().replace(" ", "")
                val cleanedLeft = cleanArtist(left).lowercase().replace(" ", "")
                val cleanedRight = cleanArtist(right).lowercase().replace(" ", "")
                
                if (cleanedChannel.contains(cleanedLeft) || cleanedLeft.contains(cleanedChannel)) {
                    splitArtist = left
                    splitTitle = right
                } else if (cleanedChannel.contains(cleanedRight) || cleanedRight.contains(cleanedChannel)) {
                    splitArtist = right
                    splitTitle = left
                } else {
                    splitArtist = left
                    splitTitle = right
                }
            }
        }

        val cleanedSplitArtist = splitArtist?.let { cleanArtist(it) }
        val cleanedSplitTitle = splitTitle?.let { cleanTitle(it) }

        // Helper function to handle a search list
        fun selectBestMatch(results: List<LrcLibResponse>?, durSec: Int): LrcLibResponse? {
            if (results.isNullOrEmpty()) return null
            
            // 1. Synced and duration matches closely (within 15 seconds)
            if (durSec > 0) {
                val match = results.find { it.syncedLyrics != null && Math.abs((it.duration ?: 0.0) - durSec) <= 15 }
                if (match != null) return match
            }
            // 2. Any synced match
            val synced = results.find { it.syncedLyrics != null }
            if (synced != null) return synced
            
            // 3. Plain and duration matches closely
            if (durSec > 0) {
                val match = results.find { it.plainLyrics != null && Math.abs((it.duration ?: 0.0) - durSec) <= 15 }
                if (match != null) return match
            }
            // 4. Any plain match
            val plain = results.find { it.plainLyrics != null }
            if (plain != null) return plain
            
            return results.firstOrNull()
        }

        // Strategy 2: BetterLyrics with original artist/title
        if (origArtist.isNotEmpty() && origTitle.isNotEmpty()) {
            try {
                val betterResponse = betterLyricsService.getLyrics(origArtist, origTitle)
                if (betterResponse.isSuccessful && betterResponse.body() != null) {
                    val body = betterResponse.body()
                    if (body?.ttml != null) {
                        val synced = ttmlToLrc(body.ttml)
                        if (synced.isNotEmpty()) {
                            return LrcLibResponse(
                                id = null,
                                trackName = title,
                                artistName = artist,
                                albumName = null,
                                duration = durationSeconds.toDouble(),
                                instrumental = false,
                                plainLyrics = synced.replace(Regex("\\[.*?\\]"), "").trim(),
                                syncedLyrics = synced
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("LyricsRepo", "BetterLyrics failed for $origArtist - $origTitle: ${e.message}")
            }
        }

        // Strategy 2b: BetterLyrics with split artist/title if available
        if (cleanedSplitArtist != null && cleanedSplitTitle != null && (cleanedSplitArtist != origArtist || cleanedSplitTitle != origTitle)) {
            try {
                val betterResponse = betterLyricsService.getLyrics(cleanedSplitArtist, cleanedSplitTitle)
                if (betterResponse.isSuccessful && betterResponse.body() != null) {
                    val body = betterResponse.body()
                    if (body?.ttml != null) {
                        val synced = ttmlToLrc(body.ttml)
                        if (synced.isNotEmpty()) {
                            return LrcLibResponse(
                                id = null,
                                trackName = title,
                                artistName = artist,
                                albumName = null,
                                duration = durationSeconds.toDouble(),
                                instrumental = false,
                                plainLyrics = synced.replace(Regex("\\[.*?\\]"), "").trim(),
                                syncedLyrics = synced
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("LyricsRepo", "BetterLyrics failed for split $cleanedSplitArtist - $cleanedSplitTitle: ${e.message}")
            }
        }

        // List of query strategies to run on LRCLIB
        data class SearchQuery(val queryArtist: String, val queryTitle: String, val useDuration: Boolean)

        val queries = mutableListOf<SearchQuery>()
        // 1. Original artist & title
        if (origArtist.isNotEmpty() && origTitle.isNotEmpty()) {
            queries.add(SearchQuery(origArtist, origTitle, true))
            queries.add(SearchQuery(origArtist, origTitle, false))
        }
        // 2. Split artist & title
        if (cleanedSplitArtist != null && cleanedSplitTitle != null && (cleanedSplitArtist != origArtist || cleanedSplitTitle != origTitle)) {
            queries.add(SearchQuery(cleanedSplitArtist, cleanedSplitTitle, true))
            queries.add(SearchQuery(cleanedSplitArtist, cleanedSplitTitle, false))
        }

        // Try LRCLIB queries
        for (q in queries) {
            // A. Try Direct Get first if useDuration is true
            if (q.useDuration && durationSeconds > 0) {
                try {
                    val response = lrcLibService.getLyrics(q.queryArtist, q.queryTitle, durationSeconds)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()
                        if (body?.syncedLyrics != null) return body
                        if (body?.plainLyrics != null && plainFallback == null) plainFallback = body
                    }
                } catch (e: Exception) {
                    Log.w("LyricsRepo", "LRCLIB get failed for ${q.queryArtist} - ${q.queryTitle}: ${e.message}")
                }
            }

            // B. Try Fielded Search
            try {
                val response = lrcLibService.searchLyrics(q.queryTitle, q.queryArtist)
                if (response.isSuccessful) {
                    val best = selectBestMatch(response.body(), durationSeconds)
                    if (best?.syncedLyrics != null) return best
                    if (best?.plainLyrics != null && plainFallback == null) plainFallback = best
                }
            } catch (e: Exception) {
                Log.w("LyricsRepo", "LRCLIB search failed for ${q.queryArtist} - ${q.queryTitle}: ${e.message}")
            }

            // C. Try Generic Text Search
            try {
                val response = lrcLibService.searchLyricsGeneric("${q.queryArtist} ${q.queryTitle}")
                if (response.isSuccessful) {
                    val best = selectBestMatch(response.body(), durationSeconds)
                    if (best?.syncedLyrics != null) return best
                    if (best?.plainLyrics != null && plainFallback == null) plainFallback = best
                }
            } catch (e: Exception) {
                Log.w("LyricsRepo", "LRCLIB generic search failed for ${q.queryArtist} ${q.queryTitle}: ${e.message}")
            }
        }

        // Last-resort fallback: search just by title (cleaned original or cleaned split)
        val titleFallbacks = listOfNotNull(origTitle, cleanedSplitTitle).distinct()
        for (t in titleFallbacks) {
            if (t.isNotEmpty()) {
                try {
                    val response = lrcLibService.searchLyricsGeneric(t)
                    if (response.isSuccessful) {
                        val best = selectBestMatch(response.body(), durationSeconds)
                        if (best?.syncedLyrics != null) return best
                        if (best?.plainLyrics != null && plainFallback == null) plainFallback = best
                    }
                } catch (e: Exception) {
                    Log.w("LyricsRepo", "LRCLIB fallback title-only search failed for $t: ${e.message}")
                }
            }
        }

        return plainFallback
    }

    private fun ttmlToLrc(ttml: String): String {
        val lines = mutableListOf<String>()
        // Capture <p begin="..."> content </p> robustly
        val pRegex = Regex("<p [^>]*begin=\"([^\"]+)\"[^>]*>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)

        pRegex.findAll(ttml).forEach { match ->
            val timeStr = match.groupValues[1]
            val content = match.groupValues[2]

            val timestamp = parseTtmlTime(timeStr) ?: return@forEach
            val text = content
                .replace(Regex("<[^>]*>"), "") // Remove nested tags
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim()

            if (text.isNotEmpty()) {
                lines.add("$timestamp $text")
            }
        }
        return lines.joinToString("\n")
    }

    private fun parseTtmlTime(timeStr: String): String? {
        // Formats: "0:00:05.10", "00:05.10", "05.10", "5.1"
        return try {
            val parts = timeStr.split(":", ".")
            when (parts.size) {
                4 -> { // h:mm:ss.ms
                    val h = parts[0].toIntOrNull() ?: 0
                    val m = parts[1].toIntOrNull() ?: 0
                    val s = parts[2].toIntOrNull() ?: 0
                    val ms = parts[3].take(2).padEnd(2, '0')
                    String.format("[%02d:%02d.%s]", h * 60 + m, s, ms)
                }
                3 -> { // mm:ss.ms
                    val m = parts[0].toIntOrNull() ?: 0
                    val s = parts[1].toIntOrNull() ?: 0
                    val ms = parts[2].take(2).padEnd(2, '0')
                    String.format("[%02d:%02d.%s]", m, s, ms)
                }
                2 -> { // ss.ms
                    val s = parts[0].toIntOrNull() ?: 0
                    val ms = parts[1].take(2).padEnd(2, '0')
                    String.format("[%00:02d.%s]", s, ms)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun cleanTitle(title: String): String {
        var s = title
        // Remove feat/ft details
        s = s.replace(Regex("\\s*[\\(\\[](feat\\.|ft\\.|cùng với|con|mukana|com|avec|合作音乐人:?|with)\\s+[^\\)\\]]+[\\)\\]]", RegexOption.IGNORE_CASE), "")
        
        // Remove common parentheticals like (Official Video), [Music Video], etc.
        val noiseWords = listOf("official", "video", "audio", "lyric", "lyrics", "hd", "4k", "remaster", "explicit", "high quality", "hq", "sped up", "slowed", "reverb", "tiktok", "remix", "cover", "visualizer", "live", "clip", "prod", "mono", "stereo", "version")
        val parentheticalRegex = Regex("[\\(\\[]([^\\)\\]]+)[\\)\\]]")
        s = parentheticalRegex.replace(s) { matchResult ->
            val innerText = matchResult.groupValues[1].lowercase()
            if (noiseWords.any { innerText.contains(it) }) "" else matchResult.value
        }
        
        // Remove trailing " - Official Video", etc.
        s = s.replace(Regex("\\s*-\\s*(official|video|audio|lyric|lyrics|hd|4k|music video|official video|remastered|remaster|explicit|high quality|clip|version)\\s*$", RegexOption.IGNORE_CASE), "")
        
        // Replace punctuation
        s = s.replace("\"", "").replace("'", "’")
        s = s.replace(Regex("\\s+"), " ").trim()
        return s
    }

    private fun cleanArtist(artist: String): String {
        var s = artist
        s = s.replace(Regex("VEVO$", RegexOption.IGNORE_CASE), "")
        s = s.replace(Regex("\\s*-\\s*Topic$", RegexOption.IGNORE_CASE), "")
        s = s.replace(Regex("Topic$", RegexOption.IGNORE_CASE), "")
        s = s.replace(Regex("Official$", RegexOption.IGNORE_CASE), "")
        s = s.replace(Regex("\\s*[\\(\\[](feat\\.|ft\\.|with)[\\)\\]]", RegexOption.IGNORE_CASE), "")
        s = s.replace(Regex("\\s+"), " ").trim()
        return s
    }
}

