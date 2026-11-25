package com.example.ecodot.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeParserAndModelsTest {

    @Test
    fun homeSectionAndMediaItemPreserveMetadataDefaultsAndOverrides() {
        val videoItem = HomeMediaItem(
            id = "kPa7bsKwL-8",
            title = "Die With A Smile",
            subtitle = "Lady Gaga • 1.2B views",
            thumbnailUrl = "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg",
            type = MediaType.TRACK,
            isExplicit = false,
            isLive = false,
            views = "1.2B views",
            aspectRatio = 16f / 9f,
            artistId = "UC123"
        )
        val section = HomeSection(
            title = "Video charts",
            items = listOf(videoItem),
            strapline = "WHAT IS BEST CHOICE TODAY",
            thumbnailUrl = "https://lh3.googleusercontent.com/avatar=w120-h120",
            moreBrowseId = "FEmusic_charts"
        )

        assertEquals(MediaType.TRACK, section.items.first().type)
        assertEquals(16f / 9f, section.items.first().aspectRatio, 0.01f)
        assertEquals("FEmusic_charts", section.moreBrowseId)
        assertEquals("UC123", section.items.first().artistId)
    }

    @Test
    fun parseMetadataFromRunsExtractsArtistAndFiltersNoise() {
        val runs = listOf(
            Run(text = "Video"),
            Run(text = " • "),
            Run(text = "The Weeknd"),
            Run(text = " • "),
            Run(text = "Starboy"),
            Run(text = " • "),
            Run(text = "2.6B views")
        )

        assertTrue(YouTubeParser.isVideoFromRuns(runs))
        val (artist, album) = YouTubeParser.parseMetadataFromRuns(runs)
        assertEquals("The Weeknd", artist)
        assertEquals("Starboy", album)

        val audioRuns = listOf(
            Run(text = "Song"),
            Run(text = " • "),
            Run(text = "Arijit Singh")
        )
        assertFalse(YouTubeParser.isVideoFromRuns(audioRuns))
    }

    @Test
    fun realHomeDataProvidesNonEmptyFallbacksForChartsMoodsAndVideos() {
        assertTrue(RealHomeData.getRealQuickPicks().size >= 8)
        assertTrue(RealHomeData.getRealMusicVideos().size >= 6)
        assertTrue(RealHomeData.getRealCountryCharts("India").isNotEmpty())
        assertTrue(RealHomeData.getRealTopArtists("United States").isNotEmpty())
        assertTrue(RealHomeData.getRealGenres().size >= 9)
        assertTrue(RealHomeData.getRealPodcasts().isNotEmpty())
    }
}
