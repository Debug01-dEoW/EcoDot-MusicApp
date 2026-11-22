package com.example.ecodot.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecodot.data.local.entities.RecentSearchItem
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.local.prefs.UserPreferencesManager
import com.example.ecodot.data.repository.MusicRepository
import com.example.ecodot.domain.model.Album
import com.example.ecodot.domain.model.Artist
import com.example.ecodot.domain.usecase.SearchMusicUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class SearchViewModel(
    private val searchMusicUseCase: SearchMusicUseCase,
    private val repository: MusicRepository,
    private val prefsManager: UserPreferencesManager
) : ViewModel() {

    private val TAG = "SearchViewModel"

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _searchAlbums = MutableStateFlow<List<Album>>(emptyList())
    val searchAlbums: StateFlow<List<Album>> = _searchAlbums.asStateFlow()

    private val _searchArtists = MutableStateFlow<List<Artist>>(emptyList())
    val searchArtists: StateFlow<List<Artist>> = _searchArtists.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    val searchHistory: StateFlow<List<RecentSearchItem>> = repository.recentSearches
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var searchJob: Job? = null
    private var suggestionsJob: Job? = null
    private var lastSearchQuery: String? = null
    private var lastSearchType: String? = null

    fun searchYouTubeCategorized(query: String, type: String? = null) {
        if (query.isBlank()) {
            clearSearch()
            return
        }

        if (lastSearchQuery == query && lastSearchType == type &&
            (_searchResults.value.isNotEmpty() || _searchAlbums.value.isNotEmpty() || _searchArtists.value.isNotEmpty() || _isSearching.value)) {
            return
        }
        lastSearchQuery = query
        lastSearchType = type

        viewModelScope.launch(Dispatchers.IO) {
            repository.addRecentSearch(query, "Query", null)
        }

        _isSearching.value = true
        _searchResults.value = emptyList()
        _searchAlbums.value = emptyList()
        _searchArtists.value = emptyList()
        _searchError.value = null
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                val localDeferred = async(Dispatchers.IO) {
                    repository.searchTracksLocally("%$query%")
                }
                val remoteDeferred = async(Dispatchers.IO) {
                    searchMusicUseCase(query, type)
                }

                val localMatches = localDeferred.await()
                val remoteResult = remoteDeferred.await()

                if (coroutineContext[Job] == searchJob) {
                    val remoteResults = remoteResult.getOrDefault(com.example.ecodot.data.remote.SearchResults())
                    val localIds = localMatches.map { it.id }.toSet()
                    val uniqueRemoteSongs = remoteResults.songs.filter { it.id !in localIds }
                    val combinedSongs = (localMatches + uniqueRemoteSongs).distinctBy { it.id }

                    _searchResults.value = combinedSongs
                    _searchAlbums.value = remoteResults.albums
                    _searchArtists.value = remoteResults.artists
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(TAG, "Search failed for query: $query", e)
                    if (coroutineContext[Job] == searchJob) {
                        _searchError.value = "Network error or timeout. Please try again."
                    }
                }
            } finally {
                if (coroutineContext[Job] == searchJob) {
                    _isSearching.value = false
                }
            }
        }
    }

    fun updateSuggestions(query: String) {
        if (query.length < 2) {
            _searchSuggestions.value = emptyList()
            return
        }
        suggestionsJob?.cancel()
        suggestionsJob = viewModelScope.launch {
            try {
                val suggestions = repository.getSearchSuggestions(query)
                if (coroutineContext[Job] == suggestionsJob) {
                    _searchSuggestions.value = suggestions
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(TAG, "Suggestions fetch failed for query: $query", e)
                }
            }
        }
    }

    fun clearSearch() {
        lastSearchQuery = null
        lastSearchType = null
        _searchResults.value = emptyList()
        _searchAlbums.value = emptyList()
        _searchArtists.value = emptyList()
        _isSearching.value = false
    }

    fun selectSearchResult(query: String, type: String, itemId: String?, imageUrl: String?, subtitle: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRecentSearch(query = query, type = type, itemId = itemId, imageUrl = imageUrl, subtitle = subtitle)
        }
    }

    fun removeSearchHistory(item: RecentSearchItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeRecentSearch(item)
        }
    }

    fun clearAllSearchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearRecentSearches()
        }
    }
}
