package com.example.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HistoryItem
import com.example.data.SettingsManager
import com.example.data.Text2Summary
import com.example.di.DatabaseProvider
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.example.BuildConfig
import com.example.data.MistralService
import com.example.util.NetworkMonitor
import java.util.Locale

sealed class GrammarState {
    object Idle : GrammarState()
    object Loading : GrammarState()
    data class Success(val result: String) : GrammarState()
    data class Error(val message: String) : GrammarState()
}

class AppViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val db = DatabaseProvider.getDatabase(application)
    private val historyDao = db.historyDao()
    private val settingsManager = SettingsManager(application)

    val allHistory: StateFlow<List<HistoryItem>> = historyDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Preferences (First launch and Theme)
    val firstLaunch = settingsManager.firstLaunchFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val appTheme = settingsManager.appThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "violet")

    fun setFirstLaunch(first: Boolean) {
        viewModelScope.launch {
            settingsManager.saveFirstLaunch(first)
        }
    }

    fun setAppTheme(theme: String) {
        viewModelScope.launch {
            settingsManager.saveAppTheme(theme)
        }
    }

    // TTS Settings
    private val _ttsSpeed = MutableStateFlow(1.0f)
    val ttsSpeed = _ttsSpeed.asStateFlow()

    private val _ttsPitch = MutableStateFlow(1.0f)
    val ttsPitch = _ttsPitch.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private val _ttsReady = MutableStateFlow(false)

    private val _availableVoices = MutableStateFlow<List<Voice>>(emptyList())
    val availableVoices = _availableVoices.asStateFlow()
    
    private val _selectedVoiceName = MutableStateFlow("")
    val selectedVoiceName = _selectedVoiceName.asStateFlow()

    private val _isTtsSpeaking = MutableStateFlow(false)
    val isTtsSpeaking = _isTtsSpeaking.asStateFlow()

    // Dynamic Translator Model States
    private val _downloadedModels = MutableStateFlow<Set<String>>(setOf(TranslateLanguage.ENGLISH))
    val downloadedModels = _downloadedModels.asStateFlow()

    private val _modelDownloadingState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val modelDownloadingState = _modelDownloadingState.asStateFlow()

    // AI Summarizer State
    private val _aiSummaryState = MutableStateFlow<AiSummaryState>(AiSummaryState.Idle)
    val aiSummaryState = _aiSummaryState.asStateFlow()

    // Dictionary State
    private val _dictionaryState = MutableStateFlow<DictionaryState>(DictionaryState.Idle)
    val dictionaryState = _dictionaryState.asStateFlow()

    // Grammar State
    private val _grammarState = MutableStateFlow<GrammarState>(GrammarState.Idle)
    val grammarState = _grammarState.asStateFlow()

    // Connectivity State
    private val networkMonitor = NetworkMonitor(application)
    val isOnline = networkMonitor.isOnline.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // AI Settings
    val aiModel = settingsManager.aiModelFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "mistral")

    val mistralApiKey = settingsManager.mistralApiKeyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "EBuoQ4q3D8IcTu1xixZcmf73hNI3tWcB")

    val mistralModel = settingsManager.mistralModelFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "mistral-small-latest")

    val streamingEnabled = settingsManager.streamingEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setAiModel(model: String) {
        viewModelScope.launch {
            settingsManager.saveAiModel(model)
        }
    }

    fun setMistralApiKey(apiKey: String) {
        viewModelScope.launch {
            settingsManager.saveMistralApiKey(apiKey)
        }
    }

    fun setMistralModel(model: String) {
        viewModelScope.launch {
            settingsManager.saveMistralModel(model)
        }
    }

    fun setStreamingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.saveStreamingEnabled(enabled)
        }
    }

    private var mistralService: MistralService? = null

    init {
        textToSpeech = TextToSpeech(application, this)
        checkDownloadedModels()
        // Guarantee Bengali model is ready/downloaded initially
        downloadLanguageModel(TranslateLanguage.BENGALI)
        
        viewModelScope.launch {
            _ttsSpeed.value = settingsManager.ttsSpeedFlow.first()
            _ttsPitch.value = settingsManager.ttsPitchFlow.first()
            _selectedVoiceName.value = settingsManager.ttsVoiceFlow.first()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _ttsReady.value = true
            textToSpeech?.setSpeechRate(_ttsSpeed.value)
            textToSpeech?.setPitch(_ttsPitch.value)
            
            textToSpeech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isTtsSpeaking.value = true
                }
                override fun onDone(utteranceId: String?) {
                    _isTtsSpeaking.value = false
                }
                override fun onError(utteranceId: String?) {
                    _isTtsSpeaking.value = false
                }
            })

            try {
                val voices = textToSpeech?.voices?.toList() ?: emptyList()
                _availableVoices.value = voices
                
                if (_selectedVoiceName.value.isNotEmpty()) {
                    val voiceToSet = voices.find { it.name == _selectedVoiceName.value }
                    if (voiceToSet != null) {
                        textToSpeech?.voice = voiceToSet
                    }
                }
            } catch (e: Exception) {
                // Some devices throw exceptions when getting voices
            }
        }
    }

    fun setTtsSpeed(speed: Float) {
        _ttsSpeed.value = speed
        textToSpeech?.setSpeechRate(speed)
        viewModelScope.launch { settingsManager.saveTtsSpeed(speed) }
    }

    fun setTtsPitch(pitch: Float) {
        _ttsPitch.value = pitch
        textToSpeech?.setPitch(pitch)
        viewModelScope.launch { settingsManager.saveTtsPitch(pitch) }
    }
    
    fun setTtsVoice(voice: Voice) {
        _selectedVoiceName.value = voice.name
        textToSpeech?.voice = voice
        viewModelScope.launch { settingsManager.saveTtsVoice(voice.name) }
    }

    fun speak(text: String, languageCode: String) {
        if (_ttsReady.value && text.isNotBlank()) {
            val locale = if (languageCode == "bn") Locale("bn", "BD") else Locale.US
            if (_selectedVoiceName.value.isEmpty()) {
                textToSpeech?.language = locale
            }
            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speak_id")
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "speak_id")
            saveHistory("TTS", text, languageCode)
        }
    }
    
    fun stopSpeaking() {
        textToSpeech?.stop()
        _isTtsSpeaking.value = false
    }

    // --- Dynamic ML Kit Translation & Model Downloads ---
    fun checkDownloadedModels() {
        val modelManager = RemoteModelManager.getInstance()
        val languagesToCheck = listOf(
            TranslateLanguage.BENGALI,
            TranslateLanguage.ARABIC,
            TranslateLanguage.HINDI,
            TranslateLanguage.URDU,
            TranslateLanguage.SPANISH,
            TranslateLanguage.FRENCH,
            TranslateLanguage.CHINESE,
            TranslateLanguage.JAPANESE,
            TranslateLanguage.RUSSIAN,
            TranslateLanguage.GERMAN
        )
        
        viewModelScope.launch {
            for (lang in languagesToCheck) {
                val model = TranslateRemoteModel.Builder(lang).build()
                modelManager.isModelDownloaded(model)
                    .addOnSuccessListener { isDownloaded ->
                        if (isDownloaded) {
                            _downloadedModels.value = _downloadedModels.value + lang
                        }
                    }
            }
        }
    }

    fun downloadLanguageModel(langCode: String, onComplete: (Boolean) -> Unit = {}) {
        if (_downloadedModels.value.contains(langCode)) {
            onComplete(true)
            return
        }
        _modelDownloadingState.value = _modelDownloadingState.value + (langCode to true)
        val model = TranslateRemoteModel.Builder(langCode).build()
        val modelManager = RemoteModelManager.getInstance()
        val conditions = DownloadConditions.Builder().build()
        
        modelManager.download(model, conditions)
            .addOnSuccessListener {
                _downloadedModels.value = _downloadedModels.value + langCode
                _modelDownloadingState.value = _modelDownloadingState.value + (langCode to false)
                onComplete(true)
            }
            .addOnFailureListener {
                _modelDownloadingState.value = _modelDownloadingState.value + (langCode to false)
                onComplete(false)
            }
    }

    fun translateText(
        text: String,
        sourceLangCode: String,
        targetLangCode: String,
        onResult: (String) -> Unit
    ) {
        // Ensure source model is downloaded
        downloadLanguageModel(sourceLangCode) { sourceOk ->
            if (!sourceOk) {
                onResult("Error: Source language model download failed")
                return@downloadLanguageModel
            }
            // Ensure target model is downloaded
            downloadLanguageModel(targetLangCode) { targetOk ->
                if (!targetOk) {
                    onResult("Error: Target language model download failed")
                    return@downloadLanguageModel
                }
                
                // Perform translation
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLangCode)
                    .setTargetLanguage(targetLangCode)
                    .build()
                val translator = Translation.getClient(options)
                
                translator.translate(text)
                    .addOnSuccessListener { translated ->
                        saveHistory("TRANSLATION", text, translated)
                        onResult(translated)
                    }
                    .addOnFailureListener { e ->
                        onResult("Error: ${e.localizedMessage}")
                    }
            }
        }
    }

    // Maintain backwards compatibility helper methods
    fun translateEnToBn(text: String, onResult: (String) -> Unit) {
        translateText(text, TranslateLanguage.ENGLISH, TranslateLanguage.BENGALI, onResult)
    }

    fun translateBnToEn(text: String, onResult: (String) -> Unit) {
        translateText(text, TranslateLanguage.BENGALI, TranslateLanguage.ENGLISH, onResult)
    }

    private suspend fun getAiResponse(prompt: String): String {
        return try {
            val key = mistralApiKey.value.ifBlank { "EBuoQ4q3D8IcTu1xixZcmf73hNI3tWcB" }
            if (key == "PLACEHOLDER" || key.contains("your_")) {
                return "Mistral API Key is not configured correctly."
            }
            val service = MistralService(key)
            service.generateContent(mistralModel.value, prompt) ?: "Mistral returned empty content."
        } catch (e: Exception) {
            e.printStackTrace()
            "AI Request failed: ${e.localizedMessage ?: "Unknown connection or verification error"}"
        }
    }

    // --- Hybrid NLP Summarizer (Online AI vs Offline Local) ---
    fun generateAiSummary(text: String, targetLanguage: String) {
        viewModelScope.launch {
            _aiSummaryState.value = AiSummaryState.Loading
            try {
                if (text.isBlank()) {
                    _aiSummaryState.value = AiSummaryState.Success("No content to summarize.")
                    return@launch
                }

                if (isOnline.value) {
                    val prompt = "Please summarize the following text completely. Also analyze grammar (tenses, pronouns). Return the result in $targetLanguage.\n\nText: $text"
                    val result = getAiResponse(prompt)
                    _aiSummaryState.value = AiSummaryState.Success(result)
                    saveHistory("AI_SUMMARY_ONLINE", text, result)
                } else {
                    // Offline local summarizer
                    val summary = Text2Summary.summarize(text, compressionFactor = 0.5f)
                    if (targetLanguage == "Bangla") {
                        translateText(summary, TranslateLanguage.ENGLISH, TranslateLanguage.BENGALI) { translated ->
                            val finalResult = "[Offline Mode]\nSummary: $translated\n\nNote: Grammatical analysis requires internet."
                            _aiSummaryState.value = AiSummaryState.Success(finalResult)
                            saveHistory("AI_SUMMARY_OFFLINE", text, finalResult)
                        }
                    } else {
                        val finalResult = "[Offline Mode]\nSummary: $summary\n\nNote: Grammatical analysis requires internet."
                        _aiSummaryState.value = AiSummaryState.Success(finalResult)
                        saveHistory("AI_SUMMARY_OFFLINE", text, finalResult)
                    }
                }
            } catch (e: Exception) {
                _aiSummaryState.value = AiSummaryState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
    
    // --- Hybrid Dictionary Lookup (Online AI vs Offline Local) ---
    fun generateDictionaryLookup(wordOrSentence: String) {
        viewModelScope.launch {
            _dictionaryState.value = DictionaryState.Loading
            try {
                if (wordOrSentence.isBlank()) {
                    _dictionaryState.value = DictionaryState.Success("No content.")
                    return@launch
                }

                if (isOnline.value) {
                    val prompt = """
                        Act as an advanced smart dictionary. Analyze the input: "$wordOrSentence"
                        Provide the response in the following structured format using headers:
                        
                        ### Summary Header
                        এখানে বিশ্লেষণটি বাংলা ও ইংরেজি উত্তর ভাষায় দেওয়া হলো:
                        
                        ### Input Text
                        "$wordOrSentence"
                        
                        ### Parts of Speech
                        (List each word with its category and Bengali explanation)
                        
                        ### Tense
                        (Mention the tense in Bengali and English)
                        
                        ### Meanings
                        (Bengali and English meanings clearly)
                        
                        ### Note
                        (A short concluding note)
                        
                        Ensure the output is clean and professional.
                    """.trimIndent()
                    val result = getAiResponse(prompt)
                    _dictionaryState.value = DictionaryState.Success(result)
                    saveHistory("DICTIONARY_ONLINE", wordOrSentence, result)
                } else {
                    // Offline: just translation
                    translateText(wordOrSentence, TranslateLanguage.ENGLISH, TranslateLanguage.BENGALI) { translatedEnToBn ->
                        translateText(wordOrSentence, TranslateLanguage.BENGALI, TranslateLanguage.ENGLISH) { translatedBnToEn ->
                            val finalResult = "[Offline Mode]\n" +
                                "English to Bangla: $translatedEnToBn\n" +
                                "Bangla to English: $translatedBnToEn\n\n" +
                                "Note: Advanced grammar analysis requires internet."
                            _dictionaryState.value = DictionaryState.Success(finalResult)
                            saveHistory("DICTIONARY_OFFLINE", wordOrSentence, finalResult)
                        }
                    }
                }
            } catch (e: Exception) {
                _dictionaryState.value = DictionaryState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // --- Grammar Checker (Online AI Only) ---
    fun generateGrammarFix(text: String, tone: String) {
        viewModelScope.launch {
            _grammarState.value = GrammarState.Loading
            try {
                if (text.isBlank()) {
                    _grammarState.value = GrammarState.Success("No content.")
                    return@launch
                }

                if (isOnline.value) {
                    val prompt = """
                        Review the following text for grammatical errors.
                        Rewrite it to be "$tone".
                        Show the original text, the rewritten text, and briefly explain the changes made.
                        
                        Text: "$text"
                    """.trimIndent()
                    val result = getAiResponse(prompt)
                    _grammarState.value = GrammarState.Success(result)
                    saveHistory("GRAMMAR_ONLINE", text, result)
                } else {
                    _grammarState.value = GrammarState.Error("Internet connection required for advanced grammar analysis.")
                }
            } catch (e: Exception) {
                _grammarState.value = GrammarState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // --- OCR ---
    private val _ocrState = MutableStateFlow<String>("")
    val ocrState = _ocrState.asStateFlow()

    fun performOcr(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val image = com.google.mlkit.vision.common.InputImage.fromFilePath(context, uri)
                val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        _ocrState.value = visionText.text
                        saveHistory("OCR", uri.toString(), visionText.text)
                    }
                    .addOnFailureListener { e ->
                        _ocrState.value = "Error: ${e.message}"
                    }
            } catch (e: Exception) {
                _ocrState.value = "Error loading image: ${e.message}"
            }
        }
    }
    
    fun clearOcr() { _ocrState.value = "" }

    // --- Language Identification ---
    private val _identifiedLanguage = MutableStateFlow<String>("")
    val identifiedLanguage = _identifiedLanguage.asStateFlow()

    fun identifyLanguage(text: String) {
        if (text.isBlank()) {
            _identifiedLanguage.value = ""
            return
        }
        val languageIdentifier = com.google.mlkit.nl.languageid.LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    _identifiedLanguage.value = "Unknown"
                } else {
                    _identifiedLanguage.value = languageCode
                }
            }
            .addOnFailureListener {
                _identifiedLanguage.value = "Error"
            }
    }

    fun clearLanguageId() {
        _identifiedLanguage.value = ""
    }

    // --- History ---
    fun saveHistory(type: String, source: String, result: String) {
        viewModelScope.launch {
            historyDao.insert(HistoryItem(type = type, sourceText = source, resultText = result))
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            historyDao.clearHistory()
        }
    }

    override fun onCleared() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onCleared()
    }
}

sealed class AiSummaryState {
    object Idle : AiSummaryState()
    object Loading : AiSummaryState()
    data class Success(val summary: String) : AiSummaryState()
    data class Error(val message: String) : AiSummaryState()
}

sealed class DictionaryState {
    object Idle : DictionaryState()
    object Loading : DictionaryState()
    data class Success(val result: String) : DictionaryState()
    data class Error(val message: String) : DictionaryState()
}
