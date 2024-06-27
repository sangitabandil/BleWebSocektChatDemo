package com.demo.bleandwebsockethandson.socket.chat

import com.demo.bleandwebsockethandson.bluetooth.Message


sealed class SocketChatViewState {
    class UpdateTitle(val name: String) : SocketChatViewState()
    class AddMessage(val msg: Message) : SocketChatViewState()
    class SetOfflineMode(val offline : Boolean) : SocketChatViewState()

}
