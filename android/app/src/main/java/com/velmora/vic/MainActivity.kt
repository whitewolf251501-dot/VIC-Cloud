package com.velmora.vic

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.velmora.vic.assistant.AssistActivity
import com.velmora.vic.assistant.AssistantDiagnosticHelper
import com.velmora.vic.cloud.DesktopRemoteBridge
import com.velmora.vic.cloud.VicCloudClient
import com.velmora.vic.skills.PhoneSkillManager
import com.velmora.vic.wolf.WolfOverlayService
import com.velmora.vic.wolf.WolfWebView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tvAssistantStatus: TextView
    private lateinit var btnSetAssistant: Button
    private lateinit var btnOpenGestureSettings: Button
    private lateinit var tvCloudStatus: TextView
    private lateinit var tvDesktopStatus: TextView
    private lateinit var btnPingDesktop: Button
    private lateinit var btnSyncMemories: Button
    private lateinit var switchFloatingWolf: SwitchCompat
    private lateinit var previewWolfWebView: WolfWebView
    private lateinit var btnTestAlarm: Button
    private lateinit var btnTestTimer: Button
    private lateinit var btnTestWhatsApp: Button
    private lateinit var btnLaunchAssistOverlay: Button

    private lateinit var desktopBridge: DesktopRemoteBridge
    private lateinit var cloudClient: VicCloudClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initBridge()
        updateAssistantStatus()
    }

    override fun onResume() {
        super.onResume()
        updateAssistantStatus()
    }

    private fun initViews() {
        tvAssistantStatus = findViewById(R.id.tvAssistantStatus)
        btnSetAssistant = findViewById(R.id.btnSetAssistant)
        btnOpenGestureSettings = findViewById(R.id.btnOpenGestureSettings)
        tvCloudStatus = findViewById(R.id.tvCloudStatus)
        tvDesktopStatus = findViewById(R.id.tvDesktopStatus)
        btnPingDesktop = findViewById(R.id.btnPingDesktop)
        btnSyncMemories = findViewById(R.id.btnSyncMemories)
        switchFloatingWolf = findViewById(R.id.switchFloatingWolf)
        previewWolfWebView = findViewById(R.id.previewWolfWebView)
        btnTestAlarm = findViewById(R.id.btnTestAlarm)
        btnTestTimer = findViewById(R.id.btnTestTimer)
        btnTestWhatsApp = findViewById(R.id.btnTestWhatsApp)
        btnLaunchAssistOverlay = findViewById(R.id.btnLaunchAssistOverlay)

        // Set Assistant Button
        btnSetAssistant.setOnClickListener {
            AssistantDiagnosticHelper.openDefaultAssistantSettings(this)
        }

        // Gesture Settings Button (Redmi Note 12 Pro+ / MIUI / HyperOS)
        btnOpenGestureSettings.setOnClickListener {
            val opened = AssistantDiagnosticHelper.openGestureShortcutsSettings(this)
            if (!opened) {
                Toast.makeText(this, "Please navigate to: Settings -> Additional Settings -> Gesture Shortcuts", Toast.LENGTH_LONG).show()
            }
        }

        // Ping Workstation Button
        btnPingDesktop.setOnClickListener {
            tvDesktopStatus.text = "Pinging Windows Workstation..."
            lifecycleScope.launch {
                val ping = desktopBridge.pingDesktop()
                tvDesktopStatus.text = ping.message
                Toast.makeText(this@MainActivity, ping.message, Toast.LENGTH_SHORT).show()
            }
        }

        // Sync Memories Button
        btnSyncMemories.setOnClickListener {
            lifecycleScope.launch {
                val memories = cloudClient.fetchRecentMemories(5)
                Toast.makeText(this@MainActivity, "Synced ${memories.size} memories from Cloud vault.", Toast.LENGTH_SHORT).show()
            }
        }

        // Floating Wolf Toggle
        switchFloatingWolf.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                    switchFloatingWolf.isChecked = false
                } else {
                    startService(Intent(this, WolfOverlayService::class.java))
                }
            } else {
                stopService(Intent(this, WolfOverlayService::class.java))
            }
        }

        // Test Skills Buttons
        btnTestAlarm.setOnClickListener {
            PhoneSkillManager.setAlarm(this, 7, 0, "VIC Test Alarm", skipUi = false)
            Toast.makeText(this, "Set Alarm intent triggered for 07:00", Toast.LENGTH_SHORT).show()
        }

        btnTestTimer.setOnClickListener {
            PhoneSkillManager.setTimer(this, 300, "VIC 5m Timer", skipUi = false)
            Toast.makeText(this, "Set Timer intent triggered for 5 minutes", Toast.LENGTH_SHORT).show()
        }

        btnTestWhatsApp.setOnClickListener {
            // Opens WhatsApp draft with user review
            PhoneSkillManager.executeWhatsAppDispatch(this, "Emergency Contact", "Hello from VIC Assistant!")
            Toast.makeText(this, "WhatsApp draft prepared for user review.", Toast.LENGTH_SHORT).show()
        }

        btnLaunchAssistOverlay.setOnClickListener {
            val intent = Intent(this, AssistActivity::class.java).apply {
                action = Intent.ACTION_ASSIST
            }
            startActivity(intent)
        }
    }

    private fun initBridge() {
        desktopBridge = DesktopRemoteBridge(this)
        cloudClient = VicCloudClient(this)

        lifecycleScope.launch {
            val connected = desktopBridge.initialize()
            if (connected) {
                tvCloudStatus.text = "Supabase Cloud: ONLINE (Device Registered)"
                tvCloudStatus.setTextColor(resources.getColor(R.color.vic_emerald_success, theme))
            } else {
                tvCloudStatus.text = "Supabase Cloud: STANDALONE MODE"
            }
        }
    }

    private fun updateAssistantStatus() {
        val isDefault = AssistantDiagnosticHelper.isDefaultAssistant(this)
        if (isDefault) {
            tvAssistantStatus.text = "ACTIVE — VIC is Default Assistant"
            tvAssistantStatus.setTextColor(resources.getColor(R.color.vic_emerald_success, theme))
            btnSetAssistant.text = "Assistant Active (Configure)"
        } else {
            tvAssistantStatus.text = "NOT SET — Standard Google Assistant Active"
            tvAssistantStatus.setTextColor(resources.getColor(R.color.vic_amber_warning, theme))
            btnSetAssistant.text = getString(R.string.set_default_assistant)
        }
    }
}
