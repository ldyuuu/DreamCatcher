package com.example.dreamcatcher.network

import com.example.dreamcatcher.models.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesAPI {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun findPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String?,
        @Query("keyword") keyword: String?,
        @Query("key") apiKey: String
    ): PlacesResponse
}
