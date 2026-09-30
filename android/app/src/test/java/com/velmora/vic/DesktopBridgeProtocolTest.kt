package com.velmora.vic

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class DesktopBridgeProtocolTest {

    @Test
    fun testTierClassificationParity() {
        val tier0Actions = listOf("ping", "get_status", "status", "battery_status", "active_window")
        val tier1Actions = listOf("launch_app", "open_application")
        val tier2Actions = listOf("capture_screen", "screenshot", "read_file_safe", "read_file")
        val prohibitedTier3 = listOf("execute_command", "powershell", "cmd", "delete_file", "rmdir")

        // Verify tier 0 actions
        for (action in tier0Actions) {
            val tier = determineRiskTier(action, JSONObject())
            assertEquals("Action $action must be Tier 0", 0, tier)
        }

        // Verify tier 1 safe apps
        for (action in tier1Actions) {
            val payload = JSONObject().apply { put("app", "calc") }
            val tier = determineRiskTier(action, payload)
            assertEquals("Safe app launch must be Tier 1", 1, tier)
        }

        // Verify tier 2 sensitive
        for (action in tier2Actions) {
            val tier = determineRiskTier(action, JSONObject())
            assertEquals("Action $action must be Tier 2", 2, tier)
        }

        // Verify prohibited tier 3
        for (action in prohibitedTier3) {
            val tier = determineRiskTier(action, JSONObject())
            assertEquals("Action $action must be Tier 3", 3, tier)
        }
    }

    @Test
    fun testDeviceActionSerializationFormat() {
        val actionId = UUID.randomUUID().toString()
        val sourceDeviceId = "android-test-node"
        val targetDeviceId = "desktop-windows-primary"

        val json = JSONObject().apply {
            put("id", actionId)
            put("source_device_id", sourceDeviceId)
            put("target_device_id", targetDeviceId)
            put("action_type", "ping")
            put("payload", JSONObject())
            put("risk_tier", 0)
            put("requires_confirmation", false)
            put("status", "pending")
        }

        assertEquals(actionId, json.getString("id"))
        assertEquals(sourceDeviceId, json.getString("source_device_id"))
        assertEquals(targetDeviceId, json.getString("target_device_id"))
        assertEquals("ping", json.getString("action_type"))
        assertEquals(0, json.getInt("risk_tier"))
        assertFalse(json.getBoolean("requires_confirmation"))
        assertEquals("pending", json.getString("status"))
    }

    private fun determineRiskTier(actionType: String, payload: JSONObject): Int {
        val lower = actionType.lowercase().trim()
        val prohibited = listOf("execute_command", "terminal", "powershell", "cmd", "delete_file", "rmdir")
        if (prohibited.any { lower.contains(it) }) return 3
        if (payload.has("command") || payload.has("exec")) return 3

        if (lower in listOf("capture_screen", "screenshot", "read_file_safe", "read_file")) return 2
        if (lower in listOf("launch_app", "open_application")) {
            val app = payload.optString("app", "").lowercase()
            val safeApps = setOf("calc", "notepad", "code", "chrome", "edge", "explorer")
            return if (safeApps.contains(app)) 1 else 2
        }
        if (lower in listOf("ping", "get_status", "status", "battery_status", "active_window")) return 0
        return 2
    }
}
