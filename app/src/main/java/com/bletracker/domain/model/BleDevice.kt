package com.bletracker.domain.model

data class BleDevice(
    val name: String?,
    val address: String,
    val rssi: Int,
    val smoothedRssi: Int = rssi,
    val estimatedDistance: Double,
    val zone: SignalZone,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = name?.takeIf { it.isNotBlank() } ?: "Unknown Device"
}
