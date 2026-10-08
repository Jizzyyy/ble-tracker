package com.bletracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.bletracker.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Upsert
    suspend fun upsertDevice(device: DeviceEntity)

    @Upsert
    suspend fun upsertDevices(devices: List<DeviceEntity>)

    @Query("SELECT * FROM device_history ORDER BY lastSeenTimestamp DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM device_history WHERE address = :address LIMIT 1")
    suspend fun getDeviceByAddress(address: String): DeviceEntity?

    @Query("DELETE FROM device_history")
    suspend fun clearAll()
}
