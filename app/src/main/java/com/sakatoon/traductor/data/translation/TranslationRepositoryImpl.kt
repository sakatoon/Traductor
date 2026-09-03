package com.sakatoon.traductor.data.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

class TranslationRepositoryImpl : TranslationRepository {

    private val _translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)
    override val translationState: StateFlow<TranslationState> = _translationState.asStateFlow()

    private val modelManager = RemoteModelManager.getInstance()
    private val translators = ConcurrentHashMap<String, Translator>()

    override suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String) {
        if (text.isBlank()) {
            _translationState.value = TranslationState.Idle
            return
        }

        _translationState.value = TranslationState.Translating

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()

        val translator = translators.getOrPut("$sourceLanguage-$targetLanguage") {
            Translation.getClient(options)
        }

        try {
            // Check if models are downloaded
            val sourceModel = TranslateRemoteModel.Builder(sourceLanguage).build()
            val targetModel = TranslateRemoteModel.Builder(targetLanguage).build()

            val isSourceDownloaded = modelManager.isModelDownloaded(sourceModel).await()
            val isTargetDownloaded = modelManager.isModelDownloaded(targetModel).await()

            if (!isSourceDownloaded || !isTargetDownloaded) {
                _translationState.value = TranslationState.ModelNotDownloaded
                return
            }

            // Ensure they are ready
            translator.downloadModelIfNeeded().await()

            val translatedText = translator.translate(text).await()
            _translationState.value = TranslationState.Success(translatedText)
        } catch (e: Exception) {
            _translationState.value = TranslationState.Error(e.message ?: "Translation failed")
        }
    }

    override suspend fun downloadModel(languageCode: String, requireWifi: Boolean) {
        val model = TranslateRemoteModel.Builder(languageCode).build()
        val conditions = DownloadConditions.Builder()
            .run { if (requireWifi) requireWifi() else this }
            .build()

        _translationState.value = TranslationState.Downloading(0f)

        try {
            modelManager.download(model, conditions).await()
            _translationState.value = TranslationState.Ready
        } catch (e: Exception) {
            _translationState.value = TranslationState.Error(e.message ?: "Download failed")
        }
    }

    override suspend fun isModelDownloaded(languageCode: String): Boolean {
        val model = TranslateRemoteModel.Builder(languageCode).build()
        return modelManager.isModelDownloaded(model).await()
    }

    override suspend fun getDownloadedModels(): List<String> {
        return modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .await()
            .map { it.language }
    }

    override suspend fun deleteModel(languageCode: String) {
        val model = TranslateRemoteModel.Builder(languageCode).build()
        try {
            modelManager.deleteDownloadedModel(model).await()
            // Clear cached translators that use this language
            val keysToRemove = translators.keys.filter { it.contains(languageCode) }
            keysToRemove.forEach { key ->
                translators.remove(key)?.close()
            }
        } catch (e: Exception) {
            _translationState.value = TranslationState.Error(e.message ?: "Deletion failed")
        }
    }
}
