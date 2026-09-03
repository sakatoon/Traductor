package com.sakatoon.traductor.data.translation

sealed class TranslationState {
    object Idle : TranslationState()
    object ModelNotDownloaded : TranslationState()
    data class Downloading(val progress: Float) : TranslationState()
    object Ready : TranslationState()
    object Translating : TranslationState()
    data class Success(val translatedText: String) : TranslationState()
    data class Error(val message: String) : TranslationState()
}
