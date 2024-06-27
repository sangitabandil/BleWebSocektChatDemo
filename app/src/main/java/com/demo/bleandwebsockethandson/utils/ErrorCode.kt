package com.demo.bleandwebsockethandson.utils

import com.demo.bleandwebsockethandson.R

enum class ErrorCode(val msg: Int) {
    PERMISSION_DENIED(R.string.permission_denied),
    BT_DISABLED(R.string.bt_disabled),
    SCAN_FAILED(R.string.scan_fail),
    ADVERTISEMENT_NOT_SUPPORTED(R.string.advertisement_not_supported),
    SERVER_NOT_STARTED(R.string.advertisement_not_supported),
}
