package com.example.smartleaf.ui.vision

import android.graphics.Bitmap
import android.graphics.Color

/**
 * ImageAnalyzer - Analyzes leaf images for visual symptoms
 * Extracts features like color, texture, and shape
 */
object ImageAnalyzer {

    data class ColorSignals(
        val hasYellowSpots: Boolean,
        val hasBrownSpots: Boolean,
        val hasWhitePatches: Boolean,
        val avgSaturation: Float,
        val avgBrightness: Float
    )

    /**
     * Analyze image for visual symptoms
     */
    fun analyze(bitmap: Bitmap): ColorSignals {
        val width = bitmap.width
        val height = bitmap.height

        var yellowCount = 0
        var brownCount = 0
        var whiteCount = 0
        var totalSaturation = 0f
        var totalBrightness = 0f

        // Sample pixels every 5 pixels to avoid processing entire image
        for (y in 0 until height step 5) {
            for (x in 0 until width step 5) {
                val pixel = bitmap.getPixel(x, y)

                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                // Check for yellow spots (high R+G, low B)
                if (red > 200 && green > 150 && blue < 100) {
                    yellowCount++
                }

                // Check for brown spots (R > G > B, all moderate)
                if (red > green && green > blue && red < 200) {
                    brownCount++
                }

                // Check for white patches (all high)
                if (red > 220 && green > 220 && blue > 220) {
                    whiteCount++
                }

                // Calculate saturation
                val max = maxOf(red, green, blue).toFloat()
                val min = minOf(red, green, blue).toFloat()
                val saturation = if (max > 0) {
                    (max - min) / max
                } else {
                    0f
                }
                totalSaturation += saturation

                // Calculate brightness - Cast Double to Float
                totalBrightness += ((0.299 * red + 0.587 * green + 0.114 * blue) / 255).toFloat()
            }
        }

        val sampleCount = (width / 5) * (height / 5)

        return ColorSignals(
            hasYellowSpots = yellowCount > sampleCount * 0.05,
            hasBrownSpots = brownCount > sampleCount * 0.1,
            hasWhitePatches = whiteCount > sampleCount * 0.02,
            avgSaturation = if (sampleCount > 0) totalSaturation / sampleCount else 0f,
            avgBrightness = if (sampleCount > 0) totalBrightness / sampleCount else 0f
        )
    }
}
