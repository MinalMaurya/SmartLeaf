package com.example.smartleaf.ui.domain

import com.example.smartleaf.ui.ml.Prediction
import com.example.smartleaf.ui.vision.ImageAnalyzer

/**
 * SymptomRuleEngine - Generates diagnosis based on ML predictions and visual analysis
 */
object SymptomRuleEngine {

    data class Diagnosis(
        val primary: String,
        val bullets: List<String>,
        val alternatives: String?
    )

    /**
     * Generate comprehensive diagnosis
     */
    fun generate(
        topK: List<Prediction>,
        img: ImageAnalyzer.ColorSignals,
        hostHint: String?,
        organHint: String?
    ): Diagnosis {
        if (topK.isEmpty()) {
            return Diagnosis(
                primary = "Unable to analyze image",
                bullets = listOf("Please provide a clearer image of the leaf"),
                alternatives = null
            )
        }

        val primary = topK[0]
        val secondary = if (topK.size > 1) topK[1] else null
        val tertiary = if (topK.size > 2) topK[2] else null

        // Generate primary diagnosis
        val primaryDiagnosis = buildString {
            append("${primary.label}: ${("%.1f".format(primary.confidence * 100))}% confidence")
        }

        // Generate detailed bullets
        val bullets = mutableListOf<String>()

        bullets.add("Primary detection: ${primary.label}")

        if (primary.confidence > 0.8) {
            bullets.add("High confidence in diagnosis")
        } else if (primary.confidence > 0.6) {
            bullets.add("Moderate confidence - consider second opinion")
        } else {
            bullets.add("Low confidence - image quality may affect accuracy")
        }

        // Add visual analysis
        if (img.hasYellowSpots) {
            bullets.add("Yellow discoloration detected")
        }
        if (img.hasBrownSpots) {
            bullets.add("Brown spots or lesions detected")
        }
        if (img.hasWhitePatches) {
            bullets.add("White patches or powder detected")
        }

        bullets.add("Saturation level: ${"%.1f".format(img.avgSaturation * 100)}%")
        bullets.add("Brightness level: ${"%.1f".format(img.avgBrightness * 100)}%")

        // Generate alternatives
        val alternativesText = if (secondary != null) {
            "Alternative: ${secondary.label} (${("%.1f".format(secondary.confidence * 100))}%)" +
                    (if (tertiary != null) ", ${tertiary.label} (${("%.1f".format(tertiary.confidence * 100))}%)" else "")
        } else {
            null
        }

        return Diagnosis(
            primary = primaryDiagnosis,
            bullets = bullets,
            alternatives = alternativesText
        )
    }
}
