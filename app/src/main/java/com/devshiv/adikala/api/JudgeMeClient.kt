package com.devshiv.adikala.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object JudgeMeClient {

    // Reads go through api.judge.me. (Review creation targets judge.me via an absolute @Url.)
    private const val BASE_URL = "https://api.judge.me/"

    const val SHOP_DOMAIN = "adikala-in.myshopify.com"

    // PUBLIC token ONLY. It is designed for client-side use and can read published reviews.
    // NEVER put the private token in the app — it grants read/write to all data + reviewer PII.
    // (Prefer moving this to BuildConfig: BuildConfig.JUDGEME_PUBLIC_TOKEN.)
    const val API_TOKEN = "JYOoqzrYJPnurdzaSqC7Wi4SFw4"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Use BODY only in debug builds; drop to NONE for release.
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: JudgeMeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JudgeMeApi::class.java)
    }
}