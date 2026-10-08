package com.bletracker.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bletracker.domain.model.BleDevice
import com.bletracker.domain.repository.BleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ScannerUiState(
    val devices: List<BleDevice> = emptyList(),
    val totalDiscoveredCount: Int = 0,
    val isScanning: Boolean = false,
    val searchQuery: String = "",
    val rssiThreshold: Int = -100,
    val error: String? = null
)

class ScannerViewModel(
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _rssiThreshold = MutableStateFlow(-100)
    private val _dismissedError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ScannerUiState> = combine(
        bleRepository.devices,
        bleRepository.isScanning,
        bleRepository.scanError,
        _searchQuery,
        _rssiThreshold
    ) { rawDevices, isScanning, repoError, query, threshold ->
        val filtered = rawDevices.filter { device ->
            val matchesQuery = query.isBlank() ||
                (device.name?.contains(query, ignoreCase = true) == true) ||
                device.address.contains(query, ignoreCase = true)

            val matchesThreshold = device.smoothedRssi >= threshold
            matchesQuery && matchesThreshold
        }

        val activeError = if (repoError == _dismissedError.value) null else repoError

        ScannerUiState(
            devices = filtered,
            totalDiscoveredCount = rawDevices.size,
            isScanning = isScanning,
            searchQuery = query,
            rssiThreshold = threshold,
            error = activeError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScannerUiState()
    )

    fun startScan() {
        _dismissedError.value = null
        bleRepository.startScan()
    }

    fun stopScan() {
        bleRepository.stopScan()
    }

    fun toggleScan() {
        if (uiState.value.isScanning) {
            stopScan()
        } else {
            startScan()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onRssiThresholdChanged(threshold: Int) {
        _rssiThreshold.value = threshold
    }

    fun clearError() {
        _dismissedError.value = uiState.value.error
    }
}
