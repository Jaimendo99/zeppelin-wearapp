package com.zeppelin.zeppelin_wear.data

sealed class ScreenUiState(open val message: String) {
    object PhoneNotConnected : ScreenUiState(message = "Conectar Teléfono")
    object PhoneConnected : ScreenUiState(message =  "Conexión Establecida")
    object SessionIdle : ScreenUiState("Sesión Inactiva")
    data class SessionWork(val timerState: TimerState): ScreenUiState("Sesión de Trabajo")
    data class SessionBreak(val timerState: TimerState): ScreenUiState("Sesión de Descanso")
}

data class TimerState(
    val timerPercentage: Float,
    val minutes: Int,
    val seconds: Int,
    val isRunning: Boolean = false
)