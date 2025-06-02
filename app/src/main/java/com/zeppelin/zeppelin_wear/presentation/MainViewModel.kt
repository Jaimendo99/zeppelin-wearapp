package com.zeppelin.zeppelin_wear.presentation

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.zeppelin_wear.data.ScreenUiState
import com.zeppelin.zeppelin_wear.services.MonitoringService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class MainViewModel() : ViewModel(){

    private val context: Context by inject(Context::class.java)

    private var isServiceRunning = false

    private val _screenUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.SessionIdle)
    val screenUiState: StateFlow<ScreenUiState> = _screenUiState

    val onWrist: StateFlow<Boolean?> = MonitoringService.isOnWristState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        try{
            updateState()
        }catch (e: Exception){
            Log.e("MainViewModel", "Error starting service", e)
        }
    }

    private fun updateState(){
        viewModelScope.launch {
            onWrist.collect {
                _screenUiState.value = if (it == true) {
                    ScreenUiState.SessionIdle
                } else {
                    ScreenUiState.OnWristOff
                }
            }
        }
    }

    fun startMonitoringService() {
        Log.d("MainViewModel", "Starting monitoring service")
        if (!isServiceRunning) {
            Log.d("MainViewModel", "Starting monitoring service")
            val serviceIntent = Intent(context, MonitoringService::class.java).apply {
                action = MonitoringService.ACTION_START_SERVICE
            }
            ContextCompat.startForegroundService(context, serviceIntent)
            isServiceRunning = true
        }else{
            Log.d("MainViewModel", "Monitoring service is already running")
        }
    }

    private fun stopMonitoringService(context: Context) {
        if (isServiceRunning) {
            val serviceIntent = Intent(context, MonitoringService::class.java).apply {
                action = MonitoringService.ACTION_STOP_SERVICE
            }
            context.startService(serviceIntent) // Can use startService to send stop command
            isServiceRunning = false
        }
    }
}