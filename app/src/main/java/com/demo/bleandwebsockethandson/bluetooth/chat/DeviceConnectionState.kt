package com.demo.bleandwebsockethandson.bluetooth.chat

sealed class DeviceConnectionState {
    object Connected : DeviceConnectionState()
    object Disconnected : DeviceConnectionState()
}
