package com.example.ecodot.util

import androidx.compose.ui.graphics.Color
import com.google.gson.Gson

data class CustomCover(
    val startColor: String = "#FF1DB954",
    val endColor: String = "#FF070708",
    val isPrivate: Boolean = false,
    val tags: List<String> = emptyList()
)

object PlaylistCustomizer {
    private val gson = Gson()

    fun parseCustomCover(json: String?): CustomCover? {
        if (json.isNullOrEmpty() || !json.trim().startsWith("{")) return null
        return try {
            gson.fromJson(json, CustomCover::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun serializeCustomCover(cover: CustomCover): String {
        return gson.toJson(cover)
    }

    fun parseColorSafe(hex: String, fallback: Color): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            fallback
        }
    }
}
