package com.velmora.vic.cloud

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class VicCloudClient(private val context: Context) {

    companion object {
        private const val TAG = "VicCloudClient"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("vic_cloud_prefs", Context.MODE_PRIVATE)

    var mobileDeviceId: String
        get() {
            var id = prefs.getString("mobile_device_id", null)
            if (id == null) {
                id = "android-" + UUID.randomUUID().toString().take(8)
                prefs.edit().putString("mobile_device_id", id).apply()
            }
            return id
        }
        set(value) = prefs.edit().putString("mobile_device_id", value).apply()

    var desktopTargetDeviceId: String
        get() = prefs.getString("desktop_device_id", VicCloudConfig.DEFAULT_DESKTOP_DEVICE_ID) ?: VicCloudConfig.DEFAULT_DESKTOP_DEVICE_ID
        set(value) = prefs.edit().putString("desktop_device_id", value).apply()

    private fun buildRequest(endpoint: String, method: String = "GET", body: String? = null): Request {
        val url = "${VicCloudConfig.SUPABASE_URL}/rest/v1/$endpoint"
        val builder = Request.Builder()
            .url(url)
            .addHeader("apikey", VicCloudConfig.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${VicCloudConfig.SUPABASE_ANON_KEY}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")

        if (method == "POST" && body != null) {
            builder.post(body.toRequestBody(JSON_MEDIA_TYPE))
        } else if (method == "PATCH" && body != null) {
            builder.patch(body.toRequestBody(JSON_MEDIA_TYPE))
        }

        return builder.build()
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("devices?select=id&limit=1")
            httpClient.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.w(TAG, "Connection test failed: ${e.message}")
            false
        }
    }

    suspend fun registerDevice(): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", mobileDeviceId)
                put("device_name", "Redmi Note 12 Pro+ (VIC Assistant)")
                put("device_type", "android_phone")
                put("status", "online")
                put("metadata", JSONObject().apply {
                    put("manufacturer", Build.MANUFACTURER)
                    put("model", Build.MODEL)
                    put("android_version", Build.VERSION.RELEASE)
                    put("sdk_int", Build.VERSION.SDK_INT)
                    put("app_version", "1.0.0")
                })
            }

            val request = buildRequest("devices", "POST", json.toString())
            httpClient.newCall(request).execute().use { response ->
                Log.d(TAG, "Register device response: ${response.code}")
                response.isSuccessful || response.code == 409 // 409 if already exists
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register mobile device: ${e.message}")
            false
        }
    }

    suspend fun dispatchDesktopAction(
        actionType: String,
        payload: JSONObject = JSONObject(),
        riskTier: Int = 0,
        requiresConfirmation: Boolean = false
    ): String? = withContext(Dispatchers.IO) {
        try {
            val actionId = UUID.randomUUID().toString()
            val json = JSONObject().apply {
                put("id", actionId)
                put("source_device_id", mobileDeviceId)
                put("target_device_id", desktopTargetDeviceId)
                put("action_type", actionType)
                put("payload", payload)
                put("risk_tier", riskTier)
                put("requires_confirmation", requiresConfirmation)
                put("status", "pending")
            }

            val request = buildRequest("device_actions", "POST", json.toString())
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.i(TAG, "Successfully dispatched action $actionType ($actionId)")
                    actionId
                } else {
                    Log.w(TAG, "Error dispatching action: ${response.code} ${response.body?.string()}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception dispatching action: ${e.message}")
            null
        }
    }

    suspend fun pollActionResult(actionId: String, maxWaitSeconds: Int = 10): JSONObject? = withContext(Dispatchers.IO) {
        val deadline = System.currentTimeMillis() + (maxWaitSeconds * 1000)
        while (System.currentTimeMillis() < deadline) {
            try {
                val request = buildRequest("action_results?action_id=eq.$actionId&select=*")
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: "[]"
                        val array = JSONArray(body)
                        if (array.length() > 0) {
                            return@withContext array.getJSONObject(0)
                        }
                    }
                }
                kotlinx.coroutines.delay(1000)
            } catch (e: Exception) {
                Log.w(TAG, "Error polling action result: ${e.message}")
                kotlinx.coroutines.delay(1000)
            }
        }
        null
    }

    suspend fun fetchRecentMemories(limit: Int = 10): List<String> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("memories?select=content&order=created_at.desc&limit=$limit")
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val array = JSONArray(response.body?.string() ?: "[]")
                    val list = mutableListOf<String>()
                    for (i in 0 until array.length()) {
                        list.add(array.getJSONObject(i).optString("content"))
                    }
                    list
                } else emptyList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching memories: ${e.message}")
            emptyList()
        }
    }
}
