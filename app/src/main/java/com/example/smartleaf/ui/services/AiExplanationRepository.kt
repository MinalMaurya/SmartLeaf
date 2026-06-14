package com.example.smartleaf.ui.services

import java.util.Locale
import com.example.smartleaf.services.GeminiClient
import com.example.smartleaf.services.GeminiContent
import com.example.smartleaf.services.GeminiPart
import com.example.smartleaf.services.GeminiRequest

object AiExplanationRepository {

    suspend fun getExplanation(
        className: String,
        confidence: Float,
        languageCode: String = Locale.getDefault().language  // "en", "hi", "mr", ...
    ): String {

        val systemPrompt = """
            You are SmartLeaf AI, an expert crop helper for small farmers.

            OUTPUT RULES (VERY IMPORTANT):
            - Output must be PLAIN TEXT only.
            - Do NOT use Markdown or formatting symbols:
              no *, **, #, _, ~, <, >, backticks, or HTML tags.
            - Write short, clear sentences.
            - Use these headings exactly, in this order,
              each followed by bullet points that start with "• ":

              Is the plant healthy or diseased:
              Likely disease name:
              Main causes:
              Prevention tips:
              Treatment steps:
              Safe home remedies:

            - Each section should have 1–3 bullet points.
            - Total 6–10 bullet points.
            - Headings themselves should NOT be bullets.
            - Write everything in the language with code "$languageCode".
        """.trimIndent()

        val userPrompt = """
            Plant type (class label): $className
            Model confidence (0–1): ${"%.2f".format(confidence)}

            Based on this crop type and typical leaf problems,
            explain to a small farmer what might be happening
            and what they should do.

            Follow the output rules strictly.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = systemPrompt),
                        GeminiPart(text = userPrompt)
                    )
                )
            )
        )

        val response = GeminiClient.service.generateContent(request)
        return response.primaryText()?.trim()
            ?: "Explanation is not available right now."
    }
}