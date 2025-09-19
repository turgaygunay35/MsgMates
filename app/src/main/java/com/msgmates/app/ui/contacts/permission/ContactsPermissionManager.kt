package com.msgmates.app.ui.contacts.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsPermissionManager @Inject constructor() {

    fun hasReadContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getPermissionRationale(): PermissionRationale {
        return PermissionRationale(
            title = "Rehber Erişimi Gerekli",
            message = "MsgMates'in rehberinizdeki kişileri görmesi ve MsgMates kullanıcılarını tespit etmesi için rehber erişimi gereklidir. Bu bilgiler güvenli şekilde işlenir ve sadece sizin cihazınızda saklanır.",
            positiveButtonText = "İzin Ver",
            negativeButtonText = "İptal",
            settingsButtonText = "Ayarlara Git"
        )
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    fun createPermissionLauncher(
        fragment: Fragment,
        onPermissionResult: (Boolean) -> Unit
    ) = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }
}

data class PermissionRationale(
    val title: String,
    val message: String,
    val positiveButtonText: String,
    val negativeButtonText: String,
    val settingsButtonText: String
)
