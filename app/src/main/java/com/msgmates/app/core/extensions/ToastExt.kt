package com.msgmates.app.core.extensions

import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Extension function for showing toast messages in Fragments.
 * Provides a convenient way to display short toast messages.
 */
fun Fragment.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}
