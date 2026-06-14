package com.example.smartleaf.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smartleaf.R
import com.example.smartleaf.ui.utils.DynamicTranslationHelper
import com.example.smartleaf.ui.utils.SharedPrefsHelper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var lang: String

    private lateinit var fabQuickScan: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var cardAskChatbot: MaterialCardView
    private lateinit var cardWeather: MaterialCardView

    // Weather views (home card)
    private lateinit var tvWeatherTitle: TextView
    private lateinit var tvWeatherLocation: TextView
    private lateinit var tvWeatherTemp: TextView
    private lateinit var tvWeatherCondition: TextView
    private lateinit var tvWeatherHumidity: TextView
    private lateinit var tvWeatherWind: TextView
    private lateinit var tvWeatherUpdated: TextView
    private lateinit var tvWeatherPermissionHint: TextView

    // last known to open details screen
    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var lastPlaceName: String? = null

    private var isPermissionDialogOpen = false

    // ✅ Permission launcher (safe)
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->

            isPermissionDialogOpen = false

            val granted = (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                    (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)

            // if activity views not ready (rare edge), return safely
            if (!::tvWeatherPermissionHint.isInitialized || !::tvWeatherLocation.isInitialized) return@registerForActivityResult

            if (granted) {
                tvWeatherPermissionHint.visibility = View.GONE
                loadWeatherNearYou()
            } else {
                tvWeatherPermissionHint.visibility = View.VISIBLE
                tvWeatherLocation.text = label("weather_location_permission_denied", "Location permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Check login
        val user = SharedPrefsHelper.getUser(this)
        if (user?.email.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // ✅ Language
        lang = SharedPrefsHelper.getLanguage(this) ?: "en"
        if (lang.isBlank()) lang = "en"

        initializeViews()

        // UI setups
        setDynamicTexts()
        setupNavigation()
        setupFloatingActionButton()
        setupChatbotCard()

        // Weather
        setWeatherTextsForLanguage()
        setupWeatherCardClick()
        loadWeatherNearYou()
    }

    override fun onResume() {
        super.onResume()

        bottomNavigation.selectedItemId = R.id.nav_home

        // ✅ re-read language each time you return
        val newLang = SharedPrefsHelper.getLanguage(this) ?: "en"
        if (newLang != lang) {
            lang = newLang

            // ✅ re-apply UI translations
            setDynamicTexts()
            setWeatherTextsForLanguage()
            setHomeCardTextsForLanguage()

            // ✅ refresh weather text too
            loadWeatherNearYou()
        } else {
            // still safe to refresh card texts
            setHomeCardTextsForLanguage()
        }
    }

    private fun setHomeCardTextsForLanguage() {
        // ✅ Weather card texts (if you have separate title/subtitle on card)
        runCatching {
            findViewById<TextView>(R.id.tv_weather_title)?.text =
                label("weather_title", "Weather near you")
        }

        runCatching {
            findViewById<TextView>(R.id.tv_chatbot_title)?.text =
                label("chat_title", "Chat Support")

            findViewById<TextView>(R.id.tv_chatbot_subtitle)?.text =
                label("chat_subtitle", "Ask your farming query")
        }

        // ✅ If you have button text on card like "Ask Now"
        runCatching {
            findViewById<TextView>(R.id.tv_chatbot_voice)?.text =
                label("chat_voice", "Voice")

            findViewById<TextView>(R.id.tv_chatbot_speaker)?.text =
                label("chat_speaker", "Speaker")
        }
    }

    private fun initializeViews() {
        fabQuickScan = findViewById(R.id.fab_quick_scan)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        cardAskChatbot = findViewById(R.id.card_ask_chatbot)
        cardWeather = findViewById(R.id.card_weather)

        tvWeatherTitle = findViewById(R.id.tv_weather_title)
        tvWeatherLocation = findViewById(R.id.tv_weather_location)
        tvWeatherTemp = findViewById(R.id.tv_weather_temp)
        tvWeatherCondition = findViewById(R.id.tv_weather_condition)
        tvWeatherHumidity = findViewById(R.id.tv_weather_humidity)
        tvWeatherWind = findViewById(R.id.tv_weather_wind)
        tvWeatherUpdated = findViewById(R.id.tv_weather_updated)
        tvWeatherPermissionHint = findViewById(R.id.tv_weather_permission_hint)
    }

    private fun setupChatbotCard() {
        cardAskChatbot.setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }
    }

    private fun setupNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_categories -> {
                    startActivity(Intent(this, CategoryActivity::class.java))
                    false
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun setupFloatingActionButton() {
        fabQuickScan.setOnClickListener {
            try {
                SharedPrefsHelper.addHistoryItem(this, "Opened Scan Now", R.drawable.ic_scan)
                startActivity(Intent(this, ScanActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening scanner: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupWeatherCardClick() {
        cardWeather.setOnClickListener {
            val hasPermission = hasLocationPermission()
            if (!hasPermission) {
                Toast.makeText(this, label("weather_permission_needed", "Please allow location to view forecast"), Toast.LENGTH_SHORT).show()
                requestLocationPermissionOnce()
                return@setOnClickListener
            }

            val lat = lastLat
            val lon = lastLon
            if (lat == null || lon == null) {
                Toast.makeText(this, label("weather_wait_location", "Getting location… please try again"), Toast.LENGTH_SHORT).show()
                loadWeatherNearYou()
                return@setOnClickListener
            }

            startActivity(
                Intent(this, WeatherDetailsActivity::class.java).apply {
                    putExtra("lat", lat)
                    putExtra("lon", lon)
                    putExtra("placeName", lastPlaceName ?: tvWeatherLocation.text.toString())
                    putExtra("lang", lang)
                }
            )
        }
    }

    // ✅ Tip translations (your existing keys)
    private fun setDynamicTexts() {
        val tips = listOf(
            R.id.card_tips_water_soil_title to "card_tips_water_soil_title",
            R.id.card_tips_water_soil_tip1 to "card_tips_water_soil_tip1",
            R.id.card_tips_water_soil_tip2 to "card_tips_water_soil_tip2",
            R.id.card_tips_sun_nutrition_title to "card_tips_sun_nutrition_title",
            R.id.card_tips_sun_nutrition_tip1 to "card_tips_sun_nutrition_tip1",
            R.id.card_tips_sun_nutrition_tip2 to "card_tips_sun_nutrition_tip2",
            R.id.card_tips_pests_hygiene_title to "card_tips_pests_hygiene_title",
            R.id.card_tips_pests_hygiene_tip1 to "card_tips_pests_hygiene_tip1",
            R.id.card_tips_pests_hygiene_tip2 to "card_tips_pests_hygiene_tip2",
            R.id.card_tips_harvest_storage_title to "card_tips_harvest_storage_title",
            R.id.card_tips_harvest_storage_tip1 to "card_tips_harvest_storage_tip1",
            R.id.card_tips_harvest_storage_tip2 to "card_tips_harvest_storage_tip2"
        )

        tips.forEach { (viewId, translationKey) ->
            try {
                val textView = findViewById<TextView>(viewId)
                val translatedText = DynamicTranslationHelper.getTranslation(lang, translationKey)

                val finalText =
                    if (translatedText.isNullOrBlank() || translatedText == translationKey) {
                        val resId = resources.getIdentifier(translationKey, "string", packageName)
                        if (resId != 0) getString(resId) else translationKey
                    } else translatedText

                textView.text = finalText
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting text for $translationKey: ${e.message}", e)
            }
        }
    }

    // =========================
    // ✅ WEATHER (HOME CARD)
    // =========================

    private fun setWeatherTextsForLanguage() {
        tvWeatherTitle.text = label("weather_title", "Weather near you")
        tvWeatherLocation.text = label("weather_location_detecting", "Detecting location…")
        tvWeatherPermissionHint.text = label("weather_permission_hint", "Enable location to show weather.")
    }

    private fun label(key: String, fallback: String): String {
        val translated = DynamicTranslationHelper.getTranslation(lang, key)
        return if (translated.isNullOrBlank() || translated == key) fallback else translated
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return (fine == PackageManager.PERMISSION_GRANTED) || (coarse == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermissionOnce() {
        if (isPermissionDialogOpen) return
        isPermissionDialogOpen = true
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun getCityNameFromLatLng(lat: Double, lon: Double): String {
        return try {
            val locale = when (lang) {
                "hi" -> Locale("hi", "IN")
                "mr" -> Locale("mr", "IN")
                "bn" -> Locale("bn", "IN")
                "ta" -> Locale("ta", "IN")
                "te" -> Locale("te", "IN")
                else -> Locale.ENGLISH
            }

            val geocoder = Geocoder(this, locale)
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses.isNullOrEmpty()) return ""

            val a = addresses[0]
            listOfNotNull(a.subLocality, a.locality, a.subAdminArea, a.adminArea)
                .distinct()
                .joinToString(", ")
        } catch (e: Exception) {
            Log.e("MainActivity", "Geocoder failed: ${e.message}")
            ""
        }
    }

    // ✅ FIX: Lint doesn't understand custom permission checks, so we suppress here.
    @SuppressLint("MissingPermission")
    private fun loadWeatherNearYou() {
        if (!hasLocationPermission()) {
            tvWeatherPermissionHint.visibility = View.VISIBLE
            requestLocationPermissionOnce()
            return
        }

        tvWeatherPermissionHint.visibility = View.GONE

        // 1) Google fused (if available)
        try {
            val gmsStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            if (gmsStatus == ConnectionResult.SUCCESS) {
                val fused = LocationServices.getFusedLocationProviderClient(this)

                fused.lastLocation
                    .addOnSuccessListener { loc ->
                        if (loc != null) updateWeatherFromLocation(loc)
                        else {
                            val fallbackLoc = getLastKnownLocationFallback()
                            if (fallbackLoc != null) updateWeatherFromLocation(fallbackLoc)
                            else tvWeatherLocation.text = label("weather_location_unavailable", "Location not available")
                        }
                    }
                    .addOnFailureListener {
                        val fallbackLoc = getLastKnownLocationFallback()
                        if (fallbackLoc != null) updateWeatherFromLocation(fallbackLoc)
                        else tvWeatherLocation.text = label("weather_location_error", "Location error")
                    }
                return
            }
        } catch (e: NoClassDefFoundError) {
            Log.e("MainActivity", "GMS classes missing: ${e.message}")
        } catch (e: Exception) {
            Log.e("MainActivity", "GMS error: ${e.message}", e)
        }

        // 2) Fallback LocationManager
        val fallbackLoc = getLastKnownLocationFallback()
        if (fallbackLoc != null) updateWeatherFromLocation(fallbackLoc)
        else {
            tvWeatherPermissionHint.visibility = View.VISIBLE
            tvWeatherLocation.text = label("weather_gms_missing", "Location service not available (weather disabled)")
        }
    }

    // ✅ FIX: Lint warning on lm.getLastKnownLocation(...) even though called after permission check.
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocationFallback(): Location? {
        return try {
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = lm.getProviders(true)
            var best: Location? = null
            for (p in providers) {
                val loc = lm.getLastKnownLocation(p) ?: continue
                if (best == null || loc.accuracy < best!!.accuracy) best = loc
            }
            best
        } catch (_: Exception) {
            null
        }
    }

    private fun updateWeatherFromLocation(loc: Location) {
        val lat = loc.latitude
        val lon = loc.longitude

        lastLat = lat
        lastLon = lon

        tvWeatherLocation.text = label("weather_location_detecting", "Detecting location…")

        Thread {
            val placeName = getCityNameFromLatLng(lat, lon)
            lastPlaceName = placeName
            runOnUiThread {
                tvWeatherLocation.text =
                    if (placeName.isNotBlank()) placeName
                    else "${label("weather_location", "Location")}: ${"%.2f".format(lat)}, ${"%.2f".format(lon)}"
            }
        }.start()

        // ✅ Home card shows today + tomorrow only
        fetchWeatherOpenMeteoTodayTomorrow(lat, lon)
    }

    private fun fetchWeatherOpenMeteoTodayTomorrow(lat: Double, lon: Double) {
        Thread {
            try {
                val urlStr =
                    "https://api.open-meteo.com/v1/forecast" +
                            "?latitude=$lat&longitude=$lon" +
                            "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code" +
                            "&daily=temperature_2m_max,temperature_2m_min,weather_code" +
                            "&forecast_days=2" +
                            "&timezone=auto"

                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                val current = json.getJSONObject("current")
                val tempNow = current.getDouble("temperature_2m")
                val humidity = current.optInt("relative_humidity_2m", -1)
                val wind = current.getDouble("wind_speed_10m")
                val nowCode = current.optInt("weather_code", 0)

                val daily = json.getJSONObject("daily")
                val codes = daily.getJSONArray("weather_code")
                val tMax = daily.getJSONArray("temperature_2m_max")
                val tMin = daily.getJSONArray("temperature_2m_min")

                val todayCode = if (codes.length() > 0) codes.getInt(0) else nowCode
                val tomorrowCode = if (codes.length() > 1) codes.getInt(1) else nowCode

                val todayMax = if (tMax.length() > 0) tMax.getDouble(0) else tempNow
                val todayMin = if (tMin.length() > 0) tMin.getDouble(0) else tempNow
                val tomorrowMax = if (tMax.length() > 1) tMax.getDouble(1) else todayMax
                val tomorrowMin = if (tMin.length() > 1) tMin.getDouble(1) else todayMin

                val todayText = translateWeatherCondition(weatherCodeToKey(todayCode))
                val tomorrowText = translateWeatherCondition(weatherCodeToKey(tomorrowCode))

                val updated = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                runOnUiThread {
                    tvWeatherTemp.text = "${tempNow.toInt()}°C"

                    tvWeatherCondition.text =
                        "${label("today_label", "Today")}: $todayText (${todayMin.toInt()}°-${todayMax.toInt()}°)  |  " +
                                "${label("tomorrow_label", "Tomorrow")}: $tomorrowText (${tomorrowMin.toInt()}°-${tomorrowMax.toInt()}°)"

                    tvWeatherHumidity.text =
                        "${label("humidity_label", "Humidity")}: ${if (humidity < 0) "--" else humidity}%"

                    tvWeatherWind.text =
                        "${label("wind_label", "Wind")}: ${wind.toInt()} km/h"

                    tvWeatherUpdated.text =
                        "${label("updated_label", "Updated")}: $updated"
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Weather error: ${e.message}", e)
                runOnUiThread {
                    tvWeatherCondition.text = label("weather_unavailable", "Weather unavailable")
                }
            }
        }.start()
    }

    private fun translateWeatherCondition(key: String): String {
        val translated = DynamicTranslationHelper.getTranslation(lang, key)
        if (!translated.isNullOrBlank() && translated != key) return translated

        return when (key) {
            "weather_clear" -> "Clear sky"
            "weather_cloudy" -> "Cloudy"
            "weather_fog" -> "Fog"
            "weather_rain" -> "Rain"
            "weather_thunder" -> "Thunderstorm"
            "weather_snow" -> "Snow"
            else -> "Weather"
        }
    }

    private fun weatherCodeToKey(code: Int): String {
        return when (code) {
            0 -> "weather_clear"
            1, 2, 3 -> "weather_cloudy"
            45, 48 -> "weather_fog"
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> "weather_rain"
            71, 73, 75, 77, 85, 86 -> "weather_snow"
            95, 96, 99 -> "weather_thunder"
            else -> "weather_cloudy"
        }
    }
}