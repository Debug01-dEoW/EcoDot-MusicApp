package com.example.ecodot.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YouTubeSearchResponse(
    val contents: Contents?,
    val responseContext: ResponseContext?
)

@JsonClass(generateAdapter = true)
data class ResponseContext(
    val visitorData: String?
)

@JsonClass(generateAdapter = true)
data class Contents(
    val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer?
)

@JsonClass(generateAdapter = true)
data class TabbedSearchResultsRenderer(
    val tabs: List<Tab>?
)

@JsonClass(generateAdapter = true)
data class Tab(
    val tabRenderer: TabRenderer?
)

@JsonClass(generateAdapter = true)
data class TabRenderer(
    val content: SectionList?
)

@JsonClass(generateAdapter = true)
data class SectionList(
    val sectionListRenderer: SectionListRenderer?
)

@JsonClass(generateAdapter = true)
data class SectionListRenderer(
    val contents: List<SectionContent>?
)

@JsonClass(generateAdapter = true)
data class SectionContent(
    val musicShelfRenderer: MusicShelfRenderer? = null,
    val musicPlaylistShelfRenderer: MusicPlaylistShelfRenderer? = null,
    val musicCarouselShelfRenderer: MusicCarouselShelfRenderer? = null,
    val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer? = null,
    val musicCardShelfRenderer: MusicCardShelfRenderer? = null,
    val itemSectionRenderer: ItemSectionRenderer? = null
)

@JsonClass(generateAdapter = true)
data class MusicCardShelfRenderer(
    val title: TextContent?,
    val subtitle: TextContent?,
    val thumbnail: ThumbnailContent?,
    val buttons: List<MusicCardButton>?,
    val navigationEndpoint: NavigationEndpoint? = null
)

@JsonClass(generateAdapter = true)
data class MusicCardButton(
    val musicPlayButtonRenderer: MusicPlayButtonRenderer?,
    val buttonRenderer: ButtonRenderer? = null
)

@JsonClass(generateAdapter = true)
data class ButtonRenderer(
    val navigationEndpoint: NavigationEndpoint? = null
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfRenderer(
    val header: CarouselHeader?,
    val contents: List<CarouselItem>?
)

@JsonClass(generateAdapter = true)
data class CarouselHeader(
    val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfBasicHeaderRenderer(
    val title: TextContent?
)

@JsonClass(generateAdapter = true)
data class CarouselItem(
    val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicTwoRowItemRenderer(
    val title: TextContent?,
    val subtitle: TextContent?,
    @Json(name = "thumbnailRenderer") val thumbnailRenderer: ThumbnailContent?,
    val thumbnail: ThumbnailContent?,
    val navigationEndpoint: NavigationEndpoint?
)

@JsonClass(generateAdapter = true)
data class MusicShelfRenderer(
    val title: TextContent? = null,
    val contents: List<MusicItem>?
)

@JsonClass(generateAdapter = true)
data class MusicDescriptionShelfRenderer(
    val description: TextContent?
)

@JsonClass(generateAdapter = true)
data class ItemSectionRenderer(
    val contents: List<MusicItem>?
)

@JsonClass(generateAdapter = true)
data class MusicItem(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemRenderer(
    val flexColumns: List<FlexColumn>?,
    val thumbnail: ThumbnailContent?,
    val playlistItemData: PlaylistItemData?,
    val overlay: MusicItemThumbnailOverlay?,
    val navigationEndpoint: NavigationEndpoint?,
    val badges: List<BadgeRenderer>? = null
)

@JsonClass(generateAdapter = true)
data class MusicItemThumbnailOverlay(
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicItemThumbnailOverlayRenderer(
    val content: MusicItemThumbnailOverlayContent?
)

@JsonClass(generateAdapter = true)
data class MusicItemThumbnailOverlayContent(
    val musicPlayButtonRenderer: MusicPlayButtonRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicPlayButtonRenderer(
    val playNavigationEndpoint: NavigationEndpoint?
)

@JsonClass(generateAdapter = true)
data class FlexColumn(
    val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemFlexColumnRenderer(
    val text: TextContent?
)

@JsonClass(generateAdapter = true)
data class TextContent(
    val runs: List<Run>?
)

@JsonClass(generateAdapter = true)
data class Run(
    val text: String,
    val navigationEndpoint: NavigationEndpoint? = null
)

@JsonClass(generateAdapter = true)
data class NavigationEndpoint(
    val browseEndpoint: BrowseEndpoint? = null,
    val watchEndpoint: WatchEndpoint? = null
)

@JsonClass(generateAdapter = true)
data class BrowseEndpoint(
    val browseId: String?
)

@JsonClass(generateAdapter = true)
data class WatchEndpoint(
    val videoId: String?
)

@JsonClass(generateAdapter = true)
data class YouTubeBrowseResponse(
    val contents: BrowseContents?,
    val header: BrowseHeader?
)

@JsonClass(generateAdapter = true)
data class BrowseContents(
    val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer? = null,
    val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer? = null,
    val sectionListRenderer: SectionListRenderer? = null
)

@JsonClass(generateAdapter = true)
data class TwoColumnBrowseResultsRenderer(
    val tabs: List<Tab>? = null
)

@JsonClass(generateAdapter = true)
data class SingleColumnBrowseResultsRenderer(
    val tabs: List<Tab>?
)

@JsonClass(generateAdapter = true)
data class BrowseHeader(
    val musicVisualHeaderRenderer: MusicVisualHeaderRenderer? = null,
    val musicDetailHeaderRenderer: MusicDetailHeaderRenderer? = null,
    val musicImmersiveHeaderRenderer: MusicImmersiveHeaderRenderer? = null
)

@JsonClass(generateAdapter = true)
data class MusicImmersiveHeaderRenderer(
    val title: TextContent?,
    val thumbnail: ThumbnailContent?,
    val description: TextContent? = null
)

@JsonClass(generateAdapter = true)
data class MusicVisualHeaderRenderer(
    val title: TextContent?,
    val thumbnail: ThumbnailContent?,
    val foregroundThumbnail: ThumbnailContent?
)

@JsonClass(generateAdapter = true)
data class MusicDetailHeaderRenderer(
    val title: TextContent?,
    val subtitle: TextContent?,
    val thumbnail: ThumbnailContent?
)

@JsonClass(generateAdapter = true)
data class ThumbnailContent(
    val musicThumbnailRenderer: MusicThumbnailRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRenderer(
    val thumbnail: Thumbnails?
)

@JsonClass(generateAdapter = true)
data class Thumbnails(
    val thumbnails: List<Thumbnail>?
)

@JsonClass(generateAdapter = true)
data class Thumbnail(
    val url: String
)

@JsonClass(generateAdapter = true)
data class PlaylistItemData(
    val videoId: String
)

@JsonClass(generateAdapter = true)
data class YouTubePlayerResponse(
    val streamingData: StreamingData?,
    val videoDetails: VideoDetails?,
    val playabilityStatus: PlayabilityStatus?,
    val responseContext: ResponseContext?
)

@JsonClass(generateAdapter = true)
data class PlayabilityStatus(
    val status: String?,
    val reason: String?
)

@JsonClass(generateAdapter = true)
data class VideoDetails(

    val videoId: String?,
    val title: String?,
    val lengthSeconds: String?,
    val author: String?,
    val thumbnail: Thumbnails?
)


@JsonClass(generateAdapter = true)
data class StreamingData(
    val adaptiveFormats: List<AdaptiveFormat>?,
    val formats: List<AdaptiveFormat>? = null,
    val hlsManifestUrl: String? = null,
    val dashManifestUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class AdaptiveFormat(
    val url: String?,
    val signatureCipher: String?,
    val mimeType: String?,
    val bitrate: Long?,
    val averageBitrate: Long?,
    val contentLength: Long?,
    val itag: Int? = null,
    val width: Int? = null,
    val height: Int? = null
)

/** Parsed rich metadata for the Info Sheet */
data class VideoInfo(
    val videoId: String,
    val title: String,
    val artist: String,
    val album: String,
    val viewCount: String,         // e.g. "186,591,170"
    val likeCount: String,         // e.g. "1,358,153 likes"
    val description: String,       // Full YouTube description
    val mimeType: String,          // e.g. "audio/webm; codecs=\"opus\""
    val codec: String,             // e.g. "opus"
    val bitrate: String,           // e.g. "150,931 bps"
    val itag: String,              // e.g. "251"
    val durationSeconds: String    // e.g. "3:19"
)

@JsonClass(generateAdapter = true)
data class MusicPlaylistShelfRenderer(
    val playlistId: String? = null,
    val contents: List<MusicItem>? = null
)

enum class MediaType {
    TRACK, PLAYLIST, ALBUM, ARTIST, UNKNOWN
}

data class HomeMediaItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val thumbnailUrl: String,
    val type: MediaType,
    val isExplicit: Boolean = false
)

data class HomeSection(
    val title: String,
    val items: List<HomeMediaItem>
)

@JsonClass(generateAdapter = true)
data class BadgeRenderer(
    val musicInlineExplicitBadgeRenderer: MusicInlineExplicitBadgeRenderer? = null,
    val musicInlineBadgeRenderer: MusicInlineBadgeRenderer? = null
)

@JsonClass(generateAdapter = true)
data class MusicInlineExplicitBadgeRenderer(
    val icon: BadgeIcon? = null
)

@JsonClass(generateAdapter = true)
data class MusicInlineBadgeRenderer(
    val icon: BadgeIcon? = null
)

@JsonClass(generateAdapter = true)
data class BadgeIcon(
    val iconType: String? = null
)

