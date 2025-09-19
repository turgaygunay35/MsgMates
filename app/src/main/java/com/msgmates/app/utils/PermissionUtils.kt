package com.msgmates.app.utils

import android.Manifest
import android.os.Build

object PermissionUtils {
    val BLE_PERMS: Array<String>
        get() = if (Build.VERSION.SDK_INT >= 31) {
            val base = mutableListOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
            if (Build.VERSION.SDK_INT >= 33) base.add(Manifest.permission.POST_NOTIFICATIONS)
            base.toTypedArray()
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
}
