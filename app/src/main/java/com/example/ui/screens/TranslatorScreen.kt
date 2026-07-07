package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var textToTranslate by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf("EnToBn") } // EnToBn or BnToEn

    val isReady by viewModel.translatorReady.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()
    val identifiedLanguage by viewModel.identifiedLanguage.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLanguageId()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Translator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isDownloading) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Downloading translation models...")
                    }
                }
            } else if (!isReady) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Model not ready. Please check internet connection to download initially.")
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = direction == "EnToBn",
                    onClick = { direction = "EnToBn"; translatedText = "" },
                    label = { Text("English to Bangla") }
                )
                FilterChip(
                    selected = direction == "BnToEn",
                    onClick = { direction = "BnToEn"; translatedText = "" },
                    label = { Text("Bangla to English") }
                )
            }

            OutlinedTextField(
                value = textToTranslate,
                onValueChange = { 
                    textToTranslate = it
                    viewModel.identifyLanguage(it)
                },
                label = { Text("Enter text") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            if (identifiedLanguage.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Detected Language Code: $identifiedLanguage",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Button(
                onClick = {
                    if (direction == "EnToBn") {
                        viewModel.translateEnToBn(textToTranslate) { translatedText = it }
                    } else {
                        viewModel.translateBnToEn(textToTranslate) { translatedText = it }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isReady && textToTranslate.isNotBlank()
            ) {
                Text("Translate")
            }

            if (translatedText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Translation:", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(translatedText, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ML Kit Language Identification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "ML Kit can identify over 100 languages offline. Tap below to see the complete list of supported languages and scripts.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            uriHandler.openUri("https://developers.google.com/ml-kit/language/identification/langid-support")
                        }
                    ) {
                        Text("View Supported Languages")
                    }
                }
            }
        }
    }
}
