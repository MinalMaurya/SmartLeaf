package com.example.smartleaf.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smartleaf.R
import com.example.smartleaf.ui.ml.SmartLeafClassifier
import com.example.smartleaf.ui.utils.LocaleHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ScanActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var cardCamera: MaterialCardView
    private lateinit var cardStorage: MaterialCardView
    private lateinit var cardAbout: MaterialButton

    private lateinit var loadingOverlay: FrameLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var progressBar: ProgressBar

    private var classifier: SmartLeafClassifier? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    // ---------------- Permissions ----------------

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openCamera()
            else Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
        }

    private val galleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openGallery()
            else Toast.makeText(this, getString(R.string.gallery_permission_denied), Toast.LENGTH_SHORT).show()
        }

    // ---------------- Camera + Gallery pickers ----------------

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = result.data?.getParcelableExtra<Bitmap>("data")
                if (bitmap != null) {
                    // Camera gives bitmap directly (no URI)
                    processBitmapAndOpenResult(bitmap, imageUri = null)
                } else {
                    Toast.makeText(this, "Camera image not found", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    try {
                        // Use bitmap for ML, but send URI to ResultActivity
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        processBitmapAndOpenResult(bitmap, imageUri)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            getString(R.string.error_loading_image, e.message ?: "Unknown error"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    // ---------------- Lifecycle ----------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        initializeViews()
        setupToolbar()
        initializeClassifier()
        setupClickListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        cardCamera = findViewById(R.id.cardCamera)
        cardStorage = findViewById(R.id.cardStorage)
        cardAbout = findViewById(R.id.cardAbout)

        loadingOverlay = findViewById(R.id.loadingOverlay)
        tvLoadingText = findViewById(R.id.tvLoadingText)
        progressBar = loadingOverlay.findViewById(R.id.progressBar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun initializeClassifier() {
        try {
            classifier = SmartLeafClassifier(this)
            Toast.makeText(this, getString(R.string.ml_model_loaded), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            classifier = null
            Toast.makeText(
                this,
                getString(R.string.error_loading_ml_model, e.message ?: "Unknown error"),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupClickListeners() {
        cardCamera.setOnClickListener { requestCameraPermission() }
        cardStorage.setOnClickListener { requestGalleryPermission() }
        cardAbout.setOnClickListener { startActivity(Intent(this, AboutActivity::class.java)) }
    }

    // ---------------- Permission helpers ----------------

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) openCamera()
        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED
            ) openGallery()
            else galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) openGallery()
            else galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    // ---------------- Core logic ----------------

    private fun processBitmapAndOpenResult(bitmap: Bitmap, imageUri: Uri?) {
        val clf = classifier
        if (clf == null) {
            Toast.makeText(this, getString(R.string.ml_model_not_initialized), Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        Thread {
            try {
                val topList = clf.predictTopK(bitmap, k = 1)
                val top = topList.firstOrNull()

                val label = top?.label ?: "unknown"
                val confidence = top?.confidence ?: 0f

                runOnUiThread {
                    showLoading(false)
                    navigateToResults(label, confidence, imageUri)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        getString(R.string.error_analyzing_image, e.message ?: "Unknown error"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun navigateToResults(label: String, confidence: Float, imageUri: Uri?) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("label", label)
            putExtra("prob", confidence)
            putExtra("image_uri", imageUri?.toString()) // ✅ send URI not bytes
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier = null
    }
}