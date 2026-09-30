package com.velmora.vic.voice

import android.content.Context
import com.velmora.vic.cloud.DesktopRemoteBridge
import com.velmora.vic.skills.PhoneSkillManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class VicConversationEngine(private val context: Context) {

    enum class ActionType {
        NONE,
        WHATSAPP_CONFIRM,
        ALARM_CREATED,
        TIMER_CREATED,
        DESKTOP_COMMAND,
        DEVICE_STATUS
    }

    data class ConversationResponse(
        val spokenResponse: String,
        val wolfState: String,
        val actionType: ActionType,
        val payload: Any? = null,
        val requiresUserConfirmation: Boolean = false
    )

    private val desktopBridge = DesktopRemoteBridge(context)

    suspend fun processQuery(query: String): ConversationResponse = withContext(Dispatchers.Default) {
        val clean = query.trim()
        val lower = clean.lowercase()

        // 1. WhatsApp Draft Intent (Requires Confirmation Safeguard)
        val whatsAppDraft = PhoneSkillManager.parseWhatsAppDraft(clean)
        if (whatsAppDraft != null) {
            return@withContext ConversationResponse(
                spokenResponse = "I have prepared the WhatsApp message for ${whatsAppDraft.recipient}. Please review and confirm below before I proceed.",
                wolfState = "speaking",
                actionType = ActionType.WHATSAPP_CONFIRM,
                payload = whatsAppDraft,
                requiresUserConfirmation = true
            )
        }

        // 2. Alarms
        val alarmMatch = Regex("""(?:set\s+(?:an\s+)?alarm\s+(?:for\s+)?|wake\s+me\s+up\s+(?:at\s+)?)(?:(\d{1,2})(?::(\d{2}))?\s*(am|pm)?)""", RegexOption.IGNORE_CASE).find(clean)
        if (alarmMatch != null) {
            var hour = alarmMatch.groupValues[1].toIntOrNull() ?: 7
            val min = alarmMatch.groupValues[2].toIntOrNull() ?: 0
            val ampm = alarmMatch.groupValues[3].lowercase()

            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0

            val formattedTime = String.format("%02d:%02d", hour, min)
            val success = PhoneSkillManager.setAlarm(context, hour, min, "VIC Alarm", skipUi = true)
            return@withContext if (success) {
                ConversationResponse(
                    spokenResponse = "Alarm set for $formattedTime.",
                    wolfState = "success",
                    actionType = ActionType.ALARM_CREATED
                )
            } else {
                ConversationResponse(
                    spokenResponse = "I was unable to schedule the alarm. Please verify permissions.",
                    wolfState = "error",
                    actionType = ActionType.NONE
                )
            }
        }

        // 3. Timers
        val timerMatch = Regex("""(?:set\s+(?:a\s+)?timer\s+(?:for\s+)?|timer\s+)(\d+)\s*(minute|min|second|sec|hour|hr)s?""", RegexOption.IGNORE_CASE).find(clean)
        if (timerMatch != null) {
            val amount = timerMatch.groupValues[1].toIntOrNull() ?: 5
            val unit = timerMatch.groupValues[2].lowercase()
            val seconds = when {
                unit.startsWith("sec") -> amount
                unit.startsWith("hour") || unit.startsWith("hr") -> amount * 3600
                else -> amount * 60
            }

            val success = PhoneSkillManager.setTimer(context, seconds, "VIC Timer", skipUi = true)
            return@withContext if (success) {
                ConversationResponse(
                    spokenResponse = "Timer set for $amount ${unit}s.",
                    wolfState = "success",
                    actionType = ActionType.TIMER_CREATED
                )
            } else {
                ConversationResponse(
                    spokenResponse = "Could not start the countdown timer.",
                    wolfState = "error",
                    actionType = ActionType.NONE
                )
            }
        }

        // 4. Remote Desktop Controls (via Desktop Bridge)
        if (lower.contains("ping desktop") || lower.contains("ping workstation") || lower.contains("check workstation")) {
            val ping = desktopBridge.pingDesktop()
            return@withContext ConversationResponse(
                spokenResponse = ping.message,
                wolfState = if (ping.success) "success" else "error",
                actionType = ActionType.DESKTOP_COMMAND,
                payload = ping
            )
        }

        if (lower.contains("desktop status") || lower.contains("workstation status") || lower.contains("status of desktop")) {
            val status = desktopBridge.getDesktopStatus()
            return@withContext ConversationResponse(
                spokenResponse = status.message,
                wolfState = if (status.success) "success" else "error",
                actionType = ActionType.DESKTOP_COMMAND,
                payload = status
            )
        }

        if (lower.startsWith("open ") || lower.startsWith("launch ")) {
            val target = lower.removePrefix("open ").removePrefix("launch ").replace(" on desktop", "").replace(" on pc", "").trim()
            if (target in listOf("calc", "calculator", "notepad", "code", "vscode", "explorer", "files", "chrome", "edge")) {
                val launch = desktopBridge.launchDesktopApp(target)
                return@withContext ConversationResponse(
                    spokenResponse = launch.message,
                    wolfState = if (launch.success) "success" else "error",
                    actionType = ActionType.DESKTOP_COMMAND,
                    payload = launch
                )
            }
        }

        if (lower.contains("capture desktop") || lower.contains("screenshot desktop") || lower.contains("desktop screen")) {
            val cap = desktopBridge.requestDesktopScreenCapture()
            return@withContext ConversationResponse(
                spokenResponse = cap.message,
                wolfState = if (cap.success) "success" else "error",
                actionType = ActionType.DESKTOP_COMMAND,
                payload = cap
            )
        }

        // 5. Battery and Phone Status
        if (lower.contains("battery") || lower.contains("battery level") || lower.contains("power")) {
            val level = PhoneSkillManager.getBatteryLevel(context)
            return@withContext ConversationResponse(
                spokenResponse = "Battery is currently at $level percent.",
                wolfState = "speaking",
                actionType = ActionType.DEVICE_STATUS
            )
        }

        // 6. Flashlight
        if (lower.contains("torch on") || lower.contains("flashlight on")) {
            PhoneSkillManager.setTorchMode(context, true)
            return@withContext ConversationResponse(
                spokenResponse = "Flashlight enabled.",
                wolfState = "success",
                actionType = ActionType.DEVICE_STATUS
            )
        }
        if (lower.contains("torch off") || lower.contains("flashlight off")) {
            PhoneSkillManager.setTorchMode(context, false)
            return@withContext ConversationResponse(
                spokenResponse = "Flashlight disabled.",
                wolfState = "speaking",
                actionType = ActionType.DEVICE_STATUS
            )
        }

        // 7. Conversational Personality: Victor Noctis
        val responseText = when {
            lower.contains("who are you") || lower.contains("your name") ->
                "I am Victor Noctis, Velmora Intelligence Commander. Your personal AI companion and desktop workstation orchestrator."

            lower.contains("hello") || lower.contains("hi") || lower.contains("hey vic") ->
                "Greetings. All systems are operational. How can I assist you today?"

            lower.contains("status") ->
                "Mobile node active. Connected to Velmora cloud architecture. Companion wolf ready."

            else ->
                "Understood. Commander standing by. How would you like to proceed?"
        }

        ConversationResponse(
            spokenResponse = responseText,
            wolfState = "speaking",
            actionType = ActionType.NONE
        )
    }
}
