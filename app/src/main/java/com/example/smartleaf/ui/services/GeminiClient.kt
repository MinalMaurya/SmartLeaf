package com.example.smartleaf.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// ---------- DTOs ----------

data class GeminiPart(
    val text: String? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiCandidate(
    val content: GeminiContent?
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
) {
    fun primaryText(): String? =
        candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.joinToString(separator = "") { it.text.orEmpty() }
}

// ---------- Service ----------
private const val GEMINI_API_KEY = "YOUR_API_KEY"
interface GeminiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Body request: GeminiRequest,
        @Query("key") apiKey: String = GEMINI_API_KEY
    ): GeminiResponse
}

// ---------- Singleton client ----------

object GeminiClient {
    val service: GeminiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiService::class.java)
    }
}