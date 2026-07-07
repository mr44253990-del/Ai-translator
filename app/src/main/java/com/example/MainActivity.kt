package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Collect the live user theme configuration
            val themeName by viewModel.appTheme.collectAsStateWithLifecycle()
            val firstLaunchState by viewModel.firstLaunch.collectAsStateWithLifecycle()

            MyApplicationTheme(themeName = themeName) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(navController = navController, startDestination = SplashRoute) {
                        composable<SplashRoute> {
                            SplashScreen(onNavigateToHome = {
                                if (firstLaunchState) {
                                    navController.navigate(OnboardingRoute) {
                                        popUpTo(SplashRoute) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(HomeRoute) {
                                        popUpTo(SplashRoute) { inclusive = true }
                                    }
                                }
                            })
                        }
                        composable<OnboardingRoute> {
                            OnboardingScreen(viewModel = viewModel, onFinish = {
                                navController.navigate(HomeRoute) {
                                    popUpTo(OnboardingRoute) { inclusive = true }
                                }
                            })
                        }
                        composable<HomeRoute> {
                            HomeScreen(
                                onNavigateToTts = { navController.navigate(TtsRoute) },
                                onNavigateToOcr = { navController.navigate(OcrRoute) },
                                onNavigateToTranslate = { navController.navigate(TranslatorRoute) },
                                onNavigateToAi = { navController.navigate(AiSummarizerRoute) },
                                onNavigateToHistory = { navController.navigate(HistoryRoute) },
                                onNavigateToFeedback = { navController.navigate(FeedbackRoute) },
                                onNavigateToSettings = { navController.navigate(SettingsRoute) }
                            )
                        }
                        composable<TtsRoute> {
                            TtsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable<OcrRoute> {
                            OcrScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable<TranslatorRoute> {
                            TranslatorScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable<AiSummarizerRoute> {
                            AiSummarizerScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable<HistoryRoute> {
                            HistoryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable<SettingsRoute> {
                            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable<FeedbackRoute> {
                            FeedbackScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
