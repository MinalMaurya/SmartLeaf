package com.example.smartleaf.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartleaf.databinding.ItemHistoryBinding
import com.example.smartleaf.ui.models.HistoryItem
import com.example.smartleaf.ui.utils.DynamicTranslationHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying history items in RecyclerView
 */
class HistoryAdapter(
    private var historyList: MutableList<HistoryItem>,
    private val onDeleteClick: (Int) -> Unit,
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var currentLang: String = "en"

    class HistoryViewHolder(val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]

        with(holder.binding) {
            // Set image
            ivHistoryImage.setImageResource(item.imageResId)

            // Translate text using the key
            val translatedText = DynamicTranslationHelper.getTranslation(
                currentLang,
                item.text
            )
            tvHistoryText.text = translatedText

            // Format timestamp
            tvHistoryTimestamp.text = formatTimestamp(item.timestamp)

            // Delete button click
            ivDelete.setOnClickListener {
                onDeleteClick(position)
            }

            // Item click
            root.setOnClickListener {
                onItemClick(item)
            }

            // View details button click
            btnViewDetails.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun getItemCount(): Int = historyList.size

    /**
     * Update list with new data and language
     * @param newList New history items
     * @param lang Current language code
     */
    fun updateList(newList: MutableList<HistoryItem>, lang: String) {
        this.historyList = newList
        this.currentLang = lang
        notifyDataSetChanged()
    }

    /**
     * Format timestamp to readable string
     * @param timestamp Unix timestamp
     * @return Formatted time string
     */
    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Just now"  // Less than 1 minute
            diff < 3600000 -> "${diff / 60000} minutes ago"  // Less than 1 hour
            diff < 86400000 -> "${diff / 3600000} hours ago"  // Less than 24 hours
            diff < 604800000 -> "${diff / 86400000} days ago"  // Less than 7 days
            else -> {
                // Show date for older items
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }
}
