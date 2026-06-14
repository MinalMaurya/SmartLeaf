package com.example.smartleaf.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.smartleaf.databinding.ActivitySplashBinding
import com.example.smartleaf.databinding.ActivitySignupBinding
import com.example.smartleaf.ui.utils.SharedPrefsHelper
import com.example.smartleaf.ui.utils.LocaleHelper
class SplashActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        val langCode = SharedPrefsHelper.getLanguage(this) ?: "en"
        val context = LocaleHelper.setLocale(this, langCode)
        val resources = context.resources

        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val isLoggedIn = SharedPrefsHelper.isLoggedIn(this)
            val intent = if (isLoggedIn) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 2000)
    }
}