package com.example.dreamcatcher.network

import com.example.dreamcatcher.models.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingAPI {
    @GET("maps/api/geocode/json")
    suspend fun geocode(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GeocodingResponse
}
