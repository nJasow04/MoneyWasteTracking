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
// TODO
/*
    Hourly Rate Input:
    - Pressing Enter should automatically save the input, not go to new line
    - Needs to be a reset button that returns the hourly rate to an input-able state,
        and sets hourly rate back to 0
    - The reset button will be the same as the start overlay service button after the start
        button has been clicked.

    - Total page:
        - Total time wasted, Today, past week, past month, past 3 months, past 6 months, past year
        - Total money wasted ---------------------
    - Changing your hourly rate doesn't necessarily reset the counter, you will have the option to
        change your rate without reseting the counter by default, and resetting the counter
        to zero if you want.
    - Total Money Wasted has to be an accumulation of all sessions and different hourly rates.
 */


class MainActivity : ComponentActivity() {
    private var hourlyRate by mutableStateOf(0f)
    private var hours by mutableStateOf(0)
    private var minutes by mutableStateOf(0)
    private var seconds by mutableStateOf(0)
    private var moneywasted by mutableStateOf(0f)
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyWasteTrackingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(
                        modifier = Modifier.padding(innerPadding),
                        onStartOverlayService = {
                            startTimer()
                        },
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
                            job?.cancel()
                        },
                        moneywasted = moneywasted,
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
                moneywasted = BigDecimal(hourlyRate * (hours + minutes / 60.0 + seconds / 3600.0).toDouble()).setScale(3, RoundingMode.HALF_UP).toFloat()
            }
        }
    }
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    onStartOverlayService: () -> Unit,
    onHourlyRateChange: (Float) -> Unit,
    hours: Int,
    minutes: Int,
    seconds: Int,
    onResetCurrent: () -> Unit,
    moneywasted: Float,
) {
    var hourlyRateInput by remember { mutableStateOf("") }
    val context = LocalContext.current
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
            onClick = onStartOverlayService,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Start Overlay Service")
        }
        Text(text = "Time Wasted: $hours hr $minutes min $seconds s")
        Text(text = "Current Hourly Rate: $$hourlyRateInput per hour")
        Text(text = "Money Wasted in Current Session: $${moneywasted}")

        Spacer(modifier = Modifier.height(16.dp))

//        Text(text = "Time Wasted in Current Session: $time hours")

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
            onHourlyRateChange = {},
            hours = 0,
            minutes = 0,
            seconds = 0,
            onResetCurrent = {},
            moneywasted = 0f,
        )
    }
}
