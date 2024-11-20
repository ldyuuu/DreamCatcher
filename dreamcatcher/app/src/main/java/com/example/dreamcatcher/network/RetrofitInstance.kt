package com.example.dreamcatcher.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://api-inference.huggingface.co/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply{
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: HuggingFaceAPI by lazy {
        retrofit.create(HuggingFaceAPI::class.java)
    }
}
