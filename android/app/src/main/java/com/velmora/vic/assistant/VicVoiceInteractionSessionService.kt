package com.velmora.vic.assistant

import android.content.Context
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService

/**
 * Session service that creates VoiceInteractionSession instances for VIC.
 */
class VicVoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return VicVoiceInteractionSession(this)
    }
}
