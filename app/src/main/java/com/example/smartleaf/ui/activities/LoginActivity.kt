package com.example.smartleaf.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartleaf.R
import com.example.smartleaf.databinding.ActivityLoginBinding
import com.example.smartleaf.ui.models.LoginRequest
import com.example.smartleaf.ui.models.User
import com.example.smartleaf.ui.services.ApiClient
import com.example.smartleaf.ui.utils.LocaleHelper
import com.example.smartleaf.ui.utils.SharedPrefsHelper
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isLoading = false

    // Apply locale before activity creation
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if already logged in
        checkLoginStatus()

        // Setup UI components
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

    private fun checkLoginStatus() {
        if (SharedPrefsHelper.isLoggedIn(this)) {
            navigateToMain()
        }
    }

    private fun setupInputValidation() {
        // Email validation
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmail(s.toString())
            }
        })

        // Password validation
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePassword(s.toString())
            }
        })
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isEmpty()) {
            binding.tilEmail.error = null
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.invalid_email)
            false
        } else {
            binding.tilEmail.error = null
            true
        }
    }

    private fun validatePassword(password: String): Boolean {
        return if (password.isEmpty()) {
            binding.tilPassword.error = null
            false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.password_too_short)
            false
        } else {
            binding.tilPassword.error = null
            true
        }
    }

    private fun setupClickListeners() {
        // Login button
        binding.btnLogin.setOnClickListener {
            if (!isLoading) {
                handleLogin()
            }
        }

        // Sign up link
        binding.tvSignup.setOnClickListener {
            navigateToSignup()
        }

        // Forgot password link
        binding.tvForgotPassword.setOnClickListener {
            handleForgotPassword()
        }

        // Guest login button
        binding.btnGuestLogin.setOnClickListener {
            handleGuestLogin()
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validate inputs
        val isEmailValid = validateEmail(email)
        val isPasswordValid = validatePassword(password)

        if (!isEmailValid || !isPasswordValid) {
            if (email.isEmpty()) {
                binding.tilEmail.error = getString(R.string.email_required)
            }
            if (password.isEmpty()) {
                binding.tilPassword.error = getString(R.string.password_required)
            }
            return
        }

        // Show loading state
        showLoading()

        // Perform login API call
        lifecycleScope.launch {
            try {
                val response = ApiClient.api.login(LoginRequest(email, password))

                if (response.ok && response.token != null) {
                    // Login successful
                    handleLoginSuccess(response.token, response.farmer, email)
                } else {
                    // Login failed
                    hideLoading()
                    showError(getString(R.string.invalid_credentials))
                }
            } catch (e: Exception) {
                hideLoading()
                handleLoginError(e)
            }
        }
    }

    private fun handleLoginSuccess(
        token: String,
        farmer: com.example.smartleaf.ui.models.Farmer?,
        email: String
    ) {
        // Save authentication token
        SharedPrefsHelper.saveAuthToken(this, token)
        ApiClient.setToken(token)

        // Create and save user object
        val user = if (farmer != null) {
            User(
                name = farmer.name,
                email = farmer.email,
                phone = farmer.phone ?: ""
            )
        } else {
            User(
                name = "Farmer",
                email = email,
                phone = ""
            )
        }
        SharedPrefsHelper.saveUser(this, user)
        SharedPrefsHelper.setLoggedIn(this, true)

        hideLoading()
        showSuccess(getString(R.string.login_successful))

        // Navigate to main activity
        navigateToMain()
    }

    private fun handleLoginError(exception: Exception) {
        val errorMessage = when {
            exception.message?.contains("Unable to resolve host") == true ->
                getString(R.string.network_error)
            exception.message?.contains("timeout") == true ->
                getString(R.string.connection_timeout)
            else -> getString(R.string.login_failed, exception.message ?: "Unknown error")
        }
        showError(errorMessage)
    }

    private fun handleForgotPassword() {
        showError(getString(R.string.forgot_password_coming_soon))
    }
    private fun handleGuestLogin() {
        // Create guest user with proper initialization
        val guestUser = User(
            name = "Guest User",
            email = "guest@smartleaf.com",
            phone = "0000000000"
        )

        SharedPrefsHelper.saveUser(this, guestUser)
        SharedPrefsHelper.setLoggedIn(this, true)
        SharedPrefsHelper.saveLanguage(this, "en")

        showSuccess(getString(R.string.logged_in_as_guest))
        navigateToMain()
    }

    private fun showLoading() {
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.text = ""
        binding.btnLogin.isEnabled = false
        binding.etEmail.isEnabled = false
        binding.etPassword.isEnabled = false
    }

    private fun hideLoading() {
        isLoading = false
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.text = getString(R.string.login)
        binding.btnLogin.isEnabled = true
        binding.etEmail.isEnabled = true
        binding.etPassword.isEnabled = true
    }


    private fun showError(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }


    private fun showSuccess(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun navigateToSignup() {
        startActivity(Intent(this, SignupActivity::class.java))
    }
}
