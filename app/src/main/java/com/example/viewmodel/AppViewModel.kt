package com.example.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HistoryItem
import com.example.data.SettingsManager
import com.example.di.DatabaseProvider
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
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

    // ML Kit Translator
    private val _translatorReady = MutableStateFlow(false)
    val translatorReady = _translatorReady.asStateFlow()
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress = _downloadProgress.asStateFlow()
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    // AI Summarizer (Mocked ONNX)
    private val _aiSummaryState = MutableStateFlow<AiSummaryState>(AiSummaryState.Idle)
    val aiSummaryState = _aiSummaryState.asStateFlow()

    init {
        textToSpeech = TextToSpeech(application, this)
        checkAndDownloadTranslatorModel()
        
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
            // Only set language if we haven't selected a specific voice or if we want to force locale
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

    // --- ML Kit Translation ---
    private fun checkAndDownloadTranslatorModel() {
        _isDownloading.value = true
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.BENGALI)
            .build()
        val englishBengaliTranslator = Translation.getClient(options)
        
        var conditions = DownloadConditions.Builder().build()
        
        englishBengaliTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                _translatorReady.value = true
                _isDownloading.value = false
            }
            .addOnFailureListener {
                _isDownloading.value = false
            }
    }

    fun translateEnToBn(text: String, onResult: (String) -> Unit) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.BENGALI)
            .build()
        val translator = Translation.getClient(options)
        translator.translate(text)
            .addOnSuccessListener {
                saveHistory("TRANSLATION", text, it)
                onResult(it)
            }
            .addOnFailureListener { onResult("Error translating") }
    }
    
    fun translateBnToEn(text: String, onResult: (String) -> Unit) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.BENGALI)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val translator = Translation.getClient(options)
        translator.translate(text)
            .addOnSuccessListener {
                saveHistory("TRANSLATION", text, it)
                onResult(it)
            }
            .addOnFailureListener { onResult("Error translating") }
    }

    // --- AI Summarizer (Mocked ONNX for CPU) ---
    fun generateAiSummary(text: String, targetLanguage: String) {
        viewModelScope.launch {
            _aiSummaryState.value = AiSummaryState.Loading
            kotlinx.coroutines.delay(2000) // Simulate offline CPU inference
            try {
                // Since we cannot package a 500MB GGUF/ONNX model in the browser emulator,
                // we mock the offline inference output to demonstrate the UI works as requested.
                val resultText = if (targetLanguage == "Bangla") {
                    "অফলাইন এআই মডেল (ONNX) থেকে সারসংক্ষেপ: এটি একটি পরীক্ষামূলক টেক্সট।"
                } else {
                    "Offline AI Model (ONNX) Summary: This is an experimental text."
                }
                _aiSummaryState.value = AiSummaryState.Success(resultText)
                saveHistory("AI_SUMMARY", text, resultText)
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
