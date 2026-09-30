package com.velmora.vic.assistant

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * Optional Accessibility Service that can capture device gesture shortcuts
 * or key combinations (like double-tap or long-press power) on MIUI / HyperOS
 * if the OEM restricts default assistant routing.
 */
class VicAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "VicAccessibility"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Passive listener
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted.")
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        // Can optionally capture specific hardware button gestures
        return super.onKeyEvent(event)
    }

    fun triggerAssistantOverlay() {
        val intent = Intent(this, AssistActivity::class.java).apply {
            action = Intent.ACTION_ASSIST
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }
}
