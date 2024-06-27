package com.demo.bleandwebsockethandson.socket.scan

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.bleandwebsockethandson.utils.ErrorCode
import com.demo.bleandwebsockethandson.utils.PreferenceManager
import com.demo.bleandwebsockethandson.utils.SOCKET_PORT
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class ScanSocketDeviceViewModel @Inject constructor(var preferenceManager: PreferenceManager) :
    ViewModel() {
    private val _viewState = MutableLiveData<ScanSocketDeviceViewState>()
    val viewState = _viewState as LiveData<ScanSocketDeviceViewState>

    private var serverSocket: ServerSocket? = null
    private var stop = false
    private var socketServerThread = SocketServerThread()

    init {
        socketServerThread.start()
    }

    fun searchDevices() = Executors.newSingleThreadExecutor().execute {
        val scanResults = mutableListOf<String>()
        _viewState.postValue(ScanSocketDeviceViewState.ScanStart)
        val ipAddress = preferenceManager.ipAddress
        val ipPrefix = ipAddress.run { substring(0, lastIndexOf(".") + 1) }
        repeat((1..255).count()) { i ->
            val ip = ipPrefix + i
            val clientSocket = Socket()
            if (ip != ipAddress) {
                try {
                    clientSocket.connect(InetSocketAddress(ip, SOCKET_PORT), 50)
                    DataOutputStream(clientSocket.getOutputStream())
                    BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    scanResults.add(ip)
                } catch (_: Exception) {
                } finally {
                    clientSocket.close()
                }
            }
        }
        _viewState.postValue(ScanSocketDeviceViewState.ScanResultsDevice(scanResults))
    }

    fun fetchNameFromIp(ipAddress: String) = Executors.newSingleThreadExecutor().execute {
        val clientSocket = Socket(ipAddress, SOCKET_PORT)
        val out = PrintWriter(clientSocket.getOutputStream(), true)
        out.println("${preferenceManager.ipAddress}-${preferenceManager.deviceName}")
        Log.e("Send my data", "${preferenceManager.ipAddress}_${preferenceManager.deviceName}")
        try {
            val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            _viewState.postValue(
                ScanSocketDeviceViewState.GotDetails(
                    ipAddress,
                    input.readLine().substringAfter("-")
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
            _viewState.postValue(ScanSocketDeviceViewState.Error(ErrorCode.valueOf(e.cause.toString())))
        } finally {
            try {
                clientSocket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private inner class SocketServerThread : Thread() {
        override fun run() {
            try {
                serverSocket = ServerSocket(SOCKET_PORT)
                while (!stop) {
                    Log.e("SERVER", "WAITING")
                    val receivedUsersocket = serverSocket!!.accept()
                    Log.e("SERVER", "CONNECTED")

                    //Receive user credentials
                    val input =
                        BufferedReader(InputStreamReader(receivedUsersocket.getInputStream()))
                    val clientCred = input.readLine() ?: continue
                    val clientIp = clientCred.substringBefore("-")
                    val clientName = clientCred.substringAfter("-")
                    _viewState.postValue(ScanSocketDeviceViewState.GotDetails(clientIp, clientName))
                    try {
                        val out = PrintWriter(receivedUsersocket.getOutputStream(), true)
                        out.println("${preferenceManager.ipAddress}-${preferenceManager.deviceName}")
                    } catch (e: IOException) {
                        Log.e("INSIDE SERVER", "Could Not Send Self Credentials")
                        e.printStackTrace()
                    }
                    stop = true
                    break
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            onDestroy()
        }
    }

    fun onDestroy() {
        try {
            serverSocket?.close()
            stop = true
            Thread.interrupted()
        } catch (e: IOException) {
            Log.e("SERVER", "Could Not Close Server")
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        onDestroy()
        super.onCleared()
    }
}