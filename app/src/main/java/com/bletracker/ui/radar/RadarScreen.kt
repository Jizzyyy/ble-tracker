package com.bletracker.ui.radar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bletracker.domain.model.SignalZone
import com.bletracker.ui.components.SignalBadge
import com.bletracker.ui.components.getColor
import com.bletracker.ui.theme.BackgroundLight
import com.bletracker.ui.theme.PrimaryBlue
import com.bletracker.ui.theme.SurfaceBorderLight
import com.bletracker.ui.theme.SurfaceLight
import com.bletracker.ui.theme.SurfaceSubtle
import com.bletracker.ui.theme.TextPrimary
import com.bletracker.ui.theme.TextSecondary
import com.bletracker.ui.theme.TextTertiary
import com.bletracker.ui.theme.ZoneImmediate
import com.bletracker.ui.theme.ZoneLost
import com.bletracker.ui.theme.ZoneVeryWeak

@Composable
fun RadarScreen(
    viewModel: RadarViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val device = uiState.device

    val zone = device?.zone ?: SignalZone.LOST
    val isLost = uiState.isLost
    val zoneColor = if (isLost) ZoneLost else zone.getColor()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceLight)
                    .border(1.dp, SurfaceBorderLight, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device?.displayName ?: "Radar Pelacakan",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = device?.address ?: "--:--:--:--:--:--",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }

            SignalBadge(zone = if (isLost) SignalZone.LOST else zone)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status Banner with Live Tracking / History Snapshot indicator
        ConnectionStatusCard(
            status = uiState.connectionStatus,
            isScanning = uiState.isScanning,
            isLost = isLost,
            lastSeenFormatted = uiState.lastSeenFormatted,
            onToggleScan = { viewModel.toggleScan() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Central Animated Radar Canvas
        RadarCanvas(
            zone = zone,
            estimatedDistance = device?.estimatedDistance ?: -1.0,
            isLost = isLost
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Distance & Zone Hero Card
        DistanceZoneHeroCard(
            zone = zone,
            estimatedDistance = device?.estimatedDistance ?: -1.0,
            isLost = isLost,
            zoneColor = zoneColor
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Telemetry Grid: Raw dBm, Filtered dBm, Stability
        TelemetryGrid(
            rawRssi = device?.rssi ?: 0,
            filteredRssi = device?.smoothedRssi ?: 0,
            history = uiState.rssiHistory,
            zoneColor = zoneColor
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ConnectionStatusCard(
    status: String,
    isScanning: Boolean,
    isLost: Boolean,
    lastSeenFormatted: String,
    onToggleScan: () -> Unit
) {
    val bgColor = when {
        !isScanning -> SurfaceSubtle
        isLost -> Color(0xFFFEF2F2)
        else -> Color(0xFFECFDF5)
    }
    val borderColor = when {
        !isScanning -> SurfaceBorderLight
        isLost -> Color(0xFFFEE2E2)
        else -> Color(0xFFD1FAE5)
    }
    val iconColor = when {
        !isScanning -> PrimaryBlue
        isLost -> ZoneVeryWeak
        else -> ZoneImmediate
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                !isScanning -> Icons.Default.History
                isLost -> Icons.Default.Warning
                else -> Icons.Default.Info
            },
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = status,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = if (isScanning) "Pelacakan sinyal real-time aktif" else "Terakhir terlihat: $lastSeenFormatted",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onToggleScan,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isScanning) ZoneVeryWeak else PrimaryBlue,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(
                imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isScanning) "Hentikan" else "Lacak",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun DistanceZoneHeroCard(
    zone: SignalZone,
    estimatedDistance: Double,
    isLost: Boolean,
    zoneColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLight)
            .border(1.dp, SurfaceBorderLight, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ESTIMASI JARAK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLost || estimatedDistance < 0) "Terputus" else "~$estimatedDistance m",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLost) ZoneLost else PrimaryBlue
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "ZONA SINYAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLost) "Sinyal Hilang" else zone.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = zoneColor
                )
                Text(
                    text = if (isLost) "Di luar jangkauan" else zone.distanceDescription,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun TelemetryGrid(
    rawRssi: Int,
    filteredRssi: Int,
    history: List<Int>,
    zoneColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLight)
            .border(1.dp, SurfaceBorderLight, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "STABILITAS & TELEMETRI SINYAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceSubtle)
                    .border(1.dp, SurfaceBorderLight.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Raw RSSI", fontSize = 11.sp, color = TextTertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (rawRssi != 0) "$rawRssi dBm" else "--",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceSubtle)
                    .border(1.dp, SurfaceBorderLight.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Filtered (EMA)", fontSize = 11.sp, color = TextTertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (filteredRssi != 0) "$filteredRssi dBm" else "--",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = zoneColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mini RSSI trend history sparkline
        Text(
            text = "Riwayat Fluktuasi RSSI (Terakhir ${history.size} paket)",
            fontSize = 11.sp,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceSubtle)
                .border(1.dp, SurfaceBorderLight.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Menunggu paket sinyal...", fontSize = 10.sp, color = TextTertiary)
                }
            } else {
                history.forEach { sample ->
                    val fraction = ((sample + 100) / 70f).coerceIn(0.12f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(fraction)
                            .clip(RoundedCornerShape(2.dp))
                            .background(zoneColor)
                    )
                }
            }
        }
    }
}
