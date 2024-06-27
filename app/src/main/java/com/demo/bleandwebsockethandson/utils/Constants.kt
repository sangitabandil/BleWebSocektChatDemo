package com.demo.bleandwebsockethandson.utils

import java.util.UUID


// Bluetooth requires uuid associate with services
val SERVICE_UUID: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")

val MESSAGE_UUID: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")

val CONFIRM_UUID: UUID = UUID.fromString("36d4dc5c-814b-4097-a5a6-b93b39085928")

const val SOCKET_PORT = 9000
const val SOCKET_PORT2 = 8080