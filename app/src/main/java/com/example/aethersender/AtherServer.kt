package com.example.aethersender

import android.Manifest
import android.app.*
import android.bluetooth.*
import android.content.*
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class AetherService : Service() {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var isConnected = false
    private val targetMac = "24:68:B0:72:5C:9F"
    private val classicUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var findRingtone: android.media.Ringtone? = null

    private val musicDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getStringExtra("payload")?.let { pushToDevice(it) }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification("Aether 2.0: Active"))

        val filter = IntentFilter("com.example.smartclock.SEND_BT_DATA")
        ContextCompat.registerReceiver(this, musicDataReceiver, filter, ContextCompat.RECEIVER_EXPORTED)

        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        setupBatteryListener()
        connectToTab()
    }

    private fun connectToTab() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return

        Thread {
            while (!isConnected) {
                try {
                    val device = bluetoothAdapter?.getRemoteDevice(targetMac)
                    bluetoothSocket = device?.createRfcommSocketToServiceRecord(classicUuid)
                    bluetoothAdapter?.cancelDiscovery()
                    bluetoothSocket?.connect()

                    outputStream = bluetoothSocket?.outputStream
                    inputStream = bluetoothSocket?.inputStream
                    isConnected = true
                    updateNotification("Smart Clock: ONLINE ✅")

                    // Start listening for commands FROM the Tablet
                    listenForCommands()
                } catch (e: Exception) {
                    SystemClock.sleep(5000)
                }
            }
        }.start()
    }

    private fun listenForCommands() {
        val reader = inputStream?.bufferedReader()
        Thread {
            try {
                while (isConnected) {
                    val command = reader?.readLine() ?: break
                    handleIncomingCommand(command)
                }
            } catch (e: Exception) { handleDisconnect() }
        }.start()
    }

    private fun handleIncomingCommand(cmd: String) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when {
            cmd.startsWith("VOL:") -> {
                val level = try { cmd.substring(4).toInt() } catch (e: Exception) { 0 }
                val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (level * max) / 100, 0)
            }
            cmd == "CMD:FIND" -> triggerFindAlarm()

            // Added the Stop trigger here
            cmd == "CMD:STOP_FIND" -> findRingtone?.stop()

            cmd == "CMD:PLAY_PAUSE" -> MediaControlHelper.togglePlayPause(this)
            cmd == "CMD:NEXT" -> MediaControlHelper.skipNext(this)
            cmd == "CMD:PREV" -> MediaControlHelper.skipPrev(this)
        }
    }

    private fun triggerFindAlarm() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Force Max Volume
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        findRingtone = RingtoneManager.getRingtone(applicationContext, alarmUri)

        findRingtone?.play()

        // Auto-stop after 15 seconds so it doesn't ring forever
        Handler(Looper.getMainLooper()).postDelayed({
            findRingtone?.stop()
        }, 15000)
    }

    fun pushToDevice(data: String) {
        if (!isConnected) return
        Thread {
            try {
                outputStream?.write((data.trim() + "\n").toByteArray())
                outputStream?.flush()
            } catch (e: Exception) { handleDisconnect() }
        }.start()
    }

    private fun setupBatteryListener() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                pushToDevice("BATT:$level|${if (isCharging) "AC" else "DC"}")
            }
        }, filter)
    }

    private fun handleDisconnect() {
        isConnected = false
        updateNotification("Reconnecting...")
        try { bluetoothSocket?.close() } catch (e: Exception) {}
        connectToTab()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm?.notify(1, createNotification(text))
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, "AetherChannel")
            .setContentTitle("Aether 2.0")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("AetherChannel", "Aether Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}