package com.velmora.vic.skills

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.util.Log
import java.net.URLEncoder

/**
 * Executes local phone actions with strict safeguards against unauthorized operations.
 */
object PhoneSkillManager {

    private const val TAG = "PhoneSkillManager"

    data class WhatsAppDraft(
        val recipient: String,
        val message: String
    )

    data class AlarmRequest(
        val hour: Int,
        val minute: Int,
        val message: String
    )

    data class TimerRequest(
        val durationSeconds: Int,
        val message: String
    )

    /**
     * Sets a native system alarm.
     */
    fun setAlarm(context: Context, hour: Int, minute: Int, message: String = "VIC Alarm", skipUi: Boolean = false): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set alarm: ${e.message}")
            false
        }
    }

    /**
     * Sets a native countdown timer.
     */
    fun setTimer(context: Context, durationSeconds: Int, message: String = "VIC Timer", skipUi: Boolean = false): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, durationSeconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set timer: ${e.message}")
            false
        }
    }

    /**
     * Parses user query to detect WhatsApp composition intents.
     * Example: "Send WhatsApp to Alex saying I will arrive in 10 minutes"
     */
    fun parseWhatsAppDraft(query: String): WhatsAppDraft? {
        val lower = query.trim()
        val regex = Regex("""(?:send\s+whatsapp\s+(?:to\s+)?|whatsapp\s+)([\w\s+]+?)\s+(?:saying|that|with message)\s+(.+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(lower)
        if (match != null) {
            val recipient = match.groupValues[1].trim()
            val message = match.groupValues[2].trim()
            if (recipient.isNotEmpty() && message.isNotEmpty()) {
                return WhatsAppDraft(recipient, message)
            }
        }
        return null
    }

    /**
     * Dispatches the WhatsApp draft to the official WhatsApp application.
     * CRITICAL SAFEGUARD: Must only be invoked AFTER the user confirms the action
     * via the interactive confirmation card or dialog.
     */
    fun executeWhatsAppDispatch(context: Context, recipient: String, message: String): Boolean {
        val pm = context.packageManager
        val isInstalled = isPackageInstalled("com.whatsapp", pm) || isPackageInstalled("com.whatsapp.w4b", pm)

        return try {
            if (recipient.matches(Regex("""^\+?[0-9]{7,15}$"""))) {
                // Direct phone number URI
                val cleanNumber = recipient.replace("+", "").trim()
                val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=${URLEncoder.encode(message, "UTF-8")}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } else {
                // Share to contact with prepopulated message
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    if (isInstalled) {
                        setPackage("com.whatsapp")
                    }
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch WhatsApp dispatch: ${e.message}")
            false
        }
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Reads current device battery level.
     */
    fun getBatteryLevel(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    }

    /**
     * Toggles flashlight/torch.
     */
    fun setTorchMode(context: Context, enabled: Boolean): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraManager != null && cameraId != null) {
                cameraManager.setTorchMode(cameraId, enabled)
                true
            } else false
        } catch (e: Exception) {
            Log.e(TAG, "Error setting torch mode: ${e.message}")
            false
        }
    }
}
