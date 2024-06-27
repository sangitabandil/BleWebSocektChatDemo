package com.demo.bleandwebsockethandson.bluetooth.chat

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import com.demo.bleandwebsockethandson.bluetooth.BleChatServer

class BleChatViewModel : ViewModel() {

    lateinit var currentDevice: BluetoothDevice

    fun connectDevice(device: BluetoothDevice) {
        BleChatServer.setCurrentChatConnection(device)
        currentDevice = device
    }
}