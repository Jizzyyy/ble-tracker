package com.bletracker.data.repository

import android.annotation.SuppressLint
import com.bletracker.data.ble.BleScanEvent
import com.bletracker.data.ble.BleScannerDataSource
import com.bletracker.domain.model.BleDevice
import com.bletracker.domain.repository.BleRepository
import com.bletracker.domain.repository.HistoryRepository
import com.bletracker.domain.util.RssiUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class BleRepositoryImpl(
    private val scannerDataSource: BleScannerDataSource,
    private val historyRepository: HistoryRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : BleRepository {

    private val deviceCache = ConcurrentHashMap<String, BleDevice>()

    private val _devices = MutableStateFlow<List<BleDevice>>(emptyList())
    override val devices: StateFlow<List<BleDevice>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    override val scanError: StateFlow<String?> = _scanError.asStateFlow()

    private var scanJob: Job? = null

    @SuppressLint("MissingPermission")
    override fun startScan() {
        if (_isScanning.value) return

        _scanError.value = null
        _isScanning.value = true

        scanJob?.cancel()
        scanJob = scope.launch {
            scannerDataSource.scanBle().collect { event ->
                when (event) {
                    is BleScanEvent.DeviceDiscovered -> {
                        val result = event.scanResult
                        val address = result.device.address ?: return@collect
                        val rawName = try {
                            result.device.name ?: result.scanRecord?.deviceName
                        } catch (_: SecurityException) {
                            result.scanRecord?.deviceName
                        }
                        val rawRssi = result.rssi

                        val existing = deviceCache[address]
                        val smoothed = if (existing != null) {
                            RssiUtil.smoothRssi(existing.smoothedRssi, rawRssi)
                        } else {
                            rawRssi
                        }

                        val distance = RssiUtil.estimateDistance(smoothed)
                        val zone = RssiUtil.classifyZone(smoothed)
                        val deviceName = rawName ?: existing?.name

                        val updatedDevice = BleDevice(
                            name = deviceName,
                            address = address,
                            rssi = rawRssi,
                            smoothedRssi = smoothed,
                            estimatedDistance = distance,
                            zone = zone,
                            lastSeenTimestamp = System.currentTimeMillis()
                        )

                        deviceCache[address] = updatedDevice

                        // Auto-sort descending by strongest signal (RSSI highest)
                        _devices.value = deviceCache.values
                            .sortedByDescending { it.smoothedRssi }

                        // Persist to local database
                        launch(Dispatchers.IO) {
                            historyRepository.saveDevice(updatedDevice)
                        }
                    }

                    is BleScanEvent.Error -> {
                        _scanError.value = event.message
                        _isScanning.value = false
                    }
                }
            }
        }
    }

    override fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _isScanning.value = false
    }

    override fun getDevice(address: String): BleDevice? {
        return deviceCache[address]
    }
}
