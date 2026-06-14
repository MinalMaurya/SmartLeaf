package com.example.smartleaf.ui.activities

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smartleaf.R
import com.example.smartleaf.databinding.ActivityAboutBinding
import com.example.smartleaf.ui.utils.LocaleHelper


class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            setupToolbar()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setupAboutContent()
    }


    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setHomeButtonEnabled(true)
            }

            binding.toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }


    private fun setupAboutContent() {
        // Build about text from string resources
        val aboutText = buildAboutText()

        // Style the text with colored and bold headings
        val styledText = styleAboutText(aboutText)
        binding.aboutContentTextView.text = styledText
    }


    private fun buildAboutText(): String {
        return """
${getString(R.string.about_title)}

${getString(R.string.about_intro)}

${getString(R.string.about_vision_title)}
${getString(R.string.about_vision_text)}

${getString(R.string.about_mission_title)}
${getString(R.string.about_mission_text)}

${getString(R.string.about_goals_title)}
${getString(R.string.about_goals_text)}

${getString(R.string.about_features_title)}
${getString(R.string.about_features_text)}

${getString(R.string.about_why_title)}
${getString(R.string.about_why_text)}

${getString(R.string.about_helps_title)}
${getString(R.string.about_helps_text)}

${getString(R.string.about_commitment_title)}
${getString(R.string.about_commitment_text)}

${getString(R.string.about_community_title)}
${getString(R.string.about_community_text)}
        """.trimIndent()
    }


    private fun styleAboutText(text: String): SpannableStringBuilder {
        val primaryColor = ContextCompat.getColor(this, R.color.primary)
        val accentColor = ContextCompat.getColor(this, R.color.amber)
        val spannable = SpannableStringBuilder(text)

        // List of heading patterns to highlight
        val headings = listOf(
            getString(R.string.about_title),
            getString(R.string.about_vision_title),
            getString(R.string.about_mission_title),
            getString(R.string.about_goals_title),
            getString(R.string.about_features_title),
            getString(R.string.about_why_title),
            getString(R.string.about_helps_title),
            getString(R.string.about_commitment_title),
            getString(R.string.about_community_title)
        )

        // Apply color and bold style to each heading
        for (heading in headings) {
            var startIndex = 0
            while (true) {
                val start = text.indexOf(heading, startIndex)
                if (start < 0) break

                val end = start + heading.length

                // Apply accent color to first heading, primary to others
                val color = if (heading == getString(R.string.about_title)) accentColor else primaryColor

                spannable.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                startIndex = end
            }
        }

        return spannable
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
