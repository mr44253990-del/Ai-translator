package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.DictionaryState

@Composable
fun StructuredAiResponse(rawResponse: String, onCopy: () -> Unit) {
    // Simple parser for ### headers
    val sections = rawResponse.split("###").filter { it.isNotBlank() }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sections.forEachIndexed { index, section ->
            val lines = section.trim().lines()
            val header = lines.firstOrNull()?.trim() ?: ""
            val content = lines.drop(1).joinToString("\n").trim()
            
            if (header.contains("Summary Header", ignoreCase = true)) {
                // Header with Stars (Blue/Dark theme)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = content.ifBlank { header },
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else if (header.contains("Input Text", ignoreCase = true)) {
                // Message Chip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(32.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = content,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                // Numbered Section Cards
                val (color, sectionName, icon) = when {
                    header.contains("Parts of Speech", ignoreCase = true) -> Triple(Color(0xFF2E7D32), "Parts of Speech", Icons.Default.Category)
                    header.contains("Tense", ignoreCase = true) -> Triple(Color(0xFF1565C0), "Tense", Icons.Default.Schedule)
                    header.contains("Meanings", ignoreCase = true) -> Triple(Color(0xFFC2185B), "Meanings", Icons.Default.Language)
                    else -> Triple(MaterialTheme.colorScheme.secondary, header, Icons.Default.Info)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
                ) {
                    Column {
                        // Section Header Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index).toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = sectionName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        // Content Area
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                .padding(16.dp)
                        ) {
                            if (header.contains("Tense", ignoreCase = true)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, null, tint = color, modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        text = content, 
                                        style = MaterialTheme.typography.bodyLarge, 
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                Text(
                                    text = content, 
                                    style = MaterialTheme.typography.bodyLarge, 
                                    lineHeight = 26.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Copy FAB or Button at the end
        OutlinedButton(
            onClick = onCopy,
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.ContentCopy, null)
            Spacer(Modifier.width(8.dp))
            Text("Copy Full Analysis")
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(viewModel: AppViewModel, initialText: String? = null, onBack: () -> Unit) {
    var textToLookup by remember { mutableStateOf(initialText ?: "") }
    
    // Auto-trigger if initialText is provided
    LaunchedEffect(initialText) {
        if (initialText != null) {
            viewModel.generateDictionaryLookup(initialText)
        }
    }
    
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
                    StructuredAiResponse(
                        rawResponse = state.result,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(state.result))
                            scope.launch {
                                snackbarHostState.showSnackbar("Result copied to clipboard")
                            }
                        }
                    )
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
