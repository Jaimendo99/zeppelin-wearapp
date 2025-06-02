package com.zeppelin.zeppelin_wear.di

import android.content.Context
import android.hardware.SensorManager
import com.zeppelin.zeppelin_wear.presentation.MainViewModel
import com.zeppelin.zeppelin_wear.sensors.ActivityMonitor
import com.zeppelin.zeppelin_wear.sensors.HeartRateMonitor
import com.zeppelin.zeppelin_wear.sensors.OnWristDetector
import com.zeppelin.zeppelin_wear.services.MonitoringService
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<SensorManager>{
        androidContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
}

val sensorsModule = module {
    single { OnWristDetector(get()) }
    single { HeartRateMonitor(get()) }
    single { ActivityMonitor(get()) }
    single { MonitoringService() }
}

val mainViewModel = module{
    viewModel {MainViewModel()}
}