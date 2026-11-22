package com.example.ecodot.data.remote

import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.IOException

class NewPipeDownloader(private val client: OkHttpClient) : Downloader() {

    @Throws(IOException::class)
    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val okHttpRequestBuilder = okhttp3.Request.Builder()
            .url(url)

        headers.forEach { (headerName, headerValues) ->
            headerValues.forEach { headerValue ->
                okHttpRequestBuilder.addHeader(headerName, headerValue)
            }
        }

        if (httpMethod.equals("POST", ignoreCase = true)) {
            val requestBody = (dataToSend ?: ByteArray(0)).toRequestBody(null)
            okHttpRequestBuilder.post(requestBody)
        } else if (httpMethod.equals("HEAD", ignoreCase = true)) {
            okHttpRequestBuilder.head()
        } else {
            okHttpRequestBuilder.get()
        }

        val okHttpResponse = client.newCall(okHttpRequestBuilder.build()).execute()
        val responseBody = okHttpResponse.body?.string() ?: ""

        val responseHeaders = mutableMapOf<String, List<String>>()
        okHttpResponse.headers.names().forEach { name ->
            responseHeaders[name] = okHttpResponse.headers.values(name)
        }

        return Response(
            okHttpResponse.code,
            okHttpResponse.message,
            responseHeaders,
            responseBody,
            okHttpResponse.request.url.toString()
        )
    }
}
