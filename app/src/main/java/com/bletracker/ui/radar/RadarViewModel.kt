package com.bletracker.ui.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bletracker.domain.model.BleDevice
import com.bletracker.domain.model.SignalZone
import com.bletracker.domain.repository.BleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RadarUiState(
    val device: BleDevice? = null,
    val isLost: Boolean = false,
    val rssiHistory: List<Int> = emptyList(),
    val secondsSinceLastUpdate: Long = 0,
    val connectionStatus: String = "Mencari target..."
)

class RadarViewModel(
    private val targetAddress: String,
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RadarUiState(
            device = bleRepository.getDevice(targetAddress)
        )
    )
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    private val recentRssi = mutableListOf<Int>()
    private var lastPacketTime = System.currentTimeMillis()

    init {
        // Ensure scanner is actively running for live tracking
        bleRepository.startScan()

        // Observe repository devices for updates on this target
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
                        connectionStatus = if (isLost) "Sinyal Terputus" else "Terhubung Stabil"
                    )
                }
            }
            .launchIn(viewModelScope)

        // Periodic watchdog ticker for packet loss / connection timeout
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val elapsedSeconds = (System.currentTimeMillis() - lastPacketTime) / 1000
                val currentDevice = _uiState.value.device

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
                    }
                )
            }
        }
    }
}
