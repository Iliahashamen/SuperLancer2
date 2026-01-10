package com.example.carggameapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class TiltDetector(context: Context, private val callback: TiltCallback) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var timestamp: Long = 0L

    interface TiltCallback {
        fun tiltLeft()
        fun tiltRight()
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]

            calculateTilt(x, y)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Not needed
        }
    }

    private fun calculateTilt(x: Float, y: Float) {
        // Debounce: Only allow a move every 300ms so the car doesn't jitter
        if (System.currentTimeMillis() - timestamp > 300) {

            // X > 3.0 means tilted LEFT (on most phones)
            // X < -3.0 means tilted RIGHT
            // You might need to swap these depending on device orientation

            if (x >= 3.0) {
                timestamp = System.currentTimeMillis()
                callback.tiltLeft()
            } else if (x <= -3.0) {
                timestamp = System.currentTimeMillis()
                callback.tiltRight()
            }
        }
    }

    fun start() {
        sensorManager.registerListener(
            sensorEventListener,
            sensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stop() {
        sensorManager.unregisterListener(sensorEventListener)
    }
}