package com.example.aethersender

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    // 1. Handles the popup for all required permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if the critical ones are granted
        val bluetoothGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else true

        if (bluetoothGranted) {
            startAetherService()
        } else {
            Toast.makeText(this, "Bluetooth Permission Denied. Cannot Link to Clock.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Prepare the list of permissions to ask the user
        val permissionsToRequest = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        )

        // Bluetooth permissions for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }

        // Notification permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Launch the request immediately
        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Duby Controller",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Linked to: J1 Ace",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { startAetherService() },
                            modifier = Modifier.fillMaxWidth(0.6f)
                        ) {
                            Text("Force Reconnect")
                        }
                    }
                }
                // ... inside your setContent { MaterialTheme { Surface { Column { ...
                Text(text = "Duby Controller", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { startAetherService() }) {
                    Text("Restart Service")
                }

// --- PASTE THE TEST BUTTON HERE ---
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    val intent = Intent("com.example.aethersender.SEND_DATA")
                    intent.putExtra("data", "CALL:RING")
                    sendBroadcast(intent)
                    android.widget.Toast.makeText(this, "Testing Red Flash...", android.widget.Toast.LENGTH_SHORT).show()
                }) {
                    Text("Test Red Flash")
                }
// ----------------------------------

            }
        }
    }

    // 3. Starts the background "engine" that talks to the J1 Ace
    private fun startAetherService() {
        try {
            val intent = Intent(this, AetherService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Aether Service: ONLINE", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start service: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}