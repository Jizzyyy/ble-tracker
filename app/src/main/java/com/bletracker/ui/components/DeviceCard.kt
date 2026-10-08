package com.bletracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bletracker.domain.model.BleDevice
import com.bletracker.ui.theme.PrimaryBlue
import com.bletracker.ui.theme.SurfaceBorderLight
import com.bletracker.ui.theme.SurfaceLight
import com.bletracker.ui.theme.SurfaceSubtle
import com.bletracker.ui.theme.TextPrimary
import com.bletracker.ui.theme.TextTertiary

@Composable
fun DeviceCard(
    device: BleDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zoneColor = device.zone.getColor()
    val zoneBg = device.zone.getBgColor()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLight)
            .border(1.dp, SurfaceBorderLight, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(zoneBg)
                        .border(1.dp, zoneColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = zoneColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = device.address,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }

                SignalBadge(zone = device.zone)

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Radar View",
                    tint = TextTertiary,
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics row: Raw RSSI, Smoothed RSSI, Estimated Distance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceSubtle)
                    .border(1.dp, SurfaceBorderLight.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricItem(
                    label = "Raw RSSI",
                    value = "${device.rssi} dBm",
                    valueColor = TextPrimary
                )

                MetricItem(
                    label = "Filtered",
                    value = "${device.smoothedRssi} dBm",
                    valueColor = zoneColor
                )

                MetricItem(
                    label = "Est. Jarak",
                    value = if (device.estimatedDistance > 0) "~${device.estimatedDistance} m" else "--",
                    valueColor = PrimaryBlue
                )

                SignalStrengthBars(rssi = device.smoothedRssi, activeColor = zoneColor)
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextTertiary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun SignalStrengthBars(
    rssi: Int,
    activeColor: Color
) {
    val totalBars = 4
    val activeBars = when {
        rssi > -50 -> 4
        rssi > -70 -> 3
        rssi > -80 -> 2
        rssi > -90 -> 1
        else -> 0
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        for (i in 1..totalBars) {
            val barHeight = (4 + (i * 3.5)).dp
            val isLit = i <= activeBars
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isLit) activeColor else Color(0xFFCBD5E1))
            )
        }
    }
}
