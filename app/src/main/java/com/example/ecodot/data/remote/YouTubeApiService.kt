package com.example.ecodot.data.remote

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface YouTubeApiService {

    companion object {
        const val API_KEY = "AIzaSyAO_f_I7S9p7j8pAbWp7vF13q9p7j8pAbW"
    }

    @POST("search?key=$API_KEY")
    suspend fun search(
        @Body body: RequestBody,
        @HeaderMap headers: Map<String, String>
    ): YouTubeSearchResponse

    @POST("player?key=$API_KEY")
    suspend fun getPlayer(
        @Body body: RequestBody,
        @HeaderMap headers: Map<String, String>
    ): YouTubePlayerResponse

    @POST("browse?key=$API_KEY")
    suspend fun browse(
        @Body body: RequestBody,
        @HeaderMap headers: Map<String, String>
    ): YouTubeBrowseResponse

    @POST("next?key=$API_KEY")
    suspend fun getNext(
        @Body body: RequestBody,
        @HeaderMap headers: Map<String, String>
    ): okhttp3.ResponseBody
}
