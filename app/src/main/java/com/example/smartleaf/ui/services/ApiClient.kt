package com.example.smartleaf.ui.services

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Emulator -> 10.0.2.2 ; Real device -> Mac/PC LAN IP
    private const val BASE_URL = "http://192.168.1.6:8888/smartleaf_api/"

    @Volatile private var token: String? = null
    fun setToken(t: String?) { token = t }

    // Adds JWT token to each request automatically
    private val auth = Interceptor { chain ->
        val b = chain.request().newBuilder()
        token?.let { b.header("Authorization", "Bearer $it") }
        chain.proceed(b.build())
    }

    // Logs all network calls
    private val log = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttp client with auth + logging
    private val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(log)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(http)
            .build()
    }

    // ✅ Ready-to-use service
    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }
}