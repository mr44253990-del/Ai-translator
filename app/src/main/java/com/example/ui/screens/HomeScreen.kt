package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToTts: () -> Unit,
    onNavigateToOcr: () -> Unit,
    onNavigateToTranslate: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDictionary: () -> Unit,
    onNavigateToGrammar: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToPhrasebook: () -> Unit
) {
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    // Dynamic gradient shift for the Hero card
    val infiniteTransition = rememberInfiniteTransition(label = "hero_gradient")
    val animatedFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_shift"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primary
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f * (1f + animatedFloat), 1000f * (1f - animatedFloat))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Offline AI Translator", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onNavigateToFeedback) {
                        Icon(Icons.Default.Feedback, contentDescription = "Feedback")
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Hero Section with dynamic gradient shifting
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(gradientBrush)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "Offline Translator & AI",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Powerful tools that work without internet.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    )
                }
            }
            
            Text(
                "Features",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    DashboardCard(
                        title = "Text to Speech",
                        description = "Listen to text offline",
                        icon = Icons.Default.RecordVoiceOver,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onNavigateToTts,
                        delay = 50
                    )
                }
                item {
                    DashboardCard(
                        title = "Image to Text",
                        description = "Extract text from images",
                        icon = Icons.Default.DocumentScanner,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = onNavigateToOcr,
                        delay = 100
                    )
                }
                item {
                    DashboardCard(
                        title = "Translator",
                        description = "En-Bn offline translation",
                        icon = Icons.Default.Translate,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = onNavigateToTranslate,
                        delay = 150
                    )
                }
                item {
                    DashboardCard(
                        title = "AI Summarizer",
                        description = "Smart AI Summarizer",
                        icon = Icons.Default.AutoAwesome,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = onNavigateToAi,
                        delay = 200
                    )
                }
                item {
                    DashboardCard(
                        title = "AI Dictionary",
                        description = "Smart grammar & lookup",
                        icon = Icons.Default.MenuBook,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = onNavigateToDictionary,
                        delay = 250
                    )
                }
                item {
                    DashboardCard(
                        title = "Grammar & Polish",
                        description = "Fix and rewrite text",
                        icon = Icons.Default.AutoFixHigh,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = onNavigateToGrammar,
                        delay = 300
                    )
                }
                item {
                    DashboardCard(
                        title = "AI Chat Partner",
                        description = "Practice foreign speaking",
                        icon = Icons.Default.Forum,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onNavigateToChat,
                        delay = 350
                    )
                }
                item {
                    DashboardCard(
                        title = "Travel Phrasebook",
                        description = "AI authentic traveler phrases",
                        icon = Icons.Default.Map,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = onNavigateToPhrasebook,
                        delay = 400
                    )
                }
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    var startFeedbackAnim by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(350)
                        startFeedbackAnim = true
                    }
                    val feedbackAlpha by animateFloatAsState(
                        targetValue = if (startFeedbackAnim) 1f else 0f,
                        animationSpec = tween(500, easing = EaseOutQuad),
                        label = "feedback_alpha"
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                this.alpha = feedbackAlpha
                            }
                            .clickable { onNavigateToFeedback() },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Feedback,
                                    contentDescription = "Feedback",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Help Us Improve",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Submit offline translator feedback",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    description: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    delay: Int = 0
) {
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        startAnim = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutQuad),
        label = "card_alpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (startAnim) 0f else 60f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutBack),
        label = "card_translateY"
    )
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.88f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutBack),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translateY
                this.scaleX = scale
                this.scaleY = scale
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Column {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description, 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
