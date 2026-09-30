package com.velmora.vic.assistant

import android.content.Intent
import android.speech.RecognitionService
import android.util.Log

/**
 * Standard RecognitionService hook declared in AndroidManifest.
 */
class VicRecognitionService : RecognitionService() {

    companion object {
        private const val TAG = "VicRecognitionService"
    }

    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        Log.d(TAG, "RecognitionService onStartListening")
    }

    override fun onCancel(listener: Callback?) {
        Log.d(TAG, "RecognitionService onCancel")
    }

    override fun onStopListening(listener: Callback?) {
        Log.d(TAG, "RecognitionService onStopListening")
    }
}
