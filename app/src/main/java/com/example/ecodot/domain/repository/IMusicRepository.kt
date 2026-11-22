package com.example.ecodot.domain.repository

import com.example.ecodot.data.local.entities.*
import com.example.ecodot.data.local.prefs.AudioQuality
import com.example.ecodot.data.local.prefs.VideoQuality
import com.example.ecodot.data.remote.HomeSection
import com.example.ecodot.data.remote.SearchResults
import com.example.ecodot.data.remote.YouTubeBrowseResponse
import kotlinx.coroutines.flow.Flow

interface IMusicRepository {
    val allTracks: Flow<List<Track>>
    val recentlyPlayed: Flow<List<Track>>
    val mostPlayed: Flow<List<Track>>
    val userProfile: Flow<UserProfile?>
    val allPlaylists: Flow<List<Playlist>>
    val playbackHistory: Flow<List<PlaybackHistoryWithTrack>>
    val recentSearches: Flow<List<RecentSearchItem>>
    val followedArtists: Flow<List<FollowedArtist>>

    suspend fun getHomeFeed(params: String? = null): List<HomeSection>
    suspend fun searchYouTube(query: String): List<Track>
    suspend fun searchYouTubeCategorized(query: String, type: String? = null): SearchResults
    suspend fun getYouTubeStreamUrl(videoId: String, quality: AudioQuality, videoQuality: VideoQuality = VideoQuality.NORMAL, isVideo: Boolean = false): String?
    suspend fun getArtistDetails(artistId: String): YouTubeBrowseResponse?
    suspend fun getAlbumJsonPair(albumId: String): Pair<String, String>?
    suspend fun followArtist(artistId: String, name: String, imageUrl: String?)
    suspend fun unfollowArtist(artistId: String)
}
