package com.velmora.vic.assistant

import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.util.Log

/**
 * System-level VoiceInteractionService declared to allow VIC to act as
 * the default digital assistant on Android.
 */
class VicVoiceInteractionService : VoiceInteractionService() {

    companion object {
        private const val TAG = "VicVoiceInteractionSvc"
    }

    override fun onReady() {
        super.onReady()
        Log.i(TAG, "VIC Voice Interaction Service is ready.")
    }

    override fun onShutdown() {
        super.onShutdown()
        Log.i(TAG, "VIC Voice Interaction Service shutting down.")
    }
}
