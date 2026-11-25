package com.example.ecodot.data.remote

import com.example.ecodot.data.local.prefs.SponsorBlockCategory
import com.example.ecodot.domain.model.SponsorSegment
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import org.json.JSONArray
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class SponsorBlockResponseItem(
    @Json(name = "category") val category: String,
    @Json(name = "segment") val segment: List<Float>
)

interface SponsorBlockApi {
    @GET("api/skipSegments")
    suspend fun getSkipSegments(
        @Query("videoID") videoId: String,
        @Query("categories") categories: String = "[\"sponsor\",\"intro\",\"outro\",\"music_offtopic\",\"preview\"]"
    ): Response<List<SponsorBlockResponseItem>>
}

class SponsorBlockService(private val client: OkHttpClient) {

    private val api: SponsorBlockApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://sponsor.ajay.app/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SponsorBlockApi::class.java)
    }

    suspend fun getSegments(
        videoId: String,
        categories: Set<String> = SponsorBlockCategory.defaultKeys
    ): List<SponsorSegment> {
        if (categories.isEmpty()) return emptyList()
        val categoriesJson = JSONArray(categories.toList()).toString()
        return try {
            val response = api.getSkipSegments(videoId, categoriesJson)
            if (response.isSuccessful) {
                response.body()?.mapNotNull { item ->
                    val start = (item.segment.getOrNull(0) ?: return@mapNotNull null) * 1000f
                    val end = (item.segment.getOrNull(1) ?: return@mapNotNull null) * 1000f
                    SponsorSegment(
                        category = item.category,
                        start = start.toLong(),
                        end = end.toLong()
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
