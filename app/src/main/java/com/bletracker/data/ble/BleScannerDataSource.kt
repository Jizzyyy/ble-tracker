package com.bletracker.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BleScannerDataSource(
    private val context: Context
) {
    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun scanBle(): Flow<BleScanEvent> = callbackFlow {
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            trySend(BleScanEvent.Error("Bluetooth tidak aktif atau tidak didukung."))
            close()
            return@callbackFlow
        }

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
            }
        }

        try {
            scanner.startScan(null, settings, callback)
        } catch (e: SecurityException) {
            trySend(BleScanEvent.Error("Izin Bluetooth belum diberikan: ${e.message}"))
            close(e)
            return@callbackFlow
        } catch (e: Exception) {
            trySend(BleScanEvent.Error("Gagal memulai scan: ${e.message}"))
            close(e)
            return@callbackFlow
        }

        awaitClose {
            try {
                scanner.stopScan(callback)
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }
    }
}

sealed interface BleScanEvent {
    data class DeviceDiscovered(val scanResult: ScanResult) : BleScanEvent
    data class Error(val message: String) : BleScanEvent
}
