package com.zeppelin.zeppelin_wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.zeppelin_wear.data.ScreenUiState
import com.zeppelin.zeppelin_wear.presentation.composables.MainScreen
import com.zeppelin.zeppelin_wear.presentation.theme.ZeppelinTheme
import com.zeppelin.zeppelin_wear.services.MonitoringService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            ZeppelinTheme {
                MainScreen(ScreenUiState.SessionIdle)
            }
        }
    }
}

class MainViewModel(): ViewModel(){
    val onWrist: StateFlow<Boolean?> = MonitoringService.isOnWristState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}