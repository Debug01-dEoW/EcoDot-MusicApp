package com.example.ecodot.util

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class CustomCover(
    val startColor: String = "#FF1DB954",
    val endColor: String = "#FF070708",
    val isPrivate: Boolean = false,
    val tags: List<String> = emptyList()
)

object PlaylistCustomizer {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val coverAdapter = moshi.adapter(CustomCover::class.java)

    fun parseCustomCover(json: String?): CustomCover? {
        if (json.isNullOrEmpty() || !json.trim().startsWith("{")) return null
        return runCatching { coverAdapter.fromJson(json) }.getOrNull()
    }

    fun serializeCustomCover(cover: CustomCover): String {
        return coverAdapter.toJson(cover)
    }

    fun parseColorSafe(hex: String, fallback: Color): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            fallback
        }
    }
}
