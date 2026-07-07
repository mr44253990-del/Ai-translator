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
import java.util.Locale

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

    // Dynamic Translator Model States
    private val _downloadedModels = MutableStateFlow<Set<String>>(setOf(TranslateLanguage.ENGLISH))
    val downloadedModels = _downloadedModels.asStateFlow()

    private val _modelDownloadingState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val modelDownloadingState = _modelDownloadingState.asStateFlow()

    // AI Summarizer State
    private val _aiSummaryState = MutableStateFlow<AiSummaryState>(AiSummaryState.Idle)
    val aiSummaryState = _aiSummaryState.asStateFlow()

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
        if (_ttsReady.value) {
            val locale = if (languageCode == "bn") Locale("bn", "BD") else Locale.US
            if (_selectedVoiceName.value.isEmpty()) {
                textToSpeech?.language = locale
            }
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            saveHistory("TTS", text, languageCode)
        }
    }
    
    fun stopSpeaking() {
        textToSpeech?.stop()
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

    // --- Dynamic Offline NLP Summarizer with Translation ---
    fun generateAiSummary(text: String, targetLanguage: String) {
        viewModelScope.launch {
            _aiSummaryState.value = AiSummaryState.Loading
            try {
                // Perform local NLP TF-IDF text summarization (completely offline, zero API call/GGUF size)
                val summary = Text2Summary.summarize(text, compressionFactor = 0.5f)
                
                if (summary.isBlank()) {
                    _aiSummaryState.value = AiSummaryState.Success("No content to summarize.")
                    return@launch
                }

                // If target language is Bangla, translate the summary
                if (targetLanguage == "Bangla") {
                    // Identify text language first
                    val languageIdentifier = com.google.mlkit.nl.languageid.LanguageIdentification.getClient()
                    languageIdentifier.identifyLanguage(summary)
                        .addOnSuccessListener { identifiedCode ->
                            val sourceCode = if (identifiedCode == "und" || identifiedCode == "bn") {
                                TranslateLanguage.ENGLISH // fallback
                            } else {
                                identifiedCode
                            }
                            
                            if (sourceCode == TranslateLanguage.BENGALI) {
                                _aiSummaryState.value = AiSummaryState.Success(summary)
                                saveHistory("AI_SUMMARY", text, summary)
                            } else {
                                translateText(summary, sourceCode, TranslateLanguage.BENGALI) { translatedSummary ->
                                    _aiSummaryState.value = AiSummaryState.Success(translatedSummary)
                                    saveHistory("AI_SUMMARY", text, translatedSummary)
                                }
                            }
                        }
                        .addOnFailureListener {
                            translateText(summary, TranslateLanguage.ENGLISH, TranslateLanguage.BENGALI) { translatedSummary ->
                                _aiSummaryState.value = AiSummaryState.Success(translatedSummary)
                                saveHistory("AI_SUMMARY", text, translatedSummary)
                            }
                        }
                } else {
                    // Target language is English
                    val languageIdentifier = com.google.mlkit.nl.languageid.LanguageIdentification.getClient()
                    languageIdentifier.identifyLanguage(summary)
                        .addOnSuccessListener { identifiedCode ->
                            val sourceCode = if (identifiedCode == "und") {
                                TranslateLanguage.BENGALI // fallback
                            } else {
                                identifiedCode
                            }
                            
                            if (sourceCode == TranslateLanguage.ENGLISH) {
                                _aiSummaryState.value = AiSummaryState.Success(summary)
                                saveHistory("AI_SUMMARY", text, summary)
                            } else {
                                translateText(summary, sourceCode, TranslateLanguage.ENGLISH) { translatedSummary ->
                                    _aiSummaryState.value = AiSummaryState.Success(translatedSummary)
                                    saveHistory("AI_SUMMARY", text, translatedSummary)
                                }
                            }
                        }
                        .addOnFailureListener {
                            translateText(summary, TranslateLanguage.BENGALI, TranslateLanguage.ENGLISH) { translatedSummary ->
                                _aiSummaryState.value = AiSummaryState.Success(translatedSummary)
                                saveHistory("AI_SUMMARY", text, translatedSummary)
                            }
                        }
                }
            } catch (e: Exception) {
                _aiSummaryState.value = AiSummaryState.Error(e.message ?: "Unknown error")
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
