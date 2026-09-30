package com.velmora.vic.assistant

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * Diagnostic helper to detect and verify whether VIC is the default digital assistant,
 * and provide direct deep links into Android / MIUI settings.
 */
object AssistantDiagnosticHelper {
    private const val TAG = "AssistantDiagnostic"

    data class AssistantVerificationReport(
        val isDefaultAssistant: Boolean,
        val activeAssistantComponent: String,
        val canHandleAssistIntent: Boolean,
        val voiceInteractionDeclared: Boolean,
        val roleAvailable: Boolean
    )

    /**
     * Determines whether this application or its VoiceInteractionService is
     * selected as the device's Default Digital Assistant.
     */
    fun isDefaultAssistant(context: Context): Boolean {
        // Method 1: Android Q+ RoleManager check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
                val isHeld = roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT)
                Log.d(TAG, "RoleManager.isRoleHeld(ROLE_ASSISTANT): $isHeld")
                if (isHeld) return true
            }
        }

        // Method 2: Settings.Secure.ASSISTANT check
        try {
            val assistantSetting = Settings.Secure.getString(
                context.contentResolver,
                "assistant"
            ) ?: ""
            Log.d(TAG, "Settings.Secure.assistant: $assistantSetting")
            val myPackage = context.packageName
            if (assistantSetting.contains(myPackage)) {
                return true
            }

            // Method 3: Voice interaction service setting check
            val voiceSetting = Settings.Secure.getString(
                context.contentResolver,
                "voice_interaction_service"
            ) ?: ""
            Log.d(TAG, "Settings.Secure.voice_interaction_service: $voiceSetting")
            if (voiceSetting.contains(myPackage)) {
                return true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading assistant settings: ${e.message}")
        }

        return false
    }

    /**
     * Generates a comprehensive verification report.
     */
    fun verifyConfiguration(context: Context): AssistantVerificationReport {
        val activeComponent = try {
            Settings.Secure.getString(context.contentResolver, "assistant") ?: "none"
        } catch (e: Exception) {
            "error: ${e.message}"
        }

        val myPackage = context.packageName
        val assistIntent = Intent(Intent.ACTION_ASSIST).setPackage(myPackage)
        val canHandleAssist = context.packageManager.queryIntentActivities(assistIntent, 0).isNotEmpty()

        val roleAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            rm?.isRoleAvailable(RoleManager.ROLE_ASSISTANT) ?: false
        } else false

        return AssistantVerificationReport(
            isDefaultAssistant = isDefaultAssistant(context),
            activeAssistantComponent = activeComponent,
            canHandleAssistIntent = canHandleAssist,
            voiceInteractionDeclared = true,
            roleAvailable = roleAvailable
        )
    }

    /**
     * Directs the user to the system Default Apps or Voice Input settings screen.
     */
    fun openDefaultAssistantSettings(context: Context) {
        val intents = listOf(
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                Log.d(TAG, "Intent failed: ${intent.action}, trying next...")
            }
        }
    }

    /**
     * Attempts to open MIUI / HyperOS Gesture Shortcuts settings screen
     * for double-tap fingerprint sensor configuration on Redmi Note 12 Pro+.
     */
    fun openGestureShortcutsSettings(context: Context): Boolean {
        // MIUI / HyperOS specific intents
        val miuiIntents = listOf(
            Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.SubSettings")).apply {
                putExtra(":settings:show_fragment", "com.android.settings.KeyShortcutSettingsFragment")
            },
            Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$KeyShortcutSettingsActivity")),
            Intent("miui.intent.action.KEY_SHORTCUTS"),
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in miuiIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                Log.d(TAG, "MIUI gesture shortcut intent failed: ${e.message}")
            }
        }
        return false
    }
}
