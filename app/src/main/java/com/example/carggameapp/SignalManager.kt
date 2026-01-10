package com.example.carggameapp

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast

class SignalManager private constructor(context: Context) {

    private val context = context.applicationContext
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        @Volatile
        private var instance: SignalManager? = null

        fun init(context: Context): SignalManager {
            return instance ?: synchronized(this) {
                instance ?: SignalManager(context).also { instance = it }
            }
        }

        fun getInstance(): SignalManager {
            return instance ?: throw IllegalStateException("SignalManager must be initialized by calling init(context) before use.")
        }
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    fun playCrashSound() {
        // Stop previous sound if playing
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        // Play new sound
        mediaPlayer = MediaPlayer.create(context, R.raw.crash)
        mediaPlayer?.start()
    }
}