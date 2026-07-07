package com.example.ui.screens

import android.speech.tts.Voice
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val currentTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val ttsSpeed by viewModel.ttsSpeed.collectAsStateWithLifecycle()
    val ttsPitch by viewModel.ttsPitch.collectAsStateWithLifecycle()
    val voices by viewModel.availableVoices.collectAsStateWithLifecycle()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsStateWithLifecycle()

    var voiceExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // 1. Theme Configuration
            Text(
                "App Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Select your preferred color scheme. The visual style will update instantly throughout the application.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ThemeOptionCard(
                            name = "Violet",
                            themeId = "violet",
                            color = Color(0xFF6650a4),
                            isSelected = currentTheme == "violet",
                            onClick = { viewModel.setAppTheme("violet") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            name = "Forest Green",
                            themeId = "green",
                            color = Color(0xFF059669),
                            isSelected = currentTheme == "green",
                            onClick = { viewModel.setAppTheme("green") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ThemeOptionCard(
                            name = "Sunset Gold",
                            themeId = "orange",
                            color = Color(0xFFEA580C),
                            isSelected = currentTheme == "orange",
                            onClick = { viewModel.setAppTheme("orange") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionCard(
                            name = "Cosmic Slate",
                            themeId = "slate",
                            color = Color(0xFF4F46E5),
                            isSelected = currentTheme == "slate",
                            onClick = { viewModel.setAppTheme("slate") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. TTS Configuration
            Text(
                "Text-To-Speech Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Speed rate slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Speech Speed Rate", style = MaterialTheme.typography.titleSmall)
                            Text(String.format("%.1fx", ttsSpeed), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = ttsSpeed,
                            onValueChange = { viewModel.setTtsSpeed(it) },
                            valueRange = 0.5f..2.5f,
                            steps = 19
                        )
                    }

                    // Pitch slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Speech Pitch Level", style = MaterialTheme.typography.titleSmall)
                            Text(String.format("%.1fx", ttsPitch), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = ttsPitch,
                            onValueChange = { viewModel.setTtsPitch(it) },
                            valueRange = 0.5f..2.0f,
                            steps = 14
                        )
                    }

                    // Voice list selection
                    if (voices.isNotEmpty()) {
                        Column {
                            Text("Speech Voice Profile", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedCard(
                                    onClick = { voiceExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            if (selectedVoiceName.isNotEmpty()) selectedVoiceName.take(30) else "Default Android Voice",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }

                                DropdownMenu(
                                    expanded = voiceExpanded,
                                    onDismissRequest = { voiceExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Default Android System Voice") },
                                        onClick = {
                                            viewModel.setTtsVoice(voices.first()) // backup / default
                                            voiceExpanded = false
                                        }
                                    )
                                    voices.take(15).forEach { voice ->
                                        DropdownMenuItem(
                                            text = { Text("${voice.locale.displayName} (${voice.name.takeLast(10)})") },
                                            onClick = {
                                                viewModel.setTtsVoice(voice)
                                                voiceExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. AI Configuration
            Text(
                "AI Configuration (Mistral & Gemini)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val currentAiModel by viewModel.aiModel.collectAsStateWithLifecycle()
            val mistralKey by viewModel.mistralApiKey.collectAsStateWithLifecycle()
            val geminiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
            val selectedMistral by viewModel.mistralModel.collectAsStateWithLifecycle()
            val selectedGemini by viewModel.geminiModel.collectAsStateWithLifecycle()

            var mistralKeyInput by remember { mutableStateOf("") }
            var geminiKeyInput by remember { mutableStateOf("") }
            var isVerifying by remember { mutableStateOf(false) }

            LaunchedEffect(mistralKey) {
                mistralKeyInput = mistralKey
            }
            LaunchedEffect(geminiKey) {
                geminiKeyInput = geminiKey
            }

            val context = androidx.compose.ui.platform.LocalContext.current

            val mistralOptions = listOf("mistral-tiny", "mistral-small-latest", "mistral-medium-latest", "mistral-large-latest", "open-mixtral-8x7b", "open-mixtral-8x22b")
            val geminiOptions = listOf("gemini-1.5-flash", "gemini-1.5-flash-8b", "gemini-1.5-pro", "gemini-2.0-flash", "gemma-2-2b-it", "gemma-2-9b-it", "gemma-2-27b-it")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Choose your preferred AI model for online summarization and dictionary lookup.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = currentAiModel == "gemini",
                            onClick = { viewModel.setAiModel("gemini") },
                            label = { Text("Gemini AI") },
                            leadingIcon = if (currentAiModel == "gemini") {
                                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentAiModel == "mistral",
                            onClick = { viewModel.setAiModel("mistral") },
                            label = { Text("Mistral AI") },
                            leadingIcon = if (currentAiModel == "mistral") {
                                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (currentAiModel == "gemini") {
                        ModelDropdownMenu(
                            options = geminiOptions,
                            selectedOption = selectedGemini,
                            onOptionSelected = { 
                                viewModel.setGeminiModel(it)
                                android.widget.Toast.makeText(context, "Gemini model updated to $it", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            label = "Select Gemini Model"
                        )
                        
                        OutlinedTextField(
                            value = geminiKeyInput,
                            onValueChange = { geminiKeyInput = it },
                            label = { Text("Gemini API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                        
                        Button(
                            onClick = {
                                isVerifying = true
                                viewModel.setGeminiApiKey(geminiKeyInput)
                                android.widget.Toast.makeText(context, "Gemini API Key saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                isVerifying = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isVerifying
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Save Gemini Config")
                            }
                        }
                    }

                    if (currentAiModel == "mistral") {
                        ModelDropdownMenu(
                            options = mistralOptions,
                            selectedOption = selectedMistral,
                            onOptionSelected = { 
                                viewModel.setMistralModel(it)
                                android.widget.Toast.makeText(context, "Mistral model updated to $it", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            label = "Select Mistral Model"
                        )

                        OutlinedTextField(
                            value = mistralKeyInput,
                            onValueChange = { mistralKeyInput = it },
                            label = { Text("Mistral API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                        
                        Button(
                            onClick = {
                                isVerifying = true
                                viewModel.setMistralApiKey(mistralKeyInput)
                                android.widget.Toast.makeText(context, "Mistral API Key saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                isVerifying = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isVerifying
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Save Mistral Config")
                            }
                        }
                    }
                }
            }

            // 4. Developer & App Info
            Text(
                "Developer & App Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Created By", style = MaterialTheme.typography.labelSmall)
                            Text("Rakibul (রকিবুল)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Privacy Guarantee", style = MaterialTheme.typography.labelSmall)
                            Text("Hybrid AI: Local processing + Secure Cloud AI", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Application Version", style = MaterialTheme.typography.labelSmall)
                            Text("v1.0.0 (rakib.translator.ai)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeOptionCard(
    name: String,
    themeId: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelDropdownMenu(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}