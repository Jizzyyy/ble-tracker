package com.bletracker.ui.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bletracker.ui.components.DeviceCard
import com.bletracker.ui.theme.BackgroundDark
import com.bletracker.ui.theme.PrimaryCyan
import com.bletracker.ui.theme.SurfaceBorderDark
import com.bletracker.ui.theme.SurfaceDark
import com.bletracker.ui.theme.SurfaceVariantDark
import com.bletracker.ui.theme.TextPrimary
import com.bletracker.ui.theme.TextSecondary
import com.bletracker.ui.theme.TextTertiary
import com.bletracker.ui.theme.ZoneImmediate
import com.bletracker.ui.theme.ZoneVeryWeak

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onDeviceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Title & Active Status
        ScannerHeader(
            isScanning = uiState.isScanning,
            totalFound = uiState.totalDiscoveredCount
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Control Button (Start / Stop)
        ScanControlButton(
            isScanning = uiState.isScanning,
            onToggle = { viewModel.toggleScan() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        SearchInputBar(
            query = uiState.searchQuery,
            onQueryChange = { viewModel.onSearchQueryChanged(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // RSSI Threshold Filter Slider
        RssiThresholdFilter(
            threshold = uiState.rssiThreshold,
            onThresholdChange = { viewModel.onRssiThresholdChanged(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Error Banner
        AnimatedVisibility(visible = uiState.error != null) {
            uiState.error?.let { errMsg ->
                ErrorBanner(
                    message = errMsg,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Device List
        if (uiState.devices.isEmpty()) {
            EmptyDevicePlaceholder(
                isScanning = uiState.isScanning,
                hasFilter = uiState.searchQuery.isNotBlank() || uiState.rssiThreshold > -100
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = uiState.devices,
                    key = { it.address }
                ) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { onDeviceClick(device.address) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerHeader(
    isScanning: Boolean,
    totalFound: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "BLE Scanner",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = "Deteksi sinyal Bluetooth real-time",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Live status indicator badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(if (isScanning) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(if (isScanning) ZoneImmediate else TextTertiary)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isScanning) "$totalFound Aktif" else "Siaga",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isScanning) ZoneImmediate else TextSecondary
            )
        }
    }
}

@Composable
private fun ScanControlButton(
    isScanning: Boolean,
    onToggle: () -> Unit
) {
    Button(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isScanning) ZoneVeryWeak else PrimaryCyan,
            contentColor = if (isScanning) TextPrimary else BackgroundDark
        )
    ) {
        Icon(
            imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isScanning) "Hentikan Pemindaian" else "Mulai Pemindaian BLE",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SearchInputBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(14.dp)),
        placeholder = {
            Text(
                text = "Cari nama perangkat atau MAC address...",
                color = TextTertiary,
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Hapus",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceDark,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun RssiThresholdFilter(
    threshold: Int,
    onThresholdChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = PrimaryCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ambang Batas RSSI",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }

            Text(
                text = if (threshold <= -100) "Semua Sinyal" else "≥ $threshold dBm",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (threshold <= -100) TextTertiary else PrimaryCyan
            )
        }

        Slider(
            value = threshold.toFloat(),
            onValueChange = { onThresholdChange(it.toInt()) },
            valueRange = -100f..-30f,
            steps = 13,
            colors = SliderDefaults.colors(
                thumbColor = PrimaryCyan,
                activeTrackColor = PrimaryCyan,
                inactiveTrackColor = SurfaceVariantDark
            )
        )
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ZoneVeryWeak.copy(alpha = 0.15f))
            .border(1.dp, ZoneVeryWeak.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = ZoneVeryWeak,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = message,
            color = TextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tutup",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun EmptyDevicePlaceholder(
    isScanning: Boolean,
    hasFilter: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Radar,
                contentDescription = null,
                tint = if (isScanning) PrimaryCyan else TextTertiary,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = when {
                    hasFilter -> "Tidak ada perangkat yang cocok dengan filter"
                    isScanning -> "Mencari perangkat BLE di sekitar..."
                    else -> "Pemindaian Belum Dimulai"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when {
                    hasFilter -> "Coba kurangi ambang batas sinyal atau periksa kata kunci pencarian."
                    isScanning -> "Pastikan perangkat Bluetooth di dekatmu dalam mode broadcast/advertising."
                    else -> "Tekan tombol Mulai Pemindaian di atas untuk memulai pencarian."
                },
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
