package com.bletracker.data.repository

import com.bletracker.data.local.dao.DeviceDao
import com.bletracker.data.local.entity.toDomainModel
import com.bletracker.data.local.entity.toEntity
import com.bletracker.domain.model.BleDevice
import com.bletracker.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl(
    private val deviceDao: DeviceDao
) : HistoryRepository {

    override fun getHistoryDevices(): Flow<List<BleDevice>> {
        return deviceDao.getAllDevices().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getDevice(address: String): BleDevice? {
        return deviceDao.getDeviceByAddress(address)?.toDomainModel()
    }

    override suspend fun saveDevice(device: BleDevice) {
        deviceDao.upsertDevice(device.toEntity())
    }

    override suspend fun clearHistory() {
        deviceDao.clearAll()
    }
}
