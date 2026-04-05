package com.example.aethersender

import android.content.Context
import android.view.KeyEvent
import android.media.AudioManager

object MediaControlHelper {
    fun togglePlayPause(context: Context) = sendKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    fun skipNext(context: Context) = sendKeyEvent(context, KeyEvent.KEYCODE_MEDIA_NEXT)
    fun skipPrev(context: Context) = sendKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)

    private fun sendKeyEvent(context: Context, keyCode: Int) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
}