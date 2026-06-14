package com.example.smartleaf.ui.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.smartleaf.R
import com.example.smartleaf.ui.models.User

object SharedPrefsHelper {

    private const val PREFS_NAME = "smartleaf_prefs"

    // Keys
    private const val KEY_LANGUAGE = "language"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHONE = "phone"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_HISTORY = "history_list"
    private const val KEY_LAST_CATEGORY = "last_category"
    private const val KEY_FAVORITE_CATEGORIES = "favorite_categories"
    private const val KEY_AUTH_TOKEN = "auth_token"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // =====================================================
    //  LANGUAGE SETTINGS
    // =====================================================

    fun saveLanguage(context: Context, language: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun getLanguage(context: Context): String {
        return prefs(context).getString(KEY_LANGUAGE, "en") ?: "en"
    }

    // =====================================================
    //  LOGIN STATUS
    // =====================================================

    fun isLoggedIn(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_LOGGED_IN, false)

    fun setLoggedIn(context: Context, status: Boolean) {
        prefs(context).edit().putBoolean(KEY_IS_LOGGED_IN, status).apply()
    }

    // =====================================================
    //  USER DETAILS
    // =====================================================

    fun saveUser(context: Context, user: User) {
        prefs(context).edit()
            .putString(KEY_USERNAME, user.name)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_PHONE, user.phone)
            .apply()
    }

    fun setUser(context: Context, name: String, email: String, phone: String, language: String) {
        prefs(context).edit()
            .putString(KEY_USERNAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PHONE, phone)
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    fun getUser(context: Context): User? {
        val p = prefs(context)
        val name = p.getString(KEY_USERNAME, null)
        val email = p.getString(KEY_EMAIL, null)
        val phone = p.getString(KEY_PHONE, null)

        return if (name != null || email != null || phone != null) {
            User(
                name = name ?: "",
                email = email ?: "",
                phone = phone ?: ""
            )
        } else null
    }

    fun clearUser(context: Context) {
        prefs(context).edit()
            .remove(KEY_USERNAME)
            .remove(KEY_EMAIL)
            .remove(KEY_PHONE)
            .remove(KEY_IS_LOGGED_IN)
            .apply()
    }

    // =====================================================
    //  AUTH TOKEN
    // =====================================================

    fun saveAuthToken(context: Context, token: String) {
        prefs(context).edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(context: Context): String? =
        prefs(context).getString(KEY_AUTH_TOKEN, null)

    fun clearAuthToken(context: Context) {
        prefs(context).edit().remove(KEY_AUTH_TOKEN).apply()
    }

    fun hasAuthToken(context: Context): Boolean =
        !getAuthToken(context).isNullOrEmpty()

    fun restoreApiToken(context: Context) {
        val token = getAuthToken(context)
        if (token != null) {
            com.example.smartleaf.ui.services.ApiClient.setToken(token)
        }
    }

    // =====================================================
    // ✅ HISTORY (FIXED + SAFE)
    // Stored as: label@timestamp@imageResId || label@timestamp@imageResId ...
    // =====================================================

    fun saveHistory(context: Context, history: String) {
        prefs(context).edit().putString(KEY_HISTORY, history).apply()
    }

    fun getHistory(context: Context): String =
        prefs(context).getString(KEY_HISTORY, "") ?: ""

    fun addHistoryItem(context: Context, text: String, imageResId: Int) {
        val p = prefs(context)
        val joined = p.getString(KEY_HISTORY, "") ?: ""
        val list = joined.split("||").filter { it.isNotBlank() }.toMutableList()

        // Remove duplicates of same label
        list.removeAll { it.substringBefore("@") == text }

        val entry = "$text@${System.currentTimeMillis()}@$imageResId"
        list.add(0, entry)

        // Limit history to 50
        p.edit().putString(KEY_HISTORY, list.take(50).joinToString("||")).apply()
    }

    fun getHistoryList(context: Context): List<String> {
        val data = prefs(context).getString(KEY_HISTORY, "") ?: ""
        return data.split("||").filter { it.isNotBlank() }
    }

    fun clearHistory(context: Context) {
        prefs(context).edit().remove(KEY_HISTORY).apply()
    }

    // ✅ Simple icon mapping (NO custom icons required)
    fun getIconForLabel(label: String): Int {
        return when (label) {
            "tomato" -> R.mipmap.ic_launcher
            "potato" -> R.mipmap.ic_launcher
            "cabbage" -> R.mipmap.ic_launcher
            "capsicum" -> R.mipmap.ic_launcher
            "bitter_gourd" -> R.mipmap.ic_launcher
            "corriander" -> R.mipmap.ic_launcher
            "curry_leafs" -> R.mipmap.ic_launcher
            "healthy" -> R.mipmap.ic_launcher
            else -> R.mipmap.ic_launcher
        }
    }

    // =====================================================
    //  CATEGORY FAVORITES
    // =====================================================

    fun setLastCategory(context: Context, category: String) {
        prefs(context).edit().putString(KEY_LAST_CATEGORY, category).apply()
    }

    fun getLastCategory(context: Context): String? =
        prefs(context).getString(KEY_LAST_CATEGORY, null)

    fun addFavoriteCategory(context: Context, category: String) {
        val p = prefs(context)
        val list = (p.getString(KEY_FAVORITE_CATEGORIES, "") ?: "")
            .split("||").filter { it.isNotBlank() }.toMutableList()

        if (!list.contains(category)) {
            list.add(category)
            p.edit().putString(KEY_FAVORITE_CATEGORIES, list.joinToString("||")).apply()
        }
    }

    fun removeFavoriteCategory(context: Context, category: String) {
        val p = prefs(context)
        val list = (p.getString(KEY_FAVORITE_CATEGORIES, "") ?: "")
            .split("||").filter { it.isNotBlank() }.toMutableList()

        if (list.remove(category)) {
            p.edit().putString(KEY_FAVORITE_CATEGORIES, list.joinToString("||")).apply()
        }
    }

    fun getFavoriteCategories(context: Context): List<String> {
        val data = prefs(context).getString(KEY_FAVORITE_CATEGORIES, "") ?: ""
        return data.split("||").filter { it.isNotBlank() }
    }

    fun isFavoriteCategory(context: Context, category: String): Boolean =
        getFavoriteCategories(context).contains(category)

    fun clearFavoriteCategories(context: Context) {
        prefs(context).edit().remove(KEY_FAVORITE_CATEGORIES).apply()
    }

    // =====================================================
    //  GENERIC SAVE / GET STRING
    // =====================================================

    fun saveString(context: Context, key: String, value: String) {
        prefs(context).edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String, default: String): String {
        return prefs(context).getString(key, default) ?: default
    }

    // =====================================================
    //  CLEAR ALL DATA
    // =====================================================

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun logout(context: Context) {
        clearUser(context)
        clearAuthToken(context)
        setLoggedIn(context, false)
    }
}