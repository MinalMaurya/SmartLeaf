package com.example.smartleaf.ui.utils

import android.content.Context
import com.example.smartleaf.ui.models.FaqItem
import org.json.JSONArray
import java.io.IOException

object FaqRepository {

    fun loadFaqs(context: Context, langCode: String): List<FaqItem> {
        // Try language file, else fallback to English
        val fileName = "faq_${langCode}.json"
        val jsonText = tryReadAsset(context, fileName) ?: tryReadAsset(context, "faq_en.json")
        if (jsonText.isNullOrBlank()) return emptyList()

        val arr = JSONArray(jsonText)
        val list = mutableListOf<FaqItem>()

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val tagsArr = o.optJSONArray("tags")
            val tags = mutableListOf<String>()
            if (tagsArr != null) {
                for (t in 0 until tagsArr.length()) tags.add(tagsArr.getString(t))
            }

            list.add(
                FaqItem(
                    id = o.optString("id"),
                    question = o.optString("question"),
                    answer = o.optString("answer"),
                    tags = tags
                )
            )
        }
        return list
    }

    private fun tryReadAsset(context: Context, file: String): String? {
        return try {
            context.assets.open(file).bufferedReader().use { it.readText() }
        } catch (_: IOException) {
            null
        }
    }
}