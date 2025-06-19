package com.zeppelin.zeppelin_wear

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.zeppelin.zeppelin_wear.presentation.MainViewModel
import com.zeppelin.zeppelin_wear.presentation.composables.MainScreen
import com.zeppelin.zeppelin_wear.presentation.theme.ZeppelinTheme
import com.zeppelin.zeppelin_wear.services.MonitoringService
import org.koin.androidx.compose.koinViewModel


class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    // 1) Launcher for multiple permission requests
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            // Find out which (if any) permissions were denied
            val denied = results.filterValues { granted -> !granted }.keys
            if (denied.isEmpty()) {
                Log.d(TAG, "All permissions granted.")
                startMonitoringService()
            } else {
                Log.w(TAG, "Permissions denied: $denied")
                Toast.makeText(
                    this,
                    "Required permissions not granted: $denied",
                    Toast.LENGTH_LONG
                ).show()
                // Optionally: guide user to Settings
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // In an appropriate place like MonitoringService.onCreate() or MainActivity.onCreate()
        val sm = getSystemService(SENSOR_SERVICE) as SensorManager
        val allSensors: List<Sensor> = sm.getSensorList(Sensor.TYPE_ALL)
        Log.d("SensorList", "Available sensors on this emulator:")
        var foundOffBodySensor = false
        for (sensor in allSensors) {
            Log.d("SensorList", "Name: ${sensor.name}, Type: ${sensor.type}, Vendor: ${sensor.vendor}")
            if (sensor.type == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT) {
                Log.i("SensorList", ">>> Found TYPE_LOW_LATENCY_OFFBODY_DETECT (Type ID: ${Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT})")
                foundOffBodySensor = true
            }
        }
        if (!foundOffBodySensor) {
            Log.e("SensorList", "TYPE_LOW_LATENCY_OFFBODY_DETECT (Type ID: 34) was NOT found in the list of all sensors.")
        }

        if (sm.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT) == null) {
            Log.e("SensorList", "Confirming: getDefaultSensor for TYPE_LOW_LATENCY_OFFBODY_DETECT is returning null.")
        } else {
            Log.i("SensorList", "Good news: getDefaultSensor for TYPE_LOW_LATENCY_OFFBODY_DETECT is NOT null (but might not have been found in the full list if logic is off).")
        }

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            val mainViewmodel = koinViewModel<MainViewModel>()

            ZeppelinTheme {
                MainScreen(mainViewmodel)
            }
        }

        // … your splash‐screen, sensor enumeration, setTheme, setContent, etc. …



        // 2) Kick off our permission check → service start
        checkPermissionsAndStartService()
    }

    private fun checkPermissionsAndStartService() {
        // 3) Build the list of “dangerous” perms we need
        val perms = mutableListOf<String>().apply {
            add(Manifest.permission.BODY_SENSORS)
            // Android 13+ needs notification permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            // Android 12+ (S) needs these for BLE advertise/connect
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                // if you ever scan on the watch, also add BLUETOOTH_SCAN
                // add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }

        // 4) Filter out the ones we already have
        val toRequest = perms.filter { perm ->
            ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (toRequest.isEmpty()) {
            // All already granted
            Log.d(TAG, "All required permissions are already granted.")
            startMonitoringService()
        } else {
            // Fire off the system dialog
            requestPermissionsLauncher.launch(toRequest)
        }
    }

    private fun startMonitoringService() {
        Intent(this, MonitoringService::class.java).also { intent ->
            intent.action = MonitoringService.ACTION_START_SERVICE
            // Android O+ requires startForegroundService for a foreground service
            ContextCompat.startForegroundService(this, intent)
        }
    }
}