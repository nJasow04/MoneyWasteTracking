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
import android.view.MotionEvent
import android.graphics.Point
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import kotlin.math.absoluteValue

import android.content.res.Resources
import android.util.Log


class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var layoutParams: WindowManager.LayoutParams

    // For padding around the circle
    private val margin = 5

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels

        // Optionally set initial position (this can be adjusted)
        layoutParams.x = screenWidth - overlayView.width - margin
        layoutParams.y = margin // Start with top margin

        windowManager.addView(overlayView, layoutParams)

        // Add touch listener for dragging
        overlayView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - initialTouchX
                        val deltaY = event.rawY - initialTouchY

                        if (deltaX.absoluteValue > 10 || deltaY.absoluteValue > 10) {
                            // If movement is significant, consider it a drag
                            layoutParams.x = initialX + deltaX.toInt()
                            layoutParams.y = initialY + deltaY.toInt()
                            windowManager.updateViewLayout(overlayView, layoutParams)
                            return true
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val deltaX = event.rawX - initialTouchX
                        val deltaY = event.rawY - initialTouchY

                        if (deltaX.absoluteValue < 10 && deltaY.absoluteValue < 10) {
                            // Consider this a click if movement is minimal
                            overlayView.performClick()
                        } else {
                            // Movement was significant, snap to closest corner
                            snapToClosestCorner()
                        }
                        return true
                    }
                }
                return false
            }

        })

        overlayView.setOnClickListener {
            Log.d("OverlayService", "Entering setOnClickListener")
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            Log.d("OverlayService", "Starting Activity")
            startActivity(intent)
        }
    }

    private fun snapToClosestCorner() {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        val centerX = layoutParams.x + overlayView.width / 2
        val centerY = layoutParams.y + overlayView.height / 2

        val snapToLeft = centerX < screenWidth / 2
        val snapToTop = centerY < screenHeight / 2


        // Determine the target corner, considering the margin
        val targetX = if (centerX < screenWidth / 2) {
            margin // Snap to left with margin
        } else {
            screenWidth - overlayView.width - margin // Snap to right with margin
        }

        val targetY = if (centerY < screenHeight / 2) {
            margin // Snap to top with margin
        } else {
            screenHeight - overlayView.height - margin // Snap to bottom with margin
        }

//        windowManager.updateViewLayout(overlayView, layoutParams)

        // Animate to the target position
        val animatorX = ValueAnimator.ofInt(layoutParams.x, targetX)
        val animatorY = ValueAnimator.ofInt(layoutParams.y, targetY)
        animatorX.addUpdateListener { animation ->
            layoutParams.x = animation.animatedValue as Int
            windowManager.updateViewLayout(overlayView, layoutParams)
        }
        animatorY.addUpdateListener { animation ->
            layoutParams.y = animation.animatedValue as Int
            windowManager.updateViewLayout(overlayView, layoutParams)
        }

        // Set duration and start animations
        animatorX.duration = 300 // 300ms for smooth transition
        animatorY.duration = 300
        animatorX.start()
        animatorY.start()
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


