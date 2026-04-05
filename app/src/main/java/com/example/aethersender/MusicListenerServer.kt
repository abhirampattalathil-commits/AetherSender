package com.example.aethersender

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

class MusicListenerService : NotificationListenerService() {
    private var lastSong = ""

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: return
        val artist = extras.getString("android.text") ?: "Unknown"

        if (title == lastSong) return
        lastSong = title

        val icon = extras.getParcelable<Bitmap>("android.largeIcon")
        val base64Image = icon?.let { encodeHDImage(it) } ?: "NO_IMAGE"

        val payload = "MUSIC:$title|$artist|$base64Image"
        sendBroadcast(Intent("com.example.smartclock.SEND_BT_DATA").putExtra("payload", payload))
    }

    private fun encodeHDImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        // HD Scaling: 500x500 at 80% quality
        val scaled = Bitmap.createScaledBitmap(bm, 500, 500, true)
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT).replace("\n", "")
    }
}