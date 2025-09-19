package com.msgmates.app.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.msgmates.app.core.analytics.EventLogger
import com.msgmates.app.databinding.BottomsheetFilterOptionsBinding

class FilterOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetFilterOptionsBinding? = null
    private val binding get() = _binding!!

    private var filterId: String = ""
    private var isHidden: Boolean = false
    private var canMoveUp: Boolean = false
    private var canMoveDown: Boolean = false

    private var onMoveUp: (() -> Unit)? = null
    private var onMoveDown: (() -> Unit)? = null
    private var onToggleVisibility: (() -> Unit)? = null

    companion object {
        fun newInstance(
            filterId: String,
            isHidden: Boolean,
            canMoveUp: Boolean,
            canMoveDown: Boolean
        ): FilterOptionsBottomSheet {
            return FilterOptionsBottomSheet().apply {
                this.filterId = filterId
                this.isHidden = isHidden
                this.canMoveUp = canMoveUp
                this.canMoveDown = canMoveDown
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetFilterOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
    }

    private fun setupButtons() {
        // Move Up button
        binding.btnMoveUp.isEnabled = canMoveUp
        binding.btnMoveUp.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            EventLogger.logFilterReorder(filterId, "up")
            onMoveUp?.invoke()
            dismiss()
        }

        // Move Down button
        binding.btnMoveDown.isEnabled = canMoveDown
        binding.btnMoveDown.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            EventLogger.logFilterReorder(filterId, "down")
            onMoveDown?.invoke()
            dismiss()
        }

        // Toggle Visibility button
        binding.btnToggleVisibility.text = if (isHidden) "Göster" else "Gizle"
        binding.btnToggleVisibility.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            EventLogger.logFilterToggleVisibility(filterId, !isHidden)
            onToggleVisibility?.invoke()
            dismiss()
        }
    }

    fun setOnMoveUpListener(listener: () -> Unit) {
        onMoveUp = listener
    }

    fun setOnMoveDownListener(listener: () -> Unit) {
        onMoveDown = listener
    }

    fun setOnToggleVisibilityListener(listener: () -> Unit) {
        onToggleVisibility = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
