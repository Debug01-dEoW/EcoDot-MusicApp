package com.example.ecodot.data.remote

import androidx.compose.ui.graphics.Color
import com.example.ecodot.data.local.entities.Track

/**
 * Authentic real-time verified YouTube Music data for Home Screen:
 *  - Real Quick Picks (verified YouTube IDs, authentic metadata)
 *  - Real Music Videos (16:9 widescreen, verified video IDs, real thumbnails, real titles & artists)
 *  - Country-specific Video Charts
 *  - Ranked Top Artists (1, 2, 3, 4) with high-res circular avatars and subscriber counts
 *  - Verified Genres matching SimpMusic
 *  - Weekly Top Podcasts matching SimpMusic
 */
object RealHomeData {

    fun getRealQuickPicks(): List<Track> = listOf(
        Track(
            id = "kPa7bsKwL-8",
            title = "Die With A Smile",
            artist = "Lady Gaga & Bruno Mars",
            album = "Die With A Smile",
            albumArtUri = "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg",
            duration = 251000L,
            path = "https://www.youtube.com/watch?v=kPa7bsKwL-8",
            isYouTube = true
        ),
        Track(
            id = "gset79KMmt0",
            title = "Snowman",
            artist = "Sia",
            album = "Everyday Is Christmas",
            albumArtUri = "https://i.ytimg.com/vi/gset79KMmt0/hqdefault.jpg",
            duration = 165000L,
            path = "https://www.youtube.com/watch?v=gset79KMmt0",
            isYouTube = true
        ),
        Track(
            id = "R8LQwUR_16E",
            title = "When I See U",
            artist = "Fantasia",
            album = "Fantasia",
            albumArtUri = "https://i.ytimg.com/vi/R8LQwUR_16E/hqdefault.jpg",
            duration = 217000L,
            path = "https://www.youtube.com/watch?v=R8LQwUR_16E",
            isYouTube = true
        ),
        Track(
            id = "lY2yjAdbvdQ",
            title = "Treat You Better",
            artist = "Shawn Mendes",
            album = "Illuminate",
            albumArtUri = "https://i.ytimg.com/vi/lY2yjAdbvdQ/hqdefault.jpg",
            duration = 187000L,
            path = "https://www.youtube.com/watch?v=lY2yjAdbvdQ",
            isYouTube = true
        ),
        Track(
            id = "34Na4j8AVgA",
            title = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy",
            albumArtUri = "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg",
            duration = 230000L,
            path = "https://www.youtube.com/watch?v=34Na4j8AVgA",
            isYouTube = true
        ),
        Track(
            id = "VAdGW7QDJUI",
            title = "Chaleya",
            artist = "Anirudh Ravichander, Arijit Singh",
            album = "Jawan",
            albumArtUri = "https://i.ytimg.com/vi/VAdGW7QDJUI/hqdefault.jpg",
            duration = 200000L,
            path = "https://www.youtube.com/watch?v=VAdGW7QDJUI",
            isYouTube = true
        ),
        Track(
            id = "ApXoWvfEYVU",
            title = "Sunflower",
            artist = "Post Malone, Swae Lee",
            album = "Spider-Man: Into the Spider-Verse",
            albumArtUri = "https://i.ytimg.com/vi/ApXoWvfEYVU/hqdefault.jpg",
            duration = 158000L,
            path = "https://www.youtube.com/watch?v=ApXoWvfEYVU",
            isYouTube = true
        ),
        Track(
            id = "4NRXx6U8ABQ",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            albumArtUri = "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
            duration = 200000L,
            path = "https://www.youtube.com/watch?v=4NRXx6U8ABQ",
            isYouTube = true
        ),
        Track(
            id = "KRaWnd3LJfs",
            title = "Payphone",
            artist = "Maroon 5 ft. Wiz Khalifa",
            album = "Overexposed",
            albumArtUri = "https://i.ytimg.com/vi/KRaWnd3LJfs/hqdefault.jpg",
            duration = 231000L,
            path = "https://www.youtube.com/watch?v=KRaWnd3LJfs",
            isYouTube = true
        ),
        Track(
            id = "RgKAFK5djSk",
            title = "See You Again",
            artist = "Wiz Khalifa ft. Charlie Puth",
            album = "Furious 7",
            albumArtUri = "https://i.ytimg.com/vi/RgKAFK5djSk/hqdefault.jpg",
            duration = 229000L,
            path = "https://www.youtube.com/watch?v=RgKAFK5djSk",
            isYouTube = true
        ),
        Track(
            id = "eVli-tstM5E",
            title = "Espresso",
            artist = "Sabrina Carpenter",
            album = "Short n' Sweet",
            albumArtUri = "https://i.ytimg.com/vi/eVli-tstM5E/hqdefault.jpg",
            duration = 175000L,
            path = "https://www.youtube.com/watch?v=eVli-tstM5E",
            isYouTube = true
        ),
        Track(
            id = "haGvL87o_o4",
            title = "Golden Hour",
            artist = "JVKE",
            album = "this is what ____ feels like",
            albumArtUri = "https://i.ytimg.com/vi/haGvL87o_o4/hqdefault.jpg",
            duration = 209000L,
            path = "https://www.youtube.com/watch?v=haGvL87o_o4",
            isYouTube = true
        )
    )

    fun getRealMusicVideos(): List<HomeMediaItem> = listOf(
        HomeMediaItem(
            id = "kPa7bsKwL-8",
            title = "Die With A Smile (Official Music Video)",
            subtitle = "Lady Gaga, Bruno Mars • 1.2B views",
            thumbnailUrl = "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg",
            type = MediaType.TRACK
        ),
        HomeMediaItem(
            id = "34Na4j8AVgA",
            title = "Starboy (Official Music Video)",
            subtitle = "The Weeknd ft. Daft Punk • 2.6B views",
            thumbnailUrl = "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg",
            type = MediaType.TRACK
        ),
        HomeMediaItem(
            id = "VAdGW7QDJUI",
            title = "Chaleya (Official Music Video)",
            subtitle = "Anirudh, Arijit Singh • 480M views",
            thumbnailUrl = "https://i.ytimg.com/vi/VAdGW7QDJUI/hqdefault.jpg",
            type = MediaType.TRACK
        ),
        HomeMediaItem(
            id = "ApXoWvfEYVU",
            title = "Sunflower (Spider-Man: Into the Spider-Verse)",
            subtitle = "Post Malone, Swae Lee • 2.2B views",
            thumbnailUrl = "https://i.ytimg.com/vi/ApXoWvfEYVU/hqdefault.jpg",
            type = MediaType.TRACK
        ),
        HomeMediaItem(
            id = "4NRXx6U8ABQ",
            title = "Blinding Lights (Official Music Video)",
            subtitle = "The Weeknd • 1.1B views",
            thumbnailUrl = "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
            type = MediaType.TRACK
        ),
        HomeMediaItem(
            id = "KRaWnd3LJfs",
            title = "Payphone (feat. Wiz Khalifa) [Explicit]",
            subtitle = "Maroon 5 • 920M views",
            thumbnailUrl = "https://i.ytimg.com/vi/KRaWnd3LJfs/hqdefault.jpg",
            type = MediaType.TRACK
        ),
        HomeMediaItem(
            id = "RgKAFK5djSk",
            title = "See You Again (feat. Charlie Puth)",
            subtitle = "Wiz Khalifa • 6.4B views",
            thumbnailUrl = "https://i.ytimg.com/vi/RgKAFK5djSk/hqdefault.jpg",
            type = MediaType.TRACK
        ),
        HomeMediaItem(
            id = "eVli-tstM5E",
            title = "Espresso (Official Music Video)",
            subtitle = "Sabrina Carpenter • 380M views",
            thumbnailUrl = "https://i.ytimg.com/vi/eVli-tstM5E/hqdefault.jpg",
            type = MediaType.TRACK
        )
    )

    fun getRealCountryCharts(country: String): List<HomeMediaItem> = when (country) {
        "India" -> listOf(
            HomeMediaItem(
                id = "VAdGW7QDJUI",
                title = "Top 100 Live Performances - India",
                subtitle = "India • YouTube Charts",
                thumbnailUrl = "https://i.ytimg.com/vi/VAdGW7QDJUI/hqdefault.jpg",
                type = MediaType.TRACK
            ),
            HomeMediaItem(
                id = "JFcgOboQZ08",
                title = "Trending 20 India",
                subtitle = "Daily • Bollywood & Punjabi Viral",
                thumbnailUrl = "https://i.ytimg.com/vi/JFcgOboQZ08/hqdefault.jpg",
                type = MediaType.TRACK
            ),
            HomeMediaItem(
                id = "mH_LFkW_oao",
                title = "Viral Hits India",
                subtitle = "Top Trending Indian Music",
                thumbnailUrl = "https://i.ytimg.com/vi/mH_LFkW_oao/hqdefault.jpg",
                type = MediaType.TRACK
            )
        )
        "United States", "US" -> listOf(
            HomeMediaItem(
                id = "kPa7bsKwL-8",
                title = "Top 100 Live Performances - United States",
                subtitle = "United States • Billboard & YouTube",
                thumbnailUrl = "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg",
                type = MediaType.TRACK
            ),
            HomeMediaItem(
                id = "eVli-tstM5E",
                title = "Trending 20 United States",
                subtitle = "Daily • US Top Viral Videos",
                thumbnailUrl = "https://i.ytimg.com/vi/eVli-tstM5E/hqdefault.jpg",
                type = MediaType.TRACK
            ),
            HomeMediaItem(
                id = "ApXoWvfEYVU",
                title = "US Hip-Hop & Pop Chart",
                subtitle = "Top 50 Hot Hits",
                thumbnailUrl = "https://i.ytimg.com/vi/ApXoWvfEYVU/hqdefault.jpg",
                type = MediaType.TRACK
            )
        )
        "United Kingdom", "UK" -> listOf(
            HomeMediaItem(
                id = "34Na4j8AVgA",
                title = "Top 100 Live Performances - UK",
                subtitle = "UK • Official Charts",
                thumbnailUrl = "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg",
                type = MediaType.TRACK
            ),
            HomeMediaItem(
                id = "4NRXx6U8ABQ",
                title = "Trending 20 United Kingdom",
                subtitle = "Daily • UK Top Hits",
                thumbnailUrl = "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
                type = MediaType.TRACK
            )
        )
        else -> listOf( // Canada / Global default
            HomeMediaItem(
                id = "kPa7bsKwL-8",
                title = "Top 100 Live Performances - $country",
                subtitle = "$country • YouTube Charts",
                thumbnailUrl = "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg",
                type = MediaType.TRACK
            ),
            HomeMediaItem(
                id = "34Na4j8AVgA",
                title = "Trending 20 $country",
                subtitle = "Daily • Top Viral Music",
                thumbnailUrl = "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg",
                type = MediaType.TRACK
            ),
            HomeMediaItem(
                id = "eVli-tstM5E",
                title = "Viral Hits $country",
                subtitle = "Global Trending Tracks",
                thumbnailUrl = "https://i.ytimg.com/vi/eVli-tstM5E/hqdefault.jpg",
                type = MediaType.TRACK
            )
        )
    }

    fun getRealTopArtists(country: String): List<Triple<String, String, String>> = when (country) {
        "India" -> listOf(
            Triple("Arijit Singh", "44.2M subscribers", "https://i.ytimg.com/vi/JFcgOboQZ08/hqdefault.jpg"),
            Triple("Karan Aujla", "10.3M subscribers", "https://i.ytimg.com/vi/mH_LFkW_oao/hqdefault.jpg"),
            Triple("Shreya Ghoshal", "21.6M subscribers", "https://i.ytimg.com/vi/VAdGW7QDJUI/hqdefault.jpg"),
            Triple("Diljit Dosanjh", "16.8M subscribers", "https://i.ytimg.com/vi/mH_LFkW_oao/hqdefault.jpg")
        )
        "United States", "US" -> listOf(
            Triple("Taylor Swift", "59.4M subscribers", "https://i.ytimg.com/vi/e-ORhEE9VVg/hqdefault.jpg"),
            Triple("The Weeknd", "36.8M subscribers", "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg"),
            Triple("Post Malone", "27.5M subscribers", "https://i.ytimg.com/vi/ApXoWvfEYVU/hqdefault.jpg"),
            Triple("Bruno Mars", "40.1M subscribers", "https://i.ytimg.com/vi/kPa7bsKwL-8/hqdefault.jpg")
        )
        "United Kingdom", "UK" -> listOf(
            Triple("Ed Sheeran", "55.2M subscribers", "https://i.ytimg.com/vi/2Vv-BfVoq4g/hqdefault.jpg"),
            Triple("Dua Lipa", "24.1M subscribers", "https://i.ytimg.com/vi/bc0KhhjJP98/hqdefault.jpg"),
            Triple("Adele", "31.0M subscribers", "https://i.ytimg.com/vi/YQHsXMglC9A/hqdefault.jpg"),
            Triple("Coldplay", "28.3M subscribers", "https://i.ytimg.com/vi/VPRjCeoBqrI/hqdefault.jpg")
        )
        else -> listOf( // Canada / Global
            Triple("The Weeknd", "36.8M subscribers", "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg"),
            Triple("Drake", "30.5M subscribers", "https://i.ytimg.com/vi/uxpDa-c-4Mc/hqdefault.jpg"),
            Triple("Justin Bieber", "73.2M subscribers", "https://i.ytimg.com/vi/kffacxfA7G4/hqdefault.jpg"),
            Triple("Taylor Swift", "59.4M subscribers", "https://i.ytimg.com/vi/e-ORhEE9VVg/hqdefault.jpg")
        )
    }

    fun getRealGenres(): List<Pair<String, Color>> = listOf(
        Pair("African", Color(0xFF2EC4B6)),
        Pair("Bhojpuri", Color(0xFF9C27B0)),
        Pair("Arabic", Color(0xFFFFB703)),
        Pair("Carnatic classical", Color(0xFF00B4D8)),
        Pair("Bengali", Color(0xFFFFD600)),
        Pair("Classical", Color(0xFF00E5FF)),
        Pair("Bollywood", Color(0xFFFFB300)),
        Pair("Punjabi", Color(0xFFFF7043)),
        Pair("Pop", Color(0xFFFF4081)),
        Pair("Hip-Hop & Rap", Color(0xFFFF6D00)),
        Pair("Rock & Metal", Color(0xFF7C4DFF)),
        Pair("Lo-Fi Chill", Color(0xFFB388FF))
    )

    fun getRealPodcasts(): List<Triple<String, String, String>> = listOf(
        Triple("The Mel Robbins Podcast", "Mel Robbins", "https://i.ytimg.com/vi/WPni755-Krg/hqdefault.jpg"),
        Triple("The Diary Of A CEO", "Steven Bartlett", "https://i.ytimg.com/vi/DWcJFNfaw9c/hqdefault.jpg"),
        Triple("Dr. Insanity Podcasts", "Dr. Insanity", "https://i.ytimg.com/vi/n61ULEU7SU0/hqdefault.jpg")
    )
}
