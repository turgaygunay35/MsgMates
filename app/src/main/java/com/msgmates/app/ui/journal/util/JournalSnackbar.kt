package com.msgmates.app.ui.journal.util

import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.msgmates.app.R

object JournalSnackbar {

    fun showSuccess(view: View, message: String, action: (() -> Unit)? = null) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(view.context.getColor(R.color.journal_success))
        action?.let {
            snackbar.setAction("Tekrar Dene") { it() }
        }
        snackbar.show()
    }

    fun showError(view: View, message: String, action: (() -> Unit)? = null) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(view.context.getColor(R.color.journal_error))
        action?.let {
            snackbar.setAction("Tekrar Dene") { it() }
        }
        snackbar.show()
    }

    fun showWarning(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(view.context.getColor(R.color.journal_warning))
        snackbar.show()
    }

    fun showInfo(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(view.context.getColor(R.color.journal_info))
        snackbar.show()
    }
}
