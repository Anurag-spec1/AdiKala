package com.devshiv.adikala.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://www.adikala.in/"

    // CHANGED: was "https://account.adikala.in/" — that domain is only for
    // OAuth/login pages. Actual Customer Account API data queries go to
    // shopify.com/{shop_id}/account/customer/api/{version}/
    private const val ACCOUNT_URL = "https://shopify.com/26970751043/account/customer/api/2025-10/"

    // CHANGED: added — Shopify rejects Storefront API calls without this header.
    private const val STOREFRONT_PUBLIC_TOKEN = "a8931a13b14b1fdc80e95151a87c6655"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // set to NONE before release build
    }

    // CHANGED: added — attaches the required Storefront token to every request
    // made through storefrontApi. Your interface has no @Header param, so it
    // must be injected here.
    private val storefrontTokenInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-Shopify-Storefront-Access-Token", STOREFRONT_PUBLIC_TOKEN)
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }

    // Storefront client — has the token interceptor
    private val storefrontOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(storefrontTokenInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Account client — no Storefront token needed here; the customer's own
    // access token is passed per-request via the @Header param already in
    // ShopifyAccountApi.executeCustomerGraphQL(accessToken, request)
    private val accountOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

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