package com.example.smartleaf.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartleaf.R
import com.example.smartleaf.databinding.ActivityDiagnoseBinding
import com.example.smartleaf.ui.domain.SymptomRuleEngine
import com.example.smartleaf.ui.ml.SmartLeafClassifier
import com.example.smartleaf.ui.utils.LocaleHelper
import com.example.smartleaf.ui.vision.ImageAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DemoDiagnoseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiagnoseBinding
    private var classifier: SmartLeafClassifier? = null
    private var currentBitmap: Bitmap? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 42
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityDiagnoseBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupUI()
            setupClassifier()
            setupClickListeners()
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up classifier resources
        try {
            classifier?.close()
            classifier = null
            currentBitmap?.recycle()
            currentBitmap = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupUI() {
        try {
            binding.llResults.visibility = View.GONE
            binding.llPlaceholder.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this, "UI setup error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupClassifier() {
        try {
            classifier = SmartLeafClassifier(this)
            Toast.makeText(this, "ML Model loaded", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading ML model: ${e.message}", Toast.LENGTH_LONG).show()
            classifier = null
        }
    }

    private fun setupClickListeners() {
        binding.pickImageBtn.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select Leaf Image"),
            PICK_IMAGE_REQUEST
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            val bitmap = uriToBitmap(uri)
            if (bitmap != null) {
                currentBitmap?.recycle()
                currentBitmap = bitmap
                displayImage(bitmap)
                diagnoseImage(bitmap)
            } else {
                showError("Failed to load image")
            }
        } catch (e: Exception) {
            showError("Error loading image: ${e.message}")
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }

    private fun displayImage(bitmap: Bitmap) {
        try {
            binding.imagePreview.setImageBitmap(bitmap)
            binding.llPlaceholder.visibility = View.GONE
        } catch (e: Exception) {
            Toast.makeText(this, "Error displaying image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading() {
        binding.flLoadingOverlay.visibility = View.VISIBLE
        binding.pickImageBtn.isEnabled = false
        binding.llResults.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.flLoadingOverlay.visibility = View.GONE
        binding.pickImageBtn.isEnabled = true
    }

    private fun diagnoseImage(bitmap: Bitmap) {
        if (classifier == null) {
            showError("ML Model not initialized")
            return
        }

        showLoading()

        lifecycleScope.launch {
            try {
                // Step 1: Run ML classification
                val topK = withContext(Dispatchers.Default) {
                    classifier!!.predictTopK(bitmap, k = 3)
                }

                // Step 2: Analyze image for visual symptoms
                val signals = withContext(Dispatchers.Default) {
                    ImageAnalyzer.analyze(bitmap)
                }

                // Step 3: Generate comprehensive diagnosis
                val generated = withContext(Dispatchers.Default) {
                    SymptomRuleEngine.generate(
                        topK = topK,
                        img = signals,
                        hostHint = null,
                        organHint = "leaf"
                    )
                }

                // Display results
                displayResults(generated.primary, generated.bullets, generated.alternatives)

            } catch (e: Exception) {
                showError("Diagnosis error: ${e.message}")
            } finally {
                hideLoading()
            }
        }
    }


    private fun displayResults(
        primary: String,
        details: List<String>,
        alternatives: String?
    ) {
        try {
            binding.llResults.visibility = View.VISIBLE

            binding.primaryText.text = primary

            val detailsText = if (details.isNotEmpty()) {
                details.joinToString("\n• ", prefix = "• ")
            } else {
                "No details available"
            }
            binding.detailsText.text = detailsText

            binding.alternativesText.text = alternatives ?: "No alternatives"
        } catch (e: Exception) {
            Toast.makeText(this, "Error displaying results: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showError(message: String) {
        hideLoading()
        binding.llResults.visibility = View.VISIBLE
        binding.primaryText.text = "Error"
        binding.detailsText.text = message
        binding.alternativesText.text = "Please try again"
    }
}
