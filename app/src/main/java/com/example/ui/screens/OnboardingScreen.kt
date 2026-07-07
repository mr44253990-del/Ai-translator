package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val textEn: String,
    val textBn: String,
    val speechText: String,
    val icon: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(viewModel: AppViewModel, onFinish: () -> Unit) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "পরিচিতি ও উদ্দেশ্য",
                textEn = "Developed by Rakibul — Built for 100% private, offline-first on-device operations.",
                textBn = "আসসালামু আলাইকুম, আমি রকিবুল (Rakibul)। এই অফলাইন ট্রান্সলেটর এবং এআই সামারাইজার অ্যাপ্লিকেশনটি তৈরি করেছি যাতে আপনারা সম্পূর্ণ ইন্টারনেট ছাড়াই যেকোনো সময় অনুবাদ এবং যেকোনো লেখার সহজ বাংলা সারসংক্ষেপ তৈরি করতে পারেন।",
                speechText = "আসসালামু আলাইকুম, আমি রকিবুল। এই অফলাইন ট্রান্সলেটর এবং এআই সামারাইজার অ্যাপ্লিকেশনটি তৈরি করেছি যাতে আপনারা সম্পূর্ণ ইন্টারনেট ছাড়াই যেকোনো সময় অনুবাদ এবং যেকোনো লেখার সহজ বাংলা সারসংক্ষেপ তৈরি করতে পারেন।",
                icon = "👋"
            ),
            OnboardingPageData(
                title = "ভাষা রূপান্তর ও টিটিএস",
                textEn = "Translate offline between Bangla, English, Arabic, Hindi, and more on-demand.",
                textBn = "এই অ্যাপে রয়েছে সম্পূর্ণ অফলাইন ট্রান্সলেশন ইঞ্জিন, যার মাধ্যমে বাংলা, ইংরেজি, আরবি, হিন্দি সহ আরো অনেক ভাষায় অনুবাদ সম্ভব। সাথে রয়েছে ভয়েস স্পিকার, যার মাধ্যমে যেকোনো লেখা সুন্দর বাংলা ও ইংরেজি কণ্ঠে শুনতে পারবেন।",
                speechText = "এই অ্যাপে রয়েছে সম্পূর্ণ অফলাইন ট্রান্সলেশন ইঞ্জিন, যার মাধ্যমে বাংলা, ইংরেজি, আরবি, হিন্দি সহ আরো অনেক ভাষায় অনুবাদ সম্ভব। সাথে রয়েছে ভয়েস স্পিকার, যার মাধ্যমে যেকোনো লেখা সুন্দর বাংলা ও ইংরেজি কণ্ঠে শুনতে পারবেন।",
                icon = "🗣️"
            ),
            OnboardingPageData(
                title = "অফলাইন এআই সামারাইজার",
                textEn = "No cloud. No APIs. Purely offline local TF-IDF text summarizer.",
                textBn = "এছাড়াও রয়েছে অফলাইন এআই টেক্সট সামারাইজার, যা সম্পূর্ণ অফলাইনে যেকোনো বড় লেখার গুরুত্বপূর্ণ অংশের নিখুঁত বাংলা ও ইংরেজি সারসংক্ষেপ তৈরি করে দেয়। চলুন, অ্যাপটি ব্যবহার শুরু করি!",
                speechText = "এছাড়াও রয়েছে অফলাইন এআই টেক্সট সামারাইজার, যা সম্পূর্ণ অফলাইনে যেকোনো বড় লেখার গুরুত্বপূর্ণ অংশের নিখুঁত বাংলা ও ইংরেজি সারসংক্ষেপ তৈরি করে দেয়। চলুন, অ্যাপটি ব্যবহার শুরু করি!",
                icon = "🤖"
            )
        )
    }

    // Stop speaking whenever page changes
    LaunchedEffect(pagerState.currentPage) {
        viewModel.stopSpeaking()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSpeaking()
        }
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                        )
                    }
                }

                // Action Button
                if (pagerState.currentPage == pages.size - 1) {
                    Button(
                        onClick = {
                            viewModel.setFirstLaunch(false)
                            onFinish()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("শুরু করুন", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                } else {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    ) {
                        Text("পরবর্তী", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Page Emoji Icon
                    Text(
                        text = page.icon,
                        fontSize = 80.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Title
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Bangla details card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = page.textBn,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Justify,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    // English subtitle/developer note
                    Text(
                        text = page.textEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 24.dp)
                    )

                    // Bangla Speech Narrator controls
                    ElevatedCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    viewModel.speak(page.speechText, "bn")
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Listen",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "বাংলায় বিস্তারিত শুনুন",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(16.dp))
                            IconButton(
                                onClick = {
                                    viewModel.stopSpeaking()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
