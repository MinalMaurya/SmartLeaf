package com.example.smartleaf.ui.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import com.example.smartleaf.R
import com.example.smartleaf.databinding.ActivityResultBinding
import com.example.smartleaf.ui.models.ScanRequest
import com.example.smartleaf.ui.services.AiExplanationRepository
import com.example.smartleaf.ui.services.ApiClient
import com.example.smartleaf.ui.utils.SharedPrefsHelper
import kotlinx.coroutines.launch
import android.speech.tts.TextToSpeech
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private var currentLabel: String = "unknown"
    private var currentProb: Float = 0f

    private var lastAiExplanationRaw: String? = null

    private val descriptions = mapOf(
        "healthy" to "Leaf appears healthy—no clear disease pattern detected.",
        "tomato" to "Tomato leaf detected. Common issues include early/late blight and leaf miners.",
        "potato" to "Potato leaf detected. Watch for late blight and nutrient stress.",
        "cabbage" to "Cabbage leaf detected. Watch for black rot (V-shaped lesions) and caterpillar feeding.",
        "capsicum" to "Capsicum leaf detected. Check for bacterial spots and mite damage.",
        "bitter_gourd" to "Bitter gourd leaf detected. Prone to powdery mildew in humidity.",
        "corriander" to "Coriander leaf detected. Sensitive to heat; bolts easily.",
        "curry_leafs" to "Curry leaf plant detected. Often affected by mites in low airflow."
    )

    private val remedies = mapOf(
        "tomato" to listOf("Prune for airflow", "Mulch; water at soil level", "Monitor for fungal spots"),
        "cabbage" to listOf("Check undersides for caterpillars", "Remove yellow leaves", "Ensure spacing/airflow"),
        "potato" to listOf("Water evenly at soil level", "Remove infected leaves", "Good drainage"),
        "capsicum" to listOf("Stake plants; inspect for mites", "Avoid waterlogging"),
        "bitter_gourd" to listOf("Increase airflow", "Remove infected leaves early"),
        "corriander" to listOf("Provide partial shade in heat", "Avoid overwatering"),
        "curry_leafs" to listOf("Prune lightly for airflow", "Ensure pot drains well"),
        "healthy" to listOf("Maintain balanced watering", "Routine pest checks")
    )

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var isSpeaking = false

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.60f
        private const val TAG = "ResultActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            setupToolbar()

            currentLabel = intent.getStringExtra("label") ?: "unknown"
            currentProb = intent.getFloatExtra("prob", 0f)

            val imageUriString = intent.getStringExtra("image_uri")
            displayImageFromUri(imageUriString)

            displayResults()
            setupActions()

            // optional backend save
            saveScanToServer()

            initTts()
            setupSpeakButton()
            fetchAiExplanation()

        } catch (e: Exception) {
            Toast.makeText(this, "Result Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun displayImageFromUri(imageUriString: String?) {
        try {
            if (imageUriString.isNullOrBlank()) return

            val uri = Uri.parse(imageUriString)
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            binding.imagePreview.setImageBitmap(bitmap)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayResults() {
        val confidencePercent = currentProb * 100f
        val isLowConfidence = currentProb < CONFIDENCE_THRESHOLD

        binding.tvConfidence.text =
            getString(R.string.confidence_format, "%.1f".format(confidencePercent))

        val labelDisplay = currentLabel
            .replace('_', ' ')
            .split(' ')
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        binding.tvDetectedLabel.text = getString(R.string.detected_format, labelDisplay)

        binding.ivStatusIcon.setImageResource(
            if (isLowConfidence) R.drawable.ic_warning else R.drawable.ic_check
        )

        val baseDescription = descriptions[currentLabel] ?: "Detected: $labelDisplay."
        binding.tvDescription.text = if (isLowConfidence) {
            "⚠️ Low confidence (~${"%.1f".format(confidencePercent)}%).\n\n$baseDescription\n\n💡 Tip: Retake the photo in good light with leaf filling the frame."
        } else baseDescription

        val baseRemediesText = remedies[currentLabel]?.joinToString("\n") { "• $it" }
            ?: "• General care: proper watering and weekly pest checks."

        binding.tvRemedies.text = buildString {
            if (isLowConfidence) append("⚠️ Provisional remedies (low confidence):\n\n")
            append(baseRemediesText)
            append("\n\n📌 Note: This version classifies plant type, not specific diseases.")
        }
    }

    private fun setupActions() {
        binding.btnScanAgain.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
            finish()
        }
        binding.btnShare.setOnClickListener { shareResults() }
    }

    private fun shareResults() {
        val labelDisplay = currentLabel.replace('_', ' ')
        val confidenceText = "%.1f".format(currentProb * 100)
        val aiText = lastAiExplanationRaw ?: binding.tvExplanation.text.toString()

        val shareText = """
            🌿 SmartLeaf Scan Results

            Detected: $labelDisplay
            Confidence: $confidenceText%

            Explanation:
            $aiText
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "SmartLeaf Scan Results")
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun fetchAiExplanation() {
        binding.tvExplanation.text = getString(R.string.ai_expl_loading)
        val langCode = SharedPrefsHelper.getLanguage(this)

        lifecycleScope.launch {
            try {
                val aiText = AiExplanationRepository.getExplanation(
                    className = currentLabel,
                    confidence = currentProb,
                    languageCode = langCode
                )
                lastAiExplanationRaw = aiText
                showFormattedExplanation(aiText)
            } catch (e: Exception) {
                Log.e(TAG, "Gemini AI error", e)
                val fallback = getString(R.string.ai_expl_failed)
                lastAiExplanationRaw = fallback
                binding.tvExplanation.text = fallback
            }
        }
    }

    private fun showFormattedExplanation(rawText: String) {
        val withHtmlBold = rawText.replace(Regex("\\*\\*(.+?)\\*\\*")) { match ->
            "<b>${match.groupValues[1]}</b>"
        }
        val htmlText = withHtmlBold.replace("\n", "<br>")
        val spanned = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.tvExplanation.text = spanned
    }

    private fun saveScanToServer() {
        lifecycleScope.launch {
            try {
                val token = SharedPrefsHelper.getAuthToken(this@ResultActivity)
                if (token.isNullOrBlank()) return@launch

                ApiClient.setToken(token)

                val request = ScanRequest(
                    image_uri = "app://scan_${System.currentTimeMillis()}",
                    model_name = "SmartLeafClassifier",
                    model_version = "1.0",
                    predicted_code = currentLabel,
                    confidence = currentProb.toDouble(),
                    generated_desc = descriptions[currentLabel],
                    generated_symptoms = null,
                    generated_remedies = remedies[currentLabel]?.joinToString("; ")
                )

                ApiClient.api.saveScan(request)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ---------- TTS ----------
    private fun initTts() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setTtsLanguage(SharedPrefsHelper.getLanguage(this))
            } else ttsReady = false
        }
    }

    private fun setupSpeakButton() {
        binding.btnSpeak.setOnClickListener {
            val textToSpeak = (lastAiExplanationRaw ?: binding.tvExplanation.text?.toString().orEmpty()).trim()
            if (textToSpeak.isEmpty()) return@setOnClickListener
            if (!ttsReady) {
                Toast.makeText(this, "Voice not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isSpeaking) stopSpeaking() else speak(textToSpeak)
        }
    }

    private fun setTtsLanguage(langCode: String) {
        val locale = when (langCode) {
            "hi" -> Locale("hi", "IN")
            "mr" -> Locale("mr", "IN")
            else -> Locale("en", "US")
        }
        val result = tts?.setLanguage(locale)
        ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED

        if (!ttsReady) {
            val fallback = tts?.setLanguage(Locale("en", "US"))
            ttsReady = fallback != TextToSpeech.LANG_MISSING_DATA &&
                    fallback != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun speak(text: String) {
        tts?.stop()
        isSpeaking = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "smartleaf_tts_expl")
    }

    private fun stopSpeaking() {
        tts?.stop()
        isSpeaking = false
    }

    override fun onDestroy() {
        stopSpeaking()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}