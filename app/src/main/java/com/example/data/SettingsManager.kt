package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val TTS_SPEED = floatPreferencesKey("tts_speed")
        val TTS_PITCH = floatPreferencesKey("tts_pitch")
        val TTS_VOICE = stringPreferencesKey("tts_voice")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val APP_THEME = stringPreferencesKey("app_theme")
        val AI_MODEL = stringPreferencesKey("ai_model")
        val MISTRAL_API_KEY = stringPreferencesKey("mistral_api_key")
    }

    val aiModelFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[AI_MODEL] ?: "gemini"
    }

    val mistralApiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[MISTRAL_API_KEY] ?: com.example.BuildConfig.MISTRAL_API_KEY
    }

    suspend fun saveAiModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[AI_MODEL] = model
        }
    }

    suspend fun saveMistralApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[MISTRAL_API_KEY] = apiKey
        }
    }

    val ttsSpeedFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[TTS_SPEED] ?: 1.0f
    }

    val ttsPitchFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[TTS_PITCH] ?: 1.0f
    }

    val ttsVoiceFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TTS_VOICE] ?: ""
    }

    val firstLaunchFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FIRST_LAUNCH] ?: true
    }

    val appThemeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_THEME] ?: "violet"
    }

    suspend fun saveTtsSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[TTS_SPEED] = speed
        }
    }

    suspend fun saveTtsPitch(pitch: Float) {
        context.dataStore.edit { preferences ->
            preferences[TTS_PITCH] = pitch
        }
    }

    suspend fun saveTtsVoice(voiceName: String) {
        context.dataStore.edit { preferences ->
            preferences[TTS_VOICE] = voiceName
        }
    }

    suspend fun saveFirstLaunch(firstLaunch: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = firstLaunch
        }
    }

    suspend fun saveAppTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = themeName
        }
    }
}
