package com.example.ecodot.data.remote

import com.example.ecodot.data.repository.extractFirstMoodArtworkUrl
import com.example.ecodot.ui.components.extractYouTubeLinkOrNull
import com.example.ecodot.ui.components.generateSearchGradientColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFeatureTest {

    @Test
    fun `extractYouTubeLinkOrNull handles video playlist and channel urls`() {
        assertEquals(
            "video" to "dQw4w9WgXcQ",
            extractYouTubeLinkOrNull("https://youtu.be/dQw4w9WgXcQ")
        )
        assertEquals(
            "video" to "dQw4w9WgXcQ",
            extractYouTubeLinkOrNull("https://music.youtube.com/watch?v=dQw4w9WgXcQ&list=RDAMVM")
        )
        assertEquals(
            "playlist" to "PL12345_abc",
            extractYouTubeLinkOrNull("https://www.youtube.com/playlist?list=PL12345_abc")
        )
        assertEquals(
            "artist" to "UC_x5XG1OV2P6uZZ5FSM9Ttw",
            extractYouTubeLinkOrNull("https://music.youtube.com/channel/UC_x5XG1OV2P6uZZ5FSM9Ttw")
        )
        assertNull(extractYouTubeLinkOrNull("Arijit Singh Kesariya"))
    }

    @Test
    fun `generateSearchGradientColors is deterministic per title`() {
        val first = generateSearchGradientColors("Chill")
        val second = generateSearchGradientColors("Chill")
        assertEquals(2, first.size)
        assertEquals(first, second)
    }

    @Test
    fun `SearchResults defaults and extended playlist and podcast collections work cleanly`() {
        val empty = SearchResults()
        assertTrue(empty.playlists.isEmpty())
        assertTrue(empty.featuredPlaylists.isEmpty())
        assertTrue(empty.podcasts.isEmpty())

        val pod = HomeMediaItem(
            id = "MPSPPL123",
            title = "Tech Music Podcast",
            subtitle = "Host",
            thumbnailUrl = "https://example.com/art.jpg",
            type = MediaType.PLAYLIST
        )
        val populated = SearchResults(podcasts = listOf(pod))
        assertNotNull(populated.podcasts.singleOrNull())
        assertEquals("MPSPPL123", populated.podcasts.first().id)
    }

    @Test
    fun `extractFirstMoodArtworkUrl scans sectionListRenderer window without full JSON parse`() {
        val sampleJson = """
            {"responseContext":{"visitorData":"abc"},"contents":{"singleColumnBrowseResultsRenderer":{"tabs":[{"tabRenderer":{"content":{"sectionListRenderer":{"contents":[{"musicCarouselShelfRenderer":{"contents":[{"musicTwoRowItemRenderer":{"thumbnailRenderer":{"musicThumbnailRenderer":{"thumbnail":{"thumbnails":[{"url":"https://lh3.googleusercontent.com/sample-cover=w226-h226-l90-rj"}]}}}}}]}}]}}}}]}}}
        """.trimIndent()
        assertEquals(
            "https://lh3.googleusercontent.com/sample-cover=w544-h544-l90-rj",
            extractFirstMoodArtworkUrl(sampleJson)
        )
    }

    @Test
    fun `getHighResUrl upgrades square w120 s60 wide and ytimg urls while fallback chains cleanly`() {
        assertEquals(
            "https://lh3.googleusercontent.com/abc=w1080-h1080-l90-rj",
            YouTubeParser.getHighResUrl("https://lh3.googleusercontent.com/abc=w120-h120-l90-rj")
        )
        assertEquals(
            "https://yt3.googleusercontent.com/art=s1080",
            YouTubeParser.getHighResUrl("https://yt3.googleusercontent.com/art=s60")
        )
        assertEquals(
            "https://lh3.googleusercontent.com/vid=w1280-h720-l90-rj",
            YouTubeParser.getHighResUrl("https://lh3.googleusercontent.com/vid=w320-h180-l90-rj")
        )
        assertEquals(
            "https://i.ytimg.com/vi/abc123/maxresdefault.jpg",
            YouTubeParser.getHighResUrl("https://i.ytimg.com/vi/abc123/mqdefault.jpg")
        )
        assertEquals(
            "https://i.ytimg.com/vi/abc123/sddefault.jpg",
            YouTubeParser.getFallbackThumbUrl("https://i.ytimg.com/vi/abc123/maxresdefault.jpg")
        )
        assertEquals(
            "https://i.ytimg.com/vi/abc123/hqdefault.jpg",
            YouTubeParser.getFallbackThumbUrl("https://i.ytimg.com/vi/abc123/sddefault.jpg")
        )
    }
}
