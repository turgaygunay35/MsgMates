package com.msgmates.app

import android.app.Application
import android.os.StrictMode
import com.msgmates.app.core.performance.StartupTracer
import com.msgmates.app.util.Notif
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * MsgMates Application class.
 * Handles global initialization and debug-only features like StrictMode.
 */
@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var startupTracer: StartupTracer

    override fun onCreate() {
        super.onCreate()

        // Start cold start tracing
        startupTracer.startTrace()
        registerActivityLifecycleCallbacks(startupTracer)

        // Timber logging setup
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(e: StackTraceElement) =
                    "MsgMates:${super.createStackElementTag(e)}:${e.lineNumber}"
            })
        }

        // Bazı ROM'larda NotificationManager erişimi çok erken patlayabiliyor.
        runCatching { Notif.ensureChannel(this) }

        // Enable StrictMode only in debug builds
        if (BuildConfig.FF_STRICT_MODE) {
            setupStrictMode()
        }
    }

    /**
     * Sets up StrictMode for detecting performance issues and policy violations.
     * Only enabled in debug builds via FF_STRICT_MODE feature flag.
     */
    private fun setupStrictMode() {
        // Thread policy - detect network, disk, and custom slow operations
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        // VM policy - detect memory leaks, SQLite issues, etc.
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }
}
