package com.msgmates.app.data.local.prefs

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreKeys {
    // Oturum / onboarding
    val KEY_PHONE_E164 = stringPreferencesKey("phone_e164")
    val KEY_TOS_ACCEPTED = booleanPreferencesKey("tos_accepted")
    val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")
    val KEY_USER_ID = stringPreferencesKey("user_id")

    // Alive / Disaster Mode örnekleri
    val KEY_ALIVE_LAST_TS = intPreferencesKey("alive_last_ts")
    val KEY_ALIVE_COOLDOWN = intPreferencesKey("alive_cooldown")

    // Capsule depolama (basit)
    val KEY_CAPSULES = stringPreferencesKey("capsules")
}
