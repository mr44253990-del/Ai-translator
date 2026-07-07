package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AppViewModel
import com.google.mlkit.nl.translate.TranslateLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var textToTranslate by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    
    // Default translation pair: English to Bengali
    var sourceLang by remember { mutableStateOf(TranslateLanguage.ENGLISH) }
    var targetLang by remember { mutableStateOf(TranslateLanguage.BENGALI) }

    val downloadedModels by viewModel.downloadedModels.collectAsStateWithLifecycle()
    val downloadingState by viewModel.modelDownloadingState.collectAsStateWithLifecycle()
    val identifiedLanguage by viewModel.identifiedLanguage.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    val languages = remember {
        listOf(
            TranslateLanguage.ENGLISH to "English",
            TranslateLanguage.BENGALI to "Bengali / বাংলা",
            TranslateLanguage.ARABIC to "Arabic / العربية",
            TranslateLanguage.HINDI to "Hindi / हिन्दी",
            TranslateLanguage.URDU to "Urdu / اردو",
            TranslateLanguage.SPANISH to "Spanish / Español",
            TranslateLanguage.FRENCH to "French / Français",
            TranslateLanguage.CHINESE to "Chinese / 中文",
            TranslateLanguage.JAPANESE to "Japanese / 日本語",
            TranslateLanguage.RUSSIAN to "Russian / Русский",
            TranslateLanguage.GERMAN to "German / Deutsch"
        )
    }

    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    val isSourceDownloaded = downloadedModels.contains(sourceLang)
    val isTargetDownloaded = downloadedModels.contains(targetLang)

    val isSourceDownloading = downloadingState[sourceLang] == true
    val isTargetDownloading = downloadingState[targetLang] == true

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
            
            // Language Selection Selectors with Swap
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Source Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            onClick = { sourceExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("From", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        languages.find { it.first == sourceLang }?.second ?: sourceLang,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = sourceExpanded,
                            onDismissRequest = { sourceExpanded = false }
                        ) {
                            languages.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(name)
                                            if (downloadedModels.contains(code)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Downloaded", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        sourceLang = code
                                        sourceExpanded = false
                                        translatedText = ""
                                    }
                                )
                            }
                        }
                    }

                    // Swap Button
                    IconButton(
                        onClick = {
                            val temp = sourceLang
                            sourceLang = targetLang
                            targetLang = temp
                            translatedText = ""
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap languages")
                    }

                    // Target Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            onClick = { targetExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("To", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        languages.find { it.first == targetLang }?.second ?: targetLang,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = targetExpanded,
                            onDismissRequest = { targetExpanded = false }
                        ) {
                            languages.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(name)
                                            if (downloadedModels.contains(code)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Downloaded", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        targetLang = code
                                        targetExpanded = false
                                        translatedText = ""
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Model Download Indicators (Arabic, Hindi, etc.)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isSourceDownloaded) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f))) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${languages.find { it.first == sourceLang }?.second} model is required for offline translation.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Button(
                                onClick = { viewModel.downloadLanguageModel(sourceLang) },
                                enabled = !isSourceDownloading,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                if (isSourceDownloading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Download", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                if (!isTargetDownloaded && targetLang != sourceLang) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f))) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${languages.find { it.first == targetLang }?.second} model is required for offline translation.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Button(
                                onClick = { viewModel.downloadLanguageModel(targetLang) },
                                enabled = !isTargetDownloading,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                if (isTargetDownloading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Download", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Enter text input field
            OutlinedTextField(
                value = textToTranslate,
                onValueChange = { 
                    textToTranslate = it
                    viewModel.identifyLanguage(it)
                },
                label = { Text("Enter text to translate") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
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

            // Translate action button
            Button(
                onClick = {
                    viewModel.translateText(textToTranslate, sourceLang, targetLang) { translatedText = it }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isSourceDownloaded && isTargetDownloaded && textToTranslate.isNotBlank()
            ) {
                Text("Translate Offline")
            }

            // Results Card
            if (translatedText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Translation:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(translatedText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Informational Box about Language Identification
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
                        "ML Kit can identify over 100 languages offline. Selected translation languages are downloaded on-demand and kept securely on your device for absolute privacy.",
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
