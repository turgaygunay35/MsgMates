package com.msgmates.app.ui.journal.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class JournalConfirmationDialog : DialogFragment() {

    interface OnConfirmListener {
        fun onConfirm()
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_POSITIVE_TEXT = "positive_text"
        private const val ARG_NEGATIVE_TEXT = "negative_text"

        fun newInstance(
            title: String,
            message: String,
            positiveText: String = "Evet",
            negativeText: String = "İptal"
        ): JournalConfirmationDialog {
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
                putString(ARG_POSITIVE_TEXT, positiveText)
                putString(ARG_NEGATIVE_TEXT, negativeText)
            }
            return JournalConfirmationDialog().apply { arguments = args }
        }
    }

    private var listener: OnConfirmListener? = null

    fun setOnConfirmListener(listener: OnConfirmListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments ?: Bundle()
        val title = args.getString(ARG_TITLE) ?: ""
        val message = args.getString(ARG_MESSAGE) ?: ""
        val positiveText = args.getString(ARG_POSITIVE_TEXT) ?: "Evet"
        val negativeText = args.getString(ARG_NEGATIVE_TEXT) ?: "İptal"

        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { _, _ ->
                listener?.onConfirm()
            }
            .setNegativeButton(negativeText, null)
            .create()
    }
}
