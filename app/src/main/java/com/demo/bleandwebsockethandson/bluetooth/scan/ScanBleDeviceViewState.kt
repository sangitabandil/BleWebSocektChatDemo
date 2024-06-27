package com.demo.bleandwebsockethandson.bluetooth.scan

import android.bluetooth.BluetoothDevice
import com.demo.bleandwebsockethandson.utils.ErrorCode

sealed class ScanBleDeviceViewState {
    object ActiveScanBleDevice : ScanBleDeviceViewState()
    class ScanResultsBleDevice(val scanResults: Map<String, BluetoothDevice>) : ScanBleDeviceViewState()
    class Error(val errorCode: ErrorCode, val subErrorCode: Int = -1) : ScanBleDeviceViewState()
}
