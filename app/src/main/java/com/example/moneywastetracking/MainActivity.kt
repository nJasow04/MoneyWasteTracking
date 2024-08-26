package com.example.moneywastetracking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneywastetracking.ui.theme.MoneyWasteTrackingTheme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager

import java.math.BigDecimal
import java.math.RoundingMode

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log



/*
    Next Steps:
    Hourly Rate Input:
    - Pressing Enter should automatically save the input, not go to new line. It should also reset
    everything back to zero too.

    - Total page:
        - Total time wasted, Today, past week, past month, past 3 months, past 6 months, past year
        - Total money wasted ---------------------
 */


class MainActivity : ComponentActivity() {

    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1234  // You can use any unique integer value
    }

    private var hourlyRate by mutableStateOf(0f)
    private var hours by mutableStateOf(0)
    private var minutes by mutableStateOf(0)
    private var seconds by mutableStateOf(0)
    private var moneywasted by mutableStateOf(0f)
    private var job: Job? = null
    private var isPaused by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MoneyWasteTrackingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(
                        modifier = Modifier.padding(innerPadding),
                        onHourlyRateChange = { rate ->
                            hourlyRate = rate
                        },
                        hours = hours,
                        minutes = minutes,
                        seconds = seconds,
                        onResetCurrent = {
                            hours = 0
                            minutes = 0
                            seconds = 0
                            moneywasted = 0f

                            // Stop the OverlayService
                            val stopIntent = Intent(this, OverlayService::class.java)
                            stopService(stopIntent)
                            job?.cancel()
                        },
                        onPauseOrResume = {
                            if (isPaused) {
//                                startTimer() // Resume the timer
//                                isPaused = false
                                startTimer() // Resume the timer
                                checkOverlayPermission()
                                val intent = Intent(this, OverlayService::class.java)
                                intent.putExtra("hourlyRate", hourlyRate) // Pass the hourlyRate value
                                Log.d("MainActivity", "moneywasted: $moneywasted")
                                intent.putExtra("initial_moneywasted", moneywasted) // Pass the current money wasted value
                                startService(intent)
                                isPaused = false
                            } else {
                                job?.cancel() // Pause the timer
                                job = null
                                isPaused = true

                                // Stop the OverlayService when paused
                                val stopIntent = Intent(this, OverlayService::class.java)
                                stopService(stopIntent)
//                                job?.cancel() // Pause the timer
//                                job = null
//                                isPaused = true
                            }
                        },
                        isPaused = isPaused,
                        moneywasted = moneywasted,
                        onStartOverlayService = {
                            startTimer()

                            checkOverlayPermission()
                            val intent = Intent(this, OverlayService::class.java)
                            intent.putExtra("hourlyRate", hourlyRate) // Pass the hourlyRate value
                            intent.putExtra("initial_moneywasted", 0f) // Initialize initial_moneywasted to 0
                            startService(intent)
//                            finish() // Close the main page if needed
                        }
                    )
                }
            }
        }
    }

    private fun startTimer() {
        job?.cancel()
        job = lifecycleScope.launch {
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
                moneywasted =
                    BigDecimal(hourlyRate * (hours + minutes / 60.0 + seconds / 3600.0).toDouble()).setScale(
                        3,
                        RoundingMode.HALF_UP
                    ).toFloat()

            }
        }
    }


    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
            } else {
                startOverlayService()
            }
        } else {
            startOverlayService()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"))
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("hourlyRate", hourlyRate)  // Pass the hourlyRate to OverlayService
        startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                startOverlayService()
            } else {
                // Permission denied, handle accordingly (maybe show a message to the user)
            }
        }
    }


}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    onHourlyRateChange: (Float) -> Unit,
    hours: Int,
    minutes: Int,
    seconds: Int,
    onResetCurrent: () -> Unit,
    onPauseOrResume: () -> Unit,
    isPaused: Boolean,
    moneywasted: Float,
    onStartOverlayService: () -> Unit // Add this parameter back
) {
    var hourlyRateInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Money Waste Tracker", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hourlyRateInput,
            onValueChange = {
                hourlyRateInput = it
                it.toFloatOrNull()?.let { rate ->
                    if (rate >= 0) {
                        onHourlyRateChange(rate)
                    } else {
                        onHourlyRateChange(0f)
                    }
                }
            },
            label = { Text("Hourly Rate $/hr") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartOverlayService, // Use the lambda passed to MainContent
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Start Overlay Service")
        }
        Text(text = "Current Hourly Rate: $$hourlyRateInput per hour")
        Text(text = "Time Wasted: $hours hr $minutes min $seconds s")
        Text(text = "Money Wasted in Current Session: $${moneywasted}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onPauseOrResume, // Use the onPauseOrResume function
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isPaused) "Resume" else "Pause") // Toggle button text based on isPaused
        }

        Button(
            onClick = onResetCurrent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Stop and Reset Current Session")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    MoneyWasteTrackingTheme {
        MainContent(
            onStartOverlayService = {},
            onResetCurrent = {},
            hours = 0,
            minutes = 0,
            seconds = 0,
            moneywasted = 0f,
            onHourlyRateChange = {},
            isPaused = false,
            onPauseOrResume = {},
        )
    }
}

