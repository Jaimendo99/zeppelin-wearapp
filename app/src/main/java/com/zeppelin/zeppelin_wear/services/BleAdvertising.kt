package com.zeppelin.zeppelin_wear.services

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.ContextCompat.getSystemService
import java.util.UUID

private val SERVICE_UUID =
    UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")

fun startBleAdvertising() {
    val btMgr = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter = btMgr.adapter
    if (!adapter.isMultipleAdvertisementSupported) {
        Log.e(TAG, "No BLE-peripheral on watch")
        return
    }
    val advertiser = adapter.bluetoothLeAdvertiser ?: run {
        Log.e(TAG, "Advertiser == null")
        return
    }
    val settings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
        .setConnectable(false)
        .build()
    val data = AdvertiseData.Builder()
        .addServiceUuid(ParcelUuid(SERVICE_UUID))
        .setIncludeDeviceName(true)
        .build()

    advertiser.startAdvertising(
        settings, data,
        object : AdvertiseCallback() {
            override fun onStartSuccess(s: AdvertiseSettings) {
                Log.d(TAG, "Advertising OK")
            }
            override fun onStartFailure(err: Int) {
                Log.e(TAG, "Advertise failed: $err")
            }
        }
    )
}