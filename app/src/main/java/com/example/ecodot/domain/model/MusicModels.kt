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
    val bands: List<Int> // 5 bands in mB (-1500..1500)
)

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
