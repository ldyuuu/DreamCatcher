package com.example.dreamcatcher.network

import com.example.dreamcatcher.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL_HUGGINGFACE = "https://api-inference.huggingface.co/"
    private const val BASE_URL_OPENAI = "https://api.openai.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun createOkHttpClient(apiKey: String): OkHttpClient {
        val headerInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", apiKey)
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60,TimeUnit.SECONDS)
            .readTimeout(60,TimeUnit.SECONDS)
            .writeTimeout(60,TimeUnit.SECONDS)
            .build()
    }


    val huggingFaceAPI: HuggingFaceAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_HUGGINGFACE)
            .client(createOkHttpClient("Bearer ${BuildConfig.HUGGINGFACE_API_KEY}"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HuggingFaceAPI::class.java)
    }

    val openAIImageAPI: OpenAIImageAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_OPENAI)
            .client(createOkHttpClient("Bearer ${BuildConfig.OPENAI_API_KEY}"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIImageAPI::class.java)
    }

}
