package com.example.ecodot.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.local.entities.UserProfile
import com.example.ecodot.data.remote.HomeMediaItem
import com.example.ecodot.data.remote.HomeSection
import com.example.ecodot.data.remote.MediaType
import com.example.ecodot.data.repository.MusicRepository
import com.example.ecodot.domain.model.Album
import com.example.ecodot.domain.model.Artist
import com.example.ecodot.domain.usecase.GetHomeFeedUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val getHomeFeedUseCase: GetHomeFeedUseCase,
    private val repository: MusicRepository
) : ViewModel() {

    private val TAG = "HomeViewModel"

    private val _homeSections = MutableStateFlow<List<HomeSection>>(emptyList())
    val homeSections: StateFlow<List<HomeSection>> = _homeSections.asStateFlow()

    private val _moodGreeting = MutableStateFlow("Time-of-Day Mix")
    val moodGreeting: StateFlow<String> = _moodGreeting.asStateFlow()

    private val _moodRecommendedTracks = MutableStateFlow<List<Track>>(emptyList())
    val moodRecommendedTracks: StateFlow<List<Track>> = _moodRecommendedTracks.asStateFlow()

    private val _suggestedTracks = MutableStateFlow<List<Track>>(getDefaultRealTracks())
    val suggestedTracks: StateFlow<List<Track>> = _suggestedTracks.asStateFlow()

    private val _historyRecommendations = MutableStateFlow<List<Track>>(emptyList())
    val historyRecommendations: StateFlow<List<Track>> = _historyRecommendations.asStateFlow()

    val allAlbums: StateFlow<List<Album>> = combine(
        repository.allTracks,
        repository.getAllPlaylistTrackIds()
    ) { tracks, playlistTrackIds ->
        val savedTrackIds = playlistTrackIds.toSet()
        val savedTracks = tracks.filter { it.id in savedTrackIds }
        savedTracks.filter { it.album.isNotBlank() }.groupBy { it.album }.map { (title, albumTracks) ->
            Album(
                id = albumTracks.first().albumId.takeIf { !it.isNullOrBlank() } ?: title,
                title = title,
                artist = albumTracks.first().artist,
                artUri = albumTracks.first().albumArtUri,
                tracks = albumTracks
            )
        }.sortedBy { it.title }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allArtists: StateFlow<List<Artist>> = combine(
        repository.allTracks,
        repository.getAllPlaylistTrackIds()
    ) { tracks, playlistTrackIds ->
        val savedTrackIds = playlistTrackIds.toSet()
        val savedTracks = tracks.filter { it.id in savedTrackIds }
        savedTracks.filter { it.artist.isNotBlank() }.groupBy { it.artist }.map { (name, artistTracks) ->
            val firstTrack = artistTracks.first()
            Artist(
                id = firstTrack.artistId.takeIf { !it.isNullOrBlank() } ?: name,
                name = name,
                imageUrl = firstTrack.albumArtUri
            )
        }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentlyPlayed: StateFlow<List<Track>> = repository.recentlyPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val onRepeatTracks: StateFlow<List<Track>> = repository.getOnRepeatTracks(
        System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
    ).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        fetchHomeContent()
        fetchStartupRecommendations()
    }

    fun refresh() {
        fetchHomeContent()
    }

    private fun fetchHomeContent() {
        viewModelScope.launch {
            val result = getHomeFeedUseCase()
            val sections = result.getOrDefault(emptyList())
            if (result.isSuccess && sections.isNotEmpty()) {
                _homeSections.value = sections
            } else {
                Log.d(TAG, "Home feed empty or failed, using real verified YouTube Music sections")
                _homeSections.value = getDefaultRealHomeSections()
            }
            fetchMoodRecommendations()
        }
    }

    private fun fetchStartupRecommendations() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tracks = repository.mostPlayed.first()
                if (tracks.isNotEmpty()) {
                    fetchSuggestions(tracks.first())
                } else {
                    val fallback = repository.searchYouTube("Top Global Hits 2026")
                    if (fallback.isNotEmpty()) {
                        _suggestedTracks.value = fallback
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch initial suggestions", e)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val history = repository.recentlyPlayed.first()
                if (history.isNotEmpty()) {
                    fetchTasteMatchedRecommendations(history.take(5))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch history recommendations", e)
            }
        }
    }

    fun fetchSuggestions(track: Track) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val related = repository.getYouTubeRelatedTracks(track.id)
                if (related.isNotEmpty()) {
                    _suggestedTracks.value = related
                } else {
                    val query = "${track.title} ${track.artist}"
                    val fallback = repository.searchYouTube(query).filter { it.id != track.id }
                    _suggestedTracks.value = fallback
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch suggestions for ${track.title}", e)
            }
        }
    }

    private fun fetchTasteMatchedRecommendations(history: List<Track>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val artists = history.map { it.artist }.distinct().take(3)
                val recommendations = mutableListOf<Track>()
                for (artist in artists) {
                    val results = repository.searchYouTube(artist).filter { track ->
                        history.none { it.id == track.id }
                    }
                    recommendations.addAll(results.take(3))
                }
                _historyRecommendations.value = recommendations.distinctBy { it.id }.shuffled()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch taste recommendations", e)
            }
        }
    }

    fun fetchMoodRecommendations() {
        viewModelScope.launch {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val mood = when (hour) {
                in 5..10 -> "Chill"      // 5 AM to 10:59 AM
                in 11..16 -> "Upbeat"    // 11 AM to 4:59 PM
                in 17..20 -> "Focus"     // 5 PM to 8:59 PM
                else -> "Chill"          // 9 PM to 4:59 AM
            }
            val greeting = when (hour) {
                in 5..10 -> "Good Morning • Chill Mix"
                in 11..16 -> "Good Afternoon • Upbeat Hype"
                in 17..20 -> "Good Evening • Focus Beats"
                else -> "Good Night • Midnight Chill"
            }
            _moodGreeting.value = greeting

            val keywords = when (mood) {
                "Chill" -> listOf("chill", "lofi", "lo-fi", "sleep", "relax", "acoustic", "calm", "slow", "ambient", "soft", "jazz", "piano", "soul")
                "Focus" -> listOf("focus", "study", "beats", "instrumental", "classical", "synthwave", "coding", "ambient", "calm", "relax")
                "Upbeat" -> listOf("upbeat", "dance", "rock", "pop", "workout", "energetic", "gym", "hype", "electronic", "electro", "party", "rap", "hip hop", "techno")
                else -> emptyList()
            }

            try {
                val localTracks = repository.allTracks.first()
                val localMatches = localTracks.filter { track ->
                    keywords.any { kw ->
                        track.title.contains(kw, ignoreCase = true) ||
                        track.artist.contains(kw, ignoreCase = true) ||
                        track.album.contains(kw, ignoreCase = true) ||
                        (track.genre != null && track.genre.contains(kw, ignoreCase = true))
                    }
                }
                if (localMatches.size >= 5) {
                    _moodRecommendedTracks.value = localMatches.shuffled().take(10)
                } else {
                    val ytQuery = when (mood) {
                        "Chill" -> "lofi chill beats"
                        "Focus" -> "synthwave focus study"
                        "Upbeat" -> "upbeat workout energetic pop"
                        else -> "chill music"
                    }
                    val ytResults = repository.searchYouTube(ytQuery)
                    val combined = (localMatches + ytResults).distinctBy { it.id }.shuffled()
                    _moodRecommendedTracks.value = combined.take(10)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch mood recommendations", e)
            }
        }
    }

    companion object {
        fun getDefaultRealTracks(): List<Track> = listOf(
            Track(
                id = "kPa7bsKwL-8",
                title = "Die With A Smile",
                artist = "Lady Gaga & Bruno Mars",
                album = "Single",
                albumArtUri = "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg",
                duration = 251,
                path = "https://www.youtube.com/watch?v=kPa7bsKwL-8",
                isYouTube = true
            ),
            Track(
                id = "34Na4j8AVgA",
                title = "Starboy (feat. Daft Punk)",
                artist = "The Weeknd",
                album = "Starboy",
                albumArtUri = "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg",
                duration = 230,
                path = "https://www.youtube.com/watch?v=34Na4j8AVgA",
                isYouTube = true,
                isExplicit = true
            ),
            Track(
                id = "yzTuBuRdAyA",
                title = "The Hills",
                artist = "The Weeknd",
                album = "Beauty Behind the Madness",
                albumArtUri = "https://i.ytimg.com/vi/yzTuBuRdAyA/hqdefault.jpg",
                duration = 242,
                path = "https://www.youtube.com/watch?v=yzTuBuRdAyA",
                isYouTube = true,
                isExplicit = true
            ),
            Track(
                id = "KRaWnd3LJfs",
                title = "Payphone (feat. Wiz Khalifa)",
                artist = "Maroon 5",
                album = "Overexposed",
                albumArtUri = "https://i.ytimg.com/vi/KRaWnd3LJfs/hqdefault.jpg",
                duration = 231,
                path = "https://www.youtube.com/watch?v=KRaWnd3LJfs",
                isYouTube = true,
                isExplicit = true
            ),
            Track(
                id = "4NRXx6U8ABQ",
                title = "Blinding Lights",
                artist = "The Weeknd",
                album = "After Hours",
                albumArtUri = "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
                duration = 200,
                path = "https://www.youtube.com/watch?v=4NRXx6U8ABQ",
                isYouTube = true
            ),
            Track(
                id = "ApXoWvfEYVU",
                title = "Sunflower",
                artist = "Post Malone, Swae Lee",
                album = "Spider-Man: Into the Spider-Verse",
                albumArtUri = "https://i.ytimg.com/vi/ApXoWvfEYVU/hqdefault.jpg",
                duration = 158,
                path = "https://www.youtube.com/watch?v=ApXoWvfEYVU",
                isYouTube = true
            ),
            Track(
                id = "VAdGW7QDJUI",
                title = "Chaleya",
                artist = "Arijit Singh, Shilpa Rao",
                album = "Jawan",
                albumArtUri = "https://i.ytimg.com/vi/VAdGW7QDJUI/hqdefault.jpg",
                duration = 200,
                path = "https://www.youtube.com/watch?v=VAdGW7QDJUI",
                isYouTube = true
            ),
            Track(
                id = "IJq0cvO40Ass",
                title = "Tum Hi Ho",
                artist = "Arijit Singh",
                album = "Aashiqui 2",
                albumArtUri = "https://i.ytimg.com/vi/IJq0cvO40Ass/hqdefault.jpg",
                duration = 262,
                path = "https://www.youtube.com/watch?v=IJq0cvO40Ass",
                isYouTube = true
            ),
            Track(
                id = "mH_LFkW_oao",
                title = "Lover",
                artist = "Diljit Dosanjh",
                album = "MoonChild Era",
                albumArtUri = "https://i.ytimg.com/vi/mH_LFkW_oao/hqdefault.jpg",
                duration = 188,
                path = "https://www.youtube.com/watch?v=mH_LFkW_oao",
                isYouTube = true
            ),
            Track(
                id = "RgKAFK5djSk",
                title = "See You Again",
                artist = "Wiz Khalifa ft. Charlie Puth",
                album = "Furious 7",
                albumArtUri = "https://i.ytimg.com/vi/RgKAFK5djSk/hqdefault.jpg",
                duration = 229,
                path = "https://www.youtube.com/watch?v=RgKAFK5djSk",
                isYouTube = true
            ),
            Track(
                id = "gset79KMmt0",
                title = "Snowman",
                artist = "Sia",
                album = "Everyday Is Christmas",
                albumArtUri = "https://i.ytimg.com/vi/gset79KMmt0/hqdefault.jpg",
                duration = 165,
                path = "https://www.youtube.com/watch?v=gset79KMmt0",
                isYouTube = true
            ),
            Track(
                id = "lY2yjAdbvdQ",
                title = "Treat You Better",
                artist = "Shawn Mendes",
                album = "Illuminate",
                albumArtUri = "https://i.ytimg.com/vi/lY2yjAdbvdQ/hqdefault.jpg",
                duration = 187,
                path = "https://www.youtube.com/watch?v=lY2yjAdbvdQ",
                isYouTube = true
            )
        )

        fun getDefaultRealHomeSections(): List<HomeSection> = listOf(
            HomeSection(
                title = "Hindi Hits",
                strapline = "TODAY'S BIGGEST BOLLYWOOD SONGS",
                items = listOf(
                    HomeMediaItem("VAdGW7QDJUI", "Chaleya", "Arijit Singh, Shilpa Rao", "https://i.ytimg.com/vi/VAdGW7QDJUI/hqdefault.jpg", MediaType.TRACK),
                    HomeMediaItem("IJq0cvO40Ass", "Tum Hi Ho", "Arijit Singh", "https://i.ytimg.com/vi/IJq0cvO40Ass/hqdefault.jpg", MediaType.TRACK),
                    HomeMediaItem("JFcgOboQZ08", "Kesariya", "Arijit Singh, Pritam", "https://i.ytimg.com/vi/JFcgOboQZ08/hqdefault.jpg", MediaType.TRACK),
                    HomeMediaItem("ilNt2bikxDI", "Raataan Lambiyan", "Jubin Nautiyal, Asees Kaur", "https://i.ytimg.com/vi/ilNt2bikxDI/hqdefault.jpg", MediaType.TRACK),
                    HomeMediaItem("mH_LFkW_oao", "Lover", "Diljit Dosanjh", "https://i.ytimg.com/vi/mH_LFkW_oao/hqdefault.jpg", MediaType.TRACK)
                )
            ),
            HomeSection(
                title = "Global Top Hits",
                strapline = "WORLDWIDE TRENDING",
                items = listOf(
                    HomeMediaItem("kPa7bsKwL-8", "Die With A Smile", "Lady Gaga & Bruno Mars", "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg", MediaType.TRACK),
                    HomeMediaItem("34Na4j8AVgA", "Starboy", "The Weeknd ft. Daft Punk", "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg", MediaType.TRACK, isExplicit = true),
                    HomeMediaItem("yzTuBuRdAyA", "The Hills", "The Weeknd", "https://i.ytimg.com/vi/yzTuBuRdAyA/hqdefault.jpg", MediaType.TRACK, isExplicit = true),
                    HomeMediaItem("4NRXx6U8ABQ", "Blinding Lights", "The Weeknd", "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg", MediaType.TRACK),
                    HomeMediaItem("ApXoWvfEYVU", "Sunflower", "Post Malone, Swae Lee", "https://i.ytimg.com/vi/ApXoWvfEYVU/hqdefault.jpg", MediaType.TRACK),
                    HomeMediaItem("KRaWnd3LJfs", "Payphone", "Maroon 5 ft. Wiz Khalifa", "https://i.ytimg.com/vi/KRaWnd3LJfs/hqdefault.jpg", MediaType.TRACK, isExplicit = true)
                )
            )
        )
    }
}
