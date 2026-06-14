package com.example.smartleaf.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartleaf.databinding.ActivityChatbotHomeBinding
import com.example.smartleaf.ui.adapters.FaqAdapter
import com.example.smartleaf.ui.utils.FaqRepository
import com.example.smartleaf.ui.utils.SharedPrefsHelper

class ChatbotHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lang = SharedPrefsHelper.getLanguage(this) ?: "en"
        val faqs = FaqRepository.loadFaqs(this, lang)

        binding.rvFaq.layoutManager = LinearLayoutManager(this)
        binding.rvFaq.adapter = FaqAdapter(faqs) { item ->
            val i = Intent(this, ChatbotActivity::class.java).apply {
                putExtra("prefill_q", item.question)
                putExtra("prefill_a", item.answer)
            }
            startActivity(i)
        }
    }
}