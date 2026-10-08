package com.bletracker.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bletracker.domain.model.BleDevice
import com.bletracker.domain.repository.BleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ScannerUiState(
    val devices: List<BleDevice> = emptyList(),
    val totalDiscoveredCount: Int = 0,
    val isScanning: Boolean = false,
    val isInitialLoading: Boolean = false,
    val isBluetoothEnabled: Boolean = true,
    val searchQuery: String = "",
    val rssiThreshold: Int = -100,
    val error: String? = null
)

private data class FilterCriteria(
    val query: String,
    val threshold: Int
)

class ScannerViewModel(
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _rssiThreshold = MutableStateFlow(-100)
    private val _dismissedError = MutableStateFlow<String?>(null)
    private val _isInitialLoading = MutableStateFlow(false)
    private var initialLoadingJob: kotlinx.coroutines.Job? = null

    private val filterFlow = combine(_searchQuery, _rssiThreshold) { query, threshold ->
        FilterCriteria(query, threshold)
    }

    val uiState: StateFlow<ScannerUiState> = combine(
        bleRepository.devices,
        bleRepository.isScanning,
        bleRepository.scanError,
        bleRepository.isBluetoothEnabled,
        filterFlow,
        _isInitialLoading
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val rawDevices = args[0] as List<BleDevice>
        val isScanning = args[1] as Boolean
        val repoError = args[2] as String?
        val isBtEnabled = args[3] as Boolean
        val filter = args[4] as FilterCriteria
        val isInitialLoading = args[5] as Boolean

        val query = filter.query.trim()
        val queryNoColon = query.replace(":", "")

        val filtered = rawDevices.filter { device ->
            val matchesQuery = query.isBlank() ||
                device.displayName.contains(query, ignoreCase = true) ||
                device.address.contains(query, ignoreCase = true) ||
                (queryNoColon.isNotBlank() && device.address.replace(":", "").contains(queryNoColon, ignoreCase = true))

            val matchesThreshold = device.smoothedRssi >= filter.threshold
            matchesQuery && matchesThreshold
        }

        val activeError = if (repoError == _dismissedError.value) null else repoError

        ScannerUiState(
            devices = filtered,
            totalDiscoveredCount = rawDevices.size,
            isScanning = isScanning,
            isInitialLoading = isScanning && isInitialLoading && rawDevices.isEmpty(),
            isBluetoothEnabled = isBtEnabled,
            searchQuery = filter.query,
            rssiThreshold = filter.threshold,
            error = activeError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScannerUiState(isBluetoothEnabled = bleRepository.isBluetoothEnabled.value)
    )

    fun refreshBluetoothStatus() {
        bleRepository.refreshBluetoothStatus()
    }

    fun startScan() {
        _dismissedError.value = null
        bleRepository.startScan()

        initialLoadingJob?.cancel()
        initialLoadingJob = viewModelScope.launch {
            _isInitialLoading.value = true
            kotlinx.coroutines.delay(5000) // Show skeletonizer for 5 seconds countdown window
            _isInitialLoading.value = false
        }
    }

    fun stopScan() {
        initialLoadingJob?.cancel()
        _isInitialLoading.value = false
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
