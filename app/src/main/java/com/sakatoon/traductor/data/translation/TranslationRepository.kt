package com.sakatoon.traductor.data.translation

import kotlinx.coroutines.flow.StateFlow

interface TranslationRepository {
    val translationState: StateFlow<TranslationState>

    suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String)

    suspend fun downloadModel(languageCode: String, requireWifi: Boolean = true)

    suspend fun isModelDownloaded(languageCode: String): Boolean

    suspend fun getDownloadedModels(): List<String>

    suspend fun deleteModel(languageCode: String)
}
