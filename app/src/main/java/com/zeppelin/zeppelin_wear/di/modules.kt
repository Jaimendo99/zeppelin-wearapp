package com.zeppelin.zeppelin_wear.di

import android.content.Context
import android.hardware.SensorManager
import com.zeppelin.zeppelin_wear.sensors.OnWristDetector
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.dsl.single

val appModule = module {
    single<Context>{androidContext()}
    single<SensorManager>{
        androidContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
}

val sensorsModule = module {
    single { OnWristDetector(get(), get()) }
}