package com.demo.bleandwebsockethandson.bluetooth

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.demo.bleandwebsockethandson.bluetooth.chat.DeviceConnectionState
import com.demo.bleandwebsockethandson.utils.CONFIRM_UUID
import com.demo.bleandwebsockethandson.utils.MESSAGE_UUID
import com.demo.bleandwebsockethandson.utils.SERVICE_UUID

private const val TAG = "BleChatServer"

@SuppressLint("MissingPermission")
object BleChatServer {

    lateinit var app: Application

    private lateinit var bluetoothManager: BluetoothManager
    private val btAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var advertiser: BluetoothLeAdvertiser
    private var advertiserCallBack: AdvertiseCallback? = null
    private val advertiserSettings =
        AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setTimeout(0).build()
    private val advertiseData =
        AdvertiseData.Builder().addServiceUuid(ParcelUuid(SERVICE_UUID)).setIncludeDeviceName(true)
            .build()

    private val _messages = MutableLiveData<Message>()
    val messages = _messages as LiveData<Message>

    private val _connectionRequest = MutableLiveData<BluetoothDevice>()
    val connectionRequest = _connectionRequest as LiveData<BluetoothDevice>

    private var gattServer: BluetoothGattServer? = null
    private var gattServerCallback: BluetoothGattServerCallback? = null

    private var gattClient: BluetoothGatt? = null
    private var gattClientCallback: BluetoothGattCallback? = null

    private var currentDevice: BluetoothDevice? = null
    private val _deviceConnection = MutableLiveData<DeviceConnectionState>()
    val deviceConnection = _deviceConnection as LiveData<DeviceConnectionState>
    private var gatt: BluetoothGatt? = null
    private var messageCharacteristic: BluetoothGattCharacteristic? = null


    fun init(application: Application) {
        app = application
        bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    fun startServer() {
        if (btAdapter.isEnabled) {
            setupGattServer()
            startAdvertisement()
        }
    }

    fun stopServer() {
        stopAdvertising()
    }

    private fun startAdvertisement() {
        if (advertiserCallBack == null) {
            advertiser = btAdapter.bluetoothLeAdvertiser
            advertiserCallBack = DeviceAdvertiseCallback()
            advertiser.startAdvertising(advertiserSettings, advertiseData, advertiserCallBack)
        }
    }

    private fun stopAdvertising() {
        advertiser.stopAdvertising(advertiserCallBack)
        advertiserCallBack = null
    }

    private fun setupGattServer() {
        gattServerCallback = GattServerCallback()
        gattServer = bluetoothManager.openGattServer(app, gattServerCallback).apply {
            addService(setupGattService())
        }
    }

    private fun setupGattService() =
        BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY).apply {
            val messageCharacteristic = BluetoothGattCharacteristic(
                MESSAGE_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            addCharacteristic(messageCharacteristic)
            val confirmCharacteristic = BluetoothGattCharacteristic(
                CONFIRM_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            addCharacteristic(confirmCharacteristic)
        }

    fun setCurrentChatConnection(device: BluetoothDevice) {
        currentDevice = device
        _deviceConnection.value = DeviceConnectionState.Connected
        connectToChatDevice(device)
    }

    private fun connectToChatDevice(device: BluetoothDevice) {
        gattClientCallback = GattClientCallback()
        gattClient = device.connectGatt(app, false, gattClientCallback)
    }

    fun sendMessage(message: String): Boolean {
        messageCharacteristic?.let { characteristic ->
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            val messageBytes = message.toByteArray(Charsets.UTF_8)
            characteristic.value = messageBytes
            gatt?.let {
                val success = it.writeCharacteristic(messageCharacteristic)
                Log.d(TAG, "onServicesDiscovered: message send: $success")
                if (success) {
                    _messages.value = Message.outgoingMessage(message)
                }
            } ?: run {
                Log.d(TAG, "sendMessage: no gatt connection to send a message with")
            }
        }
        return false
    }

    fun makeCurrentMsgEmpty() {
        _messages.value = Message.outgoingMessage("")
    }

    private class GattServerCallback : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(
            device: BluetoothDevice,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(device, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothGatt.STATE_CONNECTED
            if (isSuccess.and(isConnected)) {
                _connectionRequest.postValue(device)
            } else {
                _deviceConnection.postValue(DeviceConnectionState.Disconnected)
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            if (characteristic.uuid == MESSAGE_UUID) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8)
                message?.let {
                    _messages.postValue(Message.incomingMessage(it))
                }
            }
        }
    }

    private class GattClientCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothGatt.STATE_CONNECTED
            if (isSuccess && isConnected) {
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(discoveredGatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt = discoveredGatt
                val service = discoveredGatt.getService(SERVICE_UUID)
                messageCharacteristic = service.getCharacteristic(MESSAGE_UUID)
            }
        }
    }

    private class DeviceAdvertiseCallback : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "onStartSuccess:")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.d(TAG, "onStartFailure: $errorCode")
        }
    }
}