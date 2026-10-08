package com.bletracker.domain.repository

import com.bletracker.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistoryDevices(): Flow<List<BleDevice>>
    suspend fun saveDevice(device: BleDevice)
    suspend fun clearHistory()
}
