package com.zeppelin.zeppelin_wear.services

import android.content.Intent
import com.google.android.gms.wearable.WearableListenerService
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.zeppelin.zeppelin_wear.communication.WearOsPaths

class MyWatchListenerService : WearableListenerService() {

    private val context by lazy { applicationContext }

    companion object {
        private const val TAG = "WatchListenerService"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Watch: Message received with path: ${messageEvent.path}")
        when(messageEvent.path){
           WearOsPaths.CommandStartMonitoring.path -> {
               val serviceIntent = Intent(context, MonitoringService::class.java).apply {
                   action = MonitoringService.ACTION_START_SERVICE
               }
               ContextCompat.startForegroundService(context, serviceIntent)
           }
           WearOsPaths.CommandStopMonitoring.path -> {
               val serviceIntent = Intent(context, MonitoringService::class.java).apply {
                   action = MonitoringService.ACTION_STOP_SERVICE
               }
               context.startService(serviceIntent) // Can use startService to send stop command
           }
           else -> { Log.d(TAG, "Watch: Unhandled message) path: ${messageEvent.path}") }
        }
    }

    override fun onConnectedNodes(connectedNodes: MutableList<Node>) {
        super.onConnectedNodes(connectedNodes)
        val isConnected = connectedNodes.isNotEmpty()
        Log.i(TAG, "Watch: Connected nodes changed. Currently connected: $isConnected")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MyWatchListenerService onCreate") // Very first line
    }

}