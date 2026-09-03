package com.sakatoon.traductor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import com.sakatoon.traductor.data.translation.TranslationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: TranslationRepository) : ViewModel() {

    private val _downloadedModels = MutableStateFlow<List<String>>(emptyList())
    val downloadedModels: StateFlow<List<String>> = _downloadedModels.asStateFlow()

    val availableLanguages = TranslateLanguage.getAllLanguages()

    init {
        refreshModels()
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
