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
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smartleaf.R
import com.example.smartleaf.ui.utils.DynamicTranslationHelper
import com.example.smartleaf.ui.utils.LocaleHelper
import com.example.smartleaf.ui.utils.SharedPrefsHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream


class EditProfileActivity : AppCompatActivity() {

    // UI Components
    private lateinit var toolbar: MaterialToolbar
    private lateinit var imgProfile: ImageView
    private lateinit var btnEditPhoto: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var spinnerLanguage: Spinner

    // TextInputLayouts
    private lateinit var layoutName: TextInputLayout
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var layoutPhone: TextInputLayout
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var layoutConfirmPassword: TextInputLayout

    // EditTexts
    private lateinit var editName: TextInputEditText
    private lateinit var editEmail: TextInputEditText
    private lateinit var editPhone: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var editConfirmPassword: TextInputEditText

    // Language configuration - 11 languages
    private val languages = listOf(
        "English",
        "हिन्दी",
        "मराठी",
        "বাংলা",
        "கணிதம்",
        "മലയാളം",
        "తెలుగు",
        "تاملی",
        "ਪੰਜਾਬੀ",
        "ગુજરાતી",
        "اردو"
    )
    private val codes = listOf(
        "en",
        "hi",
        "mr",
        "bn",
        "ta",
        "ml",
        "te",
        "kn",
        "pa",
        "gu",
        "ur"
    )

    private var currentLangCode = "en"
    private var spinnerInitialized = false
    private var profilePhotoUri: Uri? = null


    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.getParcelableExtra<Bitmap>("data")
            if (bitmap != null) {
                imgProfile.setImageBitmap(bitmap)
                savePhotoToFile(bitmap)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                imgProfile.setImageURI(imageUri)
                profilePhotoUri = imageUri
                copyUriToFile(imageUri)
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val langCode = SharedPrefsHelper.getLanguage(newBase) ?: "en"
        currentLangCode = langCode
        super.attachBaseContext(LocaleHelper.setLocale(newBase, langCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initializeViews()

        setupToolbar()

        setupLanguageSpinner()

        loadUserData()

        applyTranslations(currentLangCode)

        setupClickListeners()

        setupValidationListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        imgProfile = findViewById(R.id.imgProfile)
        btnEditPhoto = findViewById(R.id.btnEditPhoto)
        btnSave = findViewById(R.id.btnSave)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)

        layoutName = findViewById(R.id.layoutName)
        layoutEmail = findViewById(R.id.layoutEmail)
        layoutPhone = findViewById(R.id.layoutPhone)
        layoutPassword = findViewById(R.id.layoutPassword)
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword)

        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editPhone = findViewById(R.id.editPhone)
        editPassword = findViewById(R.id.editPassword)
        editConfirmPassword = findViewById(R.id.editConfirmPassword)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupLanguageSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        val langIndex = codes.indexOf(currentLangCode)
        if (langIndex >= 0) spinnerLanguage.setSelection(langIndex)

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                if (!spinnerInitialized) {
                    spinnerInitialized = true
                    return
                }

                val selectedLangCode = codes[position]
                if (selectedLangCode != currentLangCode) {
                    currentLangCode = selectedLangCode
                    SharedPrefsHelper.saveLanguage(this@EditProfileActivity, selectedLangCode)
                    recreate() // Refresh UI with new language
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupClickListeners() {
        btnEditPhoto.setOnClickListener {
            showPhotoPickerDialog()
        }

        btnSave.setOnClickListener {
            if (validateInputs()) {
                saveProfile()
            }
        }
    }


    private fun showPhotoPickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Select Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> requestCameraPermission()
                    1 -> requestGalleryPermission()
                }
            }
            .show()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
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

    private fun savePhotoToFile(bitmap: Bitmap) {
        try {
            val photoDir = File(getExternalFilesDir(null), "profile_photos")
            if (!photoDir.exists()) {
                photoDir.mkdirs()
            }

            val photoFile = File(photoDir, "profile_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(photoFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            profilePhotoUri = Uri.fromFile(photoFile)
            SharedPrefsHelper.saveString(this, "profile_photo_uri", profilePhotoUri.toString())
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving photo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyUriToFile(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            savePhotoToFile(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading photo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupValidationListeners() {
        editName.addTextChangedListener(createTextWatcher { validateName() })
        editEmail.addTextChangedListener(createTextWatcher { validateEmail() })
        editPhone.addTextChangedListener(createTextWatcher { validatePhone() })
        editPassword.addTextChangedListener(createTextWatcher { validatePassword() })
        editConfirmPassword.addTextChangedListener(createTextWatcher { validateConfirmPassword() })
    }

    private fun createTextWatcher(callback: () -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                callback()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val isNameValid = validateName()
        val isEmailValid = validateEmail()
        val isPhoneValid = validatePhone()
        val isPasswordValid = validatePassword()
        val isConfirmPasswordValid = validateConfirmPassword()

        return isNameValid && isEmailValid && isPhoneValid && isPasswordValid && isConfirmPasswordValid
    }

    private fun validateName(): Boolean {
        val name = editName.text.toString().trim()
        return when {
            name.isEmpty() -> {
                layoutName.error = "Name is required"
                false
            }
            name.length < 2 -> {
                layoutName.error = "Name must be at least 2 characters"
                false
            }
            else -> {
                layoutName.error = null
                true
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = editEmail.text.toString().trim()
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return when {
            email.isEmpty() -> {
                layoutEmail.error = "Email is required"
                false
            }
            !email.matches(emailPattern.toRegex()) -> {
                layoutEmail.error = "Invalid email format"
                false
            }
            else -> {
                layoutEmail.error = null
                true
            }
        }
    }

    private fun validatePhone(): Boolean {
        val phone = editPhone.text.toString().trim()
        return when {
            phone.isEmpty() -> {
                layoutPhone.error = "Phone is required"
                false
            }
            phone.length < 10 -> {
                layoutPhone.error = "Phone must be at least 10 digits"
                false
            }
            else -> {
                layoutPhone.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = editPassword.text.toString().trim()

        if (password.isEmpty()) {
            layoutPassword.error = null
            return true
        }

        return when {
            password.length < 6 -> {
                layoutPassword.error = "Password must be at least 6 characters"
                false
            }
            else -> {
                layoutPassword.error = null
                true
            }
        }
    }

    private fun validateConfirmPassword(): Boolean {
        val password = editPassword.text.toString().trim()
        val confirmPassword = editConfirmPassword.text.toString().trim()

        if (password.isEmpty() && confirmPassword.isEmpty()) {
            layoutConfirmPassword.error = null
            return true
        }

        return when {
            confirmPassword.isEmpty() -> {
                layoutConfirmPassword.error = "Please confirm your password"
                false
            }
            password != confirmPassword -> {
                layoutConfirmPassword.error = "Passwords do not match"
                false
            }
            else -> {
                layoutConfirmPassword.error = null
                true
            }
        }
    }

    private fun saveProfile() {
        val name = editName.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val phone = editPhone.text.toString().trim()
        val selectedLangCode = codes[spinnerLanguage.selectedItemPosition]

        SharedPrefsHelper.setUser(this, name, email, phone, selectedLangCode)

        Toast.makeText(
            this,
            DynamicTranslationHelper.getTranslation(selectedLangCode, "profile_saved"),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    private fun loadUserData() {
        val user = SharedPrefsHelper.getUser(this)
        user?.let {
            editName.setText(it.name)
            editEmail.setText(it.email)
            editPhone.setText(it.phone)
        }

        val photoUri = SharedPrefsHelper.getString(this, "profile_photo_uri", "")
        if (photoUri.isNotEmpty()) {
            try {
                imgProfile.setImageURI(Uri.parse(photoUri))
            } catch (e: Exception) {
            }
        }
    }
    private fun applyTranslations(langCode: String) {
        toolbar.title = DynamicTranslationHelper.getTranslation(langCode, "edit_profile")
        btnSave.text = DynamicTranslationHelper.getTranslation(langCode, "save_changes")
        btnEditPhoto.text = DynamicTranslationHelper.getTranslation(langCode, "change_photo")

        // Input field hints
        layoutName.hint = DynamicTranslationHelper.getTranslation(langCode, "full_name")
        layoutEmail.hint = DynamicTranslationHelper.getTranslation(langCode, "email_label")
        layoutPhone.hint = DynamicTranslationHelper.getTranslation(langCode, "phone_label")
        layoutPassword.hint = DynamicTranslationHelper.getTranslation(langCode, "new_password")
        layoutConfirmPassword.hint = DynamicTranslationHelper.getTranslation(langCode, "confirm_password")
    }
}
