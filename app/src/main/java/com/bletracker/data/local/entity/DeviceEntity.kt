package com.bletracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bletracker.domain.model.BleDevice
import com.bletracker.domain.util.RssiUtil

@Entity(tableName = "device_history")
data class DeviceEntity(
    @PrimaryKey
    val address: String,
    val name: String?,
    val lastRssi: Int,
    val lastSeenTimestamp: Long
)

fun DeviceEntity.toDomainModel(): BleDevice = BleDevice(
    name = name,
    address = address,
    rssi = lastRssi,
    smoothedRssi = lastRssi,
    estimatedDistance = RssiUtil.estimateDistance(lastRssi),
    zone = RssiUtil.classifyZone(lastRssi),
    lastSeenTimestamp = lastSeenTimestamp
)

fun BleDevice.toEntity(): DeviceEntity = DeviceEntity(
    address = address,
    name = name,
    lastRssi = rssi,
    lastSeenTimestamp = lastSeenTimestamp
)
