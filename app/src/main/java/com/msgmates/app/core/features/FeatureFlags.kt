package com.msgmates.app.core.features

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlags @Inject constructor() {

    /**
     * Afet Modu özelliği aktif mi?
     * Varsayılan: KAPALI (false)
     */
    val isDisasterModeEnabled: Boolean = false

    /**
     * Login olmadan Afet Modu'na erişim var mı?
     * Varsayılan: KAPALI (false)
     */
    val isDisasterModeAccessibleWithoutAuth: Boolean = false

    /**
     * Debug modunda tüm özellikler aktif mi?
     * Varsayılan: KAPALI (false)
     */
    val isDebugModeEnabled: Boolean = false
}
