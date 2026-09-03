package com.sakatoon.traductor.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val _state = MutableStateFlow<TtsState>(TtsState.Idle)
    val state: StateFlow<TtsState> = _state.asStateFlow()

    private var isInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupUtteranceProgressListener()
        } else {
            _state.value = TtsState.Error("Initialization failed with status: $status")
        }
    }

    private fun setupUtteranceProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _state.value = TtsState.Speaking
            }

            override fun onDone(utteranceId: String?) {
                _state.value = TtsState.Idle
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _state.value = TtsState.Error("Error during speech synthesis")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _state.value = TtsState.Error("Error during speech synthesis code: $errorCode")
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                _state.value = TtsState.Idle
            }
        })
    }

    fun speak(text: String, languageCode: String) {
        if (!isInitialized) {
            _state.value = TtsState.Error("TTS not initialized yet")
            return
        }

        val locale = Locale.forLanguageTag(languageCode)
        val availability = tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED

        when {
            availability == TextToSpeech.LANG_MISSING_DATA -> {
                _state.value = TtsState.Error("Missing voice data for $languageCode")
            }
            availability == TextToSpeech.LANG_NOT_SUPPORTED -> {
                _state.value = TtsState.Error("Language $languageCode not supported")
            }
            availability >= TextToSpeech.LANG_AVAILABLE -> {
                tts?.language = locale
                val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_UTTERANCE_ID")
                if (result == TextToSpeech.ERROR) {
                    _state.value = TtsState.Error("Failed to speak")
                }
            }
            else -> {
                _state.value = TtsState.Error("Language not available")
            }
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        _state.value = TtsState.Idle
    }
}
