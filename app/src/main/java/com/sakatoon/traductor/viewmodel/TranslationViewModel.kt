package com.sakatoon.traductor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakatoon.traductor.data.SettingsRepository
import com.sakatoon.traductor.data.speech.SpeechState
import com.sakatoon.traductor.data.speech.SpeechToTextManager
import com.sakatoon.traductor.data.translation.TranslationRepository
import com.sakatoon.traductor.data.translation.TranslationState
import com.sakatoon.traductor.data.tts.TextToSpeechManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TranslationViewModel(
    private val repository: TranslationRepository,
    private val sttManager: SpeechToTextManager,
    private val ttsManager: TextToSpeechManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _showFirstLaunchDialog = MutableStateFlow(false)
    val showFirstLaunchDialog: StateFlow<Boolean> = _showFirstLaunchDialog.asStateFlow()

    private val _sourceText = MutableStateFlow("")
    val sourceText: StateFlow<String> = _sourceText.asStateFlow()

    private val _sourceLang = MutableStateFlow("en")
    val sourceLang: StateFlow<String> = _sourceLang.asStateFlow()

    private val _targetLang = MutableStateFlow("es")
    val targetLang: StateFlow<String> = _targetLang.asStateFlow()

    val translationState = repository.translationState
    val speechState = sttManager.state
    val ttsState = ttsManager.state

    init {
        viewModelScope.launch {
            settingsRepository.isFirstLaunch.collect { isFirst ->
                if (isFirst) {
                    _showFirstLaunchDialog.value = true
                }
            }
        }
        viewModelScope.launch {
            sttManager.state.collectLatest { state ->
                if (state is SpeechState.FinalResult) {
                    _sourceText.value = state.text
                    translate()
                } else if (state is SpeechState.PartialResult) {
                    _sourceText.value = state.text
                }
            }
        }
    }

    fun onSourceTextChange(text: String) {
        _sourceText.value = text
    }

    fun swapLanguages() {
        val temp = _sourceLang.value
        _sourceLang.value = _targetLang.value
        _targetLang.value = temp
        translate()
    }

    fun setSourceLang(lang: String) {
        _sourceLang.value = lang
        translate()
    }

    fun setTargetLang(lang: String) {
        _targetLang.value = lang
        translate()
    }

    fun translate() {
        viewModelScope.launch {
            repository.translate(_sourceText.value, _sourceLang.value, _targetLang.value)
        }
    }

    fun startListening() {
        sttManager.startListening(_sourceLang.value)
    }

    fun stopListening() {
        sttManager.stopListening()
    }

    fun speak(text: String, lang: String) {
        ttsManager.speak(text, lang)
    }

    fun dismissFirstLaunchDialog() {
        _showFirstLaunchDialog.value = false
        viewModelScope.launch {
            settingsRepository.setFirstLaunchCompleted()
        }
    }

    override fun onCleared() {
        super.onCleared()
        sttManager.release()
        ttsManager.shutdown()
    }
}
