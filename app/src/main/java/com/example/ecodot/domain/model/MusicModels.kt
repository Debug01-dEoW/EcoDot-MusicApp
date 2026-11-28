package com.example.ecodot.domain.model

import com.example.ecodot.data.local.entities.Track

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String?
)

data class Album(
    val id: String?,
    val title: String,
    val artist: String,
    val artUri: String?,
    val tracks: List<Track>
)

data class Genre(
    val name: String,
    val tracks: List<Track>
)

data class EqPreset(
    val name: String,
    val description: String,
    val bands: List<Int>, // bands in mB (-1500..1500)
    val isCustom: Boolean = false
)

/**
 * Serializes band index -> millibel map cleanly as a stable sorted CSV (`0:300,1:-100,...`).
 */
fun encodeEqBands(bands: Map<Int, Int>): String {
    return bands.entries
        .sortedBy { it.key }
        .joinToString(",") { "${it.key}:${it.value}" }
}

/**
 * Parses a previously encoded EQ band string, falling back to flat (0 mB) for missing bands.
 */
fun decodeEqBands(raw: String?, bandCount: Int = 5): Map<Int, Int> {
    val result = (0 until bandCount).associateWith { 0 }.toMutableMap()
    if (raw.isNullOrBlank()) return result
    raw.split(",").forEach { entry ->
        val parts = entry.split(":")
        if (parts.size == 2) {
            val idx = parts[0].trim().toIntOrNull()
            val level = parts[1].trim().toIntOrNull()
            if (idx != null && level != null && idx >= 0) {
                result[idx] = level
            }
        }
    }
    return result
}

/**
 * Encodes user-saved EQ presets cleanly using `|` and `=` delimiters to avoid JSON reflection edge cases.
 */
fun encodeCustomEqPresets(presets: List<EqPreset>): String {
    return presets.joinToString("\n") { preset ->
        val safeName = preset.name.replace("\n", " ").replace("|", "/").trim()
        val bandsStr = preset.bands.joinToString(",")
        "$safeName|$bandsStr"
    }
}

/**
 * Decodes user-saved EQ presets previously written by [encodeCustomEqPresets].
 */
fun decodeCustomEqPresets(raw: String?): List<EqPreset> {
    if (raw.isNullOrBlank()) return emptyList()
    return raw.lineSequence()
        .mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@mapNotNull null
            val sep = trimmed.indexOf('|')
            if (sep <= 0) return@mapNotNull null
            val name = trimmed.substring(0, sep).trim()
            val bands = trimmed.substring(sep + 1)
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
            if (name.isEmpty() || bands.isEmpty()) return@mapNotNull null
            EqPreset(
                name = name,
                description = "Custom user preset",
                bands = bands,
                isCustom = true
            )
        }
        .toList()
}

/**
 * Formats an integer Hz center frequency cleanly (e.g. `60 Hz`, `3.6 kHz`).
 */
fun formatEqFrequency(freqHz: Int): String {
    return when {
        freqHz >= 1000 -> {
            val k = freqHz / 1000.0
            if (k % 1.0 == 0.0) "${k.toInt()} kHz" else "${String.format(java.util.Locale.US, "%.1f", k)} kHz"
        }
        else -> "$freqHz Hz"
    }
}

val DEFAULT_EQ_PRESETS = listOf(
    EqPreset("Flat", "Linear flat response", listOf(0, 0, 0, 0, 0)),
    EqPreset("Bass Boost", "Punchy low-end sub", listOf(800, 500, 100, 0, 0)),
    EqPreset("Bass & Treble", "V-shaped dynamic punch", listOf(700, 300, -100, 400, 700)),
    EqPreset("Vocal Boost", "Crisp lyrics & speech", listOf(-200, 200, 700, 500, 100)),
    EqPreset("Rock", "Electric guitars & live drive", listOf(600, 300, -100, 400, 600)),
    EqPreset("Pop", "Radio vocals & polish", listOf(-100, 200, 500, 300, -200)),
    EqPreset("Electronic", "Synth bass & energetic highs", listOf(800, 400, 0, 300, 600)),
    EqPreset("Classical", "Airy hall acoustic room", listOf(400, 200, -200, 300, 400)),
    EqPreset("Acoustic", "Warm natural unplugged tone", listOf(400, 200, 100, 300, 400)),
    EqPreset("Hip Hop", "Deep 808 sub & snare snap", listOf(800, 500, 0, 200, 400))
)
