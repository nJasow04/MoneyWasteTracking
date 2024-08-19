package com.example.moneywastetracking

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import android.os.Binder

class TimerService : Service() {

    private var hourlyRate = 0f
    private var hours = 0
    private var minutes = 0
    private var seconds = 0
    private var moneywasted = 0f
    private var job: Job? = null

    private val binder = TimerBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        hourlyRate = intent?.getFloatExtra("hourlyRate", 0f) ?: 0f
        startTimer()
        return START_STICKY // Ensures the service keeps running
    }

    private fun startTimer() {
        job?.cancel()
        job = serviceScope.launch {
            while (true) {
                delay(1000) // 1 second in milliseconds
                seconds++
                if (seconds == 60) {
                    seconds = 0
                    minutes++
                    if (minutes == 60) {
                        minutes = 0
                        hours++
                    }
                }
                moneywasted = BigDecimal(hourlyRate * (hours + minutes / 60.0 + seconds / 3600.0).toDouble())
                    .setScale(3, RoundingMode.HALF_UP)
                    .toFloat()
                // Save to SharedPreferences or broadcast the update as needed
            }
        }
    }

    fun getHourlyRate(): Float = hourlyRate

    fun getMoneyWasted(): Float = moneywasted

    fun getTimeSpent(): Triple<Int, Int, Int> = Triple(hours, minutes, seconds)

    fun updateHourlyRate(newRate: Float) {
        hourlyRate = newRate
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
}
