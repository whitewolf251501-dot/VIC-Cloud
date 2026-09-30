package com.velmora.vic.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class VicSpeechManager(
    private val context: Context,
    private val listener: SpeechEventsListener
) {
    companion object {
        private const val TAG = "VicSpeechManager"
    }

    interface SpeechEventsListener {
        fun onSpeechListeningStarted()
        fun onSpeechRmsChanged(normalizedRms: Float)
        fun onSpeechRecognized(text: String, isFinal: Boolean)
        fun onSpeechError(errorMsg: String)
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var preferredLanguage: String = "en-IN" // Default Indian English; supports "hi-IN", "gu-IN"

    fun setLanguage(langCode: String) {
        preferredLanguage = langCode
    }

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        if (isListening) {
            stopListening()
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onSpeechError("Speech recognition service not available on device.")
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        listener.onSpeechListeningStarted()
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize dB from range [-2, 10] into [0.0, 1.0]
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        listener.onSpeechRmsChanged(normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                        listener.onSpeechRmsChanged(0f)
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        listener.onSpeechRmsChanged(0f)
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                            else -> "Recognition error ($error)"
                        }
                        Log.w(TAG, "SpeechRecognizer error: $message")
                        listener.onSpeechError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        listener.onSpeechRmsChanged(0f)
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val spokenText = matches[0]
                            Log.d(TAG, "Recognized result: $spokenText")
                            listener.onSpeechRecognized(spokenText, true)
                        } else {
                            listener.onSpeechError("No recognition result.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            listener.onSpeechRecognized(matches[0], false)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, preferredLanguage)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, preferredLanguage)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, preferredLanguage)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            isListening = false
            listener.onSpeechError("Failed to initiate voice recognition: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping recognizer: ${e.message}")
        } finally {
            speechRecognizer = null
            isListening = false
            listener.onSpeechRmsChanged(0f)
        }
    }
}
