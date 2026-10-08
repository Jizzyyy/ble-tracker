package com.bletracker.domain

import com.bletracker.domain.model.SignalZone
import com.bletracker.domain.util.RssiUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RssiUtilTest {

    @Test
    fun `classifyZone maps -10 to -30 dBm as IMMEDIATE`() {
        assertEquals(SignalZone.IMMEDIATE, RssiUtil.classifyZone(-15))
        assertEquals(SignalZone.IMMEDIATE, RssiUtil.classifyZone(-29))
    }

    @Test
    fun `classifyZone maps -31 to -50 dBm as NEAR`() {
        assertEquals(SignalZone.NEAR, RssiUtil.classifyZone(-35))
        assertEquals(SignalZone.NEAR, RssiUtil.classifyZone(-49))
    }

    @Test
    fun `classifyZone maps -51 to -70 dBm as MID`() {
        assertEquals(SignalZone.MID, RssiUtil.classifyZone(-55))
        assertEquals(SignalZone.MID, RssiUtil.classifyZone(-69))
    }

    @Test
    fun `classifyZone maps -71 to -80 dBm as WEAK`() {
        assertEquals(SignalZone.WEAK, RssiUtil.classifyZone(-72))
        assertEquals(SignalZone.WEAK, RssiUtil.classifyZone(-79))
    }

    @Test
    fun `classifyZone maps -81 to -90 dBm as VERY_WEAK`() {
        assertEquals(SignalZone.VERY_WEAK, RssiUtil.classifyZone(-85))
        assertEquals(SignalZone.VERY_WEAK, RssiUtil.classifyZone(-90))
    }

    @Test
    fun `classifyZone maps below -90 dBm as LOST`() {
        assertEquals(SignalZone.LOST, RssiUtil.classifyZone(-91))
        assertEquals(SignalZone.LOST, RssiUtil.classifyZone(-105))
    }

    @Test
    fun `estimateDistance returns valid ranges according to signal strength`() {
        val immediateDist = RssiUtil.estimateDistance(-20)
        assertTrue("Distance should be < 1.0m for -20 dBm, was $immediateDist", immediateDist < 1.0)

        val nearDist = RssiUtil.estimateDistance(-40)
        assertTrue("Distance should be between 1.0m and 3.5m for -40 dBm, was $nearDist", nearDist in 1.0..3.5)

        val weakDist = RssiUtil.estimateDistance(-75)
        assertTrue("Distance should be > 10.0m for -75 dBm, was $weakDist", weakDist > 10.0)
    }

    @Test
    fun `estimateDistance handles zero gracefully`() {
        assertEquals(-1.0, RssiUtil.estimateDistance(0), 0.01)
    }

    @Test
    fun `smoothRssi dampens fluctuating spikes`() {
        val initial = -50
        val spike = -80

        val smoothed = RssiUtil.smoothRssi(initial, spike)

        // With alpha 0.35: 0.35 * -80 + 0.65 * -50 = -28 - 32.5 = -60.5 -> -60 or -61
        assertTrue("Smoothed value must be strictly between initial and spike", smoothed in -75..-55)
    }

    @Test
    fun `displayName returns Unknown Device when name is null or blank`() {
        val deviceNull = com.bletracker.domain.model.BleDevice(
            name = null,
            address = "4F:60:56:43:9D:32",
            rssi = -56,
            estimatedDistance = 5.3,
            zone = SignalZone.MID
        )
        assertEquals("Unknown Device", deviceNull.displayName)

        val deviceBlank = deviceNull.copy(name = "   ")
        assertEquals("Unknown Device", deviceBlank.displayName)

        val deviceNamed = deviceNull.copy(name = "Mi Band 7")
        assertEquals("Mi Band 7", deviceNamed.displayName)
    }
}
