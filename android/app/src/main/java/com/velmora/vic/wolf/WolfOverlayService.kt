package com.velmora.vic.wolf

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.velmora.vic.MainActivity
import com.velmora.vic.R
import com.velmora.vic.assistant.AssistActivity

class WolfOverlayService : Service() {

    companion object {
        private const val TAG = "WolfOverlayService"
        private const val CHANNEL_ID = "vic_companion_channel"
        private const val NOTIFICATION_ID = 4040
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var wolfWebView: WolfWebView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Cannot start overlay: SYSTEM_ALERT_WINDOW permission missing.")
            stopSelf()
            return
        }

        startForegroundServiceNotification()
        setupOverlayView()
    }

    private fun startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VIC 3D Companion Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VIC Companion Active")
            .setContentText("3D Wolf companion active on screen.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOverlayView() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutParamsType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.view_wolf_overlay, null)
        wolfWebView = overlayView?.findViewById(R.id.overlayWolfWebView)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        var lastClickTime = 0L

        overlayView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isDragging = true
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(overlayView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        val now = System.currentTimeMillis()
                        if (now - lastClickTime < 300) {
                            // Double tap: Open MainActivity
                            val intent = Intent(this, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(intent)
                        } else {
                            // Single tap: Launch Assist Overlay
                            val intent = Intent(this, AssistActivity::class.java).apply {
                                action = Intent.ACTION_ASSIST
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(intent)
                        }
                        lastClickTime = now
                    }
                    true
                }
                else -> false
            }
        }

        windowManager?.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let {
            windowManager?.removeView(it)
        }
    }
}
