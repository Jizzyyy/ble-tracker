package com.bletracker.domain.util

import com.bletracker.domain.model.SignalZone
import kotlin.math.pow
import kotlin.math.roundToInt

object RssiUtil {
    // 1-meter reference RSSI in dBm (standard BLE beacon calibration)
    private const val DEFAULT_TX_POWER = -59.0

    // Path loss exponent (2.0 = free space, 2.5 - 3.0 = indoor environment)
    private const val PATH_LOSS_EXPONENT = 2.2

    // Smoothing factor for Exponential Moving Average (0.0 to 1.0)
    private const val EMA_ALPHA = 0.35

    fun estimateDistance(rssi: Int, txPower: Double = DEFAULT_TX_POWER): Double {
        if (rssi == 0) return -1.0
        val ratio = (txPower - rssi) / (10.0 * PATH_LOSS_EXPONENT)
        val distance = 10.0.pow(ratio)
        return (distance * 10.0).roundToInt() / 10.0
    }

    fun smoothRssi(currentSmoothed: Int, newRssi: Int): Int {
        if (currentSmoothed == 0) return newRssi
        val smoothed = (EMA_ALPHA * newRssi) + ((1.0 - EMA_ALPHA) * currentSmoothed)
        return smoothed.roundToInt()
    }

    fun classifyZone(rssi: Int): SignalZone = SignalZone.fromRssi(rssi)
}
