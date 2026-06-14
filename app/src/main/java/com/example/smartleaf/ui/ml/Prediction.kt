package com.example.smartleaf.ui.ml

/**
 * Prediction - Represents ML model prediction result
 * Contains disease/plant name, confidence score, and index
 */
data class Prediction(
    val label: String,
    val confidence: Float,
    val index: Int
) : Comparable<Prediction> {
    override fun compareTo(other: Prediction): Int {
        return other.confidence.compareTo(this.confidence)
    }

    override fun toString(): String {
        return "$label: ${"%.2f".format(confidence * 100)}%"
    }
}
