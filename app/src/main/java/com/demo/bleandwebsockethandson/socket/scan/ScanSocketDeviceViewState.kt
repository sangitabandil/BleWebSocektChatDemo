package com.demo.bleandwebsockethandson.socket.scan

import com.demo.bleandwebsockethandson.utils.ErrorCode


sealed class ScanSocketDeviceViewState {
    object ScanStart : ScanSocketDeviceViewState()
    class ScanResultsDevice(val scanResults: MutableList<String>) : ScanSocketDeviceViewState()
    class Error(val errorCode: ErrorCode, val errorMsg: String? = null) :
        ScanSocketDeviceViewState()

    class GotDetails(val ipAddress: String, val name: String) : ScanSocketDeviceViewState()
}
