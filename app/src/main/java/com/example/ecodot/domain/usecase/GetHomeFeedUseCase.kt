package com.example.ecodot.domain.usecase

import com.example.ecodot.data.remote.HomeSection
import com.example.ecodot.domain.repository.IMusicRepository

class GetHomeFeedUseCase(private val repository: IMusicRepository) {
    suspend operator fun invoke(params: String? = null): Result<List<HomeSection>> {
        return try {
            val sections = repository.getHomeFeed(params)
            Result.success(sections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
