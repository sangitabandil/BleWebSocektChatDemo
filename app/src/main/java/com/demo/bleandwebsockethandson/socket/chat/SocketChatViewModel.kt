package com.demo.bleandwebsockethandson.socket.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.bleandwebsockethandson.bluetooth.Message
import com.demo.bleandwebsockethandson.bluetooth.MessageType
import com.demo.bleandwebsockethandson.utils.PreferenceManager
import com.demo.bleandwebsockethandson.utils.SOCKET_PORT2
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class SocketChatViewModel @Inject constructor(var preferenceManager: PreferenceManager) :
    ViewModel() {
    private var sender: ExecutorService? = null
    private var messageSocketServerThread = MessageSocketServerThread()
    private var mIpAddress: String? = null
    private lateinit var serverSocket: ServerSocket
    private var stop = false
    private var isUserOffline = false

    private val _viewState = MutableLiveData<SocketChatViewState>()
    val viewState = _viewState as LiveData<SocketChatViewState>

    init {
        messageSocketServerThread.start()
    }

    fun getMessages(): MutableList<Message> =
        preferenceManager.getMessageList(mIpAddress.toString())

    fun saveMessageList(lastTenMessages: List<Message>) =
        preferenceManager.saveMessageList(mIpAddress.toString(), lastTenMessages)

    fun handleArgs(ipAddress: String?, name: String?) {
        mIpAddress = ipAddress
        _viewState.postValue(SocketChatViewState.UpdateTitle(name.toString()))
    }

    fun sendMessage(text: String) {
        sender = Executors.newSingleThreadExecutor()
        sender?.execute {
            try {
                val clientSocket = Socket(mIpAddress, SOCKET_PORT2)
                val out = ObjectOutputStream(clientSocket.getOutputStream())
                val msg = Message.outgoingMessage(text)
                out.writeObject(msg)
                out.flush()
                _viewState.postValue(SocketChatViewState.AddMessage(msg))
                sender?.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
                _viewState.postValue(SocketChatViewState.SetOfflineMode(true))
            }
        }
    }


    private fun handleReceiveMessage(msg: Message) {
        if (isUserOffline != msg.offine) {
            _viewState.postValue(SocketChatViewState.SetOfflineMode(msg.offine))
        }
        if (msg.offine) {
            isUserOffline = true
            sender?.shutdownNow()
            messageSocketServerThread.onDestroy()
        } else {
            isUserOffline = false
            _viewState.postValue(SocketChatViewState.AddMessage(msg))
        }
    }

    private inner class MessageSocketServerThread : Thread() {
        override fun run() {
            try {
                serverSocket = ServerSocket(SOCKET_PORT2)
                while (!stop) {
                    val receivedUserSocket: Socket = serverSocket.accept()
                    Log.e("RECEIVE", "Connected")
                    try {
                        val input = ObjectInputStream(receivedUserSocket.getInputStream())
                        val message: Message = input.readObject() as Message
                        message.type = MessageType.INCOMING
                        handleReceiveMessage(message)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: ClassNotFoundException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun onDestroy() {
            try {
                if (::serverSocket.isInitialized)
                    serverSocket.close()
                stop = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        messageSocketServerThread.onDestroy()
        super.onCleared()
    }

}