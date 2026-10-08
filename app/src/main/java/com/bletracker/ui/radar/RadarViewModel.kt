package com.bletracker.ui.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bletracker.domain.model.BleDevice
import com.bletracker.domain.model.SignalZone
import com.bletracker.domain.repository.BleRepository
import com.bletracker.domain.repository.HistoryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RadarUiState(
    val device: BleDevice? = null,
    val isScanning: Boolean = false,
    val isLost: Boolean = false,
    val rssiHistory: List<Int> = emptyList(),
    val secondsSinceLastUpdate: Long = 0,
    val connectionStatus: String = "Memuat data...",
    val lastSeenFormatted: String = ""
)

class RadarViewModel(
    private val targetAddress: String,
    private val bleRepository: BleRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RadarUiState(
            isScanning = bleRepository.isScanning.value
        )
    )
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    private val recentRssi = mutableListOf<Int>()
    private var lastPacketTime = System.currentTimeMillis()

    init {
        // 1. Initial load from in-memory cache or Room database WITHOUT starting scan automatically
        viewModelScope.launch {
            val initialDevice = bleRepository.getDevice(targetAddress)
                ?: historyRepository.getDevice(targetAddress)

            if (initialDevice != null) {
                lastPacketTime = initialDevice.lastSeenTimestamp
                recentRssi.clear()
                recentRssi.add(initialDevice.rssi)
                val isCurrentlyScanning = bleRepository.isScanning.value

                _uiState.value = _uiState.value.copy(
                    device = initialDevice,
                    rssiHistory = listOf(initialDevice.rssi),
                    isLost = initialDevice.zone == SignalZone.LOST,
                    connectionStatus = if (isCurrentlyScanning) "Mencari target..." else "Mode Riwayat (Tidak Memindai)",
                    lastSeenFormatted = formatLastSeen(initialDevice.lastSeenTimestamp)
                )
            }
        }

        // 2. Observe repository scanning state (live vs history)
        bleRepository.isScanning
            .onEach { isScanning ->
                val currentDevice = _uiState.value.device
                _uiState.value = _uiState.value.copy(
                    isScanning = isScanning,
                    connectionStatus = if (!isScanning) {
                        "Mode Riwayat (Tidak Memindai)"
                    } else if (currentDevice != null) {
                        if (_uiState.value.isLost) "Sinyal Hilang" else "Pelacakan Aktif"
                    } else {
                        "Mencari target..."
                    }
                )
            }
            .launchIn(viewModelScope)

        // 3. Observe live packets if scanner is running
        bleRepository.devices
            .onEach { deviceList ->
                val target = deviceList.find { it.address.equals(targetAddress, ignoreCase = true) }
                if (target != null) {
                    lastPacketTime = System.currentTimeMillis()

                    recentRssi.add(target.rssi)
                    if (recentRssi.size > 12) {
                        recentRssi.removeAt(0)
                    }

                    val isLost = target.zone == SignalZone.LOST || target.rssi < -90

                    _uiState.value = _uiState.value.copy(
                        device = target,
                        isLost = isLost,
                        rssiHistory = recentRssi.toList(),
                        secondsSinceLastUpdate = 0,
                        connectionStatus = if (isLost) "Sinyal Terputus" else "Terhubung Stabil",
                        lastSeenFormatted = "Baru saja"
                    )
                }
            }
            .launchIn(viewModelScope)

        // 4. Periodic watchdog ticker for packet loss / connection timeout (ONLY when actively scanning)
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val isScanning = bleRepository.isScanning.value
                val elapsedSeconds = (System.currentTimeMillis() - lastPacketTime) / 1000
                val currentDevice = _uiState.value.device

                if (isScanning) {
                    val shouldMarkLost = elapsedSeconds >= 8 ||
                        (currentDevice?.zone == SignalZone.LOST) ||
                        ((currentDevice?.rssi ?: 0) < -90)

                    _uiState.value = _uiState.value.copy(
                        secondsSinceLastUpdate = elapsedSeconds,
                        isLost = shouldMarkLost,
                        connectionStatus = when {
                            shouldMarkLost && elapsedSeconds >= 8 -> "Sinyal Hilang (Timeout > 8s)"
                            shouldMarkLost -> "Sinyal Hilang (Di luar jangkauan)"
                            else -> "Terhubung Stabil"
                        },
                        lastSeenFormatted = formatLastSeen(currentDevice?.lastSeenTimestamp ?: lastPacketTime)
                    )
                } else {
                    currentDevice?.let { dev ->
                        _uiState.value = _uiState.value.copy(
                            secondsSinceLastUpdate = elapsedSeconds,
                            isLost = dev.zone == SignalZone.LOST,
                            lastSeenFormatted = formatLastSeen(dev.lastSeenTimestamp)
                        )
                    }
                }
            }
        }
    }

    fun toggleScan() {
        if (bleRepository.isScanning.value) {
            bleRepository.stopScan()
        } else {
            bleRepository.startScan()
        }
    }

    private fun formatLastSeen(timestamp: Long): String {
        val diffMillis = System.currentTimeMillis() - timestamp
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24

        return when {
            diffSeconds < 10 -> "Baru saja"
            diffSeconds < 60 -> "$diffSeconds detik lalu"
            diffMinutes < 60 -> "$diffMinutes menit lalu"
            diffHours < 24 -> "$diffHours jam lalu"
            diffDays == 1L -> "Kemarin"
            else -> {
                val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }
}
