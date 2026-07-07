package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.DictionaryState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var textToLookup by remember { mutableStateOf("") }
    val dictionaryState by viewModel.dictionaryState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val aiModel by viewModel.aiModel.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Smart Dictionary", fontWeight = FontWeight.Bold) },
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
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isOnline) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            if (isOnline) "Online AI Mode (${aiModel.uppercase()})" else "Offline Local Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            if (isOnline) "Deep AI lookup for meanings, grammar, and synonyms." else "Using local translation for meanings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Text Input Field
            OutlinedTextField(
                value = textToLookup,
                onValueChange = { textToLookup = it },
                label = { Text("Enter a word or sentence") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5
            )

            // Lookup Action Button
            Button(
                onClick = { viewModel.generateDictionaryLookup(textToLookup) },
                modifier = Modifier.fillMaxWidth(),
                enabled = textToLookup.isNotBlank() && dictionaryState !is DictionaryState.Loading
            ) {
                if (dictionaryState is DictionaryState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Looking up...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lookup Definition")
                }
            }

            // Results Section
            when (val state = dictionaryState) {
                is DictionaryState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Result:", 
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(state.result))
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Result copied to clipboard")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy text")
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = state.result,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
                is DictionaryState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
