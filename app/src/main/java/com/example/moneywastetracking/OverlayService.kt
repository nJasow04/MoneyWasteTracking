package com.example.moneywastetracking

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.RIGHT

        windowManager.addView(overlayView, params)

        overlayView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun updateOverlay(moneyWasted: Float) {
        val moneyTextView = overlayView.findViewById<TextView>(R.id.overlayText)
        moneyTextView.text = "$${moneyWasted}"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val moneyWasted = intent?.getFloatExtra("moneywasted", 0f) ?: 0f
        updateOverlay(moneyWasted) // Update overlay with the moneywasted value
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
