package com.example.smartleaf.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartleaf.databinding.ActivityProfileBinding
import com.example.smartleaf.ui.utils.DynamicTranslationHelper
import com.example.smartleaf.ui.utils.LocaleHelper
import com.example.smartleaf.ui.utils.SharedPrefsHelper

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var langCode: String = "en"

    override fun attachBaseContext(newBase: Context) {
        // Apply locale before activity creation
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize language preference
        langCode = SharedPrefsHelper.getLanguage(this) ?: "en"

        // Setup UI components
        setupUI()
        loadProfileData()
        applyDynamicTranslations()

        // Setup click listeners
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to profile screen
        langCode = SharedPrefsHelper.getLanguage(this) ?: "en"
        loadProfileData()
        applyDynamicTranslations()
    }

    private fun setupUI() {
        // Set window flags for immersive experience (optional)
        // Can add status bar customization here if needed
    }

    private fun setupClickListeners() {
        // Edit Profile button - Navigate to EditProfileActivity
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            SharedPrefsHelper.clearUser(this)

            // Navigate to LoginActivity and clear back stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }
    }

    private fun loadProfileData() {
        val user = SharedPrefsHelper.getUser(this)

        // Display name with fallback
        binding.tvName.text = user?.name?.takeIf { it.isNotEmpty() }
            ?: DynamicTranslationHelper.getTranslation(langCode, "name_not_set")

        // Display email with fallback
        binding.tvEmail.text = user?.email?.takeIf { it.isNotEmpty() }
            ?: DynamicTranslationHelper.getTranslation(langCode, "email_not_set")

        // Display phone with fallback
        binding.tvPhone.text = user?.phone?.takeIf { it.isNotEmpty() }
            ?: DynamicTranslationHelper.getTranslation(langCode, "phone_not_set")

        // Display full language name based on current language code
        binding.tvLanguage.text = when (langCode) {
            "en" -> "English"
            "hi" -> "हिन्दी"
            "mr" -> "मराठी"
            else -> langCode.uppercase()
        }
    }

    private fun applyDynamicTranslations() {
        // Translate main heading
        binding.tvProfileHeading.text =
            DynamicTranslationHelper.getTranslation(langCode, "profile_heading")

        binding.tvNameLabel.text =
            DynamicTranslationHelper.getTranslation(langCode, "label_name")

        binding.tvEmailLabel.text =
            DynamicTranslationHelper.getTranslation(langCode, "label_email")

        binding.tvPhoneLabel.text =
            DynamicTranslationHelper.getTranslation(langCode, "label_phone")

        binding.tvLanguageLabel.text =
            DynamicTranslationHelper.getTranslation(langCode, "Language")

        // Translate button text
        binding.btnEditProfile.text =
            DynamicTranslationHelper.getTranslation(langCode, "Edit profile")

        binding.btnLogout.text =
            DynamicTranslationHelper.getTranslation(langCode, "Logout button")
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }
}
