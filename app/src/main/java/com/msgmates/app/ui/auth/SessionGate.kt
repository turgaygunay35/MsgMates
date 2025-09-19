package com.msgmates.app.ui.auth

import android.content.Context
import com.msgmates.app.BuildConfig
import com.msgmates.app.data.local.prefs.SessionPrefs
import kotlinx.coroutines.flow.first

sealed class GateResult {
    data object NeedPhone : GateResult()
    data object NeedTos : GateResult()
    data object Ready : GateResult()
}

/** Açılışta hangi ekrana gideceğimizi belirler. */
suspend fun gate(context: Context): GateResult {
    // DEBUG bypass açıkken doğrudan READY; launcher zaten Main'e gönderecek
    if (BuildConfig.DEBUG && BuildConfig.ENABLE_PASSWORD_BYPASS) {
        return GateResult.Ready
    }

    val prefs = SessionPrefs(context)
    val phone = prefs.phoneE164.first()
    if (phone.isNullOrBlank()) return GateResult.NeedPhone

    val tosOk = prefs.tosAccepted.first()
    if (!tosOk) return GateResult.NeedTos

    val logged = prefs.loggedIn.first()
    return if (logged) GateResult.Ready else GateResult.NeedPhone
}
