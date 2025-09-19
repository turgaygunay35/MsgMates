package com.msgmates.app.core.performance

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.msgmates.app.core.metrics.Metrics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Startup tracing for cold start performance measurement
 */
@Singleton
class StartupTracer @Inject constructor(
    private val metrics: Metrics
) : Application.ActivityLifecycleCallbacks {

    private var appStartTime: Long = 0
    private var firstActivityCreatedTime: Long = 0
    private var firstActivityResumedTime: Long = 0
    private var isFirstActivity = true

    companion object {
        private const val TAG = "StartupTracer"
        
        // Cold start thresholds
        private const val COLD_START_THRESHOLD_MS = 2000L // 2 seconds
        private const val FIRST_FRAME_THRESHOLD_MS = 1000L // 1 second
    }

    fun startTrace() {
        appStartTime = SystemClock.elapsedRealtime()
        Log.d(TAG, "App start time: $appStartTime")
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (isFirstActivity) {
            firstActivityCreatedTime = SystemClock.elapsedRealtime()
            val timeToFirstActivity = firstActivityCreatedTime - appStartTime
            Log.d(TAG, "Time to first activity created: ${timeToFirstActivity}ms")
            
            metrics.recordTime("cold_start_to_activity_created_ms", timeToFirstActivity)
            isFirstActivity = false
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (firstActivityResumedTime == 0L) {
            firstActivityResumedTime = SystemClock.elapsedRealtime()
            val coldStartTime = firstActivityResumedTime - appStartTime
            val firstFrameTime = firstActivityResumedTime - firstActivityCreatedTime
            
            Log.d(TAG, "Cold start time: ${coldStartTime}ms")
            Log.d(TAG, "First frame time: ${firstFrameTime}ms")
            
            // Record metrics
            metrics.recordTime("cold_start_total_ms", coldStartTime)
            metrics.recordTime("first_frame_ms", firstFrameTime)
            
            // Check thresholds
            if (coldStartTime > COLD_START_THRESHOLD_MS) {
                Log.w(TAG, "Cold start exceeded threshold: ${coldStartTime}ms > ${COLD_START_THRESHOLD_MS}ms")
                metrics.increment("cold_start_slow")
            } else {
                metrics.increment("cold_start_fast")
            }
            
            if (firstFrameTime > FIRST_FRAME_THRESHOLD_MS) {
                Log.w(TAG, "First frame exceeded threshold: ${firstFrameTime}ms > ${FIRST_FRAME_THRESHOLD_MS}ms")
                metrics.increment("first_frame_slow")
            } else {
                metrics.increment("first_frame_fast")
            }
        }
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
