package com.example.ecodot.domain.usecase

import com.example.ecodot.data.remote.SearchResults
import com.example.ecodot.domain.repository.IMusicRepository

class SearchMusicUseCase(private val repository: IMusicRepository) {
    suspend operator fun invoke(
        query: String,
        type: String? = null
    ): Result<SearchResults> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return Result.success(SearchResults())
        }
        return try {
            val results = repository.searchYouTubeCategorized(trimmed, type)
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
