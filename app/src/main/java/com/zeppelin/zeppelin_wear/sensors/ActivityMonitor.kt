package com.zeppelin.zeppelin_wear.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class ActivityMonitor(
    private val sensorManager: SensorManager,
) {
    companion object {
        private const val TAG = "ActivityMonitor"
        private const val MOVEMENT_THRESHOLD = 12.0 // m/s^2
    }

    private var accelerometerSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val significantMovementDetected: Flow<Float> = callbackFlow {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        val x = it.values[0]
                        val y = it.values[1]
                        val z = it.values[2]

                        val magnitude = sqrt(x * x + y * y + z * z)


                        if (magnitude > MOVEMENT_THRESHOLD) {
                            Log.d(TAG, "Significant movement detected! Magnitude: $magnitude")
                            launch { send(magnitude.toFloat()) }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d(TAG, "Accuracy changed: $accuracy for sensor: ${sensor?.name}")
            }
        }

        if (accelerometerSensor == null) {
            Log.e(TAG, "Accelerometer sensor not available.")
            close(IllegalStateException("Accelerometer sensor not available on this device."))
            return@callbackFlow
        }

        Log.d(TAG, "Registering ActivityMonitor listener")
        val registered = sensorManager.registerListener(
            sensorEventListener,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_NORMAL // Or SENSOR_DELAY_GAME
        )

        if (!registered) {
            Log.e(TAG, "Failed to register ActivityMonitor listener")
            close(IllegalStateException("Failed to register accelerometer sensor listener"))
            return@callbackFlow
        }

        awaitClose {
            Log.d(TAG, "Unregistering ActivityMonitor listener")
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    fun isSensorAvailable(): Boolean = accelerometerSensor != null
}