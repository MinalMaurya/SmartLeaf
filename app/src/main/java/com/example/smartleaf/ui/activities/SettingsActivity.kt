package com.example.smartleaf.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.smartleaf.R
import com.example.smartleaf.ui.models.User
import com.example.smartleaf.ui.utils.DynamicTranslationHelper
import com.example.smartleaf.ui.utils.SharedPrefsHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var tvSettingsTitle: TextView
    private lateinit var labelLanguage: TextView
    private lateinit var tvNotificationsTitle: TextView
    private lateinit var tvPrivacyPolicy: TextView
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhone: EditText
    private lateinit var spinnerLanguage: Spinner
    private lateinit var switchNotifications: Switch
    private lateinit var btnSave: Button
    private lateinit var btnFeedback: Button

    private val languages = listOf("English", "हिन्दी", "मराठी")
    private val codes = listOf("en", "hi", "mr")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize views with correct IDs from your XML
        tvSettingsTitle = findViewById(R.id.tvSettingsTitle)
        labelLanguage = findViewById(R.id.labelLanguage)
        tvNotificationsTitle = findViewById(R.id.tvNotificationsTitle)
        tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicy)
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editPhone = findViewById(R.id.editPhone)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        switchNotifications = findViewById(R.id.switchNotifications)
        btnSave = findViewById(R.id.btnSave)
        btnFeedback = findViewById(R.id.btnFeedback)

        // Populate current user info
        val user: User? = SharedPrefsHelper.getUser(this)
        editName.setText(user?.name ?: "")
        editEmail.setText(user?.email ?: "")
        editPhone.setText(user?.phone ?: "")

        // Get current locale tag
        val currentLang = AppCompatDelegate
            .getApplicationLocales()
            .toLanguageTags()
            .substringBefore(',')
            .ifEmpty { "en" }

        updateAllTexts(currentLang)
        setupLanguageSpinner(currentLang)

        btnSave.setOnClickListener {
            saveSettings()
        }

        btnFeedback.setOnClickListener {
            // Handle feedback - could open a feedback dialog or email
            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
        }

        tvPrivacyPolicy.setOnClickListener {
            // Open privacy policy
            Toast.makeText(this, "Opening Privacy Policy...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLanguageSpinner(currentLang: String) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        val index = codes.indexOf(currentLang)
        if (index >= 0) spinnerLanguage.setSelection(index)

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                updateAllTexts(codes[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateAllTexts(lang: String) {
        tvSettingsTitle.text = DynamicTranslationHelper.getTranslation(lang, "settings")
        labelLanguage.text = DynamicTranslationHelper.getTranslation(lang, "select_language")
        tvNotificationsTitle.text = DynamicTranslationHelper.getTranslation(lang, "enable_notifications")
        tvPrivacyPolicy.text = DynamicTranslationHelper.getTranslation(lang, "view_privacy_policy")
        btnSave.text = DynamicTranslationHelper.getTranslation(lang, "save")
        btnFeedback.text = DynamicTranslationHelper.getTranslation(lang, "feedback")
    }

    private fun saveSettings() {
        val updatedName = editName.text.toString().trim()
        val updatedEmail = editEmail.text.toString().trim()
        val updatedPhone = editPhone.text.toString().trim()
        val updatedLang = codes[spinnerLanguage.selectedItemPosition]

        // Validate inputs
        if (updatedName.isEmpty() || updatedEmail.isEmpty() || updatedPhone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Save user info and new language
        SharedPrefsHelper.setUser(this, updatedName, updatedEmail, updatedPhone, updatedLang)

        // Apply per-app locale
        val localeList = LocaleListCompat.forLanguageTags(updatedLang)
        AppCompatDelegate.setApplicationLocales(localeList)

        Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
    }
}
