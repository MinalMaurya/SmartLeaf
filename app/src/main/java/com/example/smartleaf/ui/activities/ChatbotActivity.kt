package com.example.smartleaf.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartleaf.R
import com.example.smartleaf.databinding.ActivityChatbotBinding
import com.example.smartleaf.ui.adapters.ChatAdapter
import com.example.smartleaf.ui.models.ChatMessage
import com.example.smartleaf.ui.utils.FaqMatcher
import com.example.smartleaf.ui.utils.FaqRepository
import com.example.smartleaf.ui.utils.SharedPrefsHelper
import java.util.Locale

class ChatbotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotBinding
    private lateinit var adapter: ChatAdapter

    // XML views (YOUR IDs)
    private lateinit var etMessage: EditText
    private lateinit var btnMicWrap: FrameLayout
    private lateinit var btnSpeakWrap: FrameLayout
    private lateinit var btnSendWrap: FrameLayout
    private lateinit var btnSendText: TextView

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var lastBotText: String = ""

    private val langCode: String by lazy { SharedPrefsHelper.getLanguage(this) ?: "en" }
    private val faqs by lazy { FaqRepository.loadFaqs(this, langCode) }

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startVoiceInput()
        else addBotMessage(getTextLocalized("mic_denied"))
    }

    private val voiceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@registerForActivityResult
        val matches =
            data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return@registerForActivityResult

        if (matches.isNotEmpty()) {
            etMessage.setText(matches[0])
            sendMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Recycler
        adapter = ChatAdapter(mutableListOf()) { msg ->
            if (!msg.isUser) speak(msg.text) // tap bot message to read
        }
        binding.rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.rvChat.adapter = adapter

        // Bind views (IMPORTANT: IDs must match your XML)
        etMessage = findViewById(R.id.etMessage)
        btnMicWrap = findViewById(R.id.btnMicWrap)
        btnSpeakWrap = findViewById(R.id.btnSpeakWrap)
        btnSendWrap = findViewById(R.id.btnSendWrap)
        btnSendText = findViewById(R.id.btnSend)

        // Set hint in selected language (optional)
        etMessage.hint = getTextLocalized("hint")

        initTts()

        // Prefill from FAQ card tap
        val preQ = intent.getStringExtra("prefill_q")
        val preA = intent.getStringExtra("prefill_a")
        if (!preQ.isNullOrBlank() && !preA.isNullOrBlank()) {
            addUserMessage(preQ)
            addBotMessage(preA)
        } else {
            addBotMessage(getTextLocalized("welcome"))
        }

        // ✅ Mic click
        btnMicWrap.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
            ) {
                startVoiceInput()
            } else {
                requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        // ✅ Speaker click (speak last bot message)
        btnSpeakWrap.setOnClickListener {
            if (lastBotText.isNotBlank()) speak(lastBotText)
        }

        // ✅ Send click (FrameLayout + TextView both trigger)
        val sendClick = View.OnClickListener { sendMessage() }
        btnSendWrap.setOnClickListener(sendClick)
        btnSendText.setOnClickListener(sendClick)

        // Optional: send when user presses Enter/Done
        etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }

    private fun sendMessage() {
        val text = etMessage.text?.toString()?.trim().orEmpty()
        if (text.isBlank()) return

        addUserMessage(text)
        etMessage.setText("")

        val best = FaqMatcher.findBestMatch(text, faqs)
        if (best != null) {
            addBotMessage(best.answer)
        } else {
            addBotMessage(getTextLocalized("fallback"))
        }
    }

    private fun addUserMessage(text: String) {
        adapter.add(ChatMessage(text = text, isUser = true))
        scrollBottom()
    }

    private fun addBotMessage(text: String) {
        lastBotText = text
        adapter.add(ChatMessage(text = text, isUser = false))
        scrollBottom()
    }

    private fun scrollBottom() {
        binding.rvChat.post {
            binding.rvChat.scrollToPosition(adapter.itemCount - 1)
        }
    }

    // --- Voice input (Speech-to-text) ---
    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCodeToLocaleTag(langCode))
            putExtra(RecognizerIntent.EXTRA_PROMPT, getTextLocalized("speak_now"))
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        voiceLauncher.launch(intent)
    }

    // --- TTS ---
    private fun initTts() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setTtsLanguage(langCode)
                ttsReady = true
            } else {
                ttsReady = false
            }
        }
    }

    private fun setTtsLanguage(code: String) {
        val locale = when (code) {
            "hi" -> Locale("hi", "IN")
            "mr" -> Locale("mr", "IN")
            "ta" -> Locale("ta", "IN")
            "te" -> Locale("te", "IN")
            "gu" -> Locale("gu", "IN")
            "bn" -> Locale("bn", "IN")
            "kn" -> Locale("kn", "IN")
            "ml" -> Locale("ml", "IN")
            "pa" -> Locale("pa", "IN")
            "ur" -> Locale("ur")
            else -> Locale("en", "US")
        }

        val res = tts?.setLanguage(locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale("en", "US"))
        }
        tts?.setSpeechRate(0.95f)
    }

    private fun speak(text: String) {
        if (!ttsReady) return
        val cleaned = text
            .replace("•", "")
            .replace("\n", ". ")
            .replace(Regex("\\s+"), " ")
            .trim()

        tts?.stop()
        tts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, "smartleaf_chat_tts")
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    // --- localized UI texts ---
    private fun getTextLocalized(key: String): String {
        return when (key) {
            "welcome" -> when (langCode) {
                "hi" -> "नमस्ते! आप कोई प्रश्न लिखें या माइक्रोफोन दबाकर बोलें।"
                "mr" -> "नमस्कार! प्रश्न टाइप करा किंवा माईक दाबून बोला."
                "ta" -> "வணக்கம்! உங்கள் கேள்வியை எழுதுங்கள் அல்லது மைக் அழுத்தி பேசுங்கள்."
                "te" -> "హలో! మీ ప్రశ్నను టైప్ చేయండి లేదా మైక్ నొక్కి మాట్లాడండి."
                "gu" -> "નમસ્તે! તમારો પ્રશ્ન લખો અથવા માઇક દબાવી બોલો."
                "bn" -> "নমস্কার! প্রশ্ন লিখুন বা মাইক চাপ দিয়ে বলুন।"
                "kn" -> "ನಮಸ್ಕಾರ! ಪ್ರಶ್ನೆಯನ್ನು ಟೈಪ್ ಮಾಡಿ ಅಥವಾ ಮೈಕ್ ಒತ್ತಿ ಮಾತನಾಡಿ."
                "ml" -> "ഹലോ! ചോദ്യം ടൈപ്പ് ചെയ്യുക അല്ലെങ്കിൽ മൈക്ക് അമർത്തി പറയുക."
                "pa" -> "ਸਤ ਸ੍ਰੀ ਅਕਾਲ! ਸਵਾਲ ਲਿਖੋ ਜਾਂ ਮਾਈਕ ਦਬਾ ਕੇ ਬੋਲੋ।"
                "ur" -> "السلام علیکم! سوال لکھیں یا مائیک دبا کر بولیں۔"
                else -> "Hi! Type your question or tap the mic to speak."
            }

            "fallback" -> when (langCode) {
                "hi" -> "मुझे इसका सटीक जवाब नहीं मिला। कृपया स्थानीय कृषि अधिकारी/कृषि विशेषज्ञ से सलाह लें।"
                "mr" -> "याचा अचूक उत्तर सापडले नाही. कृपया स्थानिक कृषी अधिकारी/तज्ञांचा सल्ला घ्या."
                "ta" -> "இதற்கு சரியான பதில் கிடைக்கவில்லை. அருகிலுள்ள வேளாண் நிபுணரை அணுகவும்."
                "te" -> "ఇందుకు ఖచ్చితమైన సమాధానం దొరకలేదు. స్థానిక వ్యవసాయ నిపుణుడిని సంప్రదించండి."
                "gu" -> "આ માટે ચોક્કસ જવાબ મળ્યો નથી. કૃપા કરીને સ્થાનિક કૃષિ નિષ્ણાતનો સંપર્ક કરો."
                "bn" -> "এর সঠিক উত্তর পাইনি। স্থানীয় কৃষি বিশেষজ্ঞের সাথে কথা বলুন।"
                "kn" -> "ಇದಕ್ಕೆ ಖಚಿತ ಉತ್ತರ ಸಿಗಲಿಲ್ಲ. ದಯವಿಟ್ಟು ಸ್ಥಳೀಯ ಕೃಷಿ ತಜ್ಞರನ್ನು ಸಂಪರ್ಕಿಸಿ."
                "ml" -> "കൃത്യമായ മറുപടി കണ്ടെത്താനായില്ല. അടുത്തുള്ള കാർഷിക വിദഗ്ധനെ ബന്ധപ്പെടുക."
                "pa" -> "ਇਸਦਾ ਪੱਕਾ ਜਵਾਬ ਨਹੀਂ ਮਿਲਿਆ। ਕਿਰਪਾ ਕਰਕੇ ਸਥਾਨਕ ਖੇਤੀ ਮਾਹਿਰ ਨਾਲ ਸੰਪਰਕ ਕਰੋ।"
                "ur" -> "مجھے اس کا درست جواب نہیں ملا۔ براہ کرم مقامی زرعی ماہر سے رابطہ کریں۔"
                else -> "I couldn’t find an exact answer. Please contact a local agriculture expert."
            }

            "speak_now" -> when (langCode) {
                "hi" -> "अब बोलें…"
                "mr" -> "आता बोला…"
                "ta" -> "இப்போது பேசுங்கள்…"
                "te" -> "ఇప్పుడు మాట్లాడండి…"
                "gu" -> "હવે બોલો…"
                "bn" -> "এখন বলুন…"
                "kn" -> "ಈಗ ಮಾತನಾಡಿ…"
                "ml" -> "ഇപ്പോൾ സംസാരിക്കുക…"
                "pa" -> "ਹੁਣ ਬੋਲੋ…"
                "ur" -> "اب بولیں…"
                else -> "Speak now…"
            }

            "mic_denied" -> when (langCode) {
                "hi" -> "माइक्रोफोन अनुमति नहीं मिली।"
                "mr" -> "माईक परवानगी मिळाली नाही."
                "ta" -> "மைக்ரோஃபோன் அனுமதி இல்லை."
                "te" -> "మైక్ అనుమతి లేదు."
                "gu" -> "માઇક પરવાનગી નથી."
                "bn" -> "মাইক অনুমতি নেই।"
                "kn" -> "ಮೈಕ್ ಅನುಮತಿ ಇಲ್ಲ."
                "ml" -> "മൈക്ക് അനുവാദം ഇല്ല."
                "pa" -> "ਮਾਈਕ ਦੀ ਆਗਿਆ ਨਹੀਂ।"
                "ur" -> "مائیک کی اجازت نہیں ملی۔"
                else -> "Mic permission not granted."
            }

            "hint" -> when (langCode) {
                "hi" -> "अपना सवाल लिखें…"
                "mr" -> "आपला प्रश्न लिहा…"
                "ta" -> "உங்கள் கேள்வியை எழுதுங்கள்…"
                "te" -> "మీ ప్రశ్న టైప్ చేయండి…"
                "gu" -> "તમારો પ્રશ્ન લખો…"
                "bn" -> "আপনার প্রশ্ন লিখুন…"
                "kn" -> "ನಿಮ್ಮ ಪ್ರಶ್ನೆ ಬರೆಯಿರಿ…"
                "ml" -> "നിങ്ങളുടെ ചോദ്യം ടൈപ്പ് ചെയ്യുക…"
                "pa" -> "ਆਪਣਾ ਸਵਾਲ ਲਿਖੋ…"
                "ur" -> "اپنا سوال لکھیں…"
                else -> "Type your question…"
            }

            else -> ""
        }
    }

    private fun langCodeToLocaleTag(code: String): String {
        return when (code) {
            "hi" -> "hi-IN"
            "mr" -> "mr-IN"
            "ta" -> "ta-IN"
            "te" -> "te-IN"
            "gu" -> "gu-IN"
            "bn" -> "bn-IN"
            "kn" -> "kn-IN"
            "ml" -> "ml-IN"
            "pa" -> "pa-IN"
            "ur" -> "ur"
            else -> "en-US"
        }
    }
}