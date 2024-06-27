package com.demo.bleandwebsockethandson.utils

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.net.NetworkInterface
import java.net.SocketException


fun Fragment.hideKeyboard() {
    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(requireView().windowToken, 0)
}

fun Snackbar.addLoadingView(): Snackbar {
    val viewGroup =
        view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup
    viewGroup.addView(ProgressBar(view.context))
    return this
}

fun getSelfIpAddress(): String {
    var myIP = ""
    try {
        val enumNetworkInterfaces = NetworkInterface
            .getNetworkInterfaces()
        while (enumNetworkInterfaces.hasMoreElements()) {
            val networkInterface = enumNetworkInterfaces
                .nextElement()
            val enumInetAddress = networkInterface
                .inetAddresses
            while (enumInetAddress.hasMoreElements()) {
                val inetAddress = enumInetAddress
                    .nextElement()
                if (inetAddress.isSiteLocalAddress) {
                    myIP = inetAddress.hostAddress
                }
            }
        }
    } catch (e: SocketException) {
        e.printStackTrace()
        Log.e("GET_IP", "IP NOT FOUND")
    }
    return myIP
}
fun Context.getSelfIpAddress2(): String {
    val wifiMgr = getSystemService(WIFI_SERVICE) as WifiManager?
    val wifiInfo = wifiMgr!!.connectionInfo
    val ip = wifiInfo.ipAddress
    return Formatter.formatIpAddress(ip)
}