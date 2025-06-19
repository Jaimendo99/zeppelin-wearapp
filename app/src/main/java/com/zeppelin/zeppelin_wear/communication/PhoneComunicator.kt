package com.zeppelin.zeppelin_wear.communication

import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import kotlinx.coroutines.tasks.await

class PhoneCommunicator(
    private val nodeClient: NodeClient,
    private val messageClient: MessageClient,
    private val dataClient: DataClient
) {
    companion object {
        private const val TAG = "PhoneCommunicator"
        val PATH_EVENT_OFF_WRIST = WearOsPaths.EventOffWrist.path
        val PATH_EVENT_ON_WRIST = WearOsPaths.EventOnWrist.path
        val PATH_EVENT_MOVEMENT_DETECTED = WearOsPaths.EventMovementDetected.path
        val DATA_HEART_RATE_SUMMARY = WearOsPaths.DataHeartRateSummary.path
    }

    suspend fun sendOffWristEvent() {
        sendMessageToNearbyNodes(PATH_EVENT_OFF_WRIST, "off_wrist".toByteArray())
    }

    suspend fun sendOnWristEvent() {
        sendMessageToNearbyNodes(PATH_EVENT_ON_WRIST, "on_wrist".toByteArray())
    }

    suspend fun sendMovementDetectedEvent(speed: Float) {
        sendMessageToNearbyNodes(PATH_EVENT_MOVEMENT_DETECTED, speed.toString().toByteArray())
    }

    suspend fun sendHeartRateSummary(rate: Int, count: Int, mean: Float) {
        val payload = "$rate,$count,$mean".toByteArray()
        sendMessageToNearbyNodes(DATA_HEART_RATE_SUMMARY, payload)
    }

    // Generic message sender
    private suspend fun sendMessageToNearbyNodes(path: String, payload: ByteArray) {
        try {
            val nodes: List<Node> = nodeClient.connectedNodes.await()
            if (nodes.isEmpty()) {
                Log.w(TAG, "No connected nodes found to send message: $path")
                return
            }
            nodes.forEach { node ->
                Log.d(TAG, "Preparing to send to Node ID: ${node.id}, Name: ${node.displayName}, Path: '$path', Payload: '${String(payload)}'") // DETAILED LOG
                messageClient.sendMessage(node.id, path, payload)
                    .addOnSuccessListener { Log.d(TAG, "Message '$path' sent to ${node.displayName}") }
                    .addOnFailureListener { e -> Log.e(TAG, "Failed to send message '$path' to ${node.displayName}", e) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting connected nodes or sending message", e)
        }
    }
}