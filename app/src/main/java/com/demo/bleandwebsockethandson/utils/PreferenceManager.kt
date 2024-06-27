package com.demo.bleandwebsockethandson.utils

import android.content.Context
import android.provider.Settings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.demo.bleandwebsockethandson.bluetooth.Message

class PreferenceManager(private val context: Context) {

    companion object {
        private const val PREF_NAME = "BLE_PREF"
        const val MESSAGE_LIST = "MESSAGES_"
    }

    private val pref by lazy { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) }
    private val editor by lazy { pref.edit() }

    val deviceName = Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
    val ipAddress = context.getSelfIpAddress2()

    private fun setStringValue(key: String, value: String) {
        editor.putString(key, value)
        editor.commit()
    }

    fun saveMessageList(chatId: String, messages: List<Message>) {
        setStringValue(MESSAGE_LIST.plus(chatId), Gson().toJson(messages))
    }

    fun getMessageList(chatId: String): MutableList<Message> =
        pref.getString(MESSAGE_LIST.plus(chatId), null).let {
            if (it.isNullOrBlank())
                ArrayList()
            else
                Gson().fromJson(it, object : TypeToken<ArrayList<Message>>() {}.type)
        }
}