package com.demo.bleandwebsockethandson.bluetooth

import java.io.Serializable

class Message(
    val text: String,
    var type: MessageType,
    var offine: Boolean = false
) : Serializable {
    companion object {
        fun outgoingMessage(message: String): Message = Message(message, MessageType.OUTGOING)
        fun incomingMessage(message: String): Message = Message(message, MessageType.INCOMING)
        fun outgoingOfflineMessage(message: String): Message =
            Message(message, MessageType.OUTGOING, true)

        fun incomingOfflineMessage(message: String): Message =
            Message(message, MessageType.INCOMING, true)
    }
}

enum class MessageType {
    INCOMING,
    OUTGOING
}