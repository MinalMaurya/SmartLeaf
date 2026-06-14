package com.example.smartleaf.ui.utils

import com.example.smartleaf.ui.models.FaqItem
import java.util.Locale

object FaqMatcher {

    fun findBestMatch(userQuery: String, faqs: List<FaqItem>): FaqItem? {
        val qTokens = tokenize(userQuery)
        if (qTokens.isEmpty()) return null

        var best: FaqItem? = null
        var bestScore = 0

        for (item in faqs) {
            val itemTokens = tokenize(item.question) + item.tags.flatMap { tokenize(it) }
            val score = overlapScore(qTokens, itemTokens)
            if (score > bestScore) {
                bestScore = score
                best = item
            }
        }

        // minimum threshold to avoid nonsense matches
        return if (bestScore >= 2) best else null
    }

    private fun overlapScore(a: List<String>, b: List<String>): Int {
        val setB = b.toSet()
        var score = 0
        for (t in a) if (setB.contains(t)) score++
        return score
    }

    private fun tokenize(text: String): List<String> {
        val cleaned = text
            .lowercase(Locale.getDefault())
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleaned.isBlank()) return emptyList()
        return cleaned.split(" ").filter { it.length >= 2 }
    }
}