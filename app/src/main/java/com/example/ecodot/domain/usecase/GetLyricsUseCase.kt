package com.example.ecodot.domain.usecase

import com.example.ecodot.data.remote.LrcLibResponse
import com.example.ecodot.data.remote.LyricsRepository

class GetLyricsUseCase(private val lyricsRepository: LyricsRepository) {
    suspend operator fun invoke(
        videoId: String?,
        artist: String,
        title: String,
        duration: Long
    ): LrcLibResponse? {
        return lyricsRepository.fetchLyrics(
            videoId = videoId,
            artist = artist,
            title = title,
            duration = duration
        )
    }
}
