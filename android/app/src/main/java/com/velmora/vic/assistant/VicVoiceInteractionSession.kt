package com.velmora.vic.assistant

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log

/**
 * Handles active voice interaction sessions when user triggers assistant gestures.
 */
class VicVoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {

    companion object {
        private const val TAG = "VicVoiceSession"
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        Log.i(TAG, "Voice interaction session triggered with flags: $showFlags")

        // Launch AssistActivity overlay
        val intent = Intent(context, AssistActivity::class.java).apply {
            action = Intent.ACTION_ASSIST
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            args?.let { putExtras(it) }
        }
        context.startActivity(intent)
        finish()
    }

    override fun onHandleAssist(
        data: Bundle?,
        structure: AssistStructure?,
        content: AssistContent?
    ) {
        super.onHandleAssist(data, structure, content)
        Log.d(TAG, "Assist context received.")
    }
}
