package com.bletracker.domain.repository

import com.bletracker.domain.model.BleDevice
import kotlinx.coroutines.flow.StateFlow

interface BleRepository {
    val devices: StateFlow<List<BleDevice>>
    val isScanning: StateFlow<Boolean>
    val scanError: StateFlow<String?>
    val isBluetoothEnabled: StateFlow<Boolean>

    fun startScan()
    fun stopScan()
    fun getDevice(address: String): BleDevice?
    fun refreshBluetoothStatus()
}
