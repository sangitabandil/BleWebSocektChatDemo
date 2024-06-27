package com.demo.bleandwebsockethandson.bluetooth.scan

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.bleandwebsockethandson.bluetooth.BleChatServer
import com.demo.bleandwebsockethandson.utils.ErrorCode
import com.demo.bleandwebsockethandson.utils.SERVICE_UUID
import javax.inject.Inject

private const val SCAN_PERIOD = 30000L

@SuppressLint("MissingPermission")
class ScanBleDevicesViewModel @Inject constructor() : ViewModel() {

    private val _viewState = MutableLiveData<ScanBleDeviceViewState>()
    val viewState = _viewState as LiveData<ScanBleDeviceViewState>
    private val scanResults = mutableMapOf<String, BluetoothDevice>()
    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: DeviceScanCallback? = null
    private val scanFilters: List<ScanFilter>
    private val scanSettings: ScanSettings


    init {
        scanFilters = buildScanFilters()
        scanSettings = buildScanSettings()
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }

    fun setupBleScan() {
        BleChatServer.startServer()
        startScan()
    }

    private fun startScan() {
        if (!adapter.isMultipleAdvertisementSupported) {
            _viewState.value = ScanBleDeviceViewState.Error(ErrorCode.ADVERTISEMENT_NOT_SUPPORTED)
            return
        }
        if (scanCallback == null) {
            _viewState.value = ScanBleDeviceViewState.ActiveScanBleDevice
            Handler().postDelayed({ stopScanning() }, SCAN_PERIOD)
            scanner = adapter.bluetoothLeScanner
            scanCallback = DeviceScanCallback()
//            scanner?.startScan(scanFilters, scanSettings, scanCallback)
            scanner?.startScan(scanCallback)
        }
    }

    private fun stopScanning() {
        scanner?.stopScan(scanCallback)
        scanCallback = null
        _viewState.value = ScanBleDeviceViewState.ScanResultsBleDevice(scanResults)
    }

    private fun buildScanFilters() = listOf(ScanFilter.Builder().apply {
        setServiceUuid(ParcelUuid(SERVICE_UUID))
    }.build())

    private fun buildScanSettings() =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()

    private inner class DeviceScanCallback : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach { scanResults[it.device.address] = it.device }
            _viewState.value = ScanBleDeviceViewState.ScanResultsBleDevice(scanResults)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let { scanResults[it.device.address] = it.device }
            _viewState.value = ScanBleDeviceViewState.ScanResultsBleDevice(scanResults)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            _viewState.value = ScanBleDeviceViewState.Error(ErrorCode.SCAN_FAILED, errorCode)
        }
    }

}