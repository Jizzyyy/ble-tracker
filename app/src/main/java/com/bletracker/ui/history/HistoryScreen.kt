package com.bletracker.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bletracker.domain.model.BleDevice
import com.bletracker.ui.components.ScannerSkeletonList
import com.bletracker.ui.components.SignalBadge
import com.bletracker.ui.components.getColor
import com.bletracker.ui.theme.BackgroundLight
import com.bletracker.ui.theme.SurfaceBorderLight
import com.bletracker.ui.theme.SurfaceLight
import com.bletracker.ui.theme.SurfaceSubtle
import com.bletracker.ui.theme.TextPrimary
import com.bletracker.ui.theme.TextSecondary
import com.bletracker.ui.theme.TextTertiary
import com.bletracker.ui.theme.ZoneVeryWeak

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onDeviceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat Perangkat",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Tersimpan di database Room lokal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            if (uiState.totalCount > 0) {
                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceLight)
                        .border(1.dp, SurfaceBorderLight, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Hapus Riwayat",
                        tint = ZoneVeryWeak,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search in History
        TextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorderLight, RoundedCornerShape(12.dp)),
            placeholder = {
                Text(
                    text = "Cari di riwayat...",
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
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
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
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Device History List, Skeleton, or Empty View
        when {
            uiState.isLoading -> {
                ScannerSkeletonList(count = 3, modifier = Modifier.padding(vertical = 8.dp))
            }
            uiState.devices.isEmpty() -> {
                EmptyHistoryPlaceholder(hasFilter = uiState.searchQuery.isNotBlank())
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.devices,
                        key = { it.address }
                    ) { device ->
                        HistoryDeviceItem(
                            device = device,
                            formattedTime = viewModel.formatLastSeen(device.lastSeenTimestamp),
                            onClick = { onDeviceClick(device.address) }
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Hapus Seluruh Riwayat?", color = TextPrimary)
            },
            text = {
                Text(
                    "Semua catatan riwayat perangkat Bluetooth yang tersimpan di database perangkat ini akan dihapus permanen.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Hapus", color = ZoneVeryWeak, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = SurfaceLight
        )
    }
}

@Composable
private fun HistoryDeviceItem(
    device: BleDevice,
    formattedTime: String,
    onClick: () -> Unit
) {
    val zoneColor = device.zone.getColor()

    Box(
        modifier = Modifier
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
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceSubtle)
                    .border(1.dp, SurfaceBorderLight.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Terakhir: $formattedTime",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "${device.rssi} dBm",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = zoneColor
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryPlaceholder(hasFilter: Boolean) {
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
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (hasFilter) "Tidak ditemukan di riwayat" else "Belum Ada Riwayat Perangkat",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (hasFilter) "Coba ubah kata kunci pencarian." else "Perangkat Bluetooth yang terdeteksi saat pemindaian akan otomatis tersimpan di database lokal.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
