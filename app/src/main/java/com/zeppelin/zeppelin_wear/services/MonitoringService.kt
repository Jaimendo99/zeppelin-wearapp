package com.zeppelin.zeppelin_wear.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zeppelin.zeppelin_wear.MainActivity
import com.zeppelin.zeppelin_wear.R
import com.zeppelin.zeppelin_wear.communication.PhoneCommunicator
import com.zeppelin.zeppelin_wear.sensors.ActivityMonitor
import com.zeppelin.zeppelin_wear.sensors.HeartRateMonitor
import com.zeppelin.zeppelin_wear.sensors.OnWristDetector
import com.zeppelin.zeppelin_wear.util.onMeanCount
import com.zeppelin.zeppelin_wear.util.windowed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MonitoringService: Service() {

    companion object {
        private const val TAG = "MonitoringService"
        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private const val NOTIFICATION_ID = 123
        private const val NOTIFICATION_CHANNEL_ID = "monitoring_channel"
        private const val HEART_RATE_WINDOW_SIZE = 20 // Number of heart rate samples to average

        private val _isOnWristState = MutableStateFlow<Boolean?>(null)
        val isOnWristState: StateFlow<Boolean?> = _isOnWristState.asStateFlow()

        private val _currentHeartRateBpm = MutableStateFlow<Int?>(null)
        val currentHeartRateBpm: StateFlow<Int?> = _currentHeartRateBpm.asStateFlow()
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val onWristDetector: OnWristDetector by inject()
    private val heartRateMonitor: HeartRateMonitor by inject()
    private val activityMonitor: ActivityMonitor by inject()
    private val phoneCommunicator: PhoneCommunicator by inject() // Inject communicator

    private var movementResetJob: Job? = null // To reset the movement flag

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Service onCreate")
        createNotificationChannel()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                Log.d(TAG, "Starting Monitoring Service")
                startForeground(NOTIFICATION_ID, createNotification())
                startMonitoring()
            }
            ACTION_STOP_SERVICE -> {
                Log.d(TAG, "Stopping Monitoring Service")
                stopMonitoring()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        if (onWristDetector.isSensorAvailable()) {
            onWristDetectorMonitoring(onWristDetector.isOnWrist)
        } else {
            Log.e(TAG, "On-wrist sensor not available.")
            _isOnWristState.value = null
        }

        if (heartRateMonitor.isSensorAvailable()) {
            onHearRateMonitoring(heartRateMonitor.heartRateBpm)
        } else {
            Log.e(TAG, "Heart rate sensor not available.")
            _currentHeartRateBpm.value = null
        }

        if (activityMonitor.isSensorAvailable()) {
            activityMonitoring(activityMonitor.significantMovementDetected)
        } else {
            Log.e(TAG, "Activity (accelerometer) sensor not available.")
        }
    }

    private fun stopMonitoring() {
        movementResetJob?.cancel()
        serviceScope.cancel()
        stopSelf()
        Log.d(TAG, "Monitoring stopped.")
    }

    private fun onWristDetectorMonitoring(isOnWrist: Flow<Boolean>){
        isOnWrist.onEach { isOnWrist ->
                Log.i(TAG, "Service: On-wrist status updated: $isOnWrist")
                _isOnWristState.value = isOnWrist
                updateNotification(isOnWrist, _currentHeartRateBpm.value)
                serviceScope.launch {
                    if (isOnWrist) phoneCommunicator.sendOnWristEvent()
                    else phoneCommunicator.sendOffWristEvent()
                }
            }
            .catch { e -> Log.e(TAG, "Error in onWristDetector flow", e); _isOnWristState.value = null }
            .launchIn(serviceScope)
    }
 
    private fun onHearRateMonitoring(heartRateBpm: Flow<Int>){
        heartRateBpm.onEach { bpm ->
                Log.i(TAG, "Service: Heart Rate BPM: $bpm")
                _currentHeartRateBpm.value = bpm
                _isOnWristState.value?.let { updateNotification(it, bpm) }
            }
            .windowed(HEART_RATE_WINDOW_SIZE).onEach {
                phoneCommunicator.sendHeartRateSummary(
                    it.last(),
                    it.size,
                    it.average().toFloat()
                )
            }
            .catch { e -> Log.e(TAG, "Error in heartRateMonitor flow", e); _currentHeartRateBpm.value = null }
            .launchIn(serviceScope)
    }

    private fun  activityMonitoring(significantMovementDetected: Flow<Float>) {
        significantMovementDetected.onMeanCount(5) { it.sum()/it.size }
            .onEach { magnitude ->
                Log.i(TAG, "Service: Significant movement detected (Magnitude: $magnitude)")
                movementResetJob?.cancel()
                movementResetJob = serviceScope.launch { delay(5000) }
                phoneCommunicator.sendMovementDetectedEvent(magnitude)
            }
            .catch { e -> Log.e(TAG, "Error in activityMonitor flow", e) }
            .launchIn(serviceScope)
    }
    
    private fun createNotification(isOnWrist: Boolean? = _isOnWristState.value, heartRate: Int? = _currentHeartRateBpm.value): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, pendingIntentFlags
        )

        val statusText = StringBuilder()
        when (isOnWrist) {
            true -> statusText.append("ON wrist")
            false -> statusText.append("OFF wrist")
            null -> statusText.append("Wrist: Unknown")
        }
        heartRate?.let {
            statusText.append(" | HR: $it bpm")
        } ?: run {
            statusText.append(" | HR: N/A")
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Focus Session Active")
            .setContentText(statusText.toString())
            .setSmallIcon(R.drawable.logo_on_dark) // Replace with your app icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(isOnWrist: Boolean, heartRate: Int?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            NotificationManagerCompat.from(this)
                .notify(NOTIFICATION_ID, createNotification(isOnWrist, heartRate))
        } else {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot update notification.")
        }
    }


    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Monitoring Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }


    override fun onBind(p0: Intent?): IBinder? {
       return null
    }
}

