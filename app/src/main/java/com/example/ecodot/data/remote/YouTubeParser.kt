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
    val artists: List<Artist> = emptyList()
)

object YouTubeParser {
    private const val TAG = "YouTubeParser"

    fun getHighResUrl(url: String?): String? {
        if (url == null) return null
        return if (url.contains("=w") && url.contains("-h")) {
            url.replace(Regex("=w\\d+-h\\d+"), "=w1080-h1080")
        } else if (url.contains("hqdefault.jpg")) {
            url.replace("hqdefault.jpg", "maxresdefault.jpg")
        } else {
            url
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
                songs.add(track)
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
                            val album = if (parsedAlbum != "Unknown") parsedAlbum else "Top Result"
                            val isExplicit = title.contains("explicit", true) || subtitle.contains("explicit", true)
                            songs.add(0, Track(
                                id = videoId,
                                title = title,
                                artist = artist,
                                album = album,
                                duration = 0,
                                path = "https://www.youtube.com/watch?v=$videoId",
                                albumArtUri = artUrl,
                                isYouTube = true,
                                isExplicit = isExplicit
                            ))
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
            artists = finalArtists.distinctBy { it.id }
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
                
                val artistRuns = item.optJSONObject("longBylineText")?.optJSONArray("runs")
                val (artist, parsedAlbum) = parseMetadataFromJsonRuns(artistRuns)
                val album = if (parsedAlbum != "Unknown") parsedAlbum else "Up Next"
                
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
                
                val thumbnailsArr = item.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                val originalThumb = thumbnailsArr?.optJSONObject(thumbnailsArr.length() - 1)?.optString("url")
                val highResThumbnail = originalThumb?.let { originalUrl ->
                    if (originalUrl.contains("=w") && originalUrl.contains("-h")) {
                        originalUrl.replace(Regex("=w\\d+-h\\d+"), "=w512-h512")
                    } else if (originalUrl.contains("hqdefault.jpg")) {
                        originalUrl.replace("hqdefault.jpg", "maxresdefault.jpg")
                    } else {
                        originalUrl
                    }
                }

                tracks.add(Track(
                    id = videoId,
                    title = title,
                    artist = artist,
                    album = album,
                    duration = 0,
                    path = "https://www.youtube.com/watch?v=$videoId",
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

        val videoId = renderer.playlistItemData?.videoId 
            ?: renderer.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId
            ?: renderer.navigationEndpoint?.watchEndpoint?.videoId
            ?: titleFlex?.getOrNull(0)?.navigationEndpoint?.watchEndpoint?.videoId
            ?: ""

        val title = titleFlex?.getOrNull(0)?.text ?: "Unknown"
        
        val (artist, parsedAlbum) = parseMetadataFromRuns(subtitleFlex)
        val artistId = subtitleFlex?.find { it.navigationEndpoint?.browseEndpoint?.browseId?.startsWith("UC") == true }
            ?.navigationEndpoint?.browseEndpoint?.browseId
        val album = if (parsedAlbum != "Unknown") parsedAlbum else "Unknown"
        
        val albumId = subtitleFlex?.find { it.navigationEndpoint?.browseEndpoint?.browseId?.startsWith("MPREb") == true }
            ?.navigationEndpoint?.browseEndpoint?.browseId

        val highResThumbnail = getHighResUrl(renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url)

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
            path = "https://www.youtube.com/watch?v=$videoId",
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
                    sections.add(HomeSection(title, items))
                }
            }

            content.musicShelfRenderer?.let { listShelf ->
                val title = listShelf.title?.runs?.joinToString("") { it.text } ?: "Quick Picks"
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
                    sections.add(HomeSection(title, items))
                }
            }
        }
        
        return sections
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
        
        val firstSeg = segments[0].lowercase()
        val isType = firstSeg == "song" || firstSeg == "video" || firstSeg == "single" || firstSeg == "album" || firstSeg == "track"
        
        val artist: String
        val album: String
        if (isType) {
            artist = if (segments.size > 1) segments[1] else "Unknown"
            album = if (segments.size > 2) segments[2] else "Unknown"
        } else {
            artist = segments[0]
            album = if (segments.size > 1) segments[1] else "Unknown"
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
        
        val firstSeg = segments[0].lowercase()
        val isType = firstSeg == "song" || firstSeg == "video" || firstSeg == "single" || firstSeg == "album" || firstSeg == "track"
        
        val artist: String
        val album: String
        if (isType) {
            artist = if (segments.size > 1) segments[1] else "Unknown"
            album = if (segments.size > 2) segments[2] else "Unknown"
        } else {
            artist = segments[0]
            album = if (segments.size > 1) segments[1] else "Unknown"
        }
        return Pair(artist, album)
    }
}
