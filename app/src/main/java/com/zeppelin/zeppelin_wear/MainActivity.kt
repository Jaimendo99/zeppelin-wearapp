package com.zeppelin.zeppelin_wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.zeppelin.zeppelin_wear.data.ScreenUiState
import com.zeppelin.zeppelin_wear.presentation.composables.MainScreen
import com.zeppelin.zeppelin_wear.presentation.theme.ZeppelinTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            ZeppelinTheme {
                MainScreen(ScreenUiState.SessionIdle, timerPercentage = 60f)
            }
        }
    }
}