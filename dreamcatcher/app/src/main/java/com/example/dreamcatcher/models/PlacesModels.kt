package com.example.dreamcatcher.models

import com.google.gson.annotations.SerializedName

data class PlacesResponse(
    val results: List<PlaceResult>,
    val status: String
)

data class PlaceResult(
    val name: String,
    val vicinity: String,
    val geometry: Geometry,
    val photos: List<Photo>?
)

data class Photo(
    @SerializedName("photo_reference") val photoReference: String,
    val height: Int? = null,
    val width: Int? = null
)

data class TherapyCenter(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val photoReference: String?
)

