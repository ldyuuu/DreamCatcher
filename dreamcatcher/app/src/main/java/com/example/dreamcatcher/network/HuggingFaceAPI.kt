package com.example.dreamcatcher.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class HuggingFaceRequest(val inputs: String)
data class HuggingFaceResponse(val label: String, val score: Float)

interface HuggingFaceAPI {
    @POST("models/j-hartmann/emotion-english-distilroberta-base")
    suspend fun analyzeEmotion(
        @Header ("Authorization") authToken: String,
        @Body request: HuggingFaceRequest
    ): List<List<HuggingFaceResponse>>
}
