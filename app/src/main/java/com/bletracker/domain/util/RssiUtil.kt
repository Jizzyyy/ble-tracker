package com.bletracker.domain.util

import com.bletracker.domain.model.SignalZone
import kotlin.math.pow
import kotlin.math.roundToInt

object RssiUtil {
    // Reference RSSI at 1-meter calibrated against study case specifications (-30 dBm boundary)
    private const val DEFAULT_TX_POWER = -30.0

    // Calibrated path loss exponent matching real BLE radio propagation tiers
    private const val PATH_LOSS_EXPONENT = 3.6

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
