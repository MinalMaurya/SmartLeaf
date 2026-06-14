package com.example.smartleaf.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.smartleaf.databinding.ActivityCategoryDetailBinding
import com.example.smartleaf.ui.utils.LocaleHelper

class CategoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryDetailBinding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Read data safely
        val plantName = intent.getStringExtra("plantName") ?: "Plant"
        val description = intent.getStringExtra("description") ?: ""
        val diseaseInfo = intent.getStringExtra("diseaseInfo") ?: ""  // ✅ match key sent
        val healthyRes = intent.getIntExtra("healthyImageResId", 0)
        val diseasedRes = intent.getIntExtra("diseasedImageResId", 0)

        supportActionBar?.title = plantName

        binding.tvDescription.text = description
        binding.tvDiseaseInfo.text = diseaseInfo

        if (healthyRes != 0) binding.imgHealthy.setImageResource(healthyRes)
        if (diseasedRes != 0) binding.imgDiseased.setImageResource(diseasedRes)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}