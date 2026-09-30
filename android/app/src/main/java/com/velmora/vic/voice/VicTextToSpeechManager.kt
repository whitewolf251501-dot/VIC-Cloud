package com.velmora.vic.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class VicTextToSpeechManager(
    private val context: Context,
    private val listener: TtsEventsListener
) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "VicTtsManager"
    }

    interface TtsEventsListener {
        fun onTtsStarted()
        fun onTtsCompleted()
        fun onTtsError(error: String)
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeechText: String? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            // Prefer Indian English voice
            val locale = Locale("en", "IN")
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            tts?.setPitch(0.95f) // Slightly authoritative, calm pitch
            tts?.setSpeechRate(1.05f) // Crisp, swift delivery

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    listener.onTtsStarted()
                }

                override fun onDone(utteranceId: String?) {
                    listener.onTtsCompleted()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    listener.onTtsError("TTS synthesis error")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    listener.onTtsError("TTS synthesis error code: $errorCode")
                }
            })

            pendingSpeechText?.let {
                speak(it)
                pendingSpeechText = null
            }
            Log.i(TAG, "TTS Engine Initialized successfully.")
        } else {
            isInitialized = false
            listener.onTtsError("TTS engine failed to initialize.")
        }
    }

    fun speak(text: String) {
        if (!isInitialized) {
            pendingSpeechText = text
            return
        }

        val utteranceId = "vic_speech_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        // Clean phonetic markdown/tags
        val sanitized = text.replace(Regex("[*#`_~]"), "").trim()
        tts?.speak(sanitized, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.w(TAG, "Error shutting down TTS: ${e.message}")
        } finally {
            tts = null
            isInitialized = false
        }
    }
}
