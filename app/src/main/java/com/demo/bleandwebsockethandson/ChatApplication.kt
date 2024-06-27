package com.demo.bleandwebsockethandson

import android.app.Application
import com.demo.bleandwebsockethandson.bluetooth.BleChatServer
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class ChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BleChatServer.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        BleChatServer.stopServer()
    }
}