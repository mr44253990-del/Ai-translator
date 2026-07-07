package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object OnboardingRoute

@Serializable
object HomeRoute

@Serializable
data class TtsRoute(val initialText: String? = null)

@Serializable
data class OcrRoute(val initialText: String? = null)

@Serializable
data class TranslatorRoute(val initialText: String? = null)

@Serializable
data class AiSummarizerRoute(val initialText: String? = null)

@Serializable
object SettingsRoute

@Serializable
object HistoryRoute

@Serializable
object FeedbackRoute

@Serializable
data class DictionaryRoute(val initialText: String? = null)

@Serializable
data class GrammarRoute(val initialText: String? = null)
