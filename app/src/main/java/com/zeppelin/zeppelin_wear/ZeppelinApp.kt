package com.zeppelin.zeppelin_wear

import android.app.Application
import com.zeppelin.zeppelin_wear.di.appModule
import com.zeppelin.zeppelin_wear.di.sensorsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ZeppelinApp : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@ZeppelinApp)
            modules(
                appModule,
                sensorsModule
            )
        }
    }
}
