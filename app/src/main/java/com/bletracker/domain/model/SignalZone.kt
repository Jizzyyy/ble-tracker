package com.bletracker.domain.model

enum class SignalZone(
    val title: String,
    val subtitle: String,
    val distanceDescription: String,
    val minRssi: Int,
    val maxRssi: Int
) {
    IMMEDIATE(
        title = "Sangat Kuat",
        subtitle = "Sangat Dekat",
        distanceDescription = "< 1 meter",
        minRssi = -30,
        maxRssi = -10
    ),
    NEAR(
        title = "Kuat",
        subtitle = "Dekat",
        distanceDescription = "1 – 3 meter",
        minRssi = -50,
        maxRssi = -30
    ),
    MID(
        title = "Cukup / Baik",
        subtitle = "Sedang",
        distanceDescription = "3 – 10 meter",
        minRssi = -70,
        maxRssi = -50
    ),
    WEAK(
        title = "Lemah",
        subtitle = "Jauh",
        distanceDescription = "10 – 20 meter",
        minRssi = -80,
        maxRssi = -70
    ),
    VERY_WEAK(
        title = "Sangat Lemah",
        subtitle = "Putus-putus",
        distanceDescription = "> 20 meter",
        minRssi = -90,
        maxRssi = -80
    ),
    LOST(
        title = "Sinyal Hilang",
        subtitle = "Terputus",
        distanceDescription = "Di luar jangkauan",
        minRssi = Int.MIN_VALUE,
        maxRssi = -90
    );

    companion object {
        fun fromRssi(rssi: Int): SignalZone = when {
            rssi > -30 -> IMMEDIATE
            rssi > -50 -> NEAR
            rssi > -70 -> MID
            rssi > -80 -> WEAK
            rssi >= -90 -> VERY_WEAK
            else -> LOST
        }
    }
}
