package com.bletracker.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

class BleScannerDataSource(
    private val context: Context
) {
    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    init {
        // Register receiver to immediately detect hardware toggle of Bluetooth state
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    _isBluetoothEnabled.value = (state == BluetoothAdapter.STATE_ON)
                }
            }
        }
        try {
            androidx.core.content.ContextCompat.registerReceiver(
                context.applicationContext,
                receiver,
                filter,
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (_: Exception) {
            try {
                context.applicationContext.registerReceiver(receiver, filter)
            } catch (_: Exception) {}
        }
    }

    fun refreshBluetoothStatus() {
        _isBluetoothEnabled.value = bluetoothAdapter?.isEnabled == true
    }

    @SuppressLint("MissingPermission")
    fun scanBle(): Flow<BleScanEvent> = callbackFlow {
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _isBluetoothEnabled.value = false
            trySend(BleScanEvent.Error("Bluetooth tidak aktif atau tidak didukung."))
            close()
            return@callbackFlow
        }

        _isBluetoothEnabled.value = true
        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            trySend(BleScanEvent.Error("Bluetooth LE Scanner tidak tersedia."))
            close()
            return@callbackFlow
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let {
                    trySend(BleScanEvent.DeviceDiscovered(it))
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.forEach {
                    trySend(BleScanEvent.DeviceDiscovered(it))
                }
            }

            override fun onScanFailed(errorCode: Int) {
                val message = when (errorCode) {
                    SCAN_FAILED_ALREADY_STARTED -> "Pemindaian sudah berjalan"
                    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "Gagal registrasi pemindaian aplikasi"
                    SCAN_FAILED_INTERNAL_ERROR -> "Terjadi kesalahan internal BLE"
                    SCAN_FAILED_FEATURE_UNSUPPORTED -> "Perangkat tidak mendukung BLE scanning"
                    else -> "Pemindaian gagal (Kode: $errorCode)"
                }
                trySend(BleScanEvent.Error(message))
                channel.close()
            }
        }

        try {
            scanner.startScan(null, settings, callback)
        } catch (e: SecurityException) {
            trySend(BleScanEvent.Error("Izin Bluetooth belum diberikan: ${e.message}"))
            channel.close()
            return@callbackFlow
        } catch (e: Exception) {
            trySend(BleScanEvent.Error("Gagal memulai scan: ${e.message}"))
            channel.close()
            return@callbackFlow
        }

        awaitClose {
            try {
                scanner.stopScan(callback)
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }
    }.buffer(Channel.UNLIMITED)
}

sealed interface BleScanEvent {
    data class DeviceDiscovered(val scanResult: ScanResult) : BleScanEvent
    data class Error(val message: String) : BleScanEvent
}
