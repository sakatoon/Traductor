package com.sakatoon.traductor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import com.sakatoon.traductor.data.SettingsRepository
import com.sakatoon.traductor.data.translation.TranslationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: TranslationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _downloadedModels = MutableStateFlow<List<String>>(emptyList())
    val downloadedModels: StateFlow<List<String>> = _downloadedModels.asStateFlow()

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val availableLanguages = TranslateLanguage.getAllLanguages()

    init {
        refreshModels()
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(enabled)
        }
    }

    fun refreshModels() {
        viewModelScope.launch {
            _downloadedModels.value = repository.getDownloadedModels()
        }
    }

    fun downloadModel(lang: String) {
        viewModelScope.launch {
            repository.downloadModel(lang)
            refreshModels()
        }
    }

    fun deleteModel(lang: String) {
        viewModelScope.launch {
            repository.deleteModel(lang)
            refreshModels()
        }
    }
}
