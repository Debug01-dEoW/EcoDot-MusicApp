package com.example.ecodot.data.remote

import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.ui.viewmodel.Album
import com.example.ecodot.ui.viewmodel.Artist
import android.util.Log
import org.json.JSONObject
import org.json.JSONArray

data class SearchResults(
    val songs: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val videos: List<Track> = emptyList(),
    val playlists: List<com.example.ecodot.data.remote.HomeMediaItem> = emptyList(),
    val featuredPlaylists: List<com.example.ecodot.data.remote.HomeMediaItem> = emptyList(),
    val podcasts: List<com.example.ecodot.data.remote.HomeMediaItem> = emptyList()
)

object YouTubeParser {
    private const val TAG = "YouTubeParser"

    private val thumbSizeRegex = Regex("""([=-])w(\d+)-h(\d+)""")
    private val thumbSquareRegex = Regex("""([=-])s(\d+)""")
    private val ytImgLowResRegex = Regex("""/(default|mqdefault|sddefault|hqdefault)\.jpg(\?.*)?$""", RegexOption.IGNORE_CASE)

    /**
     * Upgrades any YouTube Music / Googleusercontent / ytimg thumbnail URL to high-resolution
     * while preserving aspect ratio (16:9 widescreen vs 1:1 square artwork).
     */
    fun getHighResUrl(url: String?, isWide: Boolean = false): String? {
        if (url.isNullOrBlank()) return url
        val normalized = if (url.startsWith("//")) "https:$url" else url

        thumbSizeRegex.find(normalized)?.let { match ->
            val w = match.groupValues[2].toIntOrNull() ?: 0
            val h = match.groupValues[3].toIntOrNull() ?: 0
            val treatWide = isWide || (w > h && h > 0)
            val target = if (treatWide) "${match.groupValues[1]}w1280-h720" else "${match.groupValues[1]}w1080-h1080"
            return normalized.replaceRange(match.range, target)
        }

        if (normalized.contains("googleusercontent.com") || normalized.contains("ggpht.com")) {
            thumbSquareRegex.find(normalized)?.let { match ->
                return normalized.replaceRange(match.range, "${match.groupValues[1]}s1080")
            }
        }

        if (normalized.contains("i.ytimg.com", ignoreCase = true) || normalized.contains("img.youtube.com", ignoreCase = true)) {
            return ytImgLowResRegex.replace(normalized) { match ->
                val query = match.groupValues[2]
                "/maxresdefault.jpg$query"
            }
        }

        return normalized
    }

    /**
     * Progressive downgrade fallback when an upgraded ytimg URL (e.g. `maxresdefault.jpg`) 404s.
     */
    fun getFallbackThumbUrl(failedUrl: String?): String? {
        if (failedUrl.isNullOrBlank()) return null
        return when {
            failedUrl.contains("maxresdefault.jpg", ignoreCase = true) ->
                failedUrl.replace("maxresdefault.jpg", "sddefault.jpg", ignoreCase = true)
            failedUrl.contains("sddefault.jpg", ignoreCase = true) ->
                failedUrl.replace("sddefault.jpg", "hqdefault.jpg", ignoreCase = true)
            failedUrl.contains("hq720.jpg", ignoreCase = true) ->
                failedUrl.replace("hq720.jpg", "hqdefault.jpg", ignoreCase = true)
            else -> null
        }
    }

    fun parseSearchResponse(response: YouTubeSearchResponse): List<Track> {
        val tracks = mutableListOf<Track>()
        val shelf = response.contents?.tabbedSearchResultsRenderer?.tabs?.getOrNull(0)
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.find { it.musicShelfRenderer != null }
            ?.musicShelfRenderer

        shelf?.contents?.forEach { item ->
            val renderer = item.musicResponsiveListItemRenderer ?: return@forEach
            tracks.add(parseTrackRenderer(renderer))
        }
        return tracks
    }

    fun parseCategorizedSearchResponse(response: YouTubeSearchResponse): SearchResults {
        val songs = mutableListOf<Track>()
        val albums = mutableListOf<Album>()
        val artists = mutableListOf<Artist>()
        val videos = mutableListOf<Track>()
        var topMatchArtist: Artist? = null
        
        val sections = response.contents?.tabbedSearchResultsRenderer?.tabs?.getOrNull(0)
            ?.tabRenderer?.content?.sectionListRenderer?.contents ?: emptyList()

        val processResponsiveListItem = { renderer: MusicResponsiveListItemRenderer, shelfTitle: String ->
            val track = parseTrackRenderer(renderer)
            
            val subtitleRuns = renderer.flexColumns?.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
            val subtitleText = subtitleRuns?.joinToString("") { it.text }?.lowercase() ?: ""
            
            var albumId = renderer.navigationEndpoint?.browseEndpoint?.browseId
            if (albumId == null) {
                 albumId = subtitleRuns?.find { it.navigationEndpoint?.browseEndpoint?.browseId?.startsWith("MPREb") == true }
                    ?.navigationEndpoint?.browseEndpoint?.browseId
            }

            val browseId = renderer.navigationEndpoint?.browseEndpoint?.browseId
            val isArtist = browseId?.startsWith("UC") == true
            val isAlbum = albumId?.startsWith("MPREb") == true || 
                (!isArtist && (subtitleText.contains("album") || shelfTitle.contains("album")))
            val isVideoShelf = shelfTitle.contains("video")
            val isVideo = isVideoShelf ||
                subtitleText.contains("video") ||
                track.path.startsWith("ytvideo://") ||
                (!subtitleText.contains("song") && (subtitleText.contains("view") || subtitleText.contains("like")) && albumId == null)

            if (isArtist) {
                val id = browseId ?: ""
                val name = renderer.flexColumns?.getOrNull(0)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.getOrNull(0)?.text ?: "Unknown"
                val imgUrl = getHighResUrl(renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url)
                if (id.isNotEmpty()) {
                    artists.add(Artist(id, name, imgUrl))
                }
            } else if (isAlbum) {
                parseAlbumFromRenderer(renderer)?.let { albums.add(it) }
            } else if (track.id.isNotEmpty()) {
                if (isVideo) {
                    videos.add(track.copy(album = "YouTube Video", path = "ytvideo://${track.id}"))
                } else {
                    songs.add(track)
                }
            }
        }

        sections.forEach { section ->
            section.musicShelfRenderer?.let { shelf ->
                val shelfTitle = shelf.title?.runs?.getOrNull(0)?.text?.lowercase() ?: ""
                shelf.contents?.forEach { item ->
                    item.musicResponsiveListItemRenderer?.let { renderer ->
                        processResponsiveListItem(renderer, shelfTitle)
                    }
                }
            }
            
            section.itemSectionRenderer?.let { itemSection ->
                itemSection.contents?.forEach { item ->
                    item.musicResponsiveListItemRenderer?.let { renderer ->
                        processResponsiveListItem(renderer, "")
                    }
                }
            }
            
            section.musicCarouselShelfRenderer?.let { carousel ->
                val shelfTitle = carousel.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.getOrNull(0)?.text?.lowercase() ?: ""
                carousel.contents?.forEach { item ->
                    item.musicTwoRowItemRenderer?.let { renderer ->
                        val browseId = renderer.navigationEndpoint?.browseEndpoint?.browseId
                        if (browseId?.startsWith("MPREb") == true || shelfTitle.contains("album")) {
                            albums.add(Album(
                                id = browseId,
                                title = renderer.title?.runs?.getOrNull(0)?.text ?: "Unknown Album",
                                artist = renderer.subtitle?.runs?.getOrNull(0)?.text ?: "Unknown Artist",
                                artUri = getHighResUrl(renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url),
                                tracks = emptyList()
                            ))
                        }
                    }
                }
            }

            section.musicCardShelfRenderer?.let { card ->
                val title = card.title?.runs?.getOrNull(0)?.text ?: ""
                val subtitle = card.subtitle?.runs?.getOrNull(0)?.text?.lowercase() ?: ""
                val artUrl = getHighResUrl(card.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url)
                
                when {
                    subtitle.contains("artist") -> {
                        val artistBrowseId = card.title?.runs?.getOrNull(0)?.navigationEndpoint?.browseEndpoint?.browseId
                            ?: card.navigationEndpoint?.browseEndpoint?.browseId
                            ?: card.buttons?.firstNotNullOfOrNull { btn ->
                                btn.buttonRenderer?.navigationEndpoint?.browseEndpoint?.browseId
                            }
                        if (artistBrowseId != null && artistBrowseId.startsWith("UC")) {
                            topMatchArtist = Artist(artistBrowseId, title, artUrl)
                        } else if (!title.isEmpty()) {
                            topMatchArtist = Artist(artistBrowseId ?: "", title, artUrl)
                        }
                    }
                    subtitle.contains("album") -> {
                        val browseId = card.buttons?.getOrNull(0)?.musicPlayButtonRenderer?.playNavigationEndpoint?.browseEndpoint?.browseId
                            ?: card.title?.runs?.getOrNull(0)?.navigationEndpoint?.browseEndpoint?.browseId
                            ?: card.navigationEndpoint?.browseEndpoint?.browseId
                        if (browseId != null) {
                            albums.add(0, Album(
                                id = browseId,
                                title = title,
                                artist = card.subtitle?.runs?.getOrNull(2)?.text ?: "Unknown",
                                artUri = artUrl,
                                tracks = emptyList()
                            ))
                        }
                    }
                    subtitle.contains("song") || subtitle.contains("video") -> {
                        val videoId = card.buttons?.getOrNull(0)?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId
                        if (videoId != null) {
                            val cardRuns = card.subtitle?.runs
                            val (artist, parsedAlbum) = parseMetadataFromRuns(cardRuns)
                            val isVid = subtitle.contains("video")
                            val album = when {
                                isVid -> "YouTube Video"
                                parsedAlbum != "Unknown" -> parsedAlbum
                                else -> "Top Result"
                            }
                            val isExplicit = title.contains("explicit", true) || subtitle.contains("explicit", true)
                            val topTrack = Track(
                                id = videoId,
                                title = title,
                                artist = artist,
                                album = album,
                                duration = 0,
                                path = if (isVid) "ytvideo://$videoId" else "https://www.youtube.com/watch?v=$videoId",
                                albumArtUri = artUrl,
                                isYouTube = true,
                                isExplicit = isExplicit
                            )
                            if (isVid) {
                                videos.add(0, topTrack)
                            } else {
                                songs.add(0, topTrack)
                            }
                        }
                    }
                }
            }
        }
        
        val finalArtists = mutableListOf<Artist>()
        topMatchArtist?.let { finalArtists.add(it) }
        artists.forEach { a ->
            if (topMatchArtist == null || a.id != topMatchArtist!!.id) {
                finalArtists.add(a)
            }
        }
        
        return SearchResults(
            songs = songs.distinctBy { it.id }, 
            albums = albums.distinctBy { it.id ?: it.title },
            artists = finalArtists.distinctBy { it.id },
            videos = videos.distinctBy { it.id }
        )
    }

    private fun parseAlbumFromRenderer(renderer: MusicResponsiveListItemRenderer): Album? {
        val titleFlex = renderer.flexColumns?.getOrNull(0)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
        val subtitleFlex = renderer.flexColumns?.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
        
        val title = titleFlex?.getOrNull(0)?.text ?: return null
        val artist = subtitleFlex?.getOrNull(2)?.text ?: "Various Artists"
        var albumId = titleFlex?.getOrNull(0)?.navigationEndpoint?.browseEndpoint?.browseId
        if (albumId == null) {
            albumId = renderer.navigationEndpoint?.browseEndpoint?.browseId
        }
        if (albumId == null) {
            albumId = subtitleFlex?.find { it.navigationEndpoint?.browseEndpoint?.browseId?.startsWith("MPREb") == true }
                ?.navigationEndpoint?.browseEndpoint?.browseId
        }
        
        val artUrl = getHighResUrl(renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url)

        return Album(
            id = albumId,
            title = title,
            artist = artist,
            artUri = artUrl,
            tracks = emptyList()
        )
    }

    fun parseNextResponse(jsonString: String): List<Track> {
        val tracks = mutableListOf<Track>()
        try {
            val json = JSONObject(jsonString)
            val results = json.optJSONObject("contents")
                ?.optJSONObject("singleColumnMusicWatchNextResultsRenderer")
                ?.optJSONObject("tabbedRenderer")
                ?.optJSONObject("watchNextTabbedResultsRenderer")
                ?.optJSONArray("tabs")
                ?.optJSONObject(0)
                ?.optJSONObject("tabRenderer")
                ?.optJSONObject("content")
                ?.optJSONObject("musicQueueRenderer")
                ?.optJSONObject("content")
                ?.optJSONObject("playlistPanelRenderer")
                ?.optJSONArray("contents") ?: return emptyList()

            for (i in 0 until results.length()) {
                val item = results.optJSONObject(i)?.optJSONObject("playlistPanelVideoRenderer") ?: continue
                val videoId = item.optString("videoId")
                val title = item.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: "Unknown"
                
                val thumbnailsArr = item.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                val lastThumbObj = thumbnailsArr?.optJSONObject(thumbnailsArr.length() - 1)
                val originalThumb = lastThumbObj?.optString("url")
                val thumbW = lastThumbObj?.optInt("width", 0) ?: 0
                val thumbH = lastThumbObj?.optInt("height", 0) ?: 0

                val artistRuns = item.optJSONObject("longBylineText")?.optJSONArray("runs")
                val titleLower = title.lowercase()
                val musicVideoType = item.optJSONObject("navigationEndpoint")
                    ?.optJSONObject("watchEndpoint")
                    ?.optJSONObject("watchEndpointMusicSupportedConfigs")
                    ?.optJSONObject("watchEndpointMusicConfig")
                    ?.optString("musicVideoType")
                val hasAlbumBrowseId = hasAlbumBrowseIdInJsonRuns(artistRuns)
                val isVid = isVideoTrackSignal(
                    musicVideoType = musicVideoType,
                    rawThumbUrl = originalThumb,
                    thumbWidth = thumbW,
                    thumbHeight = thumbH,
                    hasExplicitVideoRun = isVideoFromJsonRuns(artistRuns),
                    hasViewsOrLikesRun = hasViewsOrLikesFromJsonRuns(artistRuns),
                    hasAlbumBrowseId = hasAlbumBrowseId,
                    titleLower = titleLower
                )
                val (artist, parsedAlbum) = parseMetadataFromJsonRuns(artistRuns)
                val album = when {
                    isVid -> "YouTube Video"
                    parsedAlbum != "Unknown" && !parsedAlbum.contains("view", true) && !parsedAlbum.contains("like", true) -> parsedAlbum
                    else -> "Unknown"
                }

                var isExplicit = false
                val badgesArr = item.optJSONArray("badges")
                if (badgesArr != null) {
                    for (j in 0 until badgesArr.length()) {
                        val badgeObj = badgesArr.optJSONObject(j)
                        if (badgeObj != null) {
                            if (badgeObj.has("musicInlineExplicitBadgeRenderer")) {
                                isExplicit = true
                                break
                            }
                            val inlineBadge = badgeObj.optJSONObject("musicInlineBadgeRenderer")
                            if (inlineBadge != null) {
                                val iconType = inlineBadge.optJSONObject("icon")?.optString("iconType")
                                if (iconType == "MUSIC_EXPLICIT_BADGE") {
                                    isExplicit = true
                                    break
                                }
                            }
                        }
                    }
                }
                if (!isExplicit) {
                    isExplicit = title.contains("explicit", true)
                }

                val highResThumbnail = getHighResUrl(originalThumb, isWide = isVid)

                tracks.add(Track(
                    id = videoId,
                    title = title,
                    artist = artist,
                    album = album,
                    duration = 0,
                    path = if (isVid) "ytvideo://$videoId" else "https://www.youtube.com/watch?v=$videoId",
                    albumArtUri = highResThumbnail,
                    isYouTube = true,
                    isExplicit = isExplicit
                ))
            }
            
            return tracks
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing next response", e)
        }
        return tracks
    }

    private fun parseTrackRenderer(renderer: MusicResponsiveListItemRenderer): Track {
        val titleFlex = renderer.flexColumns?.getOrNull(0)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
        val subtitleFlex = renderer.flexColumns?.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs

        val watchEndpoint = renderer.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint
            ?: renderer.navigationEndpoint?.watchEndpoint
            ?: titleFlex?.getOrNull(0)?.navigationEndpoint?.watchEndpoint

        val videoId = renderer.playlistItemData?.videoId
            ?: watchEndpoint?.videoId
            ?: ""

        val title = titleFlex?.getOrNull(0)?.text ?: "Unknown"
        val titleLower = title.lowercase()
        val albumId = subtitleFlex?.find { it.navigationEndpoint?.browseEndpoint?.browseId?.startsWith("MPREb") == true }
            ?.navigationEndpoint?.browseEndpoint?.browseId
        val lastThumb = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()
        val musicVideoType = watchEndpoint?.watchEndpointMusicSupportedConfigs?.watchEndpointMusicConfig?.musicVideoType

        val isVid = isVideoTrackSignal(
            musicVideoType = musicVideoType,
            rawThumbUrl = lastThumb?.url,
            thumbWidth = lastThumb?.width ?: 0,
            thumbHeight = lastThumb?.height ?: 0,
            hasExplicitVideoRun = isVideoFromRuns(subtitleFlex),
            hasViewsOrLikesRun = hasViewsOrLikesFromRuns(subtitleFlex),
            hasAlbumBrowseId = albumId != null,
            titleLower = titleLower
        )

        val (artist, parsedAlbum) = parseMetadataFromRuns(subtitleFlex)
        val artistId = subtitleFlex?.find { it.navigationEndpoint?.browseEndpoint?.browseId?.startsWith("UC") == true }
            ?.navigationEndpoint?.browseEndpoint?.browseId
        val album = when {
            isVid -> "YouTube Video"
            parsedAlbum != "Unknown" && !parsedAlbum.contains("view", true) && !parsedAlbum.contains("like", true) -> parsedAlbum
            else -> "Unknown"
        }

        val highResThumbnail = getHighResUrl(lastThumb?.url)

        val isExplicit = renderer.badges?.any { 
            it.musicInlineExplicitBadgeRenderer != null || 
            (it.musicInlineBadgeRenderer != null && it.musicInlineBadgeRenderer.icon?.iconType == "MUSIC_EXPLICIT_BADGE")
        } == true || title.contains("explicit", true) || album.contains("explicit", true)

        return Track(
            id = videoId,
            title = title,
            artist = artist,
            album = album,
            duration = 0,
            path = if (isVid) "ytvideo://$videoId" else "https://www.youtube.com/watch?v=$videoId",
            albumArtUri = highResThumbnail,
            isYouTube = true,
            artistId = artistId,
            albumId = albumId,
            isExplicit = isExplicit
        )
    }

    fun parseHomeFeed(response: YouTubeBrowseResponse): List<HomeSection> {
        val sections = mutableListOf<HomeSection>()
        val tabRenderer = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer
            ?: response.contents?.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer

        val sectionListContents = tabRenderer?.content?.sectionListRenderer?.contents ?: return emptyList()

        for (content in sectionListContents) {
            content.musicCarouselShelfRenderer?.let { carousel ->
                val title = carousel.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.joinToString("") { it.text } ?: "Unknown Section"
                val strapline = carousel.header?.musicCarouselShelfBasicHeaderRenderer?.strapline?.runs?.joinToString("") { it.text }
                val items = mutableListOf<HomeMediaItem>()
                
                carousel.contents?.forEach { item ->
                    item.musicTwoRowItemRenderer?.let { renderer ->
                        val itemTitle = renderer.title?.runs?.joinToString("") { it.text } ?: ""
                        val itemSubtitle = renderer.subtitle?.runs?.joinToString("") { it.text } ?: ""
                        val thumbUrl = getHighResUrl((renderer.thumbnailRenderer ?: renderer.thumbnail)?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url) ?: ""
                        val endpoint = renderer.navigationEndpoint
                        val watchVideoId = endpoint?.watchEndpoint?.videoId
                        val browseId = endpoint?.browseEndpoint?.browseId
                        
                        var type = MediaType.UNKNOWN
                        var id = ""
                        
                        if (watchVideoId != null) {
                            type = MediaType.TRACK
                            id = watchVideoId
                        } else if (browseId != null) {
                            if (browseId.startsWith("VL") || browseId.startsWith("PL") || browseId.startsWith("RD")) {
                                type = MediaType.PLAYLIST
                                id = browseId
                            } else if (browseId.startsWith("MPREb") || itemSubtitle.lowercase().contains("album")) {
                                type = MediaType.ALBUM
                                id = browseId
                            } else if (browseId.startsWith("UC")) {
                                type = MediaType.ARTIST
                                id = browseId
                            } else {
                                type = MediaType.PLAYLIST
                                id = browseId
                            }
                        }
                        
                        if (id.isNotEmpty()) {
                            items.add(HomeMediaItem(id, itemTitle, itemSubtitle, thumbUrl, type))
                        }
                    }
                }
                if (items.isNotEmpty()) {
                    sections.add(HomeSection(title, items, strapline))
                }
            }

            content.musicShelfRenderer?.let { listShelf ->
                val title = listShelf.title?.runs?.joinToString("") { it.text } ?: "Quick Picks"
                val strapline = listShelf.strapline?.runs?.joinToString("") { it.text } ?: "LET'S START WITH A RADIO"
                val items = mutableListOf<HomeMediaItem>()
                
                listShelf.contents?.forEach { item ->
                    item.musicResponsiveListItemRenderer?.let { renderer ->
                        val track = parseTrackRenderer(renderer)
                        if (track.id.isNotEmpty()) {
                            items.add(
                                HomeMediaItem(
                                    id = track.id,
                                    title = track.title,
                                    subtitle = track.artist,
                                    thumbnailUrl = track.albumArtUri ?: "",
                                    type = MediaType.TRACK,
                                    isExplicit = track.isExplicit
                                )
                            )
                        }
                    }
                }
                
                if (items.isNotEmpty()) {
                    sections.add(HomeSection(title, items, strapline))
                }
            }
        }
        
        return sections
    }

    // ── Rich InnerTube Raw JSON Parsers (Home, Continuation, Moods, Charts, Browse) ──

    private fun JSONObject?.optRunsText(): String {
        val runs = this?.optJSONArray("runs") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until runs.length()) {
            sb.append(runs.optJSONObject(i)?.optString("text") ?: "")
        }
        return sb.toString().trim()
    }

    private fun upgradeHomeThumbUrl(url: String?, isWide: Boolean = false): String {
        return getHighResUrl(url, isWide = isWide).orEmpty()
    }

    private fun hasExplicitBadgeInArray(badges: JSONArray?): Boolean {
        if (badges == null) return false
        for (i in 0 until badges.length()) {
            val badge = badges.optJSONObject(i) ?: continue
            if (badge.has("musicInlineExplicitBadgeRenderer")) return true
            val iconType = badge.optJSONObject("musicInlineBadgeRenderer")
                ?.optJSONObject("icon")
                ?.optString("iconType")
            if (iconType == "MUSIC_EXPLICIT_BADGE") return true
        }
        return false
    }

    private fun hasLiveSignal(subtitle: String, badges: JSONArray?, overlays: JSONArray?): Boolean {
        if (subtitle.contains("watching", ignoreCase = true) ||
            subtitle.split("•", "·").any { it.trim().equals("LIVE", ignoreCase = true) }
        ) {
            return true
        }
        if (badges != null) {
            for (i in 0 until badges.length()) {
                val label = badges.optJSONObject(i)
                    ?.optJSONObject("liveBadgeRenderer")
                    ?.optJSONObject("label")
                    ?.optRunsText()
                if (!label.isNullOrBlank()) return true
            }
        }
        if (overlays != null) {
            for (i in 0 until overlays.length()) {
                val badgeStyle = overlays.optJSONObject(i)
                    ?.optJSONObject("thumbnailOverlayTimeStatusRenderer")
                    ?.optString("style")
                if (badgeStyle.equals("LIVE", ignoreCase = true)) return true
            }
        }
        return false
    }

    private fun findSectionListJson(root: JSONObject): JSONObject? {
        val contSectionList = root.optJSONObject("continuationContents")
            ?.optJSONObject("sectionListContinuation")
        if (contSectionList != null) return contSectionList

        val contents = root.optJSONObject("contents") ?: return null
        val singleTab = contents.optJSONObject("singleColumnBrowseResultsRenderer")
            ?.optJSONArray("tabs")?.optJSONObject(0)?.optJSONObject("tabRenderer")
            ?.optJSONObject("content")?.optJSONObject("sectionListRenderer")
        if (singleTab != null) return singleTab

        val twoColTab = contents.optJSONObject("twoColumnBrowseResultsRenderer")
            ?.optJSONArray("tabs")?.optJSONObject(0)?.optJSONObject("tabRenderer")
            ?.optJSONObject("content")?.optJSONObject("sectionListRenderer")
        if (twoColTab != null) return twoColTab

        return contents.optJSONObject("sectionListRenderer")
    }

    private fun extractContinuationToken(container: JSONObject?): String? {
        val continuations = container?.optJSONArray("continuations") ?: return null
        for (i in 0 until continuations.length()) {
            val cont = continuations.optJSONObject(i) ?: continue
            val next = cont.optJSONObject("nextContinuationData")?.optString("continuation")
                ?: cont.optJSONObject("nextRadioContinuationData")?.optString("continuation")
                ?: cont.optJSONObject("reloadContinuationData")?.optString("continuation")
            if (!next.isNullOrBlank()) return next
        }
        return null
    }

    private fun parseTwoRowItem(renderer: JSONObject): HomeMediaItem? {
        val title = renderer.optJSONObject("title").optRunsText()
        if (title.isBlank()) return null

        val subtitleObj = renderer.optJSONObject("subtitle")
        val subtitleRuns = subtitleObj?.optJSONArray("runs")
        val subtitle = subtitleObj.optRunsText()

        val thumbRenderer = renderer.optJSONObject("thumbnailRenderer")
            ?: renderer.optJSONObject("thumbnail")
        val thumbArr = thumbRenderer?.optJSONObject("musicThumbnailRenderer")
            ?.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
        val lastThumb = thumbArr?.let { if (it.length() > 0) it.optJSONObject(it.length() - 1) else null }
        val thumbW = lastThumb?.optInt("width", 0) ?: 0
        val thumbH = lastThumb?.optInt("height", 0) ?: 0

        val nav = renderer.optJSONObject("navigationEndpoint")
        val watchEndpoint = nav?.optJSONObject("watchEndpoint")
        val browseEndpoint = nav?.optJSONObject("browseEndpoint")
        val overlayPlayNav = renderer.optJSONObject("thumbnailOverlay")
            ?.optJSONObject("musicItemThumbnailOverlayRenderer")
            ?.optJSONObject("content")
            ?.optJSONObject("musicPlayButtonRenderer")
            ?.optJSONObject("playNavigationEndpoint")

        val videoId = watchEndpoint?.optString("videoId")?.takeIf { it.isNotBlank() }
        val browseId = browseEndpoint?.optString("browseId")?.takeIf { it.isNotBlank() }
        val musicVideoType = watchEndpoint
            ?.optJSONObject("watchEndpointMusicSupportedConfigs")
            ?.optJSONObject("watchEndpointMusicConfig")
            ?.optString("musicVideoType")

        val playPlaylistId = watchEndpoint?.optString("playlistId")?.takeIf { it.isNotBlank() }
            ?: overlayPlayNav?.optJSONObject("watchPlaylistEndpoint")?.optString("playlistId")?.takeIf { it.isNotBlank() }
            ?: overlayPlayNav?.optJSONObject("watchEndpoint")?.optString("playlistId")?.takeIf { it.isNotBlank() }

        var artistId: String? = null
        var views: String? = null
        if (subtitleRuns != null) {
            for (i in 0 until subtitleRuns.length()) {
                val run = subtitleRuns.optJSONObject(i) ?: continue
                val runText = run.optString("text", "")
                if (runText.contains("view", ignoreCase = true) || runText.contains("watching", ignoreCase = true)) {
                    views = runText.trim()
                }
                val runBrowse = run.optJSONObject("navigationEndpoint")
                    ?.optJSONObject("browseEndpoint")?.optString("browseId")
                if (artistId == null && runBrowse?.startsWith("UC") == true) {
                    artistId = runBrowse
                }
            }
        }

        val type: MediaType
        val id: String
        when {
            !videoId.isNullOrBlank() -> {
                type = MediaType.TRACK
                id = videoId
            }
            !browseId.isNullOrBlank() -> {
                id = browseId
                type = when {
                    browseId.startsWith("UC") -> MediaType.ARTIST
                    browseId.startsWith("MPREb") || subtitle.contains("Album", true) || subtitle.startsWith("EP ", true) || subtitle.startsWith("Single", true) -> MediaType.ALBUM
                    else -> MediaType.PLAYLIST
                }
            }
            else -> return null
        }

        val isExplicit = hasExplicitBadgeInArray(renderer.optJSONArray("subtitleBadges"))
        val isLive = hasLiveSignal(subtitle, renderer.optJSONArray("subtitleBadges"), renderer.optJSONArray("thumbnailOverlays"))
        val isWideVideo = type == MediaType.TRACK && (
            (thumbW > thumbH && thumbW > 0 && thumbH > 0) ||
                musicVideoType == "MUSIC_VIDEO_TYPE_OMV" ||
                musicVideoType == "MUSIC_VIDEO_TYPE_UGC" ||
                views != null ||
                renderer.optString("aspectRatio").contains("16_9")
            )
        val aspectRatio = if (isWideVideo) 16f / 9f else 1f
        val thumbUrl = upgradeHomeThumbUrl(lastThumb?.optString("url"), isWide = isWideVideo)

        return HomeMediaItem(
            id = id,
            title = title,
            subtitle = subtitle,
            thumbnailUrl = thumbUrl,
            type = type,
            isExplicit = isExplicit,
            isLive = isLive,
            views = views,
            aspectRatio = aspectRatio,
            artistId = artistId,
            playlistId = playPlaylistId
        )
    }

    private fun parseResponsiveListItemForHome(renderer: JSONObject): HomeMediaItem? {
        val flexCols = renderer.optJSONArray("flexColumns") ?: return null
        val col0TextObj = flexCols.optJSONObject(0)
            ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
            ?.optJSONObject("text")
        val title = col0TextObj.optRunsText()
        if (title.isBlank()) return null

        val col1TextObj = flexCols.optJSONObject(1)
            ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
            ?.optJSONObject("text")
        val col2TextObj = flexCols.optJSONObject(2)
            ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
            ?.optJSONObject("text")
        val subtitleRuns = col1TextObj?.optJSONArray("runs")
        val col1Text = col1TextObj.optRunsText()
        val col2Text = col2TextObj.optRunsText()
        val subtitle = if (col2Text.isNotBlank() && !col1Text.contains(col2Text)) {
            if (col1Text.isBlank()) col2Text else "$col1Text • $col2Text"
        } else col1Text

        val nav = renderer.optJSONObject("navigationEndpoint")
        val browseId = nav?.optJSONObject("browseEndpoint")?.optString("browseId")?.takeIf { it.isNotBlank() }
        val overlayWatch = renderer.optJSONObject("overlay")
            ?.optJSONObject("musicItemThumbnailOverlayRenderer")
            ?.optJSONObject("content")
            ?.optJSONObject("musicPlayButtonRenderer")
            ?.optJSONObject("playNavigationEndpoint")
            ?.optJSONObject("watchEndpoint")
        val videoId = renderer.optJSONObject("playlistItemData")?.optString("videoId")?.takeIf { it.isNotBlank() }
            ?: overlayWatch?.optString("videoId")?.takeIf { it.isNotBlank() }
            ?: nav?.optJSONObject("watchEndpoint")?.optString("videoId")?.takeIf { it.isNotBlank() }

        val thumbArr = renderer.optJSONObject("thumbnail")
            ?.optJSONObject("musicThumbnailRenderer")
            ?.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
        val lastThumb = thumbArr?.let { if (it.length() > 0) it.optJSONObject(it.length() - 1) else null }
        val thumbW = lastThumb?.optInt("width", 0) ?: 0
        val thumbH = lastThumb?.optInt("height", 0) ?: 0

        var artistId: String? = null
        var views: String? = null
        if (subtitleRuns != null) {
            for (i in 0 until subtitleRuns.length()) {
                val run = subtitleRuns.optJSONObject(i) ?: continue
                val t = run.optString("text", "")
                if (t.contains("view", true)) views = t.trim()
                val b = run.optJSONObject("navigationEndpoint")?.optJSONObject("browseEndpoint")?.optString("browseId")
                if (artistId == null && b?.startsWith("UC") == true) artistId = b
            }
        }

        val type: MediaType
        val id: String
        when {
            browseId?.startsWith("UC") == true && videoId == null -> {
                type = MediaType.ARTIST
                id = browseId
            }
            !videoId.isNullOrBlank() -> {
                type = MediaType.TRACK
                id = videoId
            }
            !browseId.isNullOrBlank() -> {
                id = browseId
                type = if (browseId.startsWith("MPREb")) MediaType.ALBUM else MediaType.PLAYLIST
            }
            else -> return null
        }

        val isWideVideo = type == MediaType.TRACK && ((thumbW > thumbH && thumbW > 0 && thumbH > 0) || views != null)
        val isExplicit = hasExplicitBadgeInArray(renderer.optJSONArray("badges"))
        val isLive = hasLiveSignal(subtitle, renderer.optJSONArray("badges"), null)
        val thumbUrl = upgradeHomeThumbUrl(lastThumb?.optString("url"), isWide = isWideVideo)

        val cleanArtistSubtitle = if (type == MediaType.TRACK && subtitleRuns != null) {
            val (artist, album) = parseMetadataFromJsonRuns(subtitleRuns)
            when {
                isWideVideo && views != null && artist != "Unknown" -> "$artist • $views"
                artist != "Unknown" && album != "Unknown" -> "$artist • $album"
                artist != "Unknown" -> artist
                else -> subtitle
            }
        } else subtitle

        return HomeMediaItem(
            id = id,
            title = title,
            subtitle = cleanArtistSubtitle,
            thumbnailUrl = thumbUrl,
            type = type,
            isExplicit = isExplicit,
            isLive = isLive,
            views = views,
            aspectRatio = if (isWideVideo) 16f / 9f else 1f,
            artistId = artistId ?: (if (type == MediaType.ARTIST) id else null),
            playlistId = overlayWatch?.optString("playlistId")?.takeIf { it.isNotBlank() }
        )
    }

    fun parseHomeFeedRaw(jsonString: String): HomeFeedPage {
        return try {
            val root = JSONObject(jsonString)
            val visitorData = root.optJSONObject("responseContext")?.optString("visitorData")?.takeIf { it.isNotBlank() }
            val bgUrl = root.optJSONObject("background")
                ?.optJSONObject("musicThumbnailRenderer")
                ?.optJSONObject("thumbnail")
                ?.optJSONArray("thumbnails")
                ?.let { arr -> if (arr.length() > 0) arr.optJSONObject(arr.length() - 1)?.optString("url") else null }
                ?.let { upgradeHomeThumbUrl(it, isWide = true) }
                ?.takeIf { it.isNotBlank() }

            val sectionList = findSectionListJson(root) ?: return HomeFeedPage(emptyList(), null, bgUrl, visitorData)
            val continuation = extractContinuationToken(sectionList)
            val arr = sectionList.optJSONArray("contents") ?: return HomeFeedPage(emptyList(), continuation, bgUrl, visitorData)

            val sections = mutableListOf<HomeSection>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue

                obj.optJSONObject("musicCarouselShelfRenderer")?.let { carousel ->
                    val basicHeader = carousel.optJSONObject("header")
                        ?.optJSONObject("musicCarouselShelfBasicHeaderRenderer")
                    val title = basicHeader?.optJSONObject("title").optRunsText().ifBlank { "Recommended" }
                    val strapline = basicHeader?.optJSONObject("strapline").optRunsText().takeIf { it.isNotBlank() }
                    val headerThumb = basicHeader?.optJSONObject("thumbnail")
                        ?.optJSONObject("musicThumbnailRenderer")
                        ?.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                        ?.let { t -> if (t.length() > 0) t.optJSONObject(t.length() - 1)?.optString("url") else null }
                        ?.let { upgradeHomeThumbUrl(it) }
                        ?.takeIf { it.isNotBlank() }

                    val titleNav = basicHeader?.optJSONObject("title")?.optJSONArray("runs")
                        ?.optJSONObject(0)?.optJSONObject("navigationEndpoint")
                    val moreBtnNav = basicHeader?.optJSONObject("moreContentButton")
                        ?.optJSONObject("buttonRenderer")?.optJSONObject("navigationEndpoint")

                    val titleBrowseId = titleNav?.optJSONObject("browseEndpoint")?.optString("browseId")
                    val channelId = titleBrowseId?.takeIf { it.startsWith("UC") }
                    val moreBrowseEndpoint = moreBtnNav?.optJSONObject("browseEndpoint")
                        ?: titleNav?.optJSONObject("browseEndpoint")?.takeIf { !it.optString("browseId").startsWith("UC") }
                    val moreBrowseId = moreBrowseEndpoint?.optString("browseId")?.takeIf { it.isNotBlank() }
                    val moreParams = moreBrowseEndpoint?.optString("params")?.takeIf { it.isNotBlank() }

                    val playAllEndpoint = moreBtnNav?.optJSONObject("watchPlaylistEndpoint")
                        ?: moreBtnNav?.optJSONObject("watchEndpoint")
                    val playAllPlaylistId = playAllEndpoint?.optString("playlistId")?.takeIf { it.isNotBlank() }
                    val playAllVideoId = playAllEndpoint?.optString("videoId")?.takeIf { it.isNotBlank() }

                    val items = mutableListOf<HomeMediaItem>()
                    val contents = carousel.optJSONArray("contents")
                    if (contents != null) {
                        for (j in 0 until contents.length()) {
                            val itemObj = contents.optJSONObject(j) ?: continue
                            val parsed = itemObj.optJSONObject("musicTwoRowItemRenderer")?.let(::parseTwoRowItem)
                                ?: itemObj.optJSONObject("musicResponsiveListItemRenderer")?.let(::parseResponsiveListItemForHome)
                            if (parsed != null) items.add(parsed)
                        }
                    }

                    if (items.isNotEmpty()) {
                        sections.add(
                            HomeSection(
                                title = title,
                                items = items,
                                strapline = strapline,
                                thumbnailUrl = headerThumb,
                                channelId = channelId,
                                moreBrowseId = moreBrowseId,
                                moreParams = moreParams,
                                playAllPlaylistId = playAllPlaylistId,
                                playAllVideoId = playAllVideoId
                            )
                        )
                    }
                }

                obj.optJSONObject("musicShelfRenderer")?.let { shelf ->
                    val title = shelf.optJSONObject("title").optRunsText().ifBlank { "Quick picks" }
                    val strapline = shelf.optJSONObject("strapline").optRunsText().ifBlank { "LET'S START WITH A RADIO" }
                    val bottomEndpoint = shelf.optJSONObject("bottomEndpoint")
                    val moreBrowseId = bottomEndpoint?.optJSONObject("browseEndpoint")?.optString("browseId")?.takeIf { it.isNotBlank() }
                    val moreParams = bottomEndpoint?.optJSONObject("browseEndpoint")?.optString("params")?.takeIf { it.isNotBlank() }

                    val items = mutableListOf<HomeMediaItem>()
                    val contents = shelf.optJSONArray("contents")
                    if (contents != null) {
                        for (j in 0 until contents.length()) {
                            val itemObj = contents.optJSONObject(j) ?: continue
                            val parsed = itemObj.optJSONObject("musicResponsiveListItemRenderer")?.let(::parseResponsiveListItemForHome)
                                ?: itemObj.optJSONObject("musicTwoRowItemRenderer")?.let(::parseTwoRowItem)
                            if (parsed != null) items.add(parsed)
                        }
                    }
                    if (items.isNotEmpty()) {
                        sections.add(
                            HomeSection(
                                title = title,
                                items = items,
                                strapline = strapline,
                                moreBrowseId = moreBrowseId,
                                moreParams = moreParams
                            )
                        )
                    }
                }

                // Grid shelf inside category/mood browse pages
                obj.optJSONObject("gridRenderer")?.let { grid ->
                    val gridTitle = grid.optJSONObject("header")?.optJSONObject("gridHeaderRenderer")
                        ?.optJSONObject("title").optRunsText().ifBlank { "Featured" }
                    val items = mutableListOf<HomeMediaItem>()
                    val gridItems = grid.optJSONArray("items")
                    if (gridItems != null) {
                        for (j in 0 until gridItems.length()) {
                            val itemObj = gridItems.optJSONObject(j) ?: continue
                            val parsed = itemObj.optJSONObject("musicTwoRowItemRenderer")?.let(::parseTwoRowItem)
                                ?: itemObj.optJSONObject("musicResponsiveListItemRenderer")?.let(::parseResponsiveListItemForHome)
                            if (parsed != null) items.add(parsed)
                        }
                    }
                    if (items.isNotEmpty()) {
                        sections.add(HomeSection(title = gridTitle, items = items))
                    }
                }
            }

            HomeFeedPage(sections = sections, continuation = continuation, backgroundUrl = bgUrl, visitorData = visitorData)
        } catch (e: Exception) {
            Log.e(TAG, "parseHomeFeedRaw failed", e)
            HomeFeedPage(emptyList())
        }
    }

    fun parseMoodsAndGenresRaw(jsonString: String): HomeMoodsData {
        return try {
            val root = JSONObject(jsonString)
            val sectionList = findSectionListJson(root) ?: return HomeMoodsData()
            val arr = sectionList.optJSONArray("contents") ?: return HomeMoodsData()

            val moods = mutableListOf<MoodCategoryItem>()
            val genres = mutableListOf<MoodCategoryItem>()

            for (i in 0 until arr.length()) {
                val grid = arr.optJSONObject(i)?.optJSONObject("gridRenderer") ?: continue
                val title = grid.optJSONObject("header")?.optJSONObject("gridHeaderRenderer")
                    ?.optJSONObject("title").optRunsText()
                val itemsArr = grid.optJSONArray("items") ?: continue
                val parsedItems = mutableListOf<MoodCategoryItem>()
                for (j in 0 until itemsArr.length()) {
                    val navBtn = itemsArr.optJSONObject(j)?.optJSONObject("musicNavigationButtonRenderer") ?: continue
                    val label = navBtn.optJSONObject("buttonText").optRunsText()
                    val browseEp = navBtn.optJSONObject("clickCommand")?.optJSONObject("browseEndpoint")
                    val params = browseEp?.optString("params") ?: ""
                    val browseId = browseEp?.optString("browseId")?.ifBlank { "FEmusic_moods_and_genres_category" }
                        ?: "FEmusic_moods_and_genres_category"
                    val stripeColor = navBtn.optJSONObject("solid")?.optLong("leftStripeColor", 0L)?.takeIf { it != 0L }
                        ?: 0xFF7C4DFFL
                    if (label.isNotBlank() && params.isNotBlank()) {
                        parsedItems.add(MoodCategoryItem(title = label, params = params, browseId = browseId, stripeColor = stripeColor))
                    }
                }
                if (parsedItems.isNotEmpty()) {
                    if (title.contains("Genre", ignoreCase = true) || (moods.isNotEmpty() && !title.contains("Mood", ignoreCase = true))) {
                        genres.addAll(parsedItems)
                    } else {
                        moods.addAll(parsedItems)
                    }
                }
            }

            HomeMoodsData(moodsAndMoments = moods, genres = genres)
        } catch (e: Exception) {
            Log.e(TAG, "parseMoodsAndGenresRaw failed", e)
            HomeMoodsData()
        }
    }

    fun parseChartsRaw(jsonString: String, countryName: String): HomeChartData {
        return try {
            val root = JSONObject(jsonString)
            val sectionList = findSectionListJson(root) ?: return HomeChartData(countryName)
            val arr = sectionList.optJSONArray("contents") ?: return HomeChartData(countryName)

            val videoCharts = mutableListOf<HomeMediaItem>()
            val chartPlaylists = mutableListOf<HomeMediaItem>()
            val topArtists = mutableListOf<HomeChartArtist>()
            val topPodcasts = mutableListOf<HomeChartPodcast>()

            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue

                obj.optJSONObject("musicCarouselShelfRenderer")?.let { carousel ->
                    val title = carousel.optJSONObject("header")
                        ?.optJSONObject("musicCarouselShelfBasicHeaderRenderer")
                        ?.optJSONObject("title").optRunsText()
                    val titleLower = title.lowercase()
                    val contents = carousel.optJSONArray("contents") ?: return@let

                    for (j in 0 until contents.length()) {
                        val itemObj = contents.optJSONObject(j) ?: continue
                        itemObj.optJSONObject("musicResponsiveListItemRenderer")?.let { renderer ->
                            val parsed = parseResponsiveListItemForHome(renderer)
                            val rank = renderer.optJSONObject("customIndexColumn")
                                ?.optJSONObject("musicCustomIndexColumnRenderer")
                                ?.optJSONObject("text").optRunsText().ifBlank { "${j + 1}" }
                            if (parsed != null) {
                                when {
                                    parsed.type == MediaType.ARTIST || titleLower.contains("artist") -> {
                                        topArtists.add(
                                            HomeChartArtist(
                                                rank = rank,
                                                title = parsed.title,
                                                subscribers = parsed.subtitle,
                                                thumbnailUrl = parsed.thumbnailUrl,
                                                browseId = parsed.id
                                            )
                                        )
                                    }
                                    titleLower.contains("podcast") -> {
                                        topPodcasts.add(
                                            HomeChartPodcast(
                                                rank = rank,
                                                title = parsed.title,
                                                host = parsed.subtitle,
                                                thumbnailUrl = parsed.thumbnailUrl,
                                                browseId = parsed.id
                                            )
                                        )
                                    }
                                    titleLower.contains("video") || parsed.aspectRatio > 1f -> {
                                        videoCharts.add(parsed.copy(aspectRatio = 16f / 9f))
                                    }
                                    else -> chartPlaylists.add(parsed)
                                }
                            }
                        }
                        itemObj.optJSONObject("musicTwoRowItemRenderer")?.let { renderer ->
                            val parsed = parseTwoRowItem(renderer) ?: return@let
                            when {
                                parsed.type == MediaType.ARTIST || titleLower.contains("artist") -> {
                                    topArtists.add(
                                        HomeChartArtist(
                                            rank = "${topArtists.size + 1}",
                                            title = parsed.title,
                                            subscribers = parsed.subtitle,
                                            thumbnailUrl = parsed.thumbnailUrl,
                                            browseId = parsed.id
                                        )
                                    )
                                }
                                titleLower.contains("podcast") || parsed.id.startsWith("MPSP") -> {
                                    topPodcasts.add(
                                        HomeChartPodcast(
                                            rank = "${topPodcasts.size + 1}",
                                            title = parsed.title,
                                            host = parsed.subtitle,
                                            thumbnailUrl = parsed.thumbnailUrl,
                                            browseId = parsed.id
                                        )
                                    )
                                }
                                titleLower.contains("video") || parsed.aspectRatio > 1f || parsed.type == MediaType.TRACK -> {
                                    videoCharts.add(parsed.copy(aspectRatio = 16f / 9f))
                                }
                                else -> chartPlaylists.add(parsed)
                            }
                        }
                    }
                }

                obj.optJSONObject("musicShelfRenderer")?.let { shelf ->
                    val title = shelf.optJSONObject("title").optRunsText()
                    val titleLower = title.lowercase()
                    val contents = shelf.optJSONArray("contents") ?: return@let
                    for (j in 0 until contents.length()) {
                        val renderer = contents.optJSONObject(j)?.optJSONObject("musicResponsiveListItemRenderer") ?: continue
                        val parsed = parseResponsiveListItemForHome(renderer) ?: continue
                        val rank = renderer.optJSONObject("customIndexColumn")
                            ?.optJSONObject("musicCustomIndexColumnRenderer")
                            ?.optJSONObject("text").optRunsText().ifBlank { "${j + 1}" }
                        if (parsed.type == MediaType.ARTIST || titleLower.contains("artist")) {
                            topArtists.add(
                                HomeChartArtist(
                                    rank = rank,
                                    title = parsed.title,
                                    subscribers = parsed.subtitle,
                                    thumbnailUrl = parsed.thumbnailUrl,
                                    browseId = parsed.id
                                )
                            )
                        } else if (titleLower.contains("podcast")) {
                            topPodcasts.add(
                                HomeChartPodcast(
                                    rank = rank,
                                    title = parsed.title,
                                    host = parsed.subtitle,
                                    thumbnailUrl = parsed.thumbnailUrl,
                                    browseId = parsed.id
                                )
                            )
                        } else {
                            videoCharts.add(parsed)
                        }
                    }
                }
            }

            HomeChartData(
                countryName = countryName,
                videoCharts = videoCharts.distinctBy { it.id },
                chartPlaylists = chartPlaylists.distinctBy { it.id },
                topArtists = topArtists.distinctBy { it.browseId.ifBlank { it.title } },
                topPodcasts = topPodcasts.distinctBy { it.browseId.ifBlank { it.title } }
            )
        } catch (e: Exception) {
            Log.e(TAG, "parseChartsRaw failed", e)
            HomeChartData(countryName)
        }
    }

    /**
     * Parses YouTube Music playlist or podcast items from search JSON (`tabbedSearchResultsRenderer` shelves).
     */
    fun parsePlaylistsFromSearchJson(jsonString: String): List<HomeMediaItem> {
        return try {
            val root = JSONObject(jsonString)
            val tabs = root.optJSONObject("contents")
                ?.optJSONObject("tabbedSearchResultsRenderer")
                ?.optJSONArray("tabs") ?: return emptyList()
            val sections = tabs.optJSONObject(0)
                ?.optJSONObject("tabRenderer")?.optJSONObject("content")
                ?.optJSONObject("sectionListRenderer")?.optJSONArray("contents") ?: return emptyList()

            val playlists = mutableListOf<HomeMediaItem>()
            for (i in 0 until sections.length()) {
                val obj = sections.optJSONObject(i) ?: continue
                val shelfItems = obj.optJSONObject("musicShelfRenderer")?.optJSONArray("contents")
                if (shelfItems != null) {
                    for (j in 0 until shelfItems.length()) {
                        val renderer = shelfItems.optJSONObject(j)?.optJSONObject("musicResponsiveListItemRenderer") ?: continue
                        val parsed = parseResponsiveListItemForHome(renderer)
                        if (parsed != null && (
                                parsed.type == MediaType.PLAYLIST ||
                                parsed.id.startsWith("VL") ||
                                parsed.id.startsWith("PL") ||
                                parsed.id.startsWith("RD") ||
                                parsed.id.startsWith("MPSP")
                            )
                        ) {
                            playlists.add(parsed.copy(type = MediaType.PLAYLIST))
                        }
                    }
                }
                val carouselItems = obj.optJSONObject("musicCarouselShelfRenderer")?.optJSONArray("contents")
                if (carouselItems != null) {
                    for (j in 0 until carouselItems.length()) {
                        val renderer = carouselItems.optJSONObject(j)?.optJSONObject("musicTwoRowItemRenderer") ?: continue
                        val parsed = parseTwoRowItem(renderer)
                        if (parsed != null && parsed.type == MediaType.PLAYLIST) {
                            playlists.add(parsed)
                        }
                    }
                }
            }
            playlists.distinctBy { it.id }
        } catch (e: Exception) {
            Log.e(TAG, "parsePlaylistsFromSearchJson failed", e)
            emptyList()
        }
    }

    private fun isVideoTrackSignal(
        musicVideoType: String?,
        rawThumbUrl: String?,
        thumbWidth: Int,
        thumbHeight: Int,
        hasExplicitVideoRun: Boolean,
        hasViewsOrLikesRun: Boolean,
        hasAlbumBrowseId: Boolean,
        titleLower: String
    ): Boolean {
        if (musicVideoType == "MUSIC_VIDEO_TYPE_ATV") return false
        if (musicVideoType == "MUSIC_VIDEO_TYPE_OMV" ||
            musicVideoType == "MUSIC_VIDEO_TYPE_UGC" ||
            musicVideoType == "MUSIC_VIDEO_TYPE_OFFICIAL_SOURCE_MUSIC"
        ) {
            return true
        }
        if (hasExplicitVideoRun) return true
        val isWidescreenThumb = (thumbWidth > 0 && thumbHeight > 0 && thumbWidth > thumbHeight) ||
            (rawThumbUrl?.contains("i.ytimg.com/vi/", ignoreCase = true) == true)
        if (!hasAlbumBrowseId && (hasViewsOrLikesRun || isWidescreenThumb)) return true
        return titleLower.contains("official music video") ||
            titleLower.contains("official video") ||
            titleLower.contains("(music video)") ||
            titleLower.contains("[music video]") ||
            titleLower.contains("[mv]") ||
            titleLower.contains("(mv)") ||
            titleLower.contains("lyrical video") ||
            titleLower.contains("lyric video")
    }

    fun isVideoFromRuns(runs: List<Run>?): Boolean {
        if (runs.isNullOrEmpty()) return false
        return runs.any { run ->
            val t = run.text?.trim()?.lowercase() ?: ""
            t == "video" || (t.matches(Regex(""".*\bvideo\b.*""")) && !t.contains("view"))
        }
    }

    fun isVideoFromJsonRuns(runs: org.json.JSONArray?): Boolean {
        if (runs == null) return false
        for (i in 0 until runs.length()) {
            val t = runs.optJSONObject(i)?.optString("text")?.trim()?.lowercase() ?: ""
            if (t == "video" || (t.matches(Regex(""".*\bvideo\b.*""")) && !t.contains("view"))) return true
        }
        return false
    }

    private fun hasViewsOrLikesFromRuns(runs: List<Run>?): Boolean {
        if (runs.isNullOrEmpty()) return false
        return runs.any { run ->
            val t = run.text?.lowercase() ?: ""
            t.contains("view") || Regex("""\b\d[\d.,]*\s*[kmb]?\s*likes?\b""").containsMatchIn(t)
        }
    }

    private fun hasViewsOrLikesFromJsonRuns(runs: org.json.JSONArray?): Boolean {
        if (runs == null) return false
        for (i in 0 until runs.length()) {
            val t = runs.optJSONObject(i)?.optString("text")?.lowercase() ?: ""
            if (t.contains("view") || Regex("""\b\d[\d.,]*\s*[kmb]?\s*likes?\b""").containsMatchIn(t)) return true
        }
        return false
    }

    private fun hasAlbumBrowseIdInJsonRuns(runs: org.json.JSONArray?): Boolean {
        if (runs == null) return false
        for (i in 0 until runs.length()) {
            val browseId = runs.optJSONObject(i)
                ?.optJSONObject("navigationEndpoint")
                ?.optJSONObject("browseEndpoint")
                ?.optString("browseId")
            if (browseId?.startsWith("MPREb") == true) return true
        }
        return false
    }

    private fun isMetadataNoiseSegment(seg: String): Boolean {
        val s = seg.trim().lowercase()
        return s.isEmpty() ||
            s == "song" || s == "video" || s == "single" || s == "album" || s == "track" || s == "artist" ||
            s.contains("view") ||
            s.contains("subscriber") ||
            Regex("""^[\d.,]+\s*[kmb]?\s*(likes?|plays?)$""").matches(s) ||
            Regex("""^\d+:\d+(:\d+)?$""").matches(s)
    }

    fun parseMetadataFromRuns(runs: List<Run>?): Pair<String, String> {
        if (runs.isNullOrEmpty()) return Pair("Unknown", "Unknown")
        val segments = mutableListOf<String>()
        var currentSegment = StringBuilder()
        for (run in runs) {
            val text = run.text ?: ""
            val bulletIndex = text.indexOfFirst { it == '•' || it == '·' || it == '●' }
            if (bulletIndex != -1) {
                val parts = text.split(Regex("[•·●]"))
                if (parts.isNotEmpty()) {
                    currentSegment.append(parts[0])
                    val finished = currentSegment.toString().trim()
                    if (finished.isNotEmpty()) segments.add(finished)
                    currentSegment = StringBuilder()
                    for (k in 1 until parts.size) {
                        currentSegment.append(parts[k])
                        if (k < parts.size - 1) {
                            val innerFinished = currentSegment.toString().trim()
                            if (innerFinished.isNotEmpty()) segments.add(innerFinished)
                            currentSegment = StringBuilder()
                        }
                    }
                }
            } else {
                currentSegment.append(text)
            }
        }
        val lastFinished = currentSegment.toString().trim()
        if (lastFinished.isNotEmpty()) segments.add(lastFinished)
        if (segments.isEmpty()) return Pair("Unknown", "Unknown")

        // Clean out type tags, duration tags, view counts, and like counts
        val cleanedSegments = segments.filterNot(::isMetadataNoiseSegment)

        val artist = cleanedSegments.getOrNull(0) ?: "Unknown"
        val candidateAlbum = cleanedSegments.getOrNull(1)

        val album = when {
            candidateAlbum != null && candidateAlbum.isNotBlank() -> candidateAlbum
            else -> "Unknown"
        }

        return Pair(artist, album)
    }

    fun parseMetadataFromJsonRuns(runs: org.json.JSONArray?): Pair<String, String> {
        if (runs == null || runs.length() == 0) return Pair("Unknown", "Unknown")
        val segments = mutableListOf<String>()
        var currentSegment = StringBuilder()
        for (i in 0 until runs.length()) {
            val run = runs.optJSONObject(i) ?: continue
            val text = run.optString("text") ?: ""
            val bulletIndex = text.indexOfFirst { it == '•' || it == '·' || it == '●' }
            if (bulletIndex != -1) {
                val parts = text.split(Regex("[•·●]"))
                if (parts.isNotEmpty()) {
                    currentSegment.append(parts[0])
                    val finished = currentSegment.toString().trim()
                    if (finished.isNotEmpty()) segments.add(finished)
                    currentSegment = StringBuilder()
                    for (k in 1 until parts.size) {
                        currentSegment.append(parts[k])
                        if (k < parts.size - 1) {
                            val innerFinished = currentSegment.toString().trim()
                            if (innerFinished.isNotEmpty()) segments.add(innerFinished)
                            currentSegment = StringBuilder()
                        }
                    }
                }
            } else {
                currentSegment.append(text)
            }
        }
        val lastFinished = currentSegment.toString().trim()
        if (lastFinished.isNotEmpty()) segments.add(lastFinished)
        if (segments.isEmpty()) return Pair("Unknown", "Unknown")

        // Clean out type tags, duration tags, view counts, and like counts
        val cleanedSegments = segments.filterNot(::isMetadataNoiseSegment)

        val artist = cleanedSegments.getOrNull(0) ?: "Unknown"
        val candidateAlbum = cleanedSegments.getOrNull(1)

        val album = when {
            candidateAlbum != null && candidateAlbum.isNotBlank() -> candidateAlbum
            else -> "Unknown"
        }

        return Pair(artist, album)
    }
}
