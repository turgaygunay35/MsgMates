package com.msgmates.app.core.utils

import android.content.Context
import android.provider.Settings
import android.view.animation.Animation
import android.view.animation.AnimationUtils

object AnimationUtils {

    /**
     * Get animation duration based on accessibility settings
     * Reduces animation duration if user has reduced motion enabled
     */
    fun getAnimationDuration(context: Context, baseDuration: Long): Long {
        return try {
            val animationScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            (baseDuration * animationScale).toLong()
        } catch (e: Exception) {
            // Fallback to reduced duration for better performance
            (baseDuration * 0.5f).toLong()
        }
    }

    /**
     * Load animation with accessibility-aware duration
     */
    fun loadAnimation(context: Context, animResId: Int, baseDuration: Long = 300): Animation {
        val animation = AnimationUtils.loadAnimation(context, animResId)
        val duration = getAnimationDuration(context, baseDuration)
        animation.duration = duration
        return animation
    }

    /**
     * Check if animations should be reduced
     */
    fun shouldReduceAnimations(context: Context): Boolean {
        return try {
            val animationScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            animationScale < 0.5f
        } catch (e: Exception) {
            true // Default to reduced animations for better performance
        }
    }

    /**
     * Get optimized animation duration for different types
     */
    fun getFadeInDuration(context: Context): Long = getAnimationDuration(context, 200)
    fun getFadeOutDuration(context: Context): Long = getAnimationDuration(context, 150)
    fun getSlideDuration(context: Context): Long = getAnimationDuration(context, 250)
    fun getScaleDuration(context: Context): Long = getAnimationDuration(context, 200)
}
