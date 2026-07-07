package com.example.ui.screens

import android.speech.tts.Voice
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AudioWaveformVisualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    
    val heights = listOf(
        infiniteTransition.animateFloat(
            initialValue = 12f,
            targetValue = 65f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "h1"
        ),
        infiniteTransition.animateFloat(
            initialValue = 20f,
            targetValue = 95f,
            animationSpec = infiniteRepeatable(
                animation = tween(350, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "h2"
        ),
        infiniteTransition.animateFloat(
            initialValue = 15f,
            targetValue = 80f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "h3"
        ),
        infiniteTransition.animateFloat(
            initialValue = 28f,
            targetValue = 90f,
            animationSpec = infiniteRepeatable(
                animation = tween(380, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "h4"
        ),
        infiniteTransition.animateFloat(
            initialValue = 10f,
            targetValue = 55f,
            animationSpec = infiniteRepeatable(
                animation = tween(420, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "h5"
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isPlaying) {
            heights.forEach { anim ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .width(8.dp)
                        .height(anim.value.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        } else {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .width(8.dp)
                        .height(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsScreen(viewModel: AppViewModel, initialText: String? = null, onBack: () -> Unit) {
    var text by remember { mutableStateOf(initialText ?: "") }
    var selectedLanguage by remember { mutableStateOf("en") } // "en" or "bn"
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val speed by viewModel.ttsSpeed.collectAsStateWithLifecycle()
    val pitch by viewModel.ttsPitch.collectAsStateWithLifecycle()
    val availableVoices by viewModel.availableVoices.collectAsStateWithLifecycle()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isTtsSpeaking.collectAsStateWithLifecycle()

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("TTS Config Settings") },
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
                title = { Text("Dynamic Text to Speech", fontWeight = FontWeight.Bold) },
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
            
            // Audio Wave Visualization
            AudioWaveformVisualizer(isPlaying = isSpeaking)

            // Input Field
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter text to speak") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8
            )

            // Language Selector Chips
            Text("Select Language:", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedLanguage == "en",
                    onClick = { selectedLanguage = "en" },
                    label = { Text("English") },
                    leadingIcon = if (selectedLanguage == "en") {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null
                )
                FilterChip(
                    selected = selectedLanguage == "bn",
                    onClick = { selectedLanguage = "bn" },
                    label = { Text("Bangla") },
                    leadingIcon = if (selectedLanguage == "bn") {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null
                )
            }

            // Quick Example Chips
            Text("Quick Examples:", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { 
                        text = "Good morning! My name is Rakib." 
                        selectedLanguage = "en"
                    }, 
                    label = { Text("Good morning (EN)") },
                    leadingIcon = { Icon(Icons.Default.ChatBubbleOutline, null, Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = { 
                        text = "শুভ সকাল, আমি রাকিবুল।" 
                        selectedLanguage = "bn"
                    }, 
                    label = { Text("শুভ সকাল (BN)") },
                    leadingIcon = { Icon(Icons.Default.ChatBubbleOutline, null, Modifier.size(16.dp)) }
                )
            }

            // Playback Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.speak(text, selectedLanguage) }, 
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    enabled = text.isNotBlank()
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Speak")
                }
                
                OutlinedButton(
                    onClick = { viewModel.stopSpeaking() }, 
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    enabled = isSpeaking
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Stop")
                }
            }
        }
    }
}
