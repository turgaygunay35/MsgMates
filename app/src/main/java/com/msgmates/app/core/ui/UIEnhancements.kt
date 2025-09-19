package com.msgmates.app.core.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * UI enhancement utilities for better user experience
 */
object UIEnhancements {

    /**
     * Apply ripple effect to view
     */
    fun applyRippleEffect(view: View) {
        ViewCompat.setBackground(view, ContextCompat.getDrawable(view.context, android.R.drawable.list_selector_background))
    }

    /**
     * Apply subtle scale animation on touch
     */
    fun applyTouchFeedback(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(OvershootInterpolator())
                        .start()
                }
            }
            false
        }
    }

    /**
     * Create gradient background for action bar
     */
    fun createGradientBackground(context: Context, startColor: Int, endColor: Int): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(startColor, endColor)
        )
    }

    /**
     * Fade in animation
     */
    fun fadeIn(view: View, duration: Long = 300) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    /**
     * Fade out animation
     */
    fun fadeOut(view: View, duration: Long = 300, onEnd: (() -> Unit)? = null) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    onEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Slide in from bottom animation
     */
    fun slideInFromBottom(view: View, duration: Long = 300) {
        view.translationY = view.height.toFloat()
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    /**
     * Slide out to bottom animation
     */
    fun slideOutToBottom(view: View, duration: Long = 300, onEnd: (() -> Unit)? = null) {
        view.animate()
            .translationY(view.height.toFloat())
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    onEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Pulse animation for important elements
     */
    fun pulse(view: View, duration: Long = 1000) {
        val animator = ValueAnimator.ofFloat(1f, 1.1f, 1f)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            view.scaleX = scale
            view.scaleY = scale
        }
        animator.start()
    }

    /**
     * Shake animation for error states
     */
    fun shake(view: View, duration: Long = 500) {
        val animator = ValueAnimator.ofFloat(0f, 10f, -10f, 10f, -10f, 10f, -10f, 0f)
        animator.duration = duration
        animator.addUpdateListener { animation ->
            val translationX = animation.animatedValue as Float
            view.translationX = translationX
        }
        animator.start()
    }

    /**
     * Apply system window insets for edge-to-edge display
     */
    fun applySystemWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }
    }

    /**
     * Create loading shimmer effect
     */
    fun createShimmerEffect(view: View) {
        val shimmer = object : ValueAnimator() {
            init {
                setFloatValues(0f, 1f)
                duration = 1000
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                addUpdateListener { animation ->
                    val alpha = animation.animatedValue as Float
                    view.alpha = 0.3f + (alpha * 0.7f)
                }
            }
        }
        shimmer.start()
    }
}
