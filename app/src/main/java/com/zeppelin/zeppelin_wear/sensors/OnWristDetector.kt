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

class OnWristDetector(
    private val sensorManager: SensorManager,
) {
    companion object {
        private val TAG = "OnWristDetector"
    }

    private val onWristSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT)

    val isOnWrist: Flow<Boolean> = callbackFlow {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT) {
                        val onWrist = it.values[0] != 0.0f // 1.0f is on-wrist
                        Log.d(TAG, "On-wrist status: $onWrist")
                        launch { send(onWrist) }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d(TAG, "Accuracy changed: $accuracy for sensor: ${sensor?.name}")
            }
        }

        if (onWristSensor == null) {
            Log.e(TAG, "Low latency off-body sensor not available.")
            launch { send(true) } // Or false, or throw an exception
            close(IllegalStateException("Off-body sensor not available"))
            return@callbackFlow
        }

        Log.d(TAG, "Registering OnWristDetector listener")
        val registered = sensorManager.registerListener(
            sensorEventListener,
            onWristSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        if (!registered) {
            Log.e(TAG, "Failed to register OnWristDetector listener")
            close(IllegalStateException("Failed to register off-body sensor listener"))
            return@callbackFlow
        }

        awaitClose {
            Log.d(TAG, "Unregistering OnWristDetector listener")
            sensorManager.unregisterListener(sensorEventListener)
        }

    }

    fun isSensorAvailable(): Boolean = onWristSensor != null
}