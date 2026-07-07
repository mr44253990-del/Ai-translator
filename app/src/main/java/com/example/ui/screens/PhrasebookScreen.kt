package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.PhraseItem
import com.example.viewmodel.PhrasebookState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhrasebookScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state by viewModel.phrasebookState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    
    var selectedLanguage by remember { mutableStateOf("Bengali") }
    var selectedCategory by remember { mutableStateOf("Greetings") }
    var langExpanded by remember { mutableStateOf(false) }

    val languages = listOf("Bengali", "Arabic", "Hindi", "Urdu", "Spanish", "French", "German")
    val categories = listOf("Greetings", "Dining", "Travel", "Shopping", "Emergency")
    
    val clipboardManager = LocalClipboardManager.current

    // Trigger phrase generation whenever category or language shifts
    LaunchedEffect(selectedCategory, selectedLanguage) {
        viewModel.generatePhrases(selectedCategory, selectedLanguage)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Travel Phrasebook", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.generatePhrases(selectedCategory, selectedLanguage) }) {
                        Icon(Icons.Default.Refresh, "Refresh Phrases")
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
        ) {
            // Configuration Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Target Language:", style = MaterialTheme.typography.titleSmall)
                        }
                        Box {
                            Button(
                                onClick = { langExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(selectedLanguage)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(
                                expanded = langExpanded,
                                onDismissRequest = { langExpanded = false }
                            ) {
                                languages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang) },
                                        onClick = {
                                            selectedLanguage = lang
                                            langExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Categories", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    
                    // Horizontal scrollable categories row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = category == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Connectivity Indicator Bar
            Surface(
                color = if (isOnline) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.AutoAwesome else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isOnline) "Generating smart phrases using Mistral AI." else "Offline fallback: Showing static essential phrases.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Body Area (Phrases list or Loader)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (val result = state) {
                    is PhrasebookState.Idle -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Please select category and target language.")
                        }
                    }
                    is PhrasebookState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text("Generating custom authentic travel phrases...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    is PhrasebookState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(result.phrases) { phrase ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = phrase.original,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = phrase.translation,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (phrase.pronunciation.isNotBlank()) {
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = "Pronunciation: ${phrase.pronunciation}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        
                                        Spacer(Modifier.height(8.dp))
                                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.speak(phrase.translation, selectedLanguage.take(2).lowercase()) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Speak translation",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            IconButton(
                                                onClick = { clipboardManager.setText(AnnotatedString(phrase.translation)) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy translation",
                                                    tint = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is PhrasebookState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Oops! Failed to load phrases.", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(result.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.generatePhrases(selectedCategory, selectedLanguage) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}
