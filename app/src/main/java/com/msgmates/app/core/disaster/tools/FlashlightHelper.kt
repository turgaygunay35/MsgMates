package com.msgmates.app.core.disaster.tools

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FlashlightHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val _isOn = MutableStateFlow(false)
    val isOn: StateFlow<Boolean> = _isOn.asStateFlow()

    fun toggle(): Boolean {
        return if (_isOn.value) {
            turnOff()
        } else {
            turnOn()
        }
    }

    fun turnOn(): Boolean {
        return try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, true)
            _isOn.value = true
            android.util.Log.d("FlashlightHelper", "Flashlight turned on")
            true
        } catch (e: CameraAccessException) {
            android.util.Log.e("FlashlightHelper", "Failed to turn on flashlight", e)
            false
        }
    }

    fun turnOff(): Boolean {
        return try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
            _isOn.value = false
            android.util.Log.d("FlashlightHelper", "Flashlight turned off")
            true
        } catch (e: CameraAccessException) {
            android.util.Log.e("FlashlightHelper", "Failed to turn off flashlight", e)
            false
        }
    }
}
