package com.zeppelin.zeppelin_wear.di

import android.content.Context
import android.hardware.SensorManager
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import com.zeppelin.zeppelin_wear.communication.PhoneCommunicator
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
    single<NodeClient> { Wearable.getNodeClient(androidContext()) }
    single<MessageClient> { Wearable.getMessageClient(androidContext()) }
    single<DataClient> { Wearable.getDataClient(androidContext()) } // For later
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

val communicationModule = module {
    single { PhoneCommunicator(get(), get(), get()) }
}