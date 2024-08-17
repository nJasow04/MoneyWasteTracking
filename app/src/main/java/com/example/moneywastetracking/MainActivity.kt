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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyWasteTrackingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(
                        modifier = Modifier.padding(innerPadding),
                        onStartOverlayService = { hourlyRate ->
                            startOverlayService(hourlyRate)
                        }
                    )
                }
            }
        }
    }

    private fun startOverlayService(hourlyRate: Float) {
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("HOURLY_RATE", hourlyRate)
        }
        startService(intent)
    }
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    onStartOverlayService: (Float) -> Unit
) {
    var hourlyRate by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Money Waste Tracker", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hourlyRate,
            onValueChange = { hourlyRate = it },
            label = { Text("Hourly Rate") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                hourlyRate.toFloatOrNull()?.let { rate ->
                    onStartOverlayService(rate)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Start Overlay Service")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    MoneyWasteTrackingTheme {
        MainContent(onStartOverlayService = {})
    }
}

