package com.example.moneywastetracking

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import java.util.Timer
import java.util.TimerTask
import kotlinx.coroutines.*

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var moneyWastedText: TextView
    private var hourlyRate: Float = 0f
    private var startTime: Long = 0
    private val timer = Timer()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        hourlyRate = intent?.getFloatExtra("HOURLY_RATE", 0f) ?: 0f
        showOverlay()
        startTime = System.currentTimeMillis()
        startUpdatingMoneyWasted()
        return START_STICKY
    }

    private fun showOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        moneyWastedText = overlayView.findViewById(R.id.moneyWastedText)
        windowManager.addView(overlayView, params)
    }

    private fun startUpdatingMoneyWasted() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val elapsedTimeHours = (System.currentTimeMillis() - startTime) / 3600000f
                val moneyWasted = elapsedTimeHours * hourlyRate
                withContext(Dispatchers.Main) {
                    updateMoneyWastedText(moneyWasted)
                }
                delay(300000) // Wait for 5 minutes (300000 ms)
            }
        }
    }

    private fun updateMoneyWastedText(moneyWasted: Float) {
        moneyWastedText.post {
            moneyWastedText.text = String.format("$%.2f wasted", moneyWasted)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        windowManager.removeView(overlayView)
    }
}
