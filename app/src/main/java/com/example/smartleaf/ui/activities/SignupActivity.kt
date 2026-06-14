package com.example.smartleaf.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.smartleaf.R
import com.example.smartleaf.databinding.ActivitySignupBinding
import com.example.smartleaf.ui.models.User
import com.example.smartleaf.ui.utils.LocaleHelper
import com.example.smartleaf.ui.utils.SharedPrefsHelper


class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private var isLoading = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInputValidation()

        setupClickListeners()

        setupBackButton()
    }

    private fun setupBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isLoading) {
                    finish()
                }
            }
        })
    }

    private fun setupInputValidation() {
        // Name validation
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateName(s.toString())
            }
        })

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmail(s.toString())
            }
        })

        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePhone(s.toString())
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePassword(s.toString())
            }
        })
    }

    private fun validateName(name: String): Boolean {
        return if (name.isEmpty()) {
            binding.tilName.error = null
            false
        } else if (name.length < 2) {
            binding.tilName.error = "Name must be at least 2 characters"
            false
        } else {
            binding.tilName.error = null
            true
        }
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isEmpty()) {
            binding.tilEmail.error = null
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            false
        } else {
            binding.tilEmail.error = null
            true
        }
    }


    private fun validatePhone(phone: String): Boolean {
        return if (phone.isEmpty()) {
            binding.tilPhone.error = null
            false
        } else if (phone.length < 10) {
            binding.tilPhone.error = "Phone number must be at least 10 digits"
            false
        } else {
            binding.tilPhone.error = null
            true
        }
    }

    private fun validatePassword(password: String): Boolean {
        return if (password.isEmpty()) {
            binding.tilPassword.error = null
            false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            false
        } else {
            binding.tilPassword.error = null
            true
        }
    }

    private fun setupClickListeners() {
        // Sign up button
        binding.btnSignup.setOnClickListener {
            if (!isLoading) {
                handleSignUp()
            }
        }

        binding.tvLoginLink.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun handleSignUp() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validate all inputs
        val isNameValid = validateName(name)
        val isEmailValid = validateEmail(email)
        val isPhoneValid = validatePhone(phone)
        val isPasswordValid = validatePassword(password)

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
        }
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
        }
        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number is required"
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
        }

        if (isNameValid && isEmailValid && isPhoneValid && isPasswordValid) {
            showLoading()
            performSignUp(name, email, phone)
        }
    }

    private fun performSignUp(name: String, email: String, phone: String) {
        try {
            val user = User(
                name = name,
                email = email,
                phone = phone
            )

            SharedPrefsHelper.saveUser(this, user)
            SharedPrefsHelper.setLoggedIn(this, true)
            SharedPrefsHelper.saveLanguage(this, "en")

            hideLoading()
            showSuccess("Account created successfully!")

            navigateToMain()

        } catch (e: Exception) {
            hideLoading()
            showError("Sign up failed: ${e.message}")
        }
    }

    private fun showLoading() {
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSignup.text = ""
        binding.btnSignup.isEnabled = false
        binding.etName.isEnabled = false
        binding.etEmail.isEnabled = false
        binding.etPhone.isEnabled = false
        binding.etPassword.isEnabled = false
    }

    private fun hideLoading() {
        isLoading = false
        binding.progressBar.visibility = View.GONE
        binding.btnSignup.text = getString(R.string.create_account)
        binding.btnSignup.isEnabled = true
        binding.etName.isEnabled = true
        binding.etEmail.isEnabled = true
        binding.etPhone.isEnabled = true
        binding.etPassword.isEnabled = true
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
