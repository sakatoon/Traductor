package com.sakatoon.traductor.data.tts

sealed class TtsState {
    object Idle : TtsState()
    object Speaking : TtsState()
    data class Error(val message: String) : TtsState()
}
