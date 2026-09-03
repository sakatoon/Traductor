package com.sakatoon.traductor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sakatoon.traductor.data.SettingsRepository
import com.sakatoon.traductor.data.speech.SpeechToTextManager
import com.sakatoon.traductor.data.translation.TranslationRepositoryImpl
import com.sakatoon.traductor.data.tts.TextToSpeechManager
import com.sakatoon.traductor.ui.screens.AboutScreen
import com.sakatoon.traductor.ui.screens.MainScreen
import com.sakatoon.traductor.ui.screens.SettingsScreen
import com.sakatoon.traductor.ui.theme.TraductorTheme
import com.sakatoon.traductor.viewmodel.SettingsViewModel
import com.sakatoon.traductor.viewmodel.TranslationViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val repository = TranslationRepositoryImpl()
        val settingsRepository = SettingsRepository(this)
        val sttManager = SpeechToTextManager(this)
        val ttsManager = TextToSpeechManager(this)

        setContent {
            val isDarkMode by settingsRepository.isDarkMode.collectAsState(initial = true)

            TraductorTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        val viewModel: TranslationViewModel = viewModel(
                            factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return TranslationViewModel(repository, sttManager, ttsManager, settingsRepository) as T
                                }
                            }
                        )
                        MainScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }
                    composable("settings") {
                        val viewModel: SettingsViewModel = viewModel(
                            factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return SettingsViewModel(repository, settingsRepository) as T
                                }
                            }
                        )
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToAbout = { navController.navigate("about") }
                        )
                    }
                    composable("about") {
                        AboutScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
