package com.devshiv.adikala.api

import com.devshiv.adikala.auth.ShopifyAuthManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://www.adikala.in/"
    private const val ACCOUNT_URL = "https://shopify.com/26970751043/account/customer/api/2025-10/"

    private const val STOREFRONT_PUBLIC_TOKEN = "a8931a13b14b1fdc80e95151a87c6655"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val storefrontTokenInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-Shopify-Storefront-Access-Token", STOREFRONT_PUBLIC_TOKEN)
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }

    private val storefrontOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(storefrontTokenInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val accountOkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val storefrontApi: ShopifyStorefrontApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(storefrontOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ShopifyStorefrontApi::class.java)
    }

    val accountApi: ShopifyAccountApi by lazy {
        Retrofit.Builder()
            .baseUrl(ACCOUNT_URL)
            .client(accountOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ShopifyAccountApi::class.java)
    }
}