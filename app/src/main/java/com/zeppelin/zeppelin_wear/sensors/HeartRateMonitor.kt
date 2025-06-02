package com.zeppelin.zeppelin_wear.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class HeartRateMonitor(
    private val sensorManager: SensorManager,
) {
    companion object {
        private const val TAG = "HeartRateMonitor"
    }

    private var heartRateSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    val heartRateBpm: Flow<Int> = callbackFlow {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_HEART_RATE) {
                        val bpm = it.values[0].toInt()
                        if (bpm > 0) { // Often, initial readings can be 0
                            Log.d(TAG, "Heart Rate BPM: $bpm")
                            launch { send(bpm) }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d(TAG, "Accuracy changed: $accuracy for sensor: ${sensor?.name}")
                // For simplicity, we're not handling it explicitly here yet.
            }
        }

        if (heartRateSensor == null) {
            Log.e(TAG, "Heart rate sensor not available.")
            close(IllegalStateException("Heart rate sensor not available on this device."))
            return@callbackFlow
        }

        Log.d(TAG, "Registering HeartRateMonitor listener")
        val registered = sensorManager.registerListener(
            sensorEventListener,
            heartRateSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        if (!registered) {
            Log.e(TAG, "Failed to register HeartRateMonitor listener")
            close(IllegalStateException("Failed to register heart rate sensor listener"))
            return@callbackFlow
        }

        awaitClose {
            Log.d(TAG, "Unregistering HeartRateMonitor listener")
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    fun isSensorAvailable(): Boolean = heartRateSensor != null
}