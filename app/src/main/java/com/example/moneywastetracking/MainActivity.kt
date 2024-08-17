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

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var hourlyRate: Float = 0f
//    private var totalTime: Float = 0f
    private var time by mutableStateOf(0f)
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyWasteTrackingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(
                        modifier = Modifier.padding(innerPadding),
                        onStartOverlayService = {
//                            startOverlayService()
                            startTimer()
                        },
                        onHourlyRateChange = { rate ->
                            hourlyRate = rate
                        },
//                        totalTime = totalTime,
                        time = time,
                        onResetCurrent= {
                            time = 0f
                            job?.cancel()
                        }
                    )
                }
            }
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("HOURLY_RATE", hourlyRate)
//            putExtra("TOTAL_TIME", totalTime)
            putExtra("TIME", time)
        }
        startService(intent)
    }

    private fun startTimer() {
        job = lifecycleScope.launch {
            while (true) {
//                delay(360000) // 6 minutes in milliseconds
                delay(1000) // 6 minutes in milliseconds
                time += 6
//                totalTime += 6
            }
        }
    }
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    onStartOverlayService: () -> Unit,
    onHourlyRateChange: (Float) -> Unit,
//    totalTime: Float,
    time: Float,
    onResetCurrent: () -> Unit,
) {
    var hourlyRateInput by remember { mutableStateOf("") }


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

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Money Waste Tracker", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hourlyRateInput,
            onValueChange = {
                hourlyRateInput = it
                it.toFloatOrNull()?.let { rate ->
                    onHourlyRateChange(rate)
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Time Wasted in Current Session: $time hours")
//        Text(text = "Total Money Wasted: ${totalTime * hourlyRate} dollars")

        Button(
            onClick = onResetCurrent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Reset Current Session")
        }
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(text = "Total Time Wasted: $totalTime hours")
//        Button(
//            onClick = onResetTotal,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Reset Total")
//        }
    }

}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    MoneyWasteTrackingTheme {
        MainContent(
            onStartOverlayService = {},
            onHourlyRateChange = {},
//            totalTime = 0f,
            time = 0f,
            onResetCurrent = {},
//            onResetTotal = {}
        )
    }
}

