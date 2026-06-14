package com.example.smartleaf.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartleaf.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherDetailsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var tvPlace: TextView
    private lateinit var tvNoteLimit: TextView
    private lateinit var containerPast7: LinearLayout
    private lateinit var containerNext7: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_details)

        toolbar = findViewById(R.id.weather_toolbar)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        tvPlace = findViewById(R.id.tv_place)
        tvNoteLimit = findViewById(R.id.tv_note_limit)
        containerPast7 = findViewById(R.id.container_past7)
        containerNext7 = findViewById(R.id.container_next7)

        // back arrow
        toolbar.setNavigationOnClickListener { finish() }

        // bottom nav same as app
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    finish()
                    true
                }
                R.id.nav_categories -> {
                    startActivity(Intent(this, CategoryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)
        val placeName = intent.getStringExtra("placeName") ?: "—"

        tvPlace.text = placeName

        if (lat == 0.0 && lon == 0.0) {
            tvNoteLimit.visibility = View.VISIBLE
            tvNoteLimit.text = "Location not available."
            return
        }

        fetchPastAndNext7(lat, lon)
    }

    private fun fetchPastAndNext7(lat: Double, lon: Double) {
        Thread {
            try {
                // ✅ Open-Meteo: past 7 + next 7 (daily)
                val urlStr =
                    "https://api.open-meteo.com/v1/forecast" +
                            "?latitude=$lat&longitude=$lon" +
                            "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max,weather_code" +
                            "&past_days=7" +
                            "&forecast_days=7" +
                            "&timezone=auto"

                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val daily = json.getJSONObject("daily")

                val timeArr = daily.getJSONArray("time")
                val tMaxArr = daily.getJSONArray("temperature_2m_max")
                val tMinArr = daily.getJSONArray("temperature_2m_min")
                val rainArr = daily.getJSONArray("precipitation_sum")
                val windArr = daily.getJSONArray("wind_speed_10m_max")
                val codeArr = daily.getJSONArray("weather_code")

                val total = timeArr.length() // usually 14

                runOnUiThread {
                    containerPast7.removeAllViews()
                    containerNext7.removeAllViews()
                    tvNoteLimit.visibility = View.GONE
                }

                // ✅ first 7 = past
                val pastCount = minOf(7, total)
                for (i in 0 until pastCount) {
                    val row = makeRow(
                        date = timeArr.getString(i),
                        max = tMaxArr.getDouble(i),
                        min = tMinArr.getDouble(i),
                        rain = rainArr.getDouble(i),
                        wind = windArr.getDouble(i),
                        code = codeArr.getInt(i)
                    )
                    runOnUiThread { containerPast7.addView(row) }
                }

                // ✅ next 7 = future
                val startFutureIndex = pastCount
                val endFutureIndex = minOf(startFutureIndex + 7, total)

                for (i in startFutureIndex until endFutureIndex) {
                    val row = makeRow(
                        date = timeArr.getString(i),
                        max = tMaxArr.getDouble(i),
                        min = tMinArr.getDouble(i),
                        rain = rainArr.getDouble(i),
                        wind = windArr.getDouble(i),
                        code = codeArr.getInt(i)
                    )
                    runOnUiThread { containerNext7.addView(row) }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    tvNoteLimit.visibility = View.VISIBLE
                    tvNoteLimit.text = "Weather unavailable. Please check internet."
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // ✅ Row UI (with icon placeholder)
    private fun makeRow(date: String, max: Double, min: Double, rain: Double, wind: Double, code: Int): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 12)
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val tvDate = TextView(this).apply {
            text = formatDate(date)
            setTextColor(0xFF111827.toInt())
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // ✅ ICON PLACEHOLDER (you can replace later with ImageView)
        val iconHint = weatherEmoji(code) // currently emoji, you can later use drawable
        val tvIcon = TextView(this).apply {
            text = iconHint
            textSize = 16f
        }

        top.addView(tvDate)
        top.addView(tvIcon)

        val tvInfo = TextView(this).apply {
            text = "Max ${max.toInt()}°  |  Min ${min.toInt()}°   •   Rain ${rain.toInt()}mm   •   Wind ${wind.toInt()} km/h"
            setTextColor(0xFF6B7280.toInt())
            textSize = 12.5f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val divider = View(this).apply {
            setBackgroundColor(0xFFE5E7EB.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply { topMargin = 12 }
        }

        row.addView(top)
        row.addView(tvInfo)
        row.addView(divider)
        return row
    }

    private fun weatherEmoji(code: Int): String {
        return when (code) {
            0 -> "☀️"                 // clear
            1, 2, 3 -> "⛅"            // cloudy
            45, 48 -> "🌫️"            // fog
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️" // rain
            71, 73, 75, 77, 85, 86 -> "❄️" // snow
            95, 96, 99 -> "⛈️"        // thunder
            else -> "🌤️"
        }
    }

    private fun formatDate(yyyyMmDd: String): String {
        return try {
            val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outFmt = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
            val d = inFmt.parse(yyyyMmDd)
            if (d != null) outFmt.format(d) else yyyyMmDd
        } catch (e: Exception) {
            yyyyMmDd
        }
    }
}