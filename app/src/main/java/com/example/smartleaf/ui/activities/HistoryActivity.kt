package com.example.smartleaf.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartleaf.R
import com.example.smartleaf.databinding.ActivityHistoryBinding
import com.example.smartleaf.ui.adapters.HistoryAdapter
import com.example.smartleaf.ui.models.HistoryItem
import com.example.smartleaf.ui.utils.DynamicTranslationHelper
import com.example.smartleaf.ui.utils.LocaleHelper
import com.example.smartleaf.ui.utils.SharedPrefsHelper

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()
    private var currentLang: String = "en"

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        currentLang = SharedPrefsHelper.getLanguage(this)
        loadHistory()
        applyTranslations()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter = HistoryAdapter(
            historyList = historyList,
            onDeleteClick = { position ->
                showDeleteConfirmation(position)
            },
            onItemClick = { _ ->
                // Optional: navigate to details
            }
        )

        binding.historyRecyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnClearAll.setOnClickListener {
            if (historyList.isNotEmpty()) {
                showClearAllConfirmation()
            }
        }
    }

    private fun loadHistory() {
        historyList.clear()

        val rawHistory = SharedPrefsHelper.getHistory(this)

        if (rawHistory.isNotBlank()) {
            val items = rawHistory.split("||").filter { it.isNotBlank() }

            for (item in items) {
                val parts = item.split("@")
                if (parts.size >= 3) {
                    val labelKey = parts[0].trim()
                    val timestamp = parts[1].trim().toLongOrNull() ?: System.currentTimeMillis()
                    val imageResId = parts[2].trim().toIntOrNull() ?: R.mipmap.ic_launcher

                    historyList.add(
                        HistoryItem(
                            text = labelKey,
                            timestamp = timestamp,
                            imageResId = imageResId
                        )
                    )
                }
            }
        }

        updateUI()
    }

    private fun updateUI() {
        if (historyList.isEmpty()) {
            showEmptyState()
        } else {
            showHistoryList()
        }
        updateCount()
        adapter.updateList(historyList, currentLang)
    }

    private fun showEmptyState() {
        binding.llEmptyState.visibility = View.VISIBLE
        binding.historyRecyclerView.visibility = View.GONE
        binding.btnClearAll.visibility = View.GONE
    }

    private fun showHistoryList() {
        binding.llEmptyState.visibility = View.GONE
        binding.historyRecyclerView.visibility = View.VISIBLE
        binding.btnClearAll.visibility = View.VISIBLE
    }

    private fun updateCount() {
        val countText = getString(R.string.recent_items_format, historyList.size)
        binding.tvClickedOn.text = countText
    }

    private fun applyTranslations() {
        binding.tvHistoryTitle.text =
            DynamicTranslationHelper.getTranslation(currentLang, "search_history")
        binding.tvSubtitle.text =
            DynamicTranslationHelper.getTranslation(currentLang, "your_recent_diagnoses")
        adapter.updateList(historyList, currentLang)
    }

    private fun showDeleteConfirmation(position: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_item_title)
            .setMessage(R.string.delete_item_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteHistoryItem(position)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteHistoryItem(position: Int) {
        if (position >= 0 && position < historyList.size) {
            historyList.removeAt(position)
            saveHistoryToPrefs()
            updateUI()
        }
    }

    private fun showClearAllConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.clear_all_title)
            .setMessage(R.string.clear_all_message)
            .setPositiveButton(R.string.clear_all) { _, _ ->
                clearAllHistory()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun clearAllHistory() {
        historyList.clear()
        SharedPrefsHelper.clearHistory(this)
        updateUI()
    }

    private fun saveHistoryToPrefs() {
        val historyString = historyList.joinToString("||") { item ->
            "${item.text}@${item.timestamp}@${item.imageResId}"
        }
        SharedPrefsHelper.saveHistory(this, historyString)
    }
}