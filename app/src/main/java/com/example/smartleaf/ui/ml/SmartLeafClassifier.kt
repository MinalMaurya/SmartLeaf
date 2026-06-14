package com.example.smartleaf.ui.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.MappedByteBuffer

/**
 * SmartLeafClassifier - ML model for plant type/disease detection.
 * Uses TensorFlow Lite for inference.
 */
class SmartLeafClassifier(context: Context) {

    companion object {
        private const val TAG = "SmartLeafClassifier"

        // 👇 These MUST match the files in app/src/main/assets
        private const val MODEL_PATH = "smartleaf_fp16.tflite"
        private const val LABELS_FILE = "labels.txt"

        private const val IMAGE_SIZE = 224   // 224x224
    }

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    init {
        try {
            // Load model
            val modelBuffer: MappedByteBuffer = FileUtil.loadMappedFile(context, MODEL_PATH)
            interpreter = Interpreter(modelBuffer)
            Log.d(TAG, "Model loaded successfully from $MODEL_PATH")

            // Load labels
            labels = loadLabels(context)
            if (labels.isEmpty()) {
                throw IllegalStateException("labels.txt is empty or missing")
            }
            Log.d(TAG, "Labels loaded: ${labels.size} labels")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing classifier: ${e.message}", e)
            throw RuntimeException("Failed to initialize ML classifier", e)
        }
    }

    /**
     * Load labels from assets/labels.txt (one label per line).
     */
    private fun loadLabels(context: Context): List<String> {
        return try {
            val list = mutableListOf<String>()
            context.assets.open(LABELS_FILE).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) list.add(trimmed)
                }
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error loading labels: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Predict top K results.
     * Builds a FLOAT32 [1,224,224,3] tensor and applies MobileNetV3-like
     * preprocessing (scale 0–255 → 0–1 → -1..1).
     */
    fun predictTopK(bitmap: Bitmap, k: Int = 3): List<Prediction> {
        try {
            val tflite = interpreter ?: throw IllegalStateException("Interpreter not initialized")
            if (labels.isEmpty()) throw IllegalStateException("Labels not loaded")

            // 1) Resize bitmap to 224x224
            val resized = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true)

            // 2) Build input tensor: [1,224,224,3] FLOAT32
            val input = Array(1) {
                Array(IMAGE_SIZE) {
                    Array(IMAGE_SIZE) {
                        FloatArray(3)
                    }
                }
            }

            for (y in 0 until IMAGE_SIZE) {
                for (x in 0 until IMAGE_SIZE) {
                    val pixel = resized.getPixel(x, y)

                    // 0..255
                    var r = Color.red(pixel) / 255f
                    var g = Color.green(pixel) / 255f
                    var b = Color.blue(pixel) / 255f

                    // MobileNetV3 preprocess_input: scale to [-1, 1]
                    r = r * 2f - 1f
                    g = g * 2f - 1f
                    b = b * 2f - 1f

                    input[0][y][x][0] = r
                    input[0][y][x][1] = g
                    input[0][y][x][2] = b
                }
            }

            // 3) Output tensor: [1, num_labels]
            val output = Array(1) { FloatArray(labels.size) }

            // 4) Run inference
            tflite.run(input, output)

            // 5) Convert to Prediction list
            val predictions = output[0]
            val results = mutableListOf<Prediction>()

            predictions.forEachIndexed { index, confidence ->
                if (index < labels.size) {
                    results.add(
                        Prediction(
                            label = labels[index],
                            confidence = confidence.coerceIn(0f, 1f),
                            index = index
                        )
                    )
                }
            }

            // 6) Sort by confidence and return top K
            return results.sorted().take(k)

        } catch (e: Exception) {
            Log.e(TAG, "Error classifying image: ${e.message}", e)
            throw RuntimeException("Classification failed: ${e.message}", e)
        }
    }

    /**
     * Simple helper if you only need the best label.
     */
    fun classifyImage(bitmap: Bitmap): String {
        return try {
            val topK = predictTopK(bitmap, k = 1)
            if (topK.isNotEmpty()) topK[0].label else "Unknown"
        } catch (e: Exception) {
            Log.e(TAG, "Error in classifyImage: ${e.message}", e)
            "Error"
        }
    }

    fun close() {
        try {
            interpreter?.close()
            interpreter = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing interpreter: ${e.message}", e)
        }
    }
}