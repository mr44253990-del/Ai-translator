package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.AiSummaryState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSummarizerScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var textToSummarize by remember { mutableStateOf("") }
    var targetLanguage by remember { mutableStateOf("Bangla") }

    val aiState by viewModel.aiSummaryState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Summarizer & Translator") },
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
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                PaddingValues(16.dp).let {
                    Text(
                        "Note: This feature uses the Gemini API and requires an internet connection.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            OutlinedTextField(
                value = textToSummarize,
                onValueChange = { textToSummarize = it },
                label = { Text("Enter paragraph or text") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Target Language: ")
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = targetLanguage == "Bangla",
                    onClick = { targetLanguage = "Bangla" },
                    label = { Text("Bangla") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = targetLanguage == "English",
                    onClick = { targetLanguage = "English" },
                    label = { Text("English") }
                )
            }

            Button(
                onClick = { viewModel.generateAiSummary(textToSummarize, targetLanguage) },
                modifier = Modifier.fillMaxWidth(),
                enabled = textToSummarize.isNotBlank() && aiState !is AiSummaryState.Loading
            ) {
                if (aiState is AiSummaryState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Translate & Summarize")
                }
            }

            when (val state = aiState) {
                is AiSummaryState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Summary:", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(state.summary, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                is AiSummaryState.Error -> {
                    Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                else -> {}
            }
        }
    }
}
