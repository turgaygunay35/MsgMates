package com.msgmates.app.core.navigation

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController

/**
 * Safe navigation helper to prevent crashes during navigation
 * Especially useful for disaster mode transitions and rapid user interactions
 */
object SafeNavigation {

    private const val TAG = "SafeNavigation"
    private var lastNavigationTime = 0L
    private var lastDestinationId: Int? = null
    private const val NAVIGATION_DEBOUNCE_MS = 300L

    /**
     * Safely navigate to a destination with lifecycle and debounce checks
     * @param fragment The fragment initiating navigation
     * @param directions Navigation directions
     * @return true if navigation was attempted, false if skipped
     */
    fun safeNavigate(fragment: Fragment, directions: NavDirections): Boolean {
        return safeNavigate(fragment, 0) { // Placeholder ID
            fragment.findNavController().navigate(directions)
        }
    }

    /**
     * Safely navigate to a destination by ID with lifecycle and debounce checks
     * @param fragment The fragment initiating navigation
     * @param destinationId Navigation destination ID
     * @param navigationAction The actual navigation action to perform
     * @return true if navigation was attempted, false if skipped
     */
    fun safeNavigate(
        fragment: Fragment,
        destinationId: Int,
        navigationAction: () -> Unit
    ): Boolean {
        // Check if fragment is still attached and lifecycle is valid
        if (!fragment.isAdded) {
            Log.w(TAG, "Fragment not attached, skipping navigation to $destinationId")
            return false
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Log.w(TAG, "Fragment lifecycle not started, skipping navigation to $destinationId")
            return false
        }

        // Debounce rapid navigation attempts
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavigationTime < NAVIGATION_DEBOUNCE_MS) {
            Log.d(TAG, "Navigation debounced, skipping navigation to $destinationId")
            return false
        }

        // Prevent navigation to the same destination
        if (lastDestinationId == destinationId) {
            Log.d(TAG, "Already at destination $destinationId, skipping navigation")
            return false
        }

        try {
            navigationAction()
            lastNavigationTime = currentTime
            lastDestinationId = destinationId
            Log.d(TAG, "Successfully navigated to $destinationId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed to $destinationId", e)
            return false
        }
    }

    /**
     * Safely navigate using NavController directly
     * @param navController The NavController instance
     * @param directions Navigation directions
     * @return true if navigation was attempted, false if skipped
     */
    fun safeNavigate(navController: NavController, directions: NavDirections): Boolean {
        val currentTime = System.currentTimeMillis()
        val destinationId = 0 // Placeholder ID

        // Debounce rapid navigation attempts
        if (currentTime - lastNavigationTime < NAVIGATION_DEBOUNCE_MS) {
            Log.d(TAG, "Navigation debounced, skipping navigation to $destinationId")
            return false
        }

        // Prevent navigation to the same destination
        if (lastDestinationId == destinationId) {
            Log.d(TAG, "Already at destination $destinationId, skipping navigation")
            return false
        }

        try {
            navController.navigate(directions)
            lastNavigationTime = currentTime
            lastDestinationId = destinationId
            Log.d(TAG, "Successfully navigated to $destinationId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed to $destinationId", e)
            return false
        }
    }

    /**
     * Reset navigation state (useful for testing or manual state management)
     */
    fun reset() {
        lastNavigationTime = 0L
        lastDestinationId = null
    }

    /**
     * Check if navigation is currently debounced
     */
    fun isDebounced(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime - lastNavigationTime < NAVIGATION_DEBOUNCE_MS
    }
}
