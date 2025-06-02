package com.zeppelin.zeppelin_wear

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.zeppelin.zeppelin_wear.presentation.MainViewModel
import com.zeppelin.zeppelin_wear.presentation.composables.MainScreen
import com.zeppelin.zeppelin_wear.presentation.theme.ZeppelinTheme
import org.koin.androidx.compose.koinViewModel


class MainActivity : ComponentActivity() {

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGranted = true
            permissions.entries.forEach {
                if (!it.value) allGranted = false
            }
            if (allGranted) {
                Log.d("MainActivity", "All permissions granted.")
            } else {
                println("Permissions not granted by the user.")
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

        checkPermissionsAndStartService()

        setContent {
            val mainViewmodel = koinViewModel<MainViewModel>()

            ZeppelinTheme {
                MainScreen(mainViewmodel)
            }
        }
    }

    private fun checkPermissionsAndStartService() {
        val requiredPermissions = mutableListOf(Manifest.permission.BODY_SENSORS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isEmpty()) {
            Log.d("MainActivity", "All required permissions are granted.")
        } else {
            requestPermissionsLauncher.launch(permissionsToRequest)
        }
    }
}

