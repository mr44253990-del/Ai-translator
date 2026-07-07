package com.example.ui.screens

import android.speech.tts.Voice
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.viewmodel.AppViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("en") } // "en" or "bn"
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val speed by viewModel.ttsSpeed.collectAsStateWithLifecycle()
    val pitch by viewModel.ttsPitch.collectAsStateWithLifecycle()
    val availableVoices by viewModel.availableVoices.collectAsStateWithLifecycle()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsStateWithLifecycle()

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("TTS Settings") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Speed: ${"%.1f".format(speed)}")
                    Slider(value = speed, onValueChange = { viewModel.setTtsSpeed(it) }, valueRange = 0.1f..2.0f)
                    
                    Text("Pitch: ${"%.1f".format(pitch)}")
                    Slider(value = pitch, onValueChange = { viewModel.setTtsPitch(it) }, valueRange = 0.1f..2.0f)
                    
                    Text("Voice Selection (Online/Offline):")
                    if (availableVoices.isEmpty()) {
                        Text("No custom voices available on this device.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedVoiceName.ifEmpty { "Default" },
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                availableVoices.take(10).forEach { voice -> // limit to 10 to avoid huge lists
                                    DropdownMenuItem(
                                        text = { Text("${voice.name} (${voice.locale.language})") },
                                        onClick = {
                                            viewModel.setTtsVoice(voice)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("Close") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Text to Speech") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter text to speak") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedLanguage == "en",
                    onClick = { selectedLanguage = "en" },
                    label = { Text("English") }
                )
                FilterChip(
                    selected = selectedLanguage == "bn",
                    onClick = { selectedLanguage = "bn" },
                    label = { Text("Bangla") }
                )
            }

            Text("Examples", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { text = "I'm Rakibul" }, label = { Text("I'm Rakibul") })
                AssistChip(onClick = { text = "Good morning Rakib" }, label = { Text("Good morning") })
                AssistChip(onClick = { text = "আমি রাকিবুল" }, label = { Text("আমি রাকিবুল") })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { viewModel.speak(text, selectedLanguage) }, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Speak")
                }
                OutlinedButton(onClick = { viewModel.stopSpeaking() }, modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Stop")
                }
            }
        }
    }
}
