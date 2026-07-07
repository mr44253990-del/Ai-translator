package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
}
