package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.ChatMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.chatLoading.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    
    var inputText by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("English") }
    var langExpanded by remember { mutableStateOf(false) }
    
    val languages = listOf("English", "Bengali", "Arabic", "Hindi", "Urdu", "Spanish", "French", "German")
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Auto-scroll to the bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Chat Partner", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isOnline) "Practice online with Mistral AI" else "Connect online to start chat",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Default.DeleteSweep, "Clear Chat")
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
            // Language Selection Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Practice Language:", style = MaterialTheme.typography.titleSmall)
                    }
                    Box {
                        Button(
                            onClick = { langExpanded = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
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
            }

            if (!isOnline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Connection Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "AI Chat practice requires an active internet connection to communicate with Mistral AI.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Chats list area
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Forum, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            "Start Practicing!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Send a greeting in $selectedLanguage (e.g. \"Hello!\") to start practicing speaking.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.isUser
                        val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                        val bubbleTextColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Column(
                                modifier = Modifier.widthIn(max = 280.dp),
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isUser) Icons.Default.Person else Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isUser) "You" else "AI Partner",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Card(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 2.dp,
                                        bottomEnd = if (isUser) 2.dp else 16.dp
                                    ),
                                    colors = CardDefaults.cardColors(containerColor = bubbleColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = bubbleTextColor
                                        )
                                        
                                        // Actions inside bubble for AI replies
                                        if (!isUser) {
                                            Spacer(Modifier.height(8.dp))
                                            Divider(color = bubbleTextColor.copy(alpha = 0.15f))
                                            Row(
                                                horizontalArrangement = Arrangement.End,
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { viewModel.speak(msg.text, selectedLanguage.take(2).lowercase()) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.VolumeUp,
                                                        contentDescription = "Speak",
                                                        tint = bubbleTextColor.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = { clipboardManager.setText(AnnotatedString(msg.text)) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy",
                                                        tint = bubbleTextColor.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("AI Partner is thinking...", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // Chat Input Bar
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Say something in $selectedLanguage...") },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            shape = RoundedCornerShape(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        FloatingActionButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    viewModel.sendMessage(inputText, selectedLanguage)
                                    inputText = ""
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}
