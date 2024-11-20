package com.example.dreamcatcher.network

import retrofit2.http.Body
import retrofit2.http.POST

data class ImageRequest(val prompt: String, val n: Int = 1, val size: String = "512x512")
data class ImageResponse(val data: List<ImageData>)
data class ImageData(val url: String)

interface OpenAIImageAPI{
    @POST("v1/images/generations")
    suspend fun generateImage(
        @Body request: ImageRequest
    ): ImageResponse
}

