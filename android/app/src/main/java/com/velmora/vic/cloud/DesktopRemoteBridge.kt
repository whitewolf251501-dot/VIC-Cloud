package com.velmora.vic.cloud

import android.content.Context
import android.util.Log
import org.json.JSONObject

class DesktopRemoteBridge(private val context: Context) {

    companion object {
        private const val TAG = "DesktopRemoteBridge"
    }

    private val cloudClient = VicCloudClient(context)

    data class RemoteCommandResult(
        val success: Boolean,
        val message: String,
        val details: JSONObject? = null,
        val requiresConfirmation: Boolean = false
    )

    suspend fun pingDesktop(): RemoteCommandResult {
        val actionId = cloudClient.dispatchDesktopAction("ping", JSONObject(), riskTier = 0)
            ?: return RemoteCommandResult(false, "Could not queue ping command to desktop.")

        val result = cloudClient.pollActionResult(actionId, maxWaitSeconds = 6)
        return if (result != null && result.optString("status") == "success") {
            RemoteCommandResult(true, "Workstation online and responsive.", result.optJSONObject("result"))
        } else {
            RemoteCommandResult(false, "Workstation did not respond to ping.")
        }
    }

    suspend fun getDesktopStatus(): RemoteCommandResult {
        val actionId = cloudClient.dispatchDesktopAction("get_status", JSONObject(), riskTier = 0)
            ?: return RemoteCommandResult(false, "Failed to send status request to workstation.")

        val result = cloudClient.pollActionResult(actionId, maxWaitSeconds = 8)
        return if (result != null && result.optString("status") == "success") {
            val resObj = result.optJSONObject("result")
            val status = resObj?.optString("status", "OPERATIONAL") ?: "OPERATIONAL"
            val skills = resObj?.optInt("skillsCount", 14) ?: 14
            val memories = resObj?.optInt("memoriesCount", 0) ?: 0
            RemoteCommandResult(true, "Workstation status: $status. $skills active skills, $memories memories.", resObj)
        } else {
            RemoteCommandResult(false, "Could not retrieve status from workstation.")
        }
    }

    suspend fun launchDesktopApp(appName: String): RemoteCommandResult {
        val payload = JSONObject().apply {
            put("app", appName)
        }
        val actionId = cloudClient.dispatchDesktopAction("launch_app", payload, riskTier = 1)
            ?: return RemoteCommandResult(false, "Failed to send app launch request.")

        val result = cloudClient.pollActionResult(actionId, maxWaitSeconds = 8)
        return if (result != null && result.optString("status") == "success") {
            RemoteCommandResult(true, "Application '$appName' launched on workstation.", result.optJSONObject("result"))
        } else {
            val error = result?.optString("error", "Launch timed out or rejected.") ?: "Launch timed out."
            RemoteCommandResult(false, "Failed to launch $appName: $error")
        }
    }

    suspend fun requestDesktopScreenCapture(): RemoteCommandResult {
        // Tier 2: Triggers Confirmation Gate on the desktop
        val actionId = cloudClient.dispatchDesktopAction("capture_screen", JSONObject(), riskTier = 2, requiresConfirmation = true)
            ?: return RemoteCommandResult(false, "Failed to queue screen capture.")

        // Allow up to 25 seconds for user confirmation on desktop
        val result = cloudClient.pollActionResult(actionId, maxWaitSeconds = 25)
        return if (result != null && result.optString("status") == "success") {
            RemoteCommandResult(true, "Workstation screen captured successfully.", result.optJSONObject("result"))
        } else {
            val error = result?.optString("error", "Confirmation timed out or rejected by desktop user.") ?: "Desktop user did not confirm."
            RemoteCommandResult(false, "Screen capture not completed: $error")
        }
    }

    suspend fun initialize(): Boolean {
        cloudClient.registerDevice()
        return cloudClient.testConnection()
    }
}
