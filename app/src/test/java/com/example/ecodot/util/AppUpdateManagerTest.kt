package com.example.ecodot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun parseMarkdownSections_handlesStructuredSimpMusicReleaseNotes() {
        val markdown = """
            Full changelogs here: https://github.com/Debug01-dEoW/EcoDot-MusicApp/releases
            Read user-guide: https://github.com/Debug01-dEoW/EcoDot-MusicApp#readme

            ## New
            - **High AAC quality**: Stream 256kbps AAC audio streams.
            - Crossfade filter audio processor.

            ### Improved
            * AutoMix beat-aligned fade curves.

            ## Fixed
            1. Initial scroll position in UpdateDialog.

            ## What's Changed
            * feat: add HIGH_AAC by @Debug01-dEoW in https://github.com/Debug01-dEoW/EcoDot-MusicApp/pull/1

            ## New Contributors
            * @Debug01-dEoW made their first contribution
        """.trimIndent()

        val sections = AppUpdateManager.parseMarkdownSections(markdown)

        assertEquals(
            listOf("New", "Improved", "Fixed", "What's Changed", "New Contributors"),
            sections.map { it.title }
        )
        assertEquals(2, sections[0].items.size)
        assertEquals("**High AAC quality**: Stream 256kbps AAC audio streams.", sections[0].items[0])
        assertEquals("Initial scroll position in UpdateDialog.", sections[2].items[0])
    }

    @Test
    fun parseMarkdownSections_handlesLongSubheadersAndVersionBannerWithoutHashBullets() {
        val markdown = """
            ## 🎵 EcoDot v1.2 — Liquid Glass Edition
            ---
            #### 🧭 Authentic SimpMusic Collapsible Navigation & MiniPlayer
            **Exact Pill Shapes**: Capsule navigation controls.
            - Smooth frosted backdrop blur.
            ---
            #### 🎨 Dynamic Material Palette & Player Upgrades
            * Per-track vibrant background gradients.
        """.trimIndent()

        val sections = AppUpdateManager.parseMarkdownSections(markdown)

        assertEquals(
            listOf(
                "🧭 Authentic SimpMusic Collapsible Navigation & MiniPlayer",
                "🎨 Dynamic Material Palette & Player Upgrades"
            ),
            sections.map { it.title }
        )
        assertTrue(sections.none { it.title.equals("What's Changed", ignoreCase = true) })
        assertTrue(sections.flatMap { it.items }.none { it.startsWith("#") || it == "---" })
        assertEquals(
            listOf("**Exact Pill Shapes**: Capsule navigation controls.", "Smooth frosted backdrop blur."),
            sections[0].items
        )
    }

    @Test
    fun parseMarkdownSections_mergesDuplicateSectionTitlesInsteadOfDuplicatingHeaders() {
        val markdown = """
            Unheaded note before sections.

            ## What's Changed
            - Second changelog item.
        """.trimIndent()

        val sections = AppUpdateManager.parseMarkdownSections(markdown)

        assertEquals(1, sections.size)
        assertEquals("What's Changed", sections[0].title)
        assertEquals(
            listOf("Unheaded note before sections.", "Second changelog item."),
            sections[0].items
        )
        assertFalse(sections.any { it.items.isEmpty() })
    }
}
