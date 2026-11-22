package com.example.ecodot.domain.usecase

import com.example.ecodot.data.remote.SponsorBlockService
import com.example.ecodot.domain.model.SponsorSegment

class GetSponsorSegmentsUseCase(private val sponsorBlockService: SponsorBlockService) {
    suspend operator fun invoke(videoId: String): List<SponsorSegment> {
        if (videoId.isEmpty()) return emptyList()
        return sponsorBlockService.getSegments(videoId)
    }
}
