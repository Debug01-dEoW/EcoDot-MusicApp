package com.example.ecodot.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.remote.YouTubeBrowseResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ArtistUiState(
    val artistName: String = "",
    val headerImageUrl: String? = null,
    val bio: String = "",
    val topTracks: List<Track> = emptyList(),
    val albums: List<ArtistCollection> = emptyList(),
    val latestRelease: ArtistCollection? = null,
    val subscribers: String? = null,
    val views: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ArtistCollection(
    val id: String,
    val title: String,
    val year: String?,
    val thumbnailUrl: String?,
    val type: String // "Album", "Single", etc.
)

class ArtistViewModel(application: Application) : AndroidViewModel(application) {
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
    
    private val _uiState = MutableStateFlow(ArtistUiState())
    val uiState: StateFlow<ArtistUiState> = _uiState.asStateFlow()

    private suspend fun getLocalTracksForArtist(artistName: String): List<Track> {
        return try {
            repository.allTracks.first().filter { 
                it.artist.equals(artistName, ignoreCase = true) || it.artistId == artistName 
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadArtist(artistId: String) {
        if (artistId.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val isYouTubeArtist = artistId.startsWith("UC") || artistId.startsWith("FE")
            
            if (isYouTubeArtist) {
                val response = repository.getArtistDetails(artistId)
                if (response != null) {
                    parseArtistResponse(response)
                    
                    // Fallback: If headerImageUrl is empty, query YouTube Music search to fetch the artist's official profile image
                    if (_uiState.value.headerImageUrl.isNullOrEmpty() && _uiState.value.artistName.isNotEmpty()) {
                        try {
                            val searchResults = repository.searchYouTubeCategorized(_uiState.value.artistName, "Artists")
                            val artistResult = searchResults.artists.firstOrNull { it.name.equals(_uiState.value.artistName, ignoreCase = true) }
                                ?: searchResults.artists.firstOrNull()
                            if (artistResult != null && !artistResult.imageUrl.isNullOrEmpty()) {
                                _uiState.value = _uiState.value.copy(headerImageUrl = artistResult.imageUrl)
                            }
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                } else {
                    loadArtistByName(artistId)
                }
            } else {
                loadArtistByName(artistId)
            }
        }
    }

    fun loadArtistByName(artistName: String) {
        if (artistName.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, artistName = artistName)
            try {
                val localTracks = getLocalTracksForArtist(artistName)
                
                val searchResults = repository.searchYouTubeCategorized(artistName, "Artists")
                val artistResult = searchResults.artists.firstOrNull { it.name.equals(artistName, ignoreCase = true) }
                    ?: searchResults.artists.firstOrNull()
                
                if (artistResult != null) {
                    val detailsResponse = repository.getArtistDetails(artistResult.id)
                    if (detailsResponse != null) {
                        parseArtistResponse(detailsResponse)
                    }
                    
                    if (_uiState.value.headerImageUrl.isNullOrEmpty() && !artistResult.imageUrl.isNullOrEmpty()) {
                        _uiState.value = _uiState.value.copy(headerImageUrl = artistResult.imageUrl)
                    }
                    
                    val combinedTracks = (localTracks + _uiState.value.topTracks).distinctBy { it.id }
                    _uiState.value = _uiState.value.copy(
                        topTracks = combinedTracks,
                        isLoading = false
                    )
                } else {
                    if (localTracks.isNotEmpty()) {
                        _uiState.value = ArtistUiState(
                            artistName = artistName,
                            topTracks = localTracks,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Artist not found online or offline")
                    }
                }
            } catch (e: Exception) {
                val localTracks = getLocalTracksForArtist(artistName)
                if (localTracks.isNotEmpty()) {
                    _uiState.value = ArtistUiState(
                        artistName = artistName,
                        topTracks = localTracks,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
                }
            }
        }
    }

    private fun parseArtistResponse(response: YouTubeBrowseResponse) {
        val immersiveHeader = response.header?.musicImmersiveHeaderRenderer
        val visualHeader = response.header?.musicVisualHeaderRenderer
        val detailHeader = response.header?.musicDetailHeaderRenderer

        val artistName = immersiveHeader?.title?.runs?.getOrNull(0)?.text
            ?: visualHeader?.title?.runs?.getOrNull(0)?.text
            ?: detailHeader?.title?.runs?.getOrNull(0)?.text
            ?: ""

        val headerImageUrl = immersiveHeader?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
            ?: visualHeader?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
            ?: detailHeader?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
            
        // Try extracting subscribers/views. Usually formatted like "• 1M subscribers"
        val headerDescription = immersiveHeader?.description?.runs?.joinToString("") { it.text } 
            ?: detailHeader?.subtitle?.runs?.joinToString("") { it.text }
            ?: ""
        
        var subscribers: String? = null
        var views: String? = null
        
        if (headerDescription.contains("subscribers", true)) {
            val parts = headerDescription.split("•")
            subscribers = parts.find { it.contains("subscribers", true) }?.trim()
            views = parts.find { it.contains("views", true) }?.trim()
        }
        
        val sections = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.getOrNull(0)
            ?.tabRenderer?.content?.sectionListRenderer?.contents 
            ?: response.contents?.twoColumnBrowseResultsRenderer?.tabs?.getOrNull(0)
            ?.tabRenderer?.content?.sectionListRenderer?.contents
            ?: emptyList()

        val topTracks = mutableListOf<Track>()
        val albums = mutableListOf<ArtistCollection>()
        var latestRelease: ArtistCollection? = null
        var bio = ""

        sections.forEach { section ->
            // Songs / Top Tracks OR Latest Release in normal shelf
            section.musicShelfRenderer?.let { s ->
                val shelfTitle = s.title?.runs?.getOrNull(0)?.text?.lowercase() ?: ""
                if (shelfTitle.contains("songs") || shelfTitle.contains("popular")) {
                    s.contents?.forEach { item ->
                        item.musicResponsiveListItemRenderer?.let { renderer ->
                            topTracks.add(parseTrackRendererFromArtist(renderer, artistName))
                        }
                    }
                } else if (shelfTitle.contains("latest release") || shelfTitle.contains("featured")) {
                    val firstItem = s.contents?.getOrNull(0)?.musicResponsiveListItemRenderer
                    if (firstItem != null) {
                        val titleFlex = firstItem.flexColumns?.getOrNull(0)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                        val title = titleFlex?.getOrNull(0)?.text ?: "Unknown"
                        val browseId = firstItem.navigationEndpoint?.browseEndpoint?.browseId
                            ?: titleFlex?.getOrNull(0)?.navigationEndpoint?.browseEndpoint?.browseId
                            ?: ""
                        val thumb = firstItem.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
                        val subtitleFlex = firstItem.flexColumns?.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                        val subtitle = subtitleFlex?.getOrNull(0)?.text
                        latestRelease = ArtistCollection(browseId, title, subtitle, thumb, "Latest Release")
                    }
                }
            }
            
            // Latest Release in Card Shelf (usually featured releases)
            section.musicCardShelfRenderer?.let { card ->
                val title = card.title?.runs?.getOrNull(0)?.text ?: "Unknown"
                val browseId = card.title?.runs?.getOrNull(0)?.navigationEndpoint?.browseEndpoint?.browseId
                    ?: card.buttons?.getOrNull(0)?.musicPlayButtonRenderer?.playNavigationEndpoint?.browseEndpoint?.browseId
                    ?: ""
                val subtitle = card.subtitle?.runs?.getOrNull(0)?.text
                val thumb = card.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
                latestRelease = ArtistCollection(browseId, title, subtitle, thumb, "Latest Release")
            }

            // Albums / Singles / EPs Carousels
            section.musicCarouselShelfRenderer?.let { carousel ->
                val shelfTitle = carousel.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.getOrNull(0)?.text ?: ""
                
                if (shelfTitle.lowercase().contains("latest release") || shelfTitle.lowercase().contains("featured release")) {
                    val firstItem = carousel.contents?.getOrNull(0)?.musicTwoRowItemRenderer
                    if (firstItem != null) {
                        val title = firstItem.title?.runs?.getOrNull(0)?.text ?: "Unknown"
                        val browseId = firstItem.navigationEndpoint?.browseEndpoint?.browseId ?: ""
                        val thumb = firstItem.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
                            ?: firstItem.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
                        val subtitle = firstItem.subtitle?.runs?.getOrNull(0)?.text
                        latestRelease = ArtistCollection(browseId, title, subtitle, thumb, "Latest Release")
                    }
                } else {
                    carousel.contents?.forEach { item ->
                        item.musicTwoRowItemRenderer?.let { renderer ->
                            val title = renderer.title?.runs?.getOrNull(0)?.text ?: "Unknown"
                            val browseId = renderer.navigationEndpoint?.browseEndpoint?.browseId ?: ""
                            val thumb = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
                                ?: renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
                            val subtitle = renderer.subtitle?.runs?.getOrNull(0)?.text
                            albums.add(ArtistCollection(browseId, title, subtitle, thumb, shelfTitle))
                        }
                    }
                }
            }
            
            // Bio / Description
            section.musicDescriptionShelfRenderer?.let { desc ->
                bio = desc.description?.runs?.getOrNull(0)?.text ?: ""
            }
        }

        // De-duplicate: Ensure latestRelease is excluded from the albums/singles lists
        var finalLatestRelease = latestRelease
        val finalAlbums = albums.distinctBy { it.id }.toMutableList()
        if (finalLatestRelease == null && finalAlbums.isNotEmpty()) {
            val featured = finalAlbums.firstOrNull()
            if (featured != null) {
                finalLatestRelease = featured.copy(type = "Featured Release")
                finalAlbums.remove(featured)
            }
        } else if (finalLatestRelease != null) {
            finalAlbums.removeAll { it.id == finalLatestRelease.id }
        }

        _uiState.value = ArtistUiState(
            artistName = artistName,
            headerImageUrl = headerImageUrl,
            bio = bio,
            topTracks = topTracks.distinctBy { it.id },
            albums = finalAlbums,
            latestRelease = finalLatestRelease,
            subscribers = subscribers,
            views = views,
            isLoading = false
        )
    }

    private fun parseTrackRendererFromArtist(
        renderer: com.example.ecodot.data.remote.MusicResponsiveListItemRenderer, 
        artistName: String
    ): Track {
        val videoId = renderer.playlistItemData?.videoId 
            ?: renderer.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId
            ?: renderer.navigationEndpoint?.watchEndpoint?.videoId
            ?: ""
            
        val titleFlex = renderer.flexColumns?.getOrNull(0)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
        val title = titleFlex?.getOrNull(0)?.text ?: "Unknown"
        
        // Find album name or plays in subtitle
        val subtitleFlex = renderer.flexColumns?.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
        var album = subtitleFlex?.joinToString("") { it.text }?.takeIf { it.isNotBlank() } ?: artistName
        // Clean up redundant dividers like " • " if needed, though they usually just have plays or artist name.
        if (album.startsWith(artistName) && album.contains(" • ")) {
            album = album.substringAfter(" • ")
        }

        val rawThumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
        val highResThumbnail = com.example.ecodot.data.remote.YouTubeParser.getHighResUrl(rawThumbnail)
        
        return Track(
            id = videoId,
            title = title,
            artist = artistName,
            album = album,
            duration = 0,
            path = "https://www.youtube.com/watch?v=$videoId",
            albumArtUri = highResThumbnail,
            isYouTube = true
        )
    }
}

