package com.example.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MistralMessage(
    val role: String,
    val content: String
)

@Serializable
data class MistralRequest(
    val model: String,
    val messages: List<MistralMessage>
)

@Serializable
data class MistralChoice(
    val message: MistralMessage
)

@Serializable
data class MistralResponse(
    val choices: List<MistralChoice>
)

class MistralService(private val apiKey: String) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
        }
    }

    suspend fun generateContent(model: String, prompt: String): String? {
        return try {
            val response: MistralResponse = client.post("https://api.mistral.ai/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(
                    MistralRequest(
                        model = model,
                        messages = listOf(MistralMessage("user", prompt))
                    )
                )
            }.body()
            response.choices.firstOrNull()?.message?.content
        } catch (e: Exception) {
            null
        }
    }
}
